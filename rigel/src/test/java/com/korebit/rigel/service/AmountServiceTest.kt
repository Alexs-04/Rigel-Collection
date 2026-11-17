package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.AmountBuyoutRequest
import com.korebit.rigel.dto.request.AmountUpsertRequest
import com.korebit.rigel.enums.AmountStatus
import com.korebit.rigel.enums.ContainerType
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Amount
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.AmountRepository
import com.korebit.rigel.repository.ConsumerRepository
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
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Unit tests for AmountService.
 * Tests amount creation, updates, buyout operations, and returns.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("AmountService Tests")
class AmountServiceTest {

    @Mock
    private lateinit var amountRepository: AmountRepository

    @Mock
    private lateinit var consumerRepository: ConsumerRepository

    @InjectMocks
    private lateinit var amountService: AmountService

    private lateinit var testAmount: Amount
    private lateinit var testConsumer: Consumer
    private lateinit var createRequest: AmountUpsertRequest

    @BeforeEach
    fun setUp() {
        testConsumer = Consumer(id = 1L, username = "testuser", email = "test@example.com")

        testAmount = Amount(
            folio = 1L,
            description = "Test Amount",
            type = ContainerType.BEER_CONTAINER,
            customerName = "Test Customer",
            quantity = 5,
            saleUnitPrice = BigDecimal.valueOf(10.00),
            buyoutUnitPrice = BigDecimal.valueOf(5.00),
            total = BigDecimal.valueOf(50.00),
            created = LocalDateTime.now(),
            expirationDate = LocalDateTime.now().plusDays(7),
            returned = false,
            consumer = testConsumer,
            notes = "Test notes",
        )

        createRequest = upsertRequest()
    }

    private fun upsertRequest(
        description: String = "Test Description",
        type: ContainerType = ContainerType.BEER_CONTAINER,
        customerName: String = "Test Customer",
        quantity: Int = 5,
        saleUnitPrice: Double = 10.00,
        buyoutUnitPrice: Double = 5.00,
        expirationDate: LocalDateTime = LocalDateTime.now().plusDays(7),
        notes: String = "Test notes",
    ) = AmountUpsertRequest(
        description, type, customerName, quantity, saleUnitPrice, buyoutUnitPrice, expirationDate, notes,
    )

    @Test
    @DisplayName("Should retrieve all amounts")
    fun testGetAllAmounts() {
        whenever(amountRepository.findAll(any<Sort>())).thenReturn(listOf(testAmount))

        val result = amountService.getAllAmounts()

        assertEquals(1, result.size)
        assertEquals("Test Amount", result.first().description)
        verify(amountRepository, times(1)).findAll(any<Sort>())
    }

    @Test
    @DisplayName("Should get amount by folio")
    fun testGetAmountByFolio() {
        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))

        val result = amountService.getAmountByFolio(1L)

        assertNotNull(result)
        assertEquals("Test Amount", result.description)
    }

    @Test
    @DisplayName("Should throw exception when amount not found by folio")
    fun testGetAmountByFolioNotFound() {
        whenever(amountRepository.findById(anyLong())).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { amountService.getAmountByFolio(1L) }
    }

    @Test
    @DisplayName("Should create a new amount owned by the authenticated user")
    fun testCreateAmount() {
        whenever(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer)
        whenever(amountRepository.save(any<Amount>())).then(returnsFirstArg<Amount>())

        val result = amountService.createAmount(createRequest, "test@example.com")

        assertEquals("Test Customer", result.customerName)
        assertEquals(50.0, result.total)
        assertEquals(AmountStatus.ACTIVE, result.status)
        assertEquals(1L, result.ownerId)
        verify(amountRepository, times(1)).save(any<Amount>())
    }

    @Test
    @DisplayName("Should throw exception when creating amount with empty customer name")
    fun testCreateAmountEmptyCustomerName() {
        val invalidRequest = upsertRequest(customerName = "   ")

        assertFailsWith<IllegalArgumentException> {
            amountService.createAmount(invalidRequest, "test@example.com")
        }
        verifyNoInteractions(consumerRepository, amountRepository)
    }

    @Test
    @DisplayName("Should throw exception when creating amount with invalid quantity")
    fun testCreateAmountInvalidQuantity() {
        val invalidRequest = upsertRequest(quantity = 0)

        assertFailsWith<IllegalArgumentException> {
            amountService.createAmount(invalidRequest, "test@example.com")
        }
        verifyNoInteractions(consumerRepository, amountRepository)
    }

    @Test
    @DisplayName("Should throw exception when creating amount with consumer not found")
    fun testCreateAmountConsumerNotFound() {
        whenever(consumerRepository.findByEmail("notfound@example.com")).thenReturn(null)

        assertFailsWith<EntityNotFundException> {
            amountService.createAmount(createRequest, "notfound@example.com")
        }
        verify(amountRepository, never()).save(any<Amount>())
    }

    @Test
    @DisplayName("Should update an amount")
    fun testUpdateAmount() {
        val updateRequest = upsertRequest(
            description = "Updated Description",
            type = ContainerType.LARGE_GLASS_CONTAINER,
            customerName = "Updated Customer",
            quantity = 10,
            saleUnitPrice = 15.00,
            buyoutUnitPrice = 7.50,
            expirationDate = LocalDateTime.now().plusDays(14),
            notes = "Updated notes",
        )

        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))
        whenever(amountRepository.save(any<Amount>())).then(returnsFirstArg<Amount>())

        val result = amountService.updateAmount(1L, updateRequest)

        assertEquals("Updated Customer", result.customerName)
        assertEquals(ContainerType.LARGE_GLASS_CONTAINER, result.type)
        assertEquals(150.0, result.total)
        verify(amountRepository, times(1)).save(any<Amount>())
    }

    @Test
    @DisplayName("Should throw exception when updating closed amount")
    fun testUpdateClosedAmount() {
        testAmount.returned = true
        val updateRequest = upsertRequest(description = "Updated", customerName = "Customer", notes = "notes")

        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))

        assertFailsWith<IllegalArgumentException> { amountService.updateAmount(1L, updateRequest) }
    }

    @Test
    @DisplayName("Should mark amount as returned")
    fun testMarkAsReturned() {
        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))
        whenever(amountRepository.save(any<Amount>())).then(returnsFirstArg<Amount>())

        val result = amountService.markAsReturned(1L)

        assertEquals(AmountStatus.RETURNED, result.status)
        assertNotNull(result.returnedAt)
        verify(amountRepository, times(1)).save(any<Amount>())
    }

    @Test
    @DisplayName("Should throw exception when marking already returned amount")
    fun testMarkAlreadyReturnedAmount() {
        testAmount.returned = true

        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))

        assertFailsWith<IllegalArgumentException> { amountService.markAsReturned(1L) }
    }

    @Test
    @DisplayName("Should mark amount as bought out")
    fun testMarkAsBoughtOut() {
        testAmount.expirationDate = LocalDateTime.now().minusDays(1)
        val buyoutRequest = AmountBuyoutRequest(3.00, "Buyout notes")

        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))
        whenever(amountRepository.save(any<Amount>())).then(returnsFirstArg<Amount>())

        val result = amountService.markAsBoughtOut(1L, buyoutRequest)

        assertEquals(AmountStatus.BOUGHT_OUT, result.status)
        assertEquals(15.0, result.buyoutTotal)
        assertEquals("Test notes | Buyout notes", result.notes)
        verify(amountRepository, times(1)).save(any<Amount>())
    }

    @Test
    @DisplayName("Should throw exception when buying out non-expired amount")
    fun testMarkAsBoughtOutNonExpiredAmount() {
        testAmount.expirationDate = LocalDateTime.now().plusDays(7)
        val buyoutRequest = AmountBuyoutRequest(3.00, "Buyout notes")

        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))

        assertFailsWith<IllegalArgumentException> { amountService.markAsBoughtOut(1L, buyoutRequest) }
    }

    @Test
    @DisplayName("Should delete an amount")
    fun testDeleteAmount() {
        whenever(amountRepository.findById(1L)).thenReturn(Optional.of(testAmount))

        amountService.deleteAmount(1L)

        verify(amountRepository, times(1)).delete(testAmount)
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent amount")
    fun testDeleteAmountNotFound() {
        whenever(amountRepository.findById(anyLong())).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFundException> { amountService.deleteAmount(1L) }
    }

    @Test
    @DisplayName("Should get all container types")
    fun testGetContainerTypes() {
        val result = amountService.getContainerTypes()

        assertNotNull(result)
        assertFalse(result.isEmpty())
    }
}
