package com.es.monitor.node.monitor.stats;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class CommonCustomStats implements Writeable, ToXContentFragment {
    @Nullable
    private BulkStats bulkStats;

    @Nullable
    private SearchStats searchStats;

    @Nullable
    private DeleteByQueryStats deleteByQueryStats;

    @Nullable
    private UpdateByQueryStats updateByQueryStats;
    @Nullable
    private  GetStats getStats;
    @Nullable
    private MultiGetStats multiGetStats;

    @Nullable
    private IndexRequestStats indexRequestStats;

    @Nullable
    private  MultiSearchStats multiSearchStats;

    public CommonCustomStats() {
        this.bulkStats = new BulkStats();
        this.searchStats = new SearchStats();
        this.deleteByQueryStats = new DeleteByQueryStats();
        this.updateByQueryStats = new UpdateByQueryStats();
        this.getStats = new GetStats();
        this.multiGetStats = new MultiGetStats();
        this.indexRequestStats = new IndexRequestStats();
        this.multiSearchStats = new MultiSearchStats();
    }

    public CommonCustomStats(BulkStats bulkStats, SearchStats searchStats, DeleteByQueryStats deleteByQueryStats,
                             UpdateByQueryStats updateByQueryStats, GetStats getStats,
                             MultiGetStats multiGetStats, IndexRequestStats indexRequestStats,
                             MultiSearchStats multiSearchStats) {
        this.bulkStats = bulkStats;
        this.searchStats = searchStats;
        this.deleteByQueryStats = deleteByQueryStats;
        this.updateByQueryStats = updateByQueryStats;
        this.getStats = getStats;
        this.multiGetStats = multiGetStats;
        this.indexRequestStats = indexRequestStats;
        this.multiSearchStats = multiSearchStats;
    }

    public void add(CommonCustomStats stats) {
        if (bulkStats == null) {
            if (stats.getBulkStats() != null) {
                bulkStats = new BulkStats();
                bulkStats.add(stats.getBulkStats());
            }
        } else {
            bulkStats.add(stats.getBulkStats());
        }
        if (searchStats == null) {
            if (stats.getSearchStats() != null) {
                searchStats = new SearchStats();
                searchStats.add(stats.getSearchStats());
            }
        } else {
            searchStats.add(stats.getSearchStats());
        }
        if (deleteByQueryStats == null) {
            if (stats.getDeleteByQueryStats() != null) {
                deleteByQueryStats = new DeleteByQueryStats();
                deleteByQueryStats.add(stats.getDeleteByQueryStats());
            }
        } else {
            deleteByQueryStats.add(stats.getDeleteByQueryStats());
        }
        if (updateByQueryStats == null) {
            if (stats.getUpdateByQueryStats() != null) {
                updateByQueryStats = new UpdateByQueryStats();
                updateByQueryStats.add(stats.getUpdateByQueryStats());
            }
        } else {
            updateByQueryStats.add(stats.getUpdateByQueryStats());
        }
        if (getStats == null) {
            if (stats.getGetStats() != null) {
                getStats = new GetStats();
                getStats.add(stats.getGetStats());
            }
        } else {
            getStats.add(stats.getGetStats());
        }
        if (multiGetStats == null) {
            if (stats.getMultiGetStats() != null) {
                multiGetStats = new MultiGetStats();
                multiGetStats.add(stats.getMultiGetStats());
            }
        } else {
            multiGetStats.add(stats.getMultiGetStats());
        }
        if (indexRequestStats == null) {
            if (stats.getIndexRequestStats() != null) {
                indexRequestStats = new IndexRequestStats();
                indexRequestStats.add(stats.getIndexRequestStats());
            }
        } else {
            indexRequestStats.add(stats.getIndexRequestStats());
        }
        if (multiSearchStats == null) {
            if (stats.getMultiSearchStats() != null) {
                multiSearchStats = new MultiSearchStats();
                multiSearchStats.add(stats.getMultiSearchStats());
            }
        } else {
            multiSearchStats.add(stats.getMultiSearchStats());
        }
    }



    public BulkStats getBulkStats() {
        return bulkStats;
    }

    public SearchStats getSearchStats() {
        return searchStats;
    }

    public DeleteByQueryStats getDeleteByQueryStats() {
        return deleteByQueryStats;
    }

    public UpdateByQueryStats getUpdateByQueryStats() {
        return updateByQueryStats;
    }

    public GetStats getGetStats() {
        return getStats;
    }

    public MultiGetStats getMultiGetStats() {
        return multiGetStats;
    }

    public IndexRequestStats getIndexRequestStats() {
        return indexRequestStats;
    }

    public MultiSearchStats getMultiSearchStats() {
        return multiSearchStats;
    }

    public CommonCustomStats(StreamInput in) throws IOException {
        bulkStats = in.readOptionalStreamable(BulkStats::new);
        searchStats = in.readOptionalStreamable(SearchStats::new);
        deleteByQueryStats = in.readOptionalStreamable(DeleteByQueryStats::new);
        updateByQueryStats = in.readOptionalStreamable(UpdateByQueryStats::new);
        getStats = in.readOptionalStreamable(GetStats::new);
        multiGetStats = in.readOptionalStreamable(MultiGetStats::new);
        indexRequestStats = in.readOptionalStreamable(IndexRequestStats::new);
        multiSearchStats = in.readOptionalStreamable(MultiSearchStats::new);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalStreamable(bulkStats);
        out.writeOptionalStreamable(searchStats);
        out.writeOptionalStreamable(deleteByQueryStats);
        out.writeOptionalStreamable(updateByQueryStats);
        out.writeOptionalStreamable(getStats);
        out.writeOptionalStreamable(multiGetStats);
        out.writeOptionalStreamable(indexRequestStats);
        out.writeOptionalStreamable(multiSearchStats);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        final Stream<ToXContent> stream = Arrays.stream(new ToXContent[] {
                        bulkStats, searchStats, deleteByQueryStats, updateByQueryStats, getStats, multiGetStats, indexRequestStats, multiSearchStats})
                .filter(Objects::nonNull);
        for (ToXContent toXContent : ((Iterable<ToXContent>)stream::iterator)) {
            toXContent.toXContent(builder, params);
        }
        return builder;
    }
}
