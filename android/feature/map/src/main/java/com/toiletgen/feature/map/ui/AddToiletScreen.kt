package com.toiletgen.feature.map.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.feature.map.viewmodel.AddToiletViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToiletScreen(
    latitude: Double,
    longitude: Double,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddToiletViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.setCoordinates(latitude, longitude) }
    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onSuccess() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить точку") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Координаты
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Название
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Название") },
                placeholder = { Text("Например: Туалет в ТЦ Мега") },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Описание
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Описание") },
                placeholder = { Text("2 этаж, рядом с фудкортом") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            // Тип
            Text("Тип точки", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val types = listOf(
                    ToiletType.FREE to "Бесплатный",
                    ToiletType.PAID to "Платный",
                    ToiletType.REGULAR to "Обычный",
                    ToiletType.PRIVATE to "Приватный",
                )
                types.forEachIndexed { index, (type, label) ->
                    SegmentedButton(
                        selected = uiState.type == type,
                        onClick = { viewModel.onTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, types.size),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Платный — цена
            if (uiState.isPaid) {
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    label = { Text("Цена (руб.)") },
                    leadingIcon = { Icon(Icons.Default.Payments, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Туалетная бумага
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Есть туалетная бумага", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.hasToiletPaper,
                    onCheckedChange = { viewModel.onToiletPaperChange(it) },
                )
            }

            // Ошибка
            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Кнопка
            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить точку")
                }
            }
        }
    }
}
