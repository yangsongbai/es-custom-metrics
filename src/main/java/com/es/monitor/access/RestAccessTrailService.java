package com.es.monitor.access;

import com.es.monitor.access.RestAccessTrail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.rest.RestRequest;

import java.util.Collections;
import java.util.List;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/8 21:47
 */
public class RestAccessTrailService {
    private static final Logger logger = LogManager.getLogger(RestAccessTrailService.class);

    private static final RestAccessTrail NOOP_AUDIT_TRAIL = new NoopAccessTrail();
    private final CompositeAccessTrail compositeAccessTrail;

    public RestAccessTrailService(List<RestAccessTrail> restAccessTrails) {
        this.compositeAccessTrail = new CompositeAccessTrail(Collections.unmodifiableList(restAccessTrails));
    }

    public RestAccessTrail get() {
        if (compositeAccessTrail.isEmpty() == false) {
            return compositeAccessTrail;
        } else {
            return NOOP_AUDIT_TRAIL;
        }
    }

    public void audit(String requestId, RestRequest request,ThreadContext threadContext) {
        RestAccessTrail accessTrail = get();
        accessTrail.audit(requestId,request,threadContext);
    }

    private static class NoopAccessTrail implements RestAccessTrail {

        @Override
        public String name() {
            return "noop";
        }

        @Override
        public void audit(String requestId, RestRequest request, ThreadContext threadContext) {

        }

    }

    private static class CompositeAccessTrail implements RestAccessTrail {

        private final List<RestAccessTrail> accessTrails;

        private CompositeAccessTrail(List<RestAccessTrail> accessTrails) {
            this.accessTrails = accessTrails;
        }

        boolean isEmpty() {
            return accessTrails.isEmpty();
        }

        @Override
        public String name() {
            return "service";
        }

        @Override
        public void audit(String requestId, RestRequest request, ThreadContext threadContext) {
            for (RestAccessTrail restAccessTrail : accessTrails) {
                   restAccessTrail.audit(requestId, request,threadContext);
            }
        }
    }
}
