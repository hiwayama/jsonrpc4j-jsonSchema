package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonRpcResponseTitle;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.time.Duration;
import java.util.List;

/**
 * sample service class
 */
public interface SampleService {
    @JsonRpcMethod("user.get")
    @JsonSchemaTitle("get single user")
    SampleUser get(@JsonRpcParam("id") String userId);

    @JsonRpcMethod("user.getById")
    @JsonSchemaTitle("get single user by numeric id")
    SampleUser getById(@JsonRpcParam("id") long id, @JsonRpcParam("opt") String type);

    @JsonRpcMethod("user.list")
    @JsonSchemaTitle("get users")
    @JsonRpcResponseTitle("users list")
    List<SampleUser> getList(@JsonRpcParam("ids") List<String> userIds);

    // TODO
    // @JsonRpcMethod("sample.arraySchema")
    // String arraySchemaMethod(String a, int b, long c, SampleUser d);
}
