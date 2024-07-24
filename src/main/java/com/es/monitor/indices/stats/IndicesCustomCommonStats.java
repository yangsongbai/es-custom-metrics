package com.es.monitor.indices.stats;

import com.es.monitor.node.monitor.stats.CommonCustomStats;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.es.monitor.indices.stats.BigDocStats.Fields.BIG_DOC;


public class IndicesCustomCommonStats implements Writeable, ToXContentFragment {
    private  BigDocStats bigDocStats;

    public IndicesCustomCommonStats(BigDocStats bigDocStats) {
        this.bigDocStats = bigDocStats;
    }

    public IndicesCustomCommonStats() {
        this.bigDocStats = new BigDocStats();
    }

    public IndicesCustomCommonStats(StreamInput in) throws IOException {
        bigDocStats = in.readOptionalStreamable(BigDocStats::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalStreamable(bigDocStats);
    }

    public Set<String> getAllIndices() {
        if (bigDocStats == null) {
            System.out.println("bigDocStats is empty");
            return new HashSet<>();
        }
       Set<String> indices = bigDocStats.getGroupStats().keySet();
       return indices;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        Set<String> indices = getAllIndices();
        for (String index : indices){
            builder.startObject(index);
            builder.startObject(BIG_DOC);
            builder = bigDocStats.toXContentByIndex(builder, params, index);
            builder.endObject();
            //其他指标

            builder.endObject();
        }
/*        final Stream<ToXContent> stream = Arrays.stream(new ToXContent[] {
                        bigDocStats})
                .filter(Objects::nonNull);
        for (ToXContent toXContent : ((Iterable<ToXContent>)stream::iterator)) {
            toXContent.toXContent(builder, params);
        }*/
        return builder;
    }



    public XContentBuilder toXContentTotal(XContentBuilder builder, Params params)  throws IOException {
        builder.startObject(BIG_DOC);
        builder = bigDocStats.toXContentTotal(builder, params);
        builder.endObject();
        //其他指标
        return builder;
    }

    public BigDocStats getBigDocStats() {
        return bigDocStats;
    }

    public void add(IndicesCustomCommonStats stats) {
        if (bigDocStats == null) {
            if (stats.getBigDocStats() != null) {
                bigDocStats = new BigDocStats();
                bigDocStats.add(stats.getBigDocStats());
            }
        } else {
            bigDocStats.add(stats.getBigDocStats());
        }
    }
}
