package com.koriebruh.demoreactivenw.Controller;

import com.koriebruh.demoreactivenw.dto.ApiResponse;
import com.koriebruh.demoreactivenw.dto.ApiResponseFactory;
import com.koriebruh.demoreactivenw.dto.request.ProductRequest;
import com.koriebruh.demoreactivenw.dto.response.ProductPageResponse;
import com.koriebruh.demoreactivenw.dto.response.ProductResponse;
import com.koriebruh.demoreactivenw.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    private final ApiResponseFactory responseFactory;

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<ProductResponse>> create(
            @RequestBody @Validated ProductRequest request
    ) {
        return productService.create(request)
                .map(res -> responseFactory.success("Product created successfully", res));
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return productService.getById(id)
                .map(res -> responseFactory.success("Product found", res));
    }

    @PutMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @RequestBody @Validated ProductRequest request
    ) {
        return productService.update(id, request)
                .map(res -> responseFactory.success("Product updated successfully", res));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    @GetMapping
    public Mono<ApiResponse<ProductPageResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return productService.getAllWithPaging(PageRequest.of(page, size))
                .map(res -> responseFactory.success("Products fetched", res));
    }
}
