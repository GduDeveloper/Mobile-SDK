package com.gdu.demo.widgetlist.battery.bean

/**
 * @author wuqb
 * @date 2024/11/11
 * @description 电池状态及对象信息
 */
sealed class BatteryState {

    /**
     * 单个电池连接
     *
     * @property percentageRemaining - 剩余电量百分比
     * @property voltageLevel - 电压等级
     * @property batteryStatus - 电池当前状态
     */
    data class SingleBatteryState(
        val percentageRemaining: Int,
        val voltageLevel: Float,
        val batteryStatus: BatteryStatus
    ) : BatteryState()
}