package com.korebit.rigel.service;

import com.korebit.rigel.dto.PurchaseDto;
import com.korebit.rigel.dto.request.PurchaseCreateRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Batch;
import com.korebit.rigel.model.beans.Product;
import com.korebit.rigel.model.beans.Purchase;
import com.korebit.rigel.model.beans.Supplier;
import com.korebit.rigel.model.extra.ProductSupplier;
import com.korebit.rigel.repository.BatchRepository;
import com.korebit.rigel.repository.ProductSupplierRepository;
import com.korebit.rigel.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PurchaseService.
 * Tests purchase creation, retrieval, and batch management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseService Tests")
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductSupplierRepository productSupplierRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private Purchase testPurchase;
    private Product testProduct;
    private Supplier testSupplier;
    private ProductSupplier productSupplier;
    private Batch testBatch;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("Test Supplier");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");

        productSupplier = new ProductSupplier();
        productSupplier.setProduct(testProduct);
        productSupplier.setSupplier(testSupplier);

        testBatch = new Batch();
        testBatch.setId(1L);
        testBatch.setCode("BATCH-001");
        testBatch.setProductSupplier(productSupplier);
        testBatch.setReceptionDate(LocalDate.now());
        testBatch.setExpirationDate(LocalDate.now().plusDays(30));
        testBatch.setReceivedAmount(100);
        testBatch.setRemainingAmount(100);
        testBatch.setAvailable(true);
        testBatch.setPrice(BigDecimal.valueOf(10.00));

        testPurchase = new Purchase();
        testPurchase.setId(1L);
        testPurchase.setCode("PUR-20240101001");
        testPurchase.setProductSupplier(productSupplier);
        testPurchase.setBatch(testBatch);
        testPurchase.setQuantity(50);
        testPurchase.setUnitPrice(BigDecimal.valueOf(10.00));
        testPurchase.setTotalPrice(BigDecimal.valueOf(500.00));
        testPurchase.setPurchaseDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should retrieve all purchases")
    void testGetAllPurchases() {
        when(purchaseRepository.findAll()).thenReturn(Collections.singletonList(testPurchase));

        var result = purchaseService.getAllPurchases();

        assertEquals(1, result.size());
        assertEquals("PUR-20240101001", result.getFirst().getCode());
        verify(purchaseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get purchase by id")
    void testGetPurchaseById() {
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(testPurchase));

        PurchaseDto result = purchaseService.getPurchaseById(1L);

        assertNotNull(result);
        assertEquals("PUR-20240101001", result.getCode());
    }

    @Test
    @DisplayName("Should throw exception when purchase not found by id")
    void testGetPurchaseByIdNotFound() {
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> purchaseService.getPurchaseById(1L));
    }

    @Test
    @DisplayName("Should get purchases by product name")
    void testGetPurchasesByProduct() {
        when(purchaseRepository.findByProductName("Test Product"))
            .thenReturn(Collections.singletonList(testPurchase));

        var result = purchaseService.getPurchasesByProduct("Test Product");

        assertEquals(1, result.size());
        assertEquals("Test Product", result.getFirst().getProductName());
    }

    @Test
    @DisplayName("Should get purchases by supplier name")
    void testGetPurchasesBySupplier() {
        when(purchaseRepository.findBySupplierName("Test Supplier"))
            .thenReturn(Collections.singletonList(testPurchase));

        var result = purchaseService.getPurchasesBySupplier("Test Supplier");

        assertEquals(1, result.size());
        assertEquals("Test Supplier", result.getFirst().getSupplierName());
    }

    @Test
    @DisplayName("Should create a new purchase and update batch")
    void testCreatePurchase() {
        PurchaseCreateRequest request = new PurchaseCreateRequest(
            null,
            "Test Product",
            "Test Supplier",
            50,
            10.00,
            LocalDate.now(),
            "Test notes",
            1L,
            "BATCH-001",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            true,
            10.00
        );

        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier);
        when(batchRepository.findByCode("BATCH-001"))
            .thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any())).thenReturn(testBatch);
        when(purchaseRepository.save(any())).thenReturn(testPurchase);

        Response response = purchaseService.createPurchase(request);

        assertTrue(response.getSuccess());
        assertEquals(201, response.getStatus());
        verify(batchRepository, times(1)).save(any());
        verify(purchaseRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating purchase with zero quantity")
    void testCreatePurchaseInvalidQuantity() {
        PurchaseCreateRequest request = new PurchaseCreateRequest(
            null,
            "Test Product",
            "Test Supplier",
            0,
            10.00,
            LocalDate.now(),
            "Test notes",
            1L,
            "BATCH-001",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            true,
            10.00
        );

        assertThrows(IllegalArgumentException.class, () -> purchaseService.createPurchase(request));
    }

    @Test
    @DisplayName("Should throw exception when creating purchase with invalid unit price")
    void testCreatePurchaseInvalidUnitPrice() {
        PurchaseCreateRequest request = new PurchaseCreateRequest(
            null,
            "Test Product",
            "Test Supplier",
            50,
            -10.00,
            LocalDate.now(),
            "Test notes",
            1L,
            "BATCH-001",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            true,
            -10.00
        );

        assertThrows(IllegalArgumentException.class, () -> purchaseService.createPurchase(request));
    }

    @Test
    @DisplayName("Should throw exception when product-supplier relation not found")
    void testCreatePurchaseRelationNotFound() {
        PurchaseCreateRequest request = new PurchaseCreateRequest(
            null,
            "Test Product",
            "Test Supplier",
            50,
            10.00,
            LocalDate.now(),
            "Test notes",
            1L,
            "BATCH-001",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            true,
            10.00
        );

        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> purchaseService.createPurchase(request));
    }

    @Test
    @DisplayName("Should mark batch as unavailable if expired")
    void testCreatePurchaseExpiredBatch() {
        testBatch.setExpirationDate(LocalDate.now().minusDays(1));

        PurchaseCreateRequest request = new PurchaseCreateRequest(
            null,
            "Test Product",
            "Test Supplier",
            50,
            10.00,
            LocalDate.now(),
            "Test notes",
            1L,
            "BATCH-001",
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(1),
            true,
            10.00
        );

        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier);
        when(batchRepository.findByCode("BATCH-001")).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any())).thenReturn(testBatch);
        when(purchaseRepository.save(any())).thenReturn(testPurchase);

        purchaseService.createPurchase(request);

        verify(batchRepository, times(1)).save(any());
    }
}

