package com.es.monitor.monitor.stats;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/28 14:52
 */
public class NodeCustomIndicesStats  implements Writeable, ToXContentFragment  {
     CommonCustomStats stats;

    public NodeCustomIndicesStats(CommonCustomStats stats) {
        this.stats = stats;
    }

    public NodeCustomIndicesStats() {
        this.stats = new CommonCustomStats();
    }

    public CommonCustomStats getStats() {
        return stats;
    }

    public BulkStats getBulkStats(){
         return stats.getBulkStats();
     }


    public DeleteByQueryStats getDeleteByQueryStats(){
        return stats.getDeleteByQueryStats();
    }

    public GetStats getGetStats(){
        return stats.getGetStats();
    }


    public IndexRequestStats getIndexRequestStats(){
        return stats.getIndexRequestStats();
    }


    public MultiSearchStats getMultiSearchStats(){
        return stats.getMultiSearchStats();
    }

    public MultiGetStats getMultiGetStats(){
        return stats.getMultiGetStats();
    }

    public SearchStats getSearchStats(){
        return stats.getSearchStats();
    }

    public UpdateByQueryStats getUpdateByQueryStats(){
        return stats.getUpdateByQueryStats();
    }

    public NodeCustomIndicesStats(StreamInput in) throws IOException {
        stats = new CommonCustomStats(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        stats.writeTo(out);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.INDICES);
        stats.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    public void add(NodeCustomIndicesStats indices) {
        this.stats.add(indices.stats);
    }

    static final class Fields {
        static final String INDICES = "indices";
    }
}
