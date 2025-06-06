package com.formfill.api.service;

import com.formfill.api.dto.EmailConfig;
import com.formfill.api.dto.EmailSendRequest;
import com.formfill.api.dto.EmailSendResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.UnsupportedEncodingException;

/**
 * 邮件发送服务
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailConfig emailConfig;
    
    private final WebClient webClient;
    
    public EmailService() {
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * 发送带附件的邮件
     */
    public EmailSendResponse sendEmailWithAttachment(EmailSendRequest request) {
        try {
            logger.info("开始发送邮件，表单名称: {}, 下载链接: {}", request.getFormName(), request.getDownloadUrl());
            
            // 1. 下载附件
            byte[] attachmentData = downloadFile(request.getDownloadUrl());
            String fileName = extractFileNameFromUrl(request.getDownloadUrl());
            
            // 2. 生成邮件内容
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String subject = generateEmailSubject(request.getFormName(), timestamp);
            String content = generateEmailContent(request.getFormName(), request.getFormStatus(), timestamp);
            
            // 3. 发送邮件
            sendMail(emailConfig.getRecipients(), subject, content, fileName, attachmentData);
            
            logger.info("邮件发送成功，收件人: {}, 主题: {}", emailConfig.getRecipients(), subject);
            
            return EmailSendResponse.success(
                "邮件发送成功",
                subject,
                emailConfig.getRecipients(),
                fileName
            );
            
        } catch (Exception e) {
            logger.error("邮件发送失败: {}", e.getMessage(), e);
            return EmailSendResponse.failure(
                "邮件发送失败: " + e.getMessage(),
                "EMAIL_SEND_ERROR"
            );
        }
    }
    
    /**
     * 从URL下载文件
     */
    private byte[] downloadFile(String downloadUrl) {
        try {
            logger.info("正在下载文件: {}", downloadUrl);
            
            Mono<byte[]> mono = webClient.get()
                    .uri(downloadUrl)
                    .retrieve()
                    .bodyToMono(byte[].class);
            
            byte[] data = mono.block();
            
            if (data == null || data.length == 0) {
                throw new RuntimeException("下载的文件为空");
            }
            
            logger.info("文件下载成功，大小: {} bytes", data.length);
            return data;
            
        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String url) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            return fileName.isEmpty() ? "attachment.xlsx" : fileName;
        } catch (Exception e) {
            logger.warn("无法从URL提取文件名: {}", url);
            return "attachment.xlsx";
        }
    }
    
    /**
     * 生成邮件主题
     */
    private String generateEmailSubject(String formName, String timestamp) {
        String template = emailConfig.getTemplate().getSubject();
        return template
                .replace("${formName}", formName)
                .replace("${timestamp}", timestamp);
    }
    
    /**
     * 生成邮件内容
     */
    private String generateEmailContent(String formName, String formStatus, String timestamp) {
        String template = emailConfig.getTemplate().getContent();
        return template
                .replace("${formName}", formName)
                .replace("${formStatus}", getFormStatusDescription(formStatus))
                .replace("${timestamp}", timestamp);
    }
    
    /**
     * 获取表单状态描述
     */
    private String getFormStatusDescription(String formStatus) {
        switch (formStatus) {
            case "RPAProcess":
                return "RPA自动化处理";
            case "ManualProcess":
                return "人工处理";
            case "Completed":
                return "已完成";
            case "Failed":
                return "处理失败";
            default:
                return formStatus;
        }
    }
    
    /**
     * 发送邮件
     */
    private void sendMail(List<String> recipients, String subject, String content, 
                         String fileName, byte[] attachmentData) throws MessagingException, UnsupportedEncodingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        // 设置发件人 - 使用配置的邮箱地址
        String fromName = emailConfig.getFromName();
        if (fromName != null && !fromName.isEmpty()) {
            // 格式: "显示名称 <邮箱地址>"
            helper.setFrom("xingyun1982314@126.com", fromName);
        } else {
            // 直接使用邮箱地址
            helper.setFrom("xingyun1982314@126.com");
        }
        
        // 设置收件人
        helper.setTo(recipients.toArray(new String[0]));
        
        // 设置邮件主题和内容
        helper.setSubject(subject);
        helper.setText(content, false); // false表示纯文本格式
        
        // 添加附件
        if (attachmentData != null && attachmentData.length > 0) {
            ByteArrayResource attachment = new ByteArrayResource(attachmentData);
            helper.addAttachment(fileName, attachment);
        }
        
        // 发送邮件
        mailSender.send(message);
    }
} 