package com.toiletgen.core.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.toiletgen.core.ui.theme.*

@Composable
fun ToiletTypeChip(
    type: String,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (type) {
        "REGULAR" -> "Обычный" to Primary
        "USER_ADDED" -> "От пользователя" to Tertiary
        "PAID" -> "Платный" to Secondary
        "FREE" -> "Бесплатный" to RatingExcellent
        "PRIVATE" -> "Приватный" to SosRed
        else -> type to Primary
    }

    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color,
        ),
    )
}
