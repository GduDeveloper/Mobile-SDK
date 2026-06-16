package com.gdu.demo.flight.msgbox

import androidx.fragment.app.FragmentActivity
import com.gdu.demo.R
import com.gdu.lib.util.ActivityManager
import com.gdu.lib.util.ThreadHelper
import com.gdu.lib.util.core.ResourceUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.component.hms.bean.WarnBean
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.component.pod.utils.SDCardStatus
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.util.GduKVEachObservable
import com.gdu.msdk.util.GduKVObservable
import com.gdu.msdk.util.IGduKVObservable
import com.gdu.sdk.base.Diagnostics
import com.gdu.sdk.hms.DeviceHealthInformation
import com.gdu.sdk.hms.WarningLevel
import com.gdu.sdk.manager.SDKManager
import com.gdu.sdk.sound.engine.GduSoundManager
import com.gdu.sdk.sound.engine.SoundConst
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author wuqb
 * @date 2025/1/7
 * @description 消息盒子管理类
 */
class MsgBoxManager: Diagnostics.DiagnosticsInformationCallback{
    private var getAlarmDispose: Disposable? = null

    //用于显示消息盒子的数据
    private val mWarnList = CopyOnWriteArrayList<Diagnostics>()
    //用于缓存最近一次的异常信息
    private var mPreWarnBeans: HashMap<Long, Diagnostics> = HashMap()
    //用于缓存一级异常
    private var mWarnLevel1: ArrayList<Diagnostics> = ArrayList()
    //用于缓存二级异常
    private var mWarnLevel2: ArrayList<Diagnostics> = ArrayList()
    //用于缓存三级异常
    private var mWarnLevel3: ArrayList<Diagnostics> = ArrayList()

    //用于显示Toast数据缓存
    private var mWarnToast: ArrayList<Diagnostics> = ArrayList()
    //用于显示Dialog数据缓存
    private var mWarnDialog: ArrayList<Diagnostics> = ArrayList()

    /** 当前语音提示byte对应值  */
    @Volatile
    private var currentShowIndex: AtomicInteger = AtomicInteger(-1)
    val errMsgList: IGduKVObservable<List<Diagnostics>?> = GduKVEachObservable(null)
    val errMsgSize: IGduKVObservable<Int> = GduKVObservable(0)
    val errRollMsg: IGduKVObservable<Diagnostics?> = GduKVObservable(null)

    init {
        SDKManager.getInstance().getProduct()?.setDiagnosticsInformationCallback(this)
    }

    /**
     * 获取数据
     * */
    private fun initData(value: MutableList<Diagnostics>){
        resetAll()
        if (IGduDroneDevice.get.isConnected || value.isNotEmpty()) {
            if (value.isNotEmpty()){
                handleWarnData(value)
            }
            //处理非错误码上报数据
            if (IGduDroneDevice.get.isConnected) {
                handlerOtherData()
            }
            ThreadHelper.runOnUiThread {
                onMsgDialog()
                onMsgToast()
            }
            println("warnList:"+mWarnList.size)
            if (mWarnList.isEmpty()){
                onNormalStatus()
                errMsgList.value = null
                return
            }
            ThreadHelper.runOnUiThread {
                onMsgBoxList()
                onMsgRolling()
            }
        }else{
            errMsgList.value = null
            onNormalStatus()
        }
    }

    /**
     * 处理数据
     * */
    private fun handleWarnData(valueSet: Collection<Diagnostics>){
        //用于飞行记录中使用
        val warnIds: ArrayList<Long> = ArrayList()
        val mCurrWarnBeans: HashMap<Long, Diagnostics> = HashMap()
        var mNeedRestart = false
        val otherSum = mWarnList.size //非自研云台异常数量

        for (mCodeBean in valueSet) {
            val errCode = mCodeBean.code
            val warnLevel = mCodeBean.healthInformation.warningLevel
            val warnResId = ErrCodeGetStringUtils.getErrCodeStringResId(mCodeBean.healthInformation.componentId, mCodeBean.healthInformation.functionId, errCode)
            if (warnResId == 0){
                println("未适配的错误码，componentId:"+mCodeBean.healthInformation.componentId+", functionId:"+mCodeBean.healthInformation.functionId+", errCode:"+errCode)
                continue
            }
            mCodeBean.reason = ResourceUtils.getString(warnResId)
            println("warnLevel:"+warnLevel+", errCode:"+errCode+",reason:"+mCodeBean.reason)
            //判断是否是消息盒子
            if (mCodeBean.healthInformation.isNeedMsgBox){
                val mNeedFilterContent = onFilterSameContent(mCodeBean)
                if (!mNeedFilterContent) {
                    //用于渲染用
                    mWarnList.add(mCodeBean)
                    //缓存当前记录
                    mCurrWarnBeans[errCode] = mCodeBean
                    //按照等级缓存记录
                    if (warnLevel == WarningLevel.WARNING)
                        mWarnLevel1.add(mCodeBean)
                    if (warnLevel == WarningLevel.CAUTION)
                        mWarnLevel2.add(mCodeBean)
                    if (warnLevel == WarningLevel.NOTICE)
                        mWarnLevel3.add(mCodeBean)

                    if (!mNeedRestart && !mPreWarnBeans.containsKey(errCode)) {
                        mNeedRestart = true
                    }
                }
            }
            if (mCodeBean.healthInformation.isNeedToast){
                mWarnToast.add(mCodeBean)
                println("Toast错误码数量:"+mWarnToast.size)
            }

            if (mCodeBean.healthInformation.isNeedDialog){
                mWarnDialog.add(mCodeBean)
            }

            //用于飞行纪录中使用
            warnIds.add(errCode)
        }
        //判断新旧长度是否一致，如果不一致则认为有异常消息内容变化，则需要刷新，否则不刷新
        val isSameSize = mPreWarnBeans.size == (mWarnList.size-otherSum)
        if (!isSameSize) mNeedRestart = true

        if (mNeedRestart) {
            currentShowIndex = AtomicInteger(0)
            mPreWarnBeans.clear()
            mPreWarnBeans = mCurrWarnBeans
        }
    }

    /**
     * 处理相同文案的错误码，当文案相同时丢弃一个错误码不显示，只保留一个
     */
    private fun onFilterSameContent(data: Diagnostics):Boolean{
        for (bean in mWarnList){
            if (bean.code == data.code || bean.reason == data.reason){
                println("sameCode:${data.code}")
                return true
            }
        }
        return false
    }

    /**
     * 处理非自研相机或不能上报异常码的逻辑
     * */
    private fun handlerOtherData(){
        //处理飞控异常协议数据
        handlerFCData()

        println("gimbalType:"+IGduDroneDevice.get.gimbal.gimbalType)
        println("sdcard status:"+IGduDroneDevice.get.hms.sdCardStatus.value)
        when(IGduDroneDevice.get.hms.sdCardStatus.value){
            SDCardStatus.CARD_FULL->{ //外部存储卡满
                val lightCameraMemoryIsFull = Diagnostics()
                lightCameraMemoryIsFull.code = WarnBean.CAMERA_MEMORY_IS_FULL
                lightCameraMemoryIsFull.healthInformation = getHealthInfo(lightCameraMemoryIsFull.code, WarningLevel.CAUTION)
                lightCameraMemoryIsFull.reason = ResourceUtils.getString(R.string.sdcard_full_tip)
                mWarnLevel2.add(lightCameraMemoryIsFull)
                mWarnList.add(lightCameraMemoryIsFull)
            }
            SDCardStatus.LOW_SPEED_CARD->{ //外部存储低速卡
                    val lightSDCardLowSpeed = Diagnostics()
                    lightSDCardLowSpeed.code = WarnBean.FLIGHT_SD_CARD_LOW_SPEED
                    lightSDCardLowSpeed.healthInformation = getHealthInfo(lightSDCardLowSpeed.code, WarningLevel.CAUTION)
                    val lightSdLowSpeedTipStr: String = String.format(ResourceUtils.getString(R.string.sdcard_low_speed_compatible), "")
                    lightSDCardLowSpeed.reason = lightSdLowSpeedTipStr
                    mWarnLevel2.add(lightSDCardLowSpeed)
                    mWarnList.add(lightSDCardLowSpeed)
            }
            SDCardStatus.CARD_ERROR->{ //异常卡
                val lightSDCardErr = Diagnostics()
                lightSDCardErr.code = WarnBean.FLIGHT_SD_CARD_ERR
                lightSDCardErr.healthInformation = getHealthInfo(lightSDCardErr.code, WarningLevel.CAUTION)
                lightSDCardErr.reason = String.format(ResourceUtils.getString(R.string.sdcard_exception_compatible), "")
                mWarnLevel2.add(lightSDCardErr)
                mWarnList.add(lightSDCardErr)
            }
            SDCardStatus.NO_CARD->{ //外部存储卡未插入
                val lightSDCardFormatErr = Diagnostics()
                lightSDCardFormatErr.code = WarnBean.NO_SDCARD
                lightSDCardFormatErr.healthInformation = getHealthInfo(lightSDCardFormatErr.code, WarningLevel.CAUTION)
                lightSDCardFormatErr.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10504005)
                mWarnLevel2.add(lightSDCardFormatErr)
                mWarnList.add(lightSDCardFormatErr)
            }
            SDCardStatus.EXTERNAL_CARD_FORMAT_ERROR, SDCardStatus.FORMAT_ERROR->{ //外部存储卡格式错误
                val lightSDCardFormatErr = Diagnostics()
                lightSDCardFormatErr.code = WarnBean.FLIGHT_SD_CARD_FORMAT_ERR
                lightSDCardFormatErr.healthInformation = getHealthInfo(lightSDCardFormatErr.code, WarningLevel.CAUTION)
                lightSDCardFormatErr.reason = String.format(ResourceUtils.getString(R.string.sdcard_format_error_compatible), "")
                mWarnLevel2.add(lightSDCardFormatErr)
                mWarnList.add(lightSDCardFormatErr)
            }

            else -> {}
        }

        val cameraConnectStatus = IGduDroneDevice.get.gimbal.gimbalStatus?.cameraConnectStatus
        val gimbalStatus = IGduDroneDevice.get.gimbal.gimbalStatus?.gimbalStatus

        if (!IGduDroneDevice.get.planType.value.isS200Type()) {
            if ( gimbalStatus == 2.toByte()) {
                // 云台自检异常
                val cloudSelfInspectWarn = Diagnostics()
                cloudSelfInspectWarn.code = WarnBean.ClOUND_SELFINSPECT
                cloudSelfInspectWarn.reason =
                    ResourceUtils.getString(R.string.gimbal_self_check_exception)
                cloudSelfInspectWarn.healthInformation = getHealthInfo(cloudSelfInspectWarn.code, WarningLevel.CAUTION)
                mWarnLevel2.add(cloudSelfInspectWarn)
                mWarnList.add(cloudSelfInspectWarn)
            }


            if (gimbalStatus == 1.toByte()) {
                // 云台堵转
                val cloudWarn = Diagnostics()
                cloudWarn.code = WarnBean.ClOUND
                cloudWarn.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10401001)
                cloudWarn.healthInformation = getHealthInfo(cloudWarn.code, WarningLevel.WARNING)
                mWarnLevel1.add(cloudWarn)
                mWarnList.add(cloudWarn)
            }
        }


        if (cameraConnectStatus == 1.toByte()) {
            // 云台连接异常
            val cloudConnectWarn = Diagnostics()
            cloudConnectWarn.code = WarnBean.ClOUND_CONNECT
            cloudConnectWarn.reason = ResourceUtils.getString(R.string.camera_connect_exception)
            cloudConnectWarn.healthInformation = getHealthInfo(cloudConnectWarn.code, WarningLevel.CAUTION)
            mWarnLevel2.add(cloudConnectWarn)
            mWarnList.add(cloudConnectWarn)
        }

        //飞控未连接异常检查
        if (!IFlightController.get.fcConnected.value) {
            // 飞控未连接异常
            val flyControlUnConnect = Diagnostics()
            flyControlUnConnect.code = WarnBean.FLY_CONTROL_UN_CONNECT
            flyControlUnConnect.reason = ResourceUtils.getString(R.string.fly_control_unconnect)
            flyControlUnConnect.healthInformation = getHealthInfo(flyControlUnConnect.code, WarningLevel.CAUTION)
            mWarnLevel2.add(flyControlUnConnect)
            mWarnList.add(flyControlUnConnect)
        }
    }

    /**
     * 处理飞控上报的异常协议数据
     * */
    private var nearNoFly: Boolean = false
    private var nearRestrictedArea = false
    private var inRestrictedArea = false
    private var inNoFly = false
    private fun handlerFCData(){
        val fcInfo1 = IFlightController.get.fcInfo1.value
        //禁飞区内
        if (fcInfo1?.droneInNoFly == true) {
            if (inNoFly) return
            ThreadHelper.runOnUiThreadDelayed({ inNoFly = false }, 10000)
            inNoFly = true
            val inNoFly = Diagnostics()
            inNoFly.code = WarnBean.INNOFLY
            inNoFly.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10109011)
            inNoFly.healthInformation = getHealthInfo(inNoFly.code, WarningLevel.WARNING)
            var sound = SoundConst.IN_FORBID_FLIGHT_AREA
            if (fcInfo1.droneFlyState.isGround()) {
                sound = SoundConst.IN_FORBID_FLIGHT_ONGROUND
                inNoFly.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10109001)
            }
            mWarnToast.add(inNoFly)
            GduSoundManager.playSound(sound, 10000, false)
        }else if (fcInfo1?.droneNearWarnZone == true) { //靠近禁飞区
            if (nearNoFly) return
            ThreadHelper.runOnUiThreadDelayed({ nearNoFly = false }, 10000)
            nearNoFly = true
            val nearNoFly = Diagnostics()
            nearNoFly.code = WarnBean.NEARNOFLY
            nearNoFly.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10109012)
            nearNoFly.healthInformation = getHealthInfo(nearNoFly.code, WarningLevel.WARNING)
            if (fcInfo1.droneFlyState.isGround()) {
                nearNoFly.reason = ResourceUtils.getString(R.string.Msg_ErrorCode_10109002)
            }
            mWarnToast.add(nearNoFly)
            GduSoundManager.playSound(SoundConst.NEAR_FORBID_AREA, 10000, false)
        }else if (fcInfo1?.restrictedAreaState == 8) { //限飞区内
            if (inRestrictedArea) return
            ThreadHelper.runOnUiThreadDelayed({ inRestrictedArea = false }, 60000)
            inRestrictedArea = true
            val inRestrictedArea = Diagnostics()
            inRestrictedArea.code = WarnBean.NEAR_RESTRICTED_AREA
            inRestrictedArea.reason = ResourceUtils.getString(R.string.in_restricted_area)
            inRestrictedArea.healthInformation = getHealthInfo(inRestrictedArea.code, WarningLevel.WARNING)
            mWarnToast.add(inRestrictedArea)
            GduSoundManager.playSound(SoundConst.IN_RESTRICTED_AREA, 60000, false)
        }else if (fcInfo1?.restrictedAreaState == 2) { //靠近限飞区
            if (nearRestrictedArea) return
            ThreadHelper.runOnUiThreadDelayed({ nearRestrictedArea = false }, 60000)
            nearRestrictedArea = true
            val nearRestrictedArea = Diagnostics()
            nearRestrictedArea.code = WarnBean.NEAR_RESTRICTED_AREA
            nearRestrictedArea.reason = ResourceUtils.getString(R.string.near_restricted_area)
            nearRestrictedArea.healthInformation = getHealthInfo(nearRestrictedArea.code, WarningLevel.WARNING)
            mWarnToast.add(nearRestrictedArea)
            GduSoundManager.playSound(SoundConst.NEAR_RESTRICTED_AREA, 60000, false)
        }
    }

    private fun getHealthInfo(code:Long, level: WarningLevel): DeviceHealthInformation {
        val healthInfo = DeviceHealthInformation()
        healthInfo.informationId = code
        healthInfo.warningLevel = level
        return healthInfo;
    }

    /**
     * 还原数据
     * */
    private fun reset(){
        mWarnList.clear()
        mWarnLevel1.clear()
        mWarnLevel2.clear()
        mWarnLevel3.clear()
    }

    private fun resetAll(){
        XLogger.ERRCODE.i("清空所有异常码缓存")
        reset()
        mWarnToast.clear()
        mWarnDialog.clear()
    }

    /**
     * 无异常
     * */
    private fun onNormalStatus(){
        reset()
        val warnBean = Diagnostics()
        if (IGduDroneDevice.get.isConnected){
            val sb = StringBuffer()
            warnBean.reason = sb.toString()
            XLogger.APP.i("wuqb", "飞行器已连接,,,,$sb,,,,")
        }else{
            warnBean.reason = ResourceUtils.getString(R.string.aircraft_not_connect)
            XLogger.APP.i("wuqb", "飞行器未连接,,,,")
        }
        errMsgSize.value = 0
        errRollMsg.value = warnBean
    }

    /**
     * 处理消息盒子数据展示
     * */
    private fun onMsgBoxList(){
        errMsgList.value = mWarnList
        errMsgSize.value = mWarnList.size
    }

    fun getMsgBoxList(): Collection<Diagnostics>{
        return mWarnList
    }

    /**
     * 处理头部滚动栏展示
     * 1.最多13个字符，超出的文案滚动显示
     * 2.状态分为一级红色，二级黄色
     * 3.轮播显示，优先显示一级告警，若同时有多个级别告警， 只显示一级告警，二级告警在消息盒子里显示；
     * 4.轮播逻辑，如果指定时间内，如果没有新增的异常，则按照顺序轮播，如果有异常内容变化，则需从头开始
     * */
    private fun onMsgRolling(){
        println("level1:"+mWarnLevel1.size+", level2:"+mWarnLevel2.size+", level3:"+mWarnLevel3.size+", index:"+currentShowIndex.get())
        try {
            if (mWarnLevel1.isEmpty()) {
                if (mWarnLevel2.isNotEmpty()) {
                    if (currentShowIndex.get() >= mWarnLevel2.size || currentShowIndex.get() < 0) {
                        currentShowIndex = AtomicInteger(0)
                    }
                    val index = currentShowIndex.getAndIncrement()
                    if (mWarnLevel2.size > index) {
                        val curr = mWarnLevel2[index]
                        println("curr2:"+curr.code+", index:"+index)
                        errRollMsg.value = curr
                    }
                } else if (mWarnLevel3.isNotEmpty()) {
                    if (currentShowIndex.get() >= mWarnLevel3.size || currentShowIndex.get() < 0) {
                        currentShowIndex = AtomicInteger(0)
                    }
                    val index = currentShowIndex.getAndIncrement()
                    if (mWarnLevel3.size > index) {
                        val curr = mWarnLevel3[index]
                        println("curr3:"+curr.code+", index:"+index)
                        errRollMsg.value = curr
                    }
                } else {
                    onNormalStatus()
                }
            } else {
                if (currentShowIndex.get() >= mWarnLevel1.size || currentShowIndex.get() < 0) {
                    currentShowIndex = AtomicInteger(0)
                }
                val index = currentShowIndex.getAndIncrement()
                if (mWarnLevel1.isNotEmpty() && mWarnLevel1.size > index) {
                    val curr = mWarnLevel1[index]
                    println("curr1:"+curr.code+", index:"+index)
                    errRollMsg.value = curr
                }
            }

        }catch (_:Exception){}
    }

    /**
     * 错误码弹窗展示
     * */
    private var mDialog: MsgBoxDialog? = null
    private fun onMsgDialog(){
        if (null == mDialog) {
            mDialog = (ActivityManager.getInstance().activity as? FragmentActivity)?.let {
                MsgBoxDialog(it)
            }
        }
        mDialog?.showErrCodeDialog(mWarnDialog)
    }

    /**
     * Toast展示
     * */
    private var mToast: MsgBoxToast? = null
    private fun onMsgToast(){
        if (!ActivityManager.getInstance().isTopFlightHomeActivity){
            XLogger.APP.i("当前不是主Activity")
            return
        }
        val activity = ActivityManager.getInstance().activity as? FragmentActivity
        if (mWarnToast.isEmpty()){
            XLogger.ERRCODE.i("Toast错误码数量为空")
            mToast?.onDismiss(activity?.supportFragmentManager)
            mToast = null
            return
        }
        if (null == mToast) {
            mToast = MsgBoxToast()
        }
        mToast?.showToast(mWarnToast)
        XLogger.APP.i("msgToast:manager=${activity?.supportFragmentManager==null}, isAdd=${mToast?.isAdded}")
        activity?.supportFragmentManager?.let {
            if (mToast?.isAdded == false){
                mToast?.show(it)
            }
        }

    }

    /**
     * 数据销毁
     * */
    fun onDestroy(){
        getAlarmDispose?.dispose()
        getAlarmDispose = null
        mToast?.onDismiss((ActivityManager.getInstance().activity as? FragmentActivity)?.supportFragmentManager)
    }

    override fun onUpdate(p0: MutableList<Diagnostics>?) {
        ThreadHelper.runOnUiThread {
            println("==============================")
            if (p0 != null) {
                initData(p0)
            }
        }
    }
}