package com.korebit.rigel.model.beans;

import com.korebit.rigel.model.extra.ProductSupplier;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "batches")
@Table(
        name = "batches",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_batches_code", columnNames = "code")
        }
)
public class Batch implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 100, nullable = false)
    private String code;
    @Column(nullable = false)
    private LocalDate receptionDate;
    @Column(nullable = false)
    private LocalDate expirationDate;
    @Column(nullable = false, length = 20)
    private Integer receivedAmount;
    @Column(nullable = false, length = 20)
    private Integer remainingAmount;
    @Column(nullable = false)
    private Boolean available;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    @Column(nullable = false)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false),
            @JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false)
    })
    private ProductSupplier productSupplier;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Purchase> purchases = new ArrayList<>();

    public Batch(Long id, String code, LocalDate receptionDate, LocalDate expirationDate, Integer receivedAmount,
                 Integer remainingAmount, Boolean available, BigDecimal price, String notes, ProductSupplier productSupplier) {
        this.id = id;
        this.code = code;
        this.receptionDate = receptionDate;
        this.expirationDate = expirationDate;
        this.receivedAmount = receivedAmount;
        this.remainingAmount = remainingAmount;
        this.available = available;
        this.price = price;
        this.notes = notes;
        this.productSupplier = productSupplier;
    }

    public Batch() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(LocalDate receptionDate) {
        this.receptionDate = receptionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(Integer receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public Integer getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Integer remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ProductSupplier getProductSupplier() {
        return productSupplier;
    }

    public void setProductSupplier(ProductSupplier productSupplier) {
        this.productSupplier = productSupplier;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }
}
