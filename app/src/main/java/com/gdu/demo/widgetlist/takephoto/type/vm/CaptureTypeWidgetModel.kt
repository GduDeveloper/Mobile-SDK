package com.gdu.demo.widgetlist.takephoto.type.vm

import androidx.lifecycle.viewModelScope
import com.gdu.demo.R
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.demo.widgetlist.takephoto.type.CaptureType
import com.gdu.demo.widgetlist.takephoto.type.repo.CaptureTypeRepository
import com.gdu.demo.widgetlist.takephoto.type.repo.CaptureTypeRepositoryImpl
import com.gdu.lib.util.core.ToastUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.videoprocess.VideoProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * @author wuqb
 * @date 2026/8/28 16:57
 * @description 拍照类型切换视图模型
 */
class CaptureTypeWidgetModel: WidgetModel() {
    val shootTypeRepository: CaptureTypeRepository = CaptureTypeRepositoryImpl()
    /**
     * 是否正在切换拍照/录像模式
     */
    private val _isSwitchingMode = MutableStateFlow(false)

    /**
     * 拍照录像模式
     */
    private val _photoModeState = MutableStateFlow(CaptureType.PICTURE)
    /**
     * 拍照录像模式(对外)
     */
    val photoModeState: StateFlow<CaptureType>
        get() = _photoModeState.asStateFlow()

    init {
        viewModelScope.launch {
            shootTypeRepository.photoModeFlow.collect {
                if (it) {
                    _photoModeState.value = CaptureType.PICTURE
                    VideoProcessor.getInstance().setVideoMode(1)
                } else {
                    _photoModeState.value = CaptureType.VIDEO
                    VideoProcessor.getInstance().setVideoMode(0)
                }
            }
        }
    }

    /**
     * 设置当前拍摄类型
     * */
    fun setCaptureType(newShootType: CaptureType) {
        if (IGduDroneDevice.get.camera.isPhotoing.value || IGduDroneDevice.get.camera.isRecording.value) {
            ToastUtils.showShort(R.string.camera_working_tip)
            return
        }
        if (!isConnect()) {
            XLogger.APP.i("飞行器未连接10")
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }
        if (IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            ToastUtils.showShort(R.string.gimbal_not_connect)
            return
        }
        if ((newShootType == CaptureType.PICTURE
                    && IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value)
            || (newShootType == CaptureType.VIDEO
                    && !IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value)
        ) {
            XLogger.APP.i("setShootType() current already is:${IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value}"
            )
            return
        }
        XLogger.APP.i("setShootType() start")
        _isSwitchingMode.value = true
        when (newShootType) {
            CaptureType.VIDEO -> {
                changeVideoMode()
            }

            else -> {
                changePhotoMode()
            }
        }
    }

    /**
     * 设置相机拍照模式
     * */
    private fun changePhotoMode() {
        VideoProcessor.getInstance().setVideoMode(1)
        viewModelScope.launch {
            shootTypeRepository.switchCameraMode(true).catch {
                _isSwitchingMode.value = false
                ToastUtils.showShort(R.string.ui_setting_failed)
            }.collect { result ->
                _isSwitchingMode.value = false
                if (!result.success) {
                    ToastUtils.showShort(R.string.switch_fail)
                }
            }
        }

    }

    /**
     * 切换为录像模式
     * */
    private fun changeVideoMode() {
        VideoProcessor.getInstance().setVideoMode(0)

        if (!IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            viewModelScope.launch {
                shootTypeRepository.switchCameraMode(false).catch {
                    _isSwitchingMode.value = false
                    ToastUtils.showShort(R.string.ui_setting_failed)
                }.collect { result ->
                    _isSwitchingMode.value = false
                    if (!result.success) {
                        ToastUtils.showShort(R.string.switch_fail)
                    }
                }
            }
        } else {
            _isSwitchingMode.value = false
        }
    }

    override fun onStart() {

    }
}