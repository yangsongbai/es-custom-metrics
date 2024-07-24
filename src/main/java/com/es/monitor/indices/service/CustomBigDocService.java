package com.es.monitor.indices.service;

import com.es.monitor.indices.stats.BigDocStats;
import org.elasticsearch.common.metrics.CounterMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CustomBigDocService {

    private final StatsHolder totalStats = new StatsHolder();

    public CustomBigDocService() {

    }

    public void clear() {
        totalStats.clear();
    }
    public BigDocStats stats(String[] indices, boolean all) {
        return totalStats.stats(indices, all);
    }

    public synchronized void initCounterMetric(String index) {
        totalStats.bigDocIndexGroup.putIfAbsent(index, new CounterMetric());
        totalStats.bigDocDeleteGroup.putIfAbsent(index, new CounterMetric());
    }

    public synchronized void removeCounterMetric(String index) {
        totalStats.bigDocIndexGroup.remove(index);
        totalStats.bigDocDeleteGroup.remove(index);
    }

    public synchronized void incIndexing(String index) {
        CounterMetric counter = totalStats.bigDocIndexGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.bigDocIndexGroup.putIfAbsent(index, counter);
    }

    public synchronized void incDelete(String index) {
        CounterMetric counter = totalStats.bigDocDeleteGroup.get(index);
        if (counter == null) new CounterMetric();
        assert counter != null;
        counter.inc(1L);
        totalStats.bigDocDeleteGroup.putIfAbsent(index, counter);
    }

    static class StatsHolder {
        private  ConcurrentHashMap<String, CounterMetric> bigDocIndexGroup = new ConcurrentHashMap<>();
        private  ConcurrentHashMap<String, CounterMetric> bigDocDeleteGroup = new ConcurrentHashMap<>();
        public void clear(){
            bigDocIndexGroup = new ConcurrentHashMap<>();
            bigDocDeleteGroup = new ConcurrentHashMap<>();
        }

        BigDocStats stats(String[] indices, boolean all) {
            Map<String, BigDocStats.Stats> groupStats = new HashMap<>();
            if (all) {
                Set<Map.Entry<String, CounterMetric>> entrySet =  bigDocIndexGroup.entrySet();
                for (Map.Entry<String, CounterMetric> entry : entrySet) {
                    String index = entry.getKey();
                    BigDocStats.Stats stats = new BigDocStats.Stats();
                    CounterMetric counterMetric = entry.getValue();
                    if (counterMetric != null) {
                        stats.setIndexingBig(counterMetric.count());
                    }
                    CounterMetric delCounter =  bigDocDeleteGroup.get(index);
                    if (delCounter != null) {
                        stats.setDelBig(delCounter.count());
                    }
                    groupStats.put(index, stats);
                }
            } else if (indices != null) {
                for (String index : indices){
                    CounterMetric counterMetric  =  bigDocIndexGroup.get(index);
                    CounterMetric delCounter =  bigDocDeleteGroup.get(index);
                    if (counterMetric == null && delCounter == null) continue;
                    BigDocStats.Stats stats = new BigDocStats.Stats();
                    if (counterMetric != null) {
                        stats.setIndexingBig(counterMetric.count());
                    }
                    if (delCounter != null) {
                        stats.setDelBig(delCounter.count());
                    }
                    groupStats.put(index, stats);
                }
            }
            return new BigDocStats(groupStats);
        }
    }
}
