package com.gdu.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.gdu.lib.base.GduEnvConfig;

/**
 * SharedPrefrences的工具类
 *
 * @author Administrator
 */
public class SPUtils {
    private static final String SP_NAME = "config";

    /**
     * 已经注册过的SN飞机
     * 完成实名登记
     */
    public static final String REGISTER_UAV_SN = "register_uav_sn";
    /**
     * 最后一次使用的飞机类型
     */
    public static final String USER_LAST_PLANTYPE = "user_last_plantype";
    /**
     * 最后一次使用的云台类型
     */
    public static final String USER_LAST_GIMBAL = "user_last_gimbal";
    /**
     * 高德地图初始化定位坐标
     */
    public static final String AMAP_LATLNG = "amap_latlng";
    /**
     * gooogle地图初始化定位坐标
     */
    public static final String GOOGLE_LATLNG = "google_latlng";
    /**
     * 绑定邮箱
     */
    public static final String BIND_EMAILE = "bingEmail";
    /**
     * 更换手机号或者绑定
     */
    public static final String REPLACE2BIND_MOBILE = "replace2bingMobile";

    /**
     * 是否同意用户协议和隐私政策
     */
    public static final String IS_AGREE_USE_PROTOCOL = "isAgreeProtocol";

    /**
     * 主页面欢迎界面，是否是第一次连接O2飞机
     */
    public static final String IS_FIRST_CONN_PLAN = "isfirstconnplan";
    /** 记录最后一次设置的限高值 */
    public static final String LAST_LIMIT_HEIGHT = "lastLimitHeight";
    /** 记录最后一次设置的限距值 */
    public static final String LAST_LIMIT_DISTANCE = "lastLimitDistance";
    /**
     * 引导图层，是否是第一次进入
     * 遥控器连接-经典模式
     */
    public static final String IS_FIRST_IN_REMOTE_CLASSICS_MODE = "isfirstinremoteclassics";
    /**
     * 引导图层，是否是第一次进入
     * 遥控器连接-智能模式
     */
    public static final String IS_FIRST_IN_REMOTE_SMART_MODE = "isfirstinremotesmart";
    /**
     * 引导图层，是否是第一次进入
     * 地图页面
     */
    public static final String IS_FIRST_IN_MAP_PAGE = "isfirstinmappage";
    /**
     * 引导图层，是否是第一次进入Smart 倒飞自拍中
     */
    public static final String IS_FIRST_IN_SMART_INVERTED = "isfirstInverted";

    /**
     * 保存 版本信息的2 版本号和 code
     */
    public static final String SF_VERSION = "sf_Version";
    public static final String SF_VERSIONCODE = "sf_VersionCode";
    // 航迹规划
    public static String MAPTYPE = "maptype";
    public static String ZORRO_DELAY = "zorro_delay";
    /**
     * 增加测试模式 的状态保存
     */
    public static String TEXT_MODE = "text_mode";

    /********************************************--ron
     * 测试服务器MODE,是否启动测试服务器的模式
     */
    public static String TestServerMode = "TestServerMode";

    /********************************************--ron
     * 测试服务器MODE,是否启动测试服务器的模式
     */
    public static String ScreenOrientationIsOpen = "ScreenOrientationIsOpen";

    /********ron***********
     * 控制模式----ron
     **********************/
    public static String CONTROLMODEL = "CONTROLMODEL_RON";

    /**********************
     * 记得当前的模式-----ron
     */
    public static String REMANDMODEL = "REMANDMODEL";


    /*斜飞，垂飞 结束后动作*/

    /**************
     * 垂飞结束后的动作 ----ron
     */
    public static final String EXIT_ACTION_Vertical = "EXIT_ACTION_VERTICAL";

    /******************************
     * 斜飞结束后的动作 ----ron
     */
    public static final String EXIT_ACTION_Inverted = "EXIT_ACTION_Inverted";

    public static final int ACTION_HOVER = 0;

    public static final int ACTION_RETURN = 1;

    /*斜飞，垂飞 相机动作zhaijiang*/
    /********************
     * 垂飞的时候 相机的动作 ---ron
     */
    public static final String CAMERA_ACTION_VERTAICAL = "CAMERA_ACTION_VERTAICAL";

    /**************************
     * 斜飞的时候，相机的动作----ron
     */
    public static final String CAMERA_ACTION_INVERTED = "CAMERA_ACTION_INVERTED";

    /**************************
     * 垂直的距离-------ron
     */
    public static final String DISTENSE_VERTAICAL = "DISTENSE_VERTAICAL";

    /******************************
     * 斜飞的距离-------ron
     */
    public static final String DISTENSE_INVERTED = "DISTENSE_INVERTED";

    /************************
     * 是否显示避障信息----ron
     */
    public static final String SHOWOBSTACLEVIEW = "SHOWOBSTACLEVIEW";

    /**
     * 限飞高度
     */
    public static final String HEIGHT_LIMIT = "height_limit";

    /**
     * 限飞距离
     */
    public static final String DISTANCE_LIMIT = "distance_limit";

    /**
     * rtk是否开启
     */
    public static final String RTK_SWITCH = "rtk_switch";

    /**
     *  RTK是否连接成功  连接成功下次自动连接
     */
    public static final String RTK_CONNECT_STATE = "RTK_CONNECT_SUCC";

    
    /**
     *  bd报平安选中模版ID
     */
    public static final String BD_REPORT_MSG_ID = "BD_REPORT_MSG_ID";


    /**
     * bd报平安是否携带经纬度
     */
    public static final String BD_REPORT_SAVE_ADD_POSITION = "BD_REPORT_SAVE_ADD_POSITION";


    /**
     * bd sos开关
     */
    public static final String BD_SOS_SWITCH = "BD_SOS_SWITCH";

    


    /************************
     * 最后飞机经纬度，用于找飞机
     */
    public static final String LAST_DRONE_POSITION = "LAST_DRONE_POSITION";
    public static final String LAST_DRONE_TIME = "LAST_DRONE_TIME";
    public static final String LAST_DRONE_HEIGHT = "LAST_DRONE_HEIGHT";


    public static final int ACTION_RECORD = 0;

    public static final int ACTION_PHOTO = 1;

    public static final int ACTION_MANUAL = 2;


    //2.0 飞行记录常量保存

    public static final String Timestamp = "fr_timestamp";
    public static final String Startplace = "fr_startplace";
    public static final String Maxhight = "fr_Maxhight";
    public static final String Maxdistance = "fr_Maxdistance";
    public static final String Flighttime = "fr_flighttime";
    public static final String Weather = "fr_weather";
    public static final String Json = "fr_json";// 航迹点json
    public static final String START_PLACE_INFO = "fr_statrplace";
    public static final String STARTTIME = "StartTime2minute2second";

    public static final String MD5String = "md5String";
    public static final String FILEPATH = "filePath";
    public static final String FIRWARETYPE = "firmwreType";

    /***************************flightMs账号******************************/
    /** 客户id */
//    public static final String flightMsCustomerId="customerId";
    /** 密钥 */
//    public static final String flightMsSignKey="signKey";
    /** 用户ID */
    public static final String flightMsId = "id";
    /** 任务ID */
    //    public static final String flightMsTaskId="taskId";
    /** 电话 */
    public static final String flightMsMobile = "mobile";
    /** 电话 */
    public static final String flightMsEmail = "email";
    /** 国籍 */
    public static final String flightMsCountry = "country";
    /** 头像地址 */
    public static final String flightMsHeadUrl = "headUrl";
    /** 用户名 */
    public static final String flightMsUserName = "userName";
    /** 团队任务列表的json */
    public static final String flightTaskJson = "taskJson";
    public static final String RightMenuElec = "rightMenuElec";

    /** RTK模块是否开启 */
    public static final String RTK_MODULE_STATUS = "rtkModuleStatus";
    /** RTK IP */
    public static final String RTK_IP = "rtkIp";
    /** RTK Port */
    public static final String RTK_PORT = "rtkPort";
    /** RTK 端口 */
    public static final String RTK_ACCOUNT = "rtkAccount";
    /** RTK 密码 */
    public static final String RTK_PASSWORD = "rtkPassword";
    /** RTK 挂载点 */
    public static final String RTK_MOUNT_POINT = "rtkMountPoint";
    /** RTK AppKey */
    public static final String RTK_AK = "rtkAK";
    /** RTK AppSecret */
    public static final String RTK_AS = "rtkAS";
    /** RTK DeviceID */
    public static final String RTK_DEVICE_ID = "rtkDeviceID";

    public static final String MS_ADDRESS = "ms_address";
    /**
     * app对应了多个平台。部分平台的协议和其他的不兼容，需要明显平台的类型，做响应的业务逻辑处理 ---ron
     */
    public static final String serverTypeLabel = "serverTypeLabel";

    /**
     * app对应多个平台的时候，httpIP的地址
     */
    public static final String ServerUseHttpIp = "ServerUseHttpIp";

    public static final String MS_PORT = "ms_port";
    public static final String PUSH_PORT = "push_port";
    public static final String ORGANIZATION_PORT = "organization_port";

    public static final String GB28181_DEVICE_ID = "gb28181_device_id";

    public static final String LIVE_TYPE = "live_type";
    /** 附挂机测试模式状态保存key */
    public static String FUGUAJI_DEBUG_MODE = "fuguaji_debug_mode";
    /** 存储是否有记录账号密码的key */
    @Deprecated
    public static String IS_RECORD_ACCOUNT = "isRecordAccount";
    /** 存储记录的账号的key */
    @Deprecated
    public static String RECORD_ACCOUNT = "RecordAccount";
    /** 存储记录的密码的key */
    @Deprecated
    public static String RECORD_PWD = "RecordPWD";
    /** 存储记录的登录类型的key */
    @Deprecated
    public static String RECORD_LOGINTYPE = "RecordLoginType";
    /** 存储记录的企业编码的key */
    @Deprecated
    public static String RECORD_ENTERPRISE_CODE = "RecordEnterpriseCode";
    /**
     * app对应多个平台的时候，验证码的保存情况
     */
    public static final String ServerUseToken = "ServerUseToken";

    /**
     *  高增益-高温报警值
     */
    public static final String High_Enhance_High_Temp_Warn_Value ="highEnhanceHighTempWarnValue";

    /**
     *  高增益-低温报警值
     */
    public static final String High_Enhance_Low_Temp_Warn_Value ="highEnhanceLowTempWarnValue";

    /**
     *  低增益-高温报警值
     */
    public static final String Low_Enhance_High_Temp_Warn_Value ="lowEnhanceHighTempWarnValue";

    /**
     *  低增益-低温报警值
     */
    public static final String Low_Enhance_Low_Temp_Warn_Value ="lowEnhanceLowTempWarnValue";

    /**
     * 高低温报警开关
     */
    public static final String High_Low_Temp_Warn_Switch ="highLowTempWarnSwitch";

    /**
     * 高低温报警声音开关
     */
    public static final String High_Low_Temp_Warn_Voice_Switch ="highLowTempWarnVoiceSwitch";

    /**
     * 红外测温开关
     */
    @Deprecated
    public static final String IR_Temp_Switch ="irTempSwitch";

    /**
     * 存储飞机SN
     */
    public static final String DRONE_SN = "DRONE_SN";

    /**
     * 上一次链路类型
     */
    public static final String LAST_AIRLINK_TYPE = "LAST_AIRLINK_TYPE";


    public static final String FIVE_CAMERA_SHOW_VIEW = "five_camera_show_view";

    public static final String GIMBAL_FOLLOW = "GIMBAL_FOLLOW";

    /** 是否拒绝过授权请求 */
    public static final String IS_REJECTED_PERMISSION = "isRejectedPermission";

    public static final String LAST_REQUEST_PERMISSION_TIME = "LAST_REQUEST_PERMISSION_TIME";

    /** 是否同意所有授权 */
    public static final String IS_AGREE_ALL_PERMISSION = "isAgreeAllPermission";

    /** 获取创建打点定位点时的默认名称 */
    public static final String AUTO_LOCATION_POINT_ID = "crateLocationPointId";


    /**
     * 4G备份图传MQTT的地址
     */
    public static final String BACK_AIR_LINK_URL = "backAirLinkUrl";

    /**
     * 4G备份图传RTSP的地址
     */
    public static final String BACK_AIR_LINK_RTSP_URL = "backAirLinkRtspUrl";


    /**
     *  地图类型
     */
    public static final String MAP_TYPE = "mapType";

    /**
     * 上次保存的遥控器提示时间
     */
    public static final String KEY_LAST_RC_TIME = "KEY_LAST_RC_TIME";
    /**
     * 上次保存的飞机开机时间
     */
    public static final String KEY_LAST_PLANE_TIME = "KEY_LAST_PLANE_TIME";


    /**
     *  上次遥控器最后连接时间
     */
    public static final String KEY_LAST_CONNECT_TIME = "KEY_LAST_CONNECT_TIME";

    /**
     *  飞机上次连接时是否连接4G
     */
    public static final String KEY_LAST_CONNECT_LTE = "KEY_LAST_CONNECT_LTE";

    /**
     * 当前选择的语言类型
     */
    public static final String KEY_LANGUAGE_TYPE = "KEY_LANGUAGE_TYPE";


    /**
     *  OPERATOR_ID
     */
    public static final String KEY_OPERATOR_ID = "KEY_OPERATOR_ID";

    /**
     *  上报UOM飞行数据开关状态
     */
    public static final String KEY_UOM_UPLOAD_FLIGHT_DATA_SWITCH = "KEY_UOM_UPLOAD_FLIGHT_DATA_SWITCH";
    /**
     *  UOM上报架次ID
     */
    public static final String KEY_UOM_UPLOAD_ORDERID = "KEY_UOM_UPLOAD_ORDERID";
    /**
     * 4G备份图传RTSP的地址
     */
    public static final String BACK_HDMI_CAST_POSITION = "BACK_HDMI_CAST_POSITION";
    /**
     * 4G备份图传RTSP的地址
     */
    public static final String BACK_WIFI_CAST_POSITION = "BACK_WIFI_CAST_POSITION";
    /** 存储记录登录缓存的服务器地址的key */
    @Deprecated
    public static String RECORD_LOGIN_SERVER_URL = "RecordLoginServerUrl";

    /**
     * 本地限高参数
     */
    public static String KEY_LOCAL_HEIGHT_LIMIT = "KEY_LOCAL_HEIGHT_LIMIT";

    /**
     * 飞行总架次
     */
    public static final String KEY_FLIGHT_SORTIES = "KEY_FLIGHT_SORTIES";

    /**
     * 当前SN的飞行总架次
     */
    public static final String KEY_FLIGHT_SORTIES_SN = "KEY_FLIGHT_SORTIES_SN";

    /**
     * 当前SN的飞行总架次总时长（单位：分钟）
     */
    public static final String KEY_FLIGHT_SORTIES_ALL_TIME = "KEY_FLIGHT_SORTIES_ALL_TIME";

    /**
     * 缓存上一次获取飞控的失联行为
     */
    public static final String KEY_FC_LAST_LOST_ACTION = "KEY_FC_LAST_LOST_ACTION";

    /**
     * 万能的put方法
     *
     * @param context
     * @param key
     * @param value
     */
    public static void put(Context context, String key, Object value) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        //instanceof 用于判断数据类型的
        if (value instanceof String) {
            edit.putString(key, (String) value);
        } else if (value instanceof Integer) {
            edit.putInt(key, (int) value);
        } else if (value instanceof Byte) {
            edit.putInt(key, (int) value);
        } else if (value instanceof Double) {
            edit.putFloat(key, (Float) value);
        } else if (value instanceof Boolean) {
            edit.putBoolean(key, (boolean) value);
        } else if (value instanceof Long) {
            edit.putLong(key, (Long) value);
        }
        boolean commit = edit.commit();
        if (commit){
            LRUCacheUtil.put(key,value);
        }
    }

    public static void removeByKey(String key) {
        SharedPreferences sp = GduEnvConfig.application.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Editor edit = sp.edit();
        edit.remove(key);
        boolean commit = edit.commit();
        if(commit) {
            LRUCacheUtil.remove(key);
        }
    }

    /**
     * 获取字符串
     *
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, String key) {
        return getString(context,key,"");
    }

    public static String getString(Context context, String key, String defaultValue) {
        if (context == null) {
            return null;
        }
        String cache = LRUCacheUtil.getString(key);
        if (cache != null){
            return cache;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String value = sp.getString(key, defaultValue);
        LRUCacheUtil.put(key,value);
        return value;
    }

    /**
     * 获取long
     *
     * @param context
     * @param key
     * @return
     */
    public static Long getLong(Context context, String key) {
        return getCustomLong(context,key,0);
    }

    /**
     * 获取Int
     *
     * @param context
     * @param key
     * @return
     */
    public static Long getCustomLong(Context context, String key, long custom) {
        if (context == null) {
            return null;
        }
        Long cache = LRUCacheUtil.getLong(key);
        if (cache != null){
            return cache;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        long value = sp.getLong(key, custom);
        LRUCacheUtil.put(key,value);
        return value;
    }

    /**
     * 获取整数
     *
     * @param context
     * @param key
     * @return
     */
    public static Float getFloat(Context context, String key) {
        Float cache = LRUCacheUtil.getFloat(key);
        if (cache != null){
            return cache;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        float value = sp.getFloat(key, 0);
        LRUCacheUtil.put(key,value);
        return value;
    }

    /**
     * 获取Int
     *
     * @param context
     * @param key
     * @return
     */
    public static int getInt(Context context, String key) {
        return getCustomInt(context,key,0);
    }

    /**
     * 获取Int
     *
     * @param context
     * @param key
     * @return
     */
    public static int getCustomInt(Context context, String key, int custom) {
        if (context == null) {
            return 0;
        }
        Integer cache = LRUCacheUtil.getInt(key);
        if (cache != null){
            return cache;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        int value = sp.getInt(key, custom);
        LRUCacheUtil.put(key,value);
        return value;
    }

    /**
     * 获取Boolean
     * 默认为 false
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getBoolean(Context context, String key) {
        return getCustomBoolean(context,key,false);
    }

    /**
     * 获取Boolean
     * 设置自定义 Boolean
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getCustomBoolean(Context context, String key, boolean isOpen) {
        if (context == null) {
            return false;
        }
        Boolean cache = LRUCacheUtil.getBool(key);
        if (cache != null){
            return cache;
        }
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, isOpen);
        LRUCacheUtil.put(key,value);
        return value;
    }

    /**
     * 获取Boolean
     * 默认为 false
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getFalseBoolean(Context context, String key) {
        return getCustomBoolean(context,key,false);
    }

    /**
     * 获取Boolean
     * 默认为 true
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean getTrueBoolean(Context context, String key) {
        return getCustomBoolean(context,key,true);
    }

    /**
     * <p>shang</p>
     * <p>获取有默认值的字符串</p>
     * <p>是固件版本用来对比的</p>
     *
     * @param context
     * @param key
     * @return
     */
    public static String getDefaultString(Context context, String key) {
        return getString(context,key,"0");
    }

//    public static void savePreWifiSSId(String ssid) {
//        put(GduEnvConfig.application, MyConstants.KEY_PRE_CONNECTED_WIFI_SSID, ssid);
//    }
//
//    public static String getPreWifiSSId() {
//        return getString(GduEnvConfig.application, MyConstants.KEY_PRE_CONNECTED_WIFI_SSID);
//    }
//
//    public static String getWifiInfo() {
//        return getString(GduEnvConfig.application, MyConstants.SAVE_WIFI_INFO_KEY);
//    }
//
//    public static void saveDroneNoFlyZoneDownloadInfo(String json) {
//        put(GduEnvConfig.application, MyConstants.KEY_DRONE_NO_FLY_ZONE_DOWNLOAD_INFO, json);
//    }
//
//    public static String getDroneNoFlyZoneDownloadInfo() {
//        return getString(GduEnvConfig.application, MyConstants.KEY_DRONE_NO_FLY_ZONE_DOWNLOAD_INFO);
//    }

//    public static String getAppNoFlyZoneDBVersion() {
//        if (ChannelUtils.isDahua(GduAppEnv.application)
//                || ChannelUtils.isDahuaBDS(GduAppEnv.application)
//                || ChannelUtils.isNotNeedUpgradeNoFlyZone(GduAppEnv.application)) {
//            return "1.0.03";
//        }
//        String version = getString(GduAppEnv.application, MyConstants.MY_NO_FLY_ZONE_KEY);
//        if (version == null || version.isEmpty()) {
//            version = MyConstants.DEFAULT_CONTROLLER_NO_FLY_ZONE_DB_VERSION;
//        }
//        return version;
//    }
}















