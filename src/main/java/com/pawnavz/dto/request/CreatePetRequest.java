package com.pawnavz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePetRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String breed;
    private Integer age;
    private Double weight;
}
