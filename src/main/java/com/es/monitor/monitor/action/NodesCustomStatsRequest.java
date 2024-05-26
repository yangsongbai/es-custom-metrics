package com.es.monitor.monitor.action;

import org.elasticsearch.action.support.nodes.BaseNodesRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/6 16:26
 */
public class NodesCustomStatsRequest extends BaseNodesRequest<NodesCustomStatsRequest>  {
    private final Set<String> requestedMetrics = new HashSet<>();
    private boolean clear;
    public NodesCustomStatsRequest() {
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        clear = in.readBoolean();
        requestedMetrics.clear();
        requestedMetrics.addAll(in.readStringList());
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalBoolean(clear);
        out.writeStringArray(requestedMetrics.toArray(new String[0]));
    }
    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    /**
     * Get the names of requested metrics, excluding indices, which are
     * handled separately.
     */
    public Set<String> requestedMetrics() {
        return new HashSet<>(requestedMetrics);
    }

    /**
     * Get stats from nodes based on the nodes ids specified. If none are passed, stats
     * for all nodes will be returned.
     */
    public NodesCustomStatsRequest(String... nodesIds) {
        super(nodesIds);
    }

    /**
     * Sets all the request flags.
     */
    public NodesCustomStatsRequest all() {
        this.requestedMetrics.addAll(Metric.allMetrics());
        clear = false;
        return this;
    }
    /**
     * An enumeration of the "core" sections of metrics that may be requested
     * from the nodes stats endpoint. Eventually this list will be pluggable.
     */
    public enum Metric {
        SEARCH("search"),
        BULK("bulk");

        private String metricName;

        Metric(String name) {
            this.metricName = name;
        }

        public String metricName() {
            return this.metricName;
        }

        boolean containedIn(Set<String> metricNames) {
            return metricNames.contains(this.metricName());
        }

        static Set<String> allMetrics() {
            return Arrays.stream(values()).map(Metric::metricName).collect(Collectors.toSet());
        }
    }
}
