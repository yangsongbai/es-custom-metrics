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
 *   "search" : {
 *     "search_total" : 504646572,
 *     "search_time_in_millis" : 950835864,
 *     "search_current" : 0,
 *     "search_failed" : 3,
 *     "search_time_out" : 3   //超时次数
 *   // "search_time_out_twenties_ms" : 3   //超时20ms的次数
 *   }
 */
public class SearchStats  implements Streamable, ToXContentFragment {
    private  Stats totalStats;

    public SearchStats() {
        this.totalStats = new Stats();
    }

    public SearchStats(Stats totalStats) {
        this.totalStats = totalStats;
    }

    public SearchStats(StreamInput in) throws IOException {
        totalStats = new Stats(in);
    }

    public void add(SearchStats other) {
        addTotals(other);
    }

    public static class Stats implements Streamable, ToXContentFragment {
        private long searchCount;
        private long searchTimeInMillis;
        private long searchCurrent;
        private long searchTimeOutCount;
        private long searchFailedCount;
        private long inAll;
        private HistogramStats sucHistogramStats;

        private HistogramStats failHistogramStats;

        Stats() {
            sucHistogramStats = new HistogramStats();
            failHistogramStats = new HistogramStats();
        }
        public Stats(StreamInput in) throws IOException {
            searchCount = in.readVLong();
            searchTimeInMillis = in.readVLong();
            searchCurrent = in.readVLong();
            searchTimeOutCount = in.readVLong();
            searchFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = new HistogramStats(in);
            failHistogramStats = new HistogramStats(in);
        }

        public Stats(long inAll, long searchCount, long searchTimeInMillis, long searchCurrent, long searchTimeOutCount, long searchFailedCount, HistogramMetricSnapshot snapshot, HistogramMetricSnapshot fail) {
            this.searchCount = searchCount;
            this.searchTimeInMillis = searchTimeInMillis;
            this.searchCurrent = searchCurrent;
            this.searchTimeOutCount = searchTimeOutCount;
            this.searchFailedCount = searchFailedCount;
            this.inAll = inAll;
            this.sucHistogramStats = new HistogramStats(snapshot);
            this.failHistogramStats = new HistogramStats(fail);
        }

        public void add(Stats stats) {
            this.searchCount += stats.searchCount;
            this.searchTimeInMillis += stats.searchTimeInMillis;
            this.searchCurrent += stats.searchCurrent;
            this.searchTimeOutCount += stats.searchTimeOutCount;
            this.searchFailedCount += stats.searchFailedCount;
            this.inAll += stats.inAll;
            this.sucHistogramStats.calculateTotal(stats.sucHistogramStats);
            this.failHistogramStats.calculateTotal(stats.failHistogramStats);
        }

        public static Stats readStats(StreamInput in) throws IOException {
            Stats stats = new Stats();
            stats.readFrom(in);
            return stats;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            searchCount = in.readVLong();
            searchTimeInMillis = in.readVLong();
            searchCurrent = in.readVLong();
            searchTimeOutCount = in.readVLong();
            searchFailedCount = in.readVLong();
            inAll = in.readVLong();
            sucHistogramStats = HistogramStats.readStats(in);
            failHistogramStats = HistogramStats.readStats(in);
        }


        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(searchCount);
            out.writeVLong(searchTimeInMillis);
            out.writeVLong(searchCurrent);
            out.writeVLong(searchTimeOutCount);
            out.writeVLong(searchFailedCount);
            out.writeVLong(inAll);
            sucHistogramStats.writeTo(out);
            failHistogramStats.writeTo(out);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(CommonFields.TOTAL, getSearchCount());
            builder.humanReadableField(CommonFields.TIME_IN_MILLIS, CommonFields.TIME, getTime());
            builder.field(CommonFields.CURRENT, getSearchCurrent());
            builder.field(CommonFields.TIME_OUT, getSearchTimeOutCount());
            builder.field(CommonFields.FAILED, getSearchFailedCount());
            builder.field(CommonFields.IN_ALL, inAll);
            builder.startObject(CommonFields.SUC_LATENCY);
            sucHistogramStats.toXContent(builder, params);
            builder.endObject();
            builder.startObject(CommonFields.FAIL_LATENCY);
            failHistogramStats.toXContent(builder, params);
            builder.endObject();
            return builder;
        }
        public TimeValue getTime() { return new TimeValue(searchTimeInMillis); }

        public long getSearchCount() {
            return searchCount;
        }

        public long getSearchTimeInMillis() {
            return searchTimeInMillis;
        }

        public long getSearchCurrent() {
            return searchCurrent;
        }

        public long getSearchTimeOutCount() {
            return searchTimeOutCount;
        }

        public long getSearchFailedCount() {
            return searchFailedCount;
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
        builder.startObject("search");
        totalStats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public Stats getTotalStats() {
        return totalStats;
    }
    public void addTotals(SearchStats searchStats) {
        if (searchStats == null) {
            return;
        }
        totalStats.add(searchStats.totalStats);
    }
}
