package com.gdu.demo.widgetlist.takephoto.cameracapture

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import cc.taylorzhang.singleclick.SingleClickUtil
import com.gdu.demo.R
import com.gdu.demo.databinding.UxsdkWidgetCameraCaptureBinding
import com.gdu.demo.widgetlist.core.base.widget.ConstraintLayoutWidget
import com.gdu.demo.widgetlist.takephoto.ShootStatus
import com.gdu.demo.widgetlist.takephoto.cameracapture.vm.CameraCaptureWidgetModel
import com.gdu.lib.util.core.ToastUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.lib.util.extension.collectFlowData
import com.gdu.lib.util.extension.observeLiveData

/**
 * @author wuqb
 * @date 2026/8/31 10:46
 * @description 这里写描述
 */
class CameraCaptureWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<CameraCaptureWidgetModel>(context,attrs,defStyleAttr) {

    private lateinit var binding: UxsdkWidgetCameraCaptureBinding

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = UxsdkWidgetCameraCaptureBinding.bind(inflate(context, R.layout.uxsdk_widget_camera_capture, this))
        SingleClickUtil.onSingleClick(binding.ivPhotoAndVideoBtn,
            1500, false) {
            widgetModel?.takeCapture()
        }
    }



    override fun initData() {
        super.initData()
        observeLiveData(widgetModel?.showToastLiveData) {
            ToastUtils.showShort(it)
        }
        collectFlowData(widgetModel?.shootViewState){status->
            setShootStatus(status)
        }
        collectFlowData(widgetModel?.photoOrVideoTipState) { state ->
            when (state) {
                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.VideoInitTip -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.SinglePhotoInitTip -> {
                    updateRecordTime()
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.LowLightPhotoInitTip -> {
                    updateRecordTime()
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.PanoPhotoInitTip -> {
                    updateRecordTime()
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.TimingPhotoInitTip -> {
                    updateRecordTime()
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.ContinuingPhotoInitTip -> {
                    updateRecordTime()
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.SinglePhotoing -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.ContinuingPhotoing -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.TimingPhotoing -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.LowLightPhotoing -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.PanoPhotoing -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.Recording -> {
                    updateRecordTime(state.tip)
                }

                is CameraCaptureWidgetModel.PhotoOrVideoTipStatus.Hide -> {
                    updateRecordTime()
                }

                else -> {}
            }
        }

        collectFlowData(widgetModel?.uiEventState) { state ->
            when (state) {
                is CameraCaptureWidgetModel.UiEvent.Tip -> {
                    ToastUtils.showShort(state.tip)
                }

                else -> {}
            }
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
            }

            ShootStatus.ISRECORDING_UNABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.ic_gimbal_video_unable)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            ShootStatus.VIDEO_ENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_video_record)
                binding.ivPhotoAndVideoBtn.isEnabled = true
            }

            ShootStatus.VIDEO_UNENABLE -> {
                binding.ivPhotoAndVideoBtn.setImageResource(R.drawable.gimbal_ic_video_record)
                binding.ivPhotoAndVideoBtn.isEnabled = false
            }

            else -> {
            }
        }
    }

    /**
     * 更新录像时间
     * */
    private fun updateRecordTime(time: String = "") {
        binding.recordVideoTime.isVisible = time.isNotEmpty()
        binding.recordVideoTime.text = time
    }

    override fun initWidgetModel(): CameraCaptureWidgetModel  = CameraCaptureWidgetModel()

    override fun bindingData(data: Any) {

    }
}