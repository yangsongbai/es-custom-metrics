package com.es.monitor.common.logging;

import com.es.monitor.access.AccessSettings;
import com.es.monitor.access.RemoteInfo;
import com.es.monitor.access.RemoteInfoContextSerializer;
import com.es.monitor.access.RestAccessTrailService;
import com.es.monitor.common.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:30
 */
public class RestOperatorLogHandler implements RestHandler {
    private static final Logger logger = LogManager.getLogger(RestOperatorLogHandler.class);

    private final RestHandler nextHandler;
    private final ThreadContext threadContext;
    private final RestAccessTrailService restAccessTrailService;
    private final AccessSettings accessSettings;
    private RemoteInfoContextSerializer remoteInfoContextSerializer;
    public RestOperatorLogHandler(RestAccessTrailService restAccessTrailService, RestHandler original, ThreadContext threadContext,  AccessSettings accessSettings, RemoteInfoContextSerializer remoteInfoContextSerializer) {
        this.nextHandler = original;
        this.threadContext = threadContext;
        this.restAccessTrailService = restAccessTrailService;
        this.accessSettings = accessSettings;
        this.remoteInfoContextSerializer = remoteInfoContextSerializer;
    }

    /**
     *
     * {
     * 	"type": "access",
     * 	"level": "INFO",
     * 	"timestamp": "2022-11-09T12:27:20,493+0800",
     * 	"cluster": "elasticsearch",
     * 	"node.name": "ZBMac-K73JKM6DN",
     * 	"node.id": "PbWbBRhBSZGHR-TTXHLJ1w",
     * 	"host.name": "127.0.0.1",
     * 	"host.ip": "127.0.0.1",
     * 	"uri": "/.kibana_test/_search?pretty=true",
     * 	"path": "/.kibana_test/_search",
     * 	"source": "{\n  \"query\": {\n    \"match_all\": {}\n  }\n}\n",
     * 	"request_id": "14",
     * 	"method": "GET",
     * 	"ram_bytes_used": "41",
     * 	"content_length": "41",
     * 	"remote.address": "/127.0.0.1:51401"
     * }
     */
    @Override
    public void handleRequest(RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        threadContext.putHeader(Constant.REMOTE_REST, String.valueOf(true));
        //记录http链接，close response之后是否立即关闭，http1.0 keep-alive
        //boolean close =  HttpUtils.shouldCloseConnection(request.getHttpRequest());
        //request.getHttpChannel().getRemoteAddress().toString()
        //threadContext.putHeader(Constant.JES_CONNECTED_CLOSE,String.valueOf(close));
        if (this.accessSettings.getAccessLogEnable()) {
            recordAccessLog(request, "");
        }
        nextHandler.handleRequest(request,channel,client);
    }

    private void recordAccessLog(RestRequest request, String user) {
        try {
            RemoteInfo remoteInfo = new RemoteInfo(request.getRemoteAddress().toString(),true, String.valueOf(System.currentTimeMillis()),
                    request.uri(), request.method().name(),user);
            this.remoteInfoContextSerializer.writeToContext(remoteInfo, threadContext);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
