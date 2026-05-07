package com.renault.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renault.app.model.VehiclePlate;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatasetService {

    private List<VehiclePlate> dataset;

    @PostConstruct
    public void loadDataset() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            ClassPathResource resource = new ClassPathResource("vehicles-dataset.json");
            dataset = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<VehiclePlate>>() {}
            );
            System.out.println("✅ Dataset chargé: " + dataset.size() + " véhicules");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement dataset: " + e.getMessage());
            dataset = List.of();
        }
    }

    // Recherche par numéro de plaque exact
    public Optional<VehiclePlate> findByPlateNumber(String plateNumber) {
        return dataset.stream()
                .filter(v -> v.getPlateNumber()
                        .equalsIgnoreCase(plateNumber.trim()))
                .findFirst();
    }

    // Recherche partielle (si OCR n'est pas parfait)
    public Optional<VehiclePlate> findByPlateNumberFuzzy(String plateNumber) {
        String cleaned = plateNumber.replaceAll("\\s+", "").toUpperCase();
        return dataset.stream()
                .filter(v -> {
                    String dp = v.getPlateNumber()
                            .replaceAll("\\s+", "").toUpperCase();
                    return dp.contains(cleaned) || cleaned.contains(dp);
                })
                .findFirst();
    }
}