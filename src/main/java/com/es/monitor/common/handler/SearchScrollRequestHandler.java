package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/28 21:10
 */
public class SearchScrollRequestHandler <Request extends ActionRequest, Response extends ActionResponse> extends BaseHandlerReportRequest<ActionRequest, ActionResponse>{
    @Override
    protected Class getComputeType() {
        return SearchScrollRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        AccessResponseInfo access = new AccessResponseInfo(task,currTime - start);
        SearchResponse searchResponse = (SearchResponse) response;
        access.setSearchHits(String.valueOf(searchResponse.getHits().getTotalHits()));
        access.setRequestTook(String.valueOf(searchResponse.getTook().millis()));
        access.setScrollId(searchResponse.getScrollId());
        return access;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        SearchScrollRequest searchScrollRequest = (SearchScrollRequest) request;
        AccessRequestInfo access = new AccessRequestInfo();
        access.setSource(searchScrollRequest.toString());
        access.setScrollId(searchScrollRequest.scrollId());
        return access;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
    }
}
