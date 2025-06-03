# FormFillAPI - Excel表单填写API服务

## 概述

这是一个基于Spring Boot的Excel表单填写API服务，支持精确的坐标填写模式。通过API接口，您可以根据Excel单元格坐标准确填写表单数据。

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
├── logs/                                     # 日志目录
└── deploy/                                   # 部署配置目录
    ├── 快速部署指南.md                        # 快速部署指南
    ├── 云服务器部署指南.md                    # 详细部署指南
    ├── scripts/                              # 部署脚本
    └── docker/                               # Docker部署配置
```

## 快速开始

### 1. 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本（或使用内置的Maven Wrapper）

### 2. 安装Maven

如果您还没有安装Maven，可以运行以下脚本自动安装：

```bash
# 自动安装Maven（无需管理员权限）
.\install-maven.bat

# 或者如果有管理员权限，安装Chocolatey和Maven
.\install-chocolatey.bat
```

### 3. 启动应用

有多种启动方式：

```bash
# 方式1: 使用Maven（推荐）
.\run-java.bat

# 方式2: 使用Maven Wrapper（无需安装Maven）
.\run-java-wrapper.bat

# 方式3: 手动编译和运行
mvn clean compile
mvn spring-boot:run
```

### 4. 访问API

服务启动后访问: `http://localhost:8080`

## API文档

### 基础信息
- 基础路径: `/api`
- 端口: `8080`
- 内容类型: `application/json`

### 主要端点

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

#### 2. 根据表单名称获取模板信息
```http
GET /api/template/{formName}
```

**请求示例:**
```http
GET /api/template/员工信息表
```

**成功响应示例:**
```json
{
    "success": true,
    "formName": "员工信息表",
    "templateFound": true,
    "fileName": "员工信息表.xlsx",
    "templatePath": "/path/to/templates/员工信息表.xlsx",
    "downloadUrl": "/api/template/download/员工信息表.xlsx",
    "previewUrl": "/api/template/preview/员工信息表.xlsx",
    "timestamp": "2024-05-31T14:30:00"
}
```

**未找到模板响应示例:**
```json
{
    "success": false,
    "formName": "不存在的表单",
    "templateFound": false,
    "message": "Template not found for form: 不存在的表单",
    "suggestion": "Please check if the template file exists in the templates directory",
    "timestamp": "2024-05-31T14:30:00"
}
```

#### 3. 下载模板文件
```http
GET /api/template/download/{filename}
```

**请求示例:**
```http
GET /api/template/download/员工信息表.xlsx
```

**响应:** 直接下载Excel文件

#### 4. 预览模板文件信息
```http
GET /api/template/preview/{filename}
```

**请求示例:**
```http
GET /api/template/preview/员工信息表.xlsx
```

**响应示例:**
```json
{
    "success": true,
    "fileName": "员工信息表.xlsx",
    "filePath": "/path/to/templates/员工信息表.xlsx",
    "fileSize": 12345,
    "lastModified": 1640995200000,
    "readable": true,
    "downloadUrl": "/api/template/download/员工信息表.xlsx",
    "timestamp": "2024-05-31T14:30:00"
}
```

#### 5. 填写表单
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

#### 6. 下载已填写的表单文件
```http
GET /api/download/{filename}
```

#### 7. 获取所有模板列表
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
            "path": "/path/to/templates/员工信息表.xlsx",
            "downloadUrl": "/api/template/download/员工信息表.xlsx",
            "previewUrl": "/api/template/preview/员工信息表.xlsx",
            "getTemplateUrl": "/api/template/员工信息表"
        }
    ],
    "count": 1
}
```

## 数据格式说明

### 坐标填写格式
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

**格式说明:**
- **坐标格式**: `"[行, 列]"` - JSON数组格式的字符串
- **值格式**: `["值"]` - 字符串数组
- **行列编号**: 从1开始计数

### 工作流程

1. **查找模板**: 在`templates/`目录中查找对应的Excel模板文件
2. **坐标填写**: 根据提供的坐标精确填写单元格
3. **保存文件**: 将填写完成的文件保存到`output/`目录
4. **返回结果**: 提供下载链接和填写统计信息

## 配置说明

### application.yml 主要配置

```yaml
server:
  port: 8080                    # 服务端口

spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 最大文件大小

logging:
  level:
    com.formfill.api: INFO     # 应用日志级别
  file:
    name: logs/formfill-api.log # 日志文件路径
```

## 部署和运维

### 📚 部署指南

选择适合您的部署方式：

| 部署方式 | 适用场景 | 文档链接 |
|---------|---------|---------|
| **🐳 Docker部署** | 快速部署、开发测试 | [快速部署指南](deploy/快速部署指南.md) |
| **🖥️ 传统部署** | 生产环境、精细控制 | [云服务器部署指南](deploy/云服务器部署指南.md) |
| **☁️ 云平台部署** | 大规模、高可用 | [云服务器部署指南](deploy/云服务器部署指南.md) |

### 🚀 快速部署

#### Docker部署（推荐）
```bash
# 一键Docker部署
cd deploy/docker
chmod +x docker-deploy.sh
./docker-deploy.sh
```

#### 云服务器部署
```bash
# 环境检查
chmod +x deploy/scripts/check-env.sh
./deploy/scripts/check-env.sh

# 一键部署
./deploy-all.sh
```

### 生产环境运行

```bash
# 打包应用
mvn clean package

# 运行jar包
java -jar target/form-fill-api-1.0.0.jar

# 指定配置文件
java -jar target/form-fill-api-1.0.0.jar --spring.profiles.active=prod
```

### 性能优化

```bash
# 设置JVM参数
java -Xmx2g -Xms1g -XX:+UseG1GC -jar target/form-fill-api-1.0.0.jar
```

### 监控端点

- 健康检查: `GET /api/health`
- 应用信息: `GET /actuator/info`
- 详细健康状态: `GET /actuator/health`

## 故障排除

### 常见问题

1. **端口冲突**: 修改`application.yml`中的`server.port`
2. **Java版本**: 确保使用Java 17或更高版本
3. **Maven问题**: 运行`.\install-maven.bat`自动安装
4. **权限问题**: 确保`templates`和`output`目录有读写权限

### 日志查看

```bash
# 实时查看日志
Get-Content logs/formfill-api.log -Wait

# 查看错误日志
Select-String "ERROR" logs/formfill-api.log
```

## 开发指南

### 添加新功能

1. **服务层**: 在`FormFillerService`中添加业务逻辑
2. **控制层**: 在`FormFillController`中添加新的API端点
3. **DTO**: 在`dto`包中添加请求/响应类

### 测试

```bash
# 运行单元测试
mvn test

# 运行简单功能测试
java -cp target/classes com.formfill.api.test.SimpleTest
```

## 许可证

MIT License

## 支持

如有问题，请查看日志文件 `logs/formfill-api.log` 或提交Issue。

---

## 📋 部署检查清单

在部署前，请确保：

- [ ] Java 17+ 已安装
- [ ] Maven 已配置
- [ ] 服务器有足够的内存和磁盘空间
- [ ] 防火墙已正确配置
- [ ] 域名DNS已设置（如果需要）

详细的部署步骤和故障排除，请参考 [部署指南文档](deploy/)。 