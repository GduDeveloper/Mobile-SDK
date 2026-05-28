package com.gdu.demo.utils

import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.value.bean.ControlHand

object DroneUtils {

    @JvmStatic
    val planeHadLock: Boolean
        get() {
            return IFlightController.get.fcInfo1.value?.planeHadLock?: true
        }

    @JvmField
    val isOpenTextEnvironment = false

    // 是否使用新得限高策略
    @JvmField
    val isNewHeightLimitStrategy = true

    @JvmStatic
    val controlHand: ControlHand
        get() = IGduRCDevice.get.rcControlHand.value?.controlHand?: ControlHand.HAND_AMERICA

}