package com.renault.app.dto;

import com.renault.app.model.PlateType;
import com.renault.app.model.VehiclePlate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrResponseDto {
    private boolean success;
    private String plateNumber;
    private PlateType plateType;
    private double confidence;
    private String regionCode;
    private String governorateCode;
    private VehiclePlate vehicleInfo;
    private boolean isRegistered;
    private String error;
}