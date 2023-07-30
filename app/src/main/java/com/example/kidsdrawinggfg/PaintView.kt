package com.example.kidsdrawinggfg

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
class PaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeList = mutableListOf<Stroke>()
    private val paintBrush = Paint()

    // TODO(Step 21.1 : A variable for array list of undo paths.)
    // START
    private val mUndoPaths = mutableListOf<Stroke>()

    init {
        initPaintBrush()
    }

    // TODO(Step 21.3 : A function to add the paths for undo option.)
    // START
    /**
     * This function is called when the user selects the undo
     * command from the application. This function removes the
     * last stroke input by the user depending on the
     * number of times undo has been activated.
     */

    fun onCLickUndo(){
        if(strokeList.size > 0){
            mUndoPaths.add(strokeList.removeAt(strokeList.size - 1))
            invalidate()    // Invalidate the whole view. If the view is visible
        }
    }

    private fun initPaintBrush() {
        paintBrush.isAntiAlias = true
        paintBrush.style = Paint.Style.STROKE
        paintBrush.strokeJoin = Paint.Join.ROUND
        paintBrush.strokeWidth = 5f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val stroke = Stroke(Path(), paintBrush.color, paintBrush.strokeWidth)
                stroke.path.moveTo(x, y)
                strokeList.add(stroke)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                strokeList.lastOrNull()?.path?.lineTo(x, y)
            }
        }

        invalidate()
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (stroke in strokeList) {
            paintBrush.color = stroke.color
            paintBrush.strokeWidth = stroke.brushSize
            canvas.drawPath(stroke.path, paintBrush)
        }
    }

    fun setSizeForBrush(newSize: Float) {
        paintBrush.strokeWidth = newSize
    }

    fun setCurrentBrushColor(color: Int) {
        paintBrush.color = color
    }

    private data class Stroke(val path: Path, val color: Int, val brushSize: Float)
}
