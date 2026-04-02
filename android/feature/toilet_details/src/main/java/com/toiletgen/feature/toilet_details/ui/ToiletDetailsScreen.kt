package com.toiletgen.feature.toilet_details.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toiletgen.core.domain.model.Review
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.core.ui.components.*
import com.toiletgen.core.ui.theme.*
import com.toiletgen.feature.toilet_details.viewmodel.ToiletDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToiletDetailsScreen(
    toiletId: String,
    onBack: () -> Unit,
    onAddReview: () -> Unit = {},
    viewModel: ToiletDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toiletId) { viewModel.loadToilet(toiletId) }

    uiState.stampMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearStampMessage()
        }
    }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Удалить туалет?") },
            text = { Text("Это действие нельзя отменить. Все отзывы и посещения будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteToilet()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.toilet?.name ?: "Загрузка...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    val canDelete = uiState.currentUserId != null &&
                            (uiState.toilet?.ownerId == uiState.currentUserId || uiState.currentUserRole == "moderator")
                    if (canDelete) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Удалить",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding))
            uiState.error != null -> ErrorScreen(uiState.error!!, onRetry = { viewModel.loadToilet(toiletId) }, modifier = Modifier.padding(padding))
            uiState.toilet != null -> {
                val toilet = uiState.toilet!!
                val typeColor = when (toilet.type) {
                    ToiletType.FREE -> RatingExcellent
                    ToiletType.PAID -> Secondary
                    ToiletType.REGULAR -> Color(0xFF1976D2)
                    ToiletType.PRIVATE -> Color(0xFF7B1FA2)
                    ToiletType.USER_ADDED -> Primary
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Hero card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Box {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(typeColor, typeColor.copy(alpha = 0.3f))
                                            )
                                        )
                                )
                                Column(modifier = Modifier.padding(16.dp).padding(top = 6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = typeColor.copy(alpha = 0.1f),
                                        ) {
                                            Text(
                                                "${"%.1f".format(toilet.avgRating)}",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = typeColor,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            RatingBar(rating = toilet.avgRating)
                                            Text(
                                                "${toilet.reviewCount} отзывов",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Text(
                                        toilet.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ToiletTypeChip(type = toilet.type.name)
                                        if (toilet.hasToiletPaper) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = RatingExcellent.copy(alpha = 0.12f),
                                            ) {
                                                Row(
                                                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = RatingExcellent)
                                                    Text("Бумага", style = MaterialTheme.typography.labelMedium, color = RatingExcellent)
                                                }
                                            }
                                        }
                                        if (toilet.isPaid && toilet.price != null) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = Secondary.copy(alpha = 0.12f),
                                            ) {
                                                Row(
                                                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Icon(Icons.Default.Payments, null, Modifier.size(16.dp), tint = Secondary)
                                                    Text("${toilet.price?.toInt()} \u20BD", style = MaterialTheme.typography.labelMedium, color = Secondary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cleanliness
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CleaningServices, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Чистота", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.weight(1f))
                                    val cleanColor = when {
                                        toilet.avgCleanliness >= 4.0 -> RatingExcellent
                                        toilet.avgCleanliness >= 3.0 -> RatingGood
                                        toilet.avgCleanliness >= 2.0 -> RatingAverage
                                        else -> RatingTerrible
                                    }
                                    Text(
                                        "${"%.1f".format(toilet.avgCleanliness)} / 5.0",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = cleanColor,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { (toilet.avgCleanliness / 5.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            }
                        }
                    }

                    // Collect stamp button
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "\uD83D\uDCEE Марка туалета",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "Соберите уникальную марку этого места",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { viewModel.collectStamp() },
                                    enabled = !uiState.stampCollecting,
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    if (uiState.stampCollecting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Собрать")
                                    }
                                }
                            }
                            uiState.stampMessage?.let { msg ->
                                Text(
                                    msg,
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (msg.contains("получена")) RatingExcellent
                                        else MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    // Reviews header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Отзывы (${uiState.reviews.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            FilledTonalButton(
                                onClick = onAddReview,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Default.RateReview, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Написать отзыв")
                            }
                        }
                    }

                    items(uiState.reviews) { review -> ReviewCard(review) }

                    if (uiState.reviews.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ) {
                                Column(
                                    Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        Icons.Default.RateReview,
                                        null,
                                        Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Пока нет отзывов",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        "Будьте первым!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                review.username.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(review.username, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                RatingBar(rating = review.rating.toDouble())
            }

            Spacer(Modifier.height(8.dp))

            Text(review.comment, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CleanlinessChip("Запах", review.cleanlinessSmell)
                CleanlinessChip("Чистота", review.cleanlinessDirt)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (review.hasToiletPaper)
                        RatingExcellent.copy(alpha = 0.1f)
                    else
                        RatingTerrible.copy(alpha = 0.1f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            if (review.hasToiletPaper) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null, Modifier.size(12.dp),
                            tint = if (review.hasToiletPaper) RatingExcellent else RatingTerrible,
                        )
                        Text(
                            "\uD83E\uDDFB",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanlinessChip(label: String, value: Int) {
    val color = when (value) {
        5 -> RatingExcellent
        4 -> RatingGood
        3 -> RatingAverage
        2 -> RatingBad
        else -> RatingTerrible
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Text(
            "$label: $value/5",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
