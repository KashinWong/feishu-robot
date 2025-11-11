package com.hjx.utils;

import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.GetUserReq;
import com.lark.oapi.service.contact.v3.model.GetUserResp;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UserUtils {

    @Value("${feishu.ws.appId:}")
    private String appId;

    @Value("${feishu.ws.secret:}")
    private String secret;

    private Client client;

    @PostConstruct
    public void init() {
        client = Client.newBuilder(appId, secret).build();
    }

    public String getUserName(String userId) {
        try {
            // 创建请求对象
            GetUserReq req = GetUserReq.newBuilder()
                    .userId(userId)
                    .userIdType("open_id")
                    .build();

            // 发起请求
            GetUserResp resp = client.contact().v3().user().get(req);
            return resp.getData().getUser().getName();
        } catch (Exception e) {
            log.error("获取用户名失败", e);
            return "unknown";
        }
    }


    public String getSenderName(P2MessageReceiveV1 event) {
        return getUserName(event.getEvent().getSender().getSenderId().getOpenId());
    }
}
