# 飞书机器人框架

基于Spring Boot的可扩展飞书机器人框架，支持自定义命令处理器，快速构建企业级聊天机器人。

## 功能特性

- 🤖 **插件化架构** - 基于EventHandler接口的可扩展命令处理系统
- 🔄 **异步处理** - 每个命令独立队列，支持并发处理
- 📱 **飞书集成** - 完整的飞书开放平台SDK集成
- 🛡️ **消息去重** - 内置消息锁机制，防止重复处理
- 🎯 **@提及响应** - 智能识别机器人@提及消息
- 📊 **任务监控** - 实时显示各命令队列任务数量

## 技术栈

- Java 11
- Spring Boot 2.6.2
- 飞书开放平台SDK
- Hutool工具库
- Caffeine缓存

## 环境要求

- JDK 11+
- Maven 3.6+

## 配置说明

### 环境变量

设置以下环境变量：

```bash
# 机器人配置
ROBOT_NAME=your-robot-name

# 飞书应用配置
FEISHU_APP_ID=your-feishu-app-id
FEISHU_APP_SECRET=your-feishu-app-secret
```

## 快速开始

### 本地运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd feishu-robot
   ```

2. **配置环境变量**
   ```bash
   export ROBOT_NAME=your-robot-name
   export FEISHU_APP_ID=your-app-id
   export FEISHU_APP_SECRET=your-app-secret
   ```

3. **编译运行**
   ```bash
   mvn clean package
   java -jar target/feishu-robot-1.0-SNAPSHOT.jar
   ```

### Docker部署

1. **构建镜像**
   ```bash
   mvn clean package
   docker build -t feishu-robot .
   ```

2. **运行容器**
   ```bash
   docker run -d \
     --name feishu-robot \
     -e ROBOT_NAME=your-robot-name \
     -e FEISHU_APP_ID=your-app-id \
     -e FEISHU_APP_SECRET=your-app-secret \
     feishu-robot
   ```

## 使用说明

### 基础命令

在飞书群聊中@机器人并发送命令：

```
@机器人 在吗
```

机器人会回复确认消息。

## 自定义扩展

### 创建命令处理器

1. **实现EventHandler接口**

```java
@Component
@RequiredArgsConstructor
public class CustomHandler implements EventHandler {
    
    private final MessageUtils messageUtils;
    
    @Override
    public String command() {
        return "自定义命令";  // 用户@机器人时的触发词
    }
    
    @Override
    public void handle(P2MessageReceiveV1 event) {
        // 处理逻辑
        messageUtils.replyText(event, "处理完成");
    }
}
```

2. **消息回复示例**

```java
// 简单文本回复
messageUtils.replyText(event, "回复内容");

// 复杂消息回复
MessageSendReqs reqs = new MessageSendReqs()
    .target(event.getEvent().getMessage().getMessageId())
    .addLine(MessageSendReqs.at(event.getEvent().getSender().getSenderId().getOpenId()))
    .addLine(MessageSendReqs.text("消息内容"));
messageUtils.reply(reqs.build());
```

## 项目结构

```
src/main/java/com/hjx/
├── handler/            # 事件处理器
│   ├── impl/          # 具体处理实现
│   └── EventHandler.java  # 处理器接口
├── message/           # 消息处理工具
├── utils/             # 工具类
└── Application.java   # 启动类
```

## 核心组件

- **EventHandler** - 命令处理器接口
- **MessageUtils** - 消息发送工具类
- **SimpleLock** - 消息去重锁
- **CommonUtils** - 通用工具方法
- **UserUtils** - 用户信息工具

## 注意事项

- 飞书机器人需要配置消息接收权限
- 每个命令处理器会创建独立的执行队列
- 消息处理支持异步，避免阻塞
- 建议在生产环境使用Docker部署

## 故障排除

### 常见问题

1. **机器人无响应**
   - 检查飞书应用配置是否正确
   - 确认机器人名称与配置一致
   - 验证网络连接

2. **命令不识别**
   - 确保@提及了机器人
   - 检查命令格式是否正确
   - 查看日志确认消息是否接收

3. **消息发送失败**
   - 验证飞书应用权限配置
   - 检查消息格式是否符合要求

## 开发指南

### 添加新功能

1. 在 `handler/impl/` 目录下创建新的处理器
2. 实现 `EventHandler` 接口
3. 使用 `@Component` 注解注册到Spring容器
4. 重启应用即可生效

### 调试技巧

- 查看控制台日志了解消息接收情况
- 使用 `log.info()` 输出调试信息
- 检查队列任务数量监控处理进度
