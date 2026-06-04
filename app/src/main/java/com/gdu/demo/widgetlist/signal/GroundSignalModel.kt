package com.gdu.demo.widgetlist.signal

import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IAirLink
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class GroundSignalModel: WidgetModel() {

    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            notify(GroundSignalValue(-1))
        } else {
            notify(GroundSignalValue(IAirLink.get.droneAirLinkInfo.value?.groundMcs?: 0))
        }
    }
}