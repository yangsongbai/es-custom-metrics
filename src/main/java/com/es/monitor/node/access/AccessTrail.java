package com.es.monitor.node.access;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/4 10:06
 */
public interface AccessTrail {
    String name();

    void success(AccessResponseInfo access, RemoteInfo remoteInfo);

    void fail(AccessResponseInfo access, RemoteInfo remoteInfo);

    void requestInfo(AccessRequestInfo access);
}
