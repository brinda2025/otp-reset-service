package com.example.otp;

import jakarta.mail.internet.MimeMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailConsumer {

    private final JavaMailSender mailSender;

    public OtpEmailConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "password-reset-otp", groupId = "otp-email-group")
    public void sendOtpEmail(OtpEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setFrom("security@myapp.local");
            helper.setTo(event.email());
            helper.setSubject("Your Password Reset Code: " + event.otpCode());
            helper.setText(
                    "<h3>Password Reset Request</h3>" +
                    "<p>Use the following 6-digit OTP to reset your password:</p>" +
                    "<h1 style='letter-spacing: 5px; color: #2b6cb0;'>" + event.otpCode() + "</h1>" +
                    "<p>This code expires in <b>" + event.expiryMinutes() + " minutes</b>.</p>",
                    true
            );
            mailSender.send(message);
            System.out.println("OTP Email sent to -> " + event.email());
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }
}
