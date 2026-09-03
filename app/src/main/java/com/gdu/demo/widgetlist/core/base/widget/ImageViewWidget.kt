package com.gdu.demo.widgetlist.core.base.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author wuqb
 * @date 2026/8/28 17:10
 * @description 这里写描述
 */
abstract class ImageViewWidget<T: WidgetModel> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    protected var widgetModel: T
    /**是否懒加载*/
    protected var lazyLoad: Boolean = false
    private var isInflated = false

    init {
        if (isLazyLoad()){
            visibility = GONE
            setWillNotDraw(true) // 设置视图不绘制
        }else {
            initView(context, attrs, defStyleAttr)
        }
        widgetModel = initWidgetModel()
    }

    protected abstract fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)


    protected abstract fun initWidgetModel():T

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode){
            initData()
        }
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            destroy()
        }
        super.onDetachedFromWindow()
    }

    protected open fun initData(){}

    protected open fun destroy(){

    }

    /**
     * 加载隐藏视图
     */
    fun inflate() {
        if (isInflated) {
            return
        }

        visibility = VISIBLE
        isInflated = true

        setWillNotDraw(false)
        requestLayout()
        invalidate()

        onViewInflated()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isLazyLoad() && !isInflated) {
            setMeasuredDimension(0, 0)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (!isLazyLoad() || !isInflated) {
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    override fun draw(canvas: Canvas) {
        if (!isLazyLoad() || !isInflated) {
            super.draw(canvas)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (isInflated) {
            super.dispatchDraw(canvas)
        }
    }

    protected fun isLazyLoad():Boolean{
        return lazyLoad
    }

    protected open fun onViewInflated(){}
}