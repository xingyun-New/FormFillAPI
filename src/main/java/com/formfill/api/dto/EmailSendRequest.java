package com.formfill.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 邮件发送请求DTO
 */
public class EmailSendRequest {
    
    @Pattern(regexp = "^(https?://.*|/api/download/.*)$", message = "下载链接格式不正确，应为HTTP/HTTPS URL或本地路径")
    @JsonProperty("download_url")
    private String downloadUrl;
    
    private String formName;
    
    private String formStatus;
    
    private String mailContent;
    
    private String mailTitle;
    
    @NotBlank(message = "邮件收件人不能为空")
    @Email(message = "邮件收件人格式不正确")
    private String mailTo;
    
    @Email(message = "邮件抄送格式不正确")
    private String mailCc;
    
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
    
    public String getMailContent() {
        return mailContent;
    }
    
    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }
    
    public String getMailTitle() {
        return mailTitle;
    }
    
    public void setMailTitle(String mailTitle) {
        this.mailTitle = mailTitle;
    }
    
    public String getMailTo() {
        return mailTo;
    }
    
    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
    
    public String getMailCc() {
        return mailCc;
    }
    
    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }
    
    @Override
    public String toString() {
        return "EmailSendRequest{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", formName='" + formName + '\'' +
                ", formStatus='" + formStatus + '\'' +
                ", mailContent='" + mailContent + '\'' +
                ", mailTitle='" + mailTitle + '\'' +
                ", mailTo='" + mailTo + '\'' +
                ", mailCc='" + mailCc + '\'' +
                '}';
    }
} 