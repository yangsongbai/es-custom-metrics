package com.es.monitor.access;

import com.es.monitor.access.RemoteInfo;
import org.elasticsearch.Version;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.util.concurrent.ThreadContext;

import java.io.IOException;
import java.util.Base64;

/**
 *
 */
public class RemoteInfoContextSerializer {
    private final String contextKey;

    public RemoteInfoContextSerializer() {
        this(RemoteInfo.REMOTE_INFO_KEY);
    }

    public RemoteInfoContextSerializer(String contextKey) {
        this.contextKey = contextKey;
    }

    @Nullable
    public RemoteInfo readFromContext(ThreadContext ctx) throws IOException {
        RemoteInfo remoteInfo = ctx.getTransient(contextKey);
        if (remoteInfo != null) {
            assert ctx.getHeader(contextKey) != null;
            return remoteInfo;
        }

        String remoteInfoHeader = ctx.getHeader(contextKey);
        if (remoteInfoHeader == null) {
            return null;
        }
        return deserializeHeaderAndPutInContext(remoteInfoHeader, ctx);
    }

    RemoteInfo deserializeHeaderAndPutInContext(String headerValue, ThreadContext ctx)
            throws IOException, IllegalArgumentException {
        assert ctx.getTransient(contextKey) == null;

        RemoteInfo remoteInfo = decode(headerValue);
        ctx.putTransient(contextKey, remoteInfo);
        return remoteInfo;
    }

    public static RemoteInfo decode(String header) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(header);
        StreamInput input = StreamInput.wrap(bytes);
        Version version = Version.readVersion(input);
        input.setVersion(version);
        return new RemoteInfo(input);
    }

    public RemoteInfo getRemoteInfo(ThreadContext context) {
        return context.getTransient(contextKey);
    }

    /**
     * Writes the remoteInfo to the context. There must not be an existing remoteInfo in the context and if there is an
     * {@link IllegalStateException} will be thrown
     */
    public void writeToContext(RemoteInfo remoteInfo, ThreadContext ctx) throws IOException {
        ensureContextDoesNotContainRemoteInfo(ctx);
        String header = remoteInfo.encode();
        assert header != null : "remoteInfo object encoded to null"; // this usually happens with mock objects in tests
        ctx.putTransient(contextKey, remoteInfo);
        ctx.putHeader(contextKey, header);
    }

    void ensureContextDoesNotContainRemoteInfo(ThreadContext ctx) {
        if (ctx.getTransient(contextKey) != null) {
            if (ctx.getHeader(contextKey) == null) {
                throw new IllegalStateException("remoteInfo present as a transient ([" + contextKey + "]) but not a header");
            }
            throw new IllegalStateException("remoteInfo ([" + contextKey + "]) is already present in the context");
        }
    }
}
