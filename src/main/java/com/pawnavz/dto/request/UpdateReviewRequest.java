package com.pawnavz.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 150)
    private String title;

    @Size(max = 1000)
    private String comment;
}
