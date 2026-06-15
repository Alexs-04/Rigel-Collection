package com.korebit.rigel.screens

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.korebit.rigel.data.api.DashboardSnapshotDto
import com.korebit.rigel.data.repository.DashboardRepository
import com.korebit.rigel.ui.components.MobileBottomNavigation
import com.korebit.rigel.ui.components.MobileTopBar
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

/**
 * Dashboard screen that communicates with the backend to fetch a dashboard snapshot.
 * - `username` and `role` may be provided by the caller (login flow) so the screen can
 *   adapt (for example to hide POS for SUPPLIER role).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    username: String = "Usuario",
    role: String? = null,
    onLogout: () -> Unit = {},
    onNavigateTo: (String) -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf("dashboard") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Top Bar
        MobileTopBar(
            onLogout = onLogout,
            username = username
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE2E8F0)
        )

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF8FAFC))
                .padding(12.dp)
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardContent()
                "pos" -> POSContent(role = role)
                "settings" -> SettingsContent()
                else -> DashboardContent()
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE2E8F0)
        )

        // Bottom Navigation
        MobileBottomNavigation(
            currentRoute = currentRoute,
            onNavigate = { route ->
                currentRoute = route
                onNavigateTo(route)
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardContent() {
    val repository = remember { DashboardRepository() }
    val scope = rememberCoroutineScope()

    var snapshot by remember { mutableStateOf<DashboardSnapshotDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    // load initially and when selectedDate changes
    LaunchedEffect(selectedDate) {
        loading = true
        error = ""
        val res = repository.fetchSnapshot(date = selectedDate, limit = 5)
        res.onSuccess { data ->
            snapshot = data
        }
        res.onFailure { e ->
            error = e.message ?: "No se pudo cargar la informacion del dashboard."
        }
        loading = false
    }

    val scroll = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(12.dp)
    ) {
        Text(
            text = "Panel de Control",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = "Visualiza el estado general del negocio.",
            fontSize = 14.sp,
            color = Slate500,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Controls: date and refresh
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Button(onClick = {
                // show native date picker
                val parts = selectedDate.split("-").mapNotNull { it.toIntOrNull() }
                val cal = Calendar.getInstance()
                if (parts.size >= 3) {
                    cal.set(parts[0], parts[1] - 1, parts[2])
                }

                val picker = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val newDate = LocalDate.of(year, month + 1, day)
                        scope.launch { selectedDate = newDate.toString() }
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                picker.show()
            }) {
                Text(text = "Seleccionar fecha")
            }

            Button(onClick = {
                scope.launch {
                    loading = true
                    error = ""
                    val res = repository.fetchSnapshot(date = selectedDate, limit = 5)
                    res.onSuccess { snapshot = it }
                    res.onFailure { error = it.message ?: "Error loading dashboard" }
                    loading = false
                }
            }) {
                Text(text = if (loading) "Cargando..." else "Actualizar")
            }

            Text(
                text = "Fecha backend: ${snapshot?.referenceDate ?: "-"}",
                color = Slate500,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (error.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFECEF))
                    .padding(12.dp)
            ) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }

        // Stats
        val stats = snapshot?.stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Productos",
                value = stats?.totalProducts?.toString() ?: "-",
                modifier = Modifier.weight(1f) // Aquí es donde declaras el peso
            )
            StatCard(
                title = "Proveedores",
                value = stats?.totalSuppliers?.toString() ?: "-",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Lotes",
                value = stats?.totalBatches?.toString() ?: "-",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tickets",
                value = stats?.totalTickets?.toString() ?: "-",
                modifier = Modifier.weight(1f)
            )
        }

        // Sales summary
        val sales = snapshot?.sales
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
                .padding(top = 12.dp)
        ) {
            Column {
                Text(text = "Ventas", fontWeight = FontWeight.SemiBold, color = Slate900)
                Text(text = "Hoy: ${sales?.day ?: 0.0}", color = Slate500)
                Text(text = "Mes: ${sales?.month ?: 0.0}", color = Slate500)
                Text(text = "Año: ${sales?.year ?: 0.0}", color = Slate500)
            }
        }

        // Top lists
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = "Productos más vendidos",
                fontWeight = FontWeight.SemiBold,
                color = Slate900
            )
            if (snapshot?.topProducts.isNullOrEmpty()) {
                Text(
                    text = "No hay ventas registradas para el periodo seleccionado.",
                    color = Slate500
                )
            } else {
                snapshot?.topProducts?.forEach { item ->
                    Text(text = "${item.name} — ${item.total}", color = Slate500)
                }
            }

            Text(
                text = "Proveedores con más ventas",
                fontWeight = FontWeight.SemiBold,
                color = Slate900,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (snapshot?.topSuppliers.isNullOrEmpty()) {
                Text(
                    text = "No hay proveedores con ventas para el periodo seleccionado.",
                    color = Slate500
                )
            } else {
                snapshot?.topSuppliers?.forEach { item ->
                    Text(text = "${item.name} — ${item.total}", color = Slate500)
                }
            }
        }
    }
}


@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier // Añadimos el modifier como parámetro
) {
    Box(
        modifier = modifier // Aplicamos el modifier que viene del padre
            .background(Color.White)
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 12.sp, color = Slate500)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Slate900)
        }
    }
}


@Composable
private fun POSContent(role: String? = null) {
    // Access control: SUPPLIER role doesn't have access to POS
    val canAccessPos = role != "SUPPLIER"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Punto de Venta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!canAccessPos) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Acceso denegado. El rol SUPPLIER no tiene acceso al punto de venta.",
                    fontSize = 14.sp,
                    color = Slate500
                )
            }
            return
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Funcionalidad de POS próximamente",
                fontSize = 14.sp,
                color = Slate500
            )
        }
    }
}


@Composable
private fun SettingsContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Opciones de configuración próximamente",
                fontSize = 14.sp,
                color = Slate500
            )
        }
    }
}

