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
        Stats() {}

        public Stats(StreamInput in) throws IOException {
            searchCount = in.readVLong();
            searchTimeInMillis = in.readVLong();
            searchCurrent = in.readVLong();
            searchTimeOutCount = in.readVLong();
            searchFailedCount = in.readVLong();
            inAll = in.readVLong();
        }

        public Stats(long inAll, long searchCount, long searchTimeInMillis, long searchCurrent, long searchTimeOutCount, long searchFailedCount) {
            this.searchCount = searchCount;
            this.searchTimeInMillis = searchTimeInMillis;
            this.searchCurrent = searchCurrent;
            this.searchTimeOutCount = searchTimeOutCount;
            this.searchFailedCount = searchFailedCount;
            this.inAll = inAll;
        }

        public void add(Stats stats) {
            this.searchCount += stats.searchCount;
            this.searchTimeInMillis += stats.searchTimeInMillis;
            this.searchCurrent += stats.searchCurrent;
            this.searchTimeOutCount += stats.searchTimeOutCount;
            this.searchFailedCount += stats.searchFailedCount;
            this.inAll += stats.inAll;
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
        }


        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(searchCount);
            out.writeVLong(searchTimeInMillis);
            out.writeVLong(searchCurrent);
            out.writeVLong(searchTimeOutCount);
            out.writeVLong(searchFailedCount);
            out.writeVLong(inAll);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.SEARCH_TOTAL, getSearchCount());
            builder.humanReadableField(Fields.SEARCH_TIME_IN_MILLIS, Fields.SEARCH_TIME, getTime());
            builder.field(Fields.SEARCH_CURRENT, getSearchCurrent());
            builder.field(Fields.SEARCH_TIME_OUT, getSearchTimeOutCount());
            builder.field(Fields.SEARCH_FAILED, getSearchFailedCount());
            builder.field(Fields.IN_ALL, inAll);
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
    static final class Fields {
        static final String SEARCH = "search";
        static final String SEARCH_TOTAL = "total";
        static final String SEARCH_TIME = "time";
        static final String SEARCH_TIME_IN_MILLIS = "time_in_millis";
        static final String SEARCH_CURRENT = "current";
        static final String SEARCH_TIME_OUT = "time_out";
        static final String SEARCH_TIME_OUT_500MS = "time_out_500ms";
        static final String SEARCH_TIME_OUT_1000MS = "time_out_1000ms";
        static final String SEARCH_FAILED = "failed";
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
        builder.startObject(Fields.SEARCH);
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
