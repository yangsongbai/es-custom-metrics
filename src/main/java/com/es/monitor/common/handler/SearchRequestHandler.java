package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.ToXContent;

import java.util.Collections;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:04
 */
public class SearchRequestHandler<Request extends ActionRequest, Response extends ActionResponse> extends BaseHandlerReportRequest<ActionRequest, ActionResponse> {
    private static final ToXContent.Params FORMAT_PARAMS = new ToXContent.MapParams(Collections.singletonMap("pretty", "false"));

    @Override
    protected Class getComputeType() {
        return SearchRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        AccessResponseInfo accessResponse = new AccessResponseInfo(task,currTime - start);
        SearchResponse searchResponse = (SearchResponse) response;
        accessResponse.setSearchHits(String.valueOf(searchResponse.getHits().getTotalHits()));
        accessResponse.setRequestTook(String.valueOf(searchResponse.getTook().millis()));
        return accessResponse;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        SearchRequest searchRequest = (SearchRequest) request;
        int aggregations = 0 ;
        if (searchRequest.source().aggregations() != null) aggregations = searchRequest.source().aggregations().count();
        AccessRequestInfo access = new AccessRequestInfo(searchRequest.source().size(), searchRequest.source().toString(FORMAT_PARAMS),  aggregations, searchRequest.searchType().name());
        access.addIndicesInfo(searchRequest.indices());
        return access;
    }

    @Override
    protected void metric(ActionRequest request,ActionResponse response, AccessResponseInfo access) {
        SearchResponse searchResponse = (SearchResponse) response;
        getCustomStatsService().success(access.getRequestCost(), searchResponse.getTook().millis(), searchResponse.isTimedOut());
    }
}
