package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.util.List;

/**
 * sample service class
 */
public interface SampleService {
    @JsonRpcMethod("user.get")
    @JsonSchemaTitle("get single user")
    SampleUser get(@JsonRpcParam("id") String userId);

    @JsonRpcMethod("user.list")
    @JsonSchemaTitle("get users")
    List<SampleUser> getList(@JsonRpcParam("ids") List<String> userIds);
}
