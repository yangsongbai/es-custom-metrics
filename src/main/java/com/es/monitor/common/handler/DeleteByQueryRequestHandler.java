package com.es.monitor.common.handler;

import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/2 11:04
 */
public class DeleteByQueryRequestHandler extends BaseHandlerReportRequest<ActionRequest, ActionResponse> {

    @Override
    protected Class getComputeType() {
        return DeleteByQueryRequest.class;
    }

    @Override
    protected AccessResponseInfo successAccess(ActionRequest request, ActionResponse response, long start, long currTime, ActionTask task) {
        AccessResponseInfo accessResponse = new AccessResponseInfo(task,currTime - start);
        BulkByScrollResponse bulkByScrollResponse =  (BulkByScrollResponse) response;
        accessResponse.setTimeout(bulkByScrollResponse.isTimedOut());
        accessResponse.setRequestTook(String.valueOf(bulkByScrollResponse.getTook().millis()));
        if (bulkByScrollResponse.getBulkFailures().size() > 0){
            accessResponse.setStatus(AccessResponseInfo.Status.PART_FAILED.getDesc());
        } else{
            accessResponse.setStatus(AccessResponseInfo.Status.SUCCESS.getDesc());
        }
        return accessResponse;
    }

    @Override
    protected void metric(ActionRequest request, ActionResponse response, AccessResponseInfo access) {
        getCustomStatsService().success(access.getRequestCost(), access.getActionTask().getTaskCost(), access.isTimeout());
    }


    @Override
    protected AccessRequestInfo buildRequestInfo(ActionRequest request) {
        DeleteByQueryRequest deleteByQueryRequest   = (DeleteByQueryRequest) request;
        SearchRequest searchRequest =  deleteByQueryRequest.getSearchRequest();
        int size = searchRequest.source().size();
        String source = searchRequest.source().toString();
        int  count = searchRequest.source().aggregations().count();
        String []indices =  deleteByQueryRequest.indices();
        String searchType = deleteByQueryRequest.getSearchRequest().searchType().name();
        AccessRequestInfo accessRequestInfo = new AccessRequestInfo(size, source,  count, searchType);
        accessRequestInfo.addIndicesInfo(indices);
        return accessRequestInfo;
    }
}
