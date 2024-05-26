package com.es.monitor.allocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainAction;
import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainRequest;
import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainResponse;
import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplanation;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.ClusterInfo;
import org.elasticsearch.cluster.ClusterInfoService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.allocation.AllocateUnassignedDecision;
import org.elasticsearch.cluster.routing.allocation.MoveDecision;
import org.elasticsearch.cluster.routing.allocation.RoutingAllocation;
import org.elasticsearch.cluster.routing.allocation.ShardAllocationDecision;
import org.elasticsearch.cluster.routing.allocation.allocator.ShardsAllocator;
import org.elasticsearch.cluster.routing.allocation.decider.AllocationDeciders;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.gateway.GatewayAllocator;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.List;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 11:29
 */
public class TransportIndexAllocationExplainAction  extends TransportMasterNodeAction<IndexAllocationExplainRequest, ClusterAllocationExplainResponse> {


    private static final Logger logger = LogManager.getLogger(TransportIndexAllocationExplainAction.class);

    private final ClusterInfoService clusterInfoService;
    private final AllocationDeciders allocationDeciders;
    private final ShardsAllocator shardAllocator;
    private final GatewayAllocator gatewayAllocator;

    @Inject
    public TransportIndexAllocationExplainAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                                 ThreadPool threadPool, ActionFilters actionFilters,
                                                 IndexNameExpressionResolver indexNameExpressionResolver,
                                                 ClusterInfoService clusterInfoService, AllocationDeciders allocationDeciders,
                                                 ShardsAllocator shardAllocator, GatewayAllocator gatewayAllocator) {
        super(settings, IndexAllocationExplainAction.NAME, transportService, clusterService, threadPool, actionFilters,
                indexNameExpressionResolver, IndexAllocationExplainRequest::new);
        this.clusterInfoService = clusterInfoService;
        this.allocationDeciders = allocationDeciders;
        this.shardAllocator = shardAllocator;
        this.gatewayAllocator = gatewayAllocator;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.MANAGEMENT;
    }

    @Override
    protected ClusterAllocationExplainResponse newResponse() {
        return new ClusterAllocationExplainResponse();
    }


    @Override
    protected void masterOperation(IndexAllocationExplainRequest request, ClusterState state, ActionListener<ClusterAllocationExplainResponse> listener) throws Exception {
        final RoutingNodes routingNodes = state.getRoutingNodes();
        final ClusterInfo clusterInfo = clusterInfoService.getClusterInfo();
        final RoutingAllocation allocation = new RoutingAllocation(allocationDeciders, routingNodes, state,
                clusterInfo, System.nanoTime());

        ShardRouting shardRouting = findShardToExplain(request, allocation);
        logger.debug("explaining the allocation for [{}], found shard [{}]", request, shardRouting);

        ClusterAllocationExplanation cae = explainShard(shardRouting, allocation,
                request.includeDiskInfo() ? clusterInfo : null, request.includeYesDecisions(), gatewayAllocator,shardAllocator);
        listener.onResponse(new ClusterAllocationExplainResponse(cae));
    }


    // public for testing
    public static ClusterAllocationExplanation explainShard(ShardRouting shardRouting, RoutingAllocation allocation, ClusterInfo clusterInfo, boolean includeYesDecisions, GatewayAllocator gatewayAllocator, ShardsAllocator shardAllocator) {
        allocation.setDebugMode(includeYesDecisions ? RoutingAllocation.DebugMode.ON : RoutingAllocation.DebugMode.EXCLUDE_YES_DECISIONS);

        ShardAllocationDecision shardDecision;
        if (shardRouting.initializing() || shardRouting.relocating()) {
            shardDecision = ShardAllocationDecision.NOT_TAKEN;
        } else {
            AllocateUnassignedDecision allocateDecision = shardRouting.unassigned() ?
                    gatewayAllocator.decideUnassignedShardAllocation(shardRouting, allocation) : AllocateUnassignedDecision.NOT_TAKEN;
            if (allocateDecision.isDecisionTaken() == false) {
                shardDecision = shardAllocator.decideShardAllocation(shardRouting, allocation);
            } else {
                shardDecision = new ShardAllocationDecision(allocateDecision, MoveDecision.NOT_TAKEN);
            }
        }


        return new ClusterAllocationExplanation(shardRouting,
                shardRouting.currentNodeId() != null ? allocation.nodes().get(shardRouting.currentNodeId()) : null,
                shardRouting.relocatingNodeId() != null ? allocation.nodes().get(shardRouting.relocatingNodeId()) : null,
                clusterInfo, shardDecision);
    }

    // public for testing
    public static  ShardRouting findShardToExplain(IndexAllocationExplainRequest request, RoutingAllocation allocation) {
        ShardRouting foundShard = null;
        if (request.useAnyUnassignedShard()) {
            // If we can use any shard, just pick the first unassigned one (if there are any)
            RoutingNodes.UnassignedShards.UnassignedIterator ui = allocation.routingNodes().unassigned().iterator();
            if (ui.hasNext()) {
                foundShard = ui.next();
            }
            if (foundShard == null) {
                throw new IllegalArgumentException("unable to find any unassigned shards to explain [" + request + "]");
            }
        } else {
            String index = request.getIndex();
            List<ShardRouting> shardRoutingList = allocation.routingTable().allShards(index);
            int primaryShardsUnassigned = allocation.routingTable().index(index).primaryShardsUnassigned();
            for (ShardRouting shardRouting:shardRoutingList){
                if (shardRouting.unassigned()){
                    if (shardRouting.primary()) {
                        foundShard = shardRouting;
                        break;
                    }
                    if (primaryShardsUnassigned > 0) continue;
                    foundShard = shardRouting;
                    break;
                }
            }
            if (foundShard == null) {
                throw new IllegalArgumentException(
                        "No shard was specified in the request which means the response should explain a randomly-chosen unassigned shard, "
                                + "but there are no unassigned shards in index:[" +request.getIndex()+"]");
            }
        }
        return foundShard;
    }

    @Override
    protected ClusterBlockException checkBlock(IndexAllocationExplainRequest request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_READ);
    }
}

