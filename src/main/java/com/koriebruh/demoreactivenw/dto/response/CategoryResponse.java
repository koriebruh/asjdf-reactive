package com.koriebruh.demoreactivenw.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CategoryResponse {

    private Long id;

    private String name;
}
