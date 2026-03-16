package com.korebit.rigel.repository;

import com.korebit.rigel.model.beans.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {

    @Query("""
            select b from batches b
            join b.productSupplier ps
            where ps.product.id = :productId
            order by b.expirationDate asc, b.receptionDate asc, b.id asc
            """)
    List<Batch> findAllByProductId(@Param("productId") Long productId);

    @Query("""
            select b from batches b
            join b.productSupplier ps
            where ps.product.id = :productId
              and b.available = true
              and b.remainingAmount > 0
              and b.expirationDate >= :today
            order by b.expirationDate asc, b.receptionDate asc, b.id asc
            """)
    List<Batch> findSellableByProductId(@Param("productId") Long productId, @Param("today") LocalDate today);
}

