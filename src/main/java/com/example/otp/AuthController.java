package com.example.otp;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpStoreService otpStoreService;
    private final KafkaTemplate<String, OtpEvent> kafkaTemplate;

    public AuthController(OtpStoreService otpStoreService, KafkaTemplate<String, OtpEvent> kafkaTemplate) {
        this.otpStoreService = otpStoreService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(String email, String otp, String newPassword) {}

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestOtp(@RequestBody ForgotPasswordRequest request) {
        String otp = otpStoreService.generateAndSaveOtp(request.email());
        OtpEvent event = new OtpEvent(request.email(), otp, 5);

        // Publish OTP event to Kafka immediately
        kafkaTemplate.send("password-reset-otp", request.email(), event);

        return ResponseEntity.accepted().body("If the email exists, a 6-digit OTP has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean isValid = otpStoreService.validateAndConsumeOtp(request.email(), request.otp());
        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
        // In a real app, hash request.newPassword() with BCrypt and update the user table here
        return ResponseEntity.ok("Password updated successfully for " + request.email());
    }
}
