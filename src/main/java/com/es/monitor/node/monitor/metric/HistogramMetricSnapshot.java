package com.es.monitor.node.monitor.metric;

public class HistogramMetricSnapshot {
        private Snapshot snapshot;

        public HistogramMetricSnapshot(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        public double avg() {
            return snapshot.getMean();
        }

        public double median() {
            return snapshot.getValue(0.5);
        }

        public double max() {
            return snapshot.getMax();
        }

        public double min() {
            return snapshot.getMin();
        }

        public double tp90() {
            return snapshot.getValue(0.9);
        }

        public double tp99() {
            return snapshot.getValue(0.99);
        }

        public double tp999() {
        return snapshot.getValue(0.999);
    }
    }