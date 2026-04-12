package com.pawnavz.dto.request;

import lombok.Data;

@Data
public class UpdatePetRequest {

    private String name;
    private String type;

    private String breed;
    private Integer age;
    private Double weight;
}
