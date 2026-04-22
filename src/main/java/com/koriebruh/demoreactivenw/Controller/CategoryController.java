package com.koriebruh.demoreactivenw.Controller;

import com.koriebruh.demoreactivenw.dto.ApiResponse;
import com.koriebruh.demoreactivenw.dto.ApiResponseFactory;
import com.koriebruh.demoreactivenw.dto.request.CategoryRequest;
import com.koriebruh.demoreactivenw.dto.response.CategoryResponse;
import com.koriebruh.demoreactivenw.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {


    private final CategoryService categoryService;

    private final ApiResponseFactory responseFactory;

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<CategoryResponse>> created(
            @RequestBody @Validated CategoryRequest request
    ) {
        return categoryService.create(request).map(
                res -> responseFactory.success(
                        "Category created successfully",
                        res
                )
        );
    }


    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/{id}"
    )
    public Mono<ApiResponse<CategoryResponse>> getById(
            @PathVariable Long id
    ) {
        return categoryService.getById(id).map(
                res -> responseFactory.success(
                        "Category fetched successfully",
                        res
                )
        );
    }


    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ApiResponse<List<CategoryResponse>>> getAll() {
        return categoryService.getAll()
                .collectList() // Ubah Flux<CategoryResponse> jadi Mono<List<CategoryResponse>>
                .map(res -> responseFactory.success(
                        "Successfully fetched all categories",
                        res
                ));
    }

    @PatchMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @RequestBody @Validated CategoryRequest request
    ) {
        return categoryService.update(id, request)
                .map(res -> responseFactory.success(
                        "Category updated successfully",
                        res
                ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }


}
