package com.es.monitor.indices.index;

import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexingOperationListener;
import org.elasticsearch.index.shard.ShardId;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2024/5/27 16:59
 */
public class IndexingOperationTraceService  extends AbstractLifecycleComponent implements IndexingOperationListener {
    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void doClose() throws IOException {

    }


    /**
     *  安索引级别统计 tp999，请求耗时
     * @param shardId
     * @param index
     * @param result
     */
    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Engine.IndexResult result) {
         result.getTook();
    }

    @Override
    public void postIndex(ShardId shardId, Engine.Index index, Exception ex) {

    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Engine.DeleteResult result) {

    }

    @Override
    public void postDelete(ShardId shardId, Engine.Delete delete, Exception ex) {

    }
}
