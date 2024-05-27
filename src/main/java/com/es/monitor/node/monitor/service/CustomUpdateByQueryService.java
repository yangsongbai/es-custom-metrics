package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.metric.HistogramMetric;
import com.es.monitor.node.monitor.stats.UpdateByQueryStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;


public class CustomUpdateByQueryService implements  CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public UpdateByQueryStats stats() {
        UpdateByQueryStats.Stats total = totalStats.stats();
        return new UpdateByQueryStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.updateByQueryCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.updateByQueryMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.updateByQueryMetric.inc(costTime);
        totalStats.sucHistogram.inc(costTime);
        if (timeout){
            totalStats.updateByQueryTimeOut.inc();
        }
        totalStats.updateByQueryCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(long start, long end){
        totalStats.updateByQueryFailed.inc();
        totalStats.updateByQueryCurrent.dec();
        totalStats.failHistogram.inc(end - start);
    }

    @Override
    public void partFail() {
        totalStats.updateByQueryFailed.inc();
    }

    @Override
    public void clear() {
        totalStats.clear();
    }

    @Override
    public void fillEmptyData(){
        totalStats.sucHistogram.fillEmptyData();
        totalStats.failHistogram.fillEmptyData();
    }

    static class StatsHolder {
        private  MeanMetric    updateByQueryMetric = new MeanMetric();
        private  CounterMetric updateByQueryCurrent = new CounterMetric();
        private  CounterMetric updateByQueryFailed = new CounterMetric();
        private  CounterMetric updateByQueryTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        private HistogramMetric sucHistogram = new HistogramMetric();
        private HistogramMetric failHistogram = new HistogramMetric();

        public void clear(){
            updateByQueryMetric = new MeanMetric();
            updateByQueryCurrent = new CounterMetric();
            updateByQueryFailed = new CounterMetric();
            updateByQueryTimeOut = new CounterMetric();
            inAll = new CounterMetric();
            sucHistogram = new HistogramMetric();
            failHistogram = new HistogramMetric();
        }

        UpdateByQueryStats.Stats stats() {
            return new UpdateByQueryStats.Stats(inAll.count(),
                    updateByQueryMetric.count(), TimeUnit.MILLISECONDS.toMillis(updateByQueryMetric.sum()),
                    updateByQueryCurrent.count(), updateByQueryFailed.count(), updateByQueryTimeOut.count(), sucHistogram.getSnapshot(), failHistogram.getSnapshot());
        }
    }
}
