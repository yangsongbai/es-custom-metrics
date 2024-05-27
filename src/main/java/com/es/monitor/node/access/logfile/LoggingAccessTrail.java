package com.es.monitor.node.access.logfile;

import com.es.monitor.node.access.AccessLogTrail;
import com.es.monitor.node.access.AccessRequestInfo;
import com.es.monitor.node.access.AccessResponseInfo;
import com.es.monitor.node.access.AccessSettings;
import com.es.monitor.node.access.AccessTrail;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.es.monitor.node.access.RemoteInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.message.StringMapMessage;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.node.Node;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by
 *   记录内容：
 * @Author : yangsongbai
 * @create 2022/11/1 11:47
 */
public class LoggingAccessTrail implements AccessTrail,ClusterStateListener {
    public static final String NAME = "logfile";

    // changing any of this names requires changing the log4j2.properties file too

    private static final Marker ACCESS_MARKER = MarkerManager.getMarker("com.es.access.log");

    private final Logger logger;
    private final ThreadContext threadContext;
    private AccessSettings accessSettings;


    EntryCommonFields entryCommonFields;

    public LoggingAccessTrail(Settings settings, ClusterService clusterService, ThreadPool threadPool, AccessSettings accessSettings) {
        this.threadContext = threadPool.getThreadContext();
        this.logger = LogManager.getLogger(LoggingAccessTrail.class);
        this.entryCommonFields = new EntryCommonFields(settings, null);
        this.accessSettings = accessSettings;
        clusterService.addListener(this);
    }
    public boolean shouldWriteLog(RemoteInfo remoteInfo, String action) {
        if (this.accessSettings.getAccessLogEnable() == false) {
            return false;
        }
        if (shouldRecordLog(remoteInfo.getUser(),this.accessSettings.getAccessLogUserInclude(),this.accessSettings.getAccessLogUserExclude()) == false){
            return false;
        }

        if (shouldRecordLog(action,this.accessSettings.getAccessLogActionInclude(),this.accessSettings.getAccessLogActionExclude()) == false){
            return false;
        }
        if (shouldRecordLog(remoteInfo.getRemoteAddress(),this.accessSettings.getAccessLogIpInclude(),this.accessSettings.getAccessLogIpExclude()) == false){
            return false;
        }
        if (shouldRecordLog(remoteInfo.getUri(),this.accessSettings.getAccessLogUriInclude(),this.accessSettings.getAccessLogUriExclude()) == false){
            return false;
        }
        return true;
    }

    private boolean shouldRecordLog(String action, String[] include, String[] exclude) {
        if (Strings.isNullOrEmpty(action)) return true;
        if (include.length > 0 && exclude.length > 0){
            return Regex.simpleMatch(include, action) && !Regex.simpleMatch(exclude, action);
        }
        if (include.length > 0) {
            if (Regex.simpleMatch(include, action) == false) {
                return false;
            }
        }
        if (exclude.length > 0) {
            return !Regex.simpleMatch(exclude, action);
        }
        return true;
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        updateLocalNodeInfo(event.state().getNodes().getLocalNode());
    }
    void updateLocalNodeInfo(DiscoveryNode newLocalNode) {
        // check if local node changed
        final EntryCommonFields localNodeInfo = this.entryCommonFields;
        if (localNodeInfo.localNode == null || localNodeInfo.localNode.equals(newLocalNode) == false) {
            // no need to synchronize, called only from the cluster state applier thread
            this.entryCommonFields = this.entryCommonFields.withNewLocalNode(newLocalNode);
        }
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void success(AccessResponseInfo access, RemoteInfo remoteInfo) {
        if (shouldWriteLog(remoteInfo,access.getActionTask().getAction()) == false) return;
        LogEntryBuilder logEntry = new LogEntryBuilder();
        loggerResponse(access,logEntry);
        logger.info(ACCESS_MARKER,logEntry.build());
    }
    @Override
    public void fail(AccessResponseInfo access, RemoteInfo remoteInfo) {
        if (shouldWriteLog(remoteInfo,access.getActionTask().getAction()) == false) return;
        LogEntryBuilder logEntry = new LogEntryBuilder();
        loggerResponse(access,logEntry);
        logEntry.withErrorMsg(access.getSummaryException());
        logger.info(ACCESS_MARKER,logEntry.build());
    }

    private void loggerResponse(AccessResponseInfo access, LogEntryBuilder logEntry) {
        logEntry.withRequestHits(access.getSearchHits());
        logEntry.withRequestTook(access.getRequestTook());
        logEntry.withScrollId(access.getScrollId());
        logEntry.withResponseRamUsed(String.valueOf(access.getRamBytesUsed()));
        if (access.getActionTask() != null){
            logEntry.withRequestCost(String.valueOf(access.getActionTask().getTaskCost()));
            logEntry.withTaskID(access.getActionTask().getId());
            logEntry.withTaskCost(String.valueOf(access.getActionTask().getTaskCost()));
        }
    }

    @Override
    public void requestInfo(AccessRequestInfo access) {
        if (shouldWriteLog(access.getRemoteInfo(), access.getActionTask().getAction()) == false) return;
        LogEntryBuilder logEntry = new LogEntryBuilder();
        if (access.getRemoteInfo() != null){
            logEntry.with(AccessLogTrail.REQUEST_USER_FIELD_NAME, access.getRemoteInfo().getUser());
            logEntry.with(AccessLogTrail.METHOD_FIELD_NAME, access.getRemoteInfo().getMethod());
            logEntry.with(AccessLogTrail.URI_FIELD_NAME, access.getRemoteInfo().getUri());
            if (access.getRemoteInfo().rest() != null){
                logEntry.with(AccessLogTrail.REMOTE_ORIGIN_FIELD_NAME, access.getRemoteInfo().rest() == true?"rest":"transport");
            }
            logEntry.with(AccessLogTrail.REMOTE_ADDRESS_FIELD_NAME,access.getRemoteInfo().getRemoteAddress());
        }

        logEntry.withScrollId(access.getScrollId());
        // logEntry.with(AccessLogTrail.PATH_FIELD_NAME, access.getRemoteInfo().getPath());
        logEntry.withSource(access.getSource());

        if (access.getIndicesInfo() != null && access.getIndicesInfo().size() > 0){
            logEntry.with(AccessLogTrail.INDICES_FIELD_NAME,  logEntry.toQuotedJsonArray(access.getIndicesInfo().keySet().toArray()));
        }
        if (access.getRequestRam() != null ){
            logEntry.with(AccessLogTrail.REQUEST_RAM_FIELD_NAME,  access.getRequestRam());
        }

        if (access.getStart() != null ){
            logEntry.with(AccessLogTrail.REQUEST_TIME_FIELD_NAME,  access.getStart());
        }
        logEntry.with(AccessLogTrail.AGGREGATIONS_FIELD_NAME,  String.valueOf(access.getAggregation()));

        if (access.getActionTask() != null) {
            logEntry.withTaskID(access.getActionTask().getId());
            logEntry.with(AccessLogTrail.TASK_START_TIME_FIELD_NAME, String.valueOf(access.getActionTask().getTaskStartTime()));
            logEntry.with(AccessLogTrail.ACTION_FIELD_NAME, access.getActionTask().getAction());
        }
        logger.info(ACCESS_MARKER,logEntry.build());
    }

    static class EntryCommonFields {
        private final Settings settings;
        private final DiscoveryNode localNode;
        final Map<String, String> commonFields;

        EntryCommonFields(Settings settings, @Nullable DiscoveryNode newLocalNode) {
            this.settings = settings;
            this.localNode = newLocalNode;
            final Map<String, String> commonFields = new HashMap<>();
            final String nodeName = Node.NODE_NAME_SETTING.get(settings);
            final String clusterName = ClusterName.CLUSTER_NAME_SETTING.get(settings).value();
            if (Strings.hasLength(nodeName)) {
                commonFields.put(AccessLogTrail.NODE_NAME_FIELD_NAME, nodeName);
            }
            if (Strings.hasLength(clusterName)) {
                commonFields.put(AccessLogTrail.CLUSTER_NAME, clusterName);
            }
            if (newLocalNode != null && newLocalNode.getAddress() != null) {
                commonFields.put(AccessLogTrail.HOST_ADDRESS_FIELD_NAME, newLocalNode.getAddress().getAddress());
                commonFields.put(AccessLogTrail.HOST_NAME_FIELD_NAME, newLocalNode.getHostName());
                commonFields.put(AccessLogTrail.NODE_ID_FIELD_NAME, newLocalNode.getId());
            }
            commonFields.put(AccessLogTrail.LEVEL,"INFO");
            commonFields.put(AccessLogTrail.TYPE_NAME,"access_log");
            this.commonFields = Collections.unmodifiableMap(commonFields);
        }

        EntryCommonFields withNewSettings(Settings newSettings) {
            final Settings mergedSettings = Settings.builder().put(this.settings).put(newSettings, false).build();
            return new EntryCommonFields(mergedSettings, this.localNode);
        }

        EntryCommonFields withNewLocalNode(DiscoveryNode newLocalNode) {
            return new EntryCommonFields(this.settings, newLocalNode);
        }

        public DiscoveryNode getLocalNode() {
            return localNode;
        }
    }

    /**
     * 参看
     * org.elasticsearch.xpack.security.audit.logfile.LoggingAuditTrail.LogEntryBuilder
     */
    private class LogEntryBuilder {

        private final StringMapMessage logEntry;

        LogEntryBuilder() {
            logEntry = new StringMapMessage(LoggingAccessTrail.this.entryCommonFields.commonFields);
        }

        LogEntryBuilder with(String key, String value) {
            if (value != null) {
                logEntry.with(key, value);
            }
            return this;
        }

        LogEntryBuilder with(String key, String[] values) {
            if (values != null) {
                logEntry.with(key, toQuotedJsonArray(values));
            }
            return this;
        }
        public LogEntryBuilder withTaskCost(String taskCost) {
            logEntry.with(AccessLogTrail.TASK_COST_FIELD_NAME, taskCost);
            return this;
        }

        public LogEntryBuilder withScrollId(String scrollId) {
            if (!Strings.isNullOrEmpty(scrollId)){
                logEntry.with(AccessLogTrail.SCROLL_ID_FIELD_NAME, scrollId);
            }
            return this;
        }

        public LogEntryBuilder withSource(String source) {
            if (!Strings.isNullOrEmpty(source)){
                logEntry.with(AccessLogTrail.SOURCE_FIELD_NAME, source);
            }
            return this;
        }

        public LogEntryBuilder withTaskID(String id) {
            if (!Strings.isNullOrEmpty(id)){
                logEntry.with(AccessLogTrail.TASK_ID_FIELD_NAME,  LoggingAccessTrail.this.entryCommonFields.getLocalNode().getId()+":"+id);
            }
           return this;
        }

        public LogEntryBuilder withRequestCost(String requestCost) {
            if (!Strings.isNullOrEmpty(requestCost)){
                logEntry.with(AccessLogTrail.REQUEST_COSTTIME_FIELD_NAME, requestCost);
            }
            return this;
        }

        public LogEntryBuilder withRequestTook(String requestTook) {
            if (!Strings.isNullOrEmpty(requestTook)){
                logEntry.with(AccessLogTrail.REQUEST_TOOK_FIELD_NAME, requestTook);
            }
            return this;
        }

        public LogEntryBuilder withErrorMsg(String errorMsg) {
            if (!Strings.isNullOrEmpty(errorMsg)){
                logEntry.with(AccessLogTrail.ERROR_MSG_FIELD_NAME, errorMsg);
            }
            return this;
        }


        public LogEntryBuilder withRequestHits(String requestHits) {
            if (!Strings.isNullOrEmpty(requestHits)){
                logEntry.with(AccessLogTrail.REQUEST_HITS_FIELD_NAME, requestHits);
            }
            return this;
        }

        public LogEntryBuilder withResponseRamUsed(String responseRamUsed) {
            if (!Strings.isNullOrEmpty(responseRamUsed)){
                logEntry.with(AccessLogTrail.RESPONSE_RAM_FIELD_NAME, responseRamUsed);
            }
            return this;
        }

        LogEntryBuilder with(Map<String, Object> map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value.getClass().isArray()) {
                    logEntry.with(entry.getKey(), toQuotedJsonArray((Object[]) value));
                } else {
                    logEntry.with(entry.getKey(), value);
                }
            }
            return this;
        }

        StringMapMessage build() {
            return logEntry;
        }

        String toQuotedJsonArray(Object[] values) {
            assert values != null;
            final StringBuilder stringBuilder = new StringBuilder();
            final JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
            stringBuilder.append("[");
            for (final Object value : values) {
                if (value != null) {
                    if (stringBuilder.length() > 1) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append("\"");
                    jsonStringEncoder.quoteAsString(value.toString(), stringBuilder);
                    stringBuilder.append("\"");
                }
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }

}
