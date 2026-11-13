package com.example.otp;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpStoreService {

    private record OtpEntry(String code, Instant expiresAt) {}

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAndSaveOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpStorage.put(email.toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(300)));
        return otp;
    }
    // method for validate
    public boolean validateAndConsumeOtp(String email, String submittedOtp) {
        OtpEntry entry = otpStorage.get(email.toLowerCase());
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            otpStorage.remove(email.toLowerCase());
            return false;
        }
        if (entry.code().equals(submittedOtp)) {
            otpStorage.remove(email.toLowerCase()); // Single-use: delete after verification
            return true;
        }
        return false;
    }
}
