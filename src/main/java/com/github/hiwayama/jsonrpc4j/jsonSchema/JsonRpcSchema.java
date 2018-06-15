package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * JSON-RPC API schema Object
 */
@AllArgsConstructor
@Getter
public class JsonRpcSchema implements Serializable {
    /**
     * JSON-RPC method name
     */
    @JsonProperty("method")
    private String method;
    @JsonProperty("title")
    private String title;
    /**
     * JsonSchema of params object
     */
    @JsonProperty("request")
    private JsonSchema request;
    /**
     * JsonSchema of result items
     */
    @JsonProperty("response")
    private JsonSchema response;
}
