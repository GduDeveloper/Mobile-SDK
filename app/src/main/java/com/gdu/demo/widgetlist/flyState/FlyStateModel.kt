package com.gdu.demo.widgetlist.flyState

import com.gdu.config.GlobalVariable
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FlyStateModel : WidgetModel() {


    override fun onStart() {

        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    private fun updateState() {
        val distance = GlobalVariable.flyDistance
        // 相对高度

         var height = if (GlobalVariable.droneFlyState.toInt() == 1) {
             0
         } else {
            GlobalVariable.height_drone
        }
        val hs = GlobalVariable.xekf_VelX.toInt()
        val vs = GlobalVariable.xekf_VelD.toInt()

        var headAngel = GlobalVariable.planeAngle / 100.0f
        if (headAngel < 0) {
            headAngel = headAngel + 360
        }
        // 椭球高
        val ellipsoid_height = GlobalVariable.altitude_drone
        // 海拔高
        val alt = GlobalVariable.asl_drone

        notify(dataChangeChannel, FlyStateValue(distance, height, hs, vs, headAngel, ellipsoid_height, alt))
    }

    override fun onDestroy() {
        cancel()
    }
}