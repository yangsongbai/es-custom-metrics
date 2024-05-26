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
 * @create 2022/11/8 20:06
 */
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

        Stats() {}

        public Stats(StreamInput in) throws IOException {
            updateByQueryCount = in.readVLong();
            updateByQueryTimeInMillis = in.readVLong();
            updateByQueryCurrent = in.readVLong();
            updateByQueryTimeOutCount = in.readVLong();
            updateByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long updateByQueryCount, long updateByQueryTimeInMillis, long updateByQueryCurrent, long updateByQueryTimeOutCount, long searchFailedCount) {
            this.updateByQueryCount = updateByQueryCount;
            this.updateByQueryTimeInMillis = updateByQueryTimeInMillis;
            this.updateByQueryCurrent = updateByQueryCurrent;
            this.updateByQueryTimeOutCount = updateByQueryTimeOutCount;
            this.updateByQueryFailedCount = searchFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.updateByQueryCount += stats.updateByQueryCount;
            this.updateByQueryTimeInMillis += stats.updateByQueryTimeInMillis;
            this.updateByQueryCurrent += stats.updateByQueryCurrent;
            this.updateByQueryTimeOutCount += stats.updateByQueryTimeOutCount;
            this.updateByQueryFailedCount += stats.updateByQueryFailedCount;
            this.inAll += stats.inAll;
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
        }
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(updateByQueryCount);
            out.writeVLong(updateByQueryTimeInMillis);
            out.writeVLong(updateByQueryCurrent);
            out.writeVLong(updateByQueryTimeOutCount);
            out.writeVLong(updateByQueryFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.UPDATE_BY_QUERY_TOTAL, updateByQueryCount);
            builder.humanReadableField(Fields.UPDATE_BY_QUERY_TIME_IN_MILLIS, Fields.UPDATE_BY_QUERY_TIME, getTime());
            builder.field(Fields.UPDATE_BY_QUERY_CURRENT,updateByQueryCurrent);
            builder.field(Fields.UPDATE_BY_QUERY_TIME_OUT, updateByQueryTimeOutCount);
            builder.field(Fields.UPDATE_BY_QUERY_FAILED, updateByQueryFailedCount);
            builder.field(Fields.IN_ALL, inAll);
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(updateByQueryTimeInMillis); }
    }
    static final class Fields {
        static final String UPDATE_BY_QUERY = "update_by_query";
        static final String UPDATE_BY_QUERY_TOTAL = "total";
        static final String UPDATE_BY_QUERY_TIME = "time";
        static final String UPDATE_BY_QUERY_TIME_IN_MILLIS = "time_in_millis";
        static final String UPDATE_BY_QUERY_CURRENT = "current";
        static final String UPDATE_BY_QUERY_TIME_OUT = "time_out";
        static final String UPDATE_BY_QUERY_TIME_OUT_500MS = "time_out_500ms";
        static final String UPDATE_BY_QUERY_TIME_OUT_1000MS = "time_out_1000ms";
        static final String UPDATE_BY_QUERY_FAILED = "failed";
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
        builder.startObject(Fields.UPDATE_BY_QUERY);
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

