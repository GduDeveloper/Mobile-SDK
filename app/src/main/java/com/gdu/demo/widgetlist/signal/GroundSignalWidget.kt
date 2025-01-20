package com.gdu.demo.widgetlist.signal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gdu.demo.R
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class GroundSignalWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<GroundSignalModel>(context, attrs, defStyleAttr) {


    private val tvGtQuality = findViewById<SignalQuality>(R.id.tv_gt_quality)

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.ux_widget_grand_signal_layout, this)
    }

    override fun initWidgetModel(): GroundSignalModel = GroundSignalModel()


    override fun bindingData(data: Any) {
        when (data) {
            is GroundSignalValue->{
                tvGtQuality.setMCSQuality(data.signal)
            }
        }
    }
}