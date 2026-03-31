package com.toiletgen.feature.yearly_report.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.ui.components.LoadingScreen
import com.toiletgen.feature.yearly_report.viewmodel.YearlyReportViewModel
import org.koin.androidx.compose.koinViewModel

private val gradients = listOf(
    listOf(Color(0xFF00897B), Color(0xFF004D40)),
    listOf(Color(0xFF1976D2), Color(0xFF0D47A1)),
    listOf(Color(0xFFE65100), Color(0xFFBF360C)),
    listOf(Color(0xFF6A1B9A), Color(0xFF4A148C)),
    listOf(Color(0xFFC62828), Color(0xFF880E4F)),
    listOf(Color(0xFF00897B), Color(0xFF1A237E)),
)

@Composable
fun YearlyReportScreen(viewModel: YearlyReportViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) { LoadingScreen(); return }
    val report = uiState.report ?: return

    val page = uiState.currentPage

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    gradients.getOrElse(page) { gradients[0] }
                )
            )
    ) {
        // Декоративные круги на фоне
        BackgroundDecoration(page)

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (slideInHorizontally(tween(500)) { it } + fadeIn(tween(400)))
                    .togetherWith(slideOutHorizontally(tween(500)) { -it } + fadeOut(tween(300)))
            },
            label = "page",
        ) { currentPage ->
            when (currentPage) {
                0 -> IntroPage(report.year)
                1 -> StatPage("${report.toiletsVisited}", "туалетов\nпосещено", Icons.Default.Place, Color(0xFF4CAF50))
                2 -> StatPage("${report.reviewsWritten}", "отзывов\nнаписано", Icons.Default.RateReview, Color(0xFFFFB300))
                3 -> StatPage("${report.sosSent}", "SOS\nзапросов", Icons.Default.Warning, Color(0xFFFF5252))
                4 -> StatPage("${report.sosHelped}", "раз вы помогли\nдругим", Icons.Default.Favorite, Color(0xFFE91E63))
                5 -> OutroPage(report.year)
            }
        }

        // Навигация
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (page > 0) {
                FloatingActionButton(
                    onClick = { viewModel.prevPage() },
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }

            // Индикатор страниц
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(6) { i ->
                    Box(
                        Modifier
                            .size(if (i == page) 10.dp else 6.dp)
                            .background(
                                Color.White.copy(alpha = if (i == page) 0.9f else 0.3f),
                                CircleShape,
                            )
                    )
                }
            }

            if (page < 5) {
                FloatingActionButton(
                    onClick = { viewModel.nextPage() },
                    containerColor = Color.White.copy(alpha = 0.25f),
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Далее")
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun BackgroundDecoration(page: Int) {
    val transition = rememberInfiniteTransition(label = "bg")
    val float1 by transition.animateFloat(
        0f, 20f,
        infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "f1",
    )
    val float2 by transition.animateFloat(
        10f, -10f,
        infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "f2",
    )

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .offset(x = (-40).dp, y = (100 + float1).dp)
                .size(200.dp)
                .alpha(0.06f)
                .background(Color.White, CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (float2 + 200).dp)
                .size(150.dp)
                .alpha(0.05f)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
private fun IntroPage(year: Int) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "intro",
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "\uD83D\uDEBD",
                fontSize = 72.sp,
                modifier = Modifier.scale(scale),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Ваш $year",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Годовой отчёт ToiletGen",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Листайте, чтобы увидеть свою статистику",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun StatPage(number: String, label: String, icon: ImageVector, accentColor: Color) {
    // Анимация числа — count up эффект
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "stat",
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Иконка в кружке
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(40.dp), tint = Color.White.copy(alpha = 0.8f))
                }
            }

            Spacer(Modifier.height(32.dp))

            // Большое число
            Text(
                number,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(animatedScale),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                label,
                fontSize = 22.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
            )
        }
    }
}

@Composable
private fun OutroPage(year: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "outro")
    val pulse by infiniteTransition.animateFloat(
        0.95f, 1.05f,
        infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "\uD83C\uDF89",
                fontSize = 64.sp,
                modifier = Modifier.scale(pulse),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Спасибо!",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "До встречи в ${year + 1}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(32.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Text(
                    "ToiletGen \u2764\uFE0F",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }
        }
    }
}
