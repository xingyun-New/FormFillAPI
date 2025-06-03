# FormFillAPI Docker部署配置
FROM openjdk:17-jre-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r formfill && useradd -r -g formfill formfill

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p templates output logs \
    && chown -R formfill:formfill /app

# 复制应用jar包
COPY target/form-fill-api-1.0.0.jar app.jar

# 复制模板文件
COPY templates/ templates/

# 设置文件权限
RUN chown -R formfill:formfill /app

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# 切换到应用用户
USER formfill

# JVM调优参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"] 