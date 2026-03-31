package com.toiletgen.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay

/**
 * Анимированный Splash-оверлей поверх основного контента.
 * Показывает логотип с bounce-анимацией, затем исчезает.
 */
@Composable
fun SplashOverlay(onFinished: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }

    // Фазы: 0 = появление, 1 = показ, 2 = исчезновение
    LaunchedEffect(Unit) {
        delay(200)
        phase = 1  // появились
        delay(1800)
        phase = 2  // уходим
        delay(600)
        onFinished()
    }

    // Анимация масштаба иконки
    val iconScale by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0.3f
            1 -> 1f
            2 -> 1.2f
            else -> 1f
        },
        animationSpec = when (phase) {
            0 -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            1 -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            else -> tween(500)
        },
        label = "iconScale",
    )

    // Анимация прозрачности всего экрана
    val overlayAlpha by animateFloatAsState(
        targetValue = if (phase == 2) 0f else 1f,
        animationSpec = tween(500),
        label = "overlayAlpha",
    )

    // Анимация текста
    val textAlpha by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f
            1 -> 1f
            2 -> 0f
            else -> 0f
        },
        animationSpec = tween(400),
        label = "textAlpha",
    )

    // Пульсация фонового кольца
    val transition = rememberInfiniteTransition(label = "pulse")
    val ringScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "ring",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "ringAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B),
                        Color(0xFF00695C),
                        Color(0xFF004D40),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Пульсирующее кольцо
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale)
                        .alpha(ringAlpha)
                        .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                )
                // Основная иконка
                Box(
                    modifier = Modifier.scale(iconScale),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\uD83D\uDEBD",  // toilet emoji
                        fontSize = 80.sp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "ToiletGen",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Найди свой комфорт",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha),
            )
        }
    }
}
