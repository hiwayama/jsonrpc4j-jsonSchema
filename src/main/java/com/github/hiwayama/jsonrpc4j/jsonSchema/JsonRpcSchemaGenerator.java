package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonRpcResponseTitle;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRpcSchemaGenerator {
    private static final Map<String, Class> PRIMITIVE_CLASS_MAP = new HashMap<>();
    static {
        PRIMITIVE_CLASS_MAP.put("byte", byte.class);
        PRIMITIVE_CLASS_MAP.put("short", short.class);
        PRIMITIVE_CLASS_MAP.put("int", int.class);
        PRIMITIVE_CLASS_MAP.put("long", long.class);
        PRIMITIVE_CLASS_MAP.put("float", float.class);
        PRIMITIVE_CLASS_MAP.put("double", double.class);
        PRIMITIVE_CLASS_MAP.put("boolean", boolean.class);
        PRIMITIVE_CLASS_MAP.put("char", char.class);
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

    public JsonRpcSchemaGenerator(ObjectMapper mapper,  SchemaFactoryWrapper wrapperFactory) {
        this(mapper);
        this.generator = new JsonSchemaGenerator(mapper, wrapperFactory);
    }

    private JsonSchema generateRequestSchema(Method methodObj) throws JsonMappingException, ClassNotFoundException {
        ObjectSchema schema = new ObjectSchema();
        for (int i = 0; i < methodObj.getParameterCount(); i++) {
            Type paramType = methodObj.getGenericParameterTypes()[i];
            Annotation[] annotations = methodObj.getParameterAnnotations()[i];
            if (annotations != null && annotations.length > 0) {
                JsonRpcParam paramNameAnno = (JsonRpcParam) annotations[0];
                String methodName = paramNameAnno.value();
                if (paramType instanceof Class) {
                    if (((Class) paramType).isPrimitive()) {
                        schema.putProperty(methodName, generator.generateSchema(PRIMITIVE_CLASS_MAP.get(paramType.getTypeName())));
                    }
                }
                if (paramType instanceof ParameterizedType) {
                    schema.putProperty(methodName, getCollectionSchema((ParameterizedType) paramType));
                } else {
                    schema.putProperty(methodName, generator.generateSchema(paramType.getClass()));
                }
            } else {
                // TODO impl for array parameter
            }
        }
        return schema;
    }

    private JsonSchema generateResponseSchema(Method method) throws JsonMappingException, ClassNotFoundException {
        Type returnType = method.getGenericReturnType();

        JsonSchema resSchema;
        if (returnType instanceof Class<?>) {
            if (((Class) returnType).isPrimitive()) {
                resSchema = generator.generateSchema(PRIMITIVE_CLASS_MAP.get(returnType.getTypeName()));
            } else {
                resSchema = generator.generateSchema((Class)returnType);
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
            resSchema.asSimpleTypeSchema().setTitle(schemaTitle);
        }

        return resSchema;
    }

    private JsonSchema getCollectionSchema(ParameterizedType type) throws ClassNotFoundException, JsonMappingException {
        if (type.getRawType().getTypeName().equals(List.class.getTypeName())) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItemsSchema(
                    generator.generateSchema(
                            Class.forName(type.getActualTypeArguments()[0].getTypeName())));
            return arraySchema;
        } else {
            // TODO impl for java.util.Map
            return null;
        }
    }

    public List<JsonRpcSchema> generate(Class<?> serviceClass) throws JsonMappingException, ClassNotFoundException {
        List<JsonRpcSchema> schemas = new ArrayList<>();
        for (Method m : serviceClass.getMethods()) {
            JsonRpcSchema schema = generateImpl(m);
            if (schema != null) {
                schemas.add(schema);
            }
        }
        return schemas;
    }

    private JsonRpcSchema generateImpl(Method methodObj) throws JsonMappingException, ClassNotFoundException {
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
