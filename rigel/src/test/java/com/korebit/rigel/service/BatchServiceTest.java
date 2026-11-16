package com.korebit.rigel.service;

import com.korebit.rigel.dto.BatchDto;
import com.korebit.rigel.dto.request.BatchUpsertRequest;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Batch;
import com.korebit.rigel.model.beans.Product;
import com.korebit.rigel.model.beans.Supplier;
import com.korebit.rigel.model.extra.ProductSupplier;
import com.korebit.rigel.repository.BatchRepository;
import com.korebit.rigel.repository.ProductRepository;
import com.korebit.rigel.repository.ProductSupplierRepository;
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
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchService.
 * Tests batch creation, updates, deletion, and batch retrieval operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchService Tests")
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSupplierRepository productSupplierRepository;

    @InjectMocks
    private BatchService batchService;

    private Batch testBatch;
    private Product testProduct;
    private Supplier testSupplier;
    private ProductSupplier productSupplier;

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
        testProduct.setSuppliers(Collections.singletonList(productSupplier));

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
        testBatch.setNotes("");
    }

    @Test
    @DisplayName("Should retrieve batches by product")
    void testGetBatchesByProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));
        when(batchRepository.findAllByProductId(1L)).thenReturn(Collections.singletonList(testBatch));

        var result = batchService.getBatchesByProduct("Test Product");

        assertEquals(1, result.size());
        assertEquals("BATCH-001", result.getFirst().getCode());
        verify(batchRepository, times(1)).findAllByProductId(1L);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetBatchesByProductNotFound() {
        when(productRepository.findByName("NotFound")).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> batchService.getBatchesByProduct("NotFound"));
    }

    @Test
    @DisplayName("Should get batch by id")
    void testGetBatchById() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));

        BatchDto result = batchService.getBatchById(1L);

        assertNotNull(result);
        assertEquals("BATCH-001", result.getCode());
    }

    @Test
    @DisplayName("Should throw exception when batch not found by id")
    void testGetBatchByIdNotFound() {
        when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> batchService.getBatchById(1L));
    }

    @Test
    @DisplayName("Should create a new batch")
    void testCreateBatch() {
        BatchUpsertRequest request = new BatchUpsertRequest(
                "BATCH-002",
                "Test Product",
                "Test Supplier",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                50,
                null,
                true,
                10.00,
                "Test notes"
        );

        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
                .thenReturn(productSupplier);
        when(batchRepository.findByCode("BATCH-002")).thenReturn(Optional.empty());
        when(batchRepository.save(any())).then(invocation -> {
            Batch saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        BatchDto result = batchService.createBatch(request);

        assertEquals("BATCH-002", result.getCode());
        assertEquals(50, result.getRemainingAmount());
        assertEquals("Test Product", result.getProductName());
        verify(batchRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should reject a batch whose code already exists")
    void testCreateBatchDuplicateCode() {
        BatchUpsertRequest request = new BatchUpsertRequest(
                "BATCH-001", "Test Product", "Test Supplier",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, null, true, 10.00, ""
        );
        when(batchRepository.findByCode("BATCH-001")).thenReturn(Optional.of(testBatch));

        assertThrows(IllegalArgumentException.class, () -> batchService.createBatch(request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating batch with empty code")
    void testCreateBatchEmptyCode() {
        BatchUpsertRequest request = new BatchUpsertRequest(
                "   ",
                "Test Product",
                "Test Supplier",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                50,
                null,
                true,
                10.00,
                "Test notes"
        );

        assertThrows(IllegalArgumentException.class, () -> batchService.createBatch(request));
    }

    @Test
    @DisplayName("Should throw exception when expiration date is before reception date")
    void testCreateBatchInvalidDates() {
        BatchUpsertRequest request = new BatchUpsertRequest(
                "BATCH-003",
                "Test Product",
                "Test Supplier",
                LocalDate.now().plusDays(30),
                LocalDate.now(),
                50,
                null,
                true,
                10.00,
                "Test notes"
        );

        assertThrows(IllegalArgumentException.class, () -> batchService.createBatch(request));
    }

    @Test
    @DisplayName("Should update a batch")
    void testUpdateBatch() {
        BatchUpsertRequest request = new BatchUpsertRequest(
                "BATCH-001",
                "Test Product",
                "Test Supplier",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                75,
                null,
                true,
                12.00,
                "Updated notes"
        );

        testBatch.setRemainingAmount(80); // 20 units already sold
        when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
                .thenReturn(productSupplier);
        when(batchRepository.save(any())).then(returnsFirstArg());

        BatchDto result = batchService.updateBatch(1L, request);

        assertEquals(75, result.getReceivedAmount());
        assertEquals(55, result.getRemainingAmount());
        assertEquals(12.0, result.getPrice());
        verify(batchRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should reject an update that leaves less stock than already consumed")
    void testUpdateBatchBelowConsumed() {
        testBatch.setRemainingAmount(10); // 90 units already sold
        BatchUpsertRequest request = new BatchUpsertRequest(
                "BATCH-001", "Test Product", "Test Supplier",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, null, true, 10.00, ""
        );
        when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
        when(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
                .thenReturn(productSupplier);

        assertThrows(IllegalArgumentException.class, () -> batchService.updateBatch(1L, request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete a batch")
    void testDeleteBatch() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));

        batchService.deleteBatch(1L);

        verify(batchRepository, times(1)).delete(testBatch);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent batch")
    void testDeleteBatchNotFound() {
        when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> batchService.deleteBatch(1L));
    }
}
