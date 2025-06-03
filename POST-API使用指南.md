# FormFillAPI POST 方法使用指南

## 🎯 新增功能

根据您的需求，已成功添加了 **POST方式查询模板信息** 的功能。现在您可以通过JSON数据传递表单名称来查询模板信息。

## 📋 API端点详情

### POST /api/template/

**功能**: 根据JSON中的表单名称返回模板信息和下载URL

**请求方式**: POST  
**Content-Type**: application/json; charset=utf-8  
**请求体格式**:
```json
{
    "formName": "表单名称"
}
```

## 🧪 测试示例

### PowerShell 测试

#### 1. 查询存在的模板
```powershell
# 方法1: 使用UTF-8编码（推荐）
$utf8 = [System.Text.Encoding]::UTF8.GetBytes('{"formName":"员工信息表"}')
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
$response

# 方法2: 简单字符串（可能有编码问题）
$body = '{"formName":"员工信息表"}'
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $body -ContentType "application/json"
```

**成功响应示例:**
```json
{
    "success": true,
    "formName": "员工信息表",
    "templateFound": true,
    "fileName": "员工信息表.xlsx",
    "templatePath": "D:\\0.py\\FormFillAPI\\templates\\员工信息表.xlsx",
    "downloadUrl": "/api/template/download/员工信息表.xlsx",
    "previewUrl": "/api/template/preview/员工信息表.xlsx",
    "timestamp": "2025-06-01T23:36:07.272503200"
}
```

#### 2. 查询不存在的模板
```powershell
$utf8 = [System.Text.Encoding]::UTF8.GetBytes('{"formName":"不存在的表单"}')
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
} catch {
    Write-Host "状态码: $($_.Exception.Response.StatusCode)"
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "错误信息: $($errorDetails.message)"
    }
}
```

**404错误响应示例:**
```json
{
    "success": false,
    "formName": "不存在的表单",
    "templateFound": false,
    "message": "Template not found for form: 不存在的表单",
    "suggestion": "Please check if the template file exists in the templates directory",
    "timestamp": "2025-06-01T23:36:07.272503200"
}
```

#### 3. 缺少formName参数
```powershell
$utf8 = [System.Text.Encoding]::UTF8.GetBytes('{}')
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
} catch {
    Write-Host "状态码: $($_.Exception.Response.StatusCode)"
}
```

**400错误响应示例:**
```json
{
    "success": false,
    "error": "formName parameter is required",
    "code": "MISSING_FORM_NAME",
    "timestamp": "2025-06-01T23:36:07.272503200"
}
```

### JavaScript 测试

```javascript
// 使用fetch API
async function getTemplateInfo(formName) {
    try {
        const response = await fetch('http://localhost:8080/api/template', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=utf-8'
            },
            body: JSON.stringify({
                formName: formName
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            console.log('模板信息:', data);
            return data;
        } else {
            console.error('查询失败:', data.message);
            return null;
        }
    } catch (error) {
        console.error('请求错误:', error);
        return null;
    }
}

// 使用示例
getTemplateInfo('员工信息表').then(result => {
    if (result && result.templateFound) {
        console.log('下载URL:', result.downloadUrl);
        console.log('预览URL:', result.previewUrl);
    }
});
```

### Python 测试

```python
import requests
import json

def get_template_info(form_name):
    """使用POST方法查询模板信息"""
    url = "http://localhost:8080/api/template"
    
    headers = {
        'Content-Type': 'application/json; charset=utf-8'
    }
    
    data = {
        "formName": form_name
    }
    
    try:
        response = requests.post(url, json=data, headers=headers)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✓ 查询成功: {result['fileName']}")
            print(f"  下载URL: {result['downloadUrl']}")
            print(f"  预览URL: {result['previewUrl']}")
            return result
        elif response.status_code == 404:
            error_info = response.json()
            print(f"✗ 模板未找到: {error_info['message']}")
            return None
        elif response.status_code == 400:
            error_info = response.json()
            print(f"✗ 请求错误: {error_info['error']}")
            return None
        else:
            print(f"✗ 意外错误: {response.status_code}")
            return None
            
    except requests.RequestException as e:
        print(f"✗ 网络错误: {e}")
        return None

# 使用示例
if __name__ == "__main__":
    # 测试存在的模板
    result = get_template_info("员工信息表")
    
    # 测试不存在的模板
    get_template_info("不存在的表单")
    
    # 测试空参数
    get_template_info("")
```

### cURL 测试

```bash
# 查询存在的模板
curl -X POST "http://localhost:8080/api/template" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{"formName":"员工信息表"}'

# 查询不存在的模板
curl -X POST "http://localhost:8080/api/template" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{"formName":"不存在的表单"}'

# 缺少参数测试
curl -X POST "http://localhost:8080/api/template" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d '{}'
```

## 🔄 与GET方法的对比

### GET方法 (仍然支持)
```http
GET /api/template/{formName}
```

### POST方法 (新增)
```http
POST /api/template/
Content-Type: application/json; charset=utf-8

{"formName": "表单名称"}
```

**两种方法的区别:**

| 特性 | GET方法 | POST方法 |
|------|---------|----------|
| 参数传递 | URL路径参数 | JSON请求体 |
| 中文支持 | URL编码 | UTF-8编码 |
| 缓存 | 可能被缓存 | 不会被缓存 |
| 安全性 | 参数在URL中可见 | 参数在请求体中 |
| RESTful | 更符合查询语义 | 适合复杂参数 |

## 📊 状态码说明

| 状态码 | 含义 | 响应内容 |
|--------|------|----------|
| 200 | 成功找到模板 | 包含完整模板信息的JSON |
| 400 | 请求参数错误 | 错误信息和错误代码 |
| 404 | 模板未找到 | 错误信息和建议 |
| 500 | 服务器内部错误 | 错误信息和错误代码 |

## 🛠️ 字符编码注意事项

### PowerShell 中的中文处理

**推荐方式** (UTF-8编码):
```powershell
$utf8 = [System.Text.Encoding]::UTF8.GetBytes('{"formName":"员工信息表"}')
Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
```

**简单方式** (可能有编码问题):
```powershell
$body = '{"formName":"员工信息表"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $body -ContentType "application/json"
```

### 其他语言的处理

- **JavaScript**: 自动处理UTF-8编码
- **Python**: requests库自动处理UTF-8编码
- **cURL**: 确保终端支持UTF-8编码

## 🎯 使用建议

1. **开发调试**: 使用GET方法，简单直接
2. **生产环境**: 使用POST方法，更安全和灵活
3. **中文表单**: 优先使用POST方法，避免URL编码问题
4. **批量查询**: 可以扩展POST方法支持多个表单名称

## 📝 完整的PowerShell测试脚本

```powershell
# 完整的POST API测试脚本
function Test-PostAPI {
    param([string]$FormName)
    
    try {
        $utf8 = [System.Text.Encoding]::UTF8.GetBytes("{`"formName`":`"$FormName`"}")
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/template" -Method POST -Body $utf8 -ContentType "application/json; charset=utf-8"
        
        Write-Host "✓ 查询成功: $FormName" -ForegroundColor Green
        Write-Host "  文件名: $($response.fileName)"
        Write-Host "  下载URL: $($response.downloadUrl)"
        return $response
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "✗ 模板未找到: $FormName" -ForegroundColor Yellow
        } elseif ($_.Exception.Response.StatusCode -eq 400) {
            Write-Host "✗ 请求参数错误: $FormName" -ForegroundColor Red
        } else {
            Write-Host "✗ 未知错误: $($_.Exception.Message)" -ForegroundColor Red
        }
        return $null
    }
}

# 测试示例
Test-PostAPI -FormName "员工信息表"
Test-PostAPI -FormName "客户登记表"
Test-PostAPI -FormName "不存在的表单"
```

现在您可以使用POST方法来查询表单模板信息了！🎉 