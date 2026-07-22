package com.korebit.rigel.service;

import com.korebit.rigel.dto.AmountDto;
import com.korebit.rigel.dto.request.AmountBuyoutRequest;
import com.korebit.rigel.dto.request.AmountCreateRequest;
import com.korebit.rigel.dto.request.AmountUpdateRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.enums.ContainerType;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Amount;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.repository.AmountRepository;
import com.korebit.rigel.repository.ConsumerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AmountService.
 * Tests amount creation, updates, buyout operations, and returns.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AmountService Tests")
class AmountServiceTest {

    @Mock
    private AmountRepository amountRepository;

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private AmountService amountService;

    private Amount testAmount;
    private Consumer testConsumer;
    private AmountCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setId(1L);
        testConsumer.setUsername("testuser");
        testConsumer.setEmail("test@example.com");

        testAmount = new Amount();
        testAmount.setFolio(1L);
        testAmount.setDescription("Test Amount");
        testAmount.setType(ContainerType.BEER_CONTAINER);
        testAmount.setCustomerName("Test Customer");
        testAmount.setQuantity(5);
        testAmount.setSaleUnitPrice(BigDecimal.valueOf(10.00));
        testAmount.setBuyoutUnitPrice(BigDecimal.valueOf(5.00));
        testAmount.setTotal(BigDecimal.valueOf(50.00));
        testAmount.setCreated(LocalDateTime.now());
        testAmount.setExpirationDate(LocalDateTime.now().plusDays(7));
        testAmount.setReturned(false);
        testAmount.setConsumer(testConsumer);
        testAmount.setNotes("Test notes");

        createRequest = new AmountCreateRequest(
                "Test Description",
                ContainerType.BEER_CONTAINER,
                "Test Customer",
                5,
                10.00,
                5.00,
                LocalDateTime.now().plusDays(7),
                "Test notes"
        );
    }

    @Test
    @DisplayName("Should retrieve all amounts")
    void testGetAllAmounts() {
        when(amountRepository.findAll()).thenReturn(Collections.singletonList(testAmount));

        var result = amountService.getAllAmounts();

        assertEquals(1, result.size());
        assertEquals("Test Amount", result.getFirst().getDescription());
        verify(amountRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get amount by folio")
    void testGetAmountByFolio() {
        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));

        AmountDto result = amountService.getAmountByFolio(1L);

        assertNotNull(result);
        assertEquals("Test Amount", result.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when amount not found by folio")
    void testGetAmountByFolioNotFound() {
        when(amountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> amountService.getAmountByFolio(1L));
    }

    @Test
    @DisplayName("Should create a new amount")
    void testCreateAmount() {
        when(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer);
        when(amountRepository.save(any())).thenReturn(testAmount);

        Response response = amountService.createAmount(createRequest, "testuser");

        assertTrue(response.getSuccess());
        assertEquals(201, response.getStatus());
        assertTrue(response.getMessage().contains("registrado correctamente"));
        verify(amountRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating amount with empty customer name")
    void testCreateAmountEmptyCustomerName() {
        AmountCreateRequest invalidRequest = new AmountCreateRequest(
                "Test Description",
                ContainerType.BEER_CONTAINER,
                "   ",
                5,
                10.00,
                5.00,
                LocalDateTime.now().plusDays(7),
                "Test notes"
        );

        when(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer);

        assertThrows(IllegalArgumentException.class,
                () -> amountService.createAmount(invalidRequest, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when creating amount with invalid quantity")
    void testCreateAmountInvalidQuantity() {
        AmountCreateRequest invalidRequest = new AmountCreateRequest(
                "Test Description",
                ContainerType.BEER_CONTAINER,
                "Test Customer",
                0,
                10.00,
                5.00,
                LocalDateTime.now().plusDays(7),
                "Test notes"
        );

        when(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer);

        assertThrows(IllegalArgumentException.class,
                () -> amountService.createAmount(invalidRequest, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when creating amount with consumer not found")
    void testCreateAmountConsumerNotFound() {
        when(consumerRepository.findByUsername("notfound")).thenReturn(null);

        assertThrows(EntityNotFundException.class,
                () -> amountService.createAmount(createRequest, "notfound"));
    }

    @Test
    @DisplayName("Should update an amount")
    void testUpdateAmount() {
        AmountUpdateRequest updateRequest = new AmountUpdateRequest(
                "Updated Description",
                ContainerType.LARGE_GLASS_CONTAINER,
                "Updated Customer",
                10,
                15.00,
                7.50,
                LocalDateTime.now().plusDays(14),
                "Updated notes"
        );

        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));
        when(amountRepository.save(any())).thenReturn(testAmount);

        Response response = amountService.updateAmount(1L, updateRequest);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(amountRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating closed amount")
    void testUpdateClosedAmount() {
        testAmount.setReturned(true);
        AmountUpdateRequest updateRequest = new AmountUpdateRequest(
                "Updated", ContainerType.BEER_CONTAINER, "Customer", 5, 10.0, 5.0,
                LocalDateTime.now().plusDays(7), "notes"
        );

        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));

        assertThrows(IllegalArgumentException.class,
                () -> amountService.updateAmount(1L, updateRequest));
    }

    @Test
    @DisplayName("Should mark amount as returned")
    void testMarkAsReturned() {
        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));
        when(amountRepository.save(any())).thenReturn(testAmount);

        Response response = amountService.markAsReturned(1L);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(amountRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when marking already returned amount")
    void testMarkAlreadyReturnedAmount() {
        testAmount.setReturned(true);

        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));

        assertThrows(IllegalArgumentException.class, () -> amountService.markAsReturned(1L));
    }

    @Test
    @DisplayName("Should mark amount as bought out")
    void testMarkAsBoughtOut() {
        testAmount.setExpirationDate(LocalDateTime.now().minusDays(1));
        AmountBuyoutRequest buyoutRequest = new AmountBuyoutRequest(3.00, "Buyout notes");

        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));
        when(amountRepository.save(any())).thenReturn(testAmount);

        Response response = amountService.markAsBoughtOut(1L, buyoutRequest);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertNotNull(testAmount.getBoughtOutAt());
        verify(amountRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when buying out non-expired amount")
    void testMarkAsBoughtOutNonExpiredAmount() {
        testAmount.setExpirationDate(LocalDateTime.now().plusDays(7));
        AmountBuyoutRequest buyoutRequest = new AmountBuyoutRequest(3.00, "Buyout notes");

        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));

        assertThrows(IllegalArgumentException.class,
                () -> amountService.markAsBoughtOut(1L, buyoutRequest));
    }

    @Test
    @DisplayName("Should delete an amount")
    void testDeleteAmount() {
        when(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount));
        doNothing().when(amountRepository).delete(any());

        Response response = amountService.deleteAmount(1L);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(amountRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent amount")
    void testDeleteAmountNotFound() {
        when(amountRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFundException.class, () -> amountService.deleteAmount(1L));
    }

    @Test
    @DisplayName("Should get all container types")
    void testGetContainerTypes() {
        var result = amountService.getContainerTypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
