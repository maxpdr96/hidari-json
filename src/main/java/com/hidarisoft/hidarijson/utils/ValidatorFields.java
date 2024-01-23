package com.hidarisoft.hidarijson.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class ValidatorFields {

    private ValidatorFields() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static JsonNode buildJsonNodeTransaction(Map<String, String> fields) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        fields.forEach((key, value) -> {
            var keyExtension = CreatorFields.convertStringArrays(key);
            System.out.println(Arrays.toString(keyExtension));
            // Criando um stream de índices
            IntStream.range(0, keyExtension.length)
                    .filter(i -> keyExtension[i].equals("list") && i + 2 < keyExtension.length) // Filtrar índices que correspondem a 'list' e têm 2 elementos seguintes
                    .forEach(i -> {
                        var nameList = keyExtension[i + 1];
                        var valueKeyList = keyExtension[i + 2];
                        System.out.println("Segundo elemento: " + nameList);
                        System.out.println("Terceiro elemento: " + valueKeyList);
                        buildLists(nameList, valueKeyList, value, jsonNode);
                    });
//            jsonNode.putIfAbsent(key, TextNode.valueOf(value));
            System.out.println(jsonNode);
        });
        return jsonNode;
    }

    private static void buildArray(String nameArray, String valueKeyArray, String value, JsonNode jsonNode) {
        ObjectNode objectNode = ((ObjectNode) jsonNode);
        if (Objects.nonNull(objectNode.get(nameArray))) {
            var nodeList = (ObjectNode) objectNode.get(nameArray);
            var valueToInsert = TextNode.valueOf(value);
            insertNodeToJson(nodeList, valueKeyArray, valueToInsert);
        } else {
            var nodeList = objectNode.putObject(nameArray);
            var valueToInsert = TextNode.valueOf(value);
            insertNodeToJson(nodeList, valueKeyArray, valueToInsert);
        }
    }

    private static void buildLists(String nameList, String valueKeyList, String value, JsonNode jsonNode) {
        String[] values = value.split(",");
        ObjectNode objectNode = ((ObjectNode) jsonNode);
        JsonNodeFactory factory = JsonNodeFactory.instance;

        if (jsonNode.has(nameList)) {
            var arrayNodeList = (ArrayNode) objectNode.get(nameList);
            for (int i = 0; i < values.length; i++) {
                var valueToInsert = TextNode.valueOf(values[i]);
                var arrayNode = arrayNodeList.get(i);
                insertNodeToJson(arrayNode, valueKeyList, valueToInsert);
            }
        } else {
            var arrayNodeList = objectNode.putArray(nameList);
            for (String s : values) {
                var valueToInsert = TextNode.valueOf(s);
                ObjectNode mainObject = factory.objectNode();
                mainObject.putIfAbsent(valueKeyList, valueToInsert);
                insertNodeToJson(arrayNodeList, valueKeyList, mainObject);
            }
        }
    }

    private static void insertNodeToJson(Object nodeJson, String valueKeyArray, JsonNode value) {
        if (nodeJson instanceof ObjectNode objectNode) {
            objectNode.putIfAbsent(valueKeyArray, value);
        } else if (nodeJson instanceof ArrayNode arrayNode) {
            arrayNode.add(value);
        }
    }
}
