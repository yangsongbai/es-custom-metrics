package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;


public class GetStats  implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public GetStats() {
        this.totalStats = new Stats();
    }

    public GetStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public void add(GetStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long getCount;
        private long getTimeInMillis;
        private long getCurrent;
        private long getTimeOutCount;
        private long getFailedCount;
        private long inAll ;

        private HistogramStats sucHistogramStats;

        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            getCount = in.readVLong();
            getTimeInMillis = in.readVLong();
            getCurrent = in.readVLong();
            getTimeOutCount = in.readVLong();
            getFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long getCount, long getTimeInMillis, long getCurrent, long getTimeOutCount, long getFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.getCount = getCount;
            this.getTimeInMillis = getTimeInMillis;
            this.getCurrent = getCurrent;
            this.getTimeOutCount = getTimeOutCount;
            this.getFailedCount = getFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.getCount += stats.getCount;
            this.getTimeInMillis += stats.getTimeInMillis;
            this.getCurrent += stats.getCurrent;
            this.getTimeOutCount += stats.getTimeOutCount;
            this.getFailedCount += stats.getFailedCount;
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
            getCount = in.readVLong();
            getTimeInMillis = in.readVLong();
            getCurrent = in.readVLong();
            getTimeOutCount = in.readVLong();
            getFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(getCount);
            out.writeVLong(getTimeInMillis);
            out.writeVLong(getCurrent);
            out.writeVLong(getTimeOutCount);
            out.writeVLong(getFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, getCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getIndexTime());
            builder.field(CommonFields.CURRENT, getCurrent);
            builder.field(CommonFields.TIME_OUT, getTimeOutCount);
            builder.field(CommonFields.FAILED, getFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
        /**
         * The total amount of time spend on executing index operations.
         */
        public TimeValue getIndexTime() { return new TimeValue(getTimeInMillis); }

        public long getGetCount() {
            return getCount;
        }

        public long getGetTimeInMillis() {
            return getTimeInMillis;
        }

        public long getGetCurrent() {
            return getCurrent;
        }

        public long getGetTimeOutCount() {
            return getTimeOutCount;
        }

        public long getGetFailedCount() {
            return getFailedCount;
        }
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
        builder.startObject("get");
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(GetStats getStats) {
        if (getStats == null) {
            return;
        }
        totalStats.add(getStats.totalStats);
    }
}
