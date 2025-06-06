# 邮件发送API使用指南

## 🎯 功能概述

邮件发送API提供了发送带附件邮件的功能，支持从指定URL下载文件作为附件，并发送给配置的收件人。

## 📋 API端点详情

### POST /api/email/send

**功能**: 发送带附件的邮件  
**请求方式**: POST  
**Content-Type**: application/json; charset=utf-8  

**请求体格式**:
```json
{
    "download_url": "http://192.168.18.36:8080/api/download/納期回答登记表_filled_20250605_154827.xlsx",
    "formName": "纳期回答登记表",
    "formStatus": "RPAProcess"
}
```

**参数说明**:
- `download_url`: 附件下载链接 (必填)
- `formName`: 表单名称，用于生成邮件标题 (必填)
- `formStatus`: 表单状态 (必填)
  - `RPAProcess`: RPA自动化处理
  - `ManualProcess`: 人工处理
  - `Completed`: 已完成
  - `Failed`: 处理失败

## 🧪 测试示例

### PowerShell 测试

```powershell
# 发送邮件
$body = @{
    download_url = "http://192.168.18.36:8080/api/download/納期回答登记表_filled_20250605_154827.xlsx"
    formName = "纳期回答登记表"
    formStatus = "RPAProcess"
} | ConvertTo-Json -Depth 10

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    Write-Host "✓ 邮件发送成功" -ForegroundColor Green
    Write-Host "  主题: $($response.emailSubject)"
    Write-Host "  收件人: $($response.recipients -join ', ')"
    Write-Host "  附件: $($response.attachmentName)"
} catch {
    Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "  错误信息: $($errorDetails.message)"
        Write-Host "  错误代码: $($errorDetails.errorCode)"
    }
}
```

### JavaScript 测试

```javascript
async function sendEmail(downloadUrl, formName, formStatus) {
    try {
        const response = await fetch('http://localhost:8080/api/email/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=utf-8'
            },
            body: JSON.stringify({
                download_url: downloadUrl,
                formName: formName,
                formStatus: formStatus
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            console.log('✓ 邮件发送成功:', data);
            console.log('  主题:', data.emailSubject);
            console.log('  收件人:', data.recipients.join(', '));
            console.log('  附件:', data.attachmentName);
            return data;
        } else {
            console.error('✗ 邮件发送失败:', data.message);
            return null;
        }
    } catch (error) {
        console.error('✗ 请求错误:', error);
        return null;
    }
}

// 使用示例
sendEmail(
    'http://192.168.18.36:8080/api/download/納期回答登记表_filled_20250605_154827.xlsx',
    '纳期回答登记表',
    'RPAProcess'
);
```

### Python 测试

```python
import requests
import json

def send_email(download_url, form_name, form_status):
    """发送邮件"""
    url = "http://localhost:8080/api/email/send"
    
    headers = {
        'Content-Type': 'application/json; charset=utf-8'
    }
    
    data = {
        "download_url": download_url,
        "formName": form_name,
        "formStatus": form_status
    }
    
    try:
        response = requests.post(url, json=data, headers=headers)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✓ 邮件发送成功")
            print(f"  主题: {result['emailSubject']}")
            print(f"  收件人: {', '.join(result['recipients'])}")
            print(f"  附件: {result['attachmentName']}")
            return result
        else:
            error_info = response.json()
            print(f"✗ 邮件发送失败: {error_info['message']}")
            if 'errorCode' in error_info:
                print(f"  错误代码: {error_info['errorCode']}")
            return None
            
    except requests.RequestException as e:
        print(f"✗ 网络错误: {e}")
        return None

# 使用示例
if __name__ == "__main__":
    send_email(
        "http://192.168.18.36:8080/api/download/納期回答登记表_filled_20250605_154827.xlsx",
        "纳期回答登记表",
        "RPAProcess"
    )
```

### cURL 测试

```bash
# 发送邮件
curl -X POST "http://localhost:8080/api/email/send" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{
       "download_url": "http://192.168.18.36:8080/api/download/納期回答登记表_filled_20250605_154827.xlsx",
       "formName": "纳期回答登记表",
       "formStatus": "RPAProcess"
     }'
```

## 📊 响应格式

### 成功响应 (200 OK)

```json
{
    "success": true,
    "message": "邮件发送成功",
    "emailSubject": "纳期回答登记表 - 表单处理完成通知 - 2025-06-05 15:48:27",
    "recipients": [
        "recipient1@example.com",
        "recipient2@example.com"
    ],
    "attachmentName": "納期回答登记表_filled_20250605_154827.xlsx",
    "timestamp": "2025-06-05T15:48:27.123456",
    "errorCode": null
}
```

### 失败响应 (400 Bad Request / 500 Internal Server Error)

```json
{
    "success": false,
    "message": "邮件发送失败: 文件下载失败",
    "emailSubject": null,
    "recipients": null,
    "attachmentName": null,
    "timestamp": "2025-06-05T15:48:27.123456",
    "errorCode": "EMAIL_SEND_ERROR"
}
```

## ⚙️ 配置说明

### 邮件服务器配置 (application.yml)

```yaml
spring:
  mail:
    # 邮件服务器设置 (示例为QQ邮箱)
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com
    password: your_email_password  # QQ邮箱的授权码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
        debug: false
    protocol: smtp
    default-encoding: UTF-8
```

### 邮件内容配置

```yaml
email:
  # 邮件模板配置
  template:
    subject: "${formName} - 表单处理完成通知 - ${timestamp}"
    content: |
      尊敬的用户，您好！
      
      您提交的表单 "${formName}" 已经处理完成。
      
      表单状态：${formStatus}
      处理时间：${timestamp}
      
      请查看附件中的处理结果。
      
      如有任何问题，请联系系统管理员。
      
      此邮件为系统自动发送，请勿回复。
      
      谢谢！
  
  # 收件人列表
  recipients:
    - "recipient1@example.com"
    - "recipient2@example.com"
  
  # 发件人显示名称
  from-name: "表单处理系统"
```

### 常用邮箱配置

#### QQ邮箱
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587  # 或 25, 465(SSL)
    username: your_qq_email@qq.com
    password: your_authorization_code  # QQ邮箱授权码
```

#### 163邮箱
```yaml
spring:
  mail:
    host: smtp.163.com
    port: 25  # 或 994(SSL)
    username: your_163_email@163.com
    password: your_authorization_code
```

#### Gmail
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_gmail@gmail.com
    password: your_app_password
```

#### 企业邮箱
```yaml
spring:
  mail:
    host: smtp.exmail.qq.com  # 腾讯企业邮箱
    port: 587
    username: your_email@company.com
    password: your_password
```

## 🔍 状态码说明

| 状态码 | 含义 | 响应内容 |
|--------|------|----------|
| 200 | 邮件发送成功 | 包含邮件详细信息的JSON |
| 400 | 请求参数错误 | 参数验证失败信息 |
| 500 | 服务器内部错误 | 系统错误信息 |

## 🛠️ 故障排除

### 1. 邮件发送失败

**问题**: `邮件发送失败: Mail server connection failed`

**解决方案**:
- 检查邮箱服务器配置 (host, port)
- 确认用户名和密码正确
- 检查网络连接是否正常

### 2. 身份验证失败

**问题**: `邮件发送失败: Authentication failed`

**解决方案**:
- QQ邮箱：使用授权码而不是登录密码
- 163邮箱：开启SMTP服务并获取授权码
- Gmail：使用应用专用密码

### 3. 文件下载失败

**问题**: `文件下载失败: Connection timed out`

**解决方案**:
- 检查download_url是否可访问
- 确认网络连接正常
- 检查文件是否存在

### 4. 附件过大

**问题**: `邮件发送失败: Message size exceeds maximum`

**解决方案**:
- 检查邮箱服务商的附件大小限制
- 考虑压缩文件或使用文件分享链接

## 📝 注意事项

1. **邮箱安全设置**: 
   - QQ邮箱需要开启SMTP服务并获取授权码
   - Gmail需要开启两步验证并使用应用专用密码

2. **网络安全**:
   - 生产环境建议使用SSL/TLS加密连接
   - 配置防火墙允许SMTP端口访问

3. **性能考虑**:
   - 大文件下载可能需要较长时间
   - 建议设置合理的超时时间

4. **邮件模板**:
   - 支持变量替换: `${formName}`, `${formStatus}`, `${timestamp}`
   - 可以自定义邮件主题和内容

## 🎯 使用建议

1. **测试环境**: 先在测试环境验证邮箱配置和网络连通性
2. **监控日志**: 关注邮件发送日志，及时发现问题
3. **错误处理**: 根据错误码进行相应的重试或报警处理
4. **安全配置**: 定期更新邮箱密码和授权码

## 📋 健康检查

```bash
# 检查邮件服务是否正常
curl http://localhost:8080/api/email/health
```

现在您可以使用邮件发送API来发送带附件的邮件了！🎉 