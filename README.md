# jsonrpc4j-jsonSchema

This module is [jackson-module-jsonschema](https://github.com/FasterXML/jackson-module-jsonSchema) wrapper for the creation of api document (like JSON-Schema) from POJO of [jsonrpc4j](https://github.com/briandilley/jsonrpc4j) service classes.

## Installation
```
<dependency>
  <groupId>com.github.hiwayama</groupId>
  <artifactId>jsonrpc4j-jsonSchema</artifactId>
  <version>0.2-alpha-1</version>
</dependency>
```

## Example Usage

```
JsonRpcSchemaGenerator generator = new JsonRpcSchemaGenerator();

// generate schemas each json-rpc methods
for (JsonRpcSchema schema : generator.generate(HogeService.class)) {
    System.out.println(new ObjectMapper().writeAsString(schema));
    // {
    //   "method": "Hoge.methodName",
    //   "request": {
    //      // request object JSON-Schema
    //   },
    //   "response": {
    //      // response object JSON-Schema
    //   }
    // }
}
```
