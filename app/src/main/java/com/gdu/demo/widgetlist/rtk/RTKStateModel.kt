package com.gdu.demo.widgetlist.rtk

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


class RTKStateModel : WidgetModel() {

    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            notify(RTKStateValue(-1, ""))
            return;
        }
        val currentSatellite = GlobalVariable.satellite_drone
        val tkStatus = GlobalVariable.rtk_model.rtk1_status ?: ""
        notify(RTKStateValue(currentSatellite, tkStatus))
    }
}