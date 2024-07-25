package com.es.monitor.indices.service;

import com.es.monitor.indices.stats.SearchSlowLogStats;
import com.es.monitor.node.monitor.metric.HistogramMetric;
import org.elasticsearch.common.metrics.CounterMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CustomSlowSearchLogService {
    private final StatsHolder totalStats = new StatsHolder();

    public CustomSlowSearchLogService() {

    }

    public SearchSlowLogStats stats(String[] indices, boolean all) {
        return totalStats.stats(indices, all);
    }

    public synchronized void initMetric(String index) {
        totalStats.queryTotalGroup.putIfAbsent(index, new CounterMetric());
        totalStats.sucQueryGroup.putIfAbsent(index, new HistogramMetric());
        totalStats.failQueryGroup.putIfAbsent(index, new HistogramMetric());

        totalStats.fetchTotalGroup.putIfAbsent(index, new CounterMetric());
        totalStats.sucFetchGroup.putIfAbsent(index, new HistogramMetric());
        totalStats.failFetchGroup.putIfAbsent(index, new HistogramMetric());
    }

    public synchronized void removeMetric(String index) {
        totalStats.queryTotalGroup.remove(index);
        totalStats.sucQueryGroup.remove(index);
        totalStats.failQueryGroup.remove(index);

        totalStats.fetchTotalGroup.remove(index);
        totalStats.sucFetchGroup.remove(index);
        totalStats.failFetchGroup.remove(index);
    }



    public synchronized void incQuerySuc(String index, long took) {
        CounterMetric counter = totalStats.queryTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.queryTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.sucQueryGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.sucQueryGroup.putIfAbsent(index, metric);
    }

    public synchronized void incQueryFail(String index, long took) {
        CounterMetric counter = totalStats.queryTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.queryTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.failQueryGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.failQueryGroup.putIfAbsent(index, metric);
    }


    public synchronized void incFetchSuc(String index, long took) {
        CounterMetric counter = totalStats.fetchTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.fetchTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.sucFetchGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.sucFetchGroup.putIfAbsent(index, metric);
    }

    public synchronized void incFetchFail(String index, long took) {
        CounterMetric counter = totalStats.fetchTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.fetchTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.failFetchGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.failFetchGroup.putIfAbsent(index, metric);
    }

    public void clear() {
        totalStats.clear();
    }

    static class StatsHolder {
        private ConcurrentHashMap<String, CounterMetric> queryTotalGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, HistogramMetric> sucQueryGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, HistogramMetric> failQueryGroup = new ConcurrentHashMap<>();

        private ConcurrentHashMap<String, CounterMetric> fetchTotalGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, HistogramMetric> sucFetchGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, HistogramMetric> failFetchGroup = new ConcurrentHashMap<>();

        public void clear(){
            queryTotalGroup = new ConcurrentHashMap<>();
            sucQueryGroup = new ConcurrentHashMap<>();
            failQueryGroup = new ConcurrentHashMap<>();

            fetchTotalGroup = new ConcurrentHashMap<>();
            sucFetchGroup = new ConcurrentHashMap<>();
            failFetchGroup = new ConcurrentHashMap<>();
        }

        SearchSlowLogStats stats(String[] indices, boolean all) {
            Map<String, SearchSlowLogStats.Stats> groupStats = new HashMap<>();
            if (all) {
                Set<Map.Entry<String, CounterMetric>> entrySet =  queryTotalGroup.entrySet();
                for (Map.Entry<String, CounterMetric> entry : entrySet) {
                    String index = entry.getKey();
                    CounterMetric queryTotal = entry.getValue();
                    SearchSlowLogStats.Stats stats = getStats(queryTotal, index);
                    groupStats.put(index, stats);
                }
            } else if (indices != null) {
                for (String index : indices){
                    CounterMetric queryTotal = queryTotalGroup.get(index);
                    if (queryTotal == null) {
                        HistogramMetric histogramMetric =  new HistogramMetric();
                        SearchSlowLogStats.Stats stats = new SearchSlowLogStats.Stats(0, histogramMetric.getSnapshot(), histogramMetric.getSnapshot(),
                                0, histogramMetric.getSnapshot(), histogramMetric.getSnapshot());
                        groupStats.put(index, stats);
                    } else {
                        groupStats.put(index, getStats(queryTotal, index));
                    }
                }
            }
            return new SearchSlowLogStats();
        }

        private SearchSlowLogStats.Stats getStats(CounterMetric queryTotal, String index) {
            if (queryTotal == null) {
                queryTotal = new CounterMetric();
            }

            HistogramMetric sucQueryHistogram =  sucQueryGroup.get(index);
            if (sucQueryHistogram == null) {
                sucQueryHistogram = new HistogramMetric();
            }

            HistogramMetric failQueryHistogram =  failQueryGroup.get(index);
            if (failQueryHistogram == null) {
                failQueryHistogram = new HistogramMetric();
            }

            CounterMetric fetchTotal = fetchTotalGroup.get(index);
            if (fetchTotal == null) {
                fetchTotal = new CounterMetric();
            }

            HistogramMetric sucFetchHistogram =  sucFetchGroup.get(index);
            if (sucFetchHistogram == null) {
                sucFetchHistogram = new HistogramMetric();
            }

            HistogramMetric failFetchHistogram =  failFetchGroup.get(index);
            if (failFetchHistogram == null) {
                failFetchHistogram = new HistogramMetric();
            }
            return new SearchSlowLogStats.Stats(queryTotal.count(), sucQueryHistogram.getSnapshot(), failQueryHistogram.getSnapshot(),
                    fetchTotal.count(), sucFetchHistogram.getSnapshot(), failFetchHistogram.getSnapshot());
        }
    }

    public void fillEmptyData() {
        Set<String> sucQueryIndices = totalStats.sucQueryGroup.keySet();
        for(String index: sucQueryIndices) {
            assert totalStats.sucQueryGroup != null;
            HistogramMetric metric = totalStats.sucQueryGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            if (totalStats.sucQueryGroup == null) continue;
            totalStats.sucQueryGroup.putIfAbsent(index, metric);
        }

        Set<String> failQueryIndices = totalStats.failQueryGroup.keySet();
        for(String index: failQueryIndices) {
            HistogramMetric metric = totalStats.failQueryGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            totalStats.failQueryGroup.putIfAbsent(index, metric);
        }

        Set<String> sucFetchIndices = totalStats.sucFetchGroup.keySet();
        for(String index: sucFetchIndices) {
            HistogramMetric metric = totalStats.sucFetchGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            totalStats.sucFetchGroup.putIfAbsent(index, metric);
        }

        Set<String> failFetchIndices = totalStats.failFetchGroup.keySet();
        for(String index: failFetchIndices) {
            HistogramMetric metric = totalStats.failFetchGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            totalStats.failFetchGroup.putIfAbsent(index, metric);
        }

    }
}
