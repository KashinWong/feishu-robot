package com.hjx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;


public class CommonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static JsonNode resolveContent(P2MessageReceiveV1 event) throws JsonProcessingException {
        return objectMapper.readTree(event.getEvent().getMessage().getContent());
    }

    public static String getCommand(P2MessageReceiveV1 event) throws JsonProcessingException {
        JsonNode content = resolveContent(event);
        String[] contents = content.get("text").asText().split("\\r?\\n");
        return contents[0].replace("@_user_1", "").trim().toLowerCase();
    }

}
