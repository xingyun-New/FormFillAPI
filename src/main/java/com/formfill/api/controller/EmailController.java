package com.formfill.api.controller;

import com.formfill.api.dto.EmailSendRequest;
import com.formfill.api.dto.EmailSendResponse;
import com.formfill.api.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件发送控制器
 */
@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    
    @Autowired
    private EmailService emailService;
    
    /**
     * 发送带附件的邮件
     */
    @PostMapping("/send")
    public ResponseEntity<EmailSendResponse> sendEmail(@Valid @RequestBody EmailSendRequest request) {
        try {
            logger.info("收到邮件发送请求: {}", request);
            
            EmailSendResponse response = emailService.sendEmailWithAttachment(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("邮件发送接口异常: {}", e.getMessage(), e);
            
            EmailSendResponse errorResponse = EmailSendResponse.failure(
                "邮件发送接口异常: " + e.getMessage(),
                "API_ERROR"
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("邮件服务运行正常");
    }
} 