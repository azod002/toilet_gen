package com.toiletgen.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String) -> Unit,
    isLoading: Boolean = false,
) {
    val reasons = listOf(
        "Спам",
        "Оскорбления",
        "Неприемлемый контент",
        "Мошенничество",
        "Другое",
    )
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var customReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Пожаловаться") },
        text = {
            Column {
                Text(
                    "Выберите причину жалобы:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (selectedReason == "Другое") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Опишите причину") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reason = if (selectedReason == "Другое") customReason else selectedReason ?: ""
                    onSubmit(reason)
                },
                enabled = selectedReason != null &&
                        (selectedReason != "Другое" || customReason.isNotBlank()) &&
                        !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
