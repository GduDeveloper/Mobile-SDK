package com.gdu.demo.widgetlist.flyState

import android.content.Context
import android.util.AttributeSet
import com.gdu.demo.R
import com.gdu.demo.databinding.FlyStateLayoutBinding
import com.gdu.demo.databinding.TopStateViewLayoutBinding
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class FlyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<FlyStateModel>(context, attrs, defStyleAttr)  {


    private lateinit var binding: FlyStateLayoutBinding



    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = FlyStateLayoutBinding.bind(inflate(context, R.layout.fly_state_layout, this))

    }

    override fun initWidgetModel(): FlyStateModel = FlyStateModel()

    override fun bindingData(data: Any) {
    }
}