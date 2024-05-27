package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;


public class MultiGetStats implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public MultiGetStats() {
        this.totalStats = new Stats();
    }

    public MultiGetStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public void add(MultiGetStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long mGetCount;
        private long mGetTimeInMillis;
        private long mGetCurrent;
        private long mGetTimeOutCount;
        private long mGetFailedCount;
        private long inAll;

        private HistogramStats sucHistogramStats;
        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            mGetCount = in.readVLong();
            mGetTimeInMillis = in.readVLong();
            mGetCurrent = in.readVLong();
            mGetTimeOutCount = in.readVLong();
            mGetFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long mGetCount, long mGetTimeInMillis, long mGetCurrent, long mGetTimeOutCount, long mGetFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.mGetCount = mGetCount;
            this.mGetTimeInMillis = mGetTimeInMillis;
            this.mGetCurrent = mGetCurrent;
            this.mGetTimeOutCount = mGetTimeOutCount;
            this.mGetFailedCount = mGetFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.mGetCount += stats.mGetCount;
            this.mGetTimeInMillis += stats.mGetTimeInMillis;
            this.mGetCurrent += stats.mGetCurrent;
            this.mGetTimeOutCount += stats.mGetTimeOutCount;
            this.mGetFailedCount += stats.mGetFailedCount;
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
            mGetCount = in.readVLong();
            mGetTimeInMillis = in.readVLong();
            mGetCurrent = in.readVLong();
            mGetTimeOutCount = in.readVLong();
            mGetFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(mGetCount);
            out.writeVLong(mGetTimeInMillis);
            out.writeVLong(mGetCurrent);
            out.writeVLong(mGetTimeOutCount);
            out.writeVLong(mGetFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, mGetCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getGetTime());
            builder.field(CommonFields.CURRENT, mGetCurrent);
            builder.field(CommonFields.TIME_OUT, mGetTimeOutCount);
            builder.field(CommonFields.FAILED, mGetFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }

        public TimeValue getGetTime() { return new TimeValue(mGetTimeInMillis); }

        public long getmGetCount() {
            return mGetCount;
        }

        public long getmGetTimeInMillis() {
            return mGetTimeInMillis;
        }

        public long getmGetCurrent() {
            return mGetCurrent;
        }

        public long getmGetTimeOutCount() {
            return mGetTimeOutCount;
        }

        public long getmGetFailedCount() {
            return mGetFailedCount;
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
        builder.startObject( "mget");
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(MultiGetStats multiGetStats) {
        if (multiGetStats == null) {
            return;
        }
        totalStats.add(multiGetStats.totalStats);
    }
}
