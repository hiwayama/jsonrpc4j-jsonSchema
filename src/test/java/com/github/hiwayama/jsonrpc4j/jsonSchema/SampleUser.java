package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
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

    @JsonProperty("interface")
    private ISample1 sample;

    public static class InnerObj implements Serializable {
        @JsonProperty("age")
        @Max(200)
        @Min(0)
        private int age;
        @JsonProperty("string")
        private String str;
        @JsonProperty("userType")
        private A userType;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "name")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SampleImpl1.class, name = "1"),
            @JsonSubTypes.Type(value = SampleImpl2.class, name = "2")
    })
    public interface ISample1 {
        @JsonProperty("name")
        String getName();
    }

    public static class SampleImpl1 implements ISample1 {
        private String name;

        @JsonProperty("n")
        @Max(value = 10)
        private int n;

        @Override
        public String getName() {
            return name;
        }

        public SampleImpl1() {

        }
    }

    public static class SampleImpl2 implements ISample1 {
        @JsonProperty("v")
        private double v;

        @Override
        public String getName() {
            return "";
        }

        public SampleImpl2() {

        }
    }

    public enum A {
        A1,
        B1,
        C1
    }
}
