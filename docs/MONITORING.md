# 校园互助平台监控运维指南

## 一、日志规范

### 1.1 日志级别

项目采用统一的日志级别规范，确保日志信息的一致性和可读性。日志级别从低到高分为 DEBUG、INFO、WARN、ERROR 四个级别。

**DEBUG 级别**：用于开发调试，记录详细的执行过程和变量值。生产环境通常关闭 DEBUG 日志，只在排查问题时临时开启。示例场景：方法入口参数、循环执行过程、中间计算结果。

**INFO 级别**：用于记录正常的业务操作和系统事件。INFO 日志应该包含足够的上下文信息，便于后续分析和审计。示例场景：用户登录成功、API 请求处理完成、定时任务执行。

**WARN 级别**：用于记录潜在问题和异常情况，但不阻断程序执行。WARN 日志需要关注，但不一定需要立即处理。示例场景：参数接近限制、缓存命中率低、尝试访问不存在的数据。

**ERROR 级别**：用于记录错误和异常情况，影响当前请求或功能的正常执行。ERROR 日志需要及时关注和处理。示例场景：数据库操作失败、第三方服务调用异常、空指针异常。

### 1.2 日志格式

后端日志采用统一的格式，包含时间戳、日志级别、类名、方法名和日志内容。日志格式便于在日志管理系统中解析和检索。

```
[2026-01-15 10:30:45.123] [INFO] [campus.forum.service.PostService:56] - 用户[123]创建帖子成功，帖子ID: 456
[2026-01-15 10:30:46.234] [WARN] [campus.user.service.UserService:89] - 用户[123]登录密码错误，尝试次数: 3
[2026-01-15 10:30:47.345] [ERROR] [campus.common.handler.GlobalExceptionHandler:42] - 请求处理异常: /api/post/456
java.lang.NullPointerException: Cannot invoke method getTitle() on null object
    at com.campus.modules.forum.service.PostService.getPostById(PostService.java:56)
```

**日志字段规范**：

| 字段 | 说明 |
|------|------|
| timestamp | 日志时间戳，精确到毫秒 |
| level | 日志级别 |
| class | 完整的类名 |
| line | 行号 |
| message | 日志消息 |
| context | 上下文信息，如用户ID、请求ID |

### 1.3 日志配置

后端使用 Logback 作为日志框架，配置文件位于 `backend/src/main/resources/logback-spring.xml`。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 定义日志文件路径 -->
    <property name="LOG_PATH" value="./logs"/>
    <property name="APP_NAME" value="campus"/>

    <!-- 控制台输出格式 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%logger{50}:%line] - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <!-- INFO 日志文件 -->
    <appender name="INFO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}-info.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}-info.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%logger{50}:%line] - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>INFO</level>
        </filter>
    </appender>

    <!-- ERROR 日志文件 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APP_NAME}-error.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APP_NAME}-error.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%logger{50}:%line] - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
    </appender>

    <!-- 应用日志级别控制 -->
    <logger name="com.campus" level="INFO"/>
    <logger name="org.springframework" level="WARN"/>
    <logger name="org.mybatis" level="WARN"/>
    <logger name="com.mysql" level="WARN"/>

    <!-- 控制台输出 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="INFO_FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
</configuration>
```

### 1.4 日志规范实践

**正确示例**：

```java
// 使用 SLF4J 日志门面
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostVO getPostById(Long id) {
        logger.info("开始查询帖子，帖子ID: {}", id);

        try {
            Post post = postMapper.selectById(id);
            if (post == null) {
                logger.warn("帖子不存在，帖子ID: {}", id);
                throw new BusinessException("帖子不存在");
            }

            PostVO vo = convertToVO(post);
            logger.info("帖子查询成功，帖子ID: {}", id);
            return vo;
        } catch (Exception e) {
            logger.error("帖子查询失败，帖子ID: {}，错误信息: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
```

**错误示例**（禁止）：

```java
// 错误1: 使用 System.out 打印日志
System.out.println("User logged in");

// 错误2: 打印敏感信息
logger.info("User password: " + password);

// 错误3: 留空 catch 块
try {
    // some code
} catch (Exception e) {
    // 空日志
}

// 错误4: 日志消息不完整
logger.info("Error occurred");
```

## 二、健康检查

### 2.1 健康检查接口

后端提供健康检查接口，用于监控系统服务状态。健康检查接口返回系统的运行状态和依赖服务的连接状态。

```java
@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();

        // 检查数据库连接
        boolean dbHealthy = checkDatabase();
        health.put("database", dbHealthy ? "UP" : "DOWN");

        // 检查 Redis 连接
        boolean redisHealthy = checkRedis();
        health.put("redis", redisHealthy ? "UP" : "DOWN");

        // 系统信息
        health.put("uptime", System.currentTimeMillis() - SpringApplication.startTime);
        health.put("memory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");

        boolean allHealthy = dbHealthy && redisHealthy;
        return allHealthy ? Result.success(health) : Result.error("服务异常");
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            String result = redisTemplate.getConnectionFactory()
                    .getConnection().ping();
            return "PONG".equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2.2 Docker 健康检查

在 Docker 环境中，可以使用健康检查机制监控容器状态。

**docker-compose.yml 健康检查配置**：

```yaml
services:
  backend:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  mysql:
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**查看健康状态**：

```bash
# 查看容器健康状态
docker ps

# 查看详细健康信息
docker inspect --format='{{.State.Health.Status}}' <container_name>
```

### 2.3 监控指标

建议采集以下监控指标用于系统监控：

| 指标类别 | 指标名称 | 说明 |
|----------|----------|------|
| 系统 | CPU 使用率 | CPU 负载和利用率 |
| 系统 | 内存使用率 | JVM 堆内存和系统内存 |
| 系统 | 磁盘使用率 | 日志盘和数据盘空间 |
| 应用 | 请求 QPS | 每秒请求数 |
| 应用 | 响应时间 | 平均响应时间、P95、P99 |
| 应用 | 错误率 | 请求失败比例 |
| 数据库 | 连接池使用率 | Druid 连接池使用情况 |
| 数据库 | 查询慢 SQL | 执行时间超过阈值的 SQL |

## 三、性能监控

### 3.1 监控工具选择

项目推荐使用以下监控工具进行性能监控：

- **Spring Boot Actuator**：提供应用内部的健康检查和指标数据
- **Prometheus + Grafana**：时序数据库和可视化平台，用于数据采集和展示
- **SkyWalking**：分布式追踪系统，用于性能分析和问题排查
- **Arthas**：Java 诊断工具，用于运行时问题排查

### 3.2 Spring Boot Actuator 配置

添加 Spring Boot Actuator 依赖并配置端点：

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
```

### 3.3 性能指标采集

**API 响应时间监控**：

```java
@Component
@Aspect
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* com.campus.modules..*.controller..*(..))")
    public Object monitorTime(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = point.getSignature().getName();

        try {
            return point.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.info("API {}.{} 执行时间: {}ms",
                    point.getTarget().getClass().getSimpleName(),
                    methodName,
                    duration);

            // 记录到 Prometheus
            if (duration > 1000) {
                logger.warn("API {}.{} 执行时间过长: {}ms",
                        point.getTarget().getClass().getSimpleName(),
                        methodName,
                        duration);
            }
        }
    }
}
```

### 3.4 慢查询日志

配置 MyBatis 打印慢 SQL 日志：

```yaml
# application.yml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath*:mapper/**/*Mapper.xml
```

生产环境使用独立的慢查询日志配置：

```xml
<!-- logback-spring.xml -->
<logger name="com.campus.modules" level="DEBUG"/>
<logger name="java.sql.Connection" level="DEBUG"/>
<logger name="java.sql.Statement" level="DEBUG"/>
```

## 四、告警配置

### 4.1 告警规则

建议配置以下告警规则，及时发现和响应系统异常：

| 告警项 | 阈值 | 级别 | 说明 |
|--------|------|------|------|
| CPU 使用率 | > 80% 持续 5 分钟 | 警告 | CPU 负载过高 |
| 内存使用率 | > 85% | 警告 | 内存不足 |
| 磁盘使用率 | > 90% | 严重 | 磁盘空间不足 |
| API 响应时间 P99 | > 3000ms | 警告 | 响应过慢 |
| 错误率 | > 5% | 严重 | 错误率过高 |
| 数据库连接池 | > 80% | 警告 | 连接池即将耗尽 |
| 服务不可用 | 持续 1 分钟 | 严重 | 服务宕机 |

### 4.2 告警通知

告警通知支持多种渠道：

- **邮件通知**：适用于日常工作告警
- **短信通知**：适用于紧急告警
- **钉钉/飞书/企业微信**：适用于团队协作通知
- **电话通知**：适用于严重故障

### 4.3 告警配置示例

**Prometheus 告警规则**：

```yaml
# prometheus/alert-rules.yml
groups:
  - name: campus-alerts
    rules:
      - alert: HighCpuUsage
        expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU 使用率过高"
          description: "实例 {{ $labels.instance }} CPU 使用率超过 80%，当前值: {{ $value }}%"

      - alert: HighMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "实例 {{ $labels.instance }} 内存使用率超过 85%，当前值: {{ $value }}%"

      - alert: HighErrorRate
        expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) > 0.05
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "API 错误率过高"
          description: "API 错误率超过 5%，当前值: {{ $value | humanizePercentage }}"
```

## 五、故障排查

### 5.1 常见故障排查

**服务无法启动**：

```bash
# 1. 查看容器日志
docker-compose logs backend

# 2. 检查端口占用
netstat -tlnp | grep 8080

# 3. 检查配置是否正确
docker exec -it campus-backend cat /app/application-prod.yml

# 4. 进入容器内部排查
docker exec -it campus-backend /bin/bash
```

**数据库连接失败**：

```bash
# 1. 检查数据库服务状态
docker ps | grep mysql

# 2. 测试数据库连接
docker exec -it campus-mysql mysql -u root -proot123456

# 3. 检查连接配置
docker exec -it campus-backend env | grep DB

# 4. 查看数据库日志
docker logs campus-mysql
```

**API 请求超时**：

```bash
# 1. 检查后端服务响应时间
curl -w "\nTotal time: %{time_total}s\n" http://localhost:8080/api/health

# 2. 检查数据库慢查询
docker exec -it campus-mysql mysql -u root -proot123456 -e "SHOW FULL PROCESSLIST"

# 3. 检查系统资源使用情况
docker stats
```

### 5.2 日志分析方法

**查看错误日志**：

```bash
# 查看最近 100 行错误日志
tail -n 100 ./logs/campus-error.log

# 实时查看错误日志
tail -f ./logs/campus-error.log

# 搜索包含特定内容的日志
grep "NullPointerException" ./logs/campus-error.log

# 搜索特定时间段的日志
awk '$1 >= "[2026-01-15 10:00" && $1 <= "[2026-01-15 12:00"' ./logs/campus-info.log
```

**日志分析技巧**：

1. 根据错误时间点定位相关日志
2. 搜索异常堆栈信息
3. 关联分析请求 ID
4. 比对正常请求和异常请求的差异

### 5.3 问题排查工具

**Arthas 诊断工具**：

```bash
# 启动 Arthas
docker exec -it campus-backend java -jar /app/arthas-boot.jar

# 使用 Arthas 命令排查问题
# 查看方法调用链路
watch com.campus.modules.post.service PostService getPostById '{params, returnObj}'

# 查看方法执行耗时
stack com.campus.modules.post.service PostService getPostById

# 查看线程堆栈
thread

# 查看 JVM 信息
dashboard
```

### 5.4 故障恢复流程

1. **发现故障**：监控告警或用户反馈
2. **初步判断**：查看监控指标和日志，定位故障范围
3. **应急处理**：根据预案进行降级、限流、重启等操作
4. **根因分析**：深入分析故障原因
5. **恢复验证**：确认服务恢复正常
6. **复盘总结**：编写故障报告，优化监控和预案

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
