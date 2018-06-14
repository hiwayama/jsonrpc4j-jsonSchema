package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonRpcSchemaGenerator {
    private ObjectMapper mapper = new ObjectMapper();
    private com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator generator = new com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator(mapper);

    private JsonSchema generateRequestSchema(Method methodObj) throws JsonMappingException, ClassNotFoundException {
        ObjectSchema schema = new ObjectSchema();
        for (int i = 0; i < methodObj.getParameterCount(); i++) {
            Type paramType = methodObj.getGenericParameterTypes()[i];
            Annotation[] annotations = methodObj.getParameterAnnotations()[i];
            if (annotations != null && annotations.length > 0) {
                JsonRpcParam paramNameAnno = (JsonRpcParam) annotations[0];
                String methodName = paramNameAnno.value();
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
        String resClass = null;
        if (returnType instanceof Class<?>) {
            resClass = returnType.getTypeName();
        } else if(returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType)returnType);
            return getCollectionSchema(parameterizedType);
        }
        return generator.generateSchema(Class.forName(resClass));
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
