package com.es.monitor.common.handler;

import com.es.monitor.access.AccessSettings;
import com.es.monitor.access.AccessTrailService;
import com.es.monitor.access.RemoteInfo;
import com.es.monitor.monitor.service.CustomStatsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.tasks.Task;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 17:17
 */
public abstract class BaseHandler<Request extends ActionRequest, Response extends ActionResponse>{
    private static final Logger logger = LogManager.getLogger(BaseHandler.class);
    private AccessTrailService accessTrailServiceReport ;
    private CustomStatsService customStatsService;
    private AccessSettings accessSettings;
    public AccessTrailService getAccessTrailServiceReport() {
        return accessTrailServiceReport;
    }

    public AccessSettings getAccessSettings() {
        return accessSettings;
    }

    public BaseHandler<Request,Response> setAccessSettings(AccessSettings accessSettings) {
        this.accessSettings = accessSettings;
        return this;
    }

    public CustomStatsService getCustomStatsService() {
        return customStatsService;
    }

    public BaseHandler<Request,Response> setCustomStatsService(CustomStatsService customStatsService) {
        this.customStatsService = customStatsService;
        return this;
    }

    public BaseHandler<Request,Response> setAccessTrailServiceReport(AccessTrailService accessTrailServiceReport) {
        this.accessTrailServiceReport = accessTrailServiceReport;
        return this;
    }
    protected abstract Class<Request> getComputeType();

    public ActionListener<Response> buildActionListener(long start, Task task, Request request,
                                                        ActionListener<Response> listener, RemoteInfo remoteInfo){
        Class<Request> c = getComputeType();
        if (!c.isAssignableFrom(request.getClass())) {
            return null;
        }
        if (this.getAccessSettings().getAccessMetricEnable() == true) {
            //记录当前请求
            if (customStatsService != null) {
                customStatsService.request();
            }
        }
        try {
            //记录请求日志
            request(request,start,task,remoteInfo);
        }catch (Exception e){
            logger.error(e);
        }
        return  new ActionListener<Response>() {
            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
                try{
                    if (customStatsService != null) {
                        customStatsService.fail();
                    }
                    fail(request, e, start, System.currentTimeMillis(),task, remoteInfo);
                    logger.debug("request failed : {}. error:[{}]", request, e.getMessage());
                }catch (Exception ex){
                    logger.error(ex);
                }
            }

            @Override
            public void onResponse(Response response) {
                listener.onResponse(response);
                try {
                    if (customStatsService != null) {
                        success(request, response, start, System.currentTimeMillis(), task, remoteInfo);
                    }
                    logger.debug("request success: {}.", request);
                }catch (Exception e){
                    logger.error(e);
                }
            }
        };
    }

    protected abstract void request(Request request,
                                    long start, Task task, RemoteInfo remoteInfo);


    protected abstract void success(Request request,
                                    Response response,
                                    long start, long currTime,Task task, RemoteInfo remoteInfo);

    protected abstract void fail(Request request,
                                 Exception e,
                                 long start, long currTime,Task task, RemoteInfo remoteInfo);
}
