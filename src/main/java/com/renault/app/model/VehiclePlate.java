package com.renault.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_plates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePlate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "plate_type", nullable = false)
    private PlateType plateType;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "governorate_code")
    private String governorateCode;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "vehicle_brand")
    private String vehicleBrand;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "vehicle_color")
    private String vehicleColor;

    @Column(name = "vin_number")
    private String vinNumber;

    @Column(name = "horsepower")
    private Integer horsepower;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}