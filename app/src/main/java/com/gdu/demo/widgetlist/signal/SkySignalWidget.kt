package com.gdu.demo.widgetlist.signal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gdu.demo.R
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class SkySignalWidget@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<SkySignalModel>(context, attrs, defStyleAttr)  {

    private val tvSkyQuality = findViewById<SignalQuality>(R.id.tv_st_quality)

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.ux_widget_sky_signal_layout, this)
    }

    override fun initWidgetModel(): SkySignalModel = SkySignalModel()


    override fun bindingData(data: Any) {
        tvSkyQuality.setMCSQuality(data as? Int?:-1)
    }
}