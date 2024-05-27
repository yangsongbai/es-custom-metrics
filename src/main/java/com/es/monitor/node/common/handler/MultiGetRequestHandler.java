package com.es.monitor.node.common.handler;

import com.es.monitor.node.access.AccessRequestInfo;
import com.es.monitor.node.access.AccessResponseInfo;
import com.es.monitor.node.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;

import java.util.List;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:05
 */
public class MultiGetRequestHandler extends BaseHandlerReportRequest<ActionRequest, ActionResponse> {

    @Override
    protected Class getComputeType() {
        return MultiGetRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        MultiGetResponse multiGetItemResponses = (MultiGetResponse) response;
        AccessResponseInfo accessResponse = new AccessResponseInfo(task,currTime - start);
        for (MultiGetItemResponse getItemResponse: multiGetItemResponses) {
            GetResponse getResponse = getItemResponse.getResponse();
            long ramBytesUsed = 0;
            if (getResponse !=null && getResponse.getSourceAsBytesRef() != null){
                ramBytesUsed = getResponse.getSourceAsBytesRef().ramBytesUsed();
            }
            accessResponse.addRamBytesUsed(ramBytesUsed);
        }
        accessResponse.setRequestTook(String.valueOf(currTime - start));
        accessResponse.setStatus(AccessResponseInfo.Status.SUCCESS.getDesc());
        accessResponse.setTimeout(false);
        return accessResponse;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        MultiGetRequest multiGetRequest = (MultiGetRequest) request;
        List<MultiGetRequest.Item> items =  multiGetRequest.getItems();
        AccessRequestInfo accessRequestInfo = new AccessRequestInfo(0, multiGetRequest.toString(),  0, "");
        for (MultiGetRequest.Item item: items){
            accessRequestInfo.addIndicesInfo(item.indices());
        }
        return accessRequestInfo;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
        getCustomStatsService().success(access.getRequestCost(), access.getActionTask().getTaskCost(),false);
    }
}
