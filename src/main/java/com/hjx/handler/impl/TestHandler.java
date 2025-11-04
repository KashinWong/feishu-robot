package com.hjx.handler.impl;

import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.hjx.handler.EventHandler;
import com.hjx.message.MessageSendReqs;
import com.hjx.message.MessageUtils;
import com.hjx.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestHandler implements EventHandler {

    private final MessageUtils messageUtils;
    private final UserUtils userUtils;

    @Override
    public String command() {
        return "在吗";
    }

    @Override
    public void handle(P2MessageReceiveV1 event) {
        sleep(5);
        MessageSendReqs reqs = new MessageSendReqs()
                .target(event.getEvent().getMessage().getMessageId())
                .addContents(new ArrayList<>() {{
                    add(MessageSendReqs.at(event.getEvent().getSender().getSenderId().getOpenId()));
                    add(MessageSendReqs.text(userUtils.getSenderName(event) + "cpdd"));
                }});
        messageUtils.reply(reqs.build());
    }

    private static void sleep(int sc) {
        try {
            log.info("sleep-{}", sc);
            Thread.sleep(sc * 1000L);
        } catch (Exception e) {
            log.info("sleep-failed", e);
        }
    }
}
