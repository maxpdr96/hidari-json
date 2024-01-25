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
            IntStream.range(0, keyExtension.length)
                    .filter(i -> (keyExtension[i].equals("list") || keyExtension[i].equals("array")) && i + 2 < keyExtension.length) // Filtrar índices que correspondem a 'list' e têm 2 elementos seguintes
                    .forEach(i -> {
//                        boolean hasAnotherListOrArray = IntStream.range(i + 3, keyExtension.length)
//                                .anyMatch(j -> keyExtension[j].equals("list") || keyExtension[j].equals("array"));
//
//                        System.out.println("Tem outro list ou array: " + hasAnotherListOrArray);
//                        System.out.println("list: " + keyExtension[i] + " order: " + i);
//                        var valueAfter = "";
//                        if (i > 1) {
//                            valueAfter = keyExtension[i - 1];
//                            System.out.println("valor anterior: " + valueAfter);
//                        }
//                        var nameList = keyExtension[i + 1];
//                        var valueKeyList = keyExtension[i + 2];
//                        if (keyExtension[i].equals("list")) {
//                            System.out.println("chave da lista: " + nameList);
//                            System.out.println("valor da lista: " + valueKeyList);
//                            buildLists(nameList, valueKeyList, value, jsonNode, valueAfter);
//                        } else {
//                            System.out.println("chave do array: " + nameList);
//                            System.out.println("valor do array: " + valueKeyList);
//                            buildArray(nameList, valueKeyList, value, jsonNode);
//                        }

                        buildJson(jsonNode, null, keyExtension[i], keyExtension[i + 1], value);
                    });
//            addToJson(jsonNode, key, value, objectMapper);
//            jsonNode.putIfAbsent(key, TextNode.valueOf(value));
            System.out.println(jsonNode.toPrettyString());
        });
        return jsonNode;
    }

    private static void buildJson(ObjectNode objectNode, ArrayNode arrayNode, String key, String nextKey, String value) {
        if (key.contentEquals("list")) {
            arrayNode = objectMapper.createArrayNode();
            buildJson(objectNode, arrayNode, nextKey, nextKey, value);
            System.out.println("é uma lista");
        } else {
            objectNode.set(key, TextNode.valueOf(value));
            if (Objects.nonNull(arrayNode)) {
                System.out.println("Array nao é nulo, add");
                arrayNode.add(objectNode);
            }
        }
    }

    private static void buildLists(String nameList, String valueKeyList, String value, JsonNode jsonNode, String valueAfter) {
        if (Boolean.FALSE.equals(valueAfter.contentEquals(""))) {
            nameList = valueAfter;
        }
        ArrayNode arrayNodeHelp = null;
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
        } else if (Objects.nonNull(jsonNode.findParent(nameList))) {
            var valueTest = jsonNode.findValue(nameList);
            if (Objects.nonNull(valueTest)) {
                arrayNodeHelp = objectMapper.createArrayNode();
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


    private static void insertNodeToJson(Object nodeJson, String valueKeyArray, JsonNode value) {
        if (nodeJson instanceof ObjectNode objectNode) {
            objectNode.putIfAbsent(valueKeyArray, value);
        } else if (nodeJson instanceof ArrayNode arrayNode) {
            arrayNode.add(value);
        }
    }

    private static JsonNode findInArrayNode(JsonNode rootNode, String key, String value) {
        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                if (node.has(key) && node.get(key).asText().equals(value)) {
                    return node; // Elemento encontrado
                }
            }
        } else if (rootNode.isObject()) {
            for (JsonNode node : rootNode) {
                JsonNode foundNode = findInArrayNode(node, key, value);
                if (foundNode != null) {
                    return foundNode; // Elemento encontrado em sub-árvore
                }
            }
        }
        return null; // Elemento não encontrado
    }

}
