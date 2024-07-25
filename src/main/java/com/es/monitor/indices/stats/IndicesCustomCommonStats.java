package com.es.monitor.indices.stats;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.es.monitor.indices.stats.BigDocStats.Fields.BIG_DOC;
import static com.es.monitor.indices.stats.BigDocStats.Fields.INDEXING_TOTAL;
import static com.es.monitor.indices.stats.IndexingSlowLogStats.Fields.INDEXING_SLOW;
import static com.es.monitor.indices.stats.SearchSlowLogStats.Fields.SEARCH_SLOW;


public class IndicesCustomCommonStats implements Writeable, ToXContentFragment {
    private  BigDocStats bigDocStats;
    private   IndexingSlowLogStats indexingSlowLogStats;

    private   SearchSlowLogStats searchSlowLogStats;

    public IndicesCustomCommonStats(BigDocStats bigDocStats, IndexingSlowLogStats indexingSlowLogStats, SearchSlowLogStats slowLogStats) {
        this.bigDocStats = bigDocStats;
        this.indexingSlowLogStats = indexingSlowLogStats;
        this.searchSlowLogStats = slowLogStats;
    }

    public IndicesCustomCommonStats() {
        this.bigDocStats = new BigDocStats();
        this.indexingSlowLogStats = new IndexingSlowLogStats();
        this.searchSlowLogStats = new SearchSlowLogStats();
    }

    public IndicesCustomCommonStats(StreamInput in) throws IOException {
        bigDocStats = in.readOptionalStreamable(BigDocStats::new);
        indexingSlowLogStats = in.readOptionalStreamable(IndexingSlowLogStats::new);
        searchSlowLogStats = in.readOptionalStreamable(SearchSlowLogStats::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalStreamable(bigDocStats);
        out.writeOptionalStreamable(indexingSlowLogStats);
        out.writeOptionalStreamable(searchSlowLogStats);
    }

    public Set<String> getAllIndices() {
        Set<String> indices = new HashSet<>();
        if (bigDocStats != null) {
            Set<String> indicesBigDoc = bigDocStats.getGroupStats().keySet();
            indices.addAll(indicesBigDoc);
        }

        if (indexingSlowLogStats != null) {
            Set<String> indicesInIndexing = indexingSlowLogStats.getGroupStats().keySet();
            indices.addAll(indicesInIndexing);
        }

        if (searchSlowLogStats != null) {
            Set<String> indicesInSearch = searchSlowLogStats.getGroupStats().keySet();
            indices.addAll(indicesInSearch);
        }

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

            builder.startObject(INDEXING_SLOW);
            builder = indexingSlowLogStats.toXContentByIndex(builder, params, index);
            builder.endObject();


            builder.startObject(SEARCH_SLOW);
            builder = searchSlowLogStats.toXContentByIndex(builder, params, index);
            builder.endObject();

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

        builder.startObject(INDEXING_SLOW);
        builder = indexingSlowLogStats.toXContentTotal(builder, params);
        builder.endObject();


        builder.startObject(SEARCH_SLOW);
        builder = searchSlowLogStats.toXContentTotal(builder, params);
        builder.endObject();

        return builder;
    }

    public IndexingSlowLogStats getIndexingSlowLogStats() {
        return indexingSlowLogStats;
    }

    public SearchSlowLogStats getSearchSlowLogStats() {
        return searchSlowLogStats;
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

        if (indexingSlowLogStats == null) {
            if (stats.getIndexingSlowLogStats() != null) {
                indexingSlowLogStats = new IndexingSlowLogStats();
                indexingSlowLogStats.add(stats.getIndexingSlowLogStats());
            }
        }else{
            indexingSlowLogStats.add(stats.getIndexingSlowLogStats());
        }
        if (searchSlowLogStats == null) {
            if (stats.getSearchSlowLogStats() != null) {
                searchSlowLogStats = new SearchSlowLogStats();
                searchSlowLogStats.add(stats.getSearchSlowLogStats());
            }
        }else{
            searchSlowLogStats.add(stats.getSearchSlowLogStats());
        }
    }
}
