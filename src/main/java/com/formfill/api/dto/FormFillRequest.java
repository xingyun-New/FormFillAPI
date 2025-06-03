package com.formfill.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 表单填写请求DTO
 * 支持新的JSON格式：
 * {
 *   "formName": "员工信息表",
 *   "formContent": {
 *     "row3": {
 *       "col2": "aaaaa",
 *       "col3": "男"
 *     }
 *   }
 * }
 */
public class FormFillRequest {
    
    @NotBlank(message = "表单名称不能为空")
    private String formName;
    
    @NotNull(message = "表单内容不能为空")
    private Map<String, Map<String, String>> formContent;
    
    public FormFillRequest() {}
    
    public FormFillRequest(String formName, Map<String, Map<String, String>> formContent) {
        this.formName = formName;
        this.formContent = formContent;
    }
    
    public String getFormName() {
        return formName;
    }
    
    public void setFormName(String formName) {
        this.formName = formName;
    }
    
    public Map<String, Map<String, String>> getFormContent() {
        return formContent;
    }
    
    public void setFormContent(Map<String, Map<String, String>> formContent) {
        this.formContent = formContent;
    }
    
    @Override
    public String toString() {
        return "FormFillRequest{" +
                "formName='" + formName + '\'' +
                ", formContent=" + formContent +
                '}';
    }
} 