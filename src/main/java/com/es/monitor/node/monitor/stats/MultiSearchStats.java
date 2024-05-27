package com.es.monitor.node.monitor.stats;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 19:47
 */
public class MultiSearchStats  implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public MultiSearchStats() {
        this.totalStats = new Stats();
    }

    public MultiSearchStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public void add(MultiSearchStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long multiSearchCount;
        private long multiSearchTimeInMillis;
        private long multiSearchCurrent;
        private long multiSearchTimeOutCount;
        private long multiSearchFailedCount;
        private long inAll;

        Stats() {}

        public Stats(StreamInput in) throws IOException {
            multiSearchCount = in.readVLong();
            multiSearchTimeInMillis = in.readVLong();
            multiSearchCurrent = in.readVLong();
            multiSearchTimeOutCount = in.readVLong();
            multiSearchFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long multiSearchCount, long multiSearchTimeInMillis, long multiSearchCurrent, long multiSearchTimeOutCount, long multiSearchFailedCount) {
            this.multiSearchCount = multiSearchCount;
            this.multiSearchTimeInMillis = multiSearchTimeInMillis;
            this.multiSearchCurrent = multiSearchCurrent;
            this.multiSearchTimeOutCount = multiSearchTimeOutCount;
            this.multiSearchFailedCount = multiSearchFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.multiSearchCount += stats.multiSearchCount;
            this.multiSearchTimeInMillis += stats.multiSearchTimeInMillis;
            this.multiSearchCurrent += stats.multiSearchCurrent;
            this.multiSearchTimeOutCount += stats.multiSearchTimeOutCount;
            this.multiSearchFailedCount += stats.multiSearchFailedCount;
            this.inAll += stats.inAll;
        }

        public static Stats readStats(StreamInput in) throws IOException {
            Stats stats = new Stats();
            stats.readFrom(in);
            return stats;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            multiSearchCount = in.readVLong();
            multiSearchTimeInMillis = in.readVLong();
            multiSearchCurrent = in.readVLong();
            multiSearchTimeOutCount = in.readVLong();
            multiSearchFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(multiSearchCount);
            out.writeVLong(multiSearchTimeInMillis);
            out.writeVLong(multiSearchCurrent);
            out.writeVLong(multiSearchTimeOutCount);
            out.writeVLong(multiSearchFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.MSEARCH_TOTAL,multiSearchCount);
            builder.humanReadableField(Fields.MSEARCH_TIME_IN_MILLIS, Fields.MSEARCH_TIME, getTime());
            builder.field(Fields.MSEARCH_CURRENT, multiSearchCurrent);
            builder.field(Fields.MSEARCH_TIME_OUT, multiSearchTimeOutCount);
            builder.field(Fields.MSEARCH_FAILED, multiSearchFailedCount);
            builder.field(Fields.IN_ALL, inAll);
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(multiSearchTimeInMillis); }
    }
    static final class Fields {
        static final String MSEARCH = "msearch";
        static final String MSEARCH_TOTAL = "total";
        static final String MSEARCH_TIME = "time";
        static final String MSEARCH_TIME_IN_MILLIS = "time_in_millis";
        static final String MSEARCH_CURRENT = "current";
        static final String MSEARCH_TIME_OUT = "time_out";
        static final String MSEARCH_TIME_OUT_500MS = "time_out_500ms";
        static final String MSEARCH_TIME_OUT_1000MS = "time_out_1000ms";
        static final String MSEARCH_FAILED = "failed";
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
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject(Fields.MSEARCH);
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(MultiSearchStats multiSearchStats) {
        if (multiSearchStats == null) {
            return;
        }
        totalStats.add(multiSearchStats.totalStats);
    }
}
