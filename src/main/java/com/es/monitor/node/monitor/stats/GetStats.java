package com.es.monitor.node.monitor.stats;

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

        Stats() {}

        public Stats(StreamInput in) throws IOException {
            getCount = in.readVLong();
            getTimeInMillis = in.readVLong();
            getCurrent = in.readVLong();
            getTimeOutCount = in.readVLong();
            getFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long getCount, long getTimeInMillis, long getCurrent, long getTimeOutCount, long getFailedCount) {
            this.getCount = getCount;
            this.getTimeInMillis = getTimeInMillis;
            this.getCurrent = getCurrent;
            this.getTimeOutCount = getTimeOutCount;
            this.getFailedCount = getFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.getCount += stats.getCount;
            this.getTimeInMillis += stats.getTimeInMillis;
            this.getCurrent += stats.getCurrent;
            this.getTimeOutCount += stats.getTimeOutCount;
            this.getFailedCount += stats.getFailedCount;
            this.inAll += stats.inAll;
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
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(getCount);
            out.writeVLong(getTimeInMillis);
            out.writeVLong(getCurrent);
            out.writeVLong(getTimeOutCount);
            out.writeVLong(getFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.GET_TOTAL, getCount);
            builder.humanReadableField(Fields.GET_TIME_IN_MILLIS, Fields.GET_TIME, getIndexTime());
            builder.field(Fields.GET_CURRENT, getCurrent);
            builder.field(Fields.GET_TIME_OUT, getTimeOutCount);
            builder.field(Fields.GET_FAILED, getFailedCount);
            builder.field(Fields.IN_ALL, inAll);
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
    static final class Fields {
        static final String GET = "get";
        static final String GET_TOTAL = "total";
        static final String GET_TIME = "time";
        static final String GET_TIME_IN_MILLIS = "time_in_millis";
        static final String GET_CURRENT = "current";
        static final String GET_TIME_OUT = "time_out";
        static final String GET_TIME_OUT_500MS = "time_out_500ms";
        static final String GET_TIME_OUT_1000MS = "time_out_1000ms";
        static final String GET_FAILED = "failed";
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
        builder.startObject(Fields.GET);
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
