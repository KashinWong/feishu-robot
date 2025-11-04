package com.hjx.handler;

import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;

public interface EventHandler {

    String command();

    void handle(P2MessageReceiveV1 event);
}
