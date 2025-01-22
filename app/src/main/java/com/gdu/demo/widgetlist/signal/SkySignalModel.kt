package com.gdu.demo.widgetlist.signal

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SkySignalModel: WidgetModel() {


    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            notify(dataChangeChannel, -1)
        } else {
            notify(dataChangeChannel, GlobalVariable.arlink_skyMcs)
        }
    }

    override fun onDestroy() {
        cancel()
    }
}