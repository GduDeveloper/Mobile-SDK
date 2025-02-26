package com.gdu.demo.widgetlist.lighttype

import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LightTypeModel : WidgetModel() {

    override fun onStart() {
        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        notify(dataChangeChannel, LightTypeValue(GlobalVariable.sCameraLightType.toInt()))
    }

    override fun onDestroy() {
    }
}