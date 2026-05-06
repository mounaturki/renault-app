package com.renault.app.dto;

import com.renault.app.model.PlateType;
import com.renault.app.model.VehiclePlate;
import lombok.Data;

@Data
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

    // Constructeur par défaut
    public OcrResponseDto() {}

    // Builder manuel
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OcrResponseDto dto = new OcrResponseDto();

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder plateNumber(String plateNumber) {
            dto.plateNumber = plateNumber;
            return this;
        }

        public Builder plateType(PlateType plateType) {
            dto.plateType = plateType;
            return this;
        }

        public Builder confidence(double confidence) {
            dto.confidence = confidence;
            return this;
        }

        public Builder regionCode(String regionCode) {
            dto.regionCode = regionCode;
            return this;
        }

        public Builder governorateCode(String governorateCode) {
            dto.governorateCode = governorateCode;
            return this;
        }

        public Builder vehicleInfo(VehiclePlate vehicleInfo) {
            dto.vehicleInfo = vehicleInfo;
            return this;
        }

        public Builder isRegistered(boolean isRegistered) {
            dto.isRegistered = isRegistered;
            return this;
        }

        public Builder error(String error) {
            dto.error = error;
            return this;
        }

        public OcrResponseDto build() {
            return dto;
        }
    }
}