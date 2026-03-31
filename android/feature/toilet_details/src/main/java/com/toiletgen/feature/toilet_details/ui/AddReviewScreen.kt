package com.toiletgen.feature.toilet_details.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toiletgen.core.ui.components.RatingBar
import com.toiletgen.core.ui.theme.*
import com.toiletgen.feature.toilet_details.viewmodel.AddReviewViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    toiletId: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddReviewViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onSuccess() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Написать отзыв") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Общий рейтинг
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Общий рейтинг", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    RatingBar(
                        rating = uiState.rating.toDouble(),
                        onRatingChanged = { viewModel.onRatingChange(it) },
                    )
                    Text(
                        when (uiState.rating) {
                            1 -> "Ужасно"
                            2 -> "Плохо"
                            3 -> "Нормально"
                            4 -> "Хорошо"
                            5 -> "Отлично"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (uiState.rating) {
                            1 -> RatingTerrible
                            2 -> RatingBad
                            3 -> RatingAverage
                            4 -> RatingGood
                            5 -> RatingExcellent
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            // Запах
            CleanlinessSlider(
                label = "Запах",
                icon = Icons.Default.Air,
                value = uiState.cleanlinessSmell,
                onValueChange = { viewModel.onSmellChange(it) },
                lowLabel = "Вонь",
                highLabel = "Свежесть",
            )

            // Грязь
            CleanlinessSlider(
                label = "Чистота поверхностей",
                icon = Icons.Default.CleaningServices,
                value = uiState.cleanlinessDirt,
                onValueChange = { viewModel.onDirtChange(it) },
                lowLabel = "Грязно",
                highLabel = "Чисто",
            )

            // Туалетная бумага
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (uiState.hasToiletPaper) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            null,
                            tint = if (uiState.hasToiletPaper) RatingExcellent else RatingTerrible,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Туалетная бумага", style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked = uiState.hasToiletPaper,
                        onCheckedChange = { viewModel.onToiletPaperChange(it) },
                    )
                }
            }

            // Комментарий
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = { viewModel.onCommentChange(it) },
                label = { Text("Комментарий") },
                placeholder = { Text("Расскажите о вашем опыте...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )

            // Ошибка
            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Кнопка
            Button(
                onClick = { viewModel.submit(toiletId) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Отправить отзыв")
                }
            }
        }
    }
}

@Composable
private fun CleanlinessSlider(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    onValueChange: (Int) -> Unit,
    lowLabel: String,
    highLabel: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    "$value / 5",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (value) {
                        1 -> RatingTerrible
                        2 -> RatingBad
                        3 -> RatingAverage
                        4 -> RatingGood
                        5 -> RatingExcellent
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(lowLabel, style = MaterialTheme.typography.labelSmall, color = RatingTerrible)
                Text(highLabel, style = MaterialTheme.typography.labelSmall, color = RatingExcellent)
            }
        }
    }
}
