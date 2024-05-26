package com.es.monitor.monitor.service;

import com.es.monitor.monitor.stats.MultiSearchStats;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 20:41
 */
public class CustomMultiSearchService  implements  CustomStatsService {

    private final StatsHolder totalStats = new StatsHolder();

    public MultiSearchStats stats() {
        MultiSearchStats.Stats total = totalStats.stats();
        return new MultiSearchStats(total);
    }

    /**
     * 接收到请求
     * 记录当前接收到一次请求
     */
    @Override
    public void request(){
        totalStats.multiSearchCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.multiSearchMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.multiSearchMetric.inc(costTime);
        if (timeout == true){
            totalStats.multiSearchTimeOut.inc();
        }
        totalStats.multiSearchCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(){
        totalStats.multiSearchFailed.inc();
        totalStats.multiSearchCurrent.dec();
    }

    @Override
    public void partFail() {
        totalStats.multiSearchFailed.inc();
    }

    @Override
    public void clear() {
        totalStats.clear();
    }

    static class StatsHolder {
        private  MeanMetric    multiSearchMetric = new MeanMetric();
        private  CounterMetric multiSearchCurrent = new CounterMetric();
        private  CounterMetric multiSearchFailed = new CounterMetric();
        private  CounterMetric multiSearchTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        public void clear(){
            multiSearchMetric = new MeanMetric();
            multiSearchCurrent = new CounterMetric();
            multiSearchFailed = new CounterMetric();
            multiSearchTimeOut = new CounterMetric();
            inAll = new CounterMetric();
        }

        MultiSearchStats.Stats stats() {
            return new MultiSearchStats.Stats(inAll.count(),
                    multiSearchMetric.count(), TimeUnit.MILLISECONDS.toMillis(multiSearchMetric.sum()),
                    multiSearchCurrent.count(),multiSearchFailed.count(),multiSearchTimeOut.count());
        }
    }
}
