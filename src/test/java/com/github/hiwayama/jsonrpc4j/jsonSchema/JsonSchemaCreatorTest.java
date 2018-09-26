package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JsonSchemaCreatorTest {
    private JsonRpcSchemaGenerator creator = new JsonRpcSchemaGenerator();
    private SimpleModule module = new SimpleModule().addDeserializer(SampleUser.ISample1.class, new SampleDeserializer());
    private ObjectMapper mapper = new ObjectMapper().registerModule(module);

    private Map<String, String> expectedData = new HashMap<>();

    @Test
    public void test_for_object_schema() throws IOException, ClassNotFoundException {
        for (String name : new String[] {
                "user.get.json",
                "user.list.json",
                "user.getById.json"}) {
            StringBuilder expeted = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(JsonSchemaCreatorTest.class.getClassLoader().getResourceAsStream(name)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    expeted.append(line.trim());
                }
            }
            expectedData.put(name.replace(".json", ""), expeted.toString());
        }

        for (JsonRpcSchema schema : creator.generate(SampleService.class) ) {
            String jsonText = mapper.writeValueAsString(schema);
            Assert.assertEquals(expectedData.get(schema.getMethod()), jsonText);
        }
    }
}
