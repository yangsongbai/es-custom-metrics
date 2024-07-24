package com.es.monitor.indices.stats;


import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.search.stats.SearchStats;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BigDocStats  implements Streamable, ToXContentFragment {
    public static class Stats implements Writeable, ToXContentFragment {

        private long indexingBig;
        private long delBig;

        public Stats() {
        }

        public Stats(long indexingBig, long delBig) {
            this.indexingBig = indexingBig;
            this.delBig = delBig;
        }

        private Stats(StreamInput in) throws IOException {
            indexingBig = in.readVLong();
            delBig = in.readVLong();
        }

        public void add(Stats stats) {
            indexingBig += stats.indexingBig;
            delBig += stats.delBig;
        }

        public void addForClosingShard(Stats stats) {
            indexingBig += stats.indexingBig;
            delBig += stats.delBig;
        }

        public long getIndexingBig() {
            return indexingBig;
        }

        public long getDelBig() {
            return delBig;
        }

        public void setIndexingBig(long indexingBig) {
            this.indexingBig = indexingBig;
        }

        public void setDelBig(long delBig) {
            this.delBig = delBig;
        }

        public static Stats readStats(StreamInput in) throws IOException {
            return new Stats(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(indexingBig);
            out.writeVLong(delBig);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.field(Fields.DEL_TOTAL, delBig);
            builder.field(Fields.INDEXING_TOTAL, indexingBig);
            return builder;
        }
    }

    private Stats totalStats;
    @Nullable
    private Map<String, Stats> groupStats;

    public BigDocStats() {
        totalStats = new Stats();
        groupStats = new ConcurrentHashMap<>();
    }

    public Stats getTotalStats() {
        return totalStats;
    }

    public BigDocStats(Stats totalStats, @Nullable Map<String, Stats> groupStats) {
        this.totalStats = totalStats;
        this.groupStats = groupStats;
    }

    public BigDocStats(@Nullable Map<String, Stats> groupStats) {
        this.totalStats = new Stats();
        this.groupStats = groupStats;
    }

    public void add(BigDocStats bigDocStats) {
        if (bigDocStats == null) {
            return;
        }
        addTotals(bigDocStats);
        if (bigDocStats.groupStats != null && !bigDocStats.groupStats.isEmpty()) {
            for (Map.Entry<String, Stats> entry : bigDocStats.groupStats.entrySet()) {
                groupStats.putIfAbsent(entry.getKey(), new Stats());
                groupStats.get(entry.getKey()).add(entry.getValue());
            }
        }
    }

    public void addTotals(BigDocStats bigDocStats) {
        if (bigDocStats == null) {
            return;
        }
        totalStats.add(bigDocStats.totalStats);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        totalStats = Stats.readStats(in);
        if (in.readBoolean()) {
            groupStats = in.readMap(StreamInput::readString, Stats::readStats);
        }
    }

    @Nullable
    public Map<String, Stats> getGroupStats() {
        return this.groupStats != null ? Collections.unmodifiableMap(this.groupStats) : new ConcurrentHashMap<>();
    }

    public long getIndexingBigTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            total += entry.getValue().indexingBig;
        }
        return total;
    }

    public long getDelBigTotal(){
        long total = 0;
        if (groupStats == null || groupStats.isEmpty()) {
            return total;
        }
        Set<Map.Entry<String, Stats>> entrySet = groupStats.entrySet();
        for (Map.Entry<String, Stats> entry : entrySet){
            total += entry.getValue().delBig;
        }
        return total;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        totalStats.writeTo(out);
        if (groupStats == null || groupStats.isEmpty()) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeMap(groupStats, StreamOutput::writeString, (stream, stats) -> stats.writeTo(stream));
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.BIG_DOC);
        totalStats.toXContent(builder, params);
        if (groupStats != null && !groupStats.isEmpty()) {
            builder.startObject(Fields.GROUPS);
            for (Map.Entry<String, Stats> entry : groupStats.entrySet()) {
                builder.startObject(entry.getKey());
                entry.getValue().toXContent(builder, params);
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public XContentBuilder toXContentByIndex(XContentBuilder builder, Params params, String index) throws IOException {
        Stats stats = null;
        if (groupStats != null && !groupStats.isEmpty()) {
            stats = groupStats.get(index);
        }
        if (stats == null) stats = new Stats();
        stats.toXContent(builder, params);
        return builder;
    }


    public XContentBuilder toXContentTotal(XContentBuilder builder, Params params) throws IOException {
        Stats stats = new Stats(this.getIndexingBigTotal(), this.getDelBigTotal());
        stats.toXContent(builder, params);
        return builder;
    }

    static final class Fields {
        static final String BIG_DOC = "big_doc";
        static final String DEL_TOTAL = "del_total";
        static final String INDEXING_TOTAL = "indexing_total";
        static final String GROUPS = "groups";
    }

    @Override
    public String toString() {
        return Strings.toString(this, true, true);
    }
}
