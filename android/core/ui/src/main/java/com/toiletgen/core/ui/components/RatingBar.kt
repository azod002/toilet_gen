package com.toiletgen.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.toiletgen.core.ui.theme.*

@Composable
fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    onRatingChanged: ((Int) -> Unit)? = null,
) {
    val color = when {
        rating >= 4.5 -> RatingExcellent
        rating >= 3.5 -> RatingGood
        rating >= 2.5 -> RatingAverage
        rating >= 1.5 -> RatingBad
        else -> RatingTerrible
    }

    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val icon = when {
                i <= rating.toInt() -> Icons.Filled.Star
                i - 0.5 <= rating -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            if (onRatingChanged != null) {
                IconButton(onClick = { onRatingChanged(i) }) {
                    Icon(icon, contentDescription = "Рейтинг $i", tint = color)
                }
            } else {
                Icon(icon, contentDescription = "Рейтинг $i", tint = color)
            }
        }
    }
}
