package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * sample data
 */
public class SampleUser implements Serializable {
    @JsonProperty("name")
    private String userName;

    @JsonIgnore
    private double v;

    @JsonProperty("inner")
    private InnerObj obj;

    public static class InnerObj implements Serializable {
        @JsonProperty("age")
        private int age;
        @JsonProperty("string")
        private String str;
        @JsonProperty("userType")
        private A userType;
    }

    public enum A {
        A1,
        B1,
        C1
    }
}
