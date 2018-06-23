/**
 * @package   jsonrpc4j-jsonSchema
 * @author    Hiromasa IWAYAMA <iwayma1880@gmail.com>
 * @copyright 2018 hiwayama
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
package com.github.hiwayama.jsonrpc4j.jsonSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.*;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonRpcResponseTitle;
import com.github.hiwayama.jsonrpc4j.jsonSchema.annotations.JsonSchemaTitle;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;

import java.lang.annotation.Annotation;
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

    public JsonRpcSchemaGenerator(ObjectMapper mapper,  SchemaFactoryWrapper wrapperFactory) {
        this(mapper);
        this.generator = new JsonSchemaGenerator(mapper, wrapperFactory);
    }

    private JsonSchema generateRequestSchema(Method methodObj) throws JsonProcessingException, ClassNotFoundException {
        ObjectSchema objSchema = new ObjectSchema();
        for (int i = 0; i < methodObj.getParameterCount(); i++) {
            Type paramType = methodObj.getGenericParameterTypes()[i];
            Annotation[] annotations = methodObj.getParameterAnnotations()[i];
            if (annotations != null && annotations.length > 0) {
                JsonRpcParam paramNameAnno = (JsonRpcParam) annotations[0];
                String methodName = paramNameAnno.value();
                objSchema.putProperty(methodName, generateSchema(paramType));
            } else {
                // TODO implemented for arraySchema
                throw new UnsupportedOperationException("Not implemented for array schema");
            }
        }
        return objSchema;
    }

    private JsonSchema generateSchema(Type paramType) throws JsonMappingException, ClassNotFoundException {
        if (paramType instanceof Class && ((Class) paramType).isPrimitive()) {
            return generator.generateSchema(PRIMITIVE_CLASS_MAP.get(paramType.getTypeName()));
        } else if (paramType instanceof ParameterizedType) {
            return getCollectionSchema((ParameterizedType) paramType);
        } else {
            return generator.generateSchema(Class.forName(paramType.getTypeName()));
        }
    }

    private JsonSchema generateResponseSchema(Method method) throws JsonMappingException, ClassNotFoundException {
        Type returnType = method.getGenericReturnType();

        JsonSchema resSchema = null;
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
