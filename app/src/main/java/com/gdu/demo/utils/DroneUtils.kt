package com.gdu.demo.utils

import com.gdu.msdk.device.component.interfaces.IBattery
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.component.interfaces.IRTK
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.value.CycleBatteryInfo
import com.gdu.msdk.key.value.CycleFCInfo1
import com.gdu.msdk.key.value.CycleFCInfo2
import com.gdu.msdk.key.value.CycleFCInfo3
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
    val fcInfo2: CycleFCInfo2?
        get() = IFlightController.get.fcInfo2.value

    @JvmStatic
    val fcInfo3: CycleFCInfo3?
        get() = IFlightController.get.fcInfo3.value

    @JvmStatic
    val planeHadLock: Boolean
        get() {
            return fcInfo1?.planeHadLock?: true
        }

    @JvmStatic
    val droneFlyState: DroneFlyState
        get() = fcInfo1?.droneFlyState?: DroneFlyState.GROUND

    @JvmStatic
    val droneGpsLat: Double
        get() = fcInfo1?.latitude?: (-1).toDouble()

    @JvmStatic
    val backHeight: Int
        get() = fcInfo1?.backHeight?: 0

    @JvmStatic // 海拔高度
    val altitudeHeight: Int
        get() = fcInfo1?.altitudeHeight?: 0

    @JvmStatic // 椭球高度
    val ellipsoidHeight: Int
        get() = fcInfo1?.ellipsoidHeight?: 0

    @JvmStatic
    val droneGpsLon: Double
        get() = fcInfo1?.longitude?: (-1).toDouble()

    @JvmStatic
    val isGround: Boolean
        get() = droneFlyState.isGround()

    /** 是否允许切换飞行模式 */
    @JvmStatic
    val enableSwitchFlyMode: Boolean
        get() = fcInfo2?.enableSwitchFlyMode?: false

    @JvmField
    val isOpenTextEnvironment = false

    // 是否使用新得限高策略
    @JvmField
    val isNewHeightLimitStrategy = true

    /** 当前是否系留模式*/
    @JvmField
    val isTetherModel: Boolean = false

    /** 是否有虚拟按键*/
    @JvmField
    val isHasNavigationBar: Boolean = false

    @JvmStatic
    val controlHand: ControlHand
        get() = IGduRCDevice.get.rcControlHand.value?.controlHand?: ControlHand.HAND_AMERICA

    @JvmStatic
    val flyModel: FlyMode
        get() = fcInfo1?.flyModel?: FlyMode.ATTITUDE

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

    /** 单北斗定位系统状态  false：四系统； true：开启单北斗 */
    @JvmStatic
    val isBDSOpen: Boolean
        get() = IRTK.get.fcCoprocessorRtk?.isBDSOpen?: false

    /**
     * RTK 类型
     * 0：无效值
     * 1: 手机版网络RTK
     * 2: 基站版RTK
     * 3: 飞机板网络RTK
     * 5: 千寻SDK RTK
     */
    @JvmStatic
    val rtkType: Int
        get() = IRTK.get.onboardRTKInfo.value?.rtkType?.toInt()?: 0

    /** 机载rtk状态 0 未连接  1 连接中 2 已连接 */
    @JvmStatic
    val onboardRTKConnectState: Int
        get() = IRTK.get.onDroneRtkState.value

    @JvmStatic
    val rtkOnline: Boolean
        get() = fcInfo3?.rtkOnline?: false

    /**------------------------ 遥控器 ----------------------------------*/

    @JvmStatic
    val rcInfo: CycleRCInfo?
        get() = IGduRCDevice.get.rcInfo.value

    @JvmStatic
    val rcBatteryInfo: CycleRCBatteryInfo?
        get() = IGduRCDevice.get.rcBatteryInfo.value

}