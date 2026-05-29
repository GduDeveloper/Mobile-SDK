package com.gdu.demo.utils

import com.gdu.msdk.device.component.interfaces.IBattery
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.value.CycleBatteryInfo
import com.gdu.msdk.key.value.CycleFCInfo1
import com.gdu.msdk.key.value.CycleRCBatteryInfo
import com.gdu.msdk.key.value.CycleRCInfo
import com.gdu.msdk.key.value.bean.ControlHand
import com.gdu.msdk.key.value.bean.DroneFlyState
import com.gdu.msdk.key.value.bean.FlyMode

object DroneUtils {

    @JvmStatic
    val fcInfo1: CycleFCInfo1?
        get() = IFlightController.get.fcInfo1.value

    @JvmStatic
    val planeHadLock: Boolean
        get() {
            return IFlightController.get.fcInfo1.value?.planeHadLock?: true
        }

    @JvmStatic
    val droneFlyState: DroneFlyState
        get() = IFlightController.get.fcInfo1.value?.droneFlyState?: DroneFlyState.GROUND

    @JvmStatic
    val isGround: Boolean
        get() = droneFlyState.isGround()

    @JvmField
    val isOpenTextEnvironment = false

    // 是否使用新得限高策略
    @JvmField
    val isNewHeightLimitStrategy = true

    /** 当前是否系留模式*/
    @JvmField
    val isTetherModel: Boolean = false

    @JvmStatic
    val controlHand: ControlHand
        get() = IGduRCDevice.get.rcControlHand.value?.controlHand?: ControlHand.HAND_AMERICA

    @JvmStatic
    val flyModel: FlyMode
        get() = IFlightController.get.fcInfo1.value?.flyModel?: FlyMode.ATTITUDE

    @JvmStatic
    val battery1InfoZ4C: CycleBatteryInfo?
        get() = IBattery.get.droneBatteryInfo.value

    @JvmStatic
    val oneLevelLowBattery: Int
        get() = IFlightController.get.currLowerBatteryWaring.oneLevel

    @JvmStatic
    val twoLevelLowBattery: Int
        get() = IFlightController.get.currLowerBatteryWaring.twoLevel

    @JvmField
    val isDahua: Boolean = false

    @JvmField
    val isDahuaBDS: Boolean = false


    /**------------------------ 遥控器 ----------------------------------*/

    @JvmStatic
    val rcInfo: CycleRCInfo?
        get() = IGduRCDevice.get.rcInfo.value

    @JvmStatic
    val rcBatteryInfo: CycleRCBatteryInfo?
        get() = IGduRCDevice.get.rcBatteryInfo.value

}