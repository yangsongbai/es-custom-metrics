package com.es.monitor.node.allocation;

import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 11:29
 */
public class RestIndexAllocationExplainAction extends BaseRestHandler {
    @Override
    public String getName() {
        return "index_allocation_explain_action";
    }

    public RestIndexAllocationExplainAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "/_index/allocation/explain", this);
        controller.registerHandler(RestRequest.Method.POST, "/_index/allocation/explain", this);
        controller.registerHandler(RestRequest.Method.GET, "/{index}/_index/allocation/explain", this);
        controller.registerHandler(RestRequest.Method.POST, "/{index}/_index/allocation/explain", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        IndexAllocationExplainRequest req;
        req = new IndexAllocationExplainRequest();
        String index = request.param("index");
        if (!Strings.isNullOrEmpty(index)) {
            req.setIndex(index);
        }
        req.includeYesDecisions(request.paramAsBoolean("include_yes_decisions", false));
        req.includeDiskInfo(request.paramAsBoolean("include_disk_info", false));
/*        return channel ->
                client.execute(IndexAllocationExplainAction.INSTANCE, req, new RestBuilderListener<>(channel) {
                    @Override
                    public RestResponse buildResponse(ClusterAllocationExplainResponse response, XContentBuilder builder) throws IOException {
                        response.getExplanation().toXContent(builder, ToXContent.EMPTY_PARAMS);
                        return new BytesRestResponse(RestStatus.OK, builder);
                    }
                });*/
        return channel -> client.execute(IndexAllocationExplainAction.INSTANCE, req,
                new RestBuilderListener<ClusterAllocationExplainResponse>(channel) {
                    @Override
                    public RestResponse buildResponse(ClusterAllocationExplainResponse response, XContentBuilder builder) throws IOException {
                        response.getExplanation().toXContent(builder, ToXContent.EMPTY_PARAMS);
                        return new BytesRestResponse(RestStatus.OK, builder);
                    }
                });
    }

}
