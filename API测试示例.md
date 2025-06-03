# FormFillAPI 测试示例

## 新增API功能测试

### 1. 根据表单名称获取模板URL

#### 测试场景1: 查询存在的模板

```bash
# 请求
curl -X GET "http://localhost:8080/api/template/员工信息表"

# 预期响应
{
    "success": true,
    "formName": "员工信息表",
    "templateFound": true,
    "fileName": "员工信息表.xlsx",
    "templatePath": "/path/to/templates/员工信息表.xlsx",
    "downloadUrl": "/api/template/download/员工信息表.xlsx",
    "previewUrl": "/api/template/preview/员工信息表.xlsx",
    "timestamp": "2024-06-01T22:30:00"
}
```

#### 测试场景2: 查询不存在的模板

```bash
# 请求
curl -X GET "http://localhost:8080/api/template/不存在的表单"

# 预期响应 (HTTP 404)
{
    "success": false,
    "formName": "不存在的表单",
    "templateFound": false,
    "message": "Template not found for form: 不存在的表单",
    "suggestion": "Please check if the template file exists in the templates directory",
    "timestamp": "2024-06-01T22:30:00"
}
```

### 2. 下载模板文件

```bash
# 请求
curl -X GET "http://localhost:8080/api/template/download/员工信息表.xlsx" \
     -H "Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" \
     --output "downloaded_template.xlsx"

# 预期结果: 下载Excel文件到本地
```

### 3. 预览模板文件信息

```bash
# 请求
curl -X GET "http://localhost:8080/api/template/preview/员工信息表.xlsx"

# 预期响应
{
    "success": true,
    "fileName": "员工信息表.xlsx",
    "filePath": "/path/to/templates/员工信息表.xlsx",
    "fileSize": 12345,
    "lastModified": 1640995200000,
    "readable": true,
    "downloadUrl": "/api/template/download/员工信息表.xlsx",
    "timestamp": "2024-06-01T22:30:00"
}
```

### 4. 获取所有模板列表 (增强版)

```bash
# 请求
curl -X GET "http://localhost:8080/api/templates"

# 预期响应 (现在包含更多URL信息)
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

## 完整的表单处理流程示例

### 步骤1: 查询可用模板

```bash
curl -X GET "http://localhost:8080/api/templates"
```

### 步骤2: 根据表单名称获取具体模板信息

```bash
curl -X GET "http://localhost:8080/api/template/员工信息表"
```

### 步骤3: (可选) 下载模板查看结构

```bash
curl -X GET "http://localhost:8080/api/template/download/员工信息表.xlsx" \
     --output "template.xlsx"
```

### 步骤4: 填写表单

```bash
curl -X POST "http://localhost:8080/api/fill-form" \
     -H "Content-Type: application/json" \
     -d '{
         "formName": "员工信息表",
         "formContent": {
             "[3, 2]": ["张三"],
             "[4, 2]": ["男"],
             "[5, 2]": ["28"]
         }
     }'
```

### 步骤5: 下载填写后的表单

```bash
curl -X GET "http://localhost:8080/api/download/员工信息表_filled_20240601_223000.xlsx" \
     --output "filled_form.xlsx"
```

## JavaScript 客户端示例

```javascript
class FormFillAPIClient {
    constructor(baseUrl = 'http://localhost:8080/api') {
        this.baseUrl = baseUrl;
    }

    // 根据表单名称获取模板信息
    async getTemplateInfo(formName) {
        const response = await fetch(`${this.baseUrl}/template/${encodeURIComponent(formName)}`);
        return await response.json();
    }

    // 获取所有模板列表
    async getAllTemplates() {
        const response = await fetch(`${this.baseUrl}/templates`);
        return await response.json();
    }

    // 下载模板文件
    async downloadTemplate(fileName) {
        const response = await fetch(`${this.baseUrl}/template/download/${encodeURIComponent(fileName)}`);
        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } else {
            throw new Error('下载失败');
        }
    }

    // 填写表单
    async fillForm(formName, formContent) {
        const response = await fetch(`${this.baseUrl}/fill-form`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                formName: formName,
                formContent: formContent
            })
        });
        return await response.json();
    }
}

// 使用示例
const api = new FormFillAPIClient();

// 查询模板
api.getTemplateInfo('员工信息表').then(result => {
    console.log('模板信息:', result);
    if (result.templateFound) {
        console.log('下载URL:', result.downloadUrl);
    }
});

// 获取所有模板
api.getAllTemplates().then(result => {
    console.log('所有模板:', result.templates);
});
```

## Python 客户端示例

```python
import requests
import json

class FormFillAPIClient:
    def __init__(self, base_url='http://localhost:8080/api'):
        self.base_url = base_url

    def get_template_info(self, form_name):
        """根据表单名称获取模板信息"""
        url = f"{self.base_url}/template/{form_name}"
        response = requests.get(url)
        return response.json()

    def get_all_templates(self):
        """获取所有模板列表"""
        url = f"{self.base_url}/templates"
        response = requests.get(url)
        return response.json()

    def download_template(self, file_name, save_path=None):
        """下载模板文件"""
        url = f"{self.base_url}/template/download/{file_name}"
        response = requests.get(url)
        
        if response.status_code == 200:
            file_path = save_path or file_name
            with open(file_path, 'wb') as f:
                f.write(response.content)
            print(f"模板已下载到: {file_path}")
        else:
            print(f"下载失败: {response.status_code}")

    def fill_form(self, form_name, form_content):
        """填写表单"""
        url = f"{self.base_url}/fill-form"
        data = {
            "formName": form_name,
            "formContent": form_content
        }
        response = requests.post(url, json=data)
        return response.json()

# 使用示例
if __name__ == "__main__":
    api = FormFillAPIClient()
    
    # 查询模板信息
    result = api.get_template_info('员工信息表')
    print("模板信息:", json.dumps(result, indent=2, ensure_ascii=False))
    
    # 获取所有模板
    templates = api.get_all_templates()
    print("所有模板:", json.dumps(templates, indent=2, ensure_ascii=False))
    
    # 如果模板存在，下载它
    if result.get('templateFound'):
        api.download_template(result['fileName'])
```

## 错误处理说明

### 常见错误码

- `TEMPLATE_NOT_FOUND`: 模板文件不存在
- `TEMPLATE_GET_ERROR`: 获取模板信息时发生错误
- `TEMPLATE_PREVIEW_ERROR`: 预览模板时发生错误
- `INTERNAL_ERROR`: 服务器内部错误

### 错误响应格式

```json
{
    "success": false,
    "error": "错误描述信息",
    "code": "错误代码",
    "timestamp": "2024-06-01T22:30:00"
}
```

## API速率限制

目前没有实施速率限制，但建议：
- 避免频繁轮询
- 批量操作时适当延迟
- 大文件下载时考虑使用断点续传

## 安全注意事项

- 文件名会进行URL编码处理
- 仅支持`.xlsx`和`.xls`格式
- 文件路径经过安全验证，防止目录遍历攻击 