package com.es.monitor.indices.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
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
  慢日志指标:
  1 最大值耗时
  2 平均耗时
  3 慢日志总条数
 */
public class SearchSlowLogStats implements Streamable, ToXContentFragment {

    public XContentBuilder toXContentByIndex(XContentBuilder builder, Params params, String index) throws IOException {
        SearchSlowLogStats.Stats stats = null;
        if (groupStats != null && !groupStats.isEmpty()) {
            stats = groupStats.get(index);
        }
        if (stats == null) stats = new SearchSlowLogStats.Stats();
        stats.toXContent(builder, params);
        return builder;
    }

    public XContentBuilder toXContentTotal(XContentBuilder builder, Params params) throws IOException {
        SearchSlowLogStats.Stats stats = new SearchSlowLogStats.Stats(this.getQueryTotal(), this.getQuerySucHistogramStats(),this.getQueryFailHistogramStats(),
                this.getFetchTotal(), this.getFetchSucHistogramStats(), this.gettFetchFailHistogramStats());
        stats.toXContent(builder, params);
        return builder;
    }

    public long getQueryTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            total += value.getQueryTotal();
        }
        return total;
    }


    public HistogramStats getQuerySucHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getQuerySucHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }

    public HistogramStats getQueryFailHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getQueryFailHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }


    public long getFetchTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            total += value.getFetchTotal();
        }
        return total;
    }


    public HistogramStats getFetchSucHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getFetchSucHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }

    public HistogramStats gettFetchFailHistogramStats(){
        HistogramStats result = new HistogramStats();
        if (groupStats == null || groupStats.isEmpty()) {
            return result;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            Stats value  = entry.getValue();
            if (value == null) continue;
            HistogramStats histogramStats  = value.getFetchFailHistogramStats();
            if (histogramStats == null) continue;
            result.calculateTotal(histogramStats);
        }
        return result;
    }

    public static class Stats implements Writeable, ToXContentFragment {

        // query阶段慢日志总条数
        private long queryTotal;

        private HistogramStats querySucHistogramStats;
        private HistogramStats queryFailHistogramStats;
        private long fetchTotal;
        private HistogramStats fetchSucHistogramStats;
        private HistogramStats fetchFailHistogramStats;

        public Stats() {
            querySucHistogramStats = new HistogramStats();
            queryFailHistogramStats = new HistogramStats();
            fetchSucHistogramStats = new HistogramStats();
            fetchFailHistogramStats = new HistogramStats();

        }

        public Stats(long queryTotal, HistogramMetricSnapshot suc, HistogramMetricSnapshot fail,
                     long fetchTotal, HistogramMetricSnapshot fetchSuc, HistogramMetricSnapshot fetchFail) {
            this.queryTotal = queryTotal;
            this.querySucHistogramStats = new HistogramStats(suc);
            this.queryFailHistogramStats = new HistogramStats(fail);
            this.fetchTotal = fetchTotal;
            this.fetchSucHistogramStats = new HistogramStats(fetchSuc);
            this.fetchFailHistogramStats = new HistogramStats(fetchFail);
        }

        public Stats(long queryTotal, HistogramStats suc, HistogramStats fail,
                     long fetchTotal, HistogramStats fetchSuc, HistogramStats fetchFail) {
            this.queryTotal = queryTotal;
            this.querySucHistogramStats = suc;
            this.queryFailHistogramStats = fail;
            this.fetchTotal = fetchTotal;
            this.fetchSucHistogramStats = fetchSuc;
            this.fetchFailHistogramStats = fetchFail;
        }

        private Stats(StreamInput in) throws IOException {
            this.queryTotal = in.readVLong();
            this.querySucHistogramStats = new HistogramStats(in);
            this.queryFailHistogramStats = new HistogramStats(in);

            this.fetchTotal = in.readVLong();
            this.fetchSucHistogramStats = new HistogramStats(in);
            this.fetchFailHistogramStats = new HistogramStats(in);
        }

        public void add(Stats stats) {
            this.queryTotal += stats.queryTotal;
            this.querySucHistogramStats.calculateTotal(stats.querySucHistogramStats);
            this.queryFailHistogramStats.calculateTotal(stats.queryFailHistogramStats);

            this.fetchTotal += stats.fetchTotal;
            this.fetchSucHistogramStats.calculateTotal(stats.fetchSucHistogramStats);
            this.fetchFailHistogramStats.calculateTotal(stats.fetchFailHistogramStats);
        }

        public long getQueryTotal() {
            return queryTotal;
        }

        public HistogramStats getQuerySucHistogramStats() {
            return querySucHistogramStats;
        }

        public HistogramStats getQueryFailHistogramStats() {
            return queryFailHistogramStats;
        }

        public long getFetchTotal() {
            return fetchTotal;
        }

        public HistogramStats getFetchSucHistogramStats() {
            return fetchSucHistogramStats;
        }

        public HistogramStats getFetchFailHistogramStats() {
            return fetchFailHistogramStats;
        }

        public static Stats readStats(StreamInput in) throws IOException {
            return new Stats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(queryTotal);
            querySucHistogramStats.writeTo(out);
            queryFailHistogramStats.writeTo(out);

            out.writeVLong(fetchTotal);
            fetchSucHistogramStats.writeTo(out);
            fetchFailHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.QUERY_TOTAL, queryTotal);
            builder.startObject(Fields.QUERY_SUC_LATENCY);
            querySucHistogramStats.toXContent(builder, params);
            builder.endObject();

            builder.startObject(Fields.QUERY_FAIL_LATENCY);
            queryFailHistogramStats.toXContent(builder, params);
            builder.endObject();

            builder.field(Fields.FETCH_TOTAL, fetchTotal);

            builder.startObject(Fields.FETCH_SUC_LATENCY);
            fetchSucHistogramStats.toXContent(builder, params);
            builder.endObject();

            builder.startObject(Fields.FETCH_FAIL_LATENCY);
            fetchFailHistogramStats.toXContent(builder, params);
            builder.endObject();

            return builder;
        }
    }
    static final class Fields {
        static final String SEARCH_SLOW = "search_slow";
        static final String QUERY_TOTAL = "query_total";
        static final String QUERY_SUC_LATENCY = "query_suc_latency";
        static final String QUERY_FAIL_LATENCY = "query_fail_latency";

        static final String FETCH_TOTAL = "fetch_total";
        static final String FETCH_SUC_LATENCY = "fetch_suc_latency";
        static final String FETCH_FAIL_LATENCY = "fetch_fail_latency";
    }

    @Nullable
    private Map<String, Stats> groupStats;
    private Stats totalStats;
    public SearchSlowLogStats() {
        totalStats = new Stats();
        groupStats = new ConcurrentHashMap<>();
    }

    @Nullable
    public Map<String, Stats> getGroupStats() {
        return this.groupStats != null ? Collections.unmodifiableMap(this.groupStats) : new ConcurrentHashMap<>();
    }

    public Stats getTotalStats() {
        return totalStats;
    }

    public void add(SearchSlowLogStats slowLogStats) {
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

    public void addTotals(SearchSlowLogStats slowLogStats) {
        if (slowLogStats == null) {
            return;
        }
        totalStats.add(slowLogStats.totalStats);
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
}
