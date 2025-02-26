package com.gdu.demo.widgetlist.gimbalangle

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.gdu.demo.R
import com.gdu.demo.databinding.GimbalAngleLayoutBinding
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class GimbalAngleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<WidgetModel>(context, attrs, defStyleAttr) {

    private lateinit var binding: GimbalAngleLayoutBinding
    private var lastAngle = -1

    private val mBottomMaxAngle: Float = -90f
    private val mTopMaxAngle: Float = 35f



    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = GimbalAngleLayoutBinding.bind(inflate(context, R.layout.gimbal_angle_layout, this))
    }

    override fun initWidgetModel(): WidgetModel = GimbalAngleModel()

    override fun bindingData(data: Any) {
        when(data){
            is GimbalAngleValue -> {

                var currentAngele = data.angle
                if (lastAngle == currentAngele) {
                    return
                }
                if (currentAngele > mTopMaxAngle) {
                    currentAngele = mTopMaxAngle.toInt()
                } else if (currentAngele < mBottomMaxAngle) {
                    currentAngele = mBottomMaxAngle.toInt()
                }
                lastAngle = currentAngele

                val halfHeight = height / 2
                val halfAngleTextHeight = binding.tvAngle.height / 2

                var margeTop = if (lastAngle >= 0) {
                    halfHeight - ((lastAngle / mTopMaxAngle) * halfHeight).toInt() - halfAngleTextHeight
                } else {
                    halfHeight + (lastAngle / mBottomMaxAngle * halfHeight).toInt() - halfAngleTextHeight
                }
                if (margeTop > height - binding.tvAngle.height) {
                    margeTop = height - binding.tvAngle.height
                } else if (margeTop < 0) {
                    margeTop = 0
                }
                val params = binding.tvAngle.layoutParams as? RelativeLayout.LayoutParams


                params?.topMargin = margeTop
                binding.tvAngle.setLayoutParams(params)
                binding.tvAngle.text = "$lastAngle°"
            }
        }
    }


}