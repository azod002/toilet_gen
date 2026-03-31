package com.toiletgen.feature.map.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.core.ui.components.RatingBar
import com.toiletgen.core.ui.components.ToiletTypeChip
import com.toiletgen.core.ui.theme.*
import com.toiletgen.feature.map.ui.map.YandexMap
import com.toiletgen.feature.map.viewmodel.MapEvent
import com.toiletgen.feature.map.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onToiletClick: (String) -> Unit,
    onSosClick: () -> Unit,
    onAddToiletClick: (lat: Double, lon: Double) -> Unit,
    viewModel: MapViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var isPlacementMode by remember { mutableStateOf(false) }
    var showRouteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Yandex MapKit
        YandexMap(
            toilets = uiState.toilets,
            userLat = uiState.userLat,
            userLon = uiState.userLon,
            isPlacementMode = isPlacementMode,
            routeTarget = uiState.routeTarget?.let {
                com.yandex.mapkit.geometry.Point(it.lat, it.lon)
            },
            onToiletClick = { viewModel.onEvent(MapEvent.SelectToilet(it)) },
            onCameraMoved = { lat, lon -> viewModel.onEvent(MapEvent.LoadToilets(lat, lon)) },
            onMapTap = { lat, lon ->
                isPlacementMode = false
                onAddToiletClick(lat, lon)
            },
            onLocationUpdated = { lat, lon ->
                viewModel.onEvent(MapEvent.UpdateUserLocation(lat, lon))
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Placement mode hint
        if (isPlacementMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.tertiary,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.TouchApp, null, tint = MaterialTheme.colorScheme.onTertiary)
                    Text(
                        "Нажмите на карту для выбора места",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }
        }

        // Loading indicator
        if (uiState.isLoading && !isPlacementMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Загрузка...", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Route button — left side
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.routeTarget != null) {
                // Active route banner + cancel button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            uiState.routeTarget!!.toiletName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 120.dp),
                        )
                        IconButton(
                            onClick = { viewModel.onEvent(MapEvent.ClearRoute) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "Закрыть маршрут",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (uiState.routeTarget != null) {
                        viewModel.onEvent(MapEvent.ClearRoute)
                    } else {
                        showRouteDialog = true
                    }
                },
                containerColor = if (uiState.routeTarget != null)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (uiState.routeTarget != null)
                    MaterialTheme.colorScheme.onTertiary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Icon(
                    if (uiState.routeTarget != null) Icons.Default.Close else Icons.Default.Directions,
                    "Маршрут",
                )
            }
        }

        // Route type picker dialog
        if (showRouteDialog) {
            AlertDialog(
                onDismissRequest = { showRouteDialog = false },
                title = { Text("Маршрут до ближайшего") },
                text = { Text("Выберите тип туалета") },
                confirmButton = {
                    TextButton(onClick = {
                        showRouteDialog = false
                        viewModel.onEvent(MapEvent.BuildRouteToNearest(free = true))
                    }) {
                        Text("Бесплатный")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRouteDialog = false
                        viewModel.onEvent(MapEvent.BuildRouteToNearest(free = false))
                    }) {
                        Text("Платный")
                    }
                },
            )
        }

        // FAB column
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Add toilet button — toggles placement mode
            FloatingActionButton(
                onClick = { isPlacementMode = !isPlacementMode },
                containerColor = if (isPlacementMode)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isPlacementMode)
                    MaterialTheme.colorScheme.onTertiary
                else
                    MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    if (isPlacementMode) Icons.Default.Close else Icons.Default.Add,
                    "Добавить точку",
                )
            }

            // SOS Button — large, red, attention-grabbing
            LargeFloatingActionButton(
                onClick = onSosClick,
                containerColor = SosRed,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(28.dp))
                    Text("SOS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom sheet for selected toilet
        uiState.selectedToilet?.let { toilet ->
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(MapEvent.DismissToilet) },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                ToiletPreviewSheet(
                    toilet = toilet,
                    onDetailsClick = {
                        viewModel.onEvent(MapEvent.DismissToilet)
                        onToiletClick(toilet.id)
                    },
                )
            }
        }

        // Arrival celebration with confetti
        uiState.arrivedMessage?.let { message ->
            ArrivalCelebration(
                message = message,
                onDismiss = { viewModel.onEvent(MapEvent.DismissArrived) },
            )
        }

        // Error
        uiState.error?.let { error ->
            if (uiState.arrivedMessage == null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun ToiletPreviewSheet(
    toilet: Toilet,
    onDetailsClick: () -> Unit,
) {
    val typeColor = when (toilet.type) {
        ToiletType.FREE -> RatingExcellent
        ToiletType.PAID -> Secondary
        ToiletType.REGULAR -> Color(0xFF1976D2)
        ToiletType.PRIVATE -> Color(0xFF7B1FA2)
        ToiletType.USER_ADDED -> Primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        // Цветная полоска типа
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(typeColor, typeColor.copy(alpha = 0.3f))
                    )
                )
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = toilet.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            ToiletTypeChip(type = toilet.type.name)
        }

        Spacer(Modifier.height(12.dp))

        // Рейтинг
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingBar(rating = toilet.avgRating)
            Spacer(Modifier.width(8.dp))
            Text(
                "${"%.1f".format(toilet.avgRating)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    toilet.avgRating >= 4.0 -> RatingExcellent
                    toilet.avgRating >= 3.0 -> RatingGood
                    toilet.avgRating >= 2.0 -> RatingAverage
                    else -> RatingTerrible
                },
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "(${toilet.reviewCount} отзывов)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(16.dp))

        // Чипы
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Туалетная бумага
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (toilet.hasToiletPaper)
                    RatingExcellent.copy(alpha = 0.12f)
                else
                    RatingTerrible.copy(alpha = 0.12f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        if (toilet.hasToiletPaper) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (toilet.hasToiletPaper) RatingExcellent else RatingTerrible,
                    )
                    Text(
                        if (toilet.hasToiletPaper) "Бумага есть" else "Бумаги нет",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (toilet.hasToiletPaper) RatingExcellent else RatingTerrible,
                    )
                }
            }

            // Цена
            if (toilet.isPaid && toilet.price != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Secondary.copy(alpha = 0.12f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Default.Payments, null, Modifier.size(16.dp), tint = Secondary)
                        Text(
                            "${toilet.price?.toInt()} \u20BD",
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Кнопка подробнее
        Button(
            onClick = onDetailsClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Подробнее", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// --- Confetti celebration ---

private data class ConfettiParticle(
    val x: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val wobbleAmplitude: Float,
    val wobbleSpeed: Float,
)

@Composable
private fun ArrivalCelebration(
    message: String,
    onDismiss: () -> Unit,
) {
    val confettiColors = listOf(
        Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
        Color(0xFF4D96FF), Color(0xFFC77DFF), Color(0xFFFF922B),
        Color(0xFF20C997), Color(0xFFE64980),
    )

    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = (Math.random() * 1.0).toFloat(),
                speed = (0.2f + Math.random().toFloat() * 0.6f),
                size = (4f + Math.random().toFloat() * 8f),
                color = confettiColors.random(),
                rotation = (Math.random() * 360).toFloat(),
                rotationSpeed = (-3f + Math.random().toFloat() * 6f),
                wobbleAmplitude = (10f + Math.random().toFloat() * 30f),
                wobbleSpeed = (1f + Math.random().toFloat() * 3f),
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(4000, easing = LinearEasing))
    }
    val p = progress.value

    // Card scale animation
    val cardScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        cardScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Semi-transparent backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f * (1f - p * 0.5f)))
        )

        // Confetti canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            for (particle in particles) {
                val yPos = -50f + (h + 100f) * p * particle.speed
                if (yPos < -50f || yPos > h + 50f) continue

                val wobble = kotlin.math.sin(p * particle.wobbleSpeed * 10f) * particle.wobbleAmplitude
                val xPos = particle.x * w + wobble
                val rot = particle.rotation + p * particle.rotationSpeed * 360f

                rotate(rot, pivot = Offset(xPos, yPos)) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(xPos - particle.size / 2, yPos - particle.size / 2),
                        size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.6f),
                    )
                }
            }
        }

        // Celebration card
        val scale = cardScale.value
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🎉",
                    fontSize = 48.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Поздравляем!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Посещение засчитано!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Отлично!", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
