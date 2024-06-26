# 使用 OpenJDK 8 作为基础镜像
FROM openjdk:17

# 复制构建好的 JAR 文件到容器中
COPY SandboxService.jar /app/SandboxService.jar

# 启动应用程序
ENTRYPOINT ["java", "-jar", "/app/SandboxService.jar"]