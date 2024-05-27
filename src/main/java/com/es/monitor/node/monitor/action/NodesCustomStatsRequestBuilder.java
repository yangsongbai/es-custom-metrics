package com.es.monitor.node.monitor.action;

import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

public class NodesCustomStatsRequestBuilder  extends NodesOperationRequestBuilder<NodesCustomStatsRequest, NodesCustomStatsResponse, NodesCustomStatsRequestBuilder> {
    public NodesCustomStatsRequestBuilder(ElasticsearchClient client, NodesCustomStatsAction action) {
        super(client, action, new NodesCustomStatsRequest());
    }
    /**
     * Sets all the request flags.
     */
    public NodesCustomStatsRequestBuilder all() {
        request.all();
        return this;
    }
    /**
     * Clears all stats flags.
     */
    public NodesCustomStatsRequestBuilder clear() {
        return this;
    }

}
