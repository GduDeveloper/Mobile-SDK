package com.gdu.demo.widgetlist.flyState

import com.gdu.demo.utils.DroneUtils
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IFlightController
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class FlyStateModel : WidgetModel() {


    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        val distance = IFlightController.get.fcInfo1.value?.flyDistance?: 0
        // 相对高度

        val height = if (DroneUtils.isGround) 0 else GlobalVariable.height_drone

        val hs = if (DroneUtils.isGround) 0 else GlobalVariable.xekf_VelX.toInt()

        val vs = if (DroneUtils.isGround) 0 else GlobalVariable.xekf_VelD.toInt()

        var headAngel = GlobalVariable.planeAngle / 100.0f
        if (headAngel < 0) {
            headAngel = headAngel + 360
        }
        // 椭球高
        val ellipsoid_height = DroneUtils.ellipsoidHeight
        // 海拔高
        val alt = DroneUtils.altitudeHeight

        notify(FlyStateValue(distance, height, hs, vs, headAngel, ellipsoid_height, alt))
    }
}