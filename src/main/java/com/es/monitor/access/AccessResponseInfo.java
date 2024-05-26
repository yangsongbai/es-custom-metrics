package com.es.monitor.access;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/14 16:13
 */
public class AccessResponseInfo {
    private ActionTask actionTask;
    //"request.costTime": "158",
    private long requestCost;

    // "search.took": "145",
    private String requestTook;

    private String searchHits;

    private boolean timeout;

    private String status;

    private String summaryException;

    private String scrollId;

    private long ramBytesUsed;

    public AccessResponseInfo() {
    }

    public AccessResponseInfo(ActionTask actionTask, long requestCost) {
        this.actionTask = actionTask;
        this.requestCost = requestCost;
    }

    public String getScrollId() {
        return scrollId;
    }

    public AccessResponseInfo setScrollId(String scrollId) {
        this.scrollId = scrollId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ActionTask getActionTask() {
        return actionTask;
    }

    public void setActionTask(ActionTask actionTask) {
        this.actionTask = actionTask;
    }

    public long getRequestCost() {
        return requestCost;
    }

    public long getRamBytesUsed() {
        return ramBytesUsed;
    }

    public AccessResponseInfo addRamBytesUsed(long ramBytesUsed) {
        this.ramBytesUsed += ramBytesUsed;
        return this;
    }

    public void setRamBytesUsed(long ramBytesUsed) {
        this.ramBytesUsed = ramBytesUsed;
    }

    public void setRequestCost(long requestCost) {
        this.requestCost = requestCost;
    }

    public String getRequestTook() {
        return requestTook;
    }

    public void setRequestTook(String requestTook) {
        this.requestTook = requestTook;
    }

    public String getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(String searchHits) {
        this.searchHits = searchHits;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public String getSummaryException() {
        return summaryException;
    }

    public void setSummaryException(String summaryException) {
        this.summaryException = summaryException;
    }

    public enum Status {
        SUCCESS(1, "success"),
        PART_FAILED(2, "part_fail"),
        FAILED(0, "failed");
        final public int code;
        final public String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static Status of(int i) {
            for (Status item : Status.values()) {
                if (item.code == i) {
                    return item;
                }
            }
            return null;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
