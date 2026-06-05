package com.korebit.rigel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.korebit.rigel.screens.LoginScreen
import com.korebit.rigel.ui.theme.RigelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RigelTheme {
                LoginScreen(
                    onLoginSuccess = {
                        // TODO: Navegar al dashboard
                    }
                )
            }
        }
    }
}
