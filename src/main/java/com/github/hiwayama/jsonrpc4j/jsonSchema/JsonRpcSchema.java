package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import java.io.Serializable;

/**
 * JSON-RPC API schema Object
 *
 * request:
 * {
 *   "id": "xxx-xxx-xxx",
 *   "jsonrpc": "2.0",
 *   "method": JsonRpcSchema.method,
 *   "params": JsonRpcSchema.request
 * }
 *
 * response:
 * {
 *     "id": "xxx-xxx-xxx",
 *     "method": JsonRpcSchema.method,
 *     "jsonrpc": "2.0",
 *     "result": JsonRpcSchema.response
 * }
 */
public class JsonRpcSchema implements Serializable {
    @JsonProperty("method")
    private String method;
    @JsonProperty("title")
    private String title;
    @JsonProperty("request")
    private JsonSchema request;
    @JsonProperty("response")
    private JsonSchema response;

    public JsonRpcSchema(String method, String title, JsonSchema request, JsonSchema response) {
        this.method = method;
        this.title = title;
        this.request = request;
        this.response = response;
    }

    /**
     * @return JSON-RPC method name
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return title attribute of JSONSchema
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return JsonSchema of param object
     */
    public JsonSchema getRequest() {
        return request;
    }

    /**
     * @return JsonSchema of result items
     */
    public JsonSchema getResponse() {
        return response;
    }
}
