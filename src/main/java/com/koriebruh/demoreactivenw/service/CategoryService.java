package com.koriebruh.demoreactivenw.service;

import com.koriebruh.demoreactivenw.dto.request.CategoryRequest;
import com.koriebruh.demoreactivenw.dto.response.CategoryResponse;
import com.koriebruh.demoreactivenw.dto.response.ProductPageResponse;
import com.koriebruh.demoreactivenw.dto.response.ProductResponse;
import com.koriebruh.demoreactivenw.entity.Category;
import com.koriebruh.demoreactivenw.exceptions.NotFoundException;
import com.koriebruh.demoreactivenw.repository.CategoryRepository;
import com.koriebruh.demoreactivenw.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    public Mono<CategoryResponse> create(CategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        return categoryRepository.findByName(request.getName())
                .flatMap(existing -> {
                    log.warn("Category already exists: {}", request.getName());
                    return Mono.<Category>error(
                            new IllegalArgumentException("Category already exists")
                    );
                })
                .switchIfEmpty(
                        categoryRepository.save(
                                Category.builder()
                                        .name(request.getName())
                                        .build()
                        )
                )
                .map(this::toResponse)
                .doOnSuccess(res -> log.info("Category created with id: {}", res.getId()))
                .doOnError(err -> log.error("Error creating category: {}", err.getMessage()));
    }

    public Flux<CategoryResponse> getAll() {
        log.info("Fetching all categories");

        return categoryRepository.findAll()
                .map(this::toResponse)
                .doOnComplete(() -> log.info("Finished fetching categories"))
                .doOnError(err -> log.error("Error fetching categories: {}", err.getMessage()));
    }

    public Mono<CategoryResponse> getById(Long id) {
        log.info("Fetching category by id: {}", id);

        return categoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found")))
                .map(this::toResponse)
                .doOnSuccess(res -> log.info("Category found: {}", res.getId()))
                .doOnError(err -> log.error("Error fetching category {}: {}", id, err.getMessage()));
    }

    public Mono<CategoryResponse> update(Long id, CategoryRequest request) {
        log.info("Updating category id: {}", id);

        return categoryRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found")))
                .flatMap(existing ->
                        categoryRepository.findByName(request.getName())
                                .filter(cat -> !cat.getId().equals(id))
                                .flatMap(cat -> {
                                    log.warn("Category name already used: {}", request.getName());
                                    return Mono.<Category>error(
                                            new IllegalArgumentException("Category name already used")
                                    );
                                })
                                .switchIfEmpty(
                                        categoryRepository.save(
                                                existing.toBuilder()
                                                        .name(request.getName())
                                                        .build()
                                        )
                                )
                )
                .map(this::toResponse)
                .doOnSuccess(res -> log.info("Category updated: {}", res.getId()))
                .doOnError(err -> log.error("Error updating category {}: {}", id, err.getMessage()));
    }

    public Mono<Void> delete(Long id) {
        log.info("Attempting to delete category id: {}", id);

        return categoryRepository.findById(id)
                // 1. Cek apakah kategori ada
                .switchIfEmpty(Mono.error(new NotFoundException("Category not found")))
                .flatMap(category ->
                        // 2. Cek apakah ada product yang menggunakan category ini
                        productRepository.existsByCategoryId(id)
                                .flatMap(isUsed -> {
                                    if (isUsed) {
                                        log.warn("Failed to delete category {}: still in use by products", id);
                                        return Mono.error(new IllegalArgumentException(
                                                "Category cannot be deleted because it is still used by products"
                                        ));
                                    }
                                    // 3. Kalau aman, baru hapus
                                    return categoryRepository.delete(category);
                                })
                )
                .doOnSuccess(v -> log.info("Category deleted: {}", id))
                .doOnError(err -> log.error("Error deleting category {}: {}", id, err.getMessage()));
    }


    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
