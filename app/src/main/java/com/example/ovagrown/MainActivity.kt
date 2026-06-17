package com.example.ovagrown

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.ovagrown.navigation.AppNavigation
import com.example.ovagrown.ui.theme.OVAgrownTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Run backend integration test
        lifecycleScope.launch {
            Log.d("TEST", "MainActivity started test runner")
            TestRunner.runFullSystemTest()
        }

        setContent {
            OVAgrownTheme {
                AppNavigation()
            }
        }
    }
}

