package com.es.monitor.indices.action;



import org.elasticsearch.action.support.nodes.BaseNodesRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;


public class NodesIndicesCustomStatsRequest extends BaseNodesRequest<NodesIndicesCustomStatsRequest> {
    private boolean clear;
    private String[] indices;
    public NodesIndicesCustomStatsRequest() {
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        clear = in.readBoolean();
        indices = in.readOptionalStringArray();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalBoolean(clear);
        out.writeOptionalStringArray(indices);
    }
    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public String[] getIndices() {
        return indices;
    }

    public void indices(String[] indices) {
        this.indices = indices;
    }

    /**
     * Get stats from nodes based on the nodes ids specified. If none are passed, stats
     * for all nodes will be returned.
     */
    public NodesIndicesCustomStatsRequest(String... nodesIds) {
        super(nodesIds);
    }


}
