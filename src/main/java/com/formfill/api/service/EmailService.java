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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

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
    
    /**
     * 发送带附件的邮件
     */
    public EmailSendResponse sendEmailWithAttachment(EmailSendRequest request) {
        try {
            logger.info("开始发送邮件，表单名称: {}, 下载链接: {}, 收件人: {}", 
                request.getFormName(), request.getDownloadUrl(), request.getMailTo());
            
            byte[] attachmentData = null;
            String fileName = null;
            
            // 1. 如果有下载链接，则读取本地文件作为附件
            if (request.getDownloadUrl() != null && !request.getDownloadUrl().trim().isEmpty()) {
                attachmentData = readLocalFile(request.getDownloadUrl());
                fileName = extractFileNameFromPath(request.getDownloadUrl());
                logger.info("附件准备完成: {}", fileName);
            } else {
                logger.info("未提供下载链接，将发送不带附件的邮件");
            }
            
            // 2. 生成邮件内容
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 使用请求中的邮件标题，如果没有则使用默认模板
            String subject;
            if (request.getMailTitle() != null && !request.getMailTitle().trim().isEmpty()) {
                subject = request.getMailTitle();
            } else {
                // 如果没有自定义标题，使用formName生成默认标题，如果formName也为空，使用通用标题
                String formName = (request.getFormName() != null && !request.getFormName().trim().isEmpty()) 
                    ? request.getFormName() : "系统通知";
                subject = generateEmailSubject(formName, timestamp);
            }
            
            // 使用请求中的邮件内容，如果没有则使用默认模板
            String content;
            if (request.getMailContent() != null && !request.getMailContent().trim().isEmpty()) {
                content = request.getMailContent();
            } else {
                // 如果没有自定义内容，使用formName和formStatus生成默认内容
                String formName = (request.getFormName() != null && !request.getFormName().trim().isEmpty()) 
                    ? request.getFormName() : "系统通知";
                String formStatus = (request.getFormStatus() != null && !request.getFormStatus().trim().isEmpty()) 
                    ? request.getFormStatus() : "Completed";
                content = generateEmailContent(formName, formStatus, timestamp);
            }
            
            // 3. 准备收件人列表
            String[] recipients = {request.getMailTo()};
            String[] ccRecipients = null;
            if (request.getMailCc() != null && !request.getMailCc().trim().isEmpty()) {
                ccRecipients = new String[]{request.getMailCc()};
            }
            
            // 4. 发送邮件
            sendMail(recipients, ccRecipients, subject, content, fileName, attachmentData);
            
            logger.info("邮件发送成功，收件人: {}, 抄送: {}, 主题: {}", 
                request.getMailTo(), request.getMailCc(), subject);
            
            // 构建收件人列表用于响应
            java.util.List<String> recipientList = new java.util.ArrayList<>();
            recipientList.add(request.getMailTo());
            if (request.getMailCc() != null && !request.getMailCc().trim().isEmpty()) {
                recipientList.add(request.getMailCc() + " (CC)");
            }
            
            return EmailSendResponse.success(
                "邮件发送成功",
                subject,
                recipientList,
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
     * 从本地output目录读取文件
     */
    private byte[] readLocalFile(String downloadPath) {
        try {
            // 从路径中提取文件名
            String fileName = extractFileNameFromPath(downloadPath);
            
            // 构建完整的文件路径
            Path filePath = Paths.get("output", fileName);
            
            logger.info("正在读取本地文件: {}", filePath.toString());
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + filePath.toString());
            }
            
            // 读取文件内容
            byte[] data = Files.readAllBytes(filePath);
            
            if (data.length == 0) {
                throw new RuntimeException("文件为空: " + filePath.toString());
            }
            
            logger.info("文件读取成功，大小: {} bytes", data.length);
            return data;
            
        } catch (IOException e) {
            logger.error("文件读取失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }
    }
    
    /**
     * 从路径中提取文件名
     */
    private String extractFileNameFromPath(String path) {
        try {
            // 处理类似 "/api/download/文件名.xlsx" 的路径
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            return fileName.isEmpty() ? "attachment.xlsx" : fileName;
        } catch (Exception e) {
            logger.warn("无法从路径提取文件名: {}", path);
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
    private void sendMail(String[] recipients, String[] ccRecipients, String subject, String content, 
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
        helper.setTo(recipients);
        
        // 设置抄送人
        if (ccRecipients != null && ccRecipients.length > 0) {
            helper.setCc(ccRecipients);
        }
        
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