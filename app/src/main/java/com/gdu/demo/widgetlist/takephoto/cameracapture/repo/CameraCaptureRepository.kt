package com.gdu.demo.widgetlist.takephoto.cameracapture.repo

import android.util.Log
import com.gdu.lib.util.RCUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.callback.MSdkCallback
import com.gdu.msdk.key.error.MError
import com.gdu.msdk.key.value.AckPanoramaPhoto
import com.gdu.msdk.key.value.CycExactBackState
import com.gdu.msdk.key.value.base.KeyResult
import com.gdu.msdk.key.value.bean.DroneFlyState
import com.gdu.msdk.key.value.bean.GimbalType
import com.gdu.msdk.key.value.common.EmptyMsg
import com.gdu.msdk.key.value.rc.RCChannelShieldType
import com.gdu.msdk.util.KVObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Duration.Companion.milliseconds

interface CameraCaptureRepository {

    val exactBackStateFlow: Flow<Int>
    val flightStateKeyFlow:Flow<Int>
    val gimbalTypeFlow:Flow<GimbalType>

    /**
     * 拍照指令
     * */
    suspend fun takePhoto(startPhoto: Boolean): Flow<KeyResult<EmptyMsg>>

    /**
     * 全景拍照指令
     * */
    suspend fun takePanoramaPhoto(startPhoto: Boolean): Flow<KeyResult<AckPanoramaPhoto>>

    /**
     * 录像指令
     * */
    suspend fun recordVideo(startRecord: Boolean): Flow<KeyResult<EmptyMsg>>

    /**
     * 切换光类型
     * */
    suspend fun switchImageMode(mode: Byte): Boolean

    /**
     * 屏蔽遥控器拍照录像指令
     * */
    fun shieldRCControl()

    /**
     * 恢复遥控器拍照录像指令
     * */
    fun resumeRCControl()
}

class CameraCaptureRepositoryImpl : CameraCaptureRepository {

    companion object{
        var shieldSignalState = false
    }

    override val exactBackStateFlow = callbackFlow<Int> {
        val exactBackStateObserver = KVObserver<CycExactBackState?> { value ->
            trySend(value?.exactBackState ?: -1)
        }
        IGduDroneDevice.get.flightController.exactBackState.register(exactBackStateObserver)
        trySend(IGduDroneDevice.get.flightController.exactBackState.value?.exactBackState ?: -1)
        awaitClose {
            IGduDroneDevice.get.flightController.exactBackState.unregister(exactBackStateObserver)
        }
    }.distinctUntilChanged()

    override val flightStateKeyFlow = callbackFlow<Int> {
        val flightStateObserver = KVObserver<DroneFlyState?> { value -> trySend(value?.key ?: -1) }
        IGduDroneDevice.get.flightController.notifyFlightState.register(flightStateObserver)
        trySend(IGduDroneDevice.get.flightController.notifyFlightState.value?.key ?: -1)
        awaitClose {
            IGduDroneDevice.get.flightController.notifyFlightState.unregister(flightStateObserver)
        }
    }.distinctUntilChanged()

    override val gimbalTypeFlow = callbackFlow<GimbalType> {
        val gimbalTypeObserver = KVObserver<GimbalType> { value -> trySend(value) }
        IGduDroneDevice.get.gimbal.gimbalTypeFlow.register(gimbalTypeObserver)
        trySend(IGduDroneDevice.get.gimbal.gimbalTypeFlow.value)
        awaitClose {
            IGduDroneDevice.get.gimbal.gimbalTypeFlow.unregister(gimbalTypeObserver)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun takePhoto(startPhoto: Boolean) =
        callbackFlow<KeyResult<EmptyMsg>> {
            XLogger.APP.i("takePhoto cmd start:${startPhoto}")
            IGduDroneDevice.get.camera.takePhoto(startPhoto
            ) { result ->
                XLogger.APP.i("takePhoto callback():${result.success};start:${startPhoto}")
                trySend(result)
                close()
            }
            awaitClose { Log.i("wsd", "takePhoto() close") }
        }

    override suspend fun recordVideo(startRecord: Boolean) =
        callbackFlow<KeyResult<EmptyMsg>> {
            XLogger.APP.i("recordVideo cmd start:${startRecord}")
            IGduDroneDevice.get.camera.recordVideo(startRecord) { result ->
                XLogger.APP.i("recordVideo callback():${result.success};start:${startRecord}")
//                if (startRecord) {
//                    if (!result.success) {
//                        IGduDroneDevice.get.camera.isRecording.value = result.success
//                    }
//                }
                trySend(result)
                close()
            }
            awaitClose { Log.i("wsd", "recordVideo() close") }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun switchImageMode(mode: Byte): Boolean {
        return suspendCancellableCoroutine {cont->
            XLogger.APP.i("switchImageMode() start:mode:${mode}")
            IGduDroneDevice.get.camera.switchImageMode(mode, object :
                MSdkCallback.ActionCallback<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    XLogger.APP.i("switchImageMode() shootView callback:result:${result?.success}")
                    if (cont.isActive) {
                        cont.resume(result?.success == true) {}
                    }
                }

                override fun onFailure(error: MError) {
                    XLogger.APP.i("switchImageMode() shootView callback failed:${error.code}")
                    cont.resume(false) {}
                }
            })
        }
    }

    override fun shieldRCControl() {
        if (!RCUtils.isRCSEE()) return
        XLogger.APP.i("屏蔽遥控器 开始发送屏蔽指令")
        shieldSignalState = false
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                var counter = 0
                XLogger.APP.i("屏蔽遥控器 shieldSignalState：$shieldSignalState")
                while (!shieldSignalState) {
                    emit(counter)
                    counter++
                    delay(1000.milliseconds)
                }
                emit(counter)
            }.collect { _ ->
                val shield = !shieldSignalState
                XLogger.APP.i("屏蔽遥控器 shield：$shield")
                if (!shield){
                    IGduRCDevice.get.rcChannelShieldSetting(RCChannelShieldType.Channel_Shield_Normal, false) { }
                }else{
                    IGduRCDevice.get.rcChannelShieldSetting(RCChannelShieldType.Channel_Shield_Route_Take_Photo_Video, false) { }
                }
            }
        }
    }

    override fun resumeRCControl() {
        if (!RCUtils.isRCSEE()) return
        XLogger.APP.i("屏蔽遥控器 resumeRCControl")
        shieldSignalState = true
    }

    override suspend fun takePanoramaPhoto(startPhoto: Boolean) =
        callbackFlow<KeyResult<AckPanoramaPhoto>> {
            val panoCmd: Byte = if (startPhoto) {
                1
            } else {
                0
            }
            val isMontage: Byte = if (startPhoto) {
                1
            } else {
                0
            }
            XLogger.APP.i("takePanoramaPhoto cmd start:${startPhoto}")
            IGduDroneDevice.get.camera.takePanoramaPhoto(0, isMontage,panoCmd
            ) { result ->
                XLogger.APP.i("takePanoramaPhoto callback():${result.success};start:${startPhoto};state:${result.result?.state}")
                trySend(result)
                close()
            }
            awaitClose { Log.i("wsd", "takePanoramaPhoto() close") }
        }
}