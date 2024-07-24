package com.es.monitor.node.monitor.action;

import com.es.monitor.node.monitor.stats.NodeCustomIndicesStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.nodes.BaseNodeResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import java.io.IOException;
import java.util.Map;


public class NodeCustomStats extends BaseNodeResponse implements ToXContentFragment {
    private long timestamp;

    @Nullable
    private NodeCustomIndicesStats indices;
    protected NodeCustomStats() {}

    public NodeCustomStats(DiscoveryNode node, long timestamp,
                           @Nullable NodeCustomIndicesStats indices) {
        super(node);
        this.timestamp = timestamp;
        this.indices = indices;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public NodeCustomIndicesStats getIndices() {
        return indices;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVLong(timestamp);
        out.writeOptionalWriteable(indices);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {

        builder.field("name", getNode().getName());
        builder.field("transport_address", getNode().getAddress().toString());
        builder.field("host", getNode().getHostName());
        builder.field("ip", getNode().getAddress());

        builder.startArray("roles");
        for (DiscoveryNode.Role role : getNode().getRoles()) {
            builder.value(role.getRoleName());
        }
        builder.endArray();

        if (!getNode().getAttributes().isEmpty()) {
            builder.startObject("attributes");
            for (Map.Entry<String, String> attrEntry : getNode().getAttributes().entrySet()) {
                builder.field(attrEntry.getKey(), attrEntry.getValue());
            }
            builder.endObject();
        }
        if (getIndices() != null) {
            getIndices().toXContent(builder, params);
        }

        return builder;
    }

    public static  NodeCustomStats readNodeStats(StreamInput in)  throws IOException{
        NodeCustomStats nodeInfo = new NodeCustomStats();
        nodeInfo.readFrom(in);
        return nodeInfo;
    }
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        timestamp = in.readVLong();
        indices = in.readOptionalWriteable(NodeCustomIndicesStats::new);
    }
}
