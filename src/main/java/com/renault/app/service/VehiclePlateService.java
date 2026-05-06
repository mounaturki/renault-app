package com.renault.app.service;

import com.renault.app.model.VehiclePlate;
import com.renault.app.repository.VehiclePlateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiclePlateService {

    @Autowired
    private VehiclePlateRepository repository;

    @Autowired
    private DatasetService datasetService;  // ← ajouter ça

    public VehiclePlate findByPlateNumber(String plateNumber) {
        // 1. Cherche d'abord dans la base de données
        VehiclePlate fromDb = repository.findByPlateNumber(plateNumber).orElse(null);
        if (fromDb != null) return fromDb;

        // 2. Cherche dans le dataset JSON (exact)
        VehiclePlate fromDataset = datasetService.findByPlateNumber(plateNumber).orElse(null);
        if (fromDataset != null) return fromDataset;

        // 3. Recherche floue si OCR imparfait
        return datasetService.findByPlateNumberFuzzy(plateNumber).orElse(null);
    }

    public VehiclePlate save(VehiclePlate plate) {
        return repository.save(plate);
    }
}