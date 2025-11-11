package com.hjx.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUtils {

    @Value("${feishu.ws.appId:}")
    private String appId;

    @Value("${feishu.ws.secret:}")
    private String secret;

    private Client client;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        client = Client.newBuilder(appId, secret).build();
    }

    public void send(MessageSendReqs reqs) {
        try {
            // 创建请求对象
            CreateMessageReq req = CreateMessageReq.newBuilder()
                    .receiveIdType(reqs.getReceiveIdType())
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(reqs.getTargetId())
                            .msgType("post")
                            .content(reqs.getContent())
                            .build())
                    .build();
            // 发起请求
            CreateMessageResp resp = client.im().message().create(req);
            logSend(objectMapper.writeValueAsString(resp));
        } catch (Exception e) {
            log.error("send-message-failed", e);
        }
    }

    public void reply(MessageSendReqs reqs) {
        try {
            // 创建请求对象
            ReplyMessageReq req = ReplyMessageReq.newBuilder()
                    .messageId(reqs.getTargetId())
                    .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                            .msgType("post")
                            .content(reqs.getContent())
                            .build())
                    .build();
            // 发起请求
            ReplyMessageResp resp = client.im().message().reply(req);
            logSend(objectMapper.writeValueAsString(resp));
        } catch (Exception e) {
            log.error("send-message-failed", e);
        }
    }

    public MessageSendReqs.ContentObject img(File file) {
        try {
            CreateImageReq req = CreateImageReq.newBuilder()
                    .createImageReqBody(CreateImageReqBody.newBuilder()
                            .imageType("message")
                            .image(file)
                            .build())
                    .build();
            CreateImageResp resp = client.im().image().create(req);
            MessageSendReqs.ContentObject object = new MessageSendReqs.ContentObject();
            object.setTag("img");
            object.setImage_key(resp.getData().getImageKey());
            return object;
        } catch (Exception e) {
            log.error("img-failed", e);
        }
        return null;
    }

    public void errorMention(String command, P2MessageReceiveV1 event, Exception e) {
        log.error("error,command:{}", command, e);
        MessageSendReqs reqs = new MessageSendReqs()
                .target(event.getEvent().getMessage().getMessageId())
                .addLine(MessageSendReqs.at(event.getEvent().getSender().getSenderId().getOpenId()))
                .addLine(MessageSendReqs.text(command + "执行失败，请人工确认"));
        reply(reqs.build());
    }

    public void replyText(P2MessageReceiveV1 event, String text) {
        MessageSendReqs reqs = new MessageSendReqs()
                .target(event.getEvent().getMessage().getMessageId())
                .addLine(MessageSendReqs.at(event.getEvent().getSender().getSenderId().getOpenId()))
                .addLine(MessageSendReqs.text(text))
                .build();
        reply(reqs);
    }

    public void logSend(String resp) {
        log.info("send-message,resp={}", resp);
    }


}
