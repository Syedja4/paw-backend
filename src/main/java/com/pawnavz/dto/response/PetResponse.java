package com.pawnavz.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PetResponse {

    private String id;
    private String name;
    private String type;
    private String breed;
    private Integer age;
    private Double weight;
}
