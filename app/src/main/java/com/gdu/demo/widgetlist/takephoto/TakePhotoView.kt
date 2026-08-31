package com.gdu.demo.widgetlist.takephoto

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import cc.taylorzhang.singleclick.SingleClickUtil
import com.gdu.demo.R
import com.gdu.demo.databinding.LayoutTakePhotoBinding
import com.gdu.demo.mediatest.MediaTestActivity
import com.gdu.demo.widgetlist.core.base.widget.ConstraintLayoutWidget
import com.gdu.lib.util.core.XLogger

class TakePhotoView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<TakePhotoViewModel>(context, attrs, defStyleAttr) {

    private lateinit var binding: LayoutTakePhotoBinding

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = LayoutTakePhotoBinding.bind(inflate(context, R.layout.layout_take_photo, this))

        binding.ivGalleryBtn.setOnClickListener {
            context.startActivity(Intent(context, MediaTestActivity::class.java))
        }
    }

    override fun initWidgetModel(): TakePhotoViewModel = TakePhotoViewModel()



    override fun bindingData(data: Any) {
    }
}