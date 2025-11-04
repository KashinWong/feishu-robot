package com.hjx.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class MessageSendReqs {

    private String targetId;
    private String receiveIdType = "chat_id";
    private String msgType = "post";
    private String content;
    private List<List<ContentObject>> objects = new ArrayList<>();

    @Data
    public static class ContentObject {
        private String tag;
        private String href;
        private String text;
        private String user_id;
        private String image_key;
    }

    public MessageSendReqs addContents(List<ContentObject> object) {
        objects.add(object);
        return this;
    }

    public MessageSendReqs addLine(ContentObject object) {
        objects.add(new ArrayList<>() {{
            add(object);
        }});
        return this;
    }

    public MessageSendReqs target(String targetId) {
        this.setTargetId(targetId);
        return this;
    }

    public static MessageSendReqs.ContentObject at(String user_id) {
        MessageSendReqs.ContentObject contentObject = new MessageSendReqs.ContentObject();
        contentObject.setTag("at");
        contentObject.setUser_id(user_id);
        return contentObject;
    }

    public static MessageSendReqs.ContentObject text(String text) {
        MessageSendReqs.ContentObject contentObject = new MessageSendReqs.ContentObject();
        contentObject.setTag("text");
        contentObject.setText(text);
        return contentObject;
    }

    public MessageSendReqs build() {
        Map<String, Object> content = new HashMap<>() {{
            put("zh_cn", new HashMap<>() {{
                put("content", objects);
            }});
        }};

        try {
            this.content = new ObjectMapper().writeValueAsString(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
