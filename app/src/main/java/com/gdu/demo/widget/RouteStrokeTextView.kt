package com.gdu.demo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.gdu.demo.R

class RouteStrokeTextView : AppCompatTextView {

    private var mStroke = true
    private var mStrokeWidth = 0.0f
    private var mStrokeColor = 0

    constructor(context: Context, strokeWidth: Float, strokeColor: Int) : this(context, null) {
        this.mStrokeWidth = strokeWidth
        this.mStrokeColor = strokeColor
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RouteStrokeTextView)
        mStrokeColor = array.getColor(R.styleable.RouteStrokeTextView_strokeColor, Color.WHITE)
        mStrokeWidth = array.getDimension(R.styleable.RouteStrokeTextView_strokeWidth, 1.0f)
        array.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        if (mStroke) {
            // 复制原生TextView的Paint对象为描边Paint对象
            val mStrokePaint: Paint = paint
            // 设置描边宽度
            mStrokePaint.strokeWidth = mStrokeWidth
            // 设置描边颜色
            mStrokePaint.color = mStrokeColor
            // 描边要放在填充颜色的外面
            mStrokePaint.style = Paint.Style.STROKE
            mStrokePaint.isAntiAlias = true
            val mTextStr = text.toString()
            // 保存画布状态
            canvas.save()
            // 应用padding
            canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())

            // 绘制描边文字（必须绘制在原来的文字下面）
            val mLayout = layout
            if (mLayout != null) {
                for (i in 0 until mLayout.lineCount) {
                    val lineBaseline = mLayout.getLineBaseline(i)
                    val lineStart = mLayout.getLineStart(i)
                    val lineEnd = mLayout.getLineEnd(i)
                    val lineStr = mTextStr.substring(lineStart, lineEnd)
                    val x = mLayout.getLineLeft(i)
                    val y = lineBaseline.toFloat()
                    canvas.drawText(lineStr, x, y, mStrokePaint)
                    mStrokePaint.color = currentTextColor
                    mStrokePaint.strokeWidth = 0f
                    mStrokePaint.style = Paint.Style.FILL
                    canvas.drawText(lineStr, x, y, mStrokePaint)
                }
            }
            // 恢复画布状态
            canvas.restore()
        }
        // super.onDraw(canvas);
    }

    fun setStroke(stroke: Boolean) {
        mStroke = stroke
    }

    fun setStrokeWidth(strokeWidth: Float) {
        mStrokeWidth = strokeWidth
    }

    fun setStrokeColor(strokeColor: Int) {
        mStrokeColor = strokeColor
    }
}