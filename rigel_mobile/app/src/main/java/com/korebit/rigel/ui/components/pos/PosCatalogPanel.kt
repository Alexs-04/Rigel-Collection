package com.korebit.rigel.ui.components.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.korebit.rigel.data.api.PosCatalogItemUI
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import java.util.Locale

@Composable
fun PosCatalogPanel(
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    loading: Boolean = false,
    error: String = "",
    products: List<PosCatalogItemUI> = emptyList(),
    onAddProduct: (PosCatalogItemUI) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp)
    ) {
        Column {
            Text(text = "Catálogo de Productos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            TextField(
                value = search,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                placeholder = { Text("Buscar producto...") },
                singleLine = true
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Límite de altura para no romper el scroll externo
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(products) { product ->
                        ProductItemRow(product = product, onAddClick = { onAddProduct(product) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductItemRow(product: PosCatalogItemUI, onAddClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp).padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
            Text(text = "Stock: ${product.availableUnits} | \$${String.format(Locale.US, "%.2f", product.price)}", fontSize = 12.sp, color = Slate500)
        }
        Button(onClick = onAddClick, modifier = Modifier.padding(start = 8.dp)) {
            Text("Agregar", fontSize = 12.sp)
        }
    }
}