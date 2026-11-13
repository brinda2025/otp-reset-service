package com.example.otp;

public record OtpEvent(String email, String otpCode, int expiryMinutes) {}
