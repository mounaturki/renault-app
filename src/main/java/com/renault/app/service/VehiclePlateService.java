package com.renault.app.service;

import com.renault.app.model.VehiclePlate;
import com.renault.app.repository.VehiclePlateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiclePlateService {

    @Autowired
    private VehiclePlateRepository repository;

    public VehiclePlate findByPlateNumber(String plateNumber) {
        return repository.findByPlateNumber(plateNumber).orElse(null);
    }

    public VehiclePlate save(VehiclePlate plate) {
        return repository.save(plate);
    }
}