# 邮件发送API使用指南 (更新版 v3.0)

## 🎯 功能概述

邮件发送API提供了完全灵活的邮件发送功能，支持：
- **自定义收件人**: mailTo（必填）指定邮件收件人
- **可选抄送**: mailCc（可选）指定邮件抄送人
- **自定义内容**: 支持自定义邮件标题和内容
- **可选附件**: download_url 是可选参数，没有提供时发送不带附件的邮件
- **智能默认值**: 未提供的参数会使用合理的默认值
- **完全灵活**: 除mailTo外，其他参数都是可选的

## 📋 API端点详情

### POST /api/email/send

**功能**: 发送邮件（自定义收件人和抄送）  
**请求方式**: POST  
**Content-Type**: application/json; charset=utf-8  

**请求体格式**:
```json
{
    "mailContent": "这里是邮件内容",
    "mailTitle": "这里是邮件标题",
    "mailTo": "xingyun1982314@126.com",
    "mailCc": "xingyun@murata.com"
}
```

**参数说明**:
- `mailTo`: 邮件收件人 (**必填** - 必须是有效的邮箱地址)
- `mailCc`: 邮件抄送人 (**可选** - 必须是有效的邮箱地址)
- `mailTitle`: 邮件标题 (**可选** - 没有提供时使用默认模板)
- `mailContent`: 邮件内容 (**可选** - 没有提供时使用默认模板)
- `download_url`: 附件下载链接 (**可选** - 没有提供时发送不带附件的邮件)
- `formName`: 表单名称 (**可选** - 用于生成默认标题和内容)
- `formStatus`: 表单状态 (**可选** - 用于生成默认内容)

## 🧪 测试示例

### PowerShell 测试

#### 1. 您的需求示例（完整功能）
```powershell
$body = @{
    mailContent = "这里是邮件内容"
    mailTitle = "这里是邮件标题"
    mailTo = "xingyun1982314@126.com"
    mailCc = "xingyun@murata.com"
} | ConvertTo-Json -Depth 10

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    Write-Host "✓ 邮件发送成功" -ForegroundColor Green
    Write-Host "  主题: $($response.emailSubject)"
    Write-Host "  收件人: $($response.recipients -join ', ')"
} catch {
    Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
}
```

#### 2. 仅收件人（不含抄送）
```powershell
$body = @{
    mailContent = "测试邮件内容"
    mailTitle = "测试邮件标题"
    mailTo = "xingyun1982314@126.com"
} | ConvertTo-Json -Depth 10

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    Write-Host "✓ 无抄送邮件发送成功" -ForegroundColor Green
} catch {
    Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
}
```

#### 3. 带附件的邮件
```powershell
$body = @{
    download_url = "http://localhost:8080/api/download/纳期回答登记表_filled_20250605_154827.xlsx"
    mailContent = "请查看附件中的表单处理结果"
    mailTitle = "表单处理完成通知"
    mailTo = "xingyun1982314@126.com"
    mailCc = "xingyun@murata.com"
} | ConvertTo-Json -Depth 10

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    Write-Host "✓ 带附件邮件发送成功" -ForegroundColor Green
    Write-Host "  附件: $($response.attachmentName)"
} catch {
    Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
}
```

#### 4. 使用默认内容
```powershell
$body = @{
    formName = "员工信息表"
    formStatus = "Completed"
    mailTo = "xingyun1982314@126.com"
} | ConvertTo-Json -Depth 10

$utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
    Write-Host "✓ 默认内容邮件发送成功" -ForegroundColor Green
    Write-Host "  主题: $($response.emailSubject)"
} catch {
    Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
}
```

### JavaScript 测试

```javascript
// 灵活的邮件发送函数
async function sendEmail(options) {
    const requestBody = {
        mailTo: options.mailTo  // 必填字段
    };
    
    // 添加可选字段
    if (options.mailCc) requestBody.mailCc = options.mailCc;
    if (options.mailTitle) requestBody.mailTitle = options.mailTitle;
    if (options.mailContent) requestBody.mailContent = options.mailContent;
    if (options.downloadUrl) requestBody.download_url = options.downloadUrl;
    if (options.formName) requestBody.formName = options.formName;
    if (options.formStatus) requestBody.formStatus = options.formStatus;
    
    try {
        const response = await fetch('http://localhost:8080/api/email/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=utf-8'
            },
            body: JSON.stringify(requestBody)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            console.log('✓ 邮件发送成功:', data);
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

// 1. 您的需求示例
sendEmail({
    mailTo: 'xingyun1982314@126.com',
    mailCc: 'xingyun@murata.com',
    mailTitle: '这里是邮件标题',
    mailContent: '这里是邮件内容'
});

// 2. 仅收件人
sendEmail({
    mailTo: 'xingyun1982314@126.com',
    mailTitle: '测试邮件',
    mailContent: '测试内容'
});

// 3. 带附件
sendEmail({
    mailTo: 'xingyun1982314@126.com',
    mailCc: 'xingyun@murata.com',
    downloadUrl: 'http://localhost:8080/api/download/report.xlsx',
    mailTitle: '报告已完成',
    mailContent: '请查看附件中的报告'
});
```

### Python 测试

```python
import requests
import json

def send_email(**kwargs):
    """发送邮件 - 支持自定义收件人和抄送"""
    url = "http://localhost:8080/api/email/send"
    
    headers = {
        'Content-Type': 'application/json; charset=utf-8'
    }
    
    # mailTo 是必填字段
    if not kwargs.get('mail_to'):
        print("✗ 错误: mailTo 是必填字段")
        return None
    
    # 构建请求数据
    data = {
        "mailTo": kwargs['mail_to']
    }
    
    # 添加可选字段
    if kwargs.get('mail_cc'):
        data["mailCc"] = kwargs['mail_cc']
    if kwargs.get('mail_title'):
        data["mailTitle"] = kwargs['mail_title']
    if kwargs.get('mail_content'):
        data["mailContent"] = kwargs['mail_content']
    if kwargs.get('download_url'):
        data["download_url"] = kwargs['download_url']
    if kwargs.get('form_name'):
        data["formName"] = kwargs['form_name']
    if kwargs.get('form_status'):
        data["formStatus"] = kwargs['form_status']
    
    try:
        response = requests.post(url, json=data, headers=headers)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✓ 邮件发送成功")
            print(f"  主题: {result['emailSubject']}")
            print(f"  收件人: {', '.join(result['recipients'])}")
            return result
        else:
            error_info = response.json()
            print(f"✗ 邮件发送失败: {error_info['message']}")
            return None
            
    except requests.RequestException as e:
        print(f"✗ 网络错误: {e}")
        return None

# 使用示例
if __name__ == "__main__":
    
    print("=== 测试1: 完整功能 ===")
    send_email(
        mail_to="xingyun1982314@126.com",
        mail_cc="xingyun@murata.com",
        mail_title="这里是邮件标题",
        mail_content="这里是邮件内容"
    )
    
    print("\n=== 测试2: 仅收件人 ===")
    send_email(
        mail_to="xingyun1982314@126.com",
        mail_title="测试邮件",
        mail_content="测试内容"
    )
    
    print("\n=== 测试3: 带附件 ===")
    send_email(
        mail_to="xingyun1982314@126.com",
        mail_cc="xingyun@murata.com",
        download_url="http://localhost:8080/api/download/report.xlsx",
        mail_title="报告已完成",
        mail_content="请查看附件中的报告"
    )
```

### cURL 测试

```bash
# 1. 您的需求示例
curl -X POST "http://localhost:8080/api/email/send" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{
       "mailContent": "这里是邮件内容",
       "mailTitle": "这里是邮件标题",
       "mailTo": "xingyun1982314@126.com",
       "mailCc": "xingyun@murata.com"
     }'

# 2. 仅收件人
curl -X POST "http://localhost:8080/api/email/send" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{
       "mailTo": "xingyun1982314@126.com",
       "mailTitle": "测试邮件",
       "mailContent": "测试内容"
     }'

# 3. 带附件
curl -X POST "http://localhost:8080/api/email/send" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{
       "download_url": "http://localhost:8080/api/download/report.xlsx",
       "mailTo": "xingyun1982314@126.com",
       "mailCc": "xingyun@murata.com",
       "mailTitle": "报告已完成",
       "mailContent": "请查看附件中的报告"
     }'

# 4. 使用默认内容
curl -X POST "http://localhost:8080/api/email/send" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{
       "formName": "员工信息表",
       "formStatus": "Completed",
       "mailTo": "xingyun1982314@126.com"
     }'
```

## 📊 响应格式

### 成功响应示例

#### 带抄送的邮件
```json
{
    "success": true,
    "message": "邮件发送成功",
    "emailSubject": "这里是邮件标题",
    "recipients": [
        "xingyun1982314@126.com",
        "xingyun@murata.com (CC)"
    ],
    "attachmentName": null,
    "timestamp": "2025-06-09T12:00:00.123456",
    "errorCode": null
}
```

#### 无抄送的邮件
```json
{
    "success": true,
    "message": "邮件发送成功",
    "emailSubject": "测试邮件标题",
    "recipients": [
        "xingyun1982314@126.com"
    ],
    "attachmentName": null,
    "timestamp": "2025-06-09T12:00:00.123456",
    "errorCode": null
}
```

### 失败响应示例

#### 缺少必填字段
```json
{
    "success": false,
    "message": "邮件发送失败: 参数验证失败",
    "emailSubject": null,
    "recipients": null,
    "attachmentName": null,
    "timestamp": "2025-06-09T12:00:00.123456",
    "errorCode": "VALIDATION_ERROR"
}
```

## 🔄 参数使用场景

### 场景1: 完整功能邮件（您的需求）
```json
{
    "mailContent": "这里是邮件内容",
    "mailTitle": "这里是邮件标题",
    "mailTo": "xingyun1982314@126.com",
    "mailCc": "xingyun@murata.com"
}
```
**结果**: 发送自定义内容的邮件，同时发送给收件人和抄送人

### 场景2: 简单通知邮件
```json
{
    "mailTo": "xingyun1982314@126.com",
    "mailTitle": "系统通知",
    "mailContent": "这是一个系统通知"
}
```
**结果**: 发送简单通知邮件，无抄送，无附件

### 场景3: 带附件的正式邮件
```json
{
    "download_url": "http://localhost:8080/api/download/report.xlsx",
    "mailTo": "xingyun1982314@126.com",
    "mailCc": "xingyun@murata.com",
    "mailTitle": "月度报告",
    "mailContent": "请查看附件中的月度报告"
}
```
**结果**: 发送带附件的正式邮件

### 场景4: 使用默认模板
```json
{
    "formName": "员工信息表",
    "formStatus": "Completed",
    "mailTo": "xingyun1982314@126.com"
}
```
**结果**: 使用表单信息生成默认标题和内容

## 🛠️ 参数验证规则

1. **mailTo**: 必填，必须是有效的邮箱格式
2. **mailCc**: 可选，如果提供必须是有效的邮箱格式
3. **mailTitle**: 可选，如果提供则直接使用，否则使用默认模板
4. **mailContent**: 可选，如果提供则直接使用，否则使用默认模板
5. **其他字段**: 都是可选的

## 🎯 更新要点总结

### ✅ 新增功能 (v3.0)
1. **✨ 自定义收件人**: mailTo 必填字段，支持任意邮箱
2. **📧 可选抄送**: mailCc 可选字段，支持邮件抄送
3. **🎯 精确控制**: 不再依赖配置文件中的收件人列表
4. **📝 自定义内容**: mailTitle 和 mailContent 直接使用
5. **✅ 邮箱验证**: 自动验证邮箱地址格式

### ✅ 向后兼容性
- 支持所有之前的参数（download_url, formName, formStatus）
- 默认模板机制保持不变
- 附件功能完全兼容

### ✅ 灵活性增强
- 可以发送给任意收件人，不限于配置文件
- 支持可选抄送功能
- 完全自定义邮件内容和标题
- 智能默认值处理

## 📝 完整的PowerShell测试脚本

```powershell
# 邮件发送API完整测试脚本
function Send-CustomEmail {
    param(
        [Parameter(Mandatory)]
        [string]$MailTo,
        [string]$MailCc = $null,
        [string]$MailTitle = $null,
        [string]$MailContent = $null,
        [string]$DownloadUrl = $null,
        [string]$FormName = $null,
        [string]$FormStatus = $null
    )
    
    # 构建请求体
    $requestBody = @{
        mailTo = $MailTo
    }
    
    # 添加可选参数
    if ($MailCc) { $requestBody.mailCc = $MailCc }
    if ($MailTitle) { $requestBody.mailTitle = $MailTitle }
    if ($MailContent) { $requestBody.mailContent = $MailContent }
    if ($DownloadUrl) { $requestBody.download_url = $DownloadUrl }
    if ($FormName) { $requestBody.formName = $FormName }
    if ($FormStatus) { $requestBody.formStatus = $FormStatus }
    
    $body = $requestBody | ConvertTo-Json -Depth 10
    $utf8 = [System.Text.Encoding]::UTF8.GetBytes($body)
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/email/send" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
        
        Write-Host "✓ 邮件发送成功" -ForegroundColor Green
        Write-Host "  主题: $($response.emailSubject)"
        Write-Host "  收件人: $($response.recipients -join ', ')"
        if ($response.attachmentName) {
            Write-Host "  附件: $($response.attachmentName)"
        }
        return $response
    } catch {
        Write-Host "✗ 邮件发送失败: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# 测试示例
Write-Host "=== 测试1: 完整功能 ===" -ForegroundColor Cyan
Send-CustomEmail -MailTo "xingyun1982314@126.com" `
                 -MailCc "xingyun@murata.com" `
                 -MailTitle "这里是邮件标题" `
                 -MailContent "这里是邮件内容"

Write-Host "`n=== 测试2: 仅收件人 ===" -ForegroundColor Cyan
Send-CustomEmail -MailTo "xingyun1982314@126.com" `
                 -MailTitle "测试邮件" `
                 -MailContent "测试内容"

Write-Host "`n=== 测试3: 带附件 ===" -ForegroundColor Cyan
Send-CustomEmail -MailTo "xingyun1982314@126.com" `
                 -MailCc "xingyun@murata.com" `
                 -DownloadUrl "http://localhost:8080/api/download/report.xlsx" `
                 -MailTitle "报告已完成" `
                 -MailContent "请查看附件"

Write-Host "`n=== 测试4: 默认内容 ===" -ForegroundColor Cyan
Send-CustomEmail -MailTo "xingyun1982314@126.com" `
                 -FormName "员工信息表" `
                 -FormStatus "Completed"
```

## 🎉 终极邮件发送功能！

**状态**: ✅ **完美实现**

您的邮件发送API现在具备：
- 🎯 **精确收件人控制** - mailTo 必填，支持任意邮箱
- 📧 **灵活抄送功能** - mailCc 可选，按需添加抄送
- 📝 **完全自定义内容** - mailTitle 和 mailContent 直接生效
- 📎 **可选附件支持** - download_url 可选，灵活添加附件
- 🔄 **完美向后兼容** - 保持所有原有功能
- ✅ **智能验证** - 自动验证邮箱格式

现在您可以完全按照您的需求发送邮件了！🎉 