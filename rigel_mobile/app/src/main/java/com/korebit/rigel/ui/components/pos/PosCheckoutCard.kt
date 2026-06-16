package com.korebit.rigel.ui.components.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import java.util.Locale

data class PosCheckoutTotals(
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0
)

@Composable
fun PosCheckoutCard(
    description: String = "",
    onDescriptionChange: (String) -> Unit = {},
    methodPayment: String = "CASH",
    onMethodPaymentChange: (String) -> Unit = {},
    totals: PosCheckoutTotals = PosCheckoutTotals(),
    message: String = "",
    submitting: Boolean = false,
    onSubmit: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "Resumen de Compra",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Totals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Subtotal:", color = Slate500)
                    Text("Impuesto:", color = Slate500)
                    Text("Total:", fontWeight = FontWeight.Bold, color = Slate900)
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text("\$${String.format(Locale.US, "%.2f", totals.subtotal)}", color = Slate500)
                    Text("\$${String.format(Locale.US, "%.2f", totals.tax)}", color = Slate500)
                    Text(
                        "\$${String.format(Locale.US, "%.2f", totals.total)}",
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            // Description
            TextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Descripción (opcional)") },
                singleLine = false,
                maxLines = 3
            )

            // Payment method
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Método de pago: $methodPayment")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("CASH", "CARD", "CHECK", "TRANSFER").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                onMethodPaymentChange(method)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Message
            if (message.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message, color = Color.Green)
                }
            }

            // Submit button
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !submitting
            ) {
                Text(if (submitting) "Procesando..." else "Completar Venta")
            }
        }
    }
}



