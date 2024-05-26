package com.es.monitor.monitor.service;

import com.es.monitor.monitor.stats.BulkStats;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/7 15:37
 */
public class CustomBulkService implements  ClusterStateListener, CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public BulkStats stats() {
        BulkStats.Stats total = totalStats.stats();
        return new BulkStats(total);
    }

    @Override
    public void clusterChanged(ClusterChangedEvent event) {

    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.bulkCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout) {
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.bulkMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.bulkMetric.inc(costTime);
        if (timeout == true){
            totalStats.bulkTimeOut.inc();
        }
        totalStats.bulkCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(){
        totalStats.bulkFailed.inc();
        totalStats.bulkCurrent.dec();
    }

    @Override
    public void partFail() {
        totalStats.bulkFailed.inc();
    }

    @Override
    public void clear() {
       totalStats.clear();
    }

    static class StatsHolder {
        private  MeanMetric    bulkMetric = new MeanMetric();
        private  CounterMetric bulkCurrent = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();
        private  CounterMetric bulkFailed = new CounterMetric();
        private  CounterMetric bulkTimeOut = new CounterMetric();

        public void clear(){
            bulkMetric = new MeanMetric();
            bulkCurrent = new CounterMetric();
            bulkFailed = new CounterMetric();
            bulkTimeOut = new CounterMetric();
            inAll = new CounterMetric();
        }

        BulkStats.Stats stats() {
            return new BulkStats.Stats(inAll.count(),
                    bulkMetric.count(), TimeUnit.MILLISECONDS.toMillis(bulkMetric.sum()),
                    bulkCurrent.count(),bulkTimeOut.count(),bulkFailed.count());
        }
    }
}
