package com.koriebruh.demoreactivenw.repository;

import com.koriebruh.demoreactivenw.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface ProductRepository extends R2dbcRepository<Product, Long> {

    Mono<Long> count();

    Mono<Boolean> existsByCategoryId(Long categoryId);
}
