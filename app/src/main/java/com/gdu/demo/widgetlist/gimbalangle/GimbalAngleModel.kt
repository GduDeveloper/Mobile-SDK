package com.gdu.demo.widgetlist.gimbalangle

import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GimbalAngleModel: WidgetModel()  {


    override fun onStart() {
        launch {
            intervalFlow(200).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        notify(dataChangeChannel, GimbalAngleValue(Math.round(GlobalVariable.HolderPitch * 1.0 / 100).toInt()))
    }
    override fun onDestroy() {
        cancel()
    }
}