package com.toiletgen.feature.map.ui.map

import android.content.Context
import android.graphics.*
import com.yandex.runtime.image.ImageProvider

/**
 * ImageProvider для отображения текста (числа) в кластере.
 */
class TextImageProvider(
    private val text: String,
    private val context: Context,
) : ImageProvider() {

    override fun getId(): String = "cluster_$text"

    override fun getImage(): Bitmap {
        val density = context.resources.displayMetrics.density
        val width = (36 * density).toInt()
        val height = (52 * density).toInt()
        val cornerRadius = 8 * density
        val textSize = 16 * density

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Прямоугольник фона со скруглёнными углами
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00897B") // Primary color
            style = Paint.Style.FILL
        }
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // Обводка
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2 * density
        }
        val strokeRect = RectF(density, density, width - density, height - density)
        canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)

        // Текст
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textY = height / 2f + textBounds.height() / 2f

        canvas.drawText(text, width / 2f, textY, textPaint)

        return bitmap
    }
}
