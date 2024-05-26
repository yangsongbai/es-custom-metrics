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
 * @create 2022/11/8 20:13
 */
public class IndexRequestStats  implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public IndexRequestStats() {
        this.totalStats = new Stats();
    }

    public IndexRequestStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public void add(IndexRequestStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long indexCount;
        private long indexTimeInMillis;
        private long indexCurrent;
        private long indexTimeOutCount;
        private long indexFailedCount;
        private long inAll;

        Stats() {}

        public Stats(StreamInput in) throws IOException {
            indexCount = in.readVLong();
            indexTimeInMillis = in.readVLong();
            indexCurrent = in.readVLong();
            indexTimeOutCount = in.readVLong();
            indexFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long indexCount, long indexTimeInMillis, long indexCurrent, long indexTimeOutCount, long searchFailedCount) {
            this.indexCount = indexCount;
            this.indexTimeInMillis = indexTimeInMillis;
            this.indexCurrent = indexCurrent;
            this.indexTimeOutCount = indexTimeOutCount;
            this.indexFailedCount = searchFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.indexCount += stats.indexCount;
            this.indexTimeInMillis += stats.indexTimeInMillis;
            this.indexCurrent += stats.indexCurrent;
            this.indexTimeOutCount += stats.indexTimeOutCount;
            this.indexFailedCount += stats.indexFailedCount;
            this.inAll = stats.inAll;
        }

        public static Stats readStats(StreamInput in) throws IOException {
            Stats stats = new Stats();
            stats.readFrom(in);
            return stats;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            indexCount = in.readVLong();
            indexTimeInMillis = in.readVLong();
            indexCurrent = in.readVLong();
            indexTimeOutCount = in.readVLong();
            indexFailedCount = in.readVLong();
            inAll = in.readVLong();
        }
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(indexCount);
            out.writeVLong(indexTimeInMillis);
            out.writeVLong(indexCurrent);
            out.writeVLong(indexTimeOutCount);
            out.writeVLong(indexFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.INDEX_TOTAL, indexCount);
            builder.humanReadableField(Fields.INDEX_TIME_IN_MILLIS, Fields.INDEX_TIME, getTime());
            builder.field(Fields.INDEX_CURRENT, indexCurrent);
            builder.field(Fields.INDEX_TIME_OUT, indexTimeOutCount);
            builder.field(Fields.INDEX_FAILED, indexFailedCount);
            builder.field(Fields.IN_ALL, inAll);
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(indexTimeInMillis); }
    }
    static final class Fields {
        static final String INDEX = "index";
        static final String INDEX_TOTAL = "total";
        static final String INDEX_TIME = "time";
        static final String INDEX_TIME_IN_MILLIS = "time_in_millis";
        static final String INDEX_CURRENT = "current";
        static final String INDEX_TIME_OUT = "time_out";
        static final String INDEX_TIME_OUT_500MS = "time_out_500ms";
        static final String INDEX_TIME_OUT_1000MS = "time_out_1000ms";
        static final String INDEX_FAILED = "failed";
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
        builder.startObject(Fields.INDEX);
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(IndexRequestStats indexRequestStats) {
        if (indexRequestStats == null) {
            return;
        }
        totalStats.add(indexRequestStats.totalStats);
    }
}
