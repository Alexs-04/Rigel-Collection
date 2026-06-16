package com.korebit.rigel.ui.components.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korebit.rigel.data.api.PosTicketDto
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import java.util.Locale

@Composable
fun PosTicketsPanel(
    tickets: List<PosTicketDto> = emptyList(),
    loading: Boolean = false,
    onReload: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp).padding(top = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Últimos Tickets", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                Button(onClick = onReload) { Text("Actualizar") }
            }

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tickets.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No hay tickets registrados", color = Slate500, fontSize = 14.sp)
                }
            } else {
                // Límite de altura añadido
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(tickets) { ticket ->
                        TicketItemRow(ticket = ticket)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketItemRow(ticket: PosTicketDto) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp).padding(bottom = 8.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Ticket: ${ticket.barcode ?: "N/A"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                Text(text = "\$${String.format(Locale.US, "%.2f", ticket.totalAmount ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
            Text(text = "Fecha: ${ticket.dateAndTime ?: "-"}", fontSize = 12.sp, color = Slate500, modifier = Modifier.padding(top = 4.dp))
            Text(text = "Método: ${ticket.payment ?: "-"}", fontSize = 12.sp, color = Slate500)
            Text(text = "Items: ${ticket.products?.size ?: 0}", fontSize = 12.sp, color = Slate500)
        }
    }
}