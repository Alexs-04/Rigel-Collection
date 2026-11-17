package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.BatchUpsertRequest
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Unit tests for BatchService.
 * Tests batch creation, updates, deletion, and batch retrieval operations.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("BatchService Tests")
class BatchServiceTest {

    @Mock
    private lateinit var batchRepository: BatchRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var productSupplierRepository: ProductSupplierRepository

    @InjectMocks
    private lateinit var batchService: BatchService

    private lateinit var testBatch: Batch
    private lateinit var testProduct: Product
    private lateinit var testSupplier: Supplier
    private lateinit var productSupplier: ProductSupplier

    @BeforeEach
    fun setUp() {
        testSupplier = Supplier(id = 1L, name = "Test Supplier")

        testProduct = Product(id = 1L, name = "Test Product", barcode = "123456789")

        productSupplier = ProductSupplier(product = testProduct, supplier = testSupplier)
        testProduct.suppliers = mutableListOf(productSupplier)

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
            notes = "",
        )
    }

    private fun upsertRequest(
        code: String = "BATCH-001",
        receptionDate: LocalDate = LocalDate.now(),
        expirationDate: LocalDate = LocalDate.now().plusDays(30),
        receivedAmount: Int = 50,
        price: Double = 10.00,
        notes: String = "Test notes",
    ) = BatchUpsertRequest(
        code = code,
        productName = "Test Product",
        supplierName = "Test Supplier",
        receptionDate = receptionDate,
        expirationDate = expirationDate,
        receivedAmount = receivedAmount,
        remainingAmount = null,
        available = true,
        price = price,
        notes = notes,
    )

    @Test
    @DisplayName("Should retrieve batches by product")
    fun testGetBatchesByProduct() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))
        whenever(batchRepository.findAllByProductId(1L)).thenReturn(listOf(testBatch))

        val result = batchService.getBatchesByProduct("Test Product")

        assertEquals(1, result.size)
        assertEquals("BATCH-001", result.first().code)
        verify(batchRepository, times(1)).findAllByProductId(1L)
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    fun testGetBatchesByProductNotFound() {
        whenever(productRepository.findByName("NotFound")).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { batchService.getBatchesByProduct("NotFound") }
    }

    @Test
    @DisplayName("Should get batch by id")
    fun testGetBatchById() {
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))

        val result = batchService.getBatchById(1L)

        assertNotNull(result)
        assertEquals("BATCH-001", result.code)
    }

    @Test
    @DisplayName("Should throw exception when batch not found by id")
    fun testGetBatchByIdNotFound() {
        whenever(batchRepository.findById(anyLong())).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { batchService.getBatchById(1L) }
    }

    @Test
    @DisplayName("Should create a new batch")
    fun testCreateBatch() {
        val request = upsertRequest(code = "BATCH-002")

        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier)
        whenever(batchRepository.findByCode("BATCH-002")).thenReturn(Optional.empty())
        whenever(batchRepository.save(any<Batch>())).then { invocation ->
            invocation.getArgument<Batch>(0).apply { id = 2L }
        }

        val result = batchService.createBatch(request)

        assertEquals("BATCH-002", result.code)
        assertEquals(50, result.remainingAmount)
        assertEquals("Test Product", result.productName)
        verify(batchRepository, times(1)).save(any<Batch>())
    }

    @Test
    @DisplayName("Should reject a batch whose code already exists")
    fun testCreateBatchDuplicateCode() {
        val request = upsertRequest(notes = "")
        whenever(batchRepository.findByCode("BATCH-001")).thenReturn(Optional.of(testBatch))

        assertFailsWith<IllegalArgumentException> { batchService.createBatch(request) }
        verify(batchRepository, never()).save(any<Batch>())
    }

    @Test
    @DisplayName("Should throw exception when creating batch with empty code")
    fun testCreateBatchEmptyCode() {
        val request = upsertRequest(code = "   ")

        assertFailsWith<IllegalArgumentException> { batchService.createBatch(request) }
    }

    @Test
    @DisplayName("Should throw exception when expiration date is before reception date")
    fun testCreateBatchInvalidDates() {
        val request = upsertRequest(
            code = "BATCH-003",
            receptionDate = LocalDate.now().plusDays(30),
            expirationDate = LocalDate.now(),
        )

        assertFailsWith<IllegalArgumentException> { batchService.createBatch(request) }
    }

    @Test
    @DisplayName("Should update a batch")
    fun testUpdateBatch() {
        val request = upsertRequest(receivedAmount = 75, price = 12.00, notes = "Updated notes")

        testBatch.remainingAmount = 80 // 20 units already sold
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))
        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier)
        whenever(batchRepository.save(any<Batch>())).then(returnsFirstArg<Batch>())

        val result = batchService.updateBatch(1L, request)

        assertEquals(75, result.receivedAmount)
        assertEquals(55, result.remainingAmount)
        assertEquals(12.0, result.price)
        verify(batchRepository, times(1)).save(any<Batch>())
    }

    @Test
    @DisplayName("Should reject an update that leaves less stock than already consumed")
    fun testUpdateBatchBelowConsumed() {
        testBatch.remainingAmount = 10 // 90 units already sold
        val request = upsertRequest(notes = "")
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))
        whenever(productSupplierRepository.findByProductNameAndSupplierName("Test Product", "Test Supplier"))
            .thenReturn(productSupplier)

        assertFailsWith<IllegalArgumentException> { batchService.updateBatch(1L, request) }
        verify(batchRepository, never()).save(any<Batch>())
    }

    @Test
    @DisplayName("Should delete a batch")
    fun testDeleteBatch() {
        whenever(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch))

        batchService.deleteBatch(1L)

        verify(batchRepository, times(1)).delete(testBatch)
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent batch")
    fun testDeleteBatchNotFound() {
        whenever(batchRepository.findById(anyLong())).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { batchService.deleteBatch(1L) }
    }
}
