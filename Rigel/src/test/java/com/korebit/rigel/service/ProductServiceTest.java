package com.korebit.rigel.service;

import com.korebit.rigel.dto.ProductDto;
import com.korebit.rigel.dto.request.AddRelationRequest;
import com.korebit.rigel.dto.request.ProductAddRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.enums.Category;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Product;
import com.korebit.rigel.model.beans.Supplier;
import com.korebit.rigel.model.extra.ProductSupplier;
import com.korebit.rigel.repository.BatchRepository;
import com.korebit.rigel.repository.ProductRepository;
import com.korebit.rigel.repository.ProductSupplierRepository;
import com.korebit.rigel.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Tests product CRUD operations, supplier relations, and batch management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductSupplierRepository productSupplierRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Supplier testSupplier;
    private ProductAddRequest productRequest;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("Test Supplier");
        testSupplier.setProducts(Collections.emptyList());

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setBarcode("123456789");
        testProduct.setPrice(BigDecimal.valueOf(10.00));
        testProduct.setCategory(Category.SODA);
        testProduct.setDescription("Test Description");
        testProduct.setSuppliers(Collections.emptyList());

        productRequest = new ProductAddRequest(
            "Test Product",
            "Test Description",
            "123456789",
            Category.SODA.name(),
            10.00,
            0,
            null,
            "Test Supplier",
            25.00,
            null
        );
    }

    @Test
    @DisplayName("Should retrieve all products")
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(testProduct));

        var result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Test Product", result.getFirst().getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save a new product")
    void testSaveProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty());
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);
        when(batchRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenReturn(testProduct);

        Response response = productService.saveProduct(productRequest);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertTrue(response.getMessage().contains("saved successfully"));
        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product name already exists")
    void testSaveProductDuplicateName() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalArgumentException.class, () -> productService.saveProduct(productRequest));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when barcode already exists")
    void testSaveProductDuplicateBarcode() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("123456789")).thenReturn(Optional.of(testProduct));

        assertThrows(IllegalArgumentException.class, () -> productService.saveProduct(productRequest));
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    void testSaveProductSupplierNotFound() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty());
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> productService.saveProduct(productRequest));
    }

    @Test
    @DisplayName("Should find product by name")
    void testFindProductByName() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));

        ProductDto result = productService.findProductByName("Test Product");

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when product not found by name")
    void testFindProductByNameNotFound() {
        when(productRepository.findByName("NotFound")).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> productService.findProductByName("NotFound"));
    }

    @Test
    @DisplayName("Should update a product")
    void testUpdateProduct() {
        ProductAddRequest updatedRequest = new ProductAddRequest(
            "Updated Product",
            "Updated Description",
            "987654321",
            Category.BEER.name(),
            15.00,
            0,
            null,
            "Test Supplier",
            30.00,
            null
        );

        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));
        when(productRepository.findByName("Updated Product")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("987654321")).thenReturn(Optional.empty());
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);
        when(productRepository.save(any())).thenReturn(testProduct);

        Response response = productService.updateProduct("Test Product", updatedRequest);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should delete a product")
    void testDeleteProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any());

        Response response = productService.deleteProduct("Test Product");

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(productRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProductNotFound() {
        when(productRepository.findByName("NotFound")).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> productService.deleteProduct("NotFound"));
        verify(productRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should add relation to product")
    void testAddRelationToProduct() {
        testProduct.setSuppliers(Collections.emptyList());
        AddRelationRequest request = new AddRelationRequest("Test Product", "Test Supplier", 25.00);

        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);
        when(productRepository.save(any())).thenReturn(testProduct);

        Response response = productService.addRelationToProduct(request);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate supplier relation")
    void testAddDuplicateRelation() {
        ProductSupplier existingRelation = new ProductSupplier();
        existingRelation.setSupplier(testSupplier);
        testProduct.setSuppliers(Collections.singletonList(existingRelation));

        AddRelationRequest request = new AddRelationRequest("Test Product", "Test Supplier", 25.00);

        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);

        assertThrows(IllegalArgumentException.class, () -> productService.addRelationToProduct(request));
    }

    @Test
    @DisplayName("Should throw exception for invalid category")
    void testSaveProductInvalidCategory() {
        ProductAddRequest invalidRequest = new ProductAddRequest(
            "Test Product",
            "Test Description",
            "123456789",
            "INVALID_CATEGORY",
            10.00,
            0,
            null,
            "Test Supplier",
            25.00,
            null
        );

        when(productRepository.findByName("Test Product")).thenReturn(Optional.empty());
        when(productRepository.findByBarcode("123456789")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productService.saveProduct(invalidRequest));
    }
}

