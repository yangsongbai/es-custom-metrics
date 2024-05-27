package com.es.monitor.node.monitor.action;

import com.es.monitor.node.monitor.service.NodeCustomService;
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
import java.util.Set;

public class TransportNodesCustomStatsAction  extends TransportNodesAction<NodesCustomStatsRequest,
        NodesCustomStatsResponse,
        TransportNodesCustomStatsAction.NodeCustomStatsRequest,
        NodeCustomStats> {
    private static final Logger logger = LogManager.getLogger(TransportNodesCustomStatsAction.class);

    private final NodeCustomService nodeCustomService;
    private final TransportService transportService;

    @Inject
    public TransportNodesCustomStatsAction(Settings settings, ThreadPool threadPool,
                                           ClusterService clusterService, TransportService transportService,
                                           NodeCustomService nodeCustomService, ActionFilters actionFilters,
                                           IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, NodesCustomStatsAction.NAME, threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, NodesCustomStatsRequest::new, NodeCustomStatsRequest::new, ThreadPool.Names.MANAGEMENT, NodeCustomStats.class);
        this.nodeCustomService = nodeCustomService;
        this.transportService = transportService;
    }

    @Override
    protected NodesCustomStatsResponse newResponse(NodesCustomStatsRequest request, List<NodeCustomStats> responses, List<FailedNodeException> failures) {
        return new NodesCustomStatsResponse(clusterService.getClusterName(), responses, failures);
    }

    @Override
    protected NodeCustomStatsRequest newNodeRequest(String nodeId, NodesCustomStatsRequest request) {
        return new NodeCustomStatsRequest(nodeId,request);
    }

    @Override
    protected NodeCustomStats newNodeResponse() {
        return new NodeCustomStats();
    }

    @Override
    protected NodeCustomStats nodeOperation(NodeCustomStatsRequest nodeCustomStatsRequest) {

        NodesCustomStatsRequest request = nodeCustomStatsRequest.request;
        if (request.isClear()){
            nodeCustomService.clear();
        }
        Set<String> metrics = request.requestedMetrics();
        NodeCustomStats  stats = nodeCustomService.stats(transportService.getLocalNode(),
                NodesCustomStatsRequest.Metric.BULK.containedIn(metrics),
                NodesCustomStatsRequest.Metric.SEARCH.containedIn(metrics));
        return stats;
    }

    public  static class NodeCustomStatsRequest extends BaseNodeRequest {
        NodesCustomStatsRequest request;

        public NodeCustomStatsRequest() {
            request = new NodesCustomStatsRequest();
        }

        NodeCustomStatsRequest(String nodeId, NodesCustomStatsRequest request) {
            super(nodeId);
            this.request = request;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            request = new NodesCustomStatsRequest();
            request.readFrom(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            request.writeTo(out);
        }
    }
}
