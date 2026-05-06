package com.renault.app.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private SmsService smsService;

    @Value("${otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${otp.max-attempts}")
    private int maxAttempts;

    private final Cache<String, OtpData> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public String generateAndSendOtp(String phoneNumber) {
        Integer attempts = attemptCache.getIfPresent(phoneNumber);
        if (attempts != null && attempts >= maxAttempts) {
            throw new RuntimeException("Trop de tentatives. Réessayez dans 30 minutes.");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpData otpData = new OtpData(otp, LocalDateTime.now().plusMinutes(otpExpiryMinutes), false);
        otpCache.put(phoneNumber, otpData);

        attemptCache.put(phoneNumber, (attempts != null ? attempts : 0) + 1);

        // Envoyer par SMS (simulé en console)
        String message = "Votre code OTP Renault App est : " + otp + ". Valide pendant " + otpExpiryMinutes + " minutes.";
        smsService.sendSms(phoneNumber, message);

        return otp;
    }

    public boolean verifyOtp(String phoneNumber, String otpCode) {
        OtpData otpData = otpCache.getIfPresent(phoneNumber);

        if (otpData == null) {
            return false;
        }

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpCache.invalidate(phoneNumber);
            return false;
        }

        if (!otpData.getCode().equals(otpCode)) {
            return false;
        }

        otpData.setVerified(true);
        otpCache.put(phoneNumber, otpData);
        return true;
    }

    public void clearOtp(String phoneNumber) {
        otpCache.invalidate(phoneNumber);
        attemptCache.invalidate(phoneNumber);
    }

    private static class OtpData {
        private final String code;
        private final LocalDateTime expiryTime;
        private boolean verified;

        public OtpData(String code, LocalDateTime expiryTime, boolean verified) {
            this.code = code;
            this.expiryTime = expiryTime;
            this.verified = verified;
        }

        public String getCode() { return code; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }
}