package com.es.monitor.node.common.filter;

import com.es.monitor.EsMetricsPlugin;
import com.es.monitor.node.access.AccessRequestInfo;
import com.es.monitor.node.access.AccessSettings;
import com.es.monitor.node.access.AccessTrailService;
import com.es.monitor.node.access.ActionTask;
import com.es.monitor.node.common.Constant;
import com.es.monitor.node.common.handler.BaseHandler;
import com.es.monitor.node.access.RemoteInfo;
import com.es.monitor.node.monitor.service.CustomStatsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.index.seqno.GlobalCheckpointSyncAction;
import org.elasticsearch.tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by
 *
 * @Author : yangsongbai
 * @create 2022/11/1 17:30
 */
public class JPackFilter implements ActionFilter {
    private static final Logger logger = LogManager.getLogger(EsMetricsPlugin.class);

    protected final Settings settings;
    private final ClusterService clusterService;
    private final ThreadContext threadContext;
    private final AccessSettings accessSettings;
    private List<BaseHandler> baseHandlers = new ArrayList<>();
    private AccessTrailService accessTrailService;

    public JPackFilter(ClusterService clusterService,
                       ThreadContext threadContext, AccessTrailService accessTrailService,
                       Settings settings, AccessSettings accessSettings) {
        this.clusterService = clusterService;
        this.threadContext = threadContext;
        this.settings = settings;
        this.accessTrailService = accessTrailService;
        this.accessSettings = accessSettings;
    }


    public JPackFilter addHandler(BaseHandler<ActionRequest, ActionResponse> handler, CustomStatsService service) {
        baseHandlers.add(handler.setAccessTrailServiceReport(accessTrailService).setCustomStatsService(service).setAccessSettings(this.accessSettings));
        return this;
    }

    @Override
    public int order() {
        return 0;
    }

    /**
     * indices:data/read/search
     * indices:data/read/get
     * indices:data/read/msearch
     * indices:data/read/scroll
     * indices:data/read/mget
     * <p>
     * indices:data/write/delete/byquery
     * indices:data/write/update/byquery
     * <p>
     * indices:data/write/index
     * indices:data/write/bulk
     * <p>
     * cluster:monitor/nodes/stats
     * cluster:monitor/state
     * cluster:monitor/nodes/info
     * cluster:monitor/nodes/hot_threads
     * cluster:monitor/nodes/hot_threads
     * cluster:monitor/tasks/lists
     * <p>
     * cluster:admin/reroute
     * cluster:admin/settings/update
     */
    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(Task task, String action, Request request, ActionListener<Response> listener, ActionFilterChain<Request, Response> chain) {
        ActionListener<Response> accessListener = null;
        try {
            //如果日志记录和metric记录都不开启，则直接跳过
            if (this.accessSettings.getAccessLogEnable() == false && this.accessSettings.getAccessMetricEnable() == false){
                chain.proceed(task, action, request, listener);
                return;
            }
            String mark = threadContext.getHeader(Constant.JES_REQUEST_MARK);
            //如果任务的父task为空，证明是刚接收到的任务，起始端
            //带有[均为子任务
            if (isParentTask(task, action, mark) && recordAction(action)) {
                //请求开始时间
                final long start = System.currentTimeMillis();
                RemoteInfo remoteInfo = getRemoteInfo();
                if (remoteInfo.getRest() == null && remoteInfo.getRest() == false) {
                    if (request.remoteAddress() != null) {
                        remoteInfo.setRemoteAddress(request.remoteAddress().getAddress());
                    }
                    remoteInfo.setStart(task.getStartTime() + "");
                }
                for (BaseHandler baseHandler : baseHandlers) {
                    accessListener = baseHandler.buildActionListener(start,task, request, listener, remoteInfo);
                    if (accessListener != null) {
                        break;
                    }
                }
                //记录其余的日志请求
                if (accessListener == null && this.accessSettings.getAccessLogEnable() == true) {
                    AccessRequestInfo access = new AccessRequestInfo();
                    access.setSource(request.toString()).addStart(""+start).addActionTask(new ActionTask(task.getId()+"",task.getStartTime(), task.getAction())).addRemoteInfo(remoteInfo).addStart(remoteInfo.getStart());
                    accessTrailService.requestInfo(access);
                }
            }
            if (StringUtils.isEmpty(mark)) {
                threadContext.putHeader(Constant.JES_REQUEST_MARK, "true");
                logger.debug("mark，access type：[{}],action:[{}]", request.getClass().getName(), action);
            }
        }catch (Exception e) {
            logger.error(e);
        }
        if (accessListener != null) {
            chain.proceed(task, action, request, accessListener);
        } else {
            chain.proceed(task, action, request, listener);
        }
    }


    private boolean recordAction(String action) {
        return !action.startsWith("internal")
                && !action.equals(GlobalCheckpointSyncAction.ACTION_NAME);
    }

    private boolean isParentTask(Task task, String action, String mark) {
        return (task.getParentTaskId() == null || !task.getParentTaskId().isSet() || task.getParentTaskId().getId() == -1)
                && StringUtils.isEmpty(mark) && action.lastIndexOf("]") == -1;
    }

    private RemoteInfo getRemoteInfo() {
        RemoteInfo remoteInfo = new RemoteInfo();
        String  address = threadContext.getHeader(Constant.REMOTE_ADDRESS);
        if (!StringUtils.isEmpty(address)){
            remoteInfo.setRemoteAddress(address);
        }
        String  method = threadContext.getHeader(Constant.REMOTE_METHOD);
        if (!Strings.isNullOrEmpty(method)){
            remoteInfo.setMethod(method);
        }
        String  uri = threadContext.getHeader(Constant.REMOTE_URI);
        if (!Strings.isNullOrEmpty(uri)){
            remoteInfo.setUri(uri);
        }

        String  start = threadContext.getHeader(Constant.REMOTE_START);
        if (!Strings.isNullOrEmpty(start)){
            remoteInfo.setStart(start);
        }

        String  rest = threadContext.getHeader(Constant.REMOTE_REST);
        if (!Strings.isNullOrEmpty(rest)){
            remoteInfo.setRest("true".equals(rest));
        }

        //如果header头拿到的远程地址不为空，则说明是http请求
        if (!StringUtils.isEmpty(address)){
            remoteInfo.setRest(true);
        }

        String user = "";
        if (StringUtils.isNotBlank(threadContext.getHeader(Constant.BASIC_AUTH_HEADER))) {
            final String encodedAuthToken = threadContext.getHeader(Constant.BASIC_AUTH_HEADER);
            final String authToken = new String(Base64.getDecoder().decode(removeBasicPrefix(encodedAuthToken)), StandardCharsets.UTF_8);
            int indexOfSplitChar = authToken.indexOf(":");
            if (indexOfSplitChar < 0) {
                throw  new RuntimeException(String.format("Authorization Header:%s format error", authToken));
            }
            // userName: 0~indexOfSplitChar-1
            user = authToken.substring(0, indexOfSplitChar).trim();
        } else if (StringUtils.isNotBlank(threadContext.getHeader(Constant.JES_USER))) {
            user =  threadContext.getHeader(Constant.JES_USER).trim();
        }
        if (!Strings.isNullOrEmpty(user)){
            remoteInfo.setUser(user);
        }
        return remoteInfo;
    }

    public static String removeBasicPrefix(final String encodedAuthToken) {
        return encodedAuthToken.startsWith(Constant.BASIC_AUTH_PREFIX) ?
                encodedAuthToken.substring(Constant.BASIC_AUTH_PREFIX.length()).trim() : encodedAuthToken;
    }
}
