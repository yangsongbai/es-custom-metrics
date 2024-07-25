package com.es.monitor.indices.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import com.es.monitor.node.monitor.stats.CommonFields;
import com.es.monitor.node.monitor.stats.HistogramStats;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
  慢写入日志指标:
  1 最大值耗时
  2 平均耗时
  3 慢日志总条数
 */
public class IndexingSlowLogStats implements Streamable, ToXContentFragment {

    public XContentBuilder toXContentByIndex(XContentBuilder builder, Params params, String index) throws IOException {
        IndexingSlowLogStats.Stats stats = null;
        if (groupStats != null && !groupStats.isEmpty()) {
            stats = groupStats.get(index);
        }
        if (stats == null) stats = new IndexingSlowLogStats.Stats();
        stats.toXContent(builder, params);
        return builder;
    }

    public XContentBuilder toXContentTotal(XContentBuilder builder, Params params) throws IOException {
        IndexingSlowLogStats.Stats stats = new IndexingSlowLogStats.Stats(this.getIndexingTotal(), this.getIndexingHistogramStats(),
                this.getDelTotal(), this.getDelHistogramStats());
        stats.toXContent(builder, params);
        return builder;
    }

    public static class Stats implements Writeable, ToXContentFragment {

       // query阶段慢日志总条数
        private long indexingTotal;

        private HistogramStats indexingHistogramStats;
        private long delTotal;
        private HistogramStats delHistogramStats;

        public Stats() {
            indexingHistogramStats = new HistogramStats();
            delHistogramStats = new HistogramStats();
        }

        public Stats(long indexingTotal, HistogramMetricSnapshot indexing,
                     long delTotal, HistogramMetricSnapshot del) {
            this.indexingTotal = indexingTotal;
            this.indexingHistogramStats = new HistogramStats(indexing);
            this.delTotal = delTotal;
            this.delHistogramStats = new HistogramStats(del);
        }

        public Stats(long indexingTotal, HistogramStats indexing,
                     long delTotal, HistogramStats del) {
            this.indexingTotal = indexingTotal;
            this.indexingHistogramStats = indexing;
            this.delTotal = delTotal;
            this.delHistogramStats = del;
        }

        private Stats(StreamInput in) throws IOException {
            this.indexingTotal = in.readVLong();
            this.indexingHistogramStats = new HistogramStats(in);

            this.delTotal = in.readVLong();
            this.delHistogramStats = new HistogramStats(in);
        }

        public void add(Stats stats) {
            this.indexingTotal += stats.indexingTotal;
            this.indexingHistogramStats.calculateTotal(stats.indexingHistogramStats);

            this.delTotal += stats.delTotal;
            this.delHistogramStats.calculateTotal(stats.delHistogramStats);
        }

        public long getIndexingTotal() {
            return indexingTotal;
        }

        public HistogramStats getIndexingHistogramStats() {
            return indexingHistogramStats;
        }

        public long getDelTotal() {
            return delTotal;
        }

        public HistogramStats getDelHistogramStats() {
            return delHistogramStats;
        }

        public static Stats readStats(StreamInput in) throws IOException {
            return new Stats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(indexingTotal);
            indexingHistogramStats.writeTo(out);

            out.writeVLong(delTotal);
            delHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.INDEXING_TOTAL, indexingTotal);
            builder.startObject(Fields.INDEXING_LATENCY);
            indexingHistogramStats.toXContent(builder, params);
            builder.endObject();

            builder.field(Fields.DEL_TOTAL, delTotal);
            builder.startObject(Fields.DEL_LATENCY);
            delHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
    }


    @Nullable
    private Map<String, Stats> groupStats;
    private Stats totalStats;
    public IndexingSlowLogStats() {
        totalStats = new Stats();
        groupStats = new ConcurrentHashMap<>();
    }

    public Stats getTotalStats() {
        return totalStats;
    }

    @Nullable
    public Map<String, Stats> getGroupStats() {
        return this.groupStats != null ? Collections.unmodifiableMap(this.groupStats) : new ConcurrentHashMap<>();
    }

    public void add(IndexingSlowLogStats slowLogStats) {
        if (slowLogStats == null) {
            return;
        }
        addTotals(slowLogStats);
        if (slowLogStats.groupStats != null && !slowLogStats.groupStats.isEmpty()) {
            for (Map.Entry<String, Stats> entry : slowLogStats.groupStats.entrySet()) {
                groupStats.putIfAbsent(entry.getKey(), new Stats());
                groupStats.get(entry.getKey()).add(entry.getValue());
            }
        }
    }

    public void addTotals(IndexingSlowLogStats slowLogStats) {
        if (slowLogStats == null) {
            return;
        }
        totalStats.add(slowLogStats.totalStats);
    }

    public long getIndexingTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            total += value.getIndexingTotal();
        }
        return total;
    }

    public long getDelTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            total += value.getDelTotal();
        }
        return total;
    }


    public HistogramStats getIndexingHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getIndexingHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }

    public HistogramStats getDelHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getDelHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }


    @Override
    public void readFrom(StreamInput in) throws IOException {
        totalStats = Stats.readStats(in);
        if (in.readBoolean()) {
            groupStats = in.readMap(StreamInput::readString, Stats::readStats);
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        totalStats.writeTo(out);
        if (groupStats == null || groupStats.isEmpty()) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeMap(groupStats, StreamOutput::writeString, (stream, stats) -> stats.writeTo(stream));
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    static final class Fields {
        static final String INDEXING_SLOW = "indexing_slow";
        static final String INDEXING_TOTAL = "indexing_total";
        static final String DEL_TOTAL = "del_total";
        static final String INDEXING_LATENCY = "indexing_latency";
        static final String DEL_LATENCY = "del_latency";
    }
}
