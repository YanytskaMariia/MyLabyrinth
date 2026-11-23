package com.example.mylabyrinth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

// Клас для відображення лабіринту на екрані та обробки свайпів
class LabyrinthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Модель лабіринту (дані: карта, позиція гравця, шлях підказки)
    var model: LabyrinthModel? = null
        set(value) {
            field = value
            invalidate()
        }
    // при виході з лабіринту
    var onWinListener: (() -> Unit)? = null
    // точки підказки
    private val hintPaint = Paint().apply {
        color = Color.parseColor("#F1C40F")
        isAntiAlias = true
    }
    private val wallPaint = Paint().apply { color = Color.parseColor("#2C3E50") }
    private val pathPaint = Paint().apply { color = Color.parseColor("#ECF0F1") }
    private val exitPaint = Paint().apply { color = Color.parseColor("#27AE60") }
    private val playerPaint = Paint().apply {
        color = Color.parseColor("#E74C3C")
        isAntiAlias = true
    }

    // Координати початку дотику
    private var startX = 0f
    private var startY = 0f

    // Метод для визначення розміру View
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    // Метод для малювання клітинок, підказки та гравця
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentModel = model ?: return

        val cellSize = width.toFloat() / currentModel.cols

        // сітка лабіринту
        for (r in 0 until currentModel.rows) {
            for (c in 0 until currentModel.cols) {
                val paint = when (currentModel.getCell(r, c)) {
                    1 -> wallPaint
                    2 -> exitPaint
                    else -> pathPaint
                }
                canvas.drawRect(
                    c * cellSize,
                    r * cellSize,
                    (c + 1) * cellSize,
                    (r + 1) * cellSize,
                    paint
                )
            }
        }
        // підказка
        for ((hintRow, hintCol) in currentModel.hintPath) {
            val hintX = (hintCol + 0.5f) * cellSize
            val hintY = (hintRow + 0.5f) * cellSize
            canvas.drawCircle(hintX, hintY, cellSize * 0.15f, hintPaint)
        }
        // гравець
        val centerX = (currentModel.playerCol + 0.5f) * cellSize
        val centerY = (currentModel.playerRow + 0.5f) * cellSize
        canvas.drawCircle(centerX, centerY, cellSize * 0.35f, playerPaint)
    }

    // Метод для обробки дотиків
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentModel = model ?: return false

        when (event.action) {
            // запам'ятати початкову точку
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }
            // обчислити напрямок свайпу
            MotionEvent.ACTION_UP -> {
                val dx = event.x - startX
                val dy = event.y - startY

                // випадковий дотик (менше 30 рх)
                if (abs(dx) < 30 && abs(dy) < 30) return true
                // зміна рядка (стовпця) за домінантною віссю
                val result = if (abs(dx) > abs(dy)) {
                    if (dx > 0) currentModel.movePlayer(0, 1) else currentModel.movePlayer(0, -1) // вправо, вліво
                } else {
                    if (dy > 0) currentModel.movePlayer(1, 0) else currentModel.movePlayer(-1, 0) // вниз, вверх
                }

                invalidate() // перемалювати з новою позицією

                // при виході
                if (result == LabyrinthModel.MoveResult.WIN) {
                    onWinListener?.invoke()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}