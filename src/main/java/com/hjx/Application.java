package com.hjx;

import com.hjx.handler.EventHandler;
import com.hjx.message.MessageUtils;
import com.hjx.utils.CommonUtils;
import com.hjx.utils.SimpleLock;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.MentionEvent;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class Application implements CommandLineRunner {

    private static final Map<String, LinkedBlockingQueue<P2MessageReceiveV1>> QUEUE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> EXECUTOR_MAP = new ConcurrentHashMap<>();

    private final List<EventHandler> handlers;

    private final MessageUtils messageUtils;

    @Value("${feishu.ws.appId:}")
    private String appId;

    @Value("${feishu.ws.secret:}")
    private String secret;

    @Value("${robot.name:}")
    private String robotName;

    public static void main(String[] args) {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
        } else {
            System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
        }
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        for (EventHandler handler : handlers) {
            QUEUE_MAP.put(handler.command(), new LinkedBlockingQueue<>());
            EXECUTOR_MAP.put(handler.command(), Executors.newSingleThreadExecutor());
        }
        Client cli = new Client.Builder(appId, secret)
                .eventHandler(EventDispatcher.newBuilder("", "")
                        .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                            @Override
                            public void handle(P2MessageReceiveV1 event) throws Exception {

                                log.info("receive-message,{}", Jsons.DEFAULT.toJson(event.getEvent()));

                                if (Objects.isNull(event.getEvent().getMessage().getMentions())) {
                                    return;
                                }

                                if (SimpleLock.lock(event.getEvent().getMessage().getMessageId())) {
                                    return;
                                }

                                MentionEvent mention = event.getEvent().getMessage().getMentions()[0];

                                if (!robotName.equalsIgnoreCase(mention.getName())) {
                                    return;
                                }

                                EventHandler handler = handlers.stream().filter(h -> {
                                    try {
                                        return h.command().equalsIgnoreCase(CommonUtils.getCommand(event));
                                    } catch (Exception e) {
                                        messageUtils.replyText(event, "解析命令失败");
                                        return false;
                                    }
                                }).findFirst().orElse(null);

                                if (Objects.isNull(handler)) {
                                    messageUtils.replyText(event, "未知指令");
                                    return;
                                }

                                LinkedBlockingQueue<P2MessageReceiveV1> queue = QUEUE_MAP.get(handler.command());
                                ExecutorService executor = EXECUTOR_MAP.get(handler.command());
                                queue.add(event);
                                executor.submit(() -> {
                                    try {
                                        P2MessageReceiveV1 take = queue.take();
                                        handler.handle(take);
                                    } catch (Exception e) {
                                        log.error("", e);
                                    }
                                });

                                log.info("{} 任务数量:{}", handler.command(), queue.size());
                            }
                        })
                        .build())
                .build();
        cli.start();
    }

}
