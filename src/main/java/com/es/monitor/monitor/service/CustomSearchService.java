package com.es.monitor.monitor.service;

import com.es.monitor.monitor.stats.SearchStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.common.metrics.CounterMetric;
import org.elasticsearch.common.metrics.MeanMetric;

import java.util.concurrent.TimeUnit;


public class CustomSearchService  implements ClusterStateListener, CustomStatsService {
    private static final Logger logger = LogManager.getLogger(CustomSearchService.class);

    private final StatsHolder totalStats = new StatsHolder();

    public SearchStats stats() {
        SearchStats.Stats total = totalStats.stats();
        return new SearchStats(total);
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
        totalStats.searchCurrent.inc();
        totalStats.inAll.inc();
    }

    /**
     * 本次请求为成功时调用
     * @param costTime 本次请求花费的时间
     * @param timeout  本次请求是否timeOut
     */
    @Override
    public void success(long costTime,long took,boolean timeout){
        long sum  = TimeUnit.MILLISECONDS.toMillis(totalStats.searchMetric.sum()) + costTime ;
        if (sum < 0 || sum >= Long.MAX_VALUE) {
            clear();
        }
        totalStats.searchMetric.inc(costTime);
        if (timeout == true){
            totalStats.searchTimeOut.inc();
        }
        totalStats.searchCurrent.dec();
    }

    /**
     *  请求异常结束
     */
    @Override
    public void fail(){
        totalStats.searchFailed.inc();
        totalStats.searchCurrent.dec();
    }

    @Override
    public void partFail() {
        totalStats.searchFailed.inc();
    }

    @Override
    public void clear() {
        totalStats.clear();
    }

    static class StatsHolder {
        private  MeanMetric    searchMetric = new MeanMetric();
        private  CounterMetric searchCurrent = new CounterMetric();
        private  CounterMetric searchFailed = new CounterMetric();
        private  CounterMetric searchTimeOut = new CounterMetric();
        private  CounterMetric inAll = new CounterMetric();

        public void clear(){
            searchMetric = new MeanMetric();
            searchCurrent = new CounterMetric();
            searchFailed = new CounterMetric();
            searchTimeOut = new CounterMetric();
            inAll = new CounterMetric();
        }
        SearchStats.Stats stats() {
            return new SearchStats.Stats(inAll.count(),
                    searchMetric.count(), TimeUnit.MILLISECONDS.toMillis(searchMetric.sum()),
                    searchCurrent.count(), searchFailed.count(),searchTimeOut.count());
        }
    }
}
