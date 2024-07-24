package com.es.monitor.indices.service;

import com.es.monitor.indices.stats.BigDocStats;
import com.es.monitor.indices.stats.IndicesCustomCommonStats;
import com.es.monitor.indices.stats.NodesIndicesCustomStats;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.component.AbstractLifecycleComponent;

import java.io.Closeable;
import java.io.IOException;


public class NodeIndicesCustomService  extends AbstractLifecycleComponent implements Closeable {
    private final CustomBigDocService customBigDocService;

    public NodeIndicesCustomService(CustomBigDocService customBigDocService) {
        this.customBigDocService = customBigDocService;
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

    public void clear() {
        customBigDocService.clear();
    }

    public NodesIndicesCustomStats stats(DiscoveryNode localNode, String[] indices) {
        boolean all = false;
        if (indices == null || indices.length == 0) {
            all = true;
        } else  {
            for (String index : indices) {
                if ("_all".equals(index)) {
                    all = true;
                    break;
                }
            }
        }
        BigDocStats bigDocStats = customBigDocService.stats(indices, all);
        return new NodesIndicesCustomStats(localNode, System.currentTimeMillis(), new IndicesCustomCommonStats(bigDocStats));
    }
}
