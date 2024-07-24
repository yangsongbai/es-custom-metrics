package com.es.monitor.indices.action;


import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class NodeIndicesCustomStatsAction extends Action<NodesIndicesCustomStatsRequest, NodeIndicesCustomStatsResponse, NodeIndicesCustomStatsRequestBuilder> {

    public static final NodeIndicesCustomStatsAction INSTANCE = new NodeIndicesCustomStatsAction();
    public static final String NAME = "indices:monitor/indices_custom/stats";

    private NodeIndicesCustomStatsAction() {
        super(NAME);
    }

    @Override
    public NodeIndicesCustomStatsResponse newResponse() {
        return new NodeIndicesCustomStatsResponse();
    }

    @Override
    public NodeIndicesCustomStatsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new NodeIndicesCustomStatsRequestBuilder(client, this);
    }
}
