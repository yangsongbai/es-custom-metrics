package com.es.monitor;

import com.es.monitor.access.AccessSettings;
import com.es.monitor.access.AccessTrail;
import com.es.monitor.access.AccessTrailService;
import com.es.monitor.access.logfile.LoggingAccessTrail;
import com.es.monitor.allocation.IndexAllocationExplainAction;
import com.es.monitor.allocation.RestIndexAllocationExplainAction;
import com.es.monitor.allocation.TransportIndexAllocationExplainAction;
import com.es.monitor.common.filter.JPackFilter;
import com.es.monitor.common.handler.BulkRequestHandler;
import com.es.monitor.common.handler.DeleteByQueryRequestHandler;
import com.es.monitor.common.handler.GetRequestHandler;
import com.es.monitor.common.handler.IndexRequestHandler;
import com.es.monitor.common.handler.MultiGetRequestHandler;
import com.es.monitor.common.handler.MultiSearchRequestHandler;
import com.es.monitor.common.handler.SearchRequestHandler;
import com.es.monitor.common.handler.UpdateByQueryRequestHandler;
import com.es.monitor.monitor.action.NodesCustomStatsAction;
import com.es.monitor.monitor.action.RestNodesCustomStatsAction;
import com.es.monitor.monitor.action.TransportNodesCustomStatsAction;
import com.es.monitor.monitor.service.CustomBulkService;
import com.es.monitor.monitor.service.CustomDeleteByQueryService;
import com.es.monitor.monitor.service.CustomGetService;
import com.es.monitor.monitor.service.CustomIndexService;
import com.es.monitor.monitor.service.CustomMultiGetService;
import com.es.monitor.monitor.service.CustomMultiSearchService;
import com.es.monitor.monitor.service.CustomSearchService;
import com.es.monitor.monitor.service.CustomUpdateByQueryService;
import com.es.monitor.monitor.service.NodeCustomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SetOnce;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.ClusterPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by
 *
 * @Author : yangsongbai
 * @create 2022/12/2 19:46
 */
public class EsMetricsPlugin  extends Plugin implements ActionPlugin, ClusterPlugin {
    private static final Logger logger = LogManager.getLogger(EsMetricsPlugin.class);
    protected final Settings settings;
    private final SetOnce<JPackFilter> jPackFilter  = new SetOnce<>();
    private final SetOnce<NodeCustomService> nodeCustomService = new SetOnce<>();
    private final SetOnce<AccessTrailService> accessTrailService = new SetOnce<>();
    private final SetOnce<CustomBulkService> customBulkService = new SetOnce<>();
    private final SetOnce<CustomSearchService> customSearchService = new SetOnce<>();

    private final SetOnce<CustomDeleteByQueryService> deleteByQueryService = new SetOnce<>();
    private final SetOnce<CustomGetService> getService = new SetOnce<>();
    private final SetOnce<CustomIndexService> indexService = new SetOnce<>();
    private final SetOnce<CustomMultiGetService> multiGetService = new SetOnce<>();
    private final SetOnce<CustomMultiSearchService> multiSearchService = new SetOnce<>();
    private final SetOnce<CustomUpdateByQueryService> updateByQueryService = new SetOnce<>();
    private final SetOnce<AccessSettings> accessSettings  = new SetOnce<>();

    @Inject
    public EsMetricsPlugin(final Settings settings, final Path configPath) {
        this.settings = settings;
    }

    @Override
    public Collection<Object> createComponents(
            Client client, ClusterService clusterService, ThreadPool threadPool, ResourceWatcherService resourceWatcherService, ScriptService scriptService, NamedXContentRegistry xContentRegistry, Environment environment, NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry
    ) {
        List<Object> components = new ArrayList<>();

        logger.info("monitor Plugin {} is initiating", EsMetricsPlugin.class.getName());

        AccessSettings accessSettings = new AccessSettings(clusterService);
        this.accessSettings.set(accessSettings);
        AccessTrail loggingAccessTrail =  new LoggingAccessTrail(settings, clusterService, threadPool, this.accessSettings.get());
        final List<AccessTrail> accessTrails = Arrays.asList(loggingAccessTrail);
        this.accessTrailService.set(new AccessTrailService(accessTrails));
        components.add(this.accessTrailService.get());

        CustomBulkService customBulkService = new  CustomBulkService();
        CustomSearchService customSearchService = new CustomSearchService();

        CustomDeleteByQueryService deleteByQueryService = new  CustomDeleteByQueryService();
        CustomGetService  getService = new CustomGetService();
        CustomIndexService indexService = new  CustomIndexService();
        CustomMultiGetService multiGetService = new CustomMultiGetService();

        CustomMultiSearchService multiSearchService = new  CustomMultiSearchService();
        CustomUpdateByQueryService updateByQueryService = new CustomUpdateByQueryService();

        this.customBulkService.set(customBulkService);
        this.customSearchService.set(customSearchService);
        this.deleteByQueryService.set(deleteByQueryService);
        this.getService.set(getService);
        this.indexService.set(indexService);
        this.multiGetService.set(multiGetService);
        this.multiSearchService.set(multiSearchService);
        this.updateByQueryService.set(updateByQueryService);


        JPackFilter  jPackFilter= new JPackFilter(clusterService,threadPool.getThreadContext(),
                this.accessTrailService.get(),  this.settings,this.accessSettings.get());

        jPackFilter.addHandler(new BulkRequestHandler(),this.customBulkService.get());
        jPackFilter.addHandler(new IndexRequestHandler(),this.indexService.get());

        jPackFilter.addHandler(new SearchRequestHandler<>(),this.customSearchService.get());
        jPackFilter.addHandler(new DeleteByQueryRequestHandler(),this.deleteByQueryService.get());
        jPackFilter.addHandler(new GetRequestHandler(),this.getService.get());
        jPackFilter.addHandler(new MultiGetRequestHandler(),this.multiGetService.get());
        jPackFilter.addHandler(new MultiSearchRequestHandler(),this.multiSearchService.get());
        jPackFilter.addHandler(new UpdateByQueryRequestHandler(),this.updateByQueryService.get());

        this.jPackFilter.set(jPackFilter);

        NodeCustomService nodeCustomService = new NodeCustomService(this.customBulkService.get() , this.customSearchService.get(),
                this.updateByQueryService.get(),this.deleteByQueryService.get(),this.getService.get(),this.indexService.get(),
                this.multiGetService.get(),this.multiSearchService.get(), threadPool);
        this.nodeCustomService.set(nodeCustomService);
        components.add(this.nodeCustomService.get());


        logger.info(" monitor Plugin {} finished init.", EsMetricsPlugin.class.getName());
        return components;
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Arrays.asList(
                new ActionHandler<>(IndexAllocationExplainAction.INSTANCE, TransportIndexAllocationExplainAction.class),
                new ActionHandler<>(NodesCustomStatsAction.INSTANCE, TransportNodesCustomStatsAction.class)
        );
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController, ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        return Arrays.asList(
                new RestIndexAllocationExplainAction(settings,restController),
                new RestNodesCustomStatsAction(settings,restController)
        );
    }

    @Override
    public List<ActionFilter> getActionFilters() {
        return Arrays.asList(jPackFilter.get());
    }

    @Override
    public List<Setting<?>> getSettings() {
        return AccessSettings.getSettings();
    }
}
