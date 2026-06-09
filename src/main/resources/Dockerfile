# backend/Dockerfile
# 基础镜像使用 Eclipse Temurin 官方 JDK 17
FROM eclipse-temurin:17-jdk-jammy
# 容器内工作目录
WORKDIR /app
# 将构建好的 Spring Boot 可执行 jar 复制到容器中并重命名为 app.jar
COPY DPoker-V0.1.0.jar app.jar
# 声明应用监听的端口
EXPOSE 8080
# 启动命令：
# 1. 通过 --spring.profiles.active=prod 激活 application-prod.yaml
# 2. 数据库连接参数（DB_HOST / DB_PORT / DB_NAME / DB_USER / DB_PASSWORD）
#    通过环境变量在 docker run / docker-compose 中注入，不在镜像内硬编码
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]