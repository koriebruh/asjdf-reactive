package com.koriebruh.demoreactivenw.repository;

import com.koriebruh.demoreactivenw.entity.Category;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CategoryRepository extends R2dbcRepository<Category, Long> {

    Mono<Category> findByName(String name);

}
