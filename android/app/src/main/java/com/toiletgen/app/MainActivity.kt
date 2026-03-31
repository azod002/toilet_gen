package com.toiletgen.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.toiletgen.app.navigation.AppNavigation
import com.toiletgen.app.ui.SplashOverlay
import com.toiletgen.core.ui.theme.ToiletGenTheme

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Разрешения обработаны — карта подхватит автоматически
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Запрашиваем разрешение на геолокацию
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        setContent {
            ToiletGenTheme {
                var showSplash by remember { mutableStateOf(true) }

                Box(Modifier.fillMaxSize()) {
                    AppNavigation()

                    if (showSplash) {
                        SplashOverlay(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}
