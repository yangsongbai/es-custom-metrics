package com.es.monitor.indices.action;


import com.es.monitor.indices.service.NodeIndicesCustomService;
import com.es.monitor.indices.stats.NodesIndicesCustomStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.FailedNodeException;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.nodes.BaseNodeRequest;
import org.elasticsearch.action.support.nodes.TransportNodesAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.List;

public class TransportNodeIndicesCustomStatsAction extends TransportNodesAction<NodesIndicesCustomStatsRequest, NodeIndicesCustomStatsResponse,
        TransportNodeIndicesCustomStatsAction.NodeIndicesCustomStatsRequest,
        NodesIndicesCustomStats> {

    private static final Logger logger = LogManager.getLogger(TransportNodeIndicesCustomStatsAction.class);

    private final TransportService transportService;
    private final NodeIndicesCustomService nodeIndicesCustomService;

    @Inject
    public TransportNodeIndicesCustomStatsAction(Settings settings, ThreadPool threadPool,
                                                 ClusterService clusterService, TransportService transportService,
                                                 ActionFilters actionFilters,
                                                 IndexNameExpressionResolver indexNameExpressionResolver, NodeIndicesCustomService nodeIndicesCustomService) {
        super(settings, NodeIndicesCustomStatsAction.NAME, threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, NodesIndicesCustomStatsRequest::new, TransportNodeIndicesCustomStatsAction.NodeIndicesCustomStatsRequest::new,
                ThreadPool.Names.MANAGEMENT, NodesIndicesCustomStats.class);
        this.transportService = transportService;
        this.nodeIndicesCustomService = nodeIndicesCustomService;
    }

    @Override
    protected NodeIndicesCustomStatsResponse newResponse(NodesIndicesCustomStatsRequest nodesRequest, List<NodesIndicesCustomStats> responses, List<FailedNodeException> failures) {
        return new NodeIndicesCustomStatsResponse(clusterService.getClusterName(), responses, failures);
    }

    @Override
    protected NodeIndicesCustomStatsRequest newNodeRequest(String nodeId, NodesIndicesCustomStatsRequest request) {
        return new NodeIndicesCustomStatsRequest(nodeId, request);
    }

    @Override
    protected NodesIndicesCustomStats newNodeResponse() {
        return new NodesIndicesCustomStats();
    }

    @Override
    protected NodesIndicesCustomStats nodeOperation(NodeIndicesCustomStatsRequest nodeRequest) {
        NodesIndicesCustomStatsRequest request = nodeRequest.request;
        if (request.isClear()){
            nodeIndicesCustomService.clear();
        }
        return nodeIndicesCustomService.stats(transportService.getLocalNode(), request.getIndices());
    }

    public  static class NodeIndicesCustomStatsRequest extends BaseNodeRequest {
        NodesIndicesCustomStatsRequest request;

        public NodeIndicesCustomStatsRequest() {
            request = new NodesIndicesCustomStatsRequest();
        }

        NodeIndicesCustomStatsRequest(String nodeId, NodesIndicesCustomStatsRequest request) {
            super(nodeId);
            this.request = request;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            request = new NodesIndicesCustomStatsRequest();
            request.readFrom(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            request.writeTo(out);
        }
    }
}
