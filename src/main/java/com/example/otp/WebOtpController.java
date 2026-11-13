package com.example.otp;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebOtpController {

    private final OtpStoreService otpStoreService;
    private final KafkaTemplate<String, OtpEvent> kafkaTemplate;

    public WebOtpController(OtpStoreService otpStoreService, KafkaTemplate<String, OtpEvent> kafkaTemplate) {
        this.otpStoreService = otpStoreService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/forgot-password";
    }
     // getmapping
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        String otp = otpStoreService.generateAndSaveOtp(email);
        kafkaTemplate.send("password-reset-otp", email, new OtpEvent(email, otp, 5));

        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("message", "OTP sent via Kafka! Check Mailpit (localhost:8025).");
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleReset(@RequestParam String email,
                              @RequestParam String otp,
                              @RequestParam String newPassword,
                              RedirectAttributes redirectAttributes) {
        if (!otpStoreService.validateAndConsumeOtp(email, otp)) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP.");
            return "redirect:/reset-password";
        }
        redirectAttributes.addFlashAttribute("message", "Success! Password reset verified for " + email);
        return "redirect:/forgot-password";
    }
}
