package com.hidarisoft.hidarijson.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static JsonNode buildJsonNodeTransaction(Map<String, String> fields) {
        ObjectNode jsonNode = mapper.createObjectNode();
        fields.forEach((key, value) -> {
            var keys = CreatorFields.convertStringArrays(key);
            System.out.println(Arrays.toString(keys));
            IntStream.range(0, keys.length)
                    .filter(i -> (keys[i].equals("list") || keys[i].equals("array")) && i + 2 < keys.length) // Filtrar índices que correspondem a 'list' e têm 2 elementos seguintes
                    .forEach(i -> {
                        var singleKey = keys[i + 1];
                        var singleValue = keys[i + 2];
                        var lastKey = "";
                        if (i > 0) {
                            lastKey = keys[i - 1];
                        }
                        buildLists(singleKey, singleValue, lastKey, value, jsonNode, i);
                    });

            System.out.println(jsonNode.toPrettyString());
        });
        return jsonNode;
    }


    private static void buildLists(String nameList, String valueKeyList, String lastKey, String value, JsonNode jsonNode, int i) {
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode objectNode = mapper.createObjectNode();
        if (i == 0) {
            objectNode.put(valueKeyList, value);
            arrayNode.add(objectNode);
            ((ObjectNode) jsonNode).set(nameList, arrayNode);
        } else {
            var iteratorArrays = jsonNode.withArray("address").elements();
            iteratorArrays.forEachRemaining(jsonNode1 -> {
                objectNode.put(valueKeyList, value);
                arrayNode.add(objectNode);
                try {
                    JsonNode jsonNodeAux = mapper.readTree(arrayNode.toString());
                    ObjectNode objectNodeAux = mapper.createObjectNode();
                    objectNodeAux.set(nameList, jsonNodeAux);
                    ((ObjectNode) jsonNode1).findParent(lastKey).set(lastKey, objectNodeAux);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
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
