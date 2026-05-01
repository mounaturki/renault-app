package com.renault.app.repository;

import com.renault.app.model.VehiclePlate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiclePlateRepository extends JpaRepository<VehiclePlate, Long> {
    Optional<VehiclePlate> findByPlateNumber(String plateNumber);
}