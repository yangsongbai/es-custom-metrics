package com.es.monitor.node.access;

import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestRequest;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 21:40
 */
public interface RestAccessTrail {
    String name();
    void audit(String requestId, RestRequest request, ThreadContext threadContext);
}
