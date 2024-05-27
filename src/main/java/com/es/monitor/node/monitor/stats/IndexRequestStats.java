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
        private HistogramStats sucHistogramStats;

        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }


        public Stats(StreamInput in) throws IOException {
            indexCount = in.readVLong();
            indexTimeInMillis = in.readVLong();
            indexCurrent = in.readVLong();
            indexTimeOutCount = in.readVLong();
            indexFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long indexCount, long indexTimeInMillis, long indexCurrent, long indexTimeOutCount, long searchFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.indexCount = indexCount;
            this.indexTimeInMillis = indexTimeInMillis;
            this.indexCurrent = indexCurrent;
            this.indexTimeOutCount = indexTimeOutCount;
            this.indexFailedCount = searchFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.indexCount += stats.indexCount;
            this.indexTimeInMillis += stats.indexTimeInMillis;
            this.indexCurrent += stats.indexCurrent;
            this.indexTimeOutCount += stats.indexTimeOutCount;
            this.indexFailedCount += stats.indexFailedCount;
            this.inAll = stats.inAll;
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
            indexCount = in.readVLong();
            indexTimeInMillis = in.readVLong();
            indexCurrent = in.readVLong();
            indexTimeOutCount = in.readVLong();
            indexFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(indexCount);
            out.writeVLong(indexTimeInMillis);
            out.writeVLong(indexCurrent);
            out.writeVLong(indexTimeOutCount);
            out.writeVLong(indexFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, indexCount);
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getTime());
            builder.field(CommonFields.CURRENT, indexCurrent);
            builder.field(CommonFields.TIME_OUT, indexTimeOutCount);
            builder.field(CommonFields.FAILED, indexFailedCount);
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(indexTimeInMillis); }
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
        builder.startObject("index");
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
