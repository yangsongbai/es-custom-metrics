package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Collections;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:05
 */
public class MultiSearchRequestHandler extends BaseHandlerReportRequest<ActionRequest, ActionResponse> {
    private static final ToXContent.Params FORMAT_PARAMS = new ToXContent.MapParams(Collections.singletonMap("pretty", "false"));

    @Override
    protected Class getComputeType() {
        return MultiSearchRequest.class;
    }
    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        AccessResponseInfo accessResponse = new AccessResponseInfo(task,currTime - start);

        MultiSearchResponse multiSearchResponse = (MultiSearchResponse) response;
        MultiSearchResponse.Item[] searchResponses= multiSearchResponse.getResponses();
        long took = 0;
        for (MultiSearchResponse.Item hit : searchResponses) {
            SearchResponse searchResponse = hit.getResponse();
            if(searchResponse == null) continue;
            long searchTook =  hit.getResponse().getTook().millis();
            took = took > searchTook ? took : searchTook;
            SearchHits searchHits = searchResponse.getHits();
            for (SearchHit searchHit: searchHits.getHits()){
                long ramBytesUsed = 0;
                if (searchHit !=null && searchHit.getSourceRef() != null){
                    ramBytesUsed = searchHit.getSourceRef().ramBytesUsed();
                }
                accessResponse.addRamBytesUsed(ramBytesUsed);
            }
            if (searchResponse.isTimedOut() == true) {
                accessResponse.setTimeout(true);
            }
        }
        accessResponse.setRequestTook(String.valueOf(took));
        return accessResponse;
    }

    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        MultiSearchRequest multiSearchRequest = (MultiSearchRequest) request;
        AccessRequestInfo accessRequestInfo = new AccessRequestInfo(0, "",  0, "");
       // List<String> source = new ArrayList<>();
        for (SearchRequest searchRequest: multiSearchRequest.requests()){
           // source.add(searchRequest.source().toString(FORMAT_PARAMS));
            accessRequestInfo.addSize(searchRequest.source().size());
            accessRequestInfo.addIndicesInfo(searchRequest.indices());
            if (searchRequest.source().aggregations() != null){
                accessRequestInfo.addAggregation(searchRequest.source().aggregations().count());
            }
        }
        accessRequestInfo.setSource(multiSearchRequest.toString());
        return accessRequestInfo;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
        getCustomStatsService().success(access.getRequestCost(), access.getRequestCost(), access.isTimeout());
    }
}
