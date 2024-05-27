package com.es.monitor.node.common.handler;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 18:09
 */
public class BulkRequestHandler extends BaseHandlerMetricReportRequest<ActionRequest, ActionResponse> {

    @Override
    protected Class getComputeType() {
        return BulkRequest.class;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, long costTime, long taskTime) {
        BulkResponse bulkResponse =  (BulkResponse) response;
        getCustomStatsService().success(costTime, bulkResponse.getTook().millis(),false);
        //部分失败
        if (bulkResponse.hasFailures()){
            getCustomStatsService().partFail();
        }
    }

}
