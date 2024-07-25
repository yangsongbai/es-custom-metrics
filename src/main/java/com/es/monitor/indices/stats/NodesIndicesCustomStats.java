package com.es.monitor.indices.stats;

import com.es.monitor.node.monitor.action.NodeCustomStats;
import com.es.monitor.node.monitor.stats.DeleteByQueryStats;
import org.elasticsearch.action.support.nodes.BaseNodeResponse;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;


public class NodesIndicesCustomStats extends BaseNodeResponse implements ToXContentFragment {

    private  IndicesCustomCommonStats stats;
    private long timestamp;


    public NodesIndicesCustomStats() {
    }

    public NodesIndicesCustomStats(DiscoveryNode node, long currentTimeMillis, IndicesCustomCommonStats indicesCustomCommonStats) {
        super(node);
        this.timestamp = currentTimeMillis;
        this.stats = indicesCustomCommonStats;

    }

    public IndicesCustomCommonStats getIndicesCustomCommonStats() {
        return stats;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static NodesIndicesCustomStats readNodeStats(StreamInput in)  throws IOException{
        NodesIndicesCustomStats nodeInfo = new NodesIndicesCustomStats();
        nodeInfo.readFrom(in);
        return nodeInfo;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        this.timestamp = in.readVLong();
        this.stats = in.readOptionalWriteable(IndicesCustomCommonStats::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeVLong(timestamp);
        out.writeOptionalWriteable(stats);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("name", getNode().getName());
        builder.field("host", getNode().getHostName());
        builder.field("ip", getNode().getAddress());

        builder.startArray("roles");
        for (DiscoveryNode.Role role : getNode().getRoles()) {
            builder.value(role.getRoleName());
        }
        builder.endArray();

        builder.startObject("indices_custom_stats");
        if (stats != null) {
            //节点上所有索引的
            builder.startObject("node_total");
            stats.toXContentTotal(builder, params);
            builder.endObject();

            builder.startObject("indices");
            stats.toXContent(builder, params);
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public void add(NodesIndicesCustomStats indices) {
        this.stats.add(indices.stats);
    }
}
