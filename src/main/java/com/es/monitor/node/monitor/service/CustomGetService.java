package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.metric.HistogramMetric;
import com.es.monitor.node.monitor.stats.GetStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;


public class CustomGetService implements  CustomStatsService{

    private final StatsHolder totalStats = new StatsHolder();

    public GetStats stats() {
        GetStats.Stats total = totalStats.stats();
        return new GetStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.getCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.getMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.getMetric.inc(costTime);
        if (timeout == true){
            totalStats.getTimeOut.inc();
        }
        totalStats.getCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(long start, long end){
        totalStats.getFailed.inc();
        totalStats.getCurrent.dec();
        totalStats.failHistogram.inc(end - start);
    }

    @Override
    public void fillEmptyData(){
        totalStats.sucHistogram.fillEmptyData();
        totalStats.failHistogram.fillEmptyData();
    }

    @Override
    public void partFail() {
        totalStats.getFailed.inc();
    }

    @Override
    public void clear() {
        totalStats.clear();
    }

    static class StatsHolder {
        private  MeanMetric    getMetric = new MeanMetric();
        private  CounterMetric getCurrent = new CounterMetric();
        private  CounterMetric getFailed = new CounterMetric();
        private  CounterMetric getTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        private HistogramMetric sucHistogram = new HistogramMetric();
        private HistogramMetric failHistogram = new HistogramMetric();
        public void clear(){
            getMetric = new MeanMetric();
            getCurrent = new CounterMetric();
            getFailed = new CounterMetric();
            getTimeOut = new CounterMetric();
            inAll = new CounterMetric();
            sucHistogram = new HistogramMetric();
            failHistogram = new HistogramMetric();
        }


        GetStats.Stats stats() {
            return new GetStats.Stats(inAll.count(),
                    getMetric.count(), TimeUnit.MILLISECONDS.toMillis(getMetric.sum()),
                    getCurrent.count(), getFailed.count(), getTimeOut.count(), sucHistogram.getSnapshot(), failHistogram.getSnapshot());
        }
    }
}

