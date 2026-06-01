package com.gdu.demo.widgetlist.rtk

import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IRTK
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


class RTKStateModel : WidgetModel() {

    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            notify(RTKStateValue(-1, ""))
            return;
        }
        val currentSatellite = IRTK.get.gnssInfo?.mainSatelliteDrone?: 0
        val tkStatus = IRTK.get.rtkFcInfo.rtkModel?.rtk1State?.state ?: ""
        notify(RTKStateValue(currentSatellite, tkStatus))
    }
}