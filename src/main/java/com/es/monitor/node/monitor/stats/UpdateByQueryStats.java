package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;


public class UpdateByQueryStats  implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public UpdateByQueryStats() {
        this.totalStats = new Stats();
    }

    public UpdateByQueryStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public UpdateByQueryStats(StreamInput in) throws IOException {
        totalStats = new Stats(in);
    }

    public void add(UpdateByQueryStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long updateByQueryCount;
        private long  updateByQueryTimeInMillis;
        private long updateByQueryCurrent;
        private long updateByQueryTimeOutCount;
        private long updateByQueryFailedCount;
        private long inAll;

        private HistogramStats sucHistogramStats;
        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            updateByQueryCount = in.readVLong();
            updateByQueryTimeInMillis = in.readVLong();
            updateByQueryCurrent = in.readVLong();
            updateByQueryTimeOutCount = in.readVLong();
            updateByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long updateByQueryCount, long updateByQueryTimeInMillis, long updateByQueryCurrent, long updateByQueryTimeOutCount, long searchFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.updateByQueryCount = updateByQueryCount;
            this.updateByQueryTimeInMillis = updateByQueryTimeInMillis;
            this.updateByQueryCurrent = updateByQueryCurrent;
            this.updateByQueryTimeOutCount = updateByQueryTimeOutCount;
            this.updateByQueryFailedCount = searchFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.updateByQueryCount += stats.updateByQueryCount;
            this.updateByQueryTimeInMillis += stats.updateByQueryTimeInMillis;
            this.updateByQueryCurrent += stats.updateByQueryCurrent;
            this.updateByQueryTimeOutCount += stats.updateByQueryTimeOutCount;
            this.updateByQueryFailedCount += stats.updateByQueryFailedCount;
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
            updateByQueryCount = in.readVLong();
            updateByQueryTimeInMillis = in.readVLong();
            updateByQueryCurrent = in.readVLong();
            updateByQueryTimeOutCount = in.readVLong();
            updateByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(updateByQueryCount);
            out.writeVLong(updateByQueryTimeInMillis);
            out.writeVLong(updateByQueryCurrent);
            out.writeVLong(updateByQueryTimeOutCount);
            out.writeVLong(updateByQueryFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, updateByQueryCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getTime());
            builder.field(CommonFields.CURRENT,updateByQueryCurrent);
            builder.field(CommonFields.TIME_OUT, updateByQueryTimeOutCount);
            builder.field(CommonFields.FAILED, updateByQueryFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(updateByQueryTimeInMillis); }
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        totalStats = Stats.readStats(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        totalStats.writeTo(out);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("update_by_query");
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(UpdateByQueryStats updateByQueryStats) {
        if (updateByQueryStats == null) {
            return;
        }
        totalStats.add(updateByQueryStats.totalStats);
    }
}

