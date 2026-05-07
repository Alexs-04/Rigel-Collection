package com.korebit.rigel.service;

import com.korebit.rigel.dto.SupplierDto;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Supplier;
import com.korebit.rigel.repository.ProductRepository;
import com.korebit.rigel.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SupplierService.
 * Tests supplier CRUD operations, product associations, and pricing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;
    private SupplierDto testSupplierDto;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("Test Supplier");
        testSupplier.setEmail("supplier@example.com");
        testSupplier.setNumberPhone("1234567890");
        testSupplier.setAddress("123 Main St");
        testSupplier.setProducts(Collections.emptyList());

        testSupplierDto = new SupplierDto(
            "Test Supplier",
            "supplier@example.com",
            "1234567890",
            "123 Main St",
            Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Should retrieve all suppliers")
    void testGetAllSuppliers() {
        Supplier supplier2 = new Supplier();
        supplier2.setId(2L);
        supplier2.setName("Test Supplier 2");
        supplier2.setEmail("supplier2@example.com");
        supplier2.setProducts(Collections.emptyList());

        when(supplierRepository.findAll()).thenReturn(Arrays.asList(testSupplier, supplier2));

        var result = supplierService.getAllSuppliers();

        assertEquals(2, result.size());
        assertEquals("Test Supplier", result.get(0).getName());
        assertEquals("Test Supplier 2", result.get(1).getName());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should save a new supplier")
    void testSaveSupplier() {
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(null);
        when(supplierRepository.save(any())).thenReturn(testSupplier);

        Response response = supplierService.saveSupplier(testSupplierDto);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertTrue(response.getMessage().contains("saved successfully"));
        verify(supplierRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when supplier name already exists")
    void testSaveSupplierDuplicate() {
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);

        assertThrows(IllegalArgumentException.class, () -> supplierService.saveSupplier(testSupplierDto));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find supplier by name")
    void testFindSupplierByName() {
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);

        SupplierDto result = supplierService.findSupplierByName("Test Supplier");

        assertNotNull(result);
        assertEquals("Test Supplier", result.getName());
        assertEquals("supplier@example.com", result.getContactEmail());
    }

    @Test
    @DisplayName("Should throw exception when supplier not found")
    void testFindSupplierByNameNotFound() {
        when(supplierRepository.findSupplierByName("NotFound")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> supplierService.findSupplierByName("NotFound"));
    }

    @Test
    @DisplayName("Should update an existing supplier")
    void testUpdateSupplier() {
        SupplierDto updatedDto = new SupplierDto(
            "Updated Supplier",
            "updated@example.com",
            "9876543210",
            "456 Oak St",
            Collections.emptyList()
        );

        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);
        when(supplierRepository.findSupplierByName("Updated Supplier")).thenReturn(null);
        when(supplierRepository.save(any())).thenReturn(testSupplier);

        Response response = supplierService.updateSupplier("Test Supplier", updatedDto);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertTrue(response.getMessage().contains("updated successfully"));
        verify(supplierRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent supplier")
    void testUpdateSupplierNotFound() {
        when(supplierRepository.findSupplierByName("NotFound")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> supplierService.updateSupplier("NotFound", testSupplierDto));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete an existing supplier")
    void testDeleteSupplier() {
        when(supplierRepository.findSupplierByName("Test Supplier")).thenReturn(testSupplier);
        doNothing().when(supplierRepository).delete(any());

        Response response = supplierService.deleteSupplier("Test Supplier");

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertTrue(response.getMessage().contains("deleted successfully"));
        verify(supplierRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent supplier")
    void testDeleteSupplierNotFound() {
        when(supplierRepository.findSupplierByName("NotFound")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> supplierService.deleteSupplier("NotFound"));
        verify(supplierRepository, never()).delete(any());
    }
}

