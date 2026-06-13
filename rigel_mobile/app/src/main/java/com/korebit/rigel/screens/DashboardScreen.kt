package com.korebit.rigel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import com.korebit.rigel.ui.components.MobileBottomNavigation
import com.korebit.rigel.ui.components.MobileTopBar
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900

@Composable
fun DashboardScreen(
    username: String = "Usuario",
    onLogout: () -> Unit = {},
    onNavigateTo: (String) -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf("dashboard") }

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp)
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
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (currentRoute) {
                "dashboard" -> DashboardContent()
                "pos" -> POSContent()
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

@Composable
private fun DashboardContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Panel de Control",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Bienvenido a tu dashboard",
            fontSize = 14.sp,
            color = Slate500,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Placeholder for future dashboard content (e.g., charts, stats, etc.)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Contenido del dashboard aquí",
                fontSize = 14.sp,
                color = Slate500
            )
        }
    }
}

@Composable
private fun POSContent() {
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

