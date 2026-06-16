package com.korebit.rigel.ui.components.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korebit.rigel.data.api.PosCartItemDto
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import java.util.Locale

@Composable
fun PosCartPanel(
    items: List<PosCartItemDto> = emptyList(),
    onUpdateItem: (PosCartItemDto) -> Unit = {},
    onRemoveItem: (String) -> Unit = {},
    onClear: () -> Unit = {}
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
                Text(text = "Carrito (${items.size} items)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                if (items.isNotEmpty()) {
                    Button(onClick = onClear) { Text("Limpiar") }
                }
            }

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "El carrito está vacío", color = Slate500, fontSize = 14.sp)
                }
            } else {
                // Límite de altura añadido
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(items) { item ->
                        CartItemRow(item = item, onUpdate = onUpdateItem, onRemove = onRemoveItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: PosCartItemDto, onUpdate: (PosCartItemDto) -> Unit = {}, onRemove: (String) -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp).padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.productName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
            Text(text = "Cant: ${item.quantity} | \$${String.format(Locale.US, "%.2f", item.price * item.quantity)}", fontSize = 12.sp, color = Slate500)
        }
        Button(onClick = { onRemove(item.productId) }, modifier = Modifier.padding(start = 8.dp)) {
            Text("Quitar")
        }
    }
}