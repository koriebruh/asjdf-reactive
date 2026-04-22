package com.koriebruh.demoreactivenw.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

}
