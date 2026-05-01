package com.renault.app.controller;

import com.renault.app.dto.OcrResponseDto;
import com.renault.app.model.PlateType;
import com.renault.app.model.VehiclePlate;
import com.renault.app.service.OcrService;
import com.renault.app.service.OcrService.PlateScanResult;
import com.renault.app.service.VehiclePlateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "*")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private VehiclePlateService plateService;

    @PostMapping("/scan/{type}")
    public ResponseEntity<?> scanPlate(
            @PathVariable String type,
            @RequestParam("image") MultipartFile image) {

        try {
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(OcrResponseDto.builder().success(false).error("Image requise").build());
            }

            PlateType plateType = type.equalsIgnoreCase("tunisian")
                    ? PlateType.TUNISIAN
                    : PlateType.FOREIGN;

            PlateScanResult result = ocrService.scanPlate(image, plateType);

            if (!result.isValid()) {
                return ResponseEntity.ok(OcrResponseDto.builder()
                        .success(false)
                        .error("Plaque non reconnue. Réessayez avec une meilleure image.")
                        .build());
            }

            VehiclePlate vehicleInfo = plateService.findByPlateNumber(result.getPlateNumber());

            OcrResponseDto response = OcrResponseDto.builder()
                    .success(true)
                    .plateNumber(result.getPlateNumber())
                    .plateType(result.getPlateType())
                    .confidence(result.getConfidence())
                    .regionCode(result.getRegionCode())
                    .governorateCode(result.getGovernorateCode())
                    .vehicleInfo(vehicleInfo)
                    .isRegistered(vehicleInfo != null)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(OcrResponseDto.builder()
                            .success(false)
                            .error("Erreur: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/check/{plateNumber}")
    public ResponseEntity<?> checkPlate(@PathVariable String plateNumber) {
        VehiclePlate vehicle = plateService.findByPlateNumber(plateNumber);
        return ResponseEntity.ok(OcrResponseDto.builder()
                .success(true)
                .isRegistered(vehicle != null)
                .vehicleInfo(vehicle)
                .build());
    }
}