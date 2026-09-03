package com.gdu.demo.widgetlist.takephoto

import androidx.lifecycle.viewModelScope
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.component.interfaces.IHms
import com.gdu.msdk.device.component.pod.utils.SDCardStatus
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.util.KVObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * @author wuqb
 * @date 2026/8/28 13:58
 * @description 这里写描述
 */
class TakePhotoViewModel : WidgetModel() {

    /**
     * 拍照录像按钮状态
     */
    private val _shootViewState = MutableStateFlow(ShootStatus.INIT)
    /**
     * 拍照录像按钮状态(对外)
     */
    val shootViewState: StateFlow<ShootStatus>
        get() = _shootViewState.asStateFlow()

    private val photoModeFlow = callbackFlow {
        val photoModeObserver = KVObserver<Boolean> { value ->
            trySend(value)
        }
        IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.register(photoModeObserver)
        trySend(IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value)
        awaitClose {
            IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.unregister(photoModeObserver)
        }
    }

    /**
     * 卡状态
     */
    private val sdcardStateFlow = callbackFlow {
        val sdcardStateObserver = KVObserver<SDCardStatus> { value ->
            XLogger.APP.i("sdcardState trySend:value:$value")
            trySend(value)
        }
        IHms.get.sdCardStatus.register(sdcardStateObserver)
        trySend(IHms.get.sdCardStatus.value)
        awaitClose {
            IHms.get.sdCardStatus.unregister(sdcardStateObserver)
        }
    }

    private val isPhotoingFlow = callbackFlow<Boolean> {
        val isPhotoingObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.camera.isPhotoing.register(isPhotoingObserver)
        trySend(IGduDroneDevice.get.camera.isPhotoing.value)
        awaitClose {
            IGduDroneDevice.get.camera.isPhotoing.unregister(
                isPhotoingObserver
            )
        }
    }

    private val isRecordingFlow = callbackFlow {
        val recordingObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.camera.isRecording.register(recordingObserver)
        trySend(IGduDroneDevice.get.camera.isRecording.value)
        awaitClose {
            IGduDroneDevice.get.camera.isRecording.unregister(recordingObserver)
        }
    }

    private val cameraWorkingFlow =
        combine(isPhotoingFlow, isRecordingFlow) { isPhotoing, isRecording ->
            isPhotoing || isRecording
        }

    private val photoTypeFlow = callbackFlow {
        val photoTypeObserver = KVObserver<Int> { value -> trySend(value) }
        IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.register(photoTypeObserver)
        trySend(IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value)
        awaitClose {
            IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.unregister(
                photoTypeObserver
            )
        }
    }

    private val photoOrVideoEnableFlow = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            combine(
                photoModeFlow,
                sdcardStateFlow,
                cameraWorkingFlow,
                photoTypeFlow.distinctUntilChanged(),
                photoOrVideoEnableFlow
            ) { photoMode, sdcardState, isWorking, photoType, btnEnable ->
                XLogger.APP.i("combine:photoModeFlow:${photoMode};sdcardStateFlow:${sdcardState};isWorking:${isWorking};photoType:${photoType};photoOrVideoEnableFlow:${btnEnable}")
                if (photoMode) {//拍照模式
                    if (isWorking) {//工作状态
                        if (photoType == 0 || photoType == 1) {//单拍
                            ShootStatus.ISSINGLEPHOTOING_UNABLE
                        } else {
                            if (btnEnable) {
                                ShootStatus.ISPHOTOING_ENABLE
                            } else {
                                ShootStatus.ISPHOTOING_UNABLE
                            }
                        }
                    } else {//静止态
                        if (sdcardState == SDCardStatus.CARD_FULL) {
                            ShootStatus.SD_FULL
                        } else if (sdcardState == SDCardStatus.CARD_ERROR || sdcardState == SDCardStatus.FORMAT_ERROR
                            || sdcardState == SDCardStatus.EXTERNAL_CARD_FORMAT_ERROR || sdcardState == SDCardStatus.LOW_SPEED_CARD) {
                            ShootStatus.SD_ERROR
                        } else if (sdcardState == SDCardStatus.NO_CARD) {
                            ShootStatus.NO_SDCARD
                        } else if (!btnEnable) {
                            ShootStatus.PICTURE_UNABLE
                        } else {
                            ShootStatus.PICTURE_ENABLE
                        }
                    }
                } else {//录像模式
                    if (isWorking) {//工作状态
                        if (btnEnable) {
                            ShootStatus.ISRECORDING_ENABLE
                        } else {
                            ShootStatus.ISRECORDING_UNABLE
                        }
                    } else {//静止态
                        if (sdcardState == SDCardStatus.CARD_FULL) {
                            ShootStatus.SD_FULL
                        } else if (sdcardState == SDCardStatus.CARD_ERROR || sdcardState == SDCardStatus.FORMAT_ERROR
                            || sdcardState == SDCardStatus.EXTERNAL_CARD_FORMAT_ERROR || sdcardState == SDCardStatus.LOW_SPEED_CARD) {
                            ShootStatus.SD_ERROR
                        } else if (sdcardState == SDCardStatus.NO_CARD) {
                            ShootStatus.NO_SDCARD
                        } else if (!btnEnable) {
                            ShootStatus.VIDEO_UNENABLE
                        } else {
                            ShootStatus.VIDEO_ENABLE
                        }
                    }
                }
            }.collect { result ->
                _shootViewState.value = result
            }
        }
    }

    override fun onStart() {

    }


    fun takePhoto(){

    }
}