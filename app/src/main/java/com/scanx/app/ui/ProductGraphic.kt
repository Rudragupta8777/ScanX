package com.scanx.app.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect

class ProductGraphic(
    overlay: GraphicOverlay,
    private val boundingBox: Rect,
    private val trackingId: Int
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint = Paint().apply {
        color = Color.parseColor("#8000FF00")
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
    }

    private val circlePaint = Paint().apply {
        color = Color.parseColor("#00C853")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val circleBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3.0f
        isAntiAlias = true
    }

    private val tickPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        val rect = translateRect(boundingBox)

        canvas.drawRect(rect, boxPaint)

        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val radius = 35f

        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.drawCircle(centerX, centerY, radius, circleBorderPaint)

        val path = Path()
        path.moveTo(centerX - 15f, centerY)
        path.lineTo(centerX - 5f, centerY + 12f)
        path.lineTo(centerX + 18f, centerY - 12f)
        canvas.drawPath(path, tickPaint)
    }
}