package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:05
 */
public class GetRequestHandler extends BaseHandlerReportRequest<ActionRequest, ActionResponse>{

    @Override
    protected Class getComputeType() {
        return GetRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        GetResponse getResponse = (GetResponse) response;
        long ramBytesUsed = 0;
        if (getResponse.getSourceAsBytesRef() != null){
            ramBytesUsed = getResponse.getSourceAsBytesRef().ramBytesUsed();
        }
        AccessResponseInfo accessResponse = new AccessResponseInfo(task,currTime - start);
        accessResponse.setRequestTook(String.valueOf(currTime - start));
        accessResponse.setTimeout(false);
        accessResponse.setRamBytesUsed(ramBytesUsed);
        return accessResponse;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        GetRequest getRequest   = (GetRequest) request;
        String []indices =  getRequest.indices();
        AccessRequestInfo accessRequestInfo = new AccessRequestInfo(0, getRequest.toString(),  0, "");
        accessRequestInfo.addIndicesInfo(indices);
        return accessRequestInfo;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
        getCustomStatsService().success(access.getRequestCost(), access.getActionTask().getTaskCost(),false);
    }
}
