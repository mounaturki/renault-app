package com.renault.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RenaultAppApplication {

    static {
        // Charger OpenCV
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("✅ OpenCV chargé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement OpenCV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(RenaultAppApplication.class, args);
    }
}