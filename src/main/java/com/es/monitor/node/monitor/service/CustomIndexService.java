package com.es.monitor.node.monitor.service;

import com.es.monitor.node.monitor.metric.HistogramMetric;
import com.es.monitor.node.monitor.stats.IndexRequestStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;


public class CustomIndexService implements  CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public IndexRequestStats stats() {
        IndexRequestStats.Stats total = totalStats.stats();
        return new IndexRequestStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.indexCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.indexMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.indexMetric.inc(costTime);
        totalStats.sucHistogram.inc(costTime);
        if (timeout == true){
            totalStats.indexTimeOut.inc();
        }
        totalStats.indexCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(long start, long end){
        totalStats.indexFailed.inc();
        totalStats.indexCurrent.dec();
        totalStats.failHistogram.inc(end - start);
    }

    @Override
    public void partFail() {
        totalStats.indexFailed.inc();
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
        private  MeanMetric    indexMetric = new MeanMetric();
        private  CounterMetric indexCurrent = new CounterMetric();
        private  CounterMetric indexFailed = new CounterMetric();
        private  CounterMetric indexTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        private HistogramMetric sucHistogram = new HistogramMetric();
        private HistogramMetric failHistogram = new HistogramMetric();

        public void clear(){
            indexMetric = new MeanMetric();
            indexCurrent = new CounterMetric();
            indexFailed = new CounterMetric();
            indexTimeOut = new CounterMetric();
            inAll = new CounterMetric();
            sucHistogram = new HistogramMetric();
            failHistogram = new HistogramMetric();
        }

        IndexRequestStats.Stats stats() {
            return new IndexRequestStats.Stats(inAll.count(),
                    indexMetric.count(), TimeUnit.MILLISECONDS.toMillis(indexMetric.sum()),
                    indexCurrent.count(),indexFailed.count(),indexTimeOut.count(), sucHistogram.getSnapshot(), failHistogram.getSnapshot());
        }
    }
}

