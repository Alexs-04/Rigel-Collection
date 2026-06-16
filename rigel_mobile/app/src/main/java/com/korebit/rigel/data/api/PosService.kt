package com.korebit.rigel.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PosService {
    @GET("product/all")
    suspend fun getAllProducts(): Response<List<ProductSummaryDto>>

    @POST("tickets")
    suspend fun createTicket(
        @Body request: PosTicketCreatePayloadDto
    ): Response<Void>

    @GET("tickets")
    suspend fun getTickets(): Response<List<PosTicketDto>>
}

data class ProductSummaryDto(
    val name: String?,
    val barcode: String?,
    val description: String?,
    val category: String?,
    val price: Double?,
    val imageUrl: String?,
    val suppliers: List<SupplierDto>?
)

data class SupplierDto(
    val batches: List<BatchDto>?
)

data class BatchDto(
    val available: Boolean?,
    val remainingAmount: Long?,
    val expirationDate: String?
)

data class PosCatalogItemUI(
    val barcode: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val availableUnits: Long
)

data class PosTicketCreatePayloadDto(
    val description: String,
    val dateAndTime: String,
    val products: List<TicketProductCreateDto>,
    val currentConsumerEmail: String,
    val methodPayment: String
)

data class TicketProductCreateDto(
    val barcode: String,
    val quantity: Long,
    val price: Double,
    val discount: Double
)

data class PosTicketDto(
    val consumer: String?,
    val barcode: String?,
    val description: String?,
    val dateAndTime: String?,
    val totalAmount: Double?,
    val products: List<PosTicketDetailDto>?,
    val payment: String?
)

data class PosTicketDetailDto(
    val barcode: String?,
    val quantity: Long?,
    val price: Double?,
    val discount: Double?,
    val batchCode: String?,
    val productName: String?
)

data class PosCartItemDto(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Long
)