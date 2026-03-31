package com.toiletgen.feature.entertainment.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private data class EntertainmentItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentScreen(
    onNavigate: (String) -> Unit = {},
) {
    val items = listOf(
        EntertainmentItem(
            "Новости", "Свежие новости из RSS",
            Icons.Default.Newspaper, Color(0xFF1976D2), "entertainment/news",
        ),
        EntertainmentItem(
            "Радио", "Онлайн радиостанции",
            Icons.Default.Radio, Color(0xFFE91E63), "entertainment/radio",
        ),
        EntertainmentItem(
            "Анекдоты", "Туалетный юмор",
            Icons.Default.EmojiEmotions, Color(0xFFFF9800), "entertainment/jokes",
        ),
        EntertainmentItem(
            "Книги", "Загружай и читай PDF",
            Icons.Default.MenuBook, Color(0xFF4CAF50), "entertainment/books",
        ),
        EntertainmentItem(
            "Чат", "Общий и личные сообщения",
            Icons.Default.Chat, Color(0xFF9C27B0), "chat",
        ),
        EntertainmentItem(
            "Форум", "Треды и обсуждения",
            Icons.Default.Forum, Color(0xFF00897B), "entertainment/forum",
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Развлечения") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Animated illustration
            item {
                AnimatedHeader()
                Spacer(Modifier.height(8.dp))
            }

            items.forEach { item ->
                item {
                    EntertainmentCard(item = item, onClick = { onNavigate(item.route) })
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "entertainment")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "mainFloat",
    )

    val orbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbitFloat",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Orbiting icons
        val orbitRadius = 55.dp
        val orbitIcons = listOf(
            Icons.Default.Gamepad,
            Icons.Default.AutoStories,
            Icons.Default.MusicNote,
        )
        orbitIcons.forEachIndexed { index, icon ->
            val angle = orbitPhase + index * (2f * Math.PI.toFloat() / orbitIcons.size)
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(
                        x = (orbitRadius.value * cos(angle)).dp,
                        y = (orbitRadius.value * sin(angle)).dp,
                    )
                    .then(Modifier),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            )
        }

        // Central floating icon
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .offset(y = (sin(phase) * 8).dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EntertainmentCard(
    item: EntertainmentItem,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = item.color.copy(alpha = 0.12f),
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        item.icon, null,
                        modifier = Modifier.size(28.dp),
                        tint = item.color,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
