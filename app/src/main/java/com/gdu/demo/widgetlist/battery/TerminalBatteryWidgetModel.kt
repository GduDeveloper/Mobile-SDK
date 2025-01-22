package com.gdu.demo.widgetlist.battery

import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.battery.bean.BatteryState
import com.gdu.demo.widgetlist.battery.bean.BatteryStatus
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * @author wuqb
 * @date 2024/11/13
 * @description TODO
 */
class TerminalBatteryWidgetModel: WidgetModel() {
    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                var status = BatteryStatus.DEFAULT
                val battery = GlobalVariable.power_rc
                if (battery < 0) {
                    val data = BatteryState.SingleBatteryState(0, 0f, status)
                    notify(dataChangeChannel, data)
                    return@collectLatest
                }
                // 遥控器电量低于20 播报语音一次 避免首次进入立即播报语音过多8秒后提示
                if (battery <= 20) {
                    status = BatteryStatus.WARNING_LEVEL_2
                    if (GlobalVariable.oneLevelLowBattery <= 20) {
                        status = BatteryStatus.WARNING_LEVEL_1
                    }
                } else {
                    status = BatteryStatus.NORMAL
                }
                val data = BatteryState.SingleBatteryState(battery, 0f, status)
                notify(dataChangeChannel, data)
            }
        }
    }

    override fun onDestroy() {
        cancel()
    }
}