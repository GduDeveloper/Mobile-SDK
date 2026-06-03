package com.gdu.demo.widgetlist.gimbalangle

import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IGimbal
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class GimbalAngleModel: WidgetModel()  {


    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.QUICK_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        notify(GimbalAngleValue(Math.round((IGimbal.get.gimbalPosture.value?.holderPitch?.toFloat()?: 0f) * 1.0 / 100).toInt()))
    }
}