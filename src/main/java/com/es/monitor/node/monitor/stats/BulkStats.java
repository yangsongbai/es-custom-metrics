package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by
 *
 *         "bulk" : {
 *           "total" : 504646572,
 *           "time_in_millis" : 950835864,
 *           "current" : 0,
 *           "failed" : 3,
 *           "time_out" : 3, //超时次数
 *           //"time_out_twenties_ms" : 3   //超时20ms的次数
 *         }
 * @Author : yangsongbai1
 * @create 2022/11/4 18:13
 */
public class BulkStats implements Streamable,ToXContentFragment {
    private  Stats totalStats;
    public BulkStats() {
        this.totalStats = new Stats();
    }
    public BulkStats(Stats totalStats) {
        this.totalStats = totalStats;
    }


    public void add(BulkStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long bulkCount;
        private long bulkTimeInMillis;
        private long bulkCurrent;
        private long bulkTimeOutCount;
        private long bulkFailedCount;
        private long bulkPartFailedCount;
        private long inAll;

       private HistogramStats sucHistogramStats;
        Stats() {
            sucHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            bulkCount = in.readVLong();
            bulkTimeInMillis = in.readVLong();
            bulkCurrent = in.readVLong();
            bulkTimeOutCount = in.readVLong();
            bulkFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long bulkCount, long bulkTimeInMillis, long bulkCurrent, long bulkTimeOutCount, long bulkFailedCount, HistogramMetricSnapshot snapshot) {
            this.bulkCount = bulkCount;
            this.bulkTimeInMillis = bulkTimeInMillis;
            this.bulkCurrent = bulkCurrent;
            this.bulkTimeOutCount = bulkTimeOutCount;
            this.bulkFailedCount = bulkFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
        }

        public static Stats readStats(StreamInput in) throws IOException {
            Stats stats = new Stats();
            stats.readFrom(in);
            return stats;
        }

        public void add(Stats stats) {
            this.bulkCount += stats.bulkCount;
            this.bulkTimeInMillis += stats.bulkTimeInMillis;
            this.bulkCurrent += stats.bulkCurrent;
            this.bulkTimeOutCount += stats.bulkTimeOutCount;
            this.bulkFailedCount += stats.bulkFailedCount;
            this.inAll += stats.inAll;
            this.sucHistogramStats.add(stats.sucHistogramStats);
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            bulkCount = in.readVLong();
            bulkTimeInMillis = in.readVLong();
            bulkCurrent = in.readVLong();
            bulkTimeOutCount = in.readVLong();
            bulkFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(bulkCount);
            out.writeVLong(bulkTimeInMillis);
            out.writeVLong(bulkCurrent);
            out.writeVLong(bulkTimeOutCount);
            out.writeVLong(bulkFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.BULK_TOTAL, bulkCount);
            builder.humanReadableField(Fields.BULK_TIME_IN_MILLIS, Fields.BULK_TIME, getTime());
            builder.field(Fields.BULK_CURRENT, bulkCurrent);
            builder.field(Fields.BULK_TIME_OUT, bulkTimeOutCount);
            builder.field(Fields.BULK_FAILED, bulkFailedCount);
            builder.field(Fields.IN_ALL, inAll);
            builder.startObject(Fields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "bulkCount=" + bulkCount +
                    ", bulkTimeInMillis=" + bulkTimeInMillis +
                    ", bulkCurrent=" + bulkCurrent +
                    ", bulkTimeOutCount=" + bulkTimeOutCount +
                    ", bulkFailedCount=" + bulkFailedCount +
                    ", bulkPartFailedCount=" + bulkPartFailedCount +
                    ", sucHistogramStats=" + sucHistogramStats.toString() +
                    '}';
        }

        public TimeValue getTime() { return new TimeValue(bulkTimeInMillis); }

    }
    static final class Fields {
        static final String BULK = "bulk";
        static final String BULK_TOTAL = "total";
        static final String BULK_TIME = "time";
        static final String BULK_TIME_IN_MILLIS = "time_in_millis";
        static final String BULK_CURRENT = "current";
        static final String BULK_TIME_OUT = "time_out";
        static final String BULK_FAILED = "failed";
        static final String BULK_PART_FAILED = "part_failed";
        static final String IN_ALL = "in_all";
        static final String SUC_LATENCY = "suc_latency";
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
        builder.startObject(Fields.BULK);
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }

    public void addTotals(BulkStats bulkStats) {
        if (bulkStats == null) {
            return;
        }
        totalStats.add(bulkStats.totalStats);
    }

}
