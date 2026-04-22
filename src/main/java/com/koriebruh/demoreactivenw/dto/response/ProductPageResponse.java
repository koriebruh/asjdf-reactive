package com.koriebruh.demoreactivenw.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageResponse {
    private List<ProductResponse> content;
    private Integer totalPages;
    private Long totalElements;
    private Integer size;
    private Integer number;
}
