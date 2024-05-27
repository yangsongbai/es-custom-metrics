package com.es.monitor.node.allocation;

import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainResponse;
import org.elasticsearch.action.support.master.MasterNodeOperationRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/12/5 19:58
 */
public class IndexAllocationExplainRequestBuilder  extends MasterNodeOperationRequestBuilder<IndexAllocationExplainRequest,
        ClusterAllocationExplainResponse,
        IndexAllocationExplainRequestBuilder> {

    public IndexAllocationExplainRequestBuilder(ElasticsearchClient client, IndexAllocationExplainAction action) {
        super(client, action, new IndexAllocationExplainRequest());
    }

    /** The index name to use when finding the shard to explain */
    public IndexAllocationExplainRequestBuilder setIndex(String index) {
        request.setIndex(index);
        return this;
    }

    /** The shard number to use when finding the shard to explain */
    public IndexAllocationExplainRequestBuilder setShard(int shard) {
        return this;
    }

    /** Whether the primary or replica should be explained */
    public IndexAllocationExplainRequestBuilder setPrimary(boolean primary) {
        return this;
    }

    /** Whether to include "YES" decider decisions in the response instead of only "NO" decisions */
    public IndexAllocationExplainRequestBuilder setIncludeYesDecisions(boolean includeYesDecisions) {
        request.includeYesDecisions(includeYesDecisions);
        return this;
    }

    /** Whether to include information about the gathered disk information of nodes in the cluster */
    public IndexAllocationExplainRequestBuilder setIncludeDiskInfo(boolean includeDiskInfo) {
        request.includeDiskInfo(includeDiskInfo);
        return this;
    }

    /**
     * Requests the explain API to explain an already assigned replica shard currently allocated to
     * the given node.
     */
    public IndexAllocationExplainRequestBuilder setCurrentNode(String currentNode) {
        return this;
    }

    /**
     * Signal that the first unassigned shard should be used
     */
    public IndexAllocationExplainRequestBuilder useAnyUnassignedShard() {
        request.setIndex(null);
        return this;
    }
}
