package com.example.ovagrown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ovagrown.navigation.AppNavigation
import com.example.ovagrown.ui.theme.OVAgrownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OVAgrownTheme {
                AppNavigation()
            }
        }
    }
}


