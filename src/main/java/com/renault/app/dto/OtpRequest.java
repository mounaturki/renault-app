package com.renault.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;
}