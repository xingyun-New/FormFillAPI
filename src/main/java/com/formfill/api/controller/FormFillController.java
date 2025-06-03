package com.formfill.api.controller;

import com.formfill.api.dto.FormFillRequest;
import com.formfill.api.service.FormFillerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Form Fill API Controller
 */
@RestController
@RequestMapping("/api")
public class FormFillController {
    
    private static final Logger logger = LoggerFactory.getLogger(FormFillController.class);
    
    @Autowired
    private FormFillerService formFillerService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "FormFillAPI");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get form template URL by form name (POST with JSON)
     * 
     * @param request 包含表单名称的JSON请求
     * @return 返回模板信息和下载URL
     */
    @PostMapping("/template")
    public ResponseEntity<Map<String, Object>> getTemplateByNamePost(@RequestBody Map<String, String> request) {
        try {
            String formName = request.get("formName");
            
            if (formName == null || formName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "formName parameter is required");
                errorResponse.put("code", "MISSING_FORM_NAME");
                errorResponse.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            logger.info("Getting template for form: {}", formName);
            
            // 使用服务层方法查找模板
            Map<String, Object> templateInfo = formFillerService.getTemplateInfo(formName);
            
            if (templateInfo.get("found").equals(true)) {
                String templatePath = (String) templateInfo.get("templatePath");
                String fileName = (String) templateInfo.get("fileName");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("formName", formName);
                response.put("templateFound", true);
                response.put("fileName", fileName);
                response.put("templatePath", templatePath);
                response.put("downloadUrl", "/api/template/download/" + URLEncoder.encode(fileName, "UTF-8"));
                response.put("previewUrl", "/api/template/preview/" + URLEncoder.encode(fileName, "UTF-8"));
                response.put("downloadUrlRaw", "/api/template/download/" + fileName);
                response.put("previewUrlRaw", "/api/template/preview/" + fileName);
                response.put("timestamp", LocalDateTime.now().toString());
                
                logger.info("Template found for form '{}': {}", formName, fileName);
                return ResponseEntity.ok(response);
                
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("formName", formName);
                response.put("templateFound", false);
                response.put("message", "Template not found for form: " + formName);
                response.put("suggestion", "Please check if the template file exists in the templates directory");
                response.put("timestamp", LocalDateTime.now().toString());
                
                logger.warn("Template not found for form: {}", formName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error getting template: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get template: " + e.getMessage());
            errorResponse.put("code", "TEMPLATE_GET_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get form template URL by form name
     * 
     * @param formName 表单名称
     * @return 返回模板信息和下载URL
     */
    @GetMapping("/template/{formName}")
    public ResponseEntity<Map<String, Object>> getTemplateByName(@PathVariable String formName) {
        try {
            logger.info("Getting template for form: {}", formName);
            
            // 使用服务层方法查找模板
            Map<String, Object> templateInfo = formFillerService.getTemplateInfo(formName);
            
            if (templateInfo.get("found").equals(true)) {
                String templatePath = (String) templateInfo.get("templatePath");
                String fileName = (String) templateInfo.get("fileName");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("formName", formName);
                response.put("templateFound", true);
                response.put("fileName", fileName);
                response.put("templatePath", templatePath);
                response.put("downloadUrl", "/api/template/download/" + URLEncoder.encode(fileName, "UTF-8"));
                response.put("previewUrl", "/api/template/preview/" + URLEncoder.encode(fileName, "UTF-8"));
                response.put("downloadUrlRaw", "/api/template/download/" + fileName);
                response.put("previewUrlRaw", "/api/template/preview/" + fileName);
                response.put("timestamp", LocalDateTime.now().toString());
                
                logger.info("Template found for form '{}': {}", formName, fileName);
                return ResponseEntity.ok(response);
                
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("formName", formName);
                response.put("templateFound", false);
                response.put("message", "Template not found for form: " + formName);
                response.put("suggestion", "Please check if the template file exists in the templates directory");
                response.put("timestamp", LocalDateTime.now().toString());
                
                logger.warn("Template not found for form: {}", formName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error getting template for form '{}': {}", formName, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("formName", formName);
            errorResponse.put("error", "Failed to get template: " + e.getMessage());
            errorResponse.put("code", "TEMPLATE_GET_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Download template file by filename
     * 
     * @param filename 模板文件名
     * @return 模板文件下载响应
     */
    @GetMapping("/template/download/{filename}")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable String filename) {
        try {
            logger.info("Downloading template: {}", filename);
            
            Path filePath = Paths.get("templates").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                logger.warn("Template file not found: {}", filename);
                return ResponseEntity.notFound().build();
            }
            
            // URL encode the filename to handle Chinese characters
            String encodedFilename;
            try {
                encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                encodedFilename = filename;
                logger.warn("Failed to encode filename: {}", filename);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Error downloading template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Preview template file information (without downloading)
     * 
     * @param filename 模板文件名
     * @return 模板文件信息
     */
    @GetMapping("/template/preview/{filename}")
    public ResponseEntity<Map<String, Object>> previewTemplate(@PathVariable String filename) {
        try {
            logger.info("Previewing template: {}", filename);
            
            Path filePath = Paths.get("templates").resolve(filename);
            File templateFile = filePath.toFile();
            
            if (!templateFile.exists()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Template file not found: " + filename);
                errorResponse.put("code", "TEMPLATE_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileName", filename);
            response.put("filePath", templateFile.getAbsolutePath());
            response.put("fileSize", templateFile.length());
            response.put("lastModified", templateFile.lastModified());
            response.put("readable", templateFile.canRead());
            response.put("downloadUrl", "/api/template/download/" + URLEncoder.encode(filename, "UTF-8"));
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Template preview generated for: {}", filename);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error previewing template: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to preview template: " + e.getMessage());
            errorResponse.put("code", "TEMPLATE_PREVIEW_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Main API endpoint for form filling
     * 
     * Request format:
     * {
     *     "formName": "EmployeeForm",
     *     "formContent": {
     *         "[3, 2]": ["John"],
     *         "[4, 2]": ["Male"]
     *     }
     * }
     */
    @PostMapping("/fill-form")
    public ResponseEntity<Map<String, Object>> fillForm(@Valid @RequestBody FormFillRequest request) {
        try {
            logger.info("Processing form: {}", request.getFormName());
            logger.info("Form content: {}", request.getFormContent());
            
            // Fill form
            Map<String, Object> result = formFillerService.fillForm(
                request.getFormName(), 
                request.getFormContent()
            );
            
            if ((Boolean) result.get("success")) {
                logger.info("Form filled successfully: {}", result.get("output_file"));
                
                // Build response
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Form filled successfully");
                response.put("output_file", result.get("output_file"));
                
                // Generate download URL
                String outputFile = (String) result.get("output_file");
                String fileName = Paths.get(outputFile).getFileName().toString();
                response.put("download_url", "/api/download/" + fileName);
                
                response.put("filled_count", result.get("filled_count"));
                response.put("total_fields", result.get("total_fields"));
                response.put("template_used", result.get("template_used"));
                response.put("timestamp", LocalDateTime.now().toString());
                
                return ResponseEntity.ok(response);
            } else {
                logger.error("Form filling failed: {}", result.get("error"));
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", result.get("error"));
                errorResponse.put("code", result.get("code"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Error processing request: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            errorResponse.put("code", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Download completed form file
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("output").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "File not found");
                errorResponse.put("code", "FILE_NOT_FOUND");
                return ResponseEntity.notFound().build();
            }
            
            // URL encode the filename to handle Chinese characters
            String encodedFilename;
            try {
                encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                encodedFilename = filename;
                logger.warn("Failed to encode filename: {}", filename);
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Error downloading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * List all available form templates
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> listTemplates() {
        try {
            File templatesDir = new File("templates");
            List<Map<String, Object>> templates = new ArrayList<>();
            
            if (templatesDir.exists() && templatesDir.isDirectory()) {
                File[] files = templatesDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls")) {
                            Map<String, Object> template = new HashMap<>();
                            String fileName = file.getName();
                            String baseName = fileName.replaceAll("\\.(xlsx|xls)$", "");
                            
                            template.put("name", baseName);
                            template.put("filename", fileName);
                            template.put("path", file.getAbsolutePath());
                            template.put("downloadUrl", "/api/template/download/" + fileName);
                            template.put("previewUrl", "/api/template/preview/" + fileName);
                            template.put("getTemplateUrl", "/api/template/" + baseName);
                            templates.add(template);
                        }
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("templates", templates);
            response.put("count", templates.size());
            
            if (templates.isEmpty()) {
                response.put("message", "Templates directory is empty or not exists");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting template list: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get template list: " + e.getMessage());
            errorResponse.put("code", "TEMPLATE_LIST_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 