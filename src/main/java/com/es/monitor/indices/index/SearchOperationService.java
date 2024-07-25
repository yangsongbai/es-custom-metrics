package com.es.monitor.indices.index;

import com.es.monitor.indices.IndexMonitorSettings;
import com.es.monitor.indices.service.CustomSlowSearchLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.shard.IndexEventListener;
import org.elasticsearch.index.shard.SearchOperationListener;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.indices.cluster.IndicesClusterStateService;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;


public class SearchOperationService extends AbstractLifecycleComponent implements SearchOperationListener, IndexEventListener {
    private static final Logger logger = LogManager.getLogger(SearchOperationService.class);

    private final static String ACTION_QUERY_SUC = "query_suc";

    private final static String ACTION_QUERY_FAIL = "query_fail";

    private final static String ACTION_FETCH_SUC = "fetch_suc";

    private final static String ACTION_FETCH_FAIL = "fetch_fail";

    ClusterService clusterService;
    CustomSlowSearchLogService slowSearchLogService;

    private boolean slowLogMetricEnable;
    public SearchOperationService(Settings settings,
                                    ClusterService clusterService, CustomSlowSearchLogService slowSearchLogService) {
        slowLogMetricEnable = IndexMonitorSettings.SETTING_SLOW_LOG_METRIC_ENABLE.get(settings);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(IndexMonitorSettings.SETTING_SLOW_LOG_METRIC_ENABLE, this::setSlowLogMetricEnable);
        this.slowSearchLogService = slowSearchLogService;
        this.clusterService = clusterService;
    }

    public void setSlowLogMetricEnable(boolean slowLogMetricEnable) {
        this.slowLogMetricEnable = slowLogMetricEnable;
    }

    public boolean isSlowLogMetricEnable() {
        return slowLogMetricEnable;
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
        slowSearchLogService.initMetric(indexName);
    }

    @Override
    public void afterIndexRemoved(Index index, IndexSettings indexSettings, IndicesClusterStateService.AllocatedIndices.IndexRemovalReason reason) {
        //索引删除从指标中移除掉
        String indexName = index.getName();
        if ("".equals(indexName)) return;
        slowSearchLogService.removeMetric(indexName);
    }

    @Override
    public void onFailedQueryPhase(SearchContext searchContext) {
        long took = System.currentTimeMillis() - searchContext.getTask().getStartTime();
        recordIndexSearchSlowLogMetric(searchContext.indexShard().shardId(), took, ACTION_QUERY_FAIL);
    }

    @Override
    public void onQueryPhase(SearchContext searchContext, long tookInNanos) {
        recordIndexSearchSlowLogMetric(searchContext.indexShard().shardId(), nanosToMillis(tookInNanos), ACTION_QUERY_SUC);
    }

    private  long nanosToMillis(long tookInNanos) {
        return tookInNanos / 1000000;
    }

    @Override
    public void onFetchPhase(SearchContext searchContext, long tookInNanos) {
        recordIndexSearchSlowLogMetric(searchContext.indexShard().shardId(), nanosToMillis(tookInNanos), ACTION_FETCH_SUC);
    }

    @Override
    public void onFailedFetchPhase(SearchContext searchContext) {
       long took = System.currentTimeMillis() - searchContext.getTask().getStartTime();
        recordIndexSearchSlowLogMetric(searchContext.indexShard().shardId(), took, ACTION_QUERY_FAIL);
    }

    private void recordIndexSearchSlowLogMetric(ShardId shardId, long took, String action) {
        if (!isSlowLogMetricEnable()) {
            return;
        }
        String indexName = shardId.getIndexName();
        IndexMetaData indexMetaData = clusterService.state().metaData().getIndices().get(indexName);
        if (indexMetaData != null ){
            Settings settings = indexMetaData.getSettings();
            if (settings == null) return;
            if (ACTION_QUERY_SUC.equals(action) || ACTION_QUERY_FAIL.equals(action)){
                recordIndexQuerySlowLogMetric(shardId, took, action, settings, indexName);
            } else if (ACTION_FETCH_SUC.equals(action) || ACTION_FETCH_FAIL.equals(action)) {
                recordIndexFetchSlowLogMetric(shardId, took, action, settings, indexName);
            }
        }
    }

    private void recordIndexFetchSlowLogMetric(ShardId shardId, long took, String action, Settings settings, String indexName) {
        //todo 指标修正
        long  thresholdTime = settings.getAsTime( "index.search.slowlog.threshold.fetch.warn", TimeValue.timeValueMillis(-1)).getMillis();
        if (thresholdTime > 0 && thresholdTime >= took) {
            if (ACTION_FETCH_SUC.equals(action)) {
                slowSearchLogService.incFetchSuc(indexName, took);
            }
            if (ACTION_FETCH_FAIL.equals(action)) {
                slowSearchLogService.incFetchFail(indexName, took);
            }
            //记录索引的慢日志条数和阈值
            if (logger.isTraceEnabled() ){
                logger.trace("index[{}], engine action:[{}] ,记录慢日志指标, thresholdTime[{}], took:[{}]", shardId.getIndex().toString(), action, thresholdTime, took);
            }
        }
    }

    private void recordIndexQuerySlowLogMetric(ShardId shardId, long took, String action, Settings settings, String indexName) {
        //todo 指标修正
        long  thresholdTime = settings.getAsTime( "index.search.slowlog.threshold.query.warn", TimeValue.timeValueMillis(-1)).getMillis();
        if (thresholdTime > 0 && thresholdTime >= took) {
            if (ACTION_QUERY_SUC.equals(action)){
                slowSearchLogService.incQuerySuc(indexName, took);
            }
            if (ACTION_QUERY_FAIL.equals(action)) {
                slowSearchLogService.incQueryFail(indexName, took);
            }
            //记录索引的慢日志条数和阈值
            if (logger.isTraceEnabled() ){
                logger.trace("index[{}], engine action:[{}] ,记录慢查日志指标 , thresholdTime[{}ms], took:[{}ms]", shardId.getIndex().toString(), action, thresholdTime, took);
            }
        }
    }

}
