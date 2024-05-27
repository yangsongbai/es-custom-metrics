package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
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

        private HistogramStats sucHistogramStats;

        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }

        public Stats(StreamInput in) throws IOException {
            multiSearchCount = in.readVLong();
            multiSearchTimeInMillis = in.readVLong();
            multiSearchCurrent = in.readVLong();
            multiSearchTimeOutCount = in.readVLong();
            multiSearchFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long multiSearchCount, long multiSearchTimeInMillis, long multiSearchCurrent, long multiSearchTimeOutCount, long multiSearchFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.multiSearchCount = multiSearchCount;
            this.multiSearchTimeInMillis = multiSearchTimeInMillis;
            this.multiSearchCurrent = multiSearchCurrent;
            this.multiSearchTimeOutCount = multiSearchTimeOutCount;
            this.multiSearchFailedCount = multiSearchFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.multiSearchCount += stats.multiSearchCount;
            this.multiSearchTimeInMillis += stats.multiSearchTimeInMillis;
            this.multiSearchCurrent += stats.multiSearchCurrent;
            this.multiSearchTimeOutCount += stats.multiSearchTimeOutCount;
            this.multiSearchFailedCount += stats.multiSearchFailedCount;
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
            multiSearchCount = in.readVLong();
            multiSearchTimeInMillis = in.readVLong();
            multiSearchCurrent = in.readVLong();
            multiSearchTimeOutCount = in.readVLong();
            multiSearchFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(multiSearchCount);
            out.writeVLong(multiSearchTimeInMillis);
            out.writeVLong(multiSearchCurrent);
            out.writeVLong(multiSearchTimeOutCount);
            out.writeVLong(multiSearchFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL,multiSearchCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getTime());
            builder.field(CommonFields.CURRENT, multiSearchCurrent);
            builder.field(CommonFields.TIME_OUT, multiSearchTimeOutCount);
            builder.field(CommonFields.FAILED, multiSearchFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(multiSearchTimeInMillis); }
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
        builder.startObject("msearch");
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
