package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class JsonRpcSchema implements Serializable {
    @JsonProperty("method")
    private String method;
    @JsonProperty("title")
    private String title;
    @JsonProperty("request")
    private JsonSchema request;
    @JsonProperty("response")
    private JsonSchema response;
}
