package com.es.monitor.indices.search;

import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.index.shard.SearchOperationListener;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2024/5/27 16:58
 */
public class SearchOperationTraceService extends AbstractLifecycleComponent implements SearchOperationListener {
    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void doClose() throws IOException {

    }

    @Override
    public void onQueryPhase(SearchContext searchContext, long tookInNanos) {

    }

    @Override
    public void onFetchPhase(SearchContext searchContext, long tookInNanos) {

    }
}
