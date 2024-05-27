package com.es.monitor.node.access;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/11 15:11
 */
public class AccessRequestInfo {
    private RemoteInfo remoteInfo;
    private ActionTask actionTask;
    private String start;
    private int size;
    private String source;
    private int aggregation;
    private Map<String, Integer> indicesInfo;
    private String searchType;
    private String requestRam;
    private String scrollId;

    public AccessRequestInfo() {
    }

    public AccessRequestInfo(int size, String source, int aggregation, String searchType) {
        this.size = size;
        this.source = source;
        this.aggregation = aggregation;
        this.searchType = searchType;
    }

    public Map<String, Integer> getOrCreateIndicesInfoIfNull() {
        if (this.indicesInfo == null) {
            this.indicesInfo = new HashMap<>();
        }
        return this.indicesInfo;
    }

    public String getRequestRam() {
        return requestRam;
    }

    public AccessRequestInfo setScrollId(String scrollId) {
        this.scrollId = scrollId;
        return  this;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setRequestRam(String requestRam) {
        this.requestRam = requestRam;
    }

    public String getStart() {
        return start;
    }

    public AccessRequestInfo addStart(String start) {
        this.start = start;
        return this;
    }

    public RemoteInfo getRemoteInfo() {
        return remoteInfo;
    }

    public AccessRequestInfo addRemoteInfo(RemoteInfo remoteInfo) {
        this.remoteInfo = remoteInfo;
        return this;
    }

    public void addIndexInfo(String index) {
        if (StringUtils.isBlank(index)) {
            return;
        }
        Integer bulkSize = getOrCreateIndicesInfoIfNull().get(index);
        if (bulkSize == null) {
            getOrCreateIndicesInfoIfNull().put(index, 1);
        } else {
            getOrCreateIndicesInfoIfNull().put(index, bulkSize + 1);
        }
    }

    public ActionTask getActionTask() {
        return actionTask;
    }

    public AccessRequestInfo addActionTask(ActionTask actionTask) {
        this.actionTask = actionTask;
        return this;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public AccessRequestInfo addSize(int size){
        this.size += size;
        return this;
    }

    public String getSource() {
        return source;
    }

    public AccessRequestInfo setSource(String source) {
        this.source = source;
        return this;
    }

    public int getAggregation() {
        return aggregation;
    }

    public AccessRequestInfo addAggregation(int aggregation) {
        this.aggregation += aggregation;
        return this;
    }

    public void setAggregation(int aggregation) {
        this.aggregation = aggregation;
    }

    public Map<String, Integer> getIndicesInfo() {
        return indicesInfo;
    }

    public void setIndicesInfo(Map<String, Integer> indicesInfo) {
        this.indicesInfo = indicesInfo;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public void addIndicesInfo(String... indices) {
        if (ArrayUtils.isEmpty(indices)) {
            return;
        }
        for (String index : indices) {
            addIndexInfo(index);
        }
    }
}
