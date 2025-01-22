package com.gdu.demo.widgetlist.battery

import com.gdu.config.GlobalVariable
import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.widgetlist.battery.bean.BatteryState
import com.gdu.demo.widgetlist.battery.bean.BatteryStatus
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * @author wuqb
 * @date 2024/11/11
 * @description 飞机电量Widget数据模型
 */
class BatteryWidgetModel: WidgetModel() {

    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                update()
            }
        }
    }

    private fun update(){
        SdkDemoApplication.getAircraftInstance().battery.setStateCallback {
            val dronePower = it.chargeRemainingInPercent?: 0
            GlobalVariable.power_drone = dronePower
            var voltageLevel = 0.0f
            // 飞机电压
            if (GlobalVariable.flight_voltage > 0) {
                val voltage = BigDecimal((GlobalVariable.flight_voltage / 1000f).toDouble()).setScale(1, RoundingMode.HALF_UP)
                voltageLevel = voltage.toFloat()
            }
            var status  = BatteryStatus.NORMAL
            when (GlobalVariable.batteryAbnormalCode) {
                1.toByte(),4.toByte() -> status = BatteryStatus.ERROR
                2.toByte() -> status = BatteryStatus.WARNING_LEVEL_2
            }

            //小于超低电量 闪烁快
            if (dronePower <= 10) {
                status = BatteryStatus.ERROR
            }
            val data = BatteryState.SingleBatteryState(dronePower, voltageLevel, status)
            notify(dataChangeChannel, data)
        }
    }

    override fun onDestroy() {
        cancel()
    }
}