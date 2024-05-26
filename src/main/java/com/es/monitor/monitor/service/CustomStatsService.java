package com.es.monitor.monitor.service;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/7 16:53
 */
public interface CustomStatsService {

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    void request();

    /**
     *
     * @param costTime 本次请求花费的时间
     * @param took     本次es原生的计时
     * @param timeout  本次请求是否timeOut
     */
    void success(long costTime,long took,boolean timeout);

    /**
     *  请求异常结束
     */
    void fail();

    /**
     * 部分失败
     */
    void partFail();

    void clear();
}
