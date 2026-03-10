package com.scanx.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()
    var imageWidth: Int = 0
    var imageHeight: Int = 0

    abstract class Graphic(protected val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)

        fun translateRect(rect: Rect): RectF {
            val scaleX = overlay.width.toFloat() / overlay.imageWidth.toFloat()
            val scaleY = overlay.height.toFloat() / overlay.imageHeight.toFloat()
            val scale = max(scaleX, scaleY)
            val offsetX = (overlay.width - overlay.imageWidth * scale) / 2f
            val offsetY = (overlay.height - overlay.imageHeight * scale) / 2f

            return RectF(
                rect.left * scale + offsetX,
                rect.top * scale + offsetY,
                rect.right * scale + offsetX,
                rect.bottom * scale + offsetY
            )
        }
    }

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
        postInvalidate()
    }

    fun setImageSourceInfo(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}