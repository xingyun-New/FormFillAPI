@echo off
chcp 65001 > nul
echo 正在启动 FormFillAPI Java 服务...
echo =====================================


echo.
echo 启动 Spring Boot 应用程序...
echo 服务将在 http://localhost:8080 启动
echo 按 Ctrl+C 停止服务
echo.
echo 正在启动...

mvn spring-boot:run 