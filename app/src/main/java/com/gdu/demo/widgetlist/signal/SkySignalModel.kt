package com.gdu.demo.widgetlist.signal

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers


class SkySignalModel: WidgetModel() {

    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            notify(-1)
        } else {
            notify(GlobalVariable.arlink_skyMcs)
        }
    }
}