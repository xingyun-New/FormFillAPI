package com.formfill.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件发送响应DTO
 */
public class EmailSendResponse {
    
    private boolean success;
    private String message;
    private String emailSubject;
    private List<String> recipients;
    private String attachmentName;
    private LocalDateTime timestamp;
    private String errorCode;
    
    public EmailSendResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public EmailSendResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public static EmailSendResponse success(String message, String emailSubject, 
                                          List<String> recipients, String attachmentName) {
        EmailSendResponse response = new EmailSendResponse(true, message);
        response.setEmailSubject(emailSubject);
        response.setRecipients(recipients);
        response.setAttachmentName(attachmentName);
        return response;
    }
    
    public static EmailSendResponse failure(String message, String errorCode) {
        EmailSendResponse response = new EmailSendResponse(false, message);
        response.setErrorCode(errorCode);
        return response;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getEmailSubject() {
        return emailSubject;
    }
    
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
    
    public List<String> getRecipients() {
        return recipients;
    }
    
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
    
    public String getAttachmentName() {
        return attachmentName;
    }
    
    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
} 