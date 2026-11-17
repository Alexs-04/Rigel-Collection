package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.enums.Category
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
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
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Unit tests for ProductService.
 * Tests product CRUD operations, supplier relations, and batch management.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var supplierRepository: SupplierRepository

    @Mock
    private lateinit var batchRepository: BatchRepository

    @Mock
    private lateinit var productSupplierRepository: ProductSupplierRepository

    @InjectMocks
    private lateinit var productService: ProductService

    private lateinit var testProduct: Product
    private lateinit var testSupplier: Supplier
    private lateinit var productRequest: ProductAddRequest

    @BeforeEach
    fun setUp() {
        testSupplier = Supplier(id = 1L, name = "Test Supplier", products = mutableListOf())

        testProduct = Product(
            id = 1L,
            name = "Test Product",
            barcode = "123456789",
            price = BigDecimal.valueOf(10.00),
            category = Category.SODA,
            description = "Test Description",
            suppliers = mutableListOf(),
        )

        productRequest = addRequest()
    }

    private fun addRequest(
        name: String = "Test Product",
        description: String = "Test Description",
        barcode: String = "123456789",
        category: String = Category.SODA.name,
        price: Double = 10.00,
        supplierPrice: Double = 25.00,
    ) = ProductAddRequest(
        name = name,
        description = description,
        barcode = barcode,
        category = category,
        price = price,
        stock = 0,
        imageUrl = null,
        cloudinaryPublicId = null,
        supplierName = "Test Supplier",
        supplierPrice = supplierPrice,
        batch = null,
        minStock = null,
    )

    @Test
    @DisplayName("Should retrieve all products")
    fun testGetAllProducts() {
        whenever(productRepository.findAll()).thenReturn(listOf(testProduct))

        val result = productService.getAllProducts()

        assertEquals(1, result.size)
        assertEquals("Test Product", result.first().name)
        verify(productRepository, times(1)).findAll()
    }

    @Test
    @DisplayName("Should save a new product")
    fun testSaveProduct() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty())
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)
        whenever(productRepository.save(any<Product>())).then(returnsFirstArg<Product>())

        val result = productService.saveProduct(productRequest)

        assertEquals("Test Product", result.name)
        assertEquals(1, result.suppliers.size)
        assertEquals(25.0, result.suppliers.first().supplyPrice)
        verify(productRepository, times(1)).save(any<Product>())
    }

    @Test
    @DisplayName("Should throw exception when product name already exists")
    fun testSaveProductDuplicateName() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))

        assertFailsWith<IllegalArgumentException> { productService.saveProduct(productRequest) }
        verify(productRepository, never()).save(any<Product>())
    }

    @Test
    @DisplayName("Should throw exception when barcode already exists")
    fun testSaveProductDuplicateBarcode() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("123456789")).thenReturn(Optional.of(testProduct))

        assertFailsWith<IllegalArgumentException> { productService.saveProduct(productRequest) }
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    fun testSaveProductSupplierNotFound() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty())
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { productService.saveProduct(productRequest) }
    }

    @Test
    @DisplayName("Should find product by name")
    fun testFindProductByName() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))

        val result = productService.findProductByName("Test Product")

        assertNotNull(result)
        assertEquals("Test Product", result.name)
    }

    @Test
    @DisplayName("Should throw exception when product not found by name")
    fun testFindProductByNameNotFound() {
        whenever(productRepository.findByName("NotFound")).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { productService.findProductByName("NotFound") }
    }

    @Test
    @DisplayName("Should update a product")
    fun testUpdateProduct() {
        val updatedRequest = addRequest(
            name = "Updated Product",
            description = "Updated Description",
            barcode = "987654321",
            category = Category.BEER.name,
            price = 15.00,
            supplierPrice = 30.00,
        )

        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))
        whenever(productRepository.findByName("Updated Product")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("987654321")).thenReturn(Optional.empty())
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)
        whenever(productRepository.save(any<Product>())).then(returnsFirstArg<Product>())

        val result = productService.updateProduct("Test Product", updatedRequest)

        assertEquals("Updated Product", result.name)
        assertEquals("BEER", result.category)
        assertEquals(30.0, result.suppliers.first().supplyPrice)
        verify(productRepository, times(1)).save(any<Product>())
    }

    @Test
    @DisplayName("Should delete a product")
    fun testDeleteProduct() {
        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))

        productService.deleteProduct("Test Product")

        verify(productRepository, times(1)).delete(testProduct)
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    fun testDeleteProductNotFound() {
        whenever(productRepository.findByName("NotFound")).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { productService.deleteProduct("NotFound") }
        verify(productRepository, never()).delete(any<Product>())
    }

    @Test
    @DisplayName("Should add relation to product")
    fun testAddRelationToProduct() {
        testProduct.suppliers = mutableListOf()
        val request = AddRelationRequest("Test Product", "Test Supplier", 25.00)

        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)
        whenever(productRepository.save(any<Product>())).then(returnsFirstArg<Product>())

        val result = productService.addRelationToProduct(request)

        assertEquals("Test Supplier", result.suppliers.first().name)
        verify(productRepository, times(1)).save(any<Product>())
    }

    @Test
    @DisplayName("Should reject a negative minimum stock")
    fun testUpdateMinStockNegative() {
        whenever(productRepository.findByBarcode("123456789")).thenReturn(Optional.of(testProduct))

        assertFailsWith<IllegalArgumentException> { productService.updateMinStock("123456789", -1) }
        verify(productRepository, never()).save(any<Product>())
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate supplier relation")
    fun testAddDuplicateRelation() {
        val existingRelation = ProductSupplier(supplier = testSupplier)
        testProduct.suppliers = mutableListOf(existingRelation)

        val request = AddRelationRequest("Test Product", "Test Supplier", 25.00)

        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct))
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        assertFailsWith<IllegalArgumentException> { productService.addRelationToProduct(request) }
    }

    @Test
    @DisplayName("Should throw exception for invalid category")
    fun testSaveProductInvalidCategory() {
        val invalidRequest = addRequest(category = "INVALID_CATEGORY")

        whenever(productRepository.findByName("Test Product")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty())
        whenever(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier)

        assertFailsWith<IllegalArgumentException> { productService.saveProduct(invalidRequest) }
        verify(productRepository, never()).save(any<Product>())
    }
}
