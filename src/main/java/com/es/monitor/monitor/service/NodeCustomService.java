package com.es.monitor.monitor.service;

import com.es.monitor.monitor.action.NodeCustomStats;
import com.es.monitor.monitor.stats.CommonCustomStats;
import com.es.monitor.monitor.stats.NodeCustomIndicesStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.node.DiscoveryNode;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/6 16:42
 */
public class NodeCustomService  implements Closeable {
    private static final Logger logger = LogManager.getLogger(NodeCustomService.class);

    private final CustomBulkService customBulkService;
    private final CustomSearchService customSearchService;
    private final CustomUpdateByQueryService customUpdateByQueryService;
    private final CustomDeleteByQueryService customDeleteByQueryService;
    private final CustomGetService customGetService;
    private final CustomIndexService customIndexService;
    private final CustomMultiGetService customMultiGetService;
    private final CustomMultiSearchService customMultiSearchService;

    public NodeCustomService(CustomBulkService customBulkService, CustomSearchService customSearchService,
                             CustomUpdateByQueryService customUpdateByQueryService, CustomDeleteByQueryService customDeleteByQueryService,
                             CustomGetService customGetService, CustomIndexService customIndexService,
                             CustomMultiGetService customMultiGetService, CustomMultiSearchService customMultiSearchService) {
        this.customBulkService = customBulkService;
        this.customSearchService = customSearchService;
        this.customDeleteByQueryService = customDeleteByQueryService;
        this.customUpdateByQueryService = customUpdateByQueryService;
        this.customGetService = customGetService;
        this.customMultiGetService = customMultiGetService;
        this.customIndexService = customIndexService;
        this.customMultiSearchService = customMultiSearchService;
    }

    @Override
    public void close() throws IOException {

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
}
