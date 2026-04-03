package com.toiletgen.feature.stamps.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.domain.model.StampTrade
import com.toiletgen.core.domain.model.UserStamp
import com.toiletgen.feature.stamps.viewmodel.StampsViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StampsScreen(
    onBack: () -> Unit,
    viewModel: StampsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    state.message?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Коллекция марок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadStamps() }) { Text("Повторить") }
                    }
                }
            }
            else -> {
                val uniqueToilets = state.stamps.map { it.toiletId }.distinct().size
                val pendingTrades = state.trades.filter { it.status == "pending" }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Stats header
                    item(span = { GridItemSpan(3) }) {
                        StatsHeader(total = state.stamps.size, unique = uniqueToilets)
                    }

                    // Pending trades section
                    if (pendingTrades.isNotEmpty()) {
                        item(span = { GridItemSpan(3) }) {
                            Text(
                                "Входящие обмены (${pendingTrades.size})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        pendingTrades.forEach { trade ->
                            item(span = { GridItemSpan(3) }) {
                                TradeCard(
                                    trade = trade,
                                    onAccept = { viewModel.acceptTrade(trade.id) },
                                    onDecline = { viewModel.declineTrade(trade.id) },
                                )
                            }
                        }
                    }

                    // Stamps header
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            "Мои марки",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    if (state.stamps.isEmpty()) {
                        item(span = { GridItemSpan(3) }) {
                            EmptyState()
                        }
                    }

                    // Stamp cards
                    items(state.stamps) { stamp ->
                        StampCard(stamp = stamp)
                    }
                }
            }
        }

        // Snackbar for messages
        state.message?.let { msg ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.BottomCenter) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun StatsHeader(total: Int, unique: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(value = total.toString(), label = "Всего марок")
            StatItem(value = unique.toString(), label = "Уникальных")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StampCard(stamp: UserStamp) {
    val stampColor = remember(stamp.toiletId) {
        val hash = stamp.toiletId.hashCode()
        Color(
            red = ((hash and 0xFF0000) shr 16) / 255f * 0.6f + 0.2f,
            green = ((hash and 0x00FF00) shr 8) / 255f * 0.6f + 0.2f,
            blue = (hash and 0x0000FF) / 255f * 0.6f + 0.2f,
        )
    }
    val stampEmoji = remember(stamp.toiletType) {
        when (stamp.toiletType) {
            "PAID" -> "\uD83D\uDCB0"     // money bag
            "FREE" -> "\uD83C\uDD93"      // free
            "PRIVATE" -> "\uD83D\uDD12"   // lock
            else -> "\uD83D\uDEBD"         // toilet
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stamp")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wobble",
    )

    Card(
        modifier = Modifier.aspectRatio(0.75f).rotate(rotation),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(stampColor.copy(alpha = 0.8f), stampColor.copy(alpha = 0.4f))
                    )
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stampEmoji, fontSize = 24.sp)
            Spacer(Modifier.height(3.dp))
            Text(
                stamp.toiletName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(4.dp))
            // Координаты
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.3f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        "\uD83D\uDCCD",
                        fontSize = 8.sp,
                    )
                    Text(
                        "${"%.3f".format(stamp.latitude)}, ${"%.3f".format(stamp.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 7.sp,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            val dateStr = remember(stamp.obtainedAt) {
                SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(stamp.obtainedAt))
            }
            Text(
                dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun TradeCard(
    trade: StampTrade,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Предложение обмена", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Марка на марку", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, "Принять", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDecline) {
                Icon(Icons.Default.Close, "Отклонить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("\uD83D\uDEBD", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "У вас пока нет марок",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            "Посетите туалет, чтобы получить первую марку!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
