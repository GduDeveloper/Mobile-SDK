package com.gdu.demo.utils

import com.gdu.msdk.device.component.interfaces.IAi
import com.gdu.msdk.device.component.interfaces.IAirLink
import com.gdu.msdk.device.component.interfaces.IBattery
import com.gdu.msdk.device.component.interfaces.ICamera
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.component.interfaces.IRTK
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.value.CycleBatteryInfo
import com.gdu.msdk.key.value.CycleFCInfo1
import com.gdu.msdk.key.value.CycleFCInfo2
import com.gdu.msdk.key.value.CycleFCInfo3
import com.gdu.msdk.key.value.CycleInfraredCameraStatus
import com.gdu.msdk.key.value.CycleRCBatteryInfo
import com.gdu.msdk.key.value.CycleRCInfo
import com.gdu.msdk.key.value.CycleVisibleCameraStatus
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

    /** 是否返航中 */
    @JvmStatic
    val backState: Boolean
        get() = fcInfo1?.backState?: false

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

    @JvmStatic
    val isUseBackupsAirlink: Boolean
        get() = IAirLink.get.lteSdrStatus.value?.isUseBackupsAirlink?: false

    @JvmStatic // 是否有控制权，NetworkingHelper.isRCHasControlPower()
    val isRCHasControlPower: Boolean = true

    @JvmStatic
    var isTargetDetectMode: Boolean = false

    /** 算法检测的开关是否已经打开 */
    @JvmStatic
    var discernIsOpen: Boolean = false

    /** 光类型 0x00：红外图像； 0x01：红外画中画； 0x02：可见光图像；  0x03：可见光画中画； 0x04：融合默认；0x05：广角可见光；
     * 0x06：变焦可见光；0x07：分屏显示； 0x08：可见光为主融合； 0x09：红外为主融合； 0x0A：融合关； 0xff：步进切换 */
    @JvmStatic
    val lightType: Int
        get() = ICamera.get.lightType.value.toInt()

    @JvmStatic
    val visibleCameraStatus: CycleVisibleCameraStatus?
        get() = ICamera.get.currentCameraStatus.visibleCameraStatus.value

    @JvmStatic
    val infraredCameraStatus: CycleInfraredCameraStatus?
        get() = ICamera.get.currentCameraStatus.flowInfraredCameraStatus.value

    /** 可见光SD卡状态  0：正常卡；1：异常卡；2：当前卡读写速度慢；3：未插入SD卡；4：SD卡已满; 5: SD卡格式错误(目前仅四光有) */
    @JvmStatic
    val lightSDCardStatus: Int
        get() = visibleCameraStatus?.sdcardStatus?.toInt()?: 0

    @JvmStatic
    val irSDCardStatus: Int
        get() = infraredCameraStatus?.sdcardStatus?.toInt()?: 0

    @JvmStatic
    val lightESValue: Int
        get() = visibleCameraStatus?.lightESValue?: 0

    @JvmStatic
    val lightEvValue: Int
        get() = visibleCameraStatus?.lightEvValue?.toInt()?: 0

    @JvmStatic
    val lightISOValue: Int
        get() = visibleCameraStatus?.lightISOValue?: 0

    @JvmStatic
    val lightAELockValue: Int
        get() = if (visibleCameraStatus?.lightAELockValueEnable?: false) 1 else 2

    /** 可见光手/自动模式上报 0:自动模式； 1:手动模式 */
    @JvmStatic
    val isAutoMode: Boolean
        get() = visibleCameraStatus?.isAutoMode?: false

    /** 可见光拍照/视频模式状态  true:0-拍照模式； false: 1-视频模式 */
    @JvmStatic
    val isPhoto: Boolean
        get() = ICamera.get.currentCameraStatus.isPhotoMode.value

    @JvmStatic
    val aiBoxOnline: Boolean
        get() = IAi.get.aiBoxOnline.value?: false

    /**------------------------ 遥控器 ----------------------------------*/

    @JvmStatic
    val rcInfo: CycleRCInfo?
        get() = IGduRCDevice.get.rcInfo.value

    @JvmStatic
    val rcBatteryInfo: CycleRCBatteryInfo?
        get() = IGduRCDevice.get.rcBatteryInfo.value

}