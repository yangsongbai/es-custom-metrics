package com.es.monitor.indices.action;


import com.es.monitor.indices.stats.IndicesCustomCommonStats;
import com.es.monitor.indices.stats.NodesIndicesCustomStats;
import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.nodes.BaseNodesResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;

public class NodeIndicesCustomStatsResponse extends BaseNodesResponse<NodesIndicesCustomStats> implements ToXContentFragment {

    public NodeIndicesCustomStatsResponse() {

    }

    @Override
    protected List<NodesIndicesCustomStats> readNodesFrom(StreamInput in) throws IOException {
        return in.readList(NodesIndicesCustomStats::readNodeStats);
    }

    public  NodeIndicesCustomStatsResponse(ClusterName clusterName, List<NodesIndicesCustomStats> nodes, List<FailedNodeException> failures) {
        super(clusterName, nodes, failures);
    }

    @Override
    protected void writeNodesTo(StreamOutput out, List<NodesIndicesCustomStats> nodes) throws IOException {
        out.writeStreamableList(nodes);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("_all");
        IndicesCustomCommonStats indicesCustomCommonStatsTotal = getTotal();
        if (indicesCustomCommonStatsTotal != null) {

            builder.startObject("cluster_total");
            //集群维度统计
            builder =  indicesCustomCommonStatsTotal.toXContentTotal(builder, params);
            builder.endObject();

            builder.startObject("indices");
            //按索引统计
            indicesCustomCommonStatsTotal.toXContent(builder, params);
            builder.endObject();
        }
        builder.endObject();
        builder.startObject("nodes");
        for (NodesIndicesCustomStats nodeStats : getNodes()) {
            builder.startObject(nodeStats.getNode().getId());
            builder.field("timestamp", nodeStats.getTimestamp());
            nodeStats.toXContent(builder, params);
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public IndicesCustomCommonStats getTotal() {
        IndicesCustomCommonStats stats = new IndicesCustomCommonStats();
        for (NodesIndicesCustomStats nodeStats : getNodes()) {
            if (nodeStats.getIndicesCustomCommonStats() == null) continue;
            stats.add(nodeStats.getIndicesCustomCommonStats());
        }
        return stats;
    }
}
