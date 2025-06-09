# FormFillAPI - Java版本

## 概述

这是一个基于Spring Boot的Excel表单填写API服务，支持精确的坐标填写模式。

## 技术栈

- **Java**: 17+
- **Spring Boot**: 3.2.0
- **Apache POI**: 5.2.4 (Excel操作)
- **Maven**: 构建工具
- **SLF4J + Logback**: 日志框架

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/formfill/api/
│   │       ├── FormFillApiApplication.java    # 主应用类
│   │       ├── controller/
│   │       │   └── FormFillController.java    # REST控制器
│   │       ├── service/
│   │       │   └── FormFillerService.java     # 核心服务类
│   │       └── dto/
│   │           └── FormFillRequest.java       # 请求DTO
│   └── resources/
│       └── application.yml                    # 配置文件
├── templates/                                 # Excel模板目录
├── output/                                   # 输出文件目录
└── logs/                                     # 日志目录
```

## 快速开始

### 1. 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 2. 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/form-fill-api-1.0.0.jar
```

### 3. 访问API

服务启动后访问: `http://localhost:8080`

## API文档

### 基础信息
- 基础路径: `/api`
- 端口: `8080`
- 内容类型: `application/json`

### 端点列表

#### 1. 健康检查
```http
GET /api/health
```

**响应示例:**
```json
{
    "status": "healthy",
    "timestamp": "2024-05-31T14:30:00",
    "service": "FormFillAPI"
}
```

#### 2. 填写表单
```http
POST /api/fill-form
Content-Type: application/json

{
    "formName": "员工信息表",
    "formContent": {
        "[3, 2]": ["张三"],
        "[4, 2]": ["男"],
        "[5, 2]": ["28"]
    }
}
```

**响应示例:**
```json
{
    "success": true,
    "message": "表单填写成功",
    "output_file": "output/员工信息表_已填写_20240531_143000.xlsx",
    "download_url": "/api/download/员工信息表_已填写_20240531_143000.xlsx",
    "filled_count": 3,
    "total_fields": 3,
    "template_used": "templates/员工信息表.xlsx",
    "timestamp": "2024-05-31T14:30:00"
}
```

#### 3. 下载文件
```http
GET /api/download/{filename}
```

#### 4. 获取模板列表
```http
GET /api/templates
```

**响应示例:**
```json
{
    "templates": [
        {
            "name": "员工信息表",
            "filename": "员工信息表.xlsx",
            "path": "/path/to/templates/员工信息表.xlsx"
        }
    ],
    "count": 1
}
```

## 数据格式

### 输入格式
```json
{
    "formName": "表单名称",
    "formContent": {
        "[行, 列]": ["值"],
        "[3, 2]": ["张三"],
        "[4, 2]": ["男"]
    }
}
```

### 格式说明
- **坐标格式**: `"[行, 列]"` - JSON数组格式的字符串
- **值格式**: `["值"]` - 字符串数组
- **行列编号**: 从1开始计数

## 配置说明

### application.yml 配置项

```yaml
server:
  port: 8080                    # 服务端口

spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 最大文件大小
      max-request-size: 50MB   # 最大请求大小

logging:
  level:
    com.formfill.api: INFO     # 应用日志级别
  file:
    name: logs/formfill-api.log # 日志文件路径
```

## 开发指南

### 添加新功能

1. **服务层**: 在 `FormFillerService` 中添加业务逻辑
2. **控制层**: 在 `FormFillController` 中添加新的端点
3. **DTO**: 在 `dto` 包中添加请求/响应类

### 日志配置

```java
private static final Logger logger = LoggerFactory.getLogger(YourClass.class);
logger.info("信息日志");
logger.error("错误日志", exception);
```

### 异常处理

服务使用统一的异常处理机制，返回标准格式的错误响应：

```json
{
    "success": false,
    "error": "错误信息",
    "code": "ERROR_CODE"
}
```

## 与Python版本的差异

| 特性 | Python版本 | Java版本 |
|------|-----------|----------|
| 框架 | Flask | Spring Boot |
| Excel库 | openpyxl | Apache POI |
| 日志 | Python logging | SLF4J + Logback |
| 配置 | config.py | application.yml |
| 端口 | 5000 | 8080 |
| 路径前缀 | / | /api |

## 部署

### 1. 生产环境配置

```yaml
# application-prod.yml
server:
  port: 8080

logging:
  level:
    com.formfill.api: WARN
    org.apache.poi: ERROR
```

### 2. 运行命令

```bash
# 指定生产环境配置
java -jar target/form-fill-api-1.0.0.jar --spring.profiles.active=prod

# 或设置环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar target/form-fill-api-1.0.0.jar
```

### 3. Docker部署 (可选)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/form-fill-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 监控和健康检查

- 健康检查: `GET /api/health`
- 应用信息: `GET /actuator/info`
- 详细健康状态: `GET /actuator/health`

## 性能优化

1. **内存设置**: 
   ```bash
   java -Xmx2g -Xms1g -jar target/form-fill-api-1.0.0.jar
   ```

2. **GC调优**: 
   ```bash
   java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target/form-fill-api-1.0.0.jar
   ```

## 故障排除

### 常见问题

1. **端口冲突**: 修改 `application.yml` 中的 `server.port`
2. **内存不足**: 增加 JVM 堆内存大小
3. **文件权限**: 确保 `templates` 和 `output` 目录有读写权限

### 日志查看

```bash
# 实时查看日志
tail -f logs/formfill-api.log

# 查看错误日志
grep ERROR logs/formfill-api.log
``` 