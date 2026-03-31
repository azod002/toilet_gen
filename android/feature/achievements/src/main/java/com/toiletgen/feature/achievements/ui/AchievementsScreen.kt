package com.toiletgen.feature.achievements.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.domain.model.Achievement
import com.toiletgen.core.ui.components.LoadingScreen
import com.toiletgen.core.ui.theme.Primary
import com.toiletgen.core.ui.theme.Secondary
import com.toiletgen.feature.achievements.viewmodel.AchievementsViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun achievementIcon(name: String): ImageVector = when (name) {
    "Первый визит" -> Icons.Default.Place
    "Критик" -> Icons.Default.RateReview
    "Картограф" -> Icons.Default.AddLocationAlt
    "SOS Герой" -> Icons.Default.HealthAndSafety
    "Добрый самаритянин" -> Icons.Default.Favorite
    else -> Icons.Default.EmojiEvents
}

private fun formatUnlockDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale("ru"))
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit, viewModel: AchievementsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Достижения",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (uiState.isLoading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }

        val achievements = uiState.achievements
        val unlocked = achievements.count { it.isUnlocked }
        val total = achievements.size
        val targetProgress = if (total > 0) unlocked.toFloat() / total else 0f

        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            label = "progress",
        )

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // --- Progress header ---
            ProgressHeader(
                unlocked = unlocked,
                total = total,
                animatedProgress = animatedProgress,
            )

            Spacer(Modifier.height(8.dp))

            // --- Achievement grid ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(achievements, key = { it.id }) { achievement ->
                    AchievementCard(achievement)
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(unlocked: Int, total: Int, animatedProgress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Прогресс",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "$unlocked из $total",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary,
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Primary, Secondary),
                        ),
                    ),
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (unlocked == total && total > 0) "Все достижения разблокированы!"
            else "Разблокировано ${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val isUnlocked = achievement.isUnlocked
    val icon = achievementIcon(achievement.name)

    // Shine animation for unlocked cards
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shineOffset",
    )

    val gradientUnlocked = Brush.verticalGradient(
        colors = listOf(
            Primary.copy(alpha = 0.15f),
            Secondary.copy(alpha = 0.10f),
            Color.Transparent,
        ),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 4.dp else 0.dp,
        ),
    ) {
        Box {
            // Gradient overlay for unlocked
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(gradientUnlocked, shape = RoundedCornerShape(16.dp)),
                )

                // Shine sweep effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.15f),
                                        Color.Transparent,
                                    ),
                                    start = Offset(size.width * shineOffset, 0f),
                                    end = Offset(size.width * (shineOffset + 0.5f), size.height),
                                ),
                                blendMode = BlendMode.SrcAtop,
                            )
                        },
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked)
                                Brush.radialGradient(
                                    colors = listOf(
                                        Secondary.copy(alpha = 0.3f),
                                        Primary.copy(alpha = 0.15f),
                                    ),
                                )
                            else
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                    ),
                                ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isUnlocked) Primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )

                // Unlock date
                val unlockTime = achievement.unlockedAt
                if (isUnlocked && unlockTime != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formatUnlockDate(unlockTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Locked label
                if (!isUnlocked) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Заблокировано",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}
