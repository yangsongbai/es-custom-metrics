package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.metric.HistogramMetric;
import com.es.monitor.node.monitor.stats.DeleteByQueryStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;

public class CustomDeleteByQueryService implements  CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public DeleteByQueryStats stats() {
        DeleteByQueryStats.Stats total = totalStats.stats();
        return new DeleteByQueryStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.deleteByQueryCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.deleteByQueryMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.deleteByQueryMetric.inc(costTime);
        totalStats.sucHistogram.inc(costTime);
        if (timeout){
            totalStats.deleteByQueryTimeOut.inc();
        }
        totalStats.deleteByQueryCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(long start, long end){
        totalStats.deleteByQueryFailed.inc();
        totalStats.deleteByQueryCurrent.dec();
        totalStats.failHistogram.inc(end - start);
    }

    @Override
    public void partFail() {
        totalStats.deleteByQueryFailed.inc();
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
        private  MeanMetric    deleteByQueryMetric = new MeanMetric();
        private  CounterMetric deleteByQueryCurrent = new CounterMetric();
        private  CounterMetric deleteByQueryFailed = new CounterMetric();
        private  CounterMetric deleteByQueryTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        private HistogramMetric sucHistogram = new HistogramMetric();
        private HistogramMetric failHistogram = new HistogramMetric();

        public void clear(){
            deleteByQueryMetric = new MeanMetric();
            deleteByQueryCurrent = new CounterMetric();
            deleteByQueryFailed = new CounterMetric();
            deleteByQueryTimeOut = new CounterMetric();
            inAll = new CounterMetric();
            sucHistogram = new HistogramMetric();
            failHistogram = new HistogramMetric();
        }

        DeleteByQueryStats.Stats stats() {
            return new DeleteByQueryStats.Stats(inAll.count(),
                    deleteByQueryMetric.count(), TimeUnit.MILLISECONDS.toMillis(deleteByQueryMetric.sum()),
                    deleteByQueryCurrent.count(),deleteByQueryFailed.count(), deleteByQueryTimeOut.count(), sucHistogram.getSnapshot(), failHistogram.getSnapshot());
        }
    }
}
