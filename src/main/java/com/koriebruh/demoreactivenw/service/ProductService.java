package com.koriebruh.demoreactivenw.service;

import com.koriebruh.demoreactivenw.dto.request.ProductRequest;
import com.koriebruh.demoreactivenw.dto.response.CategoryResponse;
import com.koriebruh.demoreactivenw.dto.response.ProductPageResponse;
import com.koriebruh.demoreactivenw.dto.response.ProductResponse;
import com.koriebruh.demoreactivenw.entity.Product;
import com.koriebruh.demoreactivenw.exceptions.NotFoundException;
import com.koriebruh.demoreactivenw.repository.CategoryRepository;
import com.koriebruh.demoreactivenw.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    public Mono<ProductResponse> create(ProductRequest request) {
        log.info("Creating product: {}", request.getName());

        return productRepository.save(Product.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .stock(request.getStock())
                        .sku(request.getSku())
                        .categoryId(request.getCategoryId())
                        .build())
                .flatMap(this::enrichWithCategory); // Gabungkan dengan data Kategori
    }

    public Mono<ProductResponse> getById(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                .flatMap(this::enrichWithCategory);
    }

    public Flux<ProductResponse> getAll() {
        return productRepository.findAll()
                .flatMap(this::enrichWithCategory);
    }

    public Mono<ProductResponse> update(Long id, ProductRequest request) {
        log.info("Updating product id: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                .flatMap(existing -> {
                    // Update field dari request
                    Product updatedProduct = existing.toBuilder()
                            .name(request.getName())
                            .description(request.getDescription())
                            .price(request.getPrice())
                            .stock(request.getStock())
                            .sku(request.getSku())
                            .categoryId(request.getCategoryId())
                            .updatedAt(java.time.LocalDateTime.now()) // Manual update timestamp
                            .build();

                    return productRepository.save(updatedProduct);
                })
                .flatMap(this::enrichWithCategory)
                .doOnSuccess(res -> log.info("Product updated: {}", res.getId()));
    }

    public Mono<ProductPageResponse> getAllWithPaging(Pageable pageable) {
        return productRepository.findAllBy(pageable)
                .flatMap(this::enrichWithCategory)
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> {
                    List<ProductResponse> content = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());

                    return ProductPageResponse.builder()
                            .content(content)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .size(pageable.getPageSize())
                            .number(pageable.getPageNumber())
                            .build();
                });
    }

    public Mono<Void> delete(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                .flatMap(productRepository::delete);
    }


    // Helper Method untuk mapping relasi Category ke ProductResponse
    private Mono<ProductResponse> enrichWithCategory(Product product) {
        if (product.getCategoryId() == null) {
            return Mono.just(toResponse(product, null));
        }

        return categoryRepository.findById(product.getCategoryId())
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .map(catRes -> toResponse(product, catRes))
                .defaultIfEmpty(toResponse(product, null));
    }

    private ProductResponse toResponse(Product product, CategoryResponse categoryResponse) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .sku(product.getSku())
                .category(categoryResponse) // Isi field kategori hasil fetch manual
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
