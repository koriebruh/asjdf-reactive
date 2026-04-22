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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final R2dbcEntityTemplate template;

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

    public Mono<ProductPageResponse> getAllWithPaging(
            String name,
            Long categoryId,
            Integer minStock,
            Integer maxStock,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        log.info("Fetching products with dynamic filters");

        // 1. Bangun kriteria pencarian secara dinamis
        Criteria criteria = Criteria.empty();

        if (name != null && !name.isBlank()) {
            criteria = criteria.and("name").like("%" + name + "%");
        }
        if (categoryId != null) {
            criteria = criteria.and("category_id").is(categoryId);
        }
        if (minStock != null) {
            criteria = criteria.and("stock").greaterThanOrEquals(minStock);
        }
        if (minPrice != null) {
            criteria = criteria.and("price").greaterThanOrEquals(minPrice);
        }
        if (maxPrice != null) {
            criteria = criteria.and("price").lessThanOrEquals(maxPrice);
        }
        if (minStock != null) {
            criteria = criteria.and("stock").greaterThanOrEquals(minStock);
        }
        // TAMBAHKAN INI: Untuk filter stok hampir habis
        if (maxStock != null) {
            criteria = criteria.and("stock").lessThanOrEquals(maxStock);
        }


        // 2. Bungkus kriteria ke dalam Query (termasuk paging & sorting)
        Query query = Query.query(criteria).with(pageable);

        // 3. Eksekusi: Ambil data dan Hitung total secara bersamaan (Parallel)
        return template.select(Product.class)
                .from("products")
                .matching(query)
                .all()
                .flatMap(this::enrichWithCategory) // Tempelkan data kategori
                .collectList()
                .zipWith(template.count(Query.query(criteria), Product.class))
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
