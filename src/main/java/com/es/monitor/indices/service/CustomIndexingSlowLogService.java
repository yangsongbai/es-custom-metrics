package com.es.monitor.indices.service;


import com.es.monitor.indices.stats.IndexingSlowLogStats;
import com.es.monitor.node.monitor.metric.HistogramMetric;
import org.elasticsearch.common.metrics.CounterMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CustomIndexingSlowLogService {
    private final StatsHolder totalStats = new StatsHolder();

    public CustomIndexingSlowLogService() {

    }

    public IndexingSlowLogStats stats(String[] indices, boolean all) {
        return totalStats.stats(indices, all);
    }


    public synchronized void initMetric(String index) {
        totalStats.indexingTotalGroup.putIfAbsent(index, new CounterMetric());
        totalStats.indexingGroup.putIfAbsent(index, new HistogramMetric());

        totalStats.delTotalGroup.putIfAbsent(index, new CounterMetric());
        totalStats.delGroup.putIfAbsent(index, new HistogramMetric());
    }


    public synchronized void removeMetric(String index) {
        totalStats.indexingTotalGroup.remove(index);
        totalStats.indexingGroup.remove(index);

        totalStats.delTotalGroup.remove(index);
        totalStats.delGroup.remove(index);
    }

    public synchronized void incIndexing(String index, long took) {
        CounterMetric counter = totalStats.indexingTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.indexingTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.indexingGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.indexingGroup.putIfAbsent(index, metric);

    }

    public synchronized void incDelete(String index, long took) {
        CounterMetric counter = totalStats.delTotalGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.delTotalGroup.putIfAbsent(index, counter);

        HistogramMetric metric = totalStats.delGroup.get(index);
        if (metric == null) new HistogramMetric();
        assert metric != null;
        metric.inc(took);
        totalStats.delGroup.putIfAbsent(index, metric);
    }

    public void clear() {
        totalStats.clear();
    }


    public void fillEmptyData() {
        Set<String> indices = totalStats.delGroup.keySet();
        for(String index: indices) {
            HistogramMetric metric = totalStats.delGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            totalStats.delGroup.putIfAbsent(index, metric);
        }

        Set<String> indexingIndices = totalStats.indexingGroup.keySet();
        for(String index: indexingIndices) {
            HistogramMetric metric = totalStats.indexingGroup.get(index);
            if (metric == null) continue;
            metric.inc(0);
            totalStats.indexingGroup.putIfAbsent(index, metric);
        }
    }

    static class StatsHolder {
        private ConcurrentHashMap<String, CounterMetric> indexingTotalGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, HistogramMetric> indexingGroup = new ConcurrentHashMap<>();

        private ConcurrentHashMap<String, CounterMetric> delTotalGroup = new ConcurrentHashMap<>();

        private  ConcurrentHashMap<String, HistogramMetric> delGroup = new ConcurrentHashMap<>();

        public void clear(){
            indexingTotalGroup = new ConcurrentHashMap<>();
            indexingGroup = new ConcurrentHashMap<>();
            delTotalGroup = new ConcurrentHashMap<>();
            delGroup = new ConcurrentHashMap<>();
        }

        IndexingSlowLogStats stats(String[] indices, boolean all) {
            Map<String, IndexingSlowLogStats.Stats> groupStats = new HashMap<>();
            if (all) {
                Set<Map.Entry<String, CounterMetric>> entrySet =  indexingTotalGroup.entrySet();
                for (Map.Entry<String, CounterMetric> entry : entrySet) {
                    String index = entry.getKey();
                    CounterMetric counterMetric = entry.getValue();
                    IndexingSlowLogStats.Stats stats = getStats(counterMetric, index);
                    groupStats.put(index, stats);
                }
            } else if (indices != null) {
                 for (String index: indices) {
                     CounterMetric counterMetric = indexingTotalGroup.get(index);
                     if (counterMetric == null) {
                         IndexingSlowLogStats.Stats stats = new IndexingSlowLogStats.Stats(0, new HistogramMetric().getSnapshot(), 0, new HistogramMetric().getSnapshot());
                         groupStats.put(index, stats);
                     } else {
                         groupStats.put(index, getStats(counterMetric, index));
                     }
                 }
            }
            return new IndexingSlowLogStats();
        }

        private IndexingSlowLogStats.Stats getStats(CounterMetric counterMetric, String index) {
            if (counterMetric == null) counterMetric = new CounterMetric();
            HistogramMetric  indexingHistogram = indexingGroup.get(index);
            if (indexingHistogram == null) indexingHistogram = new HistogramMetric();
            CounterMetric  delTotal = delTotalGroup.get(index);
            if (delTotal == null) delTotal = new CounterMetric();
            HistogramMetric  delHistogram = delGroup.get(index);
            if (delHistogram == null) delHistogram = new HistogramMetric();
            return new IndexingSlowLogStats.Stats(counterMetric.count(), indexingHistogram.getSnapshot(), delTotal.count(), delHistogram.getSnapshot());
        }
    }
}
