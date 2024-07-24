package com.es.monitor.indices;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;

import java.util.Arrays;
import java.util.List;

public class IndexMonitorSettings {
    public static final String CLUSTER_INDEXING_SLOW_METRIC_PREFIX = "jd.cluster.indexing.slow.metric";

    public static final String CLUSTER_CLUSTER_BIG_DOC_PREFIX = "jd.cluster.big_doc";

    public static final Setting<ByteSizeValue> SETTING_CLUSTER_BIG_DOC_THRESHOLD =
            Setting.byteSizeSetting(CLUSTER_CLUSTER_BIG_DOC_PREFIX +".threshold", new ByteSizeValue(0, ByteSizeUnit.KB), Setting.Property.NodeScope, Setting.Property.Dynamic);

    public static final Setting<Boolean> SETTING_BIG_DOC_LOG_ENABLE = Setting.boolSetting(CLUSTER_CLUSTER_BIG_DOC_PREFIX +".log.enable",
            true, Setting.Property.NodeScope, Setting.Property.Dynamic);

    public static final Setting<Boolean> SETTING_BIG_DOC_METRIC_ENABLE = Setting.boolSetting(CLUSTER_CLUSTER_BIG_DOC_PREFIX +".metric.enable",
            true, Setting.Property.NodeScope, Setting.Property.Dynamic);
    public static final Setting<ByteSizeValue> SETTING_INDEX_BIG_DOC_THRESHOLD =
            Setting.byteSizeSetting("jd.index.big_doc.threshold", new ByteSizeValue(20, ByteSizeUnit.KB), Setting.Property.NodeScope, Setting.Property.Dynamic);


    public static final Setting<Boolean> SETTING_SLOW_LOG_METRIC_ENABLE = Setting.boolSetting(CLUSTER_INDEXING_SLOW_METRIC_PREFIX +".enable",
            true, Setting.Property.NodeScope, Setting.Property.Dynamic);

    public static void addSettings(List<Setting<?>> settingsList) {
        settingsList.addAll(Arrays.asList(SETTING_CLUSTER_BIG_DOC_THRESHOLD, SETTING_BIG_DOC_LOG_ENABLE,
                SETTING_BIG_DOC_METRIC_ENABLE, SETTING_SLOW_LOG_METRIC_ENABLE));
    }
}
