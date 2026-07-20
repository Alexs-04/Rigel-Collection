package com.korebit.rigel.repository;

import com.korebit.rigel.model.beans.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String code);

    Optional<Product> findByName(String name);
}
