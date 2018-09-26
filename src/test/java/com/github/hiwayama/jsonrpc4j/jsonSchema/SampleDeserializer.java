package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class SampleDeserializer extends JsonDeserializer<SampleUser.ISample1> {
    @Override
    public SampleUser.ISample1 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        String name = node.get("name").asText();
        switch (name) {
            case "1":
                return new SampleUser.SampleImpl1();
            case "2":
                return new SampleUser.SampleImpl2();
        }

        return null;
    }
}
