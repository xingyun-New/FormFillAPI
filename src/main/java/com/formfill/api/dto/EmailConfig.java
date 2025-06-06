package com.formfill.api.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件配置类
 */
@Component
@ConfigurationProperties(prefix = "email")
public class EmailConfig {
    
    private Template template;
    private List<String> recipients;
    private String fromName;
    
    public Template getTemplate() {
        return template;
    }
    
    public void setTemplate(Template template) {
        this.template = template;
    }
    
    public List<String> getRecipients() {
        return recipients;
    }
    
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
    
    public String getFromName() {
        return fromName;
    }
    
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
    
    public static class Template {
        private String subject;
        private String content;
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
} 