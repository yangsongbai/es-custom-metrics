package com.es.monitor.indices.action;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestActions;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestIndicesCustomStatsAction extends BaseRestHandler {
    private static final Logger logger = LogManager.getLogger(RestIndicesCustomStatsAction.class);

    public RestIndicesCustomStatsAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_nodes/_indices_custom_stats", this);
        controller.registerHandler(GET, "/_nodes/{nodeId}/_indices_custom_stats", this);
        controller.registerHandler(GET, "/_nodes/_indices_custom_stats/{index}", this);
        controller.registerHandler(GET, "/_nodes/{nodeId}/_indices_custom_stats/{index}", this);
    }

    @Override
    public String getName() {
        return "indices_custom_stats_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        String[] nodesIds = Strings.splitStringByCommaToArray(request.param("nodeId"));
        NodesIndicesCustomStatsRequest req = new NodesIndicesCustomStatsRequest(nodesIds);
        req.indices(Strings.splitStringByCommaToArray(request.param("index")));
        boolean clear = request.paramAsBoolean("clear",false);
        req.setClear(clear);
        return channel ->
                client.execute(NodeIndicesCustomStatsAction.INSTANCE, req, new RestActions.NodesResponseRestListener<>(channel));
    }
}
