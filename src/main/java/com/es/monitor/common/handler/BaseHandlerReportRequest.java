package com.es.monitor.common.handler;


import com.es.monitor.access.AccessRequestInfo;
import com.es.monitor.access.AccessResponseInfo;
import com.es.monitor.access.ActionTask;
import com.es.monitor.access.RemoteInfo;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.tasks.Task;

public abstract class BaseHandlerReportRequest<Request extends ActionRequest, Response extends ActionResponse> extends BaseHandler<Request, Response> {

    protected abstract  AccessResponseInfo successAccess(Request request, Response response, long start, long currTime, ActionTask task);
    protected  abstract  AccessRequestInfo buildRequestInfo(Request request);
    protected abstract  void metric(Request request, Response response, AccessResponseInfo access);

    @Override
    protected void success(Request request, Response response, long start, long currTime,  Task task, RemoteInfo remoteInfo) {
        ActionTask actionTask =  new ActionTask(task.getId()+"",currTime - start,task.getStartTime(),task.getAction());
        AccessResponseInfo access = successAccess(request, response, start, currTime , actionTask);

        if (this.getAccessSettings().getAccessMetricEnable() == true) {
            metric(request,response,access);
        }
        if (this.getAccessSettings().getAccessLogEnable() == true) {
            getAccessTrailServiceReport().success(access, remoteInfo);
        }
    }

    @Override
    protected void fail(Request request, Exception e, long start, long currTime, Task task, RemoteInfo remoteInfo) {
        if (this.getAccessSettings().getAccessLogEnable() == true) {
            ActionTask actionTask =  new ActionTask(task.getId()+"",currTime - start,task.getStartTime(),task.getAction());
            AccessResponseInfo access = new AccessResponseInfo(actionTask,currTime - start);
            access.setStatus(AccessResponseInfo.Status.FAILED.getDesc());
            access.setSummaryException(e.getLocalizedMessage());
            getAccessTrailServiceReport().fail(access, remoteInfo);
        }
    }

    @Override
    protected void request(Request request, long start, Task task, RemoteInfo remoteInfo) {
        if (this.getAccessSettings().getAccessLogEnable() == true) {
            ActionTask actionTask =  new ActionTask(task.getId()+"",task.getStartTime(),task.getAction());
            AccessRequestInfo access = buildRequestInfo(request);
            access.addActionTask(actionTask).addRemoteInfo(remoteInfo).addStart(remoteInfo.getStart());
            getAccessTrailServiceReport().requestInfo(access);
        }
    }
}
