package com.es.monitor.node.access;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

/**
 * Created by
 *
 * @Author : yangsongbai
 * @create 2022/11/4 17:19
 */
public class RemoteInfo implements ToXContentObject {
    public static final String REMOTE_INFO_KEY = "_remote_info_key";

    private String remoteAddress;
    private Boolean rest;
    private String start;
    private String user;
    private String uri;
    private String method;

    public RemoteInfo() {
        this.remoteAddress = "";
        this.rest = false;
        this.start = "";
        this.uri = "";
        this.method = "";
        this.user = "";
    }

    public RemoteInfo(String remoteAddress, Boolean rest, String start, String uri, String method, String user) {
        this.remoteAddress = remoteAddress;
        this.rest = rest;
        this.start = start;
        this.uri = uri;
        this.method = method;
        this.user = user;
    }

    public RemoteInfo(Boolean rest, String user) {
        this.rest = rest;
        this.user = user;
    }

    public Boolean getRest() {
        return rest;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Boolean rest() {
        return rest;
    }

    public void setRest(Boolean rest) {
        this.rest = rest;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUri() {
        return uri;
    }


    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public RemoteInfo(StreamInput in) throws IOException {
        this.user = in.readOptionalString();
        this.rest = in.readOptionalBoolean();
        this.remoteAddress = in.readOptionalString();
        this.method = in.readOptionalString();
        this.uri = in.readOptionalString();
    }
    public String encode() throws IOException {
        BytesStreamOutput output = new BytesStreamOutput();
        writeTo(output);
        return Base64.getEncoder().encodeToString(BytesReference.toBytes(output.bytes()));
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(user);
        out.writeOptionalBoolean(rest);
        out.writeOptionalString(remoteAddress);
        out.writeOptionalString(start);
        out.writeOptionalString(method);
        out.writeOptionalString(uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteInfo that = (RemoteInfo) o;
        return  user.equals(that.user) &&
                rest.equals(that.rest) &&
                remoteAddress.equals(that.remoteAddress) &&
                start.equals(that.start) &&
                method.equals(that.method) &&
                uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, rest, remoteAddress, start, method, uri);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        toXContentFragment(builder);
        return builder.endObject();
    }

    public interface Fields {
        ParseField USER = new ParseField("user");
        ParseField REST = new ParseField("rest");
        ParseField REMOTE_ADDRESS = new ParseField("remoteAddress");
        ParseField START = new ParseField("start");
        ParseField METHOD = new ParseField("method");
        ParseField URI = new ParseField("uri");
        ParseField PATH = new ParseField("path");
    }

    /**
     * Generates XContent without the start/end object.
     */
    public void toXContentFragment(XContentBuilder builder) throws IOException {
        builder.field(Fields.USER.getPreferredName(),user);
        builder.field(Fields.REST.getPreferredName(), rest);
        builder.field(Fields.REMOTE_ADDRESS.getPreferredName(), remoteAddress);
        builder.field(Fields.START.getPreferredName(), start);
        builder.field(Fields.METHOD.getPreferredName(), method);
        builder.field(Fields.URI.getPreferredName(), uri);
    }
}
