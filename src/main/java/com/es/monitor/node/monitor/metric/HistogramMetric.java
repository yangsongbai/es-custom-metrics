package com.es.monitor.node.monitor.metric;


import static java.util.concurrent.TimeUnit.SECONDS;

public class HistogramMetric {
    private final Histogram histogram = new Histogram(new SlidingTimeWindowArrayReservoir(30, SECONDS));


    private long lastIncModifyTime;

    public HistogramMetricSnapshot getSnapshot() {
        return new HistogramMetricSnapshot(histogram.getSnapshot());
    }

    public void inc(long n) {
        histogram.update(n);
        lastIncModifyTime = System.currentTimeMillis();
    }

    public void fillEmptyData() {
        //如果超过1s，没有出现数据更新，则自动记录空数据
        long interval = System.currentTimeMillis() - lastIncModifyTime;
        if (interval >= 1000){
            histogram.update(0);
        }
    }
}
