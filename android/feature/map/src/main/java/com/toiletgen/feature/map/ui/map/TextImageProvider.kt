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
        val size = (48 * density).toInt()
        val textSize = 16 * density

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Круг фона
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00897B") // Primary color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint)

        // Обводка
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2 * density
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - density, strokePaint)

        // Текст
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textY = size / 2f + textBounds.height() / 2f

        canvas.drawText(text, size / 2f, textY, textPaint)

        return bitmap
    }
}
