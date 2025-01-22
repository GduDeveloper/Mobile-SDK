package com.gdu.demo.widgetlist.rtk

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class RTKStateModel : WidgetModel() {

    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            notify(dataChangeChannel, RTKStateValue(-1, ""))
            return;
        }
        val currentSatellite = GlobalVariable.satellite_drone
        val tkStatus = GlobalVariable.rtk_model.rtk1_status?:""
        notify(dataChangeChannel, RTKStateValue(currentSatellite, tkStatus))
    }
    override fun onDestroy() {
        cancel()
    }

}