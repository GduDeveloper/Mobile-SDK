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
import com.gdu.msdk.key.value.bean.GimbalType
import com.gdu.msdk.key.value.bean.LightType
import com.gdu.msdk.key.value.common.EmptyMsg
import kotlin.math.abs

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
object CameraAction: IKeyAction {

    /** 变焦(大/小)最后设置时间  */
    private var lastSetZoomTime: Long = 0

    override fun doAction(actionId: Int) {
        when(actionId) {
            // 相机
            ICustomAction.KEY_CAMERA_ZOOM_IN -> { // 变焦放大
                handlerZoomAdd()
            }
            ICustomAction.KEY_CAMERA_ZOOM_OUT -> { // 变焦缩小
                handlerZoomSub()
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


    /**
     * 变焦放大
     */
    fun handlerZoomAdd() {
        // TODO RcCustomKeyManager.handlerZoomSub 有判断航迹是否执行
        val size = ICamera.get.currentVLCameraZoom.value.toInt().toShort()
        log("[handlerZoomAdd] cameraZoom = $size; gimbalType = ${IGimbal.get.gimbalType}")
        if (size < 0) {
            return
        }

        var setSize: Short = 0

        if (IGimbal.get.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT
            || IGimbal.get.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW) {

            if (size < 18) {
                setSize = (size + 1).toShort()
            } else if (size.toInt() == 18) {
                setSize = 36
            } else if (size.toInt() == 36) {
                setSize = 54
            } else if (size.toInt() == 54) {
                setSize = 72
            } else if (size.toInt() == 72) {
                setSize = 108
            } else if (size.toInt() == 108) {
                setSize = 144
            }

        } else if (IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S200
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S200_IR640
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_S_IR640_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PTL_S220_IR640
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_MICRO_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_SE
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_PZ
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_SE_PZ
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PWG01
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PWG01SE
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_P300PWG
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_P300SE
        ) {
            if (size < 10) {
                setSize = (size + 1).toShort()
            } else if (size < 20) {
                setSize = 20
            } else if (size < 30) {
                setSize = 30
            } else if (size < 40) {
                setSize = 40
            } else if (size < 50) {
                setSize = 50
            } else if (size < 60) {
                setSize = 60
            } else if (size < 70) {
                setSize = 70
            } else if (size < 80) {
                setSize = 80
            } else if (size < 90) {
                setSize = 90
            } else if (size < 100) {
                setSize = 100
            } else if (size < 110) {
                setSize = 110
            } else if (size < 120) {
                setSize = 120
            } else if (size < 130) {
                setSize = 130
            } else if (size < 140) {
                setSize = 140
            } else if (size < 150) {
                setSize = 150
            } else if (size <= 160) {
                setSize = 160
            }
        }

        if (setSize.toInt() == 0) {
            log("[handlerZoomAdd] setSize == 0")
            return
        }

        setSize = (setSize * 10).toShort()

        if (abs(System.currentTimeMillis() - lastSetZoomTime) < 300) {
            log("[handlerZoomAdd] lastSetZoomTime less than 300")
            return
        }

        lastSetZoomTime = System.currentTimeMillis()
        log("[handlerZoomAdd] size = $size, setSize = $setSize")

        IGduDroneDevice.get.camera.setZoomSizeRatio(setSize) {}
    }

    /**
     * 变焦缩小
     */
    fun handlerZoomSub() {

        val size = ICamera.get.currentVLCameraZoom.value.toInt().toShort()
        log("[handlerZoomSub] cameraZoom = $size; gimbalType = ${IGimbal.get.gimbalType}")
        if (size < 0) {
            return
        }

        var setSize: Short = 0

        if (IGimbal.get.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT
            || IGimbal.get.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW) {

            if (size <= 1) {
                setSize = 0
            } else if (size <= 18) {
                setSize = (size - 1).toShort()
            } else if (size.toInt() == 36) {
                setSize = 18
            } else if (size.toInt() == 54) {
                setSize = 36
            } else if (size.toInt() == 72) {
                setSize = 54
            } else if (size.toInt() == 108) {
                setSize = 72
            } else if (size.toInt() == 144) {
                setSize = 108
            }

        } else if (IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S200
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S200_IR640
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PDL_S220PRO_S_IR640_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PTL_S220_IR640
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_MICRO_FOUR_LIGHT
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_SE
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_PZ
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PQL02_SE_PZ
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PWG01
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_PWG01SE
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_P300PWG
            || IGimbal.get.gimbalType === GimbalType.GIMBAL_P300SE
        ) {

            setSize = if (size > 160) {
                160
            } else if (size > 150) {
                150
            } else if (size > 140) {
                140
            } else if (size > 130) {
                130
            } else if (size > 120) {
                120
            } else if (size > 110) {
                110
            } else if (size > 100) {
                100
            } else if (size > 90) {
                90
            } else if (size > 80) {
                80
            } else if (size > 70) {
                70
            } else if (size > 60) {
                60
            } else if (size > 50) {
                50
            } else if (size > 40) {
                40
            } else if (size > 30) {
                30
            } else if (size > 20) {
                20
            } else if (size > 10) {
                10
            } else if (size > 0) {
                (size - 1).toShort()
            } else {
                0
            }
        }

        if (setSize.toInt() == 0) {
            log("[handlerZoomSub] setSize == 0")
            return
        }

        setSize = (setSize * 10).toShort()

        if (abs(System.currentTimeMillis() - lastSetZoomTime) < 300) {
            log("[handlerZoomSub] lastSetZoomTime less than 300")
            return
        }

        lastSetZoomTime = System.currentTimeMillis()
        log("[handlerZoomSub] size = $size, setSize = $setSize")

        IGduDroneDevice.get.camera.setZoomSizeRatio(setSize) {}

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