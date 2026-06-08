package com.gdu.demo.widgetlist.battery

import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.utils.DroneUtils
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.battery.bean.BatteryState
import com.gdu.demo.widgetlist.battery.bean.BatteryStatus
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IBattery
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.cancel
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * @author wuqb
 * @date 2024/11/11
 * @description 飞机电量Widget数据模型
 */
class BatteryWidgetModel: WidgetModel() {

    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { update() }
    }

    private fun update(){
        SdkDemoApplication.getAircraftInstance().battery.setStateCallback {
            val dronePower = it.chargeRemainingInPercent?: 0
            var voltageLevel = 0.0f
            // 飞机电压
            val flightVolatile = IBattery.get.droneBatteryInfo.value?.totalVoltage?: 0
            if (flightVolatile > 0) {
                val voltage = BigDecimal((flightVolatile / 1000f).toDouble()).setScale(1, RoundingMode.HALF_UP)
                voltageLevel = voltage.toFloat()
            }
            var status  = BatteryStatus.NORMAL
            // 电池报警: 0：无报警; 1：电量一级报警; 2：电量二级报警; 3：电量三级报警; 4：电池不在线
            val batteryAbnormalCode = DroneUtils.fcInfo1?.batteryAbnormalCode?: 0
            when (batteryAbnormalCode) {
                1.toByte(),4.toByte() -> status = BatteryStatus.ERROR
                2.toByte() -> status = BatteryStatus.WARNING_LEVEL_2
            }

            //小于超低电量 闪烁快
            if (dronePower <= 10) {
                status = BatteryStatus.ERROR
            }
            val data = BatteryState.SingleBatteryState(dronePower, voltageLevel, status)
            notify(data)
        }
    }

    override fun onDestroy() {
        cancel()
    }
}