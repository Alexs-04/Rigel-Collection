package com.korebit.rigel.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.korebit.rigel.data.api.PosCatalogItemUI
import com.korebit.rigel.data.api.PosService
import com.korebit.rigel.data.api.PosTicketCreatePayloadDto
import com.korebit.rigel.data.api.PosTicketDto
import com.korebit.rigel.data.api.RetrofitClient
import java.time.LocalDate

class PosRepository {

    private val service: PosService = RetrofitClient.posService

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchProducts(): Result<List<PosCatalogItemUI>> = try {
        val response = service.getAllProducts()

        if (response.isSuccessful) {
            val products = response.body() ?: emptyList()
            val todayIso = LocalDate.now().toString()


            val catalogItems = products.map { product ->

                val availableUnits = product.suppliers?.sumOf { supplier ->
                    supplier.batches?.filter { batch ->
                        batch.available == true &&
                                (batch.remainingAmount ?: 0) > 0 &&
                                (batch.expirationDate ?: "") >= todayIso
                    }?.sumOf { it.remainingAmount ?: 0 } ?: 0L
                } ?: 0L

                PosCatalogItemUI(
                    barcode = product.barcode ?: "",
                    name = product.name ?: "",
                    description = product.description ?: "",
                    category = product.category ?: "OTHERS",
                    price = product.price ?: 0.0,
                    imageUrl = product.imageUrl ?: "",
                    availableUnits = availableUnits
                )
            }
            Result.success(catalogItems)
        } else {
            val msg = response.errorBody()?.string() ?: "Unknown error"
            Result.failure(Exception(msg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitSale(payload: PosTicketCreatePayloadDto): Result<Unit> = try {
        val response = service.createTicket(payload)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val msg = response.errorBody()?.string() ?: "Unknown error"
            Result.failure(Exception(msg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchTickets(): Result<List<PosTicketDto>> = try {
        val response = service.getTickets()
        if (response.isSuccessful) {
            val body = response.body() ?: emptyList()
            Result.success(body)
        } else {
            val msg = response.errorBody()?.string() ?: "Unknown error"
            Result.failure(Exception(msg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}