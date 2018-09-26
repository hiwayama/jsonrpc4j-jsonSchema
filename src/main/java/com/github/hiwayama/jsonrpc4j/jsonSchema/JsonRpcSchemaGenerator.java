/**
 * @package   jsonrpc4j-jsonSchema
 * @author    Hiromasa IWAYAMA <iwayma1880@gmail.com>
 * @copyright 2018 hiwayama
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.jackson.jsonSchema.*;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonRpcResponseTitle;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class JsonRpcSchemaGenerator {
    private static final Map<String, Class> PRIMITIVE_CLASS_MAP = new HashMap<>();
    static {
        PRIMITIVE_CLASS_MAP.put("byte", Byte.class);
        PRIMITIVE_CLASS_MAP.put("short", Integer.class);
        PRIMITIVE_CLASS_MAP.put("int", Integer.class);
        PRIMITIVE_CLASS_MAP.put("long", Integer.class);
        PRIMITIVE_CLASS_MAP.put("float", Float.class);
        PRIMITIVE_CLASS_MAP.put("double", Double.class);
        PRIMITIVE_CLASS_MAP.put("boolean", Boolean.class);
        PRIMITIVE_CLASS_MAP.put("char", Character.class);
    }

    private ObjectMapper mapper;
    private JsonSchemaGenerator generator;

    public JsonRpcSchemaGenerator() {
        this(new ObjectMapper());
    }

    public JsonRpcSchemaGenerator(ObjectMapper mapper) {
        this.mapper = mapper;
        this.generator = new JsonSchemaGenerator(mapper);
    }

    private JsonNode generateRequestSchema(Method methodObj) throws JsonProcessingException, ClassNotFoundException {
        Map<String, JsonNode> objSchema = new HashMap<>();
        for (int i = 0; i < methodObj.getParameterCount(); i++) {
            Type paramType = methodObj.getGenericParameterTypes()[i];
            Annotation[] annotations = methodObj.getParameterAnnotations()[i];
            if (annotations != null && annotations.length > 0) {
                JsonRpcParam paramNameAnno = (JsonRpcParam) annotations[0];
                String methodName = paramNameAnno.value();
                objSchema.put(methodName, generateSchema(paramType));
            } else {
                // TODO implemented for arraySchema
                throw new UnsupportedOperationException("Not implemented for array schema");
            }
        }
        return mapper.valueToTree(objSchema);
    }

    private JsonNode generateSchema(Type paramType) throws JsonMappingException, ClassNotFoundException {
        if (paramType instanceof Class && ((Class) paramType).isPrimitive()) {
            return generator.generateJsonSchema(PRIMITIVE_CLASS_MAP.get(paramType.getTypeName()));
        } else if (paramType instanceof ParameterizedType) {
            return getCollectionSchema((ParameterizedType) paramType);
        } else {
            return generator.generateJsonSchema(Class.forName(paramType.getTypeName()));
        }
    }

    private JsonNode generateResponseSchema(Method method) throws JsonMappingException, ClassNotFoundException {
        Type returnType = method.getGenericReturnType();

        JsonNode resSchema = null;
        if (returnType instanceof Class<?>) {
            if (((Class) returnType).isPrimitive()) {
                resSchema = generator.generateJsonSchema(PRIMITIVE_CLASS_MAP.get(returnType.getTypeName()));
            } else {
                resSchema = generator.generateJsonSchema((Class)returnType);
            }
        } else if(returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType)returnType);
            resSchema = getCollectionSchema(parameterizedType);
        } else {
            return null;
        }

        JsonRpcResponseTitle titleAnno = method.getAnnotation(JsonRpcResponseTitle.class);
        if (titleAnno != null) {
            String schemaTitle = titleAnno.value();
            if ( resSchema.findValuesAsText("title") == null ) {
                ((ObjectNode)resSchema).put("title", schemaTitle);
            }
        }

        return resSchema;
    }

    private JsonNode getCollectionSchema(ParameterizedType type) throws ClassNotFoundException, JsonMappingException {
        if (type.getRawType().getTypeName().equals(List.class.getTypeName())) {
            System.out.println(type.getActualTypeArguments()[0].getTypeName());
            Class<?> t = Class.forName(type.getActualTypeArguments()[0].getTypeName());
            return generator.generateJsonSchema(Array.newInstance(t, 0).getClass());
        } else {
            // TODO impl for java.util.Map
            return null;
        }
    }

    /**
     * generate API Schemas
     * @param serviceClass Service Class of jsonrpc4j
     * @return API Schema objects
     * @throws JsonProcessingException on error
     * @throws ClassNotFoundException on error
     */
    public List<JsonRpcSchema> generate(Class<?> serviceClass) throws JsonProcessingException, ClassNotFoundException {
        List<JsonRpcSchema> schemas = new ArrayList<>();
        for (Method m : serviceClass.getMethods()) {
            JsonRpcSchema schema = generateImpl(m);
            if (schema != null) {
                schemas.add(schema);
            }
        }
        return schemas;
    }

    private JsonRpcSchema generateImpl(Method methodObj) throws ClassNotFoundException, JsonProcessingException {
        JsonRpcMethod methodAnno = methodObj.getAnnotation(JsonRpcMethod.class);
        if (methodAnno == null) {
            return null;
        }
        String methodName = methodAnno.value();

        JsonSchemaTitle titleAnno = methodObj.getAnnotation(JsonSchemaTitle.class);
        String schemaTitle = "";
        if (titleAnno != null) {
            schemaTitle = titleAnno.value();
        }
        JsonRpcSchema schema = new JsonRpcSchema(
                methodName, schemaTitle, generateRequestSchema(methodObj), generateResponseSchema(methodObj));
        return schema;
    }
}
