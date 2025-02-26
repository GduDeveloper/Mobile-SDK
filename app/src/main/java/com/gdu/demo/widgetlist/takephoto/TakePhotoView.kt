package com.gdu.demo.widgetlist.takephoto

import android.content.Context
import android.util.AttributeSet
import com.gdu.demo.R
import com.gdu.demo.databinding.LayoutLightSelectedBinding
import com.gdu.demo.databinding.LayoutTakePhotoBinding
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.demo.widgetlist.flyState.FlyStateModel
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class TakePhotoView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<WidgetModel>(context, attrs, defStyleAttr) {

    private lateinit var binding: LayoutTakePhotoBinding

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {

        binding = LayoutTakePhotoBinding.bind(inflate(context, R.layout.layout_take_photo, this))
    }

    override fun initWidgetModel(): WidgetModel = FlyStateModel()



    override fun bindingData(data: Any) {
    }


}