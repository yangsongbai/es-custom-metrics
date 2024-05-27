package com.es.monitor.monitor.action;

import com.es.monitor.monitor.stats.NodeCustomIndicesStats;
import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.support.nodes.BaseNodesResponse;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.List;

public class NodesCustomStatsResponse  extends BaseNodesResponse<NodeCustomStats> implements ToXContentFragment {
    protected NodesCustomStatsResponse() {
    }
    public NodesCustomStatsResponse(ClusterName clusterName, List<NodeCustomStats> nodes, List<FailedNodeException> failures) {
        super(clusterName, nodes, failures);
    }

    public NodeCustomIndicesStats getTotal() {
        NodeCustomIndicesStats stats = new NodeCustomIndicesStats();
        for (NodeCustomStats nodeStats : getNodes()) {
            if (nodeStats.getIndices() == null) continue;
            stats.add(nodeStats.getIndices());
        }
        return stats;
    }

    @Override
    protected List<NodeCustomStats> readNodesFrom(StreamInput in) throws IOException {
        return in.readList(NodeCustomStats::readNodeStats);
    }

    @Override
    protected void writeNodesTo(StreamOutput out, List<NodeCustomStats> nodes) throws IOException {
        out.writeStreamableList(nodes);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("_all");
        NodeCustomIndicesStats  nodeCustomIndicesStats = getTotal();
        if (nodeCustomIndicesStats != null) {
            nodeCustomIndicesStats.toXContent(builder, params);
        }
        builder.endObject();
        builder.startObject("nodes");
        for (NodeCustomStats nodeStats : getNodes()) {
            builder.startObject(nodeStats.getNode().getId());
            builder.field("timestamp", nodeStats.getTimestamp());
            nodeStats.toXContent(builder, params);
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    @Override
    public String toString() {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
            builder.startObject();
            toXContent(builder, EMPTY_PARAMS);
            builder.endObject();
            return Strings.toString(builder);
        } catch (IOException e) {
            return "{ \"error\" : \"" + e.getMessage() + "\"}";
        }
    }
}
