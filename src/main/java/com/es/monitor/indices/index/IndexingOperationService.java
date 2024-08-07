package com.es.monitor.indices.index;

import com.es.monitor.indices.IndexMonitorSettings;
import com.es.monitor.indices.service.CustomBigDocService;
import com.es.monitor.indices.service.CustomIndexingSlowLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.cluster.IndicesClusterStateService;

import java.io.IOException;

import static org.elasticsearch.index.IndexingSlowLog.INDEX_INDEXING_SLOWLOG_PREFIX;


public class IndexingOperationService extends AbstractLifecycleComponent implements IndexingOperationListener, IndexEventListener {
    private static final Logger logger = LogManager.getLogger(IndexingOperationService.class);
    private final static String ACTION_DELETE = "del";
    private final static String ACTION_INDEXING = "indexing";

    private  ByteSizeValue thresholdDocSize;
    CustomBigDocService customBigDocService;

    private final CustomIndexingSlowLogService indexingSlowLogService;

    ClusterService clusterService;

    private boolean bigDocLogEnable;
    private boolean bigDocMetricEnable;

    private boolean slowLogMetricEnable;

    public IndexingOperationService(Settings settings,
                                    ClusterService clusterService, CustomBigDocService customBigDocService,
                                    CustomIndexingSlowLogService indexingSlowLogService) {
        thresholdDocSize = IndexMonitorSettings.SETTING_CLUSTER_BIG_DOC_THRESHOLD.get(settings);
        bigDocLogEnable = IndexMonitorSettings.SETTING_BIG_DOC_LOG_ENABLE.get(settings);
        bigDocMetricEnable = IndexMonitorSettings.SETTING_BIG_DOC_METRIC_ENABLE.get(settings);
        slowLogMetricEnable = IndexMonitorSettings.SETTING_SLOW_LOG_METRIC_ENABLE.get(settings);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(IndexMonitorSettings.SETTING_CLUSTER_BIG_DOC_THRESHOLD, this::setThresholdDocSize);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(IndexMonitorSettings.SETTING_BIG_DOC_LOG_ENABLE, this::setBigDocLogEnable);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(IndexMonitorSettings.SETTING_BIG_DOC_METRIC_ENABLE, this::setBigDocMetricEnable);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(IndexMonitorSettings.SETTING_SLOW_LOG_METRIC_ENABLE, this::setSlowLogMetricEnable);
        this.customBigDocService = customBigDocService;
        this.clusterService = clusterService;
        this.indexingSlowLogService = indexingSlowLogService;
    }

    public void setBigDocLogEnable(boolean bigDocLogEnable) {
        this.bigDocLogEnable = bigDocLogEnable;
    }

    public void setBigDocMetricEnable(boolean bigDocMetricEnable) {
        this.bigDocMetricEnable = bigDocMetricEnable;
    }

    public void setThresholdDocSize(ByteSizeValue thresholdDocSize) {
        this.thresholdDocSize = thresholdDocSize;
    }

    public boolean isBigDocLogEnable() {
        return bigDocLogEnable;
    }

    public boolean isBigDocMetricEnable() {
        return bigDocMetricEnable;
    }

    public boolean isSlowLogMetricEnable() {
        return slowLogMetricEnable;
    }

    public void setSlowLogMetricEnable(boolean slowLogMetricEnable) {
        this.slowLogMetricEnable = slowLogMetricEnable;
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void doClose() throws IOException {

    }

    @Override
    public void afterIndexCreated(IndexService indexService) {
        //索引创建，在指标中加入该索引
        IndexMetaData  indexMetaData = indexService.getMetaData();
        if (indexMetaData == null) return;
        Index index = indexMetaData.getIndex();
        if (index == null) return;
        String indexName = index.getName();
        if ("".equals(indexName)) return;
        customBigDocService.initMetric(indexName);
        indexingSlowLogService.initMetric(indexName);
    }

    @Override
    public void afterIndexRemoved(Index index, IndexSettings indexSettings, IndicesClusterStateService.AllocatedIndices.IndexRemovalReason reason) {
       //索引删除从指标中移除掉
        String indexName = index.getName();
        if ("".equals(indexName)) return;
        customBigDocService.removeMetric(indexName);
        indexingSlowLogService.removeMetric(indexName);
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {
         int documentSize = index.estimatedSizeInBytes();
         if(documentSize >= thresholdDocSize.getBytes()) {
             if (isBigDocLogEnable()){
                 logger.warn("index[{}], type:[{}], routing:[{}], docId:[{}], version:[{}], " +
                                 "took:[{}],  seqNo:[{}], term[{}], isCreated:[{}], docSize:[{}]  more than doc threshold size:[{}]",
                         shardId.getIndex().toString(), index.type(), index.routing(), index.id(), result.getVersion(),
                         result.getTook(), result.getSeqNo(),
                         result.getTerm(), result.isCreated(), documentSize, thresholdDocSize.getBytes());
             }
             if (isBigDocMetricEnable()){
                 customBigDocService.incIndexing(shardId.getIndexName());
             }
         }
        recordIndexSlowLogMetric(shardId, result.getTook(), ACTION_DELETE);
    }

    private void recordIndexSlowLogMetric(ShardId shardId, long took, String action) {
        if (!isSlowLogMetricEnable()) {
            return;
        }
        took = nanosToMillis(took);
        String indexName = shardId.getIndexName();
        IndexMetaData indexMetaData = clusterService.state().metaData().getIndices().get(indexName);
        if (indexMetaData != null ){
            Settings settings = indexMetaData.getSettings();
            if (settings != null) {
                long thresholdTime = getIndexingSlowThresholdTime(settings);
                if (thresholdTime > 0 && thresholdTime >= took) {
                    if (ACTION_INDEXING.equals(action)){
                        indexingSlowLogService.incIndexing(indexName, took);
                    } else if (ACTION_DELETE.equals(action)) {
                        indexingSlowLogService.incDelete(indexName, took);
                    }
                    //记录索引的慢日志条数和阈值
                    if (logger.isTraceEnabled() ){
                        logger.trace("index[{}], engine action:[{}] ,记录慢日志指标, thresholdTime[{}ms], took:[{}ms]", shardId.getIndex().toString(), action, thresholdTime, took);
                    }
                 }
            }
        }
    }

    private long getIndexingSlowThresholdTime(Settings settings) {
        long  warn = settings.getAsTime(INDEX_INDEXING_SLOWLOG_PREFIX +".threshold.index.warn", TimeValue.timeValueMillis(-1)).getMillis();
        long  info = settings.getAsTime(INDEX_INDEXING_SLOWLOG_PREFIX +".threshold.index.info", TimeValue.timeValueMillis(-1)).getMillis();
        if (warn > 0 && info > 0) {
            return Math.min(warn, info);
        }
        if (info > 0) return info;
        return warn;
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Exception ex) {
        int documentSize = index.estimatedSizeInBytes();
        if(documentSize > thresholdDocSize.getBytes()) {
            logger.warn("indexing index[{}], type:[{}], routing:[{}], docId:[{}], " +
                            "docSize:[{}] more than doc threshold size:[{}],ex:[{}]",
                    shardId.getIndex().toString(), index.type(), index.routing(),  index.id(), documentSize, thresholdDocSize.getBytes(), ex.toString());
            customBigDocService.incIndexing(shardId.getIndexName());
        }
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {
        int documentSize = delete.estimatedSizeInBytes();
        if(documentSize > thresholdDocSize.getBytes()) {
            if (isBigDocLogEnable()){
                logger.warn("delete index[{}], type:[{}],  docId:[{}], tookInNanos:[{}], " +
                                "docSize:[{}] more than doc threshold size:[{}]",
                        shardId.getIndex().toString(), delete.type(),  delete.id(), result.getTook(), documentSize, thresholdDocSize.getBytes());
            }
            if (isBigDocMetricEnable()) {
                customBigDocService.incDelete(shardId.getIndexName());
            }
        }
        recordIndexSlowLogMetric(shardId, result.getTook(), ACTION_INDEXING);
    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Exception ex) {
        int documentSize = delete.estimatedSizeInBytes();
        if(documentSize > thresholdDocSize.getBytes()) {
            if (isBigDocLogEnable()){
                logger.warn("indexing index[{}], type:[{}], docId:[{}], " +
                                "docSize:[{}] more than doc threshold size:[{}] ,ex:[{}]",
                        shardId.getIndex().toString(), delete.type(),   delete.id(), documentSize, thresholdDocSize.getBytes(), ex.toString());
            }
            if (isBigDocMetricEnable()) {
                customBigDocService.incDelete(shardId.getIndexName());
            }
        }
    }

    private  long nanosToMillis(long tookInNanos) {
        return tookInNanos / 1000000;
    }

}
