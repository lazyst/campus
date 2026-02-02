FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

# 设置时区
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 创建非 root 用户运行
RUN addgroup -g 1000 app && \
    adduser -u 1000 -G app -s /bin/sh -D app

# 复制构建产物
COPY --from=builder /app/target/backend-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R app:app /app

# 切换用户
USER app

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=8m"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
