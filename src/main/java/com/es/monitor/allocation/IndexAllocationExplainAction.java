package com.es.monitor.allocation;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainResponse;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 11:27
 */
public class IndexAllocationExplainAction  extends Action<IndexAllocationExplainRequest,
        ClusterAllocationExplainResponse,
        IndexAllocationExplainRequestBuilder> {
    public static final IndexAllocationExplainAction INSTANCE = new IndexAllocationExplainAction();
    public static final String NAME = "indices:monitor/allocation/explain";

    private IndexAllocationExplainAction() {
        super(NAME);
    }
    @Override
    public ClusterAllocationExplainResponse newResponse() {
        return new ClusterAllocationExplainResponse();
    }

    @Override
    public IndexAllocationExplainRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new IndexAllocationExplainRequestBuilder(client, this);
    }
}