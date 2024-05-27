package com.es.monitor.node.allocation;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentParser;

import java.io.IOException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * Created by
 *
 * @Author : yangsongbai1
 * @create 2022/11/1 11:28
 */
public class IndexAllocationExplainRequest  extends MasterNodeRequest<IndexAllocationExplainRequest> {

    private static final ObjectParser<IndexAllocationExplainRequest, Void> PARSER = new ObjectParser<>("index/allocation/explain");
    static {
        PARSER.declareString(IndexAllocationExplainRequest::setIndex, new ParseField("index"));

    }
    @Nullable
    private String index;

    private boolean includeYesDecisions = false;
    private boolean includeDiskInfo = false;


    public IndexAllocationExplainRequest() {
        this.index = null;
    }

    public IndexAllocationExplainRequest(String index) {
        this.index = index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        this.index = in.readOptionalString();
        this.includeYesDecisions = in.readBoolean();
        this.includeDiskInfo = in.readBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(index);
        out.writeBoolean(includeYesDecisions);
        out.writeBoolean(includeDiskInfo);
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (this.useAnyUnassignedShard() == false) {
            if (getIndex() == null) {
                validationException = addValidationError("index must be specified", validationException);
            }
        }
        return validationException;
    }

    public boolean useAnyUnassignedShard() {
        return this.index == null;
    }

    public String getIndex() {
        return index;
    }

    public boolean includeYesDecisions() {
        return includeYesDecisions;
    }

    public boolean includeDiskInfo() {
        return includeDiskInfo;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IndexAllocationExplainRequest[");
        if (this.useAnyUnassignedShard()) {
            sb.append("useAnyUnassignedShard=true");
        } else {
            sb.append("index=").append(index);
        }
        sb.append(",includeYesDecisions?=").append(includeYesDecisions);
        return sb.toString();
    }

    public static IndexAllocationExplainRequest parse(XContentParser parser) throws IOException {
        return PARSER.parse(parser, new IndexAllocationExplainRequest(), null);
    }

    public void includeYesDecisions(boolean includeYesDecision) {
        this.includeYesDecisions = includeYesDecision;
    }

    public void includeDiskInfo(boolean includeDiskInfo) {
        this.includeDiskInfo = includeDiskInfo;
    }
}
