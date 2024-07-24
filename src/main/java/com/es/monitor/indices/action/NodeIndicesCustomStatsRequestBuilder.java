package com.es.monitor.indices.action;


import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class NodeIndicesCustomStatsRequestBuilder extends NodesOperationRequestBuilder<NodesIndicesCustomStatsRequest, NodeIndicesCustomStatsResponse, NodeIndicesCustomStatsRequestBuilder> {

    public NodeIndicesCustomStatsRequestBuilder(ElasticsearchClient client, NodeIndicesCustomStatsAction action) {
        super(client, action, new NodesIndicesCustomStatsRequest());
    }
}
