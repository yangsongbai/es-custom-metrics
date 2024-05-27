package com.es.monitor.node.monitor.stats;

import com.es.monitor.node.monitor.metric.HistogramMetricSnapshot;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by
 *
 * @Author : yangsongbai
 * @create 2024/5/26 15:44
 */
public class HistogramStats implements Streamable, ToXContentFragment {
    private double avg;
    private double median;
    private double max;
    private double min;
    private double tp90;

    private double tp99;

    private double tp999;


    HistogramStats() {}

    public HistogramStats(StreamInput in) throws IOException {
        avg = in.readDouble();
        median = in.readDouble();
        max = in.readDouble();
        min = in.readDouble();
        tp90 = in.readDouble();
        tp99 = in.readDouble();
        tp999 = in.readDouble();
    }

    public HistogramStats(HistogramMetricSnapshot snapshot) {
        this.avg = snapshot.avg();
        this.median = snapshot.median();
        this.max = snapshot.max();
        this.min = snapshot.min();
        this.tp90 = snapshot.tp90();
        this.tp99 = snapshot.tp99();
        this.tp999 = snapshot.tp999();
    }

    public static HistogramStats readStats(StreamInput in) throws IOException {
        return new HistogramStats(in);
    }

    public void add(HistogramStats stats) {
        this.avg += stats.avg;
        this.median += stats.median;
        this.max += stats.max;
        this.min += stats.min;
        this.tp90 += stats.tp90;
        this.tp99 += stats.tp99;
        this.tp999 += stats.tp999;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        avg = in.readDouble();
        median = in.readDouble();
        max = in.readDouble();
        min = in.readDouble();
        tp90 = in.readDouble();
        tp99 = in.readDouble();
        tp999 = in.readDouble();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeDouble(avg);
        out.writeDouble(median);
        out.writeDouble(max);
        out.writeDouble(min);
        out.writeDouble(tp90);
        out.writeDouble(tp99);
        out.writeDouble(tp999);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(HistogramFields.AVG, avg);
        builder.field(HistogramFields.MEDIAN, median);
        builder.field(HistogramFields.MAX, max);
        builder.field(HistogramFields.MIN, min);
        builder.field(HistogramFields.TP90, tp90);
        builder.field(HistogramFields.TP99, tp99);
        builder.field(HistogramFields.TP999, tp999);
        return builder;
    }

    @Override
    public String toString() {
        return "HistogramStats{" +
                "avg=" + avg +
                ", median=" + median +
                ", max=" + max +
                ", min=" + min +
                ", tp90=" + tp90 +
                ", tp99=" + tp99 +
                ", tp999=" + tp999 +
                '}';
    }
}
