package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.action.NodeCustomStats;
import com.es.monitor.node.monitor.stats.CommonCustomStats;
import com.es.monitor.node.monitor.stats.NodeCustomIndicesStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.Scheduler;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.Closeable;
import java.io.IOException;


public class NodeCustomService  extends AbstractLifecycleComponent implements Closeable {
    private static final Logger logger = LogManager.getLogger(NodeCustomService.class);

    private final CustomBulkService customBulkService;
    private final CustomSearchService customSearchService;
    private final CustomUpdateByQueryService customUpdateByQueryService;
    private final CustomDeleteByQueryService customDeleteByQueryService;
    private final CustomGetService customGetService;
    private final CustomIndexService customIndexService;
    private final CustomMultiGetService customMultiGetService;
    private final CustomMultiSearchService customMultiSearchService;

    private final Scheduler.Cancellable fillUpdater;


    public NodeCustomService(CustomBulkService customBulkService, CustomSearchService customSearchService,
                             CustomUpdateByQueryService customUpdateByQueryService, CustomDeleteByQueryService customDeleteByQueryService,
                             CustomGetService customGetService, CustomIndexService customIndexService,
                             CustomMultiGetService customMultiGetService, CustomMultiSearchService customMultiSearchService, ThreadPool threadPool) {
        this.customBulkService = customBulkService;
        this.customSearchService = customSearchService;
        this.customDeleteByQueryService = customDeleteByQueryService;
        this.customUpdateByQueryService = customUpdateByQueryService;
        this.customGetService = customGetService;
        this.customMultiGetService = customMultiGetService;
        this.customIndexService = customIndexService;
        this.customMultiSearchService = customMultiSearchService;
        this.fillUpdater = threadPool.scheduleWithFixedDelay(new MonitorMetricUpdater(), TimeValue.timeValueSeconds(1L), ThreadPool.Names.GENERIC);
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {
        fillUpdater.cancel();
    }

    @Override
    public void close()  {

    }

    @Override
    protected void doClose() throws IOException {

    }

    public NodeCustomStats stats(DiscoveryNode localNode, boolean bulk, boolean search) {
        CommonCustomStats customStats = new CommonCustomStats(customBulkService.stats(),customSearchService.stats(),
                this.customDeleteByQueryService.stats(),this.customUpdateByQueryService.stats(),this.customGetService.stats()
                ,this.customMultiGetService.stats(),this.customIndexService.stats(), this.customMultiSearchService.stats());
        return new NodeCustomStats(localNode, System.currentTimeMillis(), new NodeCustomIndicesStats(customStats));
    }

    public void clear() {
        this.customBulkService.clear();
        this.customSearchService.clear();
        this.customDeleteByQueryService.clear();
        this.customUpdateByQueryService.clear();
        this.customGetService.clear();
        this.customMultiGetService.clear();
        this.customIndexService.clear();
        this.customMultiSearchService.clear();
    }

    private class MonitorMetricUpdater implements Runnable {
        @Override
        public void run() {
            fillMetricData();
        }
    }
    private void fillMetricData() {
        this.customBulkService.fillEmptyData();
        this.customSearchService.fillEmptyData();
        this.customDeleteByQueryService.fillEmptyData();
        this.customUpdateByQueryService.fillEmptyData();
        this.customGetService.fillEmptyData();
        this.customMultiGetService.fillEmptyData();
        this.customIndexService.fillEmptyData();
        this.customMultiSearchService.fillEmptyData();
    }
}
