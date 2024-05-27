package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.metric.HistogramMetric;
import com.es.monitor.node.monitor.stats.MultiGetStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;


public class CustomMultiGetService implements  CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public MultiGetStats stats() {
        MultiGetStats.Stats total = totalStats.stats();
        return new MultiGetStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.multiGetCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.multiGetMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.multiGetMetric.inc(costTime);
        totalStats.sucHistogram.inc(costTime);
        if (timeout == true){
            totalStats.multiGetTimeOut.inc();
        }
        totalStats.multiGetCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(long start, long end){
        totalStats.multiGetFailed.inc();
        totalStats.multiGetCurrent.dec();
        totalStats.failHistogram.inc(end - start);
    }
    @Override
    public void fillEmptyData(){
        totalStats.sucHistogram.fillEmptyData();
        totalStats.failHistogram.fillEmptyData();
    }
    @Override
    public void partFail() {
        totalStats.multiGetFailed.inc();
    }

    @Override
    public void clear() {
        totalStats.clear();
    }

    static class StatsHolder {
        private  MeanMetric    multiGetMetric = new MeanMetric();
        private  CounterMetric multiGetCurrent = new CounterMetric();
        private  CounterMetric multiGetFailed = new CounterMetric();
        private  CounterMetric multiGetTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        private HistogramMetric sucHistogram = new HistogramMetric();
        private HistogramMetric failHistogram = new HistogramMetric();

        public void clear(){
            multiGetMetric = new MeanMetric();
            multiGetCurrent = new CounterMetric();
            multiGetFailed = new CounterMetric();
            multiGetTimeOut = new CounterMetric();
            inAll = new CounterMetric();
            sucHistogram = new HistogramMetric();
            failHistogram = new HistogramMetric();
        }

        MultiGetStats.Stats stats() {
            return new MultiGetStats.Stats(inAll.count(),
                    multiGetMetric.count(), TimeUnit.MILLISECONDS.toMillis(multiGetMetric.sum()),
                    multiGetCurrent.count(),multiGetFailed.count(),multiGetTimeOut.count(), sucHistogram.getSnapshot(), failHistogram.getSnapshot());
        }
    }
}
