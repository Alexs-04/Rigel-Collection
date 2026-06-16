package com.korebit.rigel.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korebit.rigel.data.api.PosCartItemDto
import com.korebit.rigel.data.api.PosCatalogItemUI
import com.korebit.rigel.data.api.PosTicketCreatePayloadDto
import com.korebit.rigel.data.api.PosTicketDto
import com.korebit.rigel.data.api.TicketProductCreateDto
import com.korebit.rigel.data.repository.PosRepository
import com.korebit.rigel.ui.components.pos.PosCartPanel
import com.korebit.rigel.ui.components.pos.PosCatalogPanel
import com.korebit.rigel.ui.components.pos.PosCheckoutCard
import com.korebit.rigel.ui.components.pos.PosCheckoutTotals
import com.korebit.rigel.ui.components.pos.PosTicketsPanel
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun POSContent(role: String? = null) {
    val canAccessPos = role != "SUPPLIER"

    if (!canAccessPos) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "Punto de Venta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Acceso denegado. El rol SUPPLIER no tiene acceso al punto de venta.",
                    fontSize = 14.sp,
                    color = Slate500
                )
            }
        }
        return
    }

    val repository = remember { PosRepository() }
    val scope = rememberCoroutineScope()

    // Usamos el nuevo modelo de UI
    var catalog by remember { mutableStateOf<List<PosCatalogItemUI>>(emptyList()) }
    var cart by remember { mutableStateOf<List<PosCartItemDto>>(emptyList()) }
    var tickets by remember { mutableStateOf<List<PosTicketDto>>(emptyList()) }

    var search by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var methodPayment by remember { mutableStateOf("CASH") }

    var loadingCatalog by remember { mutableStateOf(false) }
    var loadingTickets by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var checkoutMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loadingCatalog = true
        repository.fetchProducts().onSuccess { catalog = it }.onFailure { error = it.message ?: "Error loading products" }
        loadingCatalog = false

        loadingTickets = true
        repository.fetchTickets().onSuccess { tickets = it }.onFailure { error = it.message ?: "Error loading tickets" }
        loadingTickets = false
    }

    val totals = remember(cart) {
        val subtotal = cart.sumOf { it.price * it.quantity }
        PosCheckoutTotals(
            subtotal = subtotal,
            tax = subtotal * 0.1,
            total = subtotal * 1.1
        )
    }

    // Actualizado para usar barcode y PosCatalogItemUI
    fun addToCart(product: PosCatalogItemUI) {
        val existingItem = cart.find { it.productId == product.barcode }
        if (existingItem != null) {
            cart = cart.map {
                if (it.productId == product.barcode) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            cart = cart + PosCartItemDto(
                productId = product.barcode,
                productName = product.name,
                price = product.price,
                quantity = 1
            )
        }
    }

    fun removeFromCart(productId: String) { cart = cart.filter { it.productId != productId } }

    fun clearCart() {
        cart = emptyList()
        description = ""
        methodPayment = "CASH"
        checkoutMessage = ""
    }

    fun submitSale() {
        if (cart.isEmpty()) {
            error = "El carrito está vacío"
            return
        }

        scope.launch {
            submitting = true
            checkoutMessage = ""
            error = ""

            // Armamos el payload con la nueva estructura
            val payload = PosTicketCreatePayloadDto(
                description = description,
                dateAndTime = LocalDateTime.now().toString(),
                currentConsumerEmail = "general@cliente.com",
                methodPayment = methodPayment,
                products = cart.map {
                    TicketProductCreateDto(
                        barcode = it.productId,
                        quantity = it.quantity,
                        price = it.price,
                        discount = 0.0
                    )
                }
            )

            val res = repository.submitSale(payload)

            res.onSuccess {
                checkoutMessage = "Venta completada."
                clearCart()
                repository.fetchTickets().onSuccess { tickets = it }
            }
            res.onFailure { e -> error = e.message ?: "Error al registrar la venta" }
            submitting = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Punto de Venta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text(
            text = "Registra ventas en tiempo real.",
            fontSize = 14.sp, color = Slate500, modifier = Modifier.padding(bottom = 12.dp)
        )

        if (error.isNotBlank()) {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFECEF)).padding(12.dp)) {
                Text(text = error, color = Color.Red, fontSize = 14.sp)
            }
        }

        PosCatalogPanel(
            search = search,
            onSearchChange = { search = it },
            loading = loadingCatalog,
            error = error,
            products = catalog.filter { it.name.contains(search, ignoreCase = true) || it.barcode.contains(search) },
            onAddProduct = { addToCart(it) }
        )

        PosCartPanel(
            items = cart,
            onRemoveItem = { removeFromCart(it) },
            onClear = { clearCart() }
        )

        PosCheckoutCard(
            description = description,
            onDescriptionChange = { description = it },
            methodPayment = methodPayment,
            onMethodPaymentChange = { methodPayment = it },
            totals = totals,
            message = checkoutMessage,
            submitting = submitting,
            onSubmit = { submitSale() }
        )

        PosTicketsPanel(
            tickets = tickets,
            loading = loadingTickets,
            onReload = {
                scope.launch {
                    loadingTickets = true
                    repository.fetchTickets().onSuccess { tickets = it }
                    loadingTickets = false
                }
            }
        )
    }
}