package com.es.monitor.access;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsAction;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction;
import org.elasticsearch.action.bulk.BulkAction;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.main.MainAction;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Setting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 13:59
 */
public class AccessSettings {

    //access rest log setting
    public static final Setting<Boolean> SETTING_ACCESS_LOG_ENABLE = Setting.boolSetting("access.log.enable",
            false, Setting.Property.NodeScope, Setting.Property.Dynamic);

    public static final Setting<List<String>> SETTING_ACCESS_LOG_ACTION_INCLUDE =
            Setting.listSetting("access.log.action.include", Collections.emptyList(),
                    Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<List<String>> SETTING_ACCESS_LOG_ACTION_EXCLUDE =
            Setting.listSetting("access.log.action.exclude",
                    Arrays.asList(IndicesStatsAction.NAME, ClusterStatsAction.NAME, NodesInfoAction.NAME,
                            NodesStatsAction.NAME, ClusterStateAction.NAME, ClusterHealthAction.NAME, MainAction.NAME, BulkAction.NAME, IndexAction.NAME
                    ), Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<List<String>> SETTING_ACCESS_LOG_URI_INCLUDE =
            Setting.listSetting("access.log.uri.include", Collections.emptyList(),
                    Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<List<String>> SETTING_ACCESS_LOG_URI_EXCLUDE =
            Setting.listSetting("access.log.uri.exclude",
                    Collections.emptyList(), Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<List<String>> SETTING_ACCESS_LOG_USER_INCLUDE =
            Setting.listSetting("access.log.user.include", Collections.emptyList(),
                    Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<List<String>> SETTING_ACCESS_LOG_USER_EXCLUDE =
            Setting.listSetting("access.log.user.exclude",
                    Arrays.asList("supervisor","kibanaUser"), Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<List<String>> SETTING_ACCESS_LOG_IP_INCLUDE =
            Setting.listSetting("access.log.ip.include", Collections.emptyList(),
                    Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);
    public static final Setting<List<String>> SETTING_ACCESS_LOG_IP_EXCLUDE =
            Setting.listSetting("access.log.ip.exclude",
                    Collections.emptyList(), Function.identity(), Setting.Property.Dynamic, Setting.Property.NodeScope);

    public static final Setting<Boolean> SETTING_ACCESS_METRIC_ENABLE = Setting.boolSetting("access.metric.enable",
            true, Setting.Property.NodeScope, Setting.Property.Dynamic);

    private Boolean accessLogEnable;
    private volatile String[] accessLogUriInclude;
    private volatile String[] accessLogUriExclude;

    private volatile String[] accessLogActionInclude;
    private volatile String[] accessLogActionExclude;

    private volatile String[] accessLogIpInclude;
    private volatile String[] accessLogIpExclude;

    private volatile String[] accessLogUserInclude;
    private volatile String[] accessLogUserExclude;

    private Boolean accessMetricEnable;

    public AccessSettings(ClusterService clusterService) {
        this.accessLogEnable = SETTING_ACCESS_LOG_ENABLE.get(clusterService.getSettings());
        setAccessLogActionInclude(SETTING_ACCESS_LOG_ACTION_INCLUDE.get(clusterService.getSettings()));
        setAccessLogActionExclude(SETTING_ACCESS_LOG_ACTION_EXCLUDE.get(clusterService.getSettings()));

        setAccessLogUriInclude(SETTING_ACCESS_LOG_URI_INCLUDE.get(clusterService.getSettings()));
        setAccessLogUriExclude(SETTING_ACCESS_LOG_URI_EXCLUDE.get(clusterService.getSettings()));

        setAccessLogIpInclude(SETTING_ACCESS_LOG_IP_INCLUDE.get(clusterService.getSettings()));
        setAccessLogIpExclude(SETTING_ACCESS_LOG_IP_EXCLUDE.get(clusterService.getSettings()));

        setAccessLogUserInclude(SETTING_ACCESS_LOG_USER_INCLUDE.get(clusterService.getSettings()));
        setAccessLogUserExclude(SETTING_ACCESS_LOG_USER_EXCLUDE.get(clusterService.getSettings()));

        this.accessMetricEnable = SETTING_ACCESS_METRIC_ENABLE.get(clusterService.getSettings());
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_ENABLE, this::setAccessLogEnable);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_ACTION_INCLUDE, this::setAccessLogActionInclude);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_ACTION_EXCLUDE, this::setAccessLogActionExclude);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_URI_INCLUDE, this::setAccessLogUriInclude);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_URI_EXCLUDE, this::setAccessLogUriExclude);

        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_IP_INCLUDE, this::setAccessLogIpInclude);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_IP_EXCLUDE, this::setAccessLogIpExclude);

        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_USER_INCLUDE, this::setAccessLogUserInclude);
        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_LOG_USER_EXCLUDE, this::setAccessLogUserExclude);

        clusterService.getClusterSettings().addSettingsUpdateConsumer(SETTING_ACCESS_METRIC_ENABLE, this::setAccessMetricEnable);
    }

    public static List<Setting<?>> getSettings() {
        return Arrays.asList(SETTING_ACCESS_LOG_USER_INCLUDE,SETTING_ACCESS_LOG_USER_EXCLUDE,SETTING_ACCESS_LOG_IP_INCLUDE,SETTING_ACCESS_LOG_IP_EXCLUDE,SETTING_ACCESS_LOG_URI_INCLUDE,
                SETTING_ACCESS_LOG_URI_EXCLUDE,SETTING_ACCESS_LOG_ENABLE,SETTING_ACCESS_LOG_ACTION_INCLUDE,
                SETTING_ACCESS_LOG_ACTION_EXCLUDE,SETTING_ACCESS_METRIC_ENABLE);
    }

    private void setAccessLogUserExclude(List<String> exclude) {
        this.accessLogUserExclude =  exclude.toArray(Strings.EMPTY_ARRAY);
    }

    private void setAccessLogUserInclude(List<String> include) {
        this.accessLogUserInclude =  include.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogUserInclude() {
        return accessLogUserInclude;
    }

    public String[] getAccessLogUserExclude() {
        return accessLogUserExclude;
    }

    public Boolean getAccessLogEnable() {
        return accessLogEnable;
    }

    public void setAccessLogEnable(Boolean accessLogEnable) {
        this.accessLogEnable = accessLogEnable;
    }


    public String[] getAccessLogUriInclude() {
        return accessLogUriInclude;
    }

    public void setAccessLogUriInclude(List<String> accessLogUriInclude) {
        this.accessLogUriInclude =  accessLogUriInclude.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogUriExclude() {
        return accessLogUriExclude;
    }

    public void setAccessLogUriExclude(List<String>  accessLogUriExclude) {
        this.accessLogUriExclude = accessLogUriExclude.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogIpInclude() {
        return accessLogIpInclude;
    }

    public void setAccessLogIpInclude(List<String> accessLogIpInclude) {
        this.accessLogIpInclude = accessLogIpInclude.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogIpExclude() {
        return accessLogIpExclude;
    }

    public void setAccessLogIpExclude(List<String> accessLogIpExclude) {
        this.accessLogIpExclude = accessLogIpExclude.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogActionInclude() {
        return accessLogActionInclude;
    }

    public void setAccessLogActionInclude(List<String> accessLogInclude) {
        this.accessLogActionInclude = accessLogInclude.toArray(Strings.EMPTY_ARRAY);
    }

    public void setAccessLogActionExclude(List<String> accessLogExclude) {
        this.accessLogActionExclude =  accessLogExclude.toArray(Strings.EMPTY_ARRAY);
    }

    public String[] getAccessLogActionExclude() {
        return accessLogActionExclude;
    }

    public Boolean getAccessMetricEnable() {
        return accessMetricEnable;
    }

    public void setAccessMetricEnable(Boolean accessMetricEnable) {
        this.accessMetricEnable = accessMetricEnable;
    }
}
