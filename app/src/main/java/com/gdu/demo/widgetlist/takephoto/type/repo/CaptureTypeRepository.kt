package com.gdu.demo.widgetlist.takephoto.type.repo

import android.util.Log
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.key.value.base.KeyResult
import com.gdu.msdk.key.value.bean.GimbalType
import com.gdu.msdk.key.value.bean.LightPhotoMode
import com.gdu.msdk.key.value.common.EmptyMsg
import com.gdu.msdk.util.KVObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface CaptureTypeRepository {
    val photoModeFlow: Flow<Boolean>

    val gimbalTypeFlow: Flow<GimbalType>

    val photoTypeFlow: Flow<Int>

    val isPhotoingFlow: Flow<Boolean>

    val isRecordingFlow: Flow<Boolean>

    val aiOpenFlow: Flow<Boolean>

    /**
     * 切换拍照模式
     * */
    suspend fun switchPhotoMode(mode: LightPhotoMode, param: Byte): Flow<KeyResult<EmptyMsg>>

    /**
     * 切换拍照录像模式
     * */
    suspend fun switchCameraMode(cameraMode: Boolean): Flow<KeyResult<EmptyMsg>>
}

class CaptureTypeRepositoryImpl : CaptureTypeRepository {
    override val photoModeFlow = callbackFlow {
        val photoModeObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.register(photoModeObserver)
        trySend(IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value)
        awaitClose {
            IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.unregister(photoModeObserver)
        }
    }

    override val gimbalTypeFlow = callbackFlow {
        val gimbalTypeObserver = KVObserver<GimbalType> { value -> trySend(value) }
        IGduDroneDevice.get.gimbal.gimbalTypeFlow.register(gimbalTypeObserver)
        trySend(IGduDroneDevice.get.gimbal.gimbalTypeFlow.value)
        awaitClose {
            IGduDroneDevice.get.gimbal.gimbalTypeFlow.unregister(gimbalTypeObserver)
        }
    }

    override val photoTypeFlow = callbackFlow {
        val photoTypeObserver = KVObserver<Int> { value -> trySend(value) }
        IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.register(photoTypeObserver)
        trySend(IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value)
        awaitClose {
            IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.unregister(
                photoTypeObserver
            )
        }
    }

    override val isPhotoingFlow = callbackFlow {
        val isPhotoingObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.camera.isPhotoing.register(isPhotoingObserver)
        trySend(IGduDroneDevice.get.camera.isPhotoing.value)
        awaitClose {
            IGduDroneDevice.get.camera.isPhotoing.unregister(
                isPhotoingObserver
            )
        }
    }

    override val isRecordingFlow = callbackFlow {
        val isRecordingObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.camera.isRecording.register(isRecordingObserver)
        trySend(IGduDroneDevice.get.camera.isRecording.value)
        awaitClose {
            IGduDroneDevice.get.camera.isRecording.unregister(
                isRecordingObserver
            )
        }
    }

    override val aiOpenFlow = callbackFlow {
        val aiOpenObserver = KVObserver<Boolean> { value -> trySend(value) }
        IGduDroneDevice.get.aiModel.isAiOpen.register(aiOpenObserver)
        trySend(IGduDroneDevice.get.aiModel.isAiOpen.value)
        awaitClose {
            IGduDroneDevice.get.aiModel.isAiOpen.unregister(
                aiOpenObserver
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun switchPhotoMode(mode: LightPhotoMode, param: Byte) =
        callbackFlow {
            XLogger.APP.i("switchPhotoMode() start;mode:${mode};param:${param}")
            IGduDroneDevice.get.camera.switchPhotoMode(mode, param) { result ->
                XLogger.APP.i("switchPhotoMode() callback:result:${result.success};code:${result.result?.code}")
                trySend(result)
                close()
            }
            awaitClose { Log.i("wsd", "switchPhotoMode() close") }
        }

    override suspend fun switchCameraMode(cameraMode: Boolean) =
        callbackFlow {
            XLogger.APP.i("switchCameraMode() start;mode:${cameraMode}")
            IGduDroneDevice.get.camera.switchCameraMode(cameraMode) { result ->
                XLogger.APP.i("switchCameraMode() callback:result:${result.success};code:${result.result?.code}")
                trySend(result)
                close()
            }
            awaitClose { Log.i("wsd", "switchCameraMode() close") }
        }
}