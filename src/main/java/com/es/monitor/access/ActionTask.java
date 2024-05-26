package com.es.monitor.access;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/11 15:18
 */
public class ActionTask {
    private final String id;
    private long taskCost;
    private long taskStartTime;
    private String action;

    public ActionTask(String id, long taskStartTime) {
        this(id,taskStartTime,"");
    }

    public ActionTask(String id, long taskStartTime, String action) {
        this(id,0,taskStartTime,action);
    }

    public ActionTask(String id, long taskCost, long taskStartTime, String action) {
        this.id = id;
        this.taskCost = taskCost;
        this.taskStartTime = taskStartTime;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public long getTaskCost() {
        return taskCost;
    }

    public void setTaskCost(long taskCost) {
        this.taskCost = taskCost;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(long taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
