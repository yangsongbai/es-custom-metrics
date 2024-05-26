package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:03
 */
public class UpdateByQueryRequestHandler  extends BaseHandlerReportRequest<ActionRequest, ActionResponse>{
    @Override
    protected Class getComputeType() {
        return UpdateByQueryRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        AccessResponseInfo access = new AccessResponseInfo(task,currTime - start);
        BulkByScrollResponse bulkByScrollResponse = (BulkByScrollResponse) response;
        access.setTimeout(bulkByScrollResponse.isTimedOut());
        access.setRequestTook(String.valueOf(bulkByScrollResponse.getTook().millis()));
        return access;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        UpdateByQueryRequest updateByQueryRequest =  (UpdateByQueryRequest) request;
        AccessRequestInfo access = new AccessRequestInfo(0,updateByQueryRequest.toString(),  0, UpdateByQueryRequest.class.getSimpleName());
        access.addIndicesInfo(updateByQueryRequest.indices());
        return access;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
        getCustomStatsService().success(access.getRequestCost(), access.getActionTask().getTaskCost(), access.isTimeout());
    }

}
