package com.es.monitor.monitor.stats;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 19:37
 */
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

        Stats() {}

        public Stats(StreamInput in) throws IOException {
            mGetCount = in.readVLong();
            mGetTimeInMillis = in.readVLong();
            mGetCurrent = in.readVLong();
            mGetTimeOutCount = in.readVLong();
            mGetFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long mGetCount, long mGetTimeInMillis, long mGetCurrent, long mGetTimeOutCount, long mGetFailedCount) {
            this.mGetCount = mGetCount;
            this.mGetTimeInMillis = mGetTimeInMillis;
            this.mGetCurrent = mGetCurrent;
            this.mGetTimeOutCount = mGetTimeOutCount;
            this.mGetFailedCount = mGetFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.mGetCount += stats.mGetCount;
            this.mGetTimeInMillis += stats.mGetTimeInMillis;
            this.mGetCurrent += stats.mGetCurrent;
            this.mGetTimeOutCount += stats.mGetTimeOutCount;
            this.mGetFailedCount += stats.mGetFailedCount;
            this.inAll += stats.inAll;
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
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(mGetCount);
            out.writeVLong(mGetTimeInMillis);
            out.writeVLong(mGetCurrent);
            out.writeVLong(mGetTimeOutCount);
            out.writeVLong(mGetFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.MGET_TOTAL, mGetCount);
            builder.humanReadableField(Fields.MGET_TIME_IN_MILLIS, Fields.MGET_TIME, getGetTime());
            builder.field(Fields.MGET_CURRENT, mGetCurrent);
            builder.field(Fields.MGET_TIME_OUT, mGetTimeOutCount);
            builder.field(Fields.MGET_FAILED, mGetFailedCount);
            builder.field(Fields.IN_ALL, inAll);
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
    static final class Fields {
        static final String MGET = "mget";
        static final String MGET_TOTAL = "total";
        static final String MGET_TIME = "time";
        static final String MGET_TIME_IN_MILLIS = "time_in_millis";
        static final String MGET_CURRENT = "current";
        static final String MGET_TIME_OUT = "time_out";
        static final String MGET_FAILED = "failed";
        static final String IN_ALL = "in_all";
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
        builder.startObject(Fields.MGET);
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
