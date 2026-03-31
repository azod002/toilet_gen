package com.toiletgen.feature.map.ui.map

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import com.toiletgen.core.domain.model.ToiletType

/**
 * Рисует кастомные маркеры туалетов для Yandex MapKit.
 *
 * Маркер = капля (pin shape) с иконкой туалета внутри.
 * Если есть туалетная бумага — рисуется маленький значок рулона сверху-справа.
 * Цвет зависит от типа туалета.
 */
object ToiletMarkerDrawer {

    private const val MARKER_WIDTH = 96
    private const val MARKER_HEIGHT = 120
    private const val PAPER_BADGE_SIZE = 36

    private val typeColors = mapOf(
        ToiletType.FREE to Color.parseColor("#4CAF50"),       // зелёный
        ToiletType.PAID to Color.parseColor("#FFB300"),       // янтарный
        ToiletType.REGULAR to Color.parseColor("#1976D2"),    // синий
        ToiletType.PRIVATE to Color.parseColor("#7B1FA2"),    // фиолетовый
        ToiletType.USER_ADDED to Color.parseColor("#00897B"), // teal
    )

    private val shadowColor = Color.parseColor("#44000000")

    fun draw(context: Context, type: ToiletType, hasToiletPaper: Boolean, rating: Double): Bitmap {
        val width = MARKER_WIDTH
        val height = MARKER_HEIGHT + if (hasToiletPaper) PAPER_BADGE_SIZE / 2 else 0
        val bitmap = Bitmap.createBitmap(width + PAPER_BADGE_SIZE / 2, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = typeColors[type] ?: typeColors[ToiletType.REGULAR]!!

        // Тень
        drawPin(canvas, baseColor, shadowOffset = true)
        // Основная капля
        drawPin(canvas, baseColor, shadowOffset = false)
        // Иконка туалета внутри
        drawToiletIcon(canvas, baseColor)
        // Рейтинг внизу капли
        if (rating > 0) drawRatingBadge(canvas, rating)
        // Бейдж туалетной бумаги
        if (hasToiletPaper) drawPaperBadge(canvas)

        return bitmap
    }

    private fun drawPin(canvas: Canvas, color: Int, shadowOffset: Boolean) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            this.color = if (shadowOffset) shadowColor else color
        }
        val offsetY = if (shadowOffset) 3f else 0f

        val path = Path().apply {
            // Круглая верхушка
            addArc(
                RectF(8f, 4f + offsetY, (MARKER_WIDTH - 8).toFloat(), (MARKER_WIDTH - 12).toFloat() + offsetY),
                180f, 180f
            )
            // Нижний треугольник (острие)
            lineTo(MARKER_WIDTH / 2f, MARKER_HEIGHT.toFloat() + offsetY)
            lineTo(8f, (MARKER_WIDTH - 12f) / 2f + 4f + offsetY)
            close()
        }
        canvas.drawPath(path, paint)

        // Белый круг внутри
        if (!shadowOffset) {
            val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = Color.WHITE
            }
            canvas.drawCircle(
                MARKER_WIDTH / 2f,
                (MARKER_WIDTH - 8f) / 2f + 2f,
                (MARKER_WIDTH - 40f) / 2f,
                innerPaint
            )
        }
    }

    private fun drawToiletIcon(canvas: Canvas, accentColor: Int) {
        val cx = MARKER_WIDTH / 2f
        val cy = (MARKER_WIDTH - 8f) / 2f + 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = accentColor
            strokeWidth = 3.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentColor
        }

        val s = 11f // масштаб иконки

        // Крышка (верхняя дуга)
        val lidRect = RectF(cx - s, cy - s * 1.1f, cx + s, cy + s * 0.2f)
        canvas.drawArc(lidRect, 180f, 180f, false, paint)

        // Чаша (нижняя часть)
        val bowlPath = Path().apply {
            moveTo(cx - s, cy - s * 0.05f)
            lineTo(cx - s * 0.85f, cy + s * 0.9f)
            quadTo(cx, cy + s * 1.3f, cx + s * 0.85f, cy + s * 0.9f)
            lineTo(cx + s, cy - s * 0.05f)
        }
        canvas.drawPath(bowlPath, paint)

        // Кнопка слива (маленький кружок)
        canvas.drawCircle(cx, cy - s * 0.65f, 2.5f, fillPaint)
    }

    private fun drawRatingBadge(canvas: Canvas, rating: Double) {
        val cx = MARKER_WIDTH / 2f
        val y = MARKER_HEIGHT.toFloat() - 26f

        val ratingColor = when {
            rating >= 4.0 -> Color.parseColor("#4CAF50")
            rating >= 3.0 -> Color.parseColor("#FFC107")
            rating >= 2.0 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }

        // Фон бейджа
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(cx - 16f, y - 8f, cx + 16f, y + 10f),
            8f, 8f, bgPaint
        )

        // Рамка
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ratingColor
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(
            RectF(cx - 16f, y - 8f, cx + 16f, y + 10f),
            8f, 8f, borderPaint
        )

        // Текст рейтинга
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ratingColor
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("%.1f".format(rating), cx, y + 6f, textPaint)
    }

    private fun drawPaperBadge(canvas: Canvas) {
        val bx = MARKER_WIDTH.toFloat() - 4f
        val by = 2f
        val r = PAPER_BADGE_SIZE / 2f

        // Белая подложка
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(3f, 0f, 1f, Color.parseColor("#40000000"))
        }
        canvas.drawCircle(bx, by + r, r, bgPaint)

        // Иконка рулона туалетной бумаги
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8D6E63") // коричневый
            style = Paint.Style.STROKE
            strokeWidth = 2f
            strokeCap = Paint.Cap.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFEBE9") // светлый
            style = Paint.Style.FILL
        }

        val pcx = bx
        val pcy = by + r

        // Рулон — прямоугольник с закруглением
        val rollRect = RectF(pcx - 7f, pcy - 6f, pcx + 5f, pcy + 6f)
        canvas.drawRoundRect(rollRect, 3f, 3f, fillPaint)
        canvas.drawRoundRect(rollRect, 3f, 3f, paint)

        // Развёрнутый хвостик бумаги
        val tailPath = Path().apply {
            moveTo(pcx + 5f, pcy - 4f)
            quadTo(pcx + 11f, pcy - 6f, pcx + 10f, pcy + 2f)
            quadTo(pcx + 9f, pcy + 6f, pcx + 5f, pcy + 4f)
        }
        val tailFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        canvas.drawPath(tailPath, tailFill)
        canvas.drawPath(tailPath, paint)

        // Внутренний кружок рулона
        canvas.drawCircle(pcx - 1f, pcy, 2.5f, paint)
    }
}
