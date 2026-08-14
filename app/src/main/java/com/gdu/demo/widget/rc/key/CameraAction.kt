package com.gdu.demo.widget.rc.key

import com.gdu.demo.R
import com.gdu.lib.util.core.ToastUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.component.interfaces.ICamera
import com.gdu.msdk.device.component.interfaces.IGimbal
import com.gdu.msdk.device.component.pod.utils.CameraUtils
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.key.callback.MSdkCallback
import com.gdu.msdk.key.error.MError
import com.gdu.msdk.key.value.bean.LightType
import com.gdu.msdk.key.value.common.EmptyMsg

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
object CameraAction: IKeyAction {

    override fun doAction(actionId: Int) {
        when(actionId) {
            // 相机
            ICustomAction.KEY_CAMERA_ZOOM_IN -> { // 变焦放大
//                TheRouter.get(IVideoService::class.java)?.handleVideoCameraZoomAdd()
            }
            ICustomAction.KEY_CAMERA_ZOOM_OUT -> { // 变焦缩小
//                TheRouter.get(IVideoService::class.java)?.handleVideoCameraZoomSub()
            }
            ICustomAction.KEY_CAMERA_INCREASE_EV -> { // 增加EV值
                changeEV(true)
            }
            ICustomAction.KEY_CAMERA_DECREASE_EV -> { // 减小EV值
                changeEV(false)
            }
            ICustomAction.KEY_CAMERA_SWITCH_MODE -> { // 切换画面显示模式
                changeModel()
            }
            ICustomAction.KEY_CAMERA_OPEN_FFC -> { // open ffc
                openFFC()
            }
            ICustomAction.KEY_CAMERA_SWITCH_TEMP_ALARM -> { // 开启/关闭高温报警
//                changeTempAlarm()
            }
        }
    }


    private fun changeEV(add: Boolean) {
        // RcCustomKeyManager.changeEv
        // todo isOpenFlightRoutePlan 如果开启了航迹
        // todo isNightModeOpen 夜视模式开启则不能调ev
        // todo isSinglePhotoTaking 各自拍照不能调ev

        if (!IGduDroneDevice.get.isConnected) {
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }

        val evValueArray = CameraUtils.getEVValuesByGimbalType(IGimbal.get.gimbalType)
        val vlEV = ICamera.get.currentCameraStatus.visibleCameraStatus.value?.lightEvValue
        if (vlEV == null) {
            ToastUtils.showShort(R.string.camera_param_acquired)
            return
        }
        var position = -1
        for (i in evValueArray.indices) {
            if (vlEV.toInt() == evValueArray[i]) {
                position = i
                break
            }
        }
        XLogger.APP.i("CameraAction", "[changeEV] add = $add, vlEV = $vlEV, position = $position, type = ${IGimbal.get.gimbalType}")
        if (position == -1) {
            ToastUtils.showShort(R.string.camera_param_acquired)
            return
        }

        var setEV = -1
        if (add) {
            if (position < evValueArray.size - 1) {
                setEV = evValueArray[position + 1]
            }
        } else {
            if (position > 0) {
                setEV = evValueArray[position - 1]
            }
        }
        if (setEV == -1) {
            return
        }

        if (ICamera.get.supportFun.enableCameraCapability) {
            IGduDroneDevice.get.camera.setEVValuePtr(setEV
            ) { result ->
                if (result.success) {
                    ToastUtils.showShort(R.string.ui_setting_success)
                } else {
                    ToastUtils.showShort(R.string.ui_setting_failed)
                }
            }
        } else {
            ICamera.get.setEVValue(setEV.toByte(), object: MSdkCallback.ActionCallback<EmptyMsg>{
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort(R.string.ui_setting_success)
                }
                override fun onFailure(error: MError) {
                    ToastUtils.showShort(R.string.ui_setting_failed)
                }
            })
        }
    }

    private fun changeModel() {
        // RcCustomKeyManager.changeModel()
        // todo 如果是夜视模式 录像中, 开启了视觉跟踪
        if (!IGduDroneDevice.get.isConnected) {
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }

//        if (!(ScreenModeManager.instance.screenMode == ScreenMode.MAIN_VIDEO
//            || ScreenModeManager.instance.screenMode == ScreenMode.MAIN_MAP)) {
//            ToastUtils.showShort(R.string.camera_no_change_light_by_screen_mode)
//            return
//        }

        val types = ICamera.get.supportFun.lightTypes
        val list = mutableListOf<Byte>()
        types?.forEach { light ->
            val value = when (light) {
                LightType.VISIBLE_LIGHT_ZOOM -> 6.toByte() // 变焦
                LightType.INFRARED_LIGHT -> 0.toByte() // 红外
                else -> 5.toByte() // 广角
            }
            list.add(value)
        }
        if (list.isEmpty()) {
            ToastUtils.showShort(R.string.camera_no_light_type)
            return
        }

        val current = ICamera.get.lightType.value
        var nextIndex = 0
        for ((index, value) in list.withIndex()) {
            if (value == current) {
                nextIndex = index + 1
            }
        }

        var nextValue = 5.toByte()
        try {
            nextValue = list[nextIndex]
        } catch (ignore: Exception) {
        }

        XLogger.APP.i("CameraAction", "[changeModel] currentLightType = $current, nextIndex = $nextIndex, nextValue = $nextValue")

        ICamera.get.switchImageMode(nextValue, null)
//        ActivityManager.getInstance().flightHomeActivity?.let {
//            HiltRouter.router(it, IVideoScopePApi::class.java).mainLightTypeController().switchLightType(LightType.get(nextValue.toInt()))
//        }
    }

    private fun openFFC() {
        if (!IGduDroneDevice.get.isConnected) {
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }
        if (ICamera.get.lightType.value != 0.toByte()) {
            ToastUtils.showShort(R.string.camera_no_infrared_light)
            return
        }
        if (ICamera.get.supportFun.enableCameraCapability) {
            ICamera.get.setIRManualFFCPtr {
                ToastUtils.showShort(if (it.success) R.string.ui_setting_success else R.string.ui_setting_failed)
            }
        } else {
            ICamera.get.setIRManualFFC {
                ToastUtils.showShort(if (it.success) R.string.ui_setting_success else R.string.ui_setting_failed)
            }
        }
    }

    private fun changeTempAlarm() {
        if (!IGduDroneDevice.get.isConnected) {
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }
//        val open = TheRouter.get(IVideoService::class.java)?.getIRTempAlarmSwitch()?: false
//        TheRouter.get(IVideoService::class.java)?.setIRTempAlarmSwitch(!open)
//        ToastUtils.showShort(R.string.ui_setting_success)
    }
}