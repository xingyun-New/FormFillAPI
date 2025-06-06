package com.formfill.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 邮件发送请求DTO
 */
public class EmailSendRequest {
    
    @NotBlank(message = "下载链接不能为空")
    @Pattern(regexp = "^(https?://.*|/api/download/.*)$", message = "下载链接格式不正确，应为HTTP/HTTPS URL或本地路径")
    @JsonProperty("download_url")
    private String downloadUrl;
    
    @NotBlank(message = "表单名称不能为空")
    private String formName;
    
    @NotBlank(message = "表单状态不能为空")
    private String formStatus;
    
    public EmailSendRequest() {}
    
    public EmailSendRequest(String downloadUrl, String formName, String formStatus) {
        this.downloadUrl = downloadUrl;
        this.formName = formName;
        this.formStatus = formStatus;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getFormName() {
        return formName;
    }
    
    public void setFormName(String formName) {
        this.formName = formName;
    }
    
    public String getFormStatus() {
        return formStatus;
    }
    
    public void setFormStatus(String formStatus) {
        this.formStatus = formStatus;
    }
    
    @Override
    public String toString() {
        return "EmailSendRequest{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", formName='" + formName + '\'' +
                ", formStatus='" + formStatus + '\'' +
                '}';
    }
} 