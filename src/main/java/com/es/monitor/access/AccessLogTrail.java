package com.es.monitor.access;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 22:08
 */
public class AccessLogTrail {
    //common
    public static final String TYPE_NAME = "type";
    public static final String CLUSTER_NAME = "cluster";
    public static final String LEVEL = "level";
    public static final String NODE_NAME_FIELD_NAME = "node_name";
    public static final String NODE_ID_FIELD_NAME = "node_id";
    public static final String HOST_ADDRESS_FIELD_NAME = "host_ip";
    public static final String HOST_NAME_FIELD_NAME = "host_name";

    //request
    public static final String REQUEST_USER_FIELD_NAME = "request_user";
    public static final String ACTION_FIELD_NAME = "action";
    public static final String METHOD_FIELD_NAME = "method";
    public static final String URI_FIELD_NAME = "uri";
    public static final String PATH_FIELD_NAME = "path";
    public static final String SOURCE_FIELD_NAME = "source";
    public static final String INDICES_FIELD_NAME = "indices";
    public static final String REQUEST_RAM_FIELD_NAME = "request_ram";

    public static final String REQUEST_TIME_FIELD_NAME = "request_time";
    public static final String AGGREGATIONS_FIELD_NAME = "aggregations";
    public static final String REQUEST_ID_FIELD_NAME = "request_id";

    //scroll id
    public static final String SCROLL_ID_FIELD_NAME = "scroll_id";
    //task
    public static final String TASK_ID_FIELD_NAME = "task_id";
    public static final String TASK_START_TIME_FIELD_NAME = "task_start_time";

    //remote
    public static final String REMOTE_ADDRESS_FIELD_NAME = "remote_address";
    public static final String REMOTE_ORIGIN_FIELD_NAME = "remote_origin";


    //response
    public static final String REQUEST_COSTTIME_FIELD_NAME = "request_costTime";
    public static final String REQUEST_TOOK_FIELD_NAME = "request_took";
    public static final String REQUEST_HITS_FIELD_NAME = "request_hits";
    public static final String RESPONSE_RAM_FIELD_NAME = "response_ram";
    public static final String TASK_COST_FIELD_NAME = "task_cost";
    public static final String ERROR_MSG_FIELD_NAME = "error_msg";


    public static final String OPAQUE_ID_FIELD_NAME = "opaque_id";
    public static final String X_FORWARDED_FOR_FIELD_NAME = "x_forwarded_for";
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";


}
