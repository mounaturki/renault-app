package com.renault.app.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendSms(String phoneNumber, String message) {
        System.out.println("========================================");
        System.out.println("📱 SMS SIMULÉ (mode gratuit)");
        System.out.println("========================================");
        System.out.println("Destinataire : " + phoneNumber);
        System.out.println("Message      : " + message);
        System.out.println("========================================");
        System.out.println("⚠️  En production : remplacer par Twilio/Vonage");
        System.out.println("========================================");
    }
}