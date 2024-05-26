package com.es.monitor.access;

import java.util.Collections;
import java.util.List;

/**
 * Created by
 *
 * @Author : yangsongbai
 * @create 2022/11/4 10:07
 */
public class AccessTrailService {


    private static final AccessTrail NOOP_AUDIT_TRAIL = new NoopAccessTrail();
    private final CompositeAccessTrail compositeAccessTrail;

    public AccessTrailService(List<AccessTrail> accessTrails) {
        this.compositeAccessTrail = new CompositeAccessTrail(Collections.unmodifiableList(accessTrails));
    }

    public AccessTrail get() {
        if (compositeAccessTrail.isEmpty() == false) {
            return compositeAccessTrail;
        } else {
            return NOOP_AUDIT_TRAIL;
        }
    }


    public void fail(AccessResponseInfo access, RemoteInfo remoteInfo) {
        AccessTrail accessTrail = get();
        accessTrail.fail(access,remoteInfo);
    }


    public void success(AccessResponseInfo access, RemoteInfo remoteInfo) {
        AccessTrail accessTrail = get();
        accessTrail.success(access,remoteInfo);
    }

    public void requestInfo(AccessRequestInfo access) {
        AccessTrail accessTrail = get();
        accessTrail.requestInfo(access);
    }

    public void operatorInfo(AccessRequestInfo access) {
        AccessTrail accessTrail = get();
        accessTrail.requestInfo(access);
    }

    private static class NoopAccessTrail implements AccessTrail {

        @Override
        public String name() {
            return "noop";
        }

        @Override
        public void success(AccessResponseInfo access, RemoteInfo remoteInfo) {}

        @Override
        public void fail(AccessResponseInfo access, RemoteInfo remoteInfo) {

        }

        @Override
        public void requestInfo(AccessRequestInfo access) {

        }

    }

    private static class CompositeAccessTrail implements AccessTrail {

        private final List<AccessTrail> accessTrails;

        private CompositeAccessTrail(List<AccessTrail> accessTrails) {
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
        public void success(AccessResponseInfo access, RemoteInfo remoteInfo) {
            for (AccessTrail auditTrail : accessTrails) {
                auditTrail.success(access, remoteInfo);
            }
        }


        @Override
        public void fail(AccessResponseInfo access, RemoteInfo remoteInfo) {
            for (AccessTrail accessTrail : accessTrails) {
                accessTrail.fail(access, remoteInfo);
            }
        }

        @Override
        public void requestInfo(AccessRequestInfo access) {
            for (AccessTrail accessTrail : accessTrails) {
                accessTrail.requestInfo(access);
            }
        }

    }
}
