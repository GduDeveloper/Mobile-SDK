package com.gdu.demo.widgetlist.camerapara

import com.gdu.demo.utils.CameraUtil
import com.gdu.demo.utils.DroneUtils
import com.gdu.demo.utils.MultiTimerManager
import com.gdu.demo.utils.MultiTimerManager.Companion.instance
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.msdk.device.component.interfaces.IGimbal
import com.gdu.msdk.device.component.pod.utils.SDCardManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class CameraParaModel: WidgetModel() {


    override fun onStart() {
        disposable = instance
            .getTimerObservable(MultiTimerManager.NORMAL_TIMER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateState() }
    }

    private fun updateState() {
        val sdCardState = SDCardState()
        val isMultiSDCard = CameraUtil.isSupportMultiSDCardGimbal(IGimbal.get.gimbalType)
        val vlStatus = SDCardManager.instance.checkVLSDCardStatus()
        val irState = SDCardManager.instance.checkIRSDCardStatus();
        sdCardState.isMultiSDCard = isMultiSDCard
        sdCardState.vlSDState = vlStatus
        sdCardState.irSDStatus = irState
        if (isMultiSDCard) {
            GlobalVariable.reMainCardSum =
                GlobalVariable.SdCardSum - GlobalVariable.SDCardUsedSizeIr
            GlobalVariable.reMainSD2Sum =
                GlobalVariable.Sd2CardSum - GlobalVariable.SDCardUsedSizeVisible
            sdCardState.reMainCardSum = GlobalVariable.reMainCardSum
            sdCardState.reMainCard2Sum = GlobalVariable.Sd2CardSum
        } else {
            GlobalVariable.reMainCardSum =
                GlobalVariable.SdCardSum - GlobalVariable.SDCardUsedSizeVisible
            sdCardState.reMainCardSum = GlobalVariable.reMainCardSum
        }

        notify(CameraParaValue(
                DroneUtils.isAutoMode,
                DroneUtils.lightISOValue,
                DroneUtils.lightESValue,
                DroneUtils.lightEvValue,
                sdCardState,
                DroneUtils.lightAELockValue
            )
        )
    }
}