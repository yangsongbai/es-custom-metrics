package com.es.monitor.monitor.action;

import org.elasticsearch.action.support.nodes.NodesOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/12/5 20:12
 */
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
