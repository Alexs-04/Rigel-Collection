package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.PurchaseCreateRequest
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Purchase
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.PurchaseRepository
import com.korebit.rigel.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Unit tests for PurchaseService.
 * Tests purchase creation, retrieval, and batch management.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("PurchaseService Tests")
class PurchaseServiceTest {

    @Mock
    private lateinit var purchaseRepository: PurchaseRepository

    @Mock
    private lateinit var productSupplierRepository: ProductSupplierRepository

    @Mock
    private lateinit var batchRepository: BatchRepository

    @InjectMocks
    private lateinit var purchaseService: PurchaseService

    private lateinit var testPurchase: Purchase
    private lateinit var testProduct: Product
    private lateinit var testSupplier: Supplier
    private lateinit var productSupplier: ProductSupplier
    private lateinit var testBatch: Batch

    @BeforeEach
    fun setUp() {
        testSupplier = Supplier(id = 1L, name = "Test Supplier")

        testProduct = Product(id = 1L, name = "Test Product", barcode = "123456789")

        productSupplier = ProductSupplier(product = testProduct, supplier = testSupplier)

        testBatch = Batch(
            id = 1L,
            code = "BATCH-001",
            productSupplier = productSupplier,
            receptionDate = LocalDate.now(),
            expirationDate = LocalDate.now().plusDays(30),
            receivedAmount = 100,
            remainingAmount = 100,
            available = true,
            price = BigDecimal.valueOf(10.00),
        )

        testPurchase = Purchase(
            id = 1L,
            code = "PUR-20240101001",
            productSupplier = productSupplier,
            batch = testBatch,
            quantity = 50,
            unitPrice = BigDecimal.valueOf(10.00),
            totalPrice = BigDecimal.valueOf(500.00),
            purchaseDate = LocalDate.now(),
        )
    }

    private fun createRequest(
        quantity: Int = 50,
        unitPrice: Double = 10.00,
        receptionDate: LocalDate = LocalDate.now(),
        expirationDate: LocalDate = LocalDate.now().plusDays(30),
        batchPrice: Double = 10.00,
    ) = PurchaseCreateRequest(
        code = null,
        productName = "Test Product",
        supplierName = "Test Supplier",
        quantity = quantity,
        unitPrice = unitPrice,
        purchaseDate = LocalDate.now(),
        notes = "Test notes",
        batchId = 1L,
        batchCode = "BATCH-001",
        receptionDate = receptionDate,
        expirationDate = expirationDate,
        available = true,
        batchPrice = batchPrice,
    )

    @Test
    @DisplayName("Should retrieve all purchases")
    fun testGetAllPurchases() {
        whenever(purchaseRepository.findAll()).thenReturn(listOf(testPurchase))

        val result = purchaseService.getAllPurchases()

        assertEquals(1, result.size)
        assertEquals("PUR-20240101001", result.first().code)
        verify(purchaseRepository, times(1)).findAll()
    }

    @Test
    @DisplayName("Should get purchase by id")
    fun testGetPurchaseById() {
        whenever(purchaseRepository.findById(1L)).thenReturn(Optional.of(testPurchase))

        val result = purchaseService.getPurchaseById(1L)

        assertNotNull(result)
        assertEquals("PUR-20240101001", result.code)
    }

    @Test
    @DisplayName("Should throw exception when purchase not found by id")
    fun testGetPurchaseByIdNotFound() {
        whenever(purchaseRepository.findById(anyLong())).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { purchaseService.getPurchaseById(1L) }
    }

    @Test
    @DisplayName("Should get purchases by product name")
    fun testGetPurchasesByProduct() {
        whenever(purchaseRepository.findByProductName("Test Product")).thenReturn(listOf(testPurchase))

        val result = purchaseService.getPurchasesByProduct("Test Product")

        assertEquals(1, result.size)
        assertEquals("Test Product", result.first().productName)
    }

    @Test
    @DisplayName("Should get purchases by supplier name")
    fun testGetPurchasesBySupplier() {
        whenever(purchaseRepository.findBySupplierName("Test Supplier")).thenReturn(listOf(testPurchase))

        val result = purchaseService.getPurchasesBySupplier("Test Supplier")

        assertEquals(1, result.size)
        assertEquals("Test Supplier", result.first().supplierName)
    }

    @Test
    @DisplayName("Should create a new purchase and update batch")
    fun testCreatePurchase() {
        val request = createRequest()

        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier)
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))
        whenever(batchRepository.save(any<Batch>())).thenReturn(testBatch)
        whenever(purchaseRepository.save(any<Purchase>())).thenReturn(testPurchase)

        val result = purchaseService.createPurchase(request)

        assertEquals("PUR-20240101001", result.code)
        assertEquals(150, testBatch.receivedAmount)
        assertEquals(150, testBatch.remainingAmount)
        verify(batchRepository, times(1)).save(any<Batch>())
        verify(purchaseRepository, times(1)).save(any<Purchase>())
    }

    @Test
    @DisplayName("Should throw exception when creating purchase with zero quantity")
    fun testCreatePurchaseInvalidQuantity() {
        val request = createRequest(quantity = 0)

        assertFailsWith<IllegalArgumentException> { purchaseService.createPurchase(request) }
    }

    @Test
    @DisplayName("Should throw exception when creating purchase with invalid unit price")
    fun testCreatePurchaseInvalidUnitPrice() {
        val request = createRequest(unitPrice = -10.00, batchPrice = -10.00)

        assertFailsWith<IllegalArgumentException> { purchaseService.createPurchase(request) }
    }

    @Test
    @DisplayName("Should throw exception when product-supplier relation not found")
    fun testCreatePurchaseRelationNotFound() {
        val request = createRequest()

        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(null)

        assertFailsWith<EntityNotFundException> { purchaseService.createPurchase(request) }
    }

    @Test
    @DisplayName("Should mark batch as unavailable if expired")
    fun testCreatePurchaseExpiredBatch() {
        testBatch.expirationDate = LocalDate.now().minusDays(1)

        val request = createRequest(
            receptionDate = LocalDate.now().minusDays(1),
            expirationDate = LocalDate.now().minusDays(1),
        )

        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier)
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))
        whenever(batchRepository.save(any<Batch>())).thenReturn(testBatch)
        whenever(purchaseRepository.save(any<Purchase>())).thenReturn(testPurchase)

        purchaseService.createPurchase(request)

        assertFalse(testBatch.available)
        verify(batchRepository, times(1)).save(any<Batch>())
    }
}
