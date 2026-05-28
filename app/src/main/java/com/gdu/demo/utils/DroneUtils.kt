package com.gdu.demo.utils

import com.gdu.msdk.device.component.interfaces.IFlightController

object DroneUtils {

    @JvmStatic
    val planeHadLock: Boolean
        get() {
            return IFlightController.get.fcInfo1.value?.planeHadLock?: true
        }

    @JvmField
    val isOpenTextEnvironment = false

}