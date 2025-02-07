package com.gdu.demo.flight.airlink;

import android.text.TextUtils;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.event.EventMessage;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.DataUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.eventbus.GlobalEventBus;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;

/**
 * 链路工具类
 */
public class AirlinkUtils {

    private static final String TAG = AirlinkUtils.class.getSimpleName();
    /**
     * 旧飞机SN长度  19
     */
    public static final int OLD_DRONE_SN_LEN = 19;
    /**
     * 新飞机SN长度 22
     */
    public static final int NEW_DRONE_SN_LEN = 22;

    /**
     * 获取飞机SN的ACK长度
     */
    public static final int GET_SN_ACK_LEN = 2;



    /**
     * 判断是否满足链路切换条件
     * 1.飞行器未连接，遥控器已连接；
     * @return
     */
    public static boolean isCanChangeAirlink(){
        return GlobalVariable.connStateEnum == ConnStateEnum.Conn_None
                && GlobalVariable.sRCConnState == ConnStateEnum.Conn_Sucess;
    }



    /**
     * 判断图传信号质量是否弱
     * 地面端G_RSSI1 或者 地面端G_RSSI2 或者 天空端A_RSSI1 或者 天空端A_RSSI2 小于 -105
     * 地面端SNR 或者 天空端SNR 小于 6
     * 判定为 信号弱
     * （更改5.8g信道后范围判断不同，因未使用直接注释/删除）
     * @return
     */
    /*
    public static boolean isAirlinkAsignalQualityWeak(){
        if (GlobalVariable.arlink_grdSignalQuality < -105 ||
                GlobalVariable.arlink_grdSignalQuality2 < -105
                || GlobalVariable.arlink_skySignalQuality < -105 ||
                GlobalVariable.arlink_skySignalQuality2 < -105 ||
                GlobalVariable.arlink_grdSNR < 6 || GlobalVariable.arlink_skySNR < 6) {
            return true;
        }
        if (GlobalVariable.arlink_grdSignalQuality == 0 && GlobalVariable.arlink_grdSignalQuality2 == 0) {
            return true;
        }
        return GlobalVariable.arlink_skySignalQuality == 0 && GlobalVariable.arlink_skySignalQuality2 == 0;
    }*/

    /**
     *  判断飞机4G信号强度差
     * @return
     */
    public static boolean is4GAsignalQualityWeak(){
        if (GlobalVariable.sFourthGStatus == null) {
            return true;
        } else {
            return GlobalVariable.sFourthGStatus._4g_signal_quality < -115;
        }
    }

    public static int get4gSignal() {
        if (GlobalVariable.sFourthGStatus == null) {
            return 0;
        }
        return GlobalVariable.sFourthGStatus._4g_signal_quality;

    }

    public static int get4gNetStatus() {
        if (GlobalVariable.sFourthGStatus == null) {
            return 0;
        }
        return GlobalVariable.sFourthGStatus._4g_net_status;

    }

    public static int getOperator() {
        if (GlobalVariable.sFourthGStatus == null) {
            return 0;
        }
        return GlobalVariable.sFourthGStatus.operator;

    }

    public static int getNetModelType() {
        if (GlobalVariable.sFourthGStatus == null) {
            return -1;
        }
        return GlobalVariable.sFourthGStatus.netModelType;

    }


    /**
     *  发给地面站
     */
    public static boolean isSendGroundSystem(byte[] data) {
        return ( data[6] == GduSocketConfig3.GROUND_STATION_SYSTEMS_ID);
//                && data[7] == GduSocketConfig3.GROUND_STATION_COMP_RELAY_ID
//                && (ByteUtilsLowBefore.byte2short(data, 8) == 0x0011
//                || (ByteUtilsLowBefore.byte2short(data, 8) == 0x0010));
    }


    /**
     *
     *  摇杆数据 4G下发送给mqtt
     */
    public static boolean isRcData(byte[] data) {
        return (data.length >= 9 && data[6] == GduSocketConfig3.ONBOARD_SYSTEMS_ID
                && data[7] == GduSocketConfig3.ONBOARD_COMP_RC_ID
                && ByteUtilsLowBefore.byte2short(data, 8) == 0x0004);
    }


    /**
     *  是否自动模式 和4G模式 需要发送给mqtt
     */
    public static boolean  isNeedSend4G() {
        return GlobalVariable.sFourthGStatus != null && (GlobalVariable.sFourthGStatus.airlink_type == 0x01
                || GlobalVariable.sFourthGStatus.airlink_type == 0x02);
    }

    public static int getCurrentPushStream() {
        if (GlobalVariable.sFourthGStatus != null) {
            return GlobalVariable.sFourthGStatus.currentPushStream;
        }
        return -1;
    }


    public static boolean aircraftIsConnect() {
        return GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus.mqtt_connect_status == 1;
    }



    public static String getSN() {
        String SN = "";
        if (TextUtils.isEmpty(GlobalVariable.SN)) {
            SN = SPUtils.getString(GduAppEnv.application, SPUtils.DRONE_SN, "");
        } else {
            SN = GlobalVariable.SN;
        }
        if ("000000000000000000".equals(SN)) {
            SN = "";
        }
        return SN;
    }

    public static void getUnique() {
//        MyLogUtils.i("getUnique()");
//        if (GduApplication.getSingleApp() == null || GduApplication.getSingleApp().gduCommunication == null) {
////            MyLogUtils.i("getUnique() judge is null");
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.getUnique((code, bean) -> {
////            MyLogUtils.i("getUnique callBack() code = " + code);
//            if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                GlobalEventBus.getBus().post(new EventMessage(MyConstants.GET_DEV_SN_FAIL));
//                return;
//            }
//            final String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("getUnique callBack() hexStr = " + hexStr);
//            AppLog.i(TAG, "getUnique callBack() hexStr = " + hexStr);
//            byte[] snByte;
//            int contentLen = bean.frameContent.length;
//            if (contentLen > OLD_DRONE_SN_LEN) {
//                snByte = new byte[NEW_DRONE_SN_LEN - GET_SN_ACK_LEN];
//            } else {
//                snByte = new byte[OLD_DRONE_SN_LEN - GET_SN_ACK_LEN];
//            }
//            if (contentLen - GET_SN_ACK_LEN >= 0)
//                System.arraycopy(bean.frameContent, GET_SN_ACK_LEN, snByte, 0, contentLen - GET_SN_ACK_LEN);
//            // 末尾空白补零处理(应对harmony系统空白变乱码问题)
//            for (int j = 0; j < snByte.length; j++) {
//                if (snByte[j] == 0 || snByte[j] == -1) {
//                    snByte[j] = (byte) 48;
//                }
//            }
//            String snStr = new String(snByte);
//            if (snStr.contains("\t") || snStr.contains("\b") || snStr.contains("\n")) {
//                snStr = "000000000000000000";
//            } else {
//                // 协议兼容 HFZN-华飞(大华)
//                if (!(snStr.contains("GDU") || snStr.contains("HFZN")) && snStr.length() == 20) {
//                    snStr = snStr.substring(0, 17);
//                }
//                GlobalVariable.SN = snStr;
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    GlobalVariable.EXPECTED_CONNECT_WIFI_SSID = snStr;
//                } else {
//                    GlobalVariable.EXPECTED_CONNECT_WIFI_SSID = "S400-" + snStr;
//                }
//                SPUtils.put(GduApplication.context, SPUtils.DRONE_SN, snStr);
//                RouteManager.Companion.getInstance().getDroneStatusManager().setDroneSn(snStr);
//            }
//            MyLogUtils.i("getUnique callBack() snStr = " + snStr);
//            GlobalEventBus.getBus().post(new EventMessage(MyConstants.GET_DEV_SN_SUC));
//        });
    }

}
