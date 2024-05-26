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
 * @create 2022/11/8 19:56
 */
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
        builder.startObject(Fields.DELETE_BY_QUERY);
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

        Stats() {
        }

        public Stats(StreamInput in) throws IOException {
            deleteByQueryCount = in.readVLong();
            deleteByQueryTimeInMillis = in.readVLong();
            deleteByQueryCurrent = in.readVLong();
            deleteByQueryTimeOutCount = in.readVLong();
            deleteByQueryFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long deleteByQueryCount, long deleteByQueryTimeInMillis, long deleteByQueryCurrent, long deleteByQueryTimeOutCount, long searchFailedCount) {
            this.deleteByQueryCount = deleteByQueryCount;
            this.deleteByQueryTimeInMillis = deleteByQueryTimeInMillis;
            this.deleteByQueryCurrent = deleteByQueryCurrent;
            this.deleteByQueryTimeOutCount = deleteByQueryTimeOutCount;
            this.deleteByQueryFailedCount = searchFailedCount;
            this.inAll = inAll;
        }



        public void add(Stats stats) {
            this.deleteByQueryCount += stats.deleteByQueryCount;
            this.deleteByQueryTimeInMillis += stats.deleteByQueryTimeInMillis;
            this.deleteByQueryCurrent += stats.deleteByQueryCurrent;
            this.deleteByQueryTimeOutCount += stats.deleteByQueryTimeOutCount;
            this.deleteByQueryFailedCount += stats.deleteByQueryFailedCount;
            this.inAll += stats.inAll;
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
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(deleteByQueryCount);
            out.writeVLong(deleteByQueryTimeInMillis);
            out.writeVLong(deleteByQueryCurrent);
            out.writeVLong(deleteByQueryTimeOutCount);
            out.writeVLong(deleteByQueryFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.DELETE_BY_QUERY_TOTAL, deleteByQueryCount);
            builder.humanReadableField(Fields.DELETE_BY_QUERY_TIME_IN_MILLIS, Fields.DELETE_BY_QUERY_TIME, getTime());
            builder.field(Fields.DELETE_BY_QUERY_CURRENT, deleteByQueryCurrent);
            builder.field(Fields.DELETE_BY_QUERY_TIME_OUT, deleteByQueryTimeOutCount);
            builder.field(Fields.DELETE_BY_QUERY_FAILED, deleteByQueryFailedCount);
            builder.field(Fields.IN_ALL, inAll);
            return builder;
        }

        public TimeValue getTime() {
            return new TimeValue(deleteByQueryTimeInMillis);
        }
    }

    static final class Fields {
        static final String DELETE_BY_QUERY = "delete_by_query";
        static final String DELETE_BY_QUERY_TOTAL = "total";
        static final String DELETE_BY_QUERY_TIME = "time";
        static final String DELETE_BY_QUERY_TIME_IN_MILLIS = "time_in_millis";
        static final String DELETE_BY_QUERY_CURRENT = "current";
        static final String DELETE_BY_QUERY_TIME_OUT = "time_out";
        static final String DELETE_BY_QUERY_FAILED = "failed";
        static final String IN_ALL = "in_all";
    }
}
