package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

public class DeleteByQueryStats implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public DeleteByQueryStats() {
        this.totalStats = new Stats();
    }

    public DeleteByQueryStats(Stats totalStats) {
        this.totalStats = totalStats;
    }


    public void add(DeleteByQueryStats other) {
        addTotals(other);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        totalStats.writeTo(out);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        totalStats = Stats.readStats(in);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("delete_by_query");
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }

    public void addTotals(DeleteByQueryStats deleteByQueryStats) {
        if (deleteByQueryStats == null) {
            return;
        }
        totalStats.add(deleteByQueryStats.totalStats);
    }

    public static class Stats implements Streamable, ToXContentFragment {

        private long deleteByQueryCount;
        private long deleteByQueryTimeInMillis;
        private long deleteByQueryCurrent;
        private long deleteByQueryTimeOutCount;
        private long deleteByQueryFailedCount;
        private long inAll;

        private HistogramStats sucHistogramStats;
        private HistogramStats failHistogramStats;
        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            deleteByQueryCount = in.readVLong();
            deleteByQueryTimeInMillis = in.readVLong();
            deleteByQueryCurrent = in.readVLong();
            deleteByQueryTimeOutCount = in.readVLong();
            deleteByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long deleteByQueryCount, long deleteByQueryTimeInMillis, long deleteByQueryCurrent, long deleteByQueryTimeOutCount, long searchFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.deleteByQueryCount = deleteByQueryCount;
            this.deleteByQueryTimeInMillis = deleteByQueryTimeInMillis;
            this.deleteByQueryCurrent = deleteByQueryCurrent;
            this.deleteByQueryTimeOutCount = deleteByQueryTimeOutCount;
            this.deleteByQueryFailedCount = searchFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.deleteByQueryCount += stats.deleteByQueryCount;
            this.deleteByQueryTimeInMillis += stats.deleteByQueryTimeInMillis;
            this.deleteByQueryCurrent += stats.deleteByQueryCurrent;
            this.deleteByQueryTimeOutCount += stats.deleteByQueryTimeOutCount;
            this.deleteByQueryFailedCount += stats.deleteByQueryFailedCount;
            this.inAll += stats.inAll;
            this.sucHistogramStats.add(stats.sucHistogramStats);
            this.failHistogramStats.add(stats.failHistogramStats);
        }

        public static Stats readStats(StreamInput in) throws IOException {
            Stats stats = new Stats();
            stats.readFrom(in);
            return stats;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            deleteByQueryCount = in.readVLong();
            deleteByQueryTimeInMillis = in.readVLong();
            deleteByQueryCurrent = in.readVLong();
            deleteByQueryTimeOutCount = in.readVLong();
            deleteByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(deleteByQueryCount);
            out.writeVLong(deleteByQueryTimeInMillis);
            out.writeVLong(deleteByQueryCurrent);
            out.writeVLong(deleteByQueryTimeOutCount);
            out.writeVLong(deleteByQueryFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, deleteByQueryCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getTime());
            builder.field(CommonFields.CURRENT, deleteByQueryCurrent);
            builder.field(CommonFields.TIME_OUT, deleteByQueryTimeOutCount);
            builder.field(CommonFields.FAILED, deleteByQueryFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }

        public TimeValue getTime() {
            return new TimeValue(deleteByQueryTimeInMillis);
        }
    }
}
