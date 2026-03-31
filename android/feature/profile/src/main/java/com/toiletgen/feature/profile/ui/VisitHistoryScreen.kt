package com.toiletgen.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toiletgen.core.network.model.VisitResponse
import com.toiletgen.core.ui.components.LoadingScreen
import com.toiletgen.core.ui.theme.Primary
import com.toiletgen.feature.profile.viewmodel.VisitHistoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    onBack: () -> Unit,
    viewModel: VisitHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История посещений") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingScreen(Modifier.padding(padding).fillMaxSize())
            uiState.visits.isEmpty() -> {
                Box(
                    Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Посещений пока нет",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Постройте маршрут и дойдите до туалета",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.visits) { visit ->
                        VisitCard(visit)
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitCard(visit: VisitResponse) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru"))
    val typeLabel = when (visit.toiletType) {
        "FREE" -> "Бесплатный"
        "PAID" -> "Платный"
        "REGULAR" -> "Обычный"
        "USER_ADDED" -> "Пользовательский"
        "PRIVATE" -> "Приватный"
        else -> visit.toiletType
    }
    val typeColor = when (visit.toiletType) {
        "FREE" -> Color(0xFF4CAF50)
        "PAID" -> Color(0xFFFF9800)
        "PRIVATE" -> Color(0xFF9C27B0)
        else -> Primary
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = typeColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = visit.toiletName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(visit.visitedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
