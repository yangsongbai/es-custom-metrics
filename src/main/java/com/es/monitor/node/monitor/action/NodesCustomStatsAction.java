package com.es.monitor.node.monitor.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class NodesCustomStatsAction extends Action<NodesCustomStatsRequest, NodesCustomStatsResponse, NodesCustomStatsRequestBuilder> {

    public static final NodesCustomStatsAction INSTANCE = new NodesCustomStatsAction();
    public static final String NAME = "cluster:monitor/nodes/custom/stats";

    private NodesCustomStatsAction() {
        super(NAME);
    }
    @Override
    public NodesCustomStatsResponse newResponse() {
        return new NodesCustomStatsResponse();
    }

    @Override
    public NodesCustomStatsRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new NodesCustomStatsRequestBuilder(client, this);
    }

}
