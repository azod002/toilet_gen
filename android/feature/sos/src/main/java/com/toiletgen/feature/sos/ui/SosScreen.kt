package com.toiletgen.feature.sos.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.ui.theme.SosRed
import com.toiletgen.feature.sos.viewmodel.SosViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SosScreen(
    userLat: Double,
    userLon: Double,
    onClose: () -> Unit,
    viewModel: SosViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.requestSos(userLat, userLon) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2D0000),
                        Color(0xFF1A0000),
                        Color.Black,
                    ),
                    radius = 800f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Кнопка закрытия
        IconButton(
            onClick = { viewModel.reset(); onClose() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f),
            ) {
                Icon(
                    Icons.Default.Close, "Закрыть",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Пульсирующий индикатор
            if (uiState.isSearching) {
                PulsingIndicator()
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = SosRed.copy(alpha = 0.2f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = SosRed, modifier = Modifier.size(56.dp))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "SOS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = SosRed,
                letterSpacing = 8.sp,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                uiState.statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            uiState.error?.let { error ->
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SosRed.copy(alpha = 0.15f),
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFFF8A80),
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.requestSos(userLat, userLon) },
                    colors = ButtonDefaults.buttonColors(containerColor = SosRed),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Повторить", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PulsingIndicator() {
    val transition = rememberInfiniteTransition(label = "sos")

    // Три кольца с разным таймингом
    val scale1 by transition.animateFloat(
        0.6f, 2.0f,
        infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "s1",
    )
    val alpha1 by transition.animateFloat(
        0.4f, 0f,
        infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "a1",
    )

    val scale2 by transition.animateFloat(
        0.6f, 2.0f,
        infiniteRepeatable(tween(2000, delayMillis = 600), RepeatMode.Restart),
        label = "s2",
    )
    val alpha2 by transition.animateFloat(
        0.4f, 0f,
        infiniteRepeatable(tween(2000, delayMillis = 600), RepeatMode.Restart),
        label = "a2",
    )

    val scale3 by transition.animateFloat(
        0.6f, 2.0f,
        infiniteRepeatable(tween(2000, delayMillis = 1200), RepeatMode.Restart),
        label = "s3",
    )
    val alpha3 by transition.animateFloat(
        0.4f, 0f,
        infiniteRepeatable(tween(2000, delayMillis = 1200), RepeatMode.Restart),
        label = "a3",
    )

    // Пульсация центральной иконки
    val iconPulse by transition.animateFloat(
        0.95f, 1.05f,
        infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "ip",
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Кольца
        Box(
            Modifier
                .size(100.dp)
                .scale(scale1)
                .alpha(alpha1)
                .background(SosRed, CircleShape)
        )
        Box(
            Modifier
                .size(100.dp)
                .scale(scale2)
                .alpha(alpha2)
                .background(SosRed, CircleShape)
        )
        Box(
            Modifier
                .size(100.dp)
                .scale(scale3)
                .alpha(alpha3)
                .background(SosRed, CircleShape)
        )

        // Центральная иконка
        Surface(
            modifier = Modifier
                .size(100.dp)
                .scale(iconPulse),
            shape = CircleShape,
            color = SosRed,
            shadowElevation = 16.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
    }
}
