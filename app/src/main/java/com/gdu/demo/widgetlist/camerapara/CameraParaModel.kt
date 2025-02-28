package com.gdu.demo.widgetlist.camerapara

import com.gdu.config.GlobalVariable
import com.gdu.demo.utils.CameraUtil
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.util.GimbalUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CameraParaModel: WidgetModel() {


    override fun onStart() {

        launch {
            intervalFlow(1000).collectLatest {
                updateState()
            }
        }
    }

    fun updateState() {
        val sdCardState = SDCardState()
        val isMultiSDCard = CameraUtil.isSupportMultiSDCardGimbal(GlobalVariable.gimbalType)
        val vlStatus = GimbalUtil.checkVLSDCardStatus()
        val irState = GimbalUtil.checkIRSDCardStatus();
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

        notify(
            dataChangeChannel, CameraParaValue(
                GlobalVariable.isAutoMode,
                GlobalVariable.lightISOValue,
                GlobalVariable.lightESValue,
                GlobalVariable.lightEvValue,
                sdCardState,
                GlobalVariable.lightAELockValue
            )
        )
    }

    override fun onDestroy() {
    }
}