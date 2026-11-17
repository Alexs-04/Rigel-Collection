package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.repository.SupplierRepository
import com.korebit.rigel.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for SupplierService.
 * Tests supplier CRUD operations, product associations, and pricing.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("SupplierService Tests")
class SupplierServiceTest {

    @Mock
    private lateinit var supplierRepository: SupplierRepository

    @InjectMocks
    private lateinit var supplierService: SupplierService

    private lateinit var testSupplier: Supplier
    private lateinit var testSupplierDto: SupplierDto

    @BeforeEach
    fun setUp() {
        testSupplier = Supplier(
            id = 1L,
            name = "Test Supplier",
            email = "supplier@example.com",
            numberPhone = "1234567890",
            address = "123 Main St",
            products = mutableListOf(),
        )

        testSupplierDto = SupplierDto(
            "Test Supplier",
            "supplier@example.com",
            "1234567890",
            "123 Main St",
            emptyList(),
        )
    }

    @Test
    @DisplayName("Should retrieve all suppliers")
    fun testGetAllSuppliers() {
        val supplier2 = Supplier(
            id = 2L,
            name = "Test Supplier 2",
            email = "supplier2@example.com",
            products = mutableListOf(),
        )

        whenever(supplierRepository.findAll()).thenReturn(listOf(testSupplier, supplier2))

        val result = supplierService.getAllSuppliers()

        assertEquals(2, result.size)
        assertEquals("Test Supplier", result[0].name)
        assertEquals("Test Supplier 2", result[1].name)
        verify(supplierRepository, times(1)).findAll()
    }

    @Test
    @DisplayName("Should save a new supplier")
    fun testSaveSupplier() {
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(null)
        whenever(supplierRepository.save(any<Supplier>())).then(returnsFirstArg<Supplier>())

        val result = supplierService.saveSupplier(testSupplierDto)

        assertEquals("Test Supplier", result.name)
        assertEquals("supplier@example.com", result.contactEmail)
        verify(supplierRepository, times(1)).save(any<Supplier>())
    }

    @Test
    @DisplayName("Should reject a supplier without name")
    fun testSaveSupplierBlankName() {
        val blank = SupplierDto("  ", "a@b.c", "1", "", emptyList())

        assertFailsWith<IllegalArgumentException> { supplierService.saveSupplier(blank) }
        verifyNoInteractions(supplierRepository)
    }

    @Test
    @DisplayName("Should throw exception when supplier name already exists")
    fun testSaveSupplierDuplicate() {
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        assertFailsWith<IllegalArgumentException> { supplierService.saveSupplier(testSupplierDto) }
        verify(supplierRepository, never()).save(any<Supplier>())
    }

    @Test
    @DisplayName("Should find supplier by name")
    fun testFindSupplierByName() {
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        val result = supplierService.findSupplierByName("Test Supplier")

        assertNotNull(result)
        assertEquals("Test Supplier", result.name)
        assertEquals("supplier@example.com", result.contactEmail)
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    fun testFindSupplierByNameNotFound() {
        whenever(supplierRepository.findSupplierByName("NotFound")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { supplierService.findSupplierByName("NotFound") }
    }

    @Test
    @DisplayName("Should update an existing supplier")
    fun testUpdateSupplier() {
        val updatedDto = SupplierDto(
            "Updated Supplier",
            "updated@example.com",
            "9876543210",
            "456 Oak St",
            emptyList(),
        )

        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)
        whenever(supplierRepository.findSupplierByName("Updated Supplier")).thenReturn(null)
        whenever(supplierRepository.save(any<Supplier>())).then(returnsFirstArg<Supplier>())

        val result = supplierService.updateSupplier("Test Supplier", updatedDto)

        assertEquals("Updated Supplier", result.name)
        assertEquals("456 Oak St", result.address)
        verify(supplierRepository, times(1)).save(any<Supplier>())
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent supplier")
    fun testUpdateSupplierNotFound() {
        whenever(supplierRepository.findSupplierByName("NotFound")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { supplierService.updateSupplier("NotFound", testSupplierDto) }
        verify(supplierRepository, never()).save(any<Supplier>())
    }

    @Test
    @DisplayName("Should delete an existing supplier")
    fun testDeleteSupplier() {
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        supplierService.deleteSupplier("Test Supplier")

        verify(supplierRepository, times(1)).delete(testSupplier)
    }

    @Test
    @DisplayName("A supplier without products returns an empty list instead of 404")
    fun testGetProductsBySupplierEmpty() {
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        assertTrue(supplierService.getProductsBySupplier("Test Supplier").isEmpty())
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent supplier")
    fun testDeleteSupplierNotFound() {
        whenever(supplierRepository.findSupplierByName("NotFound")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { supplierService.deleteSupplier("NotFound") }
        verify(supplierRepository, never()).delete(any<Supplier>())
    }
}
