package com.gdu.demo.widgetlist.takephoto.cameracapture.vm

import androidx.lifecycle.viewModelScope
import com.gdu.demo.R
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.demo.widgetlist.takephoto.ShootStatus
import com.gdu.demo.widgetlist.takephoto.cameracapture.repo.CameraCaptureRepository
import com.gdu.demo.widgetlist.takephoto.cameracapture.repo.CameraCaptureRepositoryImpl
import com.gdu.demo.widgetlist.takephoto.type.CaptureType
import com.gdu.lib.util.TimeUtil
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.component.interfaces.IHms
import com.gdu.msdk.device.component.pod.enums.PhotoStatusResult
import com.gdu.msdk.device.component.pod.enums.VideoStatusResult
import com.gdu.msdk.device.component.pod.utils.SDCardStatus
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.key.value.CycleRecordVideo
import com.gdu.msdk.key.value.CycleTakePhoto
import com.gdu.msdk.util.KVObserver
import com.gdu.sdk.sound.engine.GduSoundManager
import com.gdu.sdk.sound.engine.SoundConst
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * @author wuqb
 * @date 2026/8/31 10:56
 * @description 这里写描述
 */
class CameraCaptureWidgetModel: WidgetModel() {

    val cameraCaptureRepository: CameraCaptureRepository = CameraCaptureRepositoryImpl()

    private var lastReceiveRecordCBTime: Long = 0
    /**
     * 拍照录像按钮状态
     */
    private val _shootViewState = MutableStateFlow(ShootStatus.INIT)

    /**
     * 录像中拍照按钮状态(对外)
     */
    val photoInVideoState: StateFlow<PhotoInVideoStatus>
        get() = _photoInVideoState.asStateFlow()
    /**
     * 录像中拍照按钮状态
     */
    private val _photoInVideoState: MutableStateFlow<PhotoInVideoStatus> =
        MutableStateFlow<PhotoInVideoStatus>(PhotoInVideoStatus.Hide)

    /**
     * 状态变更(对外)
     */
    val uiEventState: SharedFlow<UiEvent>
        get() = _uiEventState.asSharedFlow()
    private var _uiEventState: MutableSharedFlow<UiEvent> =
        MutableSharedFlow<UiEvent>()

    /**
     * 拍照录像按钮底部提示文案
     */
    private val _photoOrVideoTipState =
        MutableStateFlow<PhotoOrVideoTipStatus>(PhotoOrVideoTipStatus.None())

    /**
     * 拍照录像按钮底部提示文案(对外)
     */
    val photoOrVideoTipState: StateFlow<PhotoOrVideoTipStatus>
        get() = _photoOrVideoTipState.asStateFlow()

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

    private val photoCallbackFlow = callbackFlow<CycleTakePhoto?> {
        val takePhotoObserver = KVObserver<CycleTakePhoto?> { value -> value?.let { trySend(it) } }
        IGduDroneDevice.get.camera.flowTakePhotoAck.register(takePhotoObserver)
        awaitClose {
            IGduDroneDevice.get.camera.flowTakePhotoAck.unregister(takePhotoObserver)
        }
    }

    private val videoCallbackFlow = callbackFlow<CycleRecordVideo?> {
        val recordVideoObserver = KVObserver<CycleRecordVideo?> { value -> value?.let { trySend(it) } }
        IGduDroneDevice.get.camera.flowRecordVideoAck.register(recordVideoObserver)
        awaitClose {
            IGduDroneDevice.get.camera.flowRecordVideoAck.unregister(recordVideoObserver)
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

    private val photoParamFlow = callbackFlow<Int> {
        val photoParamObserver = KVObserver<Int> { value -> trySend(value) }
        IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoParam.register(photoParamObserver)
        awaitClose {
            IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoParam.unregister(
                photoParamObserver
            )
        }
    }

    private val photoOrVideoEnableFlow = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            photoCallbackFlow.shareIn(
                viewModelScope,
                started = SharingStarted.Eagerly,
                0
            ).collect { result ->
                result?.let { curTakePhotoFeedbackHandle(it) }
            }
        }
        viewModelScope.launch {
            videoCallbackFlow.shareIn(
                viewModelScope,
                started = SharingStarted.Eagerly,
                0
            ).collect { result ->
                result?.let { onRecordResult(it) }
            }
        }
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

        //拍照模式、录像提示文案和模式图标显示逻辑
        viewModelScope.launch {
            combine(
                cameraWorkingFlow,
                photoModeFlow,
                photoTypeFlow.distinctUntilChanged(),
                photoParamFlow.distinctUntilChanged(),
                sdcardStateFlow
            ) { isWorking, photoMode, photoType, photoParam, sdcardState ->
                XLogger.APP.i("photoOrVideoTipStatus:isWorking:${isWorking};photoMode:${photoMode};photoType:${photoType};photoParam:${photoParam};sdcard:${sdcardState}")
                if (!isWorking) {//此处只处理非工作态度
                    if (sdcardState != SDCardStatus.NO_CARD) {//异常卡，隐藏提示文案和模式图标显示
                        PhotoOrVideoTipStatus.Hide()
                    } else {
                        if (photoMode) {//拍照模式
                            when (photoType) {
                                1 -> {//智能低光
                                    PhotoOrVideoTipStatus.LowLightPhotoInitTip()
                                }
                                2 -> {//全景
                                    PhotoOrVideoTipStatus.PanoPhotoInitTip()
                                }

                                3 -> {//定时拍
                                    PhotoOrVideoTipStatus.TimingPhotoInitTip(photoParam)
                                }

                                4 -> {//连拍
                                    PhotoOrVideoTipStatus.ContinuingPhotoInitTip(photoParam)
                                }
                                else -> {//单拍
                                    PhotoOrVideoTipStatus.SinglePhotoInitTip()
                                }
                            }
                        } else {//录像模式
                            PhotoOrVideoTipStatus.VideoInitTip(TimeUtil.getTimeStrCount(0))
                        }
                    }
                } else {
                    PhotoOrVideoTipStatus.None()
                }
            }.collect { status ->
                if (status !is PhotoOrVideoTipStatus.None) {
                    _photoOrVideoTipState.value = status
                }
            }
        }
    }


    /**
     * 视频录制或暂停
     * @param onlyStop 仅停止，不允许开启/停止切换
     * */
    fun onRecord(onlyStop:Boolean = false) {
        if (IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
            XLogger.APP.e("onRecord() isPhotoMode;onlyStop:${onlyStop}")
            return
        }
        XLogger.APP.i("============onRecord================;onlyStop:${onlyStop}")
        if (!IGduDroneDevice.get.camera.isRecording.value && !onlyStop) {
            startRecord()
        } else if (IGduDroneDevice.get.camera.isRecording.value){
            stopRecord()
        }
    }

    /**
     * 开始录制
     * */
    private fun startRecord() {
        if (!isConnect()) {
            XLogger.APP.i("飞行器未连接12")
            showToastLiveData.value = R.string.aircraft_not_connect
            return
        }
        if (IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            XLogger.APP.i("云台未连接")
            showToastLiveData.value = R.string.gimbal_not_connect
            return
        }
//        if (IGduDroneDevice.get.camera.isSdcardFormating.value) {
//            showToastLiveData.value = R.string.gimbal_sdcard_formating
//            return
//        }
//        if (IHms.get.sdCardStatus.value != SDCardStatus.NO_CARD) {
//            showToastLiveData.value = PhotoOrVideoUtils.getSdcardErrStatusIconTips(IHms.get.sdCardStatus.value)
//            return
//        }
        if (!IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            viewModelScope.launch {
                cameraCaptureRepository.recordVideo(true).catch {
                    showToastLiveData.value = R.string.operation_failed
                }.collect { result ->
                    if (result.success) {
                        XLogger.APP.i("recordVideo success")
                        photoOrVideoEnableFlow.value = true
                    } else {
                        XLogger.APP.i("recordVideo failed")
                        showToastLiveData.value = R.string.operation_failed
                        photoOrVideoEnableFlow.value = true
                    }
                }
            }
        }
    }

    /**
     * 停止录制
     * */
    private fun stopRecord() {
        if (!IGduDroneDevice.get.isConnected) {
            XLogger.APP.i("飞行器未连接13")
            showToastLiveData.value = R.string.aircraft_not_connect
            return
        }
        if (IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            XLogger.APP.i("云台未连接13")
            showToastLiveData.value = R.string.gimbal_not_connect
            return
        }

        viewModelScope.launch {
            cameraCaptureRepository.recordVideo(false).catch {
                showToastLiveData.value = R.string.operation_failed
            }.collect { result ->
                if (result.success) {
                    XLogger.APP.i("recordVideo success")
                } else {
                    XLogger.APP.i("recordVideo failed")
                    showToastLiveData.value = R.string.operation_failed
                    photoOrVideoEnableFlow.value = true
                }
            }
        }
    }


    /**
     * 开始拍照
     * */
    fun startTakePicture() {
        XLogger.APP.i("============startTakePicture================")
        if (!isConnect()) {
            XLogger.APP.i("飞行器未连接11")
            showToastLiveData.value = R.string.aircraft_not_connect
            return
        }
//        if (IGduDroneDevice.get.camera.isSdcardFormating.value) {
//            showToastLiveData.value = R.string.gimbal_sdcard_formating
//            return
//        }
        if (IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            showToastLiveData.value = R.string.gimbal_not_connect
            return
        }
//        if (IHms.get.sdCardStatus.value != SDCardStatus.NO_CARD)  {
//            showToastLiveData.value =
//                PhotoOrVideoUtils.getSdcardErrStatusIconTips(IHms.get.sdCardStatus.value)
//            return
//        }
//        if (RouteBox.isRecordingRoute() && (ICamera.get.currentCameraStatus.notifyPhotoType.value != 0)) {
//            showToastLiveData.value = R.string.string_stop_route_recording_tip
//            return
//        }
        if (!IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
            XLogger.APP.e("startTakePicture() isPhotoMode=false,return")
            return
        }
        takePhoto() //拍照
    }


    /**
     * 拍照
     * 变倍连拍，连拍，正常拍
     * 发送拍照指令
     */
    private fun takePhoto() {
        if (IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value == 2) {//全景
            takePanoramaPhoto()
        } else {
            takeCommonPhoto()
        }
    }

    /**
     * 全景拍流程
     */
    private fun takePanoramaPhoto() {
        val isStart = !IGduDroneDevice.get.camera.isPhotoing.value
//        if (isStart) {//如果是开启全景拍，需要校验飞机是否在地面上
//            if (IGduDroneDevice.get.flightController.notifyFlightState.value == DroneFlyState.GROUND) {
//                showToastLiveData.value = R.string.bizvideo_panorama_photo_cant_start_no_ground
//                return
//            }
//            if (IGduDroneDevice.get.flightController.notifyFlightState.value == DroneFlyState.LAND) {
//                showToastLiveData.value = R.string.bizvideo_need_stop_landing_when_pano
//                return
//            }
//            if (IGduDroneDevice.get.flightController.notifyFlightState.value == DroneFlyState.BACK) {
//                showToastLiveData.value = R.string.bizvideo_need_stop_return_when_pano
//                return
//            }
//        }
        viewModelScope.launch {
            var result = true
            if (IGduDroneDevice.get.camera.lightType.value != 5.toByte() && !IGduDroneDevice.get.camera.supportFun.multiStreamCapability.enableMultiStream()) {
                result = cameraCaptureRepository.switchImageMode(5)
            }
            if (!result) {
                showToastLiveData.value = R.string.ui_setting_failed
                return@launch
            }
            cameraCaptureRepository.takePanoramaPhoto(isStart).catch {
                showToastLiveData.value = R.string.ui_setting_failed
            }.collect { result ->
                if (isStart) {
                    if (result.success && result.result?.state == 0.toByte()) {
                        photoOrVideoEnableFlow.value = true
                    } else {
                        showToastLiveData.value = R.string.take_photo_fail
                        photoOrVideoEnableFlow.value = true
                    }
                } else {
                    if (result.success && result.result?.state == (-5).toByte()) {
                        photoOrVideoEnableFlow.value = true
                    } else {
                        showToastLiveData.value = R.string.take_photo_fail
                        photoOrVideoEnableFlow.value = true
                    }
                }
            }
        }
    }

    private fun takeCommonPhoto(){
        val isStart =
            if (IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value == 3 && IGduDroneDevice.get.camera.isPhotoing.value) {//定时拍
                false
            } else if (IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value == 4 && IGduDroneDevice.get.camera.isPhotoing.value) {//连拍
                false
            } else {
                true
            }
        if (!IGduDroneDevice.get.gimbal.gimbalType.isNoGimbal()) {
            if (IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value == 0 || IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value == 1) {
                photoOrVideoEnableFlow.value = false
            }
            viewModelScope.launch {
                XLogger.APP.i(
                    "takeCommonPhoto() photoType:${IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value};isPhoto:${IGduDroneDevice.get.camera.isPhotoing.value};isStart:${isStart}"
                )
                cameraCaptureRepository.takePhoto(isStart).catch {
                    showToastLiveData.value = R.string.ui_setting_failed
                }.collect { result ->
                    if (result.success) {
                        photoOrVideoEnableFlow.value = true
                    } else {
                        showToastLiveData.value = R.string.take_photo_fail
                        photoOrVideoEnableFlow.value = true
                    }
                }
            }
        }
    }


    override fun onStart() {

    }


    fun takeCapture(){
        if (!IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
            onRecord()
        } else {
            startTakePicture()
        }
    }

    /**
     * 当前拍照反馈状态处理
     * @param resultCode 当前拍照反馈状态
     *
     *
     * 0-拍照完成；
     * 1-正在连拍；
     * 2-正在连续拍；
     * 3-正在延时拍；
     * 4-停止拍照；
     * 5-正在单拍；
     * 6-拍照失败：
     * 7-未插入SD卡；
     * 8-SD卡已满；
     * 9-异常卡；
     * 10-低速卡；
     * 11-SD卡格式错误
     * 12-正在定时拍
     * 13-APP 执行全景拍照(相机全景拍照模式收到拍照指令)
     *
     */
    private suspend fun curTakePhotoFeedbackHandle(result: CycleTakePhoto) {
        XLogger.APP.i("curTakePhotoFeedbackHandle() status:${result.takePhotoStatus};photoType:${IGduDroneDevice.get.camera.currentCameraStatus.notifyPhotoType.value};photoNum:${result.photoNum};allPhotoNum:${result.allPhotoNum};photoTime:${result.photoTime};isPhotoMode:${IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value}")
        when (result.takePhotoStatus) {
            PhotoStatusResult.SUCCESS_CODE.code -> {
                takePhotoSucceed()
            }

            PhotoStatusResult.STOP_PHOTO.code -> {
                photoOrVideoEnableFlow.value = true
            }

            PhotoStatusResult.IS_SINGLE_PHOTOING.code -> {
                if (IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
                    photoOrVideoEnableFlow.value = false
                    _photoOrVideoTipState.value = PhotoOrVideoTipStatus.SinglePhotoing("")
                } else {
                    _photoInVideoState.value = PhotoInVideoStatus.PhotoUnable
                }
            }

            PhotoStatusResult.IS_DELAY_PHOTOING.code -> {//智能低光走这里
                if (IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
                    photoOrVideoEnableFlow.value = false
                    _photoOrVideoTipState.value = PhotoOrVideoTipStatus.LowLightPhotoing("")
                } else {
                    //因视熙下发状态有问题，视熙会根据拍照模式下发对应模式的录像中拍照进行的状态，所以这里需要坐下处理，自研无此问题，会统一下发单拍拍照中状态
                    _photoInVideoState.value = PhotoInVideoStatus.PhotoUnable
                }
            }

            PhotoStatusResult.IS_COUNTING_PHOTO.code, PhotoStatusResult.IS_COUNTING2_PHOTOING.code -> {
                if (IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
                    photoOrVideoEnableFlow.value = false
                } else {
                    //因视熙下发状态有问题，视熙会根据拍照模式下发对应模式的录像中拍照进行的状态，所以这里需要坐下处理，自研无此问题，会统一下发单拍拍照中状态
                    _photoInVideoState.value = PhotoInVideoStatus.PhotoUnable
                }
            }

            PhotoStatusResult.NO_SDCARD.code -> {
                showToastLiveData.value = R.string.take_photo_fail
                photoOrVideoEnableFlow.value = true
            }

            PhotoStatusResult.SD_FULL.code -> {
                showToastLiveData.value = R.string.take_photo_fail
                photoOrVideoEnableFlow.value = true
            }

            PhotoStatusResult.IS_TIME_PHOTOING.code -> {
                if (IGduDroneDevice.get.camera.currentCameraStatus.isPhotoMode.value) {
                    photoOrVideoEnableFlow.value = true
                } else {
                    //因视熙下发状态有问题，视熙会根据拍照模式下发对应模式的录像中拍照进行的状态，所以这里需要坐下处理，自研无此问题，会统一下发单拍拍照中状态
                    _photoInVideoState.value = PhotoInVideoStatus.PhotoUnable
                }
            }

            PhotoStatusResult.CAMERA_NOTIFY_PANO_PHOTO.code -> {
                _uiEventState.emit(UiEvent.CameraNotifyPanoPhoto())
            }

            else -> {
                showToastLiveData.value = R.string.take_photo_fail
                photoOrVideoEnableFlow.value = true
            }
        }
    }

    private fun takePhotoSucceed() {
        photoOrVideoEnableFlow.value = true
        GduSoundManager.playSound(SoundConst.PHOTOSINGLE, 0, 0.8f, 0.8f, true)
        showToastLiveData.value = R.string.take_photo_success
    }

    /**
     * 录像状态回调处理
     *  videoStatus 当前录像反馈
     *      0-停止录像；
     *      1-正在录像；
     *      2-延时录影等待中；
     *      3-缩时录影中
     *      4-未插入SD卡；
     *      5-SD卡已满；
     *      6-异常卡；
     *      7-低速卡；
     *      8-SD卡格式错误
     * */
    private var isRecording = false
    private fun onRecordResult(result: CycleRecordVideo) {
        lastReceiveRecordCBTime = System.currentTimeMillis()
        XLogger.APP.i("onRecordResult() videoStatus:${result.videoStatus};time:${result.recordTime}")
        when (result.videoStatus) {
            VideoStatusResult.STOP.code -> {  //停止录像
                if (isRecording) {
                    isRecording = false
                    stopRecordTip()
                }
            }

            VideoStatusResult.IS_RECORDING.code, VideoStatusResult.IS_RECORDING2.code -> {  //正在录像
                if (!isRecording) {
                    isRecording = true
                    if (result.recordTime == 0) {
                        showToastLiveData.value = R.string.start_record
                        playVideoSound()
                    }
                }
                photoOrVideoEnableFlow.value = true
                _photoOrVideoTipState.value = PhotoOrVideoTipStatus.Recording(TimeUtil.getTimeStrCount(result.recordTime))
            }

            VideoStatusResult.SDCARD_FULL.code -> {  //卡已满
                showToastLiveData.value = R.string.record_fail
                isRecording = false
            }

            VideoStatusResult.NO_SDCARD.code -> { //低速卡
                showToastLiveData.value = R.string.record_fail
                isRecording = false
            }

            VideoStatusResult.SDCARD_FORMAT_ERROR.code -> { //SD卡格式错误
                showToastLiveData.value = R.string.record_fail
                isRecording = false
            }

            VideoStatusResult.RECORD_LESS_FOUR_SEC.code -> { //非常规状态用途未知
                showToastLiveData.value = R.string.photo_or_record_can_not_stop_recording
                isRecording = false
            }
            else->{
                isRecording = false
            }
        }
    }

    /**
     * 结束录像
     * */
    private fun onStopRecord() {
        lastReceiveRecordCBTime = 0
        photoOrVideoEnableFlow.value = true
    }

    private fun stopRecordTip() {
        playVideoSound()
        showToastLiveData.value = R.string.record_end
    }

    private fun playVideoSound() {
        GduSoundManager.playSound(SoundConst.RECORDSOUND, 0, true)
    }

    sealed class PhotoInVideoStatus {
        object Hide : PhotoInVideoStatus()
        object PhotoEnable : PhotoInVideoStatus()
        object PhotoUnable : PhotoInVideoStatus()  //单拍拍照中-不可点击
    }

    sealed class PhotoOrVideoTipStatus {
        class VideoInitTip(val tip: String) : PhotoOrVideoTipStatus()
        class SinglePhotoInitTip() : PhotoOrVideoTipStatus()
        data class ContinuingPhotoInitTip(val photoParam: Int) : PhotoOrVideoTipStatus()
        data class TimingPhotoInitTip(val photoParam: Int) : PhotoOrVideoTipStatus()
        class LowLightPhotoInitTip() : PhotoOrVideoTipStatus()
        class PanoPhotoInitTip() : PhotoOrVideoTipStatus()
        data class Recording(val tip: String) : PhotoOrVideoTipStatus()
        data class SinglePhotoing(val tip: String) : PhotoOrVideoTipStatus()
        data class ContinuingPhotoing(val tip: String) : PhotoOrVideoTipStatus()
        data class TimingPhotoing(val tip: String) : PhotoOrVideoTipStatus()
        data class LowLightPhotoing(val tip: String) : PhotoOrVideoTipStatus()
        data class PanoPhotoing(val tip: String) : PhotoOrVideoTipStatus()
        class Hide() : PhotoOrVideoTipStatus()
        class None() : PhotoOrVideoTipStatus()
    }

    sealed class UiEvent {
        data class Tip(val tip: String) : UiEvent()
        class CameraNotifyPanoPhoto() : UiEvent()
    }
}