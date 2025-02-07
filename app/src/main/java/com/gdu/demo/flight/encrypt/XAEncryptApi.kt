//package com.gdu.demo.flight.encrypt
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Build
//import android.os.Process
//import android.provider.Settings
//import android.text.TextUtils
//import android.util.Base64
//import com.gdu.config.GduAppEnv
//import com.gdu.config.GlobalVariable
//import com.gdu.util.MyConstants
//import com.gdu.util.SPUtils
//import com.gdu.util.SystemUtils
//import com.gdu.util.ThreadHelper
//import com.gdu.util.mqtt.MqttProxy
//import org.json.JSONObject
//import javax.net.ssl.SSLSocketFactory
//
///**
// * 信安加密SDK的api包装类
// *
// * 信安SDK版本：3.0.12.1_Android_build20231206 (常规版本，无法解签原始数据)
// * 信安SDK版本: 3.0.0.0_Android_build20240725:3.0.0.8 （新版本）
// * @author guoyang
// */
//@SuppressLint("HardwareIds")
//object XAEncryptApi {
//
//    const val TAG = "XAEncrypt"
//    private const val SP_XA_CERT_EXIST = "xa_encrypt_cert_exist"
//    private const val NET_TIMEOUT = 10 * 1000 // 信安接口访问的超时时间（初始化，签约等接口）
//
//    // PIN码（设置给证书的PIN码）（暂时使用ANDROID_ID, 最大长度256）
//    private val mPin : String by lazy {
//        val androidId = Settings.Secure.getString(GduAppEnv.application.contentResolver, Settings.Secure.ANDROID_ID)
//        androidId
//    }
//    // 信安MAuth的用户名（绑定设备的设备SN）
//    private val mXAUserName : String by lazy { generateSN() }
//
//    private val mCertManager: CertManager by lazy { CertManager(GduAppEnv.application) }
//    private val mSignManager: SignManager by lazy { SignManager(GduAppEnv.application) }
//    private var mHasInit = false
//
//    /**
//     * 信安加密SDK 初始化
//     * 需要读写存储权限（日志在sdcard中，没有权限则在app内的file目录中）
//     */
//    @JvmStatic
//    fun init(callback: (() -> Unit)?) {
//        if (mHasInit) {
//            callback?.invoke()
//            return
//        }
//        // 非主进程不调用初始化
//        if (!SystemUtils.isMainProcess()) {
//            callback?.invoke()
//            return
//        }
//
//        // callback不等于空，认为是启动页流程过来得
//        // 如果是非东高渠道，需要启动页时检查证书是否已经下载成功(不能用isCertExist方法，因为sdk还没初始化)
//        // 东高渠道就跳过，直接去下载初始化
//        if (callback != null
//                && !EncryptHelper.isDgSecretChannel()
//                && !SPUtils.getBoolean(GduAppEnv.application, SP_XA_CERT_EXIST)) {
//            encryptLog( "[init] stop!!! cert not exist!")
//            callback.invoke()
//            return
//        }
//
//        ThreadHelper.runOnAsync {
//
//            // 异步调用IMSSdk，IMSSdk类加载时会非常耗时，在主线程直接ANR
//            IMSSdk.mContext = GduAppEnv.application
//            IMSSdk.enableLogRequestTime = GduAppEnv.DEBUG
//            IMSSdk.enableDebugLog = GduAppEnv.DEBUG
//            IMSSdk.setNetworkTimeout(NET_TIMEOUT)
//
//            encryptLog("[init] HOST = ${XAEncryptConfig.HOST}")
//            encryptLog("[init] APP_ID = ${XAEncryptConfig.APP_ID}")
//
//            IMSSdk.initialization(
//                GduAppEnv.application,
//                XAEncryptConfig.HOST,
//                XAEncryptConfig.TRUST_CERT,
//                XAEncryptConfig.APP_ID,
//                XAEncryptConfig.APP_SECRET
//            ) { result ->
//
//                encryptLog("[init] resultID = ${result?.resultID} , resultDesc = ${result?.resultDesc}")
//                openLocalSignSwitch()
//                logIMSSdkInfo()
//
//                if (Result.OPERATION_SUCCEED == result?.resultID) {
//                    mHasInit = true
//
//                    // 执行下证流程，如果是隐藏入口重启到这里的，会直接跳过
//                    verifyCert {
//
//                        // 是启动页的话，这里就回调出去了，执行启动页后续逻辑
//                        callback?.let {
//                            ThreadHelper.runOnUiThread {
//                                it.invoke()
//                            }
//                        }
//
//                        if (canEncrypt()) {
//                            startRecordNativeLog()
//                            // 已签约、已下证
//                            ThreadHelper.runOnUiThread {
//                                Toaster.show(R.string.encrypt_flow_complete)
//                            }
//                        } else {
//                            Toaster.showLong(R.string.encrypt_flow_init_fail_cancel)
//                        }
//                    }
//                } else {
//                    EncryptHelper.showRetryInitDialog(result?.resultID, result?.resultDesc, {
//                        // 重试
//                        init(callback)
//                    }, {
//                        // 取消
//                        Toaster.showLong(R.string.encrypt_flow_init_fail_cancel)
//                        callback?.invoke()
//                    })
//                }
//            }
//        }
//    }
//
//    private fun reInit() {
//        if (mHasInit) {
//            // 直接去验证证书
//            verifyCert(null)
//        } else {
//            init(null)
//        }
//    }
//
//
//    /**
//     * 检查证书状态
//     * 没有下载则去下载证书
//     */
//    private fun verifyCert(callback: (() -> Unit)?) {
//        // local native method check Cert
//        val isCertExist = mCertManager.isCertExist(mXAUserName)
//        SPUtils.put(GduAppEnv.application, SP_XA_CERT_EXIST, isCertExist)
//        encryptLog("[verifyCert] isCertExist = $isCertExist")
//
//        if (!isCertExist) {
//            // 证书不存在，下载证书
//            mCertManager.requestCertDirect(mXAUserName, mPin) {
//                encryptLog("[verifyCert] resultID = ${it?.resultID} , resultDesc = ${it?.resultDesc}")
//
//                if (Result.OPERATION_SUCCEED == it?.resultID) {
//                    SPUtils.put(GduAppEnv.application, SP_XA_CERT_EXIST, true)
//
//                    // 下载证书成功；重启app
//                    ThreadHelper.runOnUiThread {
//                        Toaster.show(R.string.encrypt_flow_complete)
//
//                        callback?.invoke()
//
//                        // 非渠道在启动页下载证书的，都需要重启
//                        if (!EncryptHelper.isTopSplashActivity()) {
//                            reboot()
//                        }
//                    }
//                } else {
//                    EncryptHelper.showRetryDownloadCertDialog(it?.resultID, it?.resultDesc, {
//                        verifyCert(callback)
//                    }, {
//                        callback?.invoke()
//                    })
//                }
//            }
//        } else {
//            // 证书有效期10年，证书其他状态问题在建立SSL阶段暴露问题
//            // 证书到期后通过app更新方式调整
//            // CertManager.getStatusOnLine 返回证书的4种状态，为简化流程，默认证书不会冻结，不会作废
//            // Result.STATUS_NOT_ACQUIRED：证书未申请
//            // Result.STATUS_EFFECTIVE：证书有效
//            // Result.STATUS_LOCKED：证书被冻结
//            // Result.STATUS_REVOKED：证书被作废
//
//            // 证书存在，直接回调
//            callback?.let {
//                ThreadHelper.runOnUiThread {
//                    it.invoke()
//                }
//            }
//        }
//    }
//
//    /**
//     * 重建mqtt链接
//     */
//    private fun rebuildSslMqttAndHttps() {
//        encryptLog("[rebuildSslMqtt]")
//
//        // 连上了才尝试重连，没连上不管，可能是没有连飞机，也有可能没有配置mqtt配置
//        if (MqttProxy.mqttManagerProxy.isConnect()) {
//            encryptLog("[rebuildSslMqtt] MqttManager has connected; reconnect")
//            MqttProxy.mqttManagerProxy.destroyClient()
//            MqttProxy.mqttManagerProxy.initMqttClient()
//        }
//
//        // 直接销毁，BackupsAirLink会定时重连（启动延时5s，3s重试）
//        MqttProxy.mqttBackupsAirLinkProxy.destroyMqtt()
//
//        ThreadHelper.runOnUiThread {
//            Toaster.show(R.string.encrypt_flow_complete)
//        }
//    }
//
//    /**
//     * 能否加密
//     * 已完全签约，已下载证书
//     * @return true: 可以加密
//     */
//    @JvmStatic
//    fun canEncrypt(): Boolean {
//        return mHasInit
//                && mXAUserName.isNotBlank()
//                && mCertManager.isCertExist(mXAUserName)
//    }
//
//    /**
//     * 是否支持签名解签
//     * 1. 加密流程完成（初始化，签约，下证）
//     * 2. mqtt地址是ssl
//     *
//     * @param isLteOrIot true：lte（备份链路）；false：iot感知链路
//     */
//    private fun canSign(isLteOrIot: Boolean): Boolean  {
//        // 优先判断信安环境
//        if (!canEncrypt()) {
//            return false
//        }
//
//        val url = if (isLteOrIot) {
//            GlobalVariable.BackAirLinkUrl
//        } else {
//            SPUtils.getString(GduAppEnv.application, "SP_MQTT_URL_KEY", "")
//        }
//        return url?.startsWith("ssl", true) == true
//    }
//
//    /**
//     * 获取MQTT的SSL下的SocketFactory
//     * @return SSLSocketFactory Factory
//     */
//    @JvmStatic
//    fun getSSLSocketFactory(mqttUrl: String?): SSLSocketFactory? {
//        val can = canEncrypt()
//        encryptLog("[getSSLSocketFactory] canEncrypt = $can; mqttUrl = $mqttUrl")
//
//        if (!can || mqttUrl?.startsWith("ssl", true) != true) {
//            return null
//        }
//        encryptLog("[getSSLSocketFactory] createSSLSocketFactory!")
//        return TLSAndroidUtils.createSSLSocketFactory(
//            XAEncryptConfig.SSL_PROTOCOL,
//            arrayOf(
//                XAEncryptConfig.MQTT_TRUST_CERT_DG1,
//                XAEncryptConfig.MQTT_TRUST_CERT_DG2
//            )
//        )
//    }
//
//    /**
//     * 获取OKHTTP的SSL下的Builder
//     * @return Builder Builder
//     */
//    @JvmStatic
//    fun getSSLOkHttpBuilder(): OkHttpClient.Builder {
//        return OkHttpClient.Builder()
//    }
//
//    /**
//     * 签名
//     * @param bytes 要签名的数据
//     * @param isLteOrIot true：lte（备份链路）；false：iot感知链路
//     */
//    @JvmStatic
//    fun doSignBytes(bytes: ByteArray?, isLteOrIot: Boolean):ByteArray? {
//
//        if (!canSign(isLteOrIot) || bytes == null || bytes.isEmpty()) {
//            return bytes
//        }
//
////        encryptLog( "[doSignBytes] source hexString = ${bytes2HexString(bytes)}")
//        encryptLog( "[doSignBytes] bytes.size = ${bytes.size}")
//
//        // sign内部会verifyPIN PIN码验证错误次数过多，会造成user被锁
//        // 签名有4种方式
//        // 1. signManager.signWithToken() // 签名的同时产生动态口令，
//        // 2. signManager.signFrontAuth() // 签名前先进行口令验证
//        // 2. signManager.signHash()      // 对已经做过HASH操作的数据进行签名
//        // 3. signManager.sign()          // 纯签名
//        // 签名有三种类型
//        // 0 : Raw签名     对应验签: rawVerifyLocal
//        // 1 : Attach签名  对应验签: attachVerifyLocal
//        // 2 : Detach签名  对应验签: detachVerifyLocal
//
//        openLocalSignSwitch()
//
//        // 开启本地签名后，下面方法为同步方法
//        var signData = bytes
//        mSignManager.sign(mXAUserName, mPin, bytes, 1) {
//            encryptLog("[doSing] resultID = ${it?.resultID} , resultDesc = ${it?.resultDesc}")
//            if (Result.OPERATION_SUCCEED == it?.resultID) {
//                // 签名成功
//                signData = it.resultDesc.toByteArray()
//            }
//        }
//        return signData
//    }
//
//    /**
//     * 本地验证签名
//     * @param signData 加密数据
//     * @param isLteOrIot true：lte（备份链路）；false：iot感知链路
//     */
//    @JvmStatic
//    fun doVerifySign(signData: ByteArray?, isLteOrIot: Boolean): ByteArray? {
//        if (!canSign(isLteOrIot) || signData == null || signData.isEmpty()) {
//            return signData
//        }
//
//        var bytes: ByteArray? = null
//        // 下面方法为同步方法
//        mSignManager.attachVerifyLocalWithPlain(String(signData)) {
//            if (it.resultID == Result.OPERATION_SUCCEED) {
//                bytes = checkSignJson(it.resultDesc)
//            }
//            encryptLog("[doVerifySign] resultID = ${it.resultID}; resultDesc ${bytes2HexString(bytes)}")
//        }
//        if (bytes == null) {
//            bytes = signData
//        }
//        // 1. 如果是iot给的数据，一般都是json格式，这个时候的byte 可以根据utf8转换为字符串。byte数据不会丢失
//        // 2. 如果是LTE给的数据，一般都是byte数组，是自定义的格式，不是utf8中包含的字符集，转为字符串会导致数据丢失
//        //    如果需要转为字符串，在转回来一定得使用Charsets.ISO_8859_1
//        return bytes
//    }
//
//
//    private fun checkSignJson(json: String?): ByteArray? {
//        if (json.isNullOrBlank()) {
//            return null
//        }
//        try {
//            return Base64.decode(
//                        JSONObject(json).optString("plain"),
//                        Base64.NO_WRAP
//            )
//        } catch (ignore: Exception) {
//        }
//        return json.toByteArray()
//    }
//
//    /**
//     * 显示开启流程的弹窗
//     * 直接下证，后端配置后可以跳过签约码
//     */
//    @JvmStatic
//    fun showStartFlowDialog() {
//        EncryptHelper.showStartEncryptFlowDialog {
//            reInit()
//        }
//    }
//
//    private fun reboot() {
//        encryptLog("[reboot] reboot!!")
//        EncryptHelper.showRebootDialog{
//            val intent: Intent? = GduAppEnv.application.packageManager.getLaunchIntentForPackage(GduAppEnv.application.packageName)
//            intent?.addFlags(
//                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            )
//            GduAppEnv.application.startActivity(intent)
//
//            Process.killProcess(Process.myPid())
//            System.exit(0)
//        }
//    }
//
//    /**
//     * app是否已登录
//     * @return true：已登录（无登录模式直接返回true）
//     */
//    private fun hasLogin(): Boolean {
//        val tokenStr = SPUtils.getString(GduAppEnv.application, MyConstants.SAVE_TOKEN)
//        if (!GlobalVariable.isUnUseLoginMode) {
//            return !TextUtils.isEmpty(tokenStr)
//        }
//        return true
//    }
//
//    /**
//     * 优先使用SN作为用户名，如果SN没有，则使用变形的AndroidID
//     */
//    private fun generateSN(): String {
//        var sn: String? = null
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
//                && (GlobalVariable.isRCSEE || GlobalVariable.isCustomRC)) {
//            sn = Build.getSerial()
//        }
//        // 0123456789ABCDEF 这是老遥控器在使用的固定SN
//        if (sn.isNullOrBlank() || "0123456789ABCDEF".equals(sn)) {
//            sn = "p${mPin}in"
//        }
//        encryptLog("gen username sn = $sn")
//        return sn
//    }
//
//    private fun logIMSSdkInfo() {
//        encryptLog(
//            "SDK版本: ${IMSSdk.version()} \n"
//               + "证书是否可用: ${IMSSdk.isCertAvailable()} \n"
//               + "是否为双证: ${IMSSdk.isPairedCert()} \n"
//               + "数字签名是否可用: ${IMSSdk.isSignAvailable()} \n"
//               + "Log目录: ${IMSSdk.getLogPath()} \n"
//               + "* 客户是否注册: ${IMSSdk.clientRegistEnable()} \n"
//               + "* 证书KEY的长度: ${IMSAction(IMSSdk.mContext).certKeyLength} \n"
//               + "* 是否为协同签名(false为本地签名): ${IMSSdk.isCollaborative()} \n"
//        )
//    }
//
//}