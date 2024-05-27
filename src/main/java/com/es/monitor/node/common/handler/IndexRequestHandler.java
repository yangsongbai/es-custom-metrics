package com.es.monitor.node.common.handler;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexRequest;


/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:03
 */
public class IndexRequestHandler extends BaseHandlerMetricReportRequest<ActionRequest, ActionResponse> {
    @Override
    protected Class getComputeType() {
        return  IndexRequest.class;
    }
    @Override
    protected void metric(ActionRequest request, ActionResponse response, long costTime, long taskTime) {
        getCustomStatsService().success(costTime, taskTime,false);
    }
}
