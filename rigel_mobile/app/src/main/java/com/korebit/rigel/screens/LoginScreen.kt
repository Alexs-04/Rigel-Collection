package com.korebit.rigel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korebit.rigel.presentation.viewmodel.LoginViewModel
import com.korebit.rigel.ui.components.PrimaryButton
import com.korebit.rigel.ui.components.SecondaryButton
import com.korebit.rigel.ui.components.RigelTextField
import com.korebit.rigel.ui.theme.BrandPrimary
import com.korebit.rigel.ui.theme.Slate500
import com.korebit.rigel.ui.theme.Slate900

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onLoginError: (String) -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Observe the isLoggedIn flag and navigate when it becomes true.
    LaunchedEffect(Unit) {
        snapshotFlow { uiState.isLoggedIn }.collect { loggedIn ->
            if (loggedIn) onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.95f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Card Content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Header with logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = BrandPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rigel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                    }

                    // Title
                    Text(
                        text = "Bienvenido de vuelta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Subtitle
                    Text(
                        text = "Ingresa con tu cuenta para continuar",
                        fontSize = 14.sp,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email Field
                    Text(
                        text = "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate900,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    RigelTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = "Email",
                        placeholder = "correo@ejemplo.com",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Email",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    // Password Field
                    Text(
                        text = "Contraseña",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate900,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    RigelTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        isPassword = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Contraseña",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    // Error Message
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryButton(
                            text = "Ingresar",
                            onClick = {
                                viewModel.login()
                            },
                            enabled = !uiState.isLoading,
                            isLoading = uiState.isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        SecondaryButton(
                            text = "Limpiar",
                            onClick = {
                                viewModel.clearForm()
                            },
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    // Forgot Password Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.width(0.dp))
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            fontSize = 12.sp,
                            color = BrandPrimary
                        )
                    }
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Rigel",
                fontSize = 12.sp,
                color = Slate500,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}



