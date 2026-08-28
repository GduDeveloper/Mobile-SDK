package com.gdu.demo.widgetlist.takephoto

import android.content.Context
import android.util.AttributeSet
import cc.taylorzhang.singleclick.SingleClickUtil
import com.gdu.demo.R
import com.gdu.demo.databinding.LayoutTakePhotoBinding
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

        SingleClickUtil.onSingleClick(binding.ivPhotoAndVideoBtn,
                1500, false) {
            widgetModel?.takePhoto()
        }
    }

    override fun initWidgetModel(): TakePhotoViewModel = TakePhotoViewModel()



    override fun bindingData(data: Any) {
    }

    override fun initData() {
        super.initData()
        widgetCollectFlowData(widgetModel?.shootViewState){status->
            setShootStatus(status)
        }
    }

    /**
     * 更新当前拍摄类型
     * @param type
     * */
    private fun setShootStatus(type: ShootStatus) {
        XLogger.APP.i("setShootStatus:${type}")
        when (type) {
            ShootStatus.NO_SDCARD -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_video_no_sdcard)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.SD_ERROR -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_video_sdcard_error)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.SD_FULL -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_video_sdcard_full)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.PICTURE_ENABLE,ShootStatus.ISPHOTOING_ENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_photograph)
                binding.ivPhotoAndVideoBtn.isEnabled = true
            }

            ShootStatus.PICTURE_UNABLE,ShootStatus.ISPHOTOING_UNABLE, ShootStatus.ISSINGLEPHOTOING_UNABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_photo_unable)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.ISRECORDING_ENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_video_recording)
                binding.ivPhotoAndVideoBtn.isEnabled = true
                //录像中，变更返航按钮显示位置
//                changeCameraControl()
            }

            ShootStatus.ISRECORDING_UNABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_video_unable)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.VIDEO_ENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_video_record)
                binding.ivPhotoAndVideoBtn.isEnabled = true
                //录像中，变更返航按钮显示位置
//                changeCameraControl()
            }

            ShootStatus.VIDEO_UNENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_video_record)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            else -> {
            }
        }
    }
}