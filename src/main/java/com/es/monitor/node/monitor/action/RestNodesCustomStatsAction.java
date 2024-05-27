package com.es.monitor.node.monitor.action;


import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;

import java.io.IOException;

public class RestNodesCustomStatsAction extends BaseRestHandler {
    @Override
    public String getName() {
        return "nodes_custom_stats_action";
    }

    public RestNodesCustomStatsAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "/_nodes/custom/stats", this);
        controller.registerHandler(RestRequest.Method.POST, "/_nodes/custom/stats", this);
        controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/custom/stats", this);
        controller.registerHandler(RestRequest.Method.GET, "/_nodes/custom/stats/{metric}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_nodes/{nodeId}/custom/stats/{metric}", this);

    }
    /**
     *  POST /_nodes/custom/stats?clear=true
     *  {
     *      "clear": true
     *  }
     */
    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        boolean clear = request.paramAsBoolean("clear",false);
        NodesCustomStatsRequest req = new NodesCustomStatsRequest();
        req.setClear(clear);
        return channel ->
                client.execute(NodesCustomStatsAction.INSTANCE, req, new RestActions.NodesResponseRestListener<>(channel));
    }
}
