package com.es.monitor.node.common.handler;

import com.es.monitor.node.access.RemoteInfo;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.tasks.Task;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 18:37
 */
public abstract class  BaseHandlerMetricReportRequest<Request extends ActionRequest, Response extends ActionResponse> extends BaseHandler<Request, Response> {
    abstract protected void metric(Request request, Response response, long costTime, long taskTime);
    @Override
    protected void success(Request request, Response response, long start, long currTime, Task task, RemoteInfo remoteInfo) {
        metric(request,response,currTime - start,currTime - task.getStartTime());
    }

    @Override
    protected void fail(Request request, Exception e, long start, long currTime, Task task, RemoteInfo remoteInfo) {

    }

    @Override
    protected void request(ActionRequest request, long start, Task task, RemoteInfo remoteInfo) {

    }
}
