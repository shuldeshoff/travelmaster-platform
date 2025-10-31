package com.travelmaster.notification.service;

import com.travelmaster.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Сервис для отправки email уведомлений.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@travelmaster.com}")
    private String fromEmail;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    /**
     * Отправка email асинхронно.
     */
    @Async
    public void sendEmail(Notification notification) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping notification {}", notification.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getContent(), true); // HTML

            mailSender.send(message);
            
            log.info("Email sent successfully to {}", notification.getRecipientEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", 
                    notification.getRecipientEmail(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Отправка email с использованием шаблона.
     */
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Context context) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping templated email to {}", to);
            return;
        }

        try {
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
            log.info("Templated email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send templated email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send templated email", e);
        }
    }
}

