package com.gdu.demo.widgetlist.signal

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroundSignalModel: WidgetModel() {

    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            notify(dataChangeChannel, GroundSignalValue(-1))
        } else {
            notify(dataChangeChannel, GroundSignalValue(GlobalVariable.arlink_grdMcs))
        }
    }

    override fun onDestroy() {
        cancel()
    }
}