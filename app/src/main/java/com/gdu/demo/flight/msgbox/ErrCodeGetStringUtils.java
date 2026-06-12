package com.gdu.demo.flight.msgbox;

import com.gdu.demo.R;
import com.gdu.msdk.device.component.hms.DroneCameraErrCode;
import com.gdu.msdk.device.component.hms.DroneFlightControlErrCode;
import com.gdu.msdk.device.component.hms.DroneGimbalErrCode;
import com.gdu.msdk.device.component.hms.DroneMissionManagerErrCode;
import com.gdu.msdk.device.component.hms.DronePicTransmissionErrCode;
import com.gdu.msdk.device.component.hms.DronePowerErrCode;
import com.gdu.msdk.device.component.hms.DroneSystemStatusErrCode;
import com.gdu.msdk.device.component.hms.DroneVisionErrCode;
import com.gdu.msdk.device.component.hms.RemoteControlMCUErrCode;
import com.gdu.msdk.device.component.hms.RemoteControlSystemProxyErrCode;
import com.gdu.msdk.device.component.hms.nest.NestIntegratedControlErrCode;
import com.gdu.msdk.device.component.hms.nest.NestNewIntegratedControlErrCode;
import com.gdu.msdk.device.component.hms.nest.NestPeripheralMcuErrCode;
import com.gdu.msdk.device.component.hms.nest.NestSDRErrCode;
import com.gdu.msdk.device.component.hms.nest.NestSensorMcuErrCode;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;

/**
 * 获取错误码对应的提示字段
 * @author wixche
 */
public class ErrCodeGetStringUtils {
    private ErrCodeGetStringUtils() {
    }

    /**
     * @param devType 设备类型， 1：飞机  3：遥控器，4：机库
     * @param type 模块类型
     * @param errCode 错误码
     * */
    public static int getErrCodeStringResId(int devType, long type, long errCode) {
        if (devType == 4){
            return getNestStringResId(type, errCode);
        }if (devType == 2){
            return getNewNestStringResId(type, errCode);
        }else {
            return getErrCodeStringResId(type, errCode);
        }
    }

    public static int getErrCodeStringResId(long type, long errCode) {
        int resId = 0;
        if (type == DroneFlightControlErrCode.type){ //代号0x01
            return getFlightControlStringResId(errCode);
        }else if (type == DronePowerErrCode.type){  //代号0x02
            return getPowerStringResId(errCode);
        }else if (DroneVisionErrCode.type == type){ //代号0x03
            return getVisionStringResId(errCode);
        }else if (DroneGimbalErrCode.type == type){ //代号0x04
            return getGimbalStringResId(errCode);
        }else if (type == DroneCameraErrCode.type){ //代号0x05
            return getCameraStringResId(errCode);
        }else if (DronePicTransmissionErrCode.type == type){ //代号0x06
            return getSDRStringResId(errCode);
        }else if (type == DroneSystemStatusErrCode.type){ //代号0x07
            return getSystemStatusStringResId(errCode);
        }

        // 飞行器-任务管理器三级异常
        else if (DroneMissionManagerErrCode.ERROR_CODE_10801101 == errCode) {
            resId = R.string.Msg_ErrorCode_10801101;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801102 == errCode) {
            resId = R.string.Msg_ErrorCode_10801102;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801103 == errCode) {
            resId = R.string.Msg_ErrorCode_10801103;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801201 == errCode) {
            resId = R.string.Msg_ErrorCode_10801201;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801202 == errCode) {
            resId = R.string.Msg_ErrorCode_10801202;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801203 == errCode) {
            resId = R.string.Msg_ErrorCode_10801203;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801302 == errCode) {
            resId = R.string.Msg_ErrorCode_10801302;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801303 == errCode) {
            resId = R.string.Msg_ErrorCode_10801303;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801304 == errCode) {
            resId = R.string.Msg_ErrorCode_10801304;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801305 == errCode) {
            resId = R.string.Msg_ErrorCode_10801305;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801306 == errCode) {
            resId = R.string.Msg_ErrorCode_10801306;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801307 == errCode) {
            resId = R.string.Msg_ErrorCode_10801307;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801308 == errCode) {
            resId = R.string.Msg_ErrorCode_10801308;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801309 == errCode) {
            resId = R.string.Msg_ErrorCode_10801309;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080130A == errCode) {
            resId = R.string.Msg_ErrorCode_1080130A;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080130B == errCode) {
            resId = R.string.Msg_ErrorCode_1080130B;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080130C == errCode) {
            resId = R.string.Msg_ErrorCode_1080130C;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080130D == errCode) {
            resId = R.string.Msg_ErrorCode_1080130D;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801401 == errCode) {
            resId = R.string.Msg_ErrorCode_10801401;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801402 == errCode) {
            resId = R.string.Msg_ErrorCode_10801402;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10801403 == errCode) {
            resId = R.string.Msg_ErrorCode_10801403;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802104 == errCode) {
            resId = R.string.Msg_ErrorCode_10802104;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802105 == errCode) {
            resId = R.string.Msg_ErrorCode_10802105;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802106 == errCode) {
            resId = R.string.Msg_ErrorCode_10802106;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802107 == errCode) {
            resId = R.string.Msg_ErrorCode_10802107;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802108 == errCode) {
            resId = R.string.Msg_ErrorCode_10802108;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802109 == errCode) {
            resId = R.string.Msg_ErrorCode_10802109;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210a == errCode) {
            resId = R.string.Msg_ErrorCode_1080210a;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210b == errCode) {
            resId = R.string.Msg_ErrorCode_1080210b;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210c == errCode) {
            resId = R.string.Msg_ErrorCode_1080210c;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210d == errCode) {
            resId = R.string.Msg_ErrorCode_1080210d;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210e == errCode) {
            resId = R.string.Msg_ErrorCode_1080210e;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080210f == errCode) {
            resId = R.string.Msg_ErrorCode_1080210f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802111 == errCode) {
            resId = R.string.Msg_ErrorCode_10802111;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802112 == errCode) {
            resId = R.string.Msg_ErrorCode_10802112;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802115 == errCode) {
            resId = R.string.Msg_ErrorCode_10802115;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802116 == errCode) {
            resId = R.string.Msg_ErrorCode_10802116;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802117 == errCode) {
            resId = R.string.Msg_ErrorCode_10802117;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080211f == errCode) {
            resId = R.string.Msg_ErrorCode_1080211f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802120 == errCode) {
            resId = R.string.Msg_ErrorCode_10802120;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802121 == errCode) {
            resId = R.string.Msg_ErrorCode_10802121;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802124 == errCode) {
            resId = R.string.Msg_ErrorCode_10802124;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802125 == errCode) {
            resId = R.string.Msg_ErrorCode_10802125;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802126 == errCode) {
            resId = R.string.Msg_ErrorCode_10802126;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802127 == errCode) {
            resId = R.string.Msg_ErrorCode_10802127;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802128 == errCode) {
            resId = R.string.Msg_ErrorCode_10802128;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802129 == errCode) {
            resId = R.string.Msg_ErrorCode_10802129;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212a == errCode) {
            resId = R.string.Msg_ErrorCode_1080212a;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212b == errCode) {
            resId = R.string.Msg_ErrorCode_1080212b;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212c == errCode) {
            resId = R.string.Msg_ErrorCode_1080212c;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212d == errCode) {
            resId = R.string.Msg_ErrorCode_1080212d;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212e == errCode) {
            resId = R.string.Msg_ErrorCode_1080212e;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080212f == errCode) {
            resId = R.string.Msg_ErrorCode_1080212f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802130 == errCode) {
            resId = R.string.Msg_ErrorCode_10802130;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802132 == errCode) {
            resId = R.string.Msg_ErrorCode_10802132;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802133 == errCode) {
            resId = R.string.Msg_ErrorCode_10802133;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802134 == errCode) {
            resId = R.string.Msg_ErrorCode_10802134;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802135 == errCode) {
            resId = R.string.Msg_ErrorCode_10802135;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802137 == errCode) {
            resId = R.string.Msg_ErrorCode_10802137;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802138 == errCode) {
            resId = R.string.Msg_ErrorCode_10802138;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802139 == errCode) {
            resId = R.string.Msg_ErrorCode_10802139;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080213a == errCode) {
            resId = R.string.Msg_ErrorCode_1080213a;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080213c == errCode) {
            resId = R.string.Msg_ErrorCode_1080213c;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080213d == errCode) {
            resId = R.string.Msg_ErrorCode_1080213d;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080213e == errCode) {
            resId = R.string.Msg_ErrorCode_1080213e;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080213f == errCode) {
            resId = R.string.Msg_ErrorCode_1080213f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802145 == errCode) {
            resId = R.string.Msg_ErrorCode_10802145;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080214f == errCode) {
            resId = R.string.Msg_ErrorCode_1080214f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802150 == errCode) {
            resId = R.string.Msg_ErrorCode_10802150;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10802151 == errCode) {
            resId = R.string.Msg_ErrorCode_10802151;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803202 == errCode) {
            resId = R.string.Msg_ErrorCode_10803202;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803203 == errCode) {
            resId = R.string.Msg_ErrorCode_10803203;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803204 == errCode) {
            resId = R.string.Msg_ErrorCode_10803204;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803205 == errCode) {
            resId = R.string.Msg_ErrorCode_10803205;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803601 == errCode) {
            resId = R.string.Msg_ErrorCode_10803601;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803602 == errCode) {
            resId = R.string.Msg_ErrorCode_10803602;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803603 == errCode) {
            resId = R.string.Msg_ErrorCode_10803603;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803604 == errCode) {
            resId = R.string.Msg_ErrorCode_10803604;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803605 == errCode) {
            resId = R.string.Msg_ErrorCode_10803605;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803606 == errCode) {
            resId = R.string.Msg_ErrorCode_10803606;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803650 == errCode) {
            resId = R.string.Msg_ErrorCode_10803650;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803651 == errCode) {
            resId = R.string.Msg_ErrorCode_10803651;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803652 == errCode) {
            resId = R.string.Msg_ErrorCode_10803652;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803653 == errCode) {
            resId = R.string.Msg_ErrorCode_10803653;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803654 == errCode) {
            resId = R.string.Msg_ErrorCode_10803654;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803656 == errCode) {
            resId = R.string.Msg_ErrorCode_10803656;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803657 == errCode) {
            resId = R.string.Msg_ErrorCode_10803657;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803658 == errCode) {
            resId = R.string.Msg_ErrorCode_10803658;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803659 == errCode) {
            resId = R.string.Msg_ErrorCode_10803659;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080366e == errCode) {
            resId = R.string.Msg_ErrorCode_1080366e;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_1080366f == errCode) {
            resId = R.string.Msg_ErrorCode_1080366f;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803670 == errCode) {
            resId = R.string.Msg_ErrorCode_10803670;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803703 == errCode) {
            resId = R.string.Msg_ErrorCode_10803703;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803704 == errCode) {
            resId = R.string.Msg_ErrorCode_10803704;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803705 == errCode) {
            resId = R.string.Msg_ErrorCode_10803705;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803706 == errCode) {
            resId = R.string.Msg_ErrorCode_10803706;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803707 == errCode) {
            resId = R.string.Msg_ErrorCode_10803707;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803708 == errCode) {
            resId = R.string.Msg_ErrorCode_10803708;
        } else if (DroneMissionManagerErrCode.ERROR_CODE_10803709 == errCode) {
            resId = R.string.Msg_ErrorCode_10803709;
        }
        // 遥控器-系统代理一级异常
        else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102001 == errCode) {
            resId = R.string.Msg_ErrorCode_30102001;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102002 == errCode) {
            resId = R.string.Msg_ErrorCode_30102002;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102003 == errCode) {
            resId = R.string.Msg_ErrorCode_30102003;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102004 == errCode) {
            resId = R.string.Msg_ErrorCode_30102004;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102005 == errCode) {
            resId = R.string.Msg_ErrorCode_30102005;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102006 == errCode) {
            resId = R.string.Msg_ErrorCode_30102006;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102007 == errCode) {
            resId = R.string.Msg_ErrorCode_30102007;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102008 == errCode) {
            resId = R.string.Msg_ErrorCode_30102008;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102009 == errCode) {
            resId = R.string.Msg_ErrorCode_30102009;
        } else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102010 == errCode) {
            resId = R.string.Msg_ErrorCode_30102010;
        }
        else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30102013 == errCode) {
            resId = R.string.Msg_ErrorCode_30102013;
        }

        else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103001 == errCode) {
            resId = R.string.aircraft_not_connect;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103002 == errCode) {
            resId = R.string.ble_disconnect;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103003 == errCode) {
            resId = R.string.Msg_ErrorCode_30103003;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103004 == errCode) {
            resId = R.string.Msg_ErrorCode_30103004;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103005 == errCode) {
            resId = R.string.Msg_ErrorCode_30103005;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30103006 == errCode) {
            resId = R.string.Msg_ErrorCode_30103006;
        }else if (RemoteControlSystemProxyErrCode.ERROR_CODE_30109001 == errCode) {
            resId = R.string.Msg_ErrorCode_30109001;
        }else if (RemoteControlMCUErrCode.ERROR_CODE_30304001 == errCode) {
            resId = R.string.Msg_ErrorCode_30304001;
        }
        return resId;
    }

    /**
     * 获取飞控异常错误码提示文案
     * */
    private static int getFlightControlStringResId(long errCode){
        int resId = 0;
        //// 飞行器-飞控一级异常，共49项
        if (DroneFlightControlErrCode.ERROR_CODE_10101001 == errCode) {
            resId = R.string.Msg_ErrorCode_10101001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10101011 == errCode) {
            resId = R.string.Msg_ErrorCode_10101011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10101002 == errCode) {
            resId = R.string.Msg_ErrorCode_10101002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10101012 == errCode) {
            resId = R.string.Msg_ErrorCode_10101012;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10102001 == errCode) {
            resId = R.string.Msg_ErrorCode_10102001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10102011 == errCode) {
            resId = R.string.Msg_ErrorCode_10102011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10102002 == errCode) {
            resId = R.string.Msg_ErrorCode_10102002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10102012 == errCode) {
            resId = R.string.Msg_ErrorCode_10102012;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103001 == errCode) {
            resId = R.string.Msg_ErrorCode_10103001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103011 == errCode) {
            resId = R.string.Msg_ErrorCode_10103011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103002 == errCode) {
            resId = R.string.Msg_ErrorCode_10103002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103012 == errCode) {
            resId = R.string.Msg_ErrorCode_10103012;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104001 == errCode) {
            resId = R.string.Msg_ErrorCode_10104001;
        }else if (DroneFlightControlErrCode.ERROR_CODE_10104011 == errCode) {
            resId = R.string.Msg_ErrorCode_10104011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104006 == errCode) {
            resId = R.string.Msg_ErrorCode_10104006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104007 == errCode) {
            resId = R.string.Msg_ErrorCode_10104007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104008 == errCode) {
            resId = R.string.Msg_ErrorCode_10104008;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104018 == errCode) {
            resId = R.string.Msg_ErrorCode_10104018;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104009 == errCode) {
            resId = R.string.Msg_ErrorCode_10104009;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10106001 == errCode) {
            resId = R.string.Msg_ErrorCode_10106001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10106002 == errCode) {
            resId = R.string.Msg_ErrorCode_10106002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10107001 == errCode) {
            resId = R.string.Msg_ErrorCode_10107001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10107011 == errCode) {
            resId = R.string.Msg_ErrorCode_10107011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108001 == errCode) {
            if (IGduDroneDevice.get().getFlightController().getFcInfo1().getValue().getDroneFlyState().isGround()) {
                resId = R.string.Msg_ErrorCode_10108001_1;
            } else {
                resId = R.string.Msg_ErrorCode_10108001_2;
            }
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108003 == errCode) {
            resId = R.string.Msg_ErrorCode_10108003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108013 == errCode) {
            resId = R.string.Msg_ErrorCode_10108013;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108017 == errCode) {
            resId = R.string.Msg_ErrorCode_10108017;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108009 == errCode) {
            resId = R.string.Msg_ErrorCode_10108009;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108019 == errCode) {
            resId = R.string.Msg_ErrorCode_10108019;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010800a == errCode) {
            resId = R.string.Msg_ErrorCode_1010800a;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109001 == errCode) {
            resId = R.string.Msg_ErrorCode_10109001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109011 == errCode) {
            resId = R.string.Msg_ErrorCode_10109011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109002 == errCode) {
            resId = R.string.Msg_ErrorCode_10109002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109012 == errCode) {
            resId = R.string.Msg_ErrorCode_10109012;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109006 == errCode) {
            resId = R.string.Msg_ErrorCode_10109006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109007 == errCode) {
            resId = R.string.Msg_ErrorCode_10109007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b004 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b005 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b006 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b007 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b008 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b008;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b009 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b009;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b00a == errCode) {
            resId = R.string.Msg_ErrorCode_1010b00a;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b00b == errCode) {
            resId = R.string.Msg_ErrorCode_1010b00b;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b00c == errCode) {
            resId = R.string.Msg_ErrorCode_1010b00c;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b00d == errCode) {
            resId = R.string.Msg_ErrorCode_1010b00d;
        }  else if (DroneFlightControlErrCode.ERROR_CODE_1010b00e == errCode) {
            resId = R.string.Msg_ErrorCode_1010b00e;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A011 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A002 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A012 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a012;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A003 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A013 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a013;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A004 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A014 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a014;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A005 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A015 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a015;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A006 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A016 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a016;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A007 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010A017 == errCode) {
            resId = R.string.Msg_ErrorCode_1010a017;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010c001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010c001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010c005 == errCode) {
            resId = R.string.Msg_ErrorCode_1010c005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010c006 == errCode) {
            resId = R.string.Msg_ErrorCode_1010c006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d005 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d006 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d009 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d009;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d010 == errCode) {
            resId = R.string.Msg_ErrorCode_1010D010;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d011 == errCode) {
            resId = R.string.Msg_ErrorCode_1010D011;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d015 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d015;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d016 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d016;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d00c == errCode) {
            resId = R.string.Msg_ErrorCode_1010d00c;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010e006 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110004 == errCode) {
            resId = R.string.Msg_ErrorCode_10110004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111005 == errCode) {
            resId = R.string.Msg_ErrorCode_10111005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112005 == errCode) {
            resId = R.string.Msg_ErrorCode_10112005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112006 == errCode) {
            resId = R.string.Msg_ErrorCode_10112006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112007 == errCode) {
            resId = R.string.Msg_ErrorCode_10112007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112008 == errCode) {
            resId = R.string.Msg_ErrorCode_10112008;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1011200c == errCode) {
            resId = R.string.Msg_ErrorCode_1011200c;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10114005 == errCode) {
            resId = R.string.Msg_ErrorCode_10114005;
        }
        // 飞行器-飞控二级异常
        else if (DroneFlightControlErrCode.ERROR_CODE_10101005 == errCode) {
            resId = R.string.Msg_ErrorCode_10101005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10101006 == errCode) {
            resId = R.string.Msg_ErrorCode_10101006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103003 == errCode) {
            resId = R.string.Msg_ErrorCode_10103003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103013 == errCode) {
            resId = R.string.Msg_ErrorCode_10103013;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10103004 == errCode) {
            resId = R.string.Msg_ErrorCode_10103004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104003 == errCode) {
            resId = R.string.Msg_ErrorCode_10104003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104013 == errCode) {
            resId = R.string.Msg_ErrorCode_10104013;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104004 == errCode) {
            resId = R.string.Msg_ErrorCode_10104004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104005 == errCode) {
            resId = R.string.Msg_ErrorCode_10104005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10104015 == errCode) {
            resId = R.string.Msg_ErrorCode_10104015;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10105001 == errCode) {
            resId = R.string.Msg_ErrorCode_10105001;
        }else if (DroneFlightControlErrCode.ERROR_CODE_10108002 == errCode) {
            if (IGduDroneDevice.get().getFlightController().getFcInfo1().getValue().getDroneFlyState().isGround()) {
                resId = R.string.Msg_ErrorCode_10108002_1;
            } else {
                resId = R.string.Msg_ErrorCode_10108002_2;
            }
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108004 == errCode) {
            resId = R.string.Msg_ErrorCode_10108004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108005 == errCode) {
            resId = R.string.Msg_ErrorCode_10108005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108006 == errCode) {
            resId = R.string.Msg_ErrorCode_10108006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108016 == errCode) {
            resId = R.string.Msg_ErrorCode_10108016;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10108007 == errCode) {
            resId = R.string.Msg_ErrorCode_10108007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10109003 == errCode) {
            resId = R.string.Msg_ErrorCode_10109003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b002 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010b003 == errCode) {
            resId = R.string.Msg_ErrorCode_1010b003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010c003 == errCode) {
            resId = R.string.Msg_ErrorCode_1010c003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010c004 == errCode) {
            resId = R.string.Msg_ErrorCode_1010c004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010d002 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d002;
        }  else if (DroneFlightControlErrCode.ERROR_CODE_1010e001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010e002 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010e003 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010e004 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010e005 == errCode) {
            resId = R.string.Msg_ErrorCode_1010e005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010f001 == errCode) {
            resId = R.string.Msg_ErrorCode_1010f001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010f002 == errCode) {
            resId = R.string.Msg_ErrorCode_1010f002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010f003 == errCode) {
            resId = R.string.Msg_ErrorCode_1010f003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1010f004 == errCode) {
            resId = R.string.Msg_ErrorCode_1010f004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110001 == errCode) {
            resId = R.string.Msg_ErrorCode_10110001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110002 == errCode) {
            resId = R.string.Msg_ErrorCode_10110002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110003 == errCode) {
            resId = R.string.Msg_ErrorCode_10110003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110005 == errCode) {
            resId = R.string.Msg_ErrorCode_10110005;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110006 == errCode) {
            resId = R.string.Msg_ErrorCode_10110006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10110007 == errCode) {
            resId = R.string.Msg_ErrorCode_10110007;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111001 == errCode) {
            resId = R.string.Msg_ErrorCode_10111001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111002 == errCode) {
            resId = R.string.Msg_ErrorCode_10111002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111003 == errCode) {
            resId = R.string.Msg_ErrorCode_10111003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111004 == errCode) {
            resId = R.string.Msg_ErrorCode_10111004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111006 == errCode) {
            resId = R.string.Msg_ErrorCode_10111006;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10111007 == errCode) {
            resId = R.string.Msg_ErrorCode_10111007;
        }else if (DroneFlightControlErrCode.ERROR_CODE_10112001 == errCode) {
            resId = R.string.Msg_ErrorCode_10112001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112002 == errCode) {
            resId = R.string.Msg_ErrorCode_10112002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112003 == errCode) {
            resId = R.string.Msg_ErrorCode_10112003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112004 == errCode) {
            resId = R.string.Msg_ErrorCode_10112004;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10112009 == errCode) {
            resId = R.string.Msg_ErrorCode_10112009;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1011200a == errCode) {
            resId = R.string.Msg_ErrorCode_1011200a;
        } else if (DroneFlightControlErrCode.ERROR_CODE_1011200b == errCode) {
            resId = R.string.Msg_ErrorCode_1011200b;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10114001 == errCode) {
            resId = R.string.Msg_ErrorCode_10114001;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10114002 == errCode) {
            resId = R.string.Msg_ErrorCode_10114002;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10114003 == errCode) {
            resId = R.string.Msg_ErrorCode_10114003;
        } else if (DroneFlightControlErrCode.ERROR_CODE_10114006 == errCode) {
            resId = R.string.Msg_ErrorCode_10114006;
        }
        // 飞行器-飞控三级异常
        else if (DroneFlightControlErrCode.ERROR_CODE_1010d008 == errCode) {
            resId = R.string.Msg_ErrorCode_1010d008;
        }
        return resId;
    }

    /**
     * 获取动力异常错误码提示文案
     * */
    private static int getPowerStringResId(long errCode) {
        int resId = 0;
        // 飞行器-动力系统一级异常
        if (DronePowerErrCode.ERROR_CODE_10201004 == errCode) {
            resId = R.string.Msg_ErrorCode_10201004;
        } else if (DronePowerErrCode.ERROR_CODE_10201005 == errCode) {
            resId = R.string.Msg_ErrorCode_10201005;
        } else if (DronePowerErrCode.ERROR_CODE_10201105 == errCode) {
            resId = R.string.Msg_ErrorCode_10201105;
        } else if (DronePowerErrCode.ERROR_CODE_10201007 == errCode) {
            resId = R.string.Msg_ErrorCode_10201007;
        } else if (DronePowerErrCode.ERROR_CODE_10201107 == errCode) {
            resId = R.string.Msg_ErrorCode_10201107;
        } else if (DronePowerErrCode.ERROR_CODE_10201008 == errCode) {
            resId = R.string.Msg_ErrorCode_10201008;
        } else if (DronePowerErrCode.ERROR_CODE_10201108 == errCode) {
            resId = R.string.Msg_ErrorCode_10201108;
        } else if (DronePowerErrCode.ERROR_CODE_10201009 == errCode) {
            resId = R.string.Msg_ErrorCode_10201009;
        } else if (DronePowerErrCode.ERROR_CODE_10201010 == errCode) {
            resId = R.string.Msg_ErrorCode_10201010;
        } else if (DronePowerErrCode.ERROR_CODE_10201011 == errCode) {
            resId = R.string.Msg_ErrorCode_10201011;
        } else if (DronePowerErrCode.ERROR_CODE_10202006 == errCode) {
            resId = R.string.Msg_ErrorCode_10202006;
        } else if (DronePowerErrCode.ERROR_CODE_10202007 == errCode) {
            resId = R.string.Msg_ErrorCode_10202007;
        } else if (DronePowerErrCode.ERROR_CODE_10202008 == errCode) {
            resId = R.string.Msg_ErrorCode_10202008;
        } else if (DronePowerErrCode.ERROR_CODE_10202009 == errCode) {
            resId = R.string.Msg_ErrorCode_10202009;
        } else if (DronePowerErrCode.ERROR_CODE_10202015 == errCode) {
            resId = R.string.Msg_ErrorCode_10202015;
        } else if (DronePowerErrCode.ERROR_CODE_10202115 == errCode) {
            resId = R.string.Msg_ErrorCode_10202115;
        } else if (DronePowerErrCode.ERROR_CODE_10202016 == errCode) {
            resId = R.string.Msg_ErrorCode_10202016;
        } else if (DronePowerErrCode.ERROR_CODE_10202116 == errCode) {
            resId = R.string.Msg_ErrorCode_10202116;
        } else if (DronePowerErrCode.ERROR_CODE_10202017 == errCode) {
            resId = R.string.Msg_ErrorCode_10202017;
        } else if (DronePowerErrCode.ERROR_CODE_10202117 == errCode) {
            resId = R.string.Msg_ErrorCode_10202117;
        } else if (DronePowerErrCode.ERROR_CODE_10202018 == errCode) {
            resId = R.string.Msg_ErrorCode_10202018;
        } else if (DronePowerErrCode.ERROR_CODE_10202118 == errCode) {
            resId = R.string.Msg_ErrorCode_10202118;
        } else if (DronePowerErrCode.ERROR_CODE_10202019 == errCode) {
            resId = R.string.Msg_ErrorCode_10202019;
        } else if (DronePowerErrCode.ERROR_CODE_10202020 == errCode) {
            resId = R.string.Msg_ErrorCode_10202020;
        } else if (DronePowerErrCode.ERROR_CODE_10202120 == errCode) {
            resId = R.string.Msg_ErrorCode_10202120;
        } else if (DronePowerErrCode.ERROR_CODE_10202021 == errCode) {
            resId = R.string.Msg_ErrorCode_10202021;
        } else if (DronePowerErrCode.ERROR_CODE_10202022 == errCode) {
            resId = R.string.Msg_ErrorCode_10202022;
        } else if (DronePowerErrCode.ERROR_CODE_10202122 == errCode) {
            resId = R.string.Msg_ErrorCode_10202122;
        } else if (DronePowerErrCode.ERROR_CODE_10202023 == errCode) {
            resId = R.string.Msg_ErrorCode_10202023;
        } else if (DronePowerErrCode.ERROR_CODE_10202024 == errCode) {
            resId = R.string.Msg_ErrorCode_10202024;
        } else if (DronePowerErrCode.ERROR_CODE_10202025 == errCode) {
            resId = R.string.Msg_ErrorCode_10202025;
        } else if (DronePowerErrCode.ERROR_CODE_10202027 == errCode) {
            resId = R.string.Msg_ErrorCode_10202027;
        }
        // 飞行器-动力系统二级异常
        else if (DronePowerErrCode.ERROR_CODE_10202026 == errCode) {
            resId = R.string.Msg_ErrorCode_10202026;
        }
        return resId;
    }

    /**
     * 获取相机异常错误码提示文案
     * */
    private static int getVisionStringResId(long errCode) {
        int resId = 0;
        // 飞行器-视觉一级异常
        if (DroneVisionErrCode.ERROR_CODE_10301001 == errCode) {
            resId = R.string.Msg_ErrorCode_10301001;
        } else if (DroneVisionErrCode.ERROR_CODE_10301002 == errCode) {
            resId = R.string.Msg_ErrorCode_10301002;
        } else if (DroneVisionErrCode.ERROR_CODE_10301003 == errCode) {
            resId = R.string.Msg_ErrorCode_10301003;
        } else if (DroneVisionErrCode.ERROR_CODE_10301004 == errCode) {
            resId = R.string.Msg_ErrorCode_10301004;
        } else if (DroneVisionErrCode.ERROR_CODE_10301005 == errCode) {
            resId = R.string.Msg_ErrorCode_10301005;
        } else if (DroneVisionErrCode.ERROR_CODE_10301006 == errCode) {
            resId = R.string.Msg_ErrorCode_10301006;
        } else if (DroneVisionErrCode.ERROR_CODE_10301007 == errCode) {
            resId = R.string.Msg_ErrorCode_10301007;
        } else if (DroneVisionErrCode.ERROR_CODE_10301008 == errCode) {
            resId = R.string.Msg_ErrorCode_10301008;
        } else if (DroneVisionErrCode.ERROR_CODE_10301009 == errCode) {
            resId = R.string.Msg_ErrorCode_10301009;
        } else if (DroneVisionErrCode.ERROR_CODE_1030100A == errCode) {
            resId = R.string.Msg_ErrorCode_1030100A;
        } else if (DroneVisionErrCode.ERROR_CODE_1030100B == errCode) {
            resId = R.string.Msg_ErrorCode_1030100B;
        } else if (DroneVisionErrCode.ERROR_CODE_1030100C == errCode) {
            resId = R.string.Msg_ErrorCode_1030100C;
        } else if (DroneVisionErrCode.ERROR_CODE_1030100D == errCode) {
            resId = R.string.Msg_ErrorCode_1030100D;
        } else if (DroneVisionErrCode.ERROR_CODE_1030100E == errCode) {
            resId = R.string.Msg_ErrorCode_1030100E;
        } else if (DroneVisionErrCode.ERROR_CODE_10301010 == errCode) {
            resId = R.string.Msg_ErrorCode_10301010;
        } else if (DroneVisionErrCode.ERROR_CODE_10301011 == errCode) {
            resId = R.string.Msg_ErrorCode_10301011;
        } else if (DroneVisionErrCode.ERROR_CODE_10301012 == errCode) {
            resId = R.string.Msg_ErrorCode_10301012;
        } else if (DroneVisionErrCode.ERROR_CODE_10301013 == errCode) {
            resId = R.string.Msg_ErrorCode_10301013;
        } else if (DroneVisionErrCode.ERROR_CODE_10302001 == errCode) {
            resId = R.string.Msg_ErrorCode_10302001;
        } else if (DroneVisionErrCode.ERROR_CODE_10302003 == errCode) {
            resId = R.string.Msg_ErrorCode_10302003;
        } else if (DroneVisionErrCode.ERROR_CODE_10302004 == errCode) {
            resId = R.string.Msg_ErrorCode_10302004;
        } else if (DroneVisionErrCode.ERROR_CODE_10302005 == errCode) {
            resId = R.string.Msg_ErrorCode_10302005;
        } else if (DroneVisionErrCode.ERROR_CODE_10302006 == errCode) {
            resId = R.string.Msg_ErrorCode_10302006;
        } else if (DroneVisionErrCode.ERROR_CODE_10302007 == errCode) {
            resId = R.string.Msg_ErrorCode_10302007;
        } else if (DroneVisionErrCode.ERROR_CODE_10303001 == errCode) {
            resId = R.string.Msg_ErrorCode_10303001;
        } else if (DroneVisionErrCode.ERROR_CODE_10304001 == errCode) {
            resId = R.string.Msg_ErrorCode_10304001;
        } else if (DroneVisionErrCode.ERROR_CODE_10304002 == errCode) {
            resId = R.string.Msg_ErrorCode_10304002;
        } else if (DroneVisionErrCode.ERROR_CODE_10304003 == errCode) {
            resId = R.string.Msg_ErrorCode_10304003;
        } else if (DroneVisionErrCode.ERROR_CODE_10304004 == errCode) {
            resId = R.string.Msg_ErrorCode_10304004;
        } else if (DroneVisionErrCode.ERROR_CODE_10304005 == errCode) {
            resId = R.string.Msg_ErrorCode_10304005;
        } else if (DroneVisionErrCode.ERROR_CODE_10304007 == errCode) {
            resId = R.string.Msg_ErrorCode_10304007;
        } else if (DroneVisionErrCode.ERROR_CODE_10304008 == errCode) {
            resId = R.string.Msg_ErrorCode_10304008;
        } else if (DroneVisionErrCode.ERROR_CODE_10304009 == errCode) {
            resId = R.string.Msg_ErrorCode_10304009;
        } else if (DroneVisionErrCode.ERROR_CODE_1030400A == errCode) {
            resId = R.string.Msg_ErrorCode_1030400A;
        }
        // 飞行器-视觉二级异常
        else if (DroneVisionErrCode.ERROR_CODE_10301020 == errCode) {
            resId = R.string.Msg_ErrorCode_10301020;
        } else if (DroneVisionErrCode.ERROR_CODE_10301021 == errCode) {
            resId = R.string.Msg_ErrorCode_10301021;
        } else if (DroneVisionErrCode.ERROR_CODE_10301022 == errCode) {
            resId = R.string.Msg_ErrorCode_10301022;
        } else if (DroneVisionErrCode.ERROR_CODE_10301023 == errCode) {
            resId = R.string.Msg_ErrorCode_10301023;
        } else if (DroneVisionErrCode.ERROR_CODE_10301024 == errCode) {
            resId = R.string.Msg_ErrorCode_10301024;
        } else if (DroneVisionErrCode.ERROR_CODE_10301025 == errCode) {
            resId = R.string.Msg_ErrorCode_10301025;
        } else if (DroneVisionErrCode.ERROR_CODE_10301026 == errCode) {
            resId = R.string.Msg_ErrorCode_10301026;
        } else if (DroneVisionErrCode.ERROR_CODE_10301027 == errCode) {
            resId = R.string.Msg_ErrorCode_10301027;
        } else if (DroneVisionErrCode.ERROR_CODE_10301030 == errCode) {
            resId = R.string.Msg_ErrorCode_10301030;
        } else if (DroneVisionErrCode.ERROR_CODE_10301031 == errCode) {
            resId = R.string.Msg_ErrorCode_10301031;
        } else if (DroneVisionErrCode.ERROR_CODE_10301032 == errCode) {
            resId = R.string.Msg_ErrorCode_10301032;
        } else if (DroneVisionErrCode.ERROR_CODE_10301033 == errCode) {
            resId = R.string.Msg_ErrorCode_10301033;
        } else if (DroneVisionErrCode.ERROR_CODE_10301034 == errCode) {
            resId = R.string.Msg_ErrorCode_10301034;
        } else if (DroneVisionErrCode.ERROR_CODE_10301035 == errCode) {
            resId = R.string.Msg_ErrorCode_10301035;
        } else if (DroneVisionErrCode.ERROR_CODE_10302008 == errCode) {
            resId = R.string.Msg_ErrorCode_10302008;
        } else if (DroneVisionErrCode.ERROR_CODE_10304006 == errCode) {
            resId = R.string.Msg_ErrorCode_10304006;
        } else if (DroneVisionErrCode.ERROR_CODE_1030400F == errCode) {
            resId = R.string.Msg_ErrorCode_1030400F;
        }else if (DroneVisionErrCode.ERROR_CODE_10305001 == errCode) {
            resId = R.string.Msg_ErrorCode_10305001;
        }else if (DroneVisionErrCode.ERROR_CODE_10305002 == errCode) {
            resId = R.string.Msg_ErrorCode_10305002;
        }else if (DroneVisionErrCode.ERROR_CODE_10305003 == errCode) {
            resId = R.string.Msg_ErrorCode_10305003;
        }else if (DroneVisionErrCode.ERROR_CODE_10305004 == errCode) {
            resId = R.string.Msg_ErrorCode_10305004;
        }
        // 飞行器-视觉三级异常
        else if (DroneVisionErrCode.ERROR_CODE_10301014 == errCode) {
            resId = R.string.Msg_ErrorCode_10301014;
        } else if (DroneVisionErrCode.ERROR_CODE_10301015 == errCode) {
            resId = R.string.Msg_ErrorCode_10301015;
        } else if (DroneVisionErrCode.ERROR_CODE_10301016 == errCode) {
            resId = R.string.Msg_ErrorCode_10301016;
        } else if (DroneVisionErrCode.ERROR_CODE_10301017 == errCode) {
            resId = R.string.Msg_ErrorCode_10301017;
        } else if (DroneVisionErrCode.ERROR_CODE_10301018 == errCode) {
            resId = R.string.Msg_ErrorCode_10301018;
        } else if (DroneVisionErrCode.ERROR_CODE_10301019 == errCode) {
            resId = R.string.Msg_ErrorCode_10301019;
        } else if (DroneVisionErrCode.ERROR_CODE_10301028 == errCode) {
            resId = R.string.Msg_ErrorCode_10301028;
        } else if (DroneVisionErrCode.ERROR_CODE_10301029 == errCode) {
            resId = R.string.Msg_ErrorCode_10301029;
        }  else if (DroneVisionErrCode.ERROR_CODE_1030400B == errCode) {
            resId = R.string.Msg_ErrorCode_1030400B;
        } else if (DroneVisionErrCode.ERROR_CODE_1030400C == errCode) {
            resId = R.string.Msg_ErrorCode_1030400C;
        } else if (DroneVisionErrCode.ERROR_CODE_1030400D == errCode) {
            resId = R.string.Msg_ErrorCode_1030400D;
        } else if (DroneVisionErrCode.ERROR_CODE_1030400E == errCode) {
            resId = R.string.Msg_ErrorCode_1030400E;
        }
        return resId;
    }

    /**
     * 获取相机异常错误码提示文案
     * */
    private static int getGimbalStringResId(long errCode) {
        int resId = 0;
        // 飞行器-云台一级异常
        if (DroneGimbalErrCode.ERROR_CODE_10401002 == errCode) {
            resId = R.string.Msg_ErrorCode_10401002;
        } else if (DroneGimbalErrCode.ERROR_CODE_10401005 == errCode) {
            resId = R.string.Msg_ErrorCode_10401005;
        }
        // 飞行器-云台二级异常
        else if (DroneGimbalErrCode.ERROR_CODE_10401001 == errCode) {
            resId = R.string.Msg_ErrorCode_10401001;
        }
        // 飞行器-云台三级异常
        else if (DroneGimbalErrCode.ERROR_CODE_10401007 == errCode) {
            resId = R.string.Msg_ErrorCode_10401007;
        }
        return resId;
    }

    /**
     * 获取相机异常错误码提示文案
     * */
    private static int getCameraStringResId(long errCode) {
        int resId = 0;
        // 飞行器-相机一级异常
        if (DroneCameraErrCode.ERROR_CODE_10501001 == errCode) {
            resId = R.string.Msg_ErrorCode_10501001;
        } else if (DroneCameraErrCode.ERROR_CODE_10501002 == errCode) {
            resId = R.string.Msg_ErrorCode_10501002;
        } else if (DroneCameraErrCode.ERROR_CODE_10501011 == errCode) {
            resId = R.string.Msg_ErrorCode_10501011;
        } else if (DroneCameraErrCode.ERROR_CODE_10501012 == errCode) {
            resId = R.string.Msg_ErrorCode_10501012;
        } else if (DroneCameraErrCode.ERROR_CODE_10501021 == errCode) {
            resId = R.string.Msg_ErrorCode_10501021;
        } else if (DroneCameraErrCode.ERROR_CODE_10501022 == errCode) {
            resId = R.string.Msg_ErrorCode_10501022;
        } else if (DroneCameraErrCode.ERROR_CODE_10502001 == errCode) {
            resId = R.string.Msg_ErrorCode_10502001;
        } else if (DroneCameraErrCode.ERROR_CODE_10502002 == errCode) {
            resId = R.string.Msg_ErrorCode_10502002;
        } else if (DroneCameraErrCode.ERROR_CODE_10502003 == errCode) {
            resId = R.string.Msg_ErrorCode_10502003;
        } else if (DroneCameraErrCode.ERROR_CODE_10506001 == errCode) {
            resId = R.string.Msg_ErrorCode_10506001;
        } else if (DroneCameraErrCode.ERROR_CODE_10506011 == errCode) {
            resId = R.string.Msg_ErrorCode_10506011;
        } else if (DroneCameraErrCode.ERROR_CODE_10506021 == errCode) {
            resId = R.string.Msg_ErrorCode_10506021;
        } else if (DroneCameraErrCode.ERROR_CODE_10507001 == errCode) {
            resId = R.string.Msg_ErrorCode_10507001;
        } else if (DroneCameraErrCode.ERROR_CODE_10507002 == errCode) {
            resId = R.string.Msg_ErrorCode_10507002;
        } else if (DroneCameraErrCode.ERROR_CODE_10508001 == errCode) {
            resId = R.string.Msg_ErrorCode_10508001;
        } else if (DroneCameraErrCode.ERROR_CODE_10508002 == errCode) {
            resId = R.string.Msg_ErrorCode_10508002;
        } else if (DroneCameraErrCode.ERROR_CODE_10509001 == errCode) {
            resId = R.string.Msg_ErrorCode_10509001;
        } else if (DroneCameraErrCode.ERROR_CODE_10509002 == errCode) {
            resId = R.string.Msg_ErrorCode_10509002;
        } else if (DroneCameraErrCode.ERROR_CODE_10509011 == errCode) {
            resId = R.string.Msg_ErrorCode_10509011;
        } else if (DroneCameraErrCode.ERROR_CODE_10509012 == errCode) {
            resId = R.string.Msg_ErrorCode_10509012;
        } else if (DroneCameraErrCode.ERROR_CODE_10509021 == errCode) {
            resId = R.string.Msg_ErrorCode_10509021;
        } else if (DroneCameraErrCode.ERROR_CODE_10509022 == errCode) {
            resId = R.string.Msg_ErrorCode_10509022;
        } else if (DroneCameraErrCode.ERROR_CODE_1050A001 == errCode) {
            resId = R.string.Msg_ErrorCode_1050A001;
        } else if (DroneCameraErrCode.ERROR_CODE_1050A002 == errCode) {
            resId = R.string.Msg_ErrorCode_1050A002;
        } else if (DroneCameraErrCode.ERROR_CODE_1050A003 == errCode) {
            resId = R.string.Msg_ErrorCode_1050A003;
        } else if (DroneCameraErrCode.ERROR_CODE_1050B001 == errCode) {
            resId = R.string.Msg_ErrorCode_1050B001;
        } else if (DroneCameraErrCode.ERROR_CODE_1050C001 == errCode) {
            resId = R.string.Msg_ErrorCode_1050C001;
        } else if (DroneCameraErrCode.ERROR_CODE_1050D001 == errCode) {
            resId = R.string.Msg_ErrorCode_1050D001;
        } else if (DroneCameraErrCode.ERROR_CODE_1050D002 == errCode) {
            resId = R.string.Msg_ErrorCode_1050D002;
        }
        // 飞行器-相机二级异常
        else if (DroneCameraErrCode.ERROR_CODE_10503001 == errCode) {
            resId = R.string.Msg_ErrorCode_10503001;
        } else if (DroneCameraErrCode.ERROR_CODE_10504003 == errCode) {
            resId = R.string.Msg_ErrorCode_10504003;
        } else if (DroneCameraErrCode.ERROR_CODE_10504004 == errCode) {
            resId = R.string.Msg_ErrorCode_10504004;
        } else if (DroneCameraErrCode.ERROR_CODE_10504005 == errCode) {
            resId = R.string.Msg_ErrorCode_10504005;
        } else if (DroneCameraErrCode.ERROR_CODE_10504006 == errCode) {
            resId = R.string.Msg_ErrorCode_10504006;
        } else if (DroneCameraErrCode.ERROR_CODE_10505001 == errCode) {
            resId = R.string.Msg_ErrorCode_10505001;
        }
        // 飞行器-相机三级异常
        else if (DroneCameraErrCode.ERROR_CODE_10504001 == errCode) {
            resId = R.string.Msg_ErrorCode_10504001;
        } else if (DroneCameraErrCode.ERROR_CODE_10504002 == errCode) {
            resId = R.string.Msg_ErrorCode_10504002;
        } else if (DroneCameraErrCode.ERROR_CODE_10510001 == errCode) {
            resId = R.string.Msg_ErrorCode_10510001;
        } else if (DroneCameraErrCode.ERROR_CODE_10510002 == errCode) {
            resId = R.string.Msg_ErrorCode_10510002;
        }
        return resId;
    }

    /**
     * 获取图传异常错误码提示文案
     * */
    private static int getSDRStringResId(long errCode) {
        int resId = 0;
        // 飞行器-图传一级异常
        if (DronePicTransmissionErrCode.ERROR_CODE_10601001 == errCode) {
            resId = R.string.Msg_ErrorCode_10601001;
        } else if (DronePicTransmissionErrCode.ERROR_CODE_10601003 == errCode) {
            resId = R.string.Msg_ErrorCode_10601003;
        } else if (DronePicTransmissionErrCode.ERROR_CODE_10601004 == errCode) {
            resId = R.string.Msg_ErrorCode_10601004;
        } else if (DronePicTransmissionErrCode.ERROR_CODE_10601005 == errCode) {
            resId = R.string.Msg_ErrorCode_10601005;
        }
        return resId;
    }

    /**
     * 获取系统状态异常错误码提示文案
     * */
    private static int getSystemStatusStringResId(long errCode) {
        int resId = 0;
        // 飞行器-系统状态一级异常
        if (DroneSystemStatusErrCode.ERROR_CODE_10702001 == errCode) {
            resId = R.string.Msg_ErrorCode_10702001;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10702002 == errCode) {
            resId = R.string.Msg_ErrorCode_10702002;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10703002 == errCode) {
            resId = R.string.Msg_ErrorCode_10703002;
        }
        // 飞行器-系统状态二级异常
        else if (DroneSystemStatusErrCode.ERROR_CODE_10701002 == errCode) {
            resId = R.string.Msg_ErrorCode_10701002;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10701003 == errCode) {
            resId = R.string.Msg_ErrorCode_10701003;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10701004 == errCode) {
            resId = R.string.Msg_ErrorCode_10701004;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10701005 == errCode) {
            resId = R.string.Msg_ErrorCode_10701005;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10701006 == errCode) {
            resId = R.string.Msg_ErrorCode_10701006;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10701007 == errCode) {
            resId = R.string.Msg_ErrorCode_10701007;
        } else if (DroneSystemStatusErrCode.ERROR_CODE_10703001 == errCode) {
            resId = R.string.Msg_ErrorCode_10703001;
        }
        // 飞行器-系统状态三级异常
        else if (DroneSystemStatusErrCode.ERROR_CODE_10701001 == errCode) {
            resId = R.string.Msg_ErrorCode_10701001;
        }
        return resId;
    }

    /**
     * 获取机库错误码文案
     * */
    private static int getNestStringResId(long type, long errCode){
        int resId = 0;
        if (type == NestSDRErrCode.TYPE){ //代号0x01
            return getNestSDRStringResId(errCode);
        }else if (type == NestIntegratedControlErrCode.TYPE){  //代号0x02
            return getNestIntegratedControlStringResId(errCode);
        }else if (type == NestPeripheralMcuErrCode.type){ //代号0x03
            return getNestPeripheralMcuStringResId(errCode);
        }else if (type == NestSensorMcuErrCode.type){ //代号0x04
            return getNestSensorMcuStringResId(errCode);
        }
        return resId;
    }

    /**
     * 获取机库错误码文案
     * */
    private static int getNewNestStringResId(long type, long errCode){
        int resId = 0;
        if (type == NestNewIntegratedControlErrCode.TYPE){ //代号0x01
            return getNestNewIntegratedControlStringResId(errCode);
        }
        return resId;
    }

    /**
     * 获取机库图传故障码提示文案
     * */
    private static int getNestSDRStringResId(long errCode) {
        int resId = 0;
        // 飞行器-系统状态一级异常
        if (NestSDRErrCode.ERROR_CODE_40101001 == errCode) {
            resId = R.string.Msg_ErrorCode_40101001;
        } else if (NestSDRErrCode.ERROR_CODE_40101002 == errCode) {
            resId = R.string.Msg_ErrorCode_40101002;
        } else if (NestSDRErrCode.ERROR_CODE_40101004 == errCode) {
            resId = R.string.Msg_ErrorCode_40101004;
        } else if (NestSDRErrCode.ERROR_CODE_40101005 == errCode) {
            resId = R.string.Msg_ErrorCode_40101005;
        }
        return resId;
    }

    /**
     * 获取机库综控故障码提示文案
     * */
    private static int getNestIntegratedControlStringResId(long errCode) {
        int resId = 0;
        // 飞行器-系统状态一级异常
        if (NestIntegratedControlErrCode.ERROR_CODE_40203001 == errCode) {
            resId = R.string.Msg_ErrorCode_40203001;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203002 == errCode) {
            resId = R.string.Msg_ErrorCode_20103106;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203003 == errCode) {
            resId = R.string.Msg_ErrorCode_40203003;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203004 == errCode) {
            resId = R.string.Msg_ErrorCode_40203004;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203005 == errCode) {
            resId = R.string.Msg_ErrorCode_40203005;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203006 == errCode) {
            resId = R.string.Msg_ErrorCode_40203006;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203007 == errCode) {
            resId = R.string.Msg_ErrorCode_40203007;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203008 == errCode) {
            resId = R.string.Msg_ErrorCode_40203008;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203009 == errCode) {
            resId = R.string.Msg_ErrorCode_40203009;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203010 == errCode) {
            resId = R.string.Msg_ErrorCode_40203010;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203011 == errCode) {
            resId = R.string.Msg_ErrorCode_40203011;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203012 == errCode) {
            resId = R.string.Msg_ErrorCode_40203012;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203013 == errCode) {
            resId = R.string.Msg_ErrorCode_40203013;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203014 == errCode) {
            resId = R.string.Msg_ErrorCode_40203014;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203015 == errCode) {
            resId = R.string.Msg_ErrorCode_40203015;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203016 == errCode) {
            resId = R.string.Msg_ErrorCode_40203016;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203017 == errCode) {
            resId = R.string.Msg_ErrorCode_40203017;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203018 == errCode) {
            resId = R.string.Msg_ErrorCode_40203018;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203019 == errCode) {
            resId = R.string.Msg_ErrorCode_40203019;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301A == errCode) {
            resId = R.string.Msg_ErrorCode_4020301A;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301B == errCode) {
            resId = R.string.Msg_ErrorCode_4020301B;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301C == errCode) {
            resId = R.string.Msg_ErrorCode_4020301C;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301D == errCode) {
            resId = R.string.Msg_ErrorCode_4020301D;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301E == errCode) {
            resId = R.string.Msg_ErrorCode_4020301E;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020301F == errCode) {
            resId = R.string.Msg_ErrorCode_4020301F;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203020 == errCode) {
            resId = R.string.Msg_ErrorCode_40203020;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203021 == errCode) {
            resId = R.string.Msg_ErrorCode_40203021;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203022 == errCode) {
            resId = R.string.Msg_ErrorCode_40203022;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203023 == errCode) {
            resId = R.string.Msg_ErrorCode_40203023;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203024 == errCode) {
            resId = R.string.Msg_ErrorCode_40203024;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203025 == errCode) {
            resId = R.string.Msg_ErrorCode_40203025;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203026 == errCode) {
            resId = R.string.Msg_ErrorCode_40203026;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203027 == errCode) {
            resId = R.string.Msg_ErrorCode_40203027;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203028 == errCode) {
            resId = R.string.Msg_ErrorCode_40203028;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_40203029 == errCode) {
            resId = R.string.Msg_ErrorCode_40203029;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020302A == errCode) {
            resId = R.string.Msg_ErrorCode_4020302a;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020302B == errCode) {
            resId = R.string.Msg_ErrorCode_4020302b;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020302C == errCode) {
            resId = R.string.Msg_ErrorCode_4020302c;
        } else if (NestIntegratedControlErrCode.ERROR_CODE_4020302D == errCode) {
            resId = R.string.Msg_ErrorCode_4020302d;
        }
        return resId;
    }

    /**
     * 获取机库综控故障码提示文案
     * */
    private static int getNestNewIntegratedControlStringResId(long errCode) {
        int resId = 0;
        // 飞行器-系统状态一级异常
        if (NestNewIntegratedControlErrCode.ERROR_CODE_20103048 == errCode) {
            resId = R.string.Msg_ErrorCode_40203001;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103106 == errCode) {
            resId = R.string.Msg_ErrorCode_20103106;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103001 == errCode) {
            resId = R.string.Msg_ErrorCode_40203003;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103003 == errCode) {
            resId = R.string.Msg_ErrorCode_40203004;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103091 == errCode) {
            resId = R.string.Msg_ErrorCode_40203005;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103057 == errCode) {
            resId = R.string.Msg_ErrorCode_40203006;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103059 == errCode) {
            resId = R.string.Msg_ErrorCode_40203007;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103250 == errCode) {
            resId = R.string.Msg_ErrorCode_40203008;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103251 == errCode) {
            resId = R.string.Msg_ErrorCode_40203009;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103252 == errCode) {
            resId = R.string.Msg_ErrorCode_40203010;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103253 == errCode) {
            resId = R.string.Msg_ErrorCode_40203011;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103098 == errCode) {
            resId = R.string.Msg_ErrorCode_40203012;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103100 == errCode) {
            resId = R.string.Msg_ErrorCode_40203013;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103099 == errCode) {
            resId = R.string.Msg_ErrorCode_40203014;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103101 == errCode) {
            resId = R.string.Msg_ErrorCode_40203015;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103102 == errCode) {
            resId = R.string.Msg_ErrorCode_40203016;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103104 == errCode) {
            resId = R.string.Msg_ErrorCode_40203017;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103105 == errCode) {
            resId = R.string.Msg_ErrorCode_40203018;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103244 == errCode) {
            resId = R.string.Msg_ErrorCode_40203019;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103243 == errCode) {
            resId = R.string.Msg_ErrorCode_4020301A;
        }else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103249 == errCode) {
            resId = R.string.Msg_ErrorCode_40203025;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103245 == errCode) {
            resId = R.string.Msg_ErrorCode_40203028;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103247 == errCode) {
            resId = R.string.Msg_ErrorCode_40203029;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103248 == errCode) {
            resId = R.string.Msg_ErrorCode_4020302a;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103254 == errCode) {
            resId = R.string.Msg_ErrorCode_4020302b;
        } else if (NestNewIntegratedControlErrCode.ERROR_CODE_20103246 == errCode) {
            resId = R.string.Msg_ErrorCode_4020302c;
        }
        return resId;
    }

    /**
     * 机库外设MCU故障码文案
     * */
    private static int getNestPeripheralMcuStringResId(long errCode){
        int resId = 0;
        if (NestPeripheralMcuErrCode.ERROR_CODE_40301001 == errCode) {
            resId = R.string.Msg_ErrorCode_40301001;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40301002 == errCode) {
            resId = R.string.Msg_ErrorCode_40301002;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40301003 == errCode) {
            resId = R.string.Msg_ErrorCode_40301003;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40301004 == errCode) {
            resId = R.string.Msg_ErrorCode_40301004;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40301005 == errCode) {
            resId = R.string.Msg_ErrorCode_40301005;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40302001 == errCode) {
            resId = R.string.Msg_ErrorCode_40302001;
        } else if (NestPeripheralMcuErrCode.ERROR_CODE_40302003 == errCode) {
            resId = R.string.Msg_ErrorCode_40302003;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302004 == errCode) {
            resId = R.string.Msg_ErrorCode_40302004;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302005 == errCode) {
            resId = R.string.Msg_ErrorCode_40302005;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302006 == errCode) {
            resId = R.string.Msg_ErrorCode_40302006;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302007 == errCode) {
            resId = R.string.Msg_ErrorCode_40302007;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302008 == errCode) {
            resId = R.string.Msg_ErrorCode_40302008;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40302009 == errCode) {
            resId = R.string.Msg_ErrorCode_40302009;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_4030200a == errCode) {
            resId = R.string.Msg_ErrorCode_4030200a;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40303001 == errCode) {
            resId = R.string.Msg_ErrorCode_40303001;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40303003 == errCode) {
            resId = R.string.Msg_ErrorCode_40303003;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40303004 == errCode) {
            resId = R.string.Msg_ErrorCode_40303004;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40304001 == errCode) {
            resId = R.string.Msg_ErrorCode_40304001;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40304003 == errCode) {
            resId = R.string.Msg_ErrorCode_40304003;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40304004 == errCode) {
            resId = R.string.Msg_ErrorCode_40304004;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40305001 == errCode) {
            resId = R.string.Msg_ErrorCode_40305001;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40305003 == errCode) {
            resId = R.string.Msg_ErrorCode_40305003;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40305004 == errCode) {
            resId = R.string.Msg_ErrorCode_40305004;
        }else if (NestPeripheralMcuErrCode.ERROR_CODE_40306001 == errCode) {
            resId = R.string.Msg_ErrorCode_40306001;
        }
        return resId;
    }

    /**
     * 获取飞控传感器MCU故障码文案
     * */
    private static int getNestSensorMcuStringResId(long errCode){
        int resId = 0;
        if (NestSensorMcuErrCode.ERROR_CODE_40402001 == errCode) {
            resId = R.string.Msg_ErrorCode_40402001;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402002 == errCode) {
            resId = R.string.Msg_ErrorCode_40402002;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402003 == errCode) {
            resId = R.string.Msg_ErrorCode_40402003;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402004 == errCode) {
            resId = R.string.Msg_ErrorCode_40402004;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402005 == errCode) {
            resId = R.string.Msg_ErrorCode_40402005;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402006 == errCode) {
            resId = R.string.Msg_ErrorCode_40402006;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402007 == errCode) {
            resId = R.string.Msg_ErrorCode_40402007;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402008 == errCode) {
            resId = R.string.Msg_ErrorCode_40402008;
        } else if (NestSensorMcuErrCode.ERROR_CODE_40402009 == errCode) {
            resId = R.string.Msg_ErrorCode_40402009;
        } else if (NestSensorMcuErrCode.ERROR_CODE_4040200A == errCode) {
            resId = R.string.Msg_ErrorCode_4040200A;
        } else if (NestSensorMcuErrCode.ERROR_CODE_4040200B == errCode) {
            resId = R.string.Msg_ErrorCode_4040200B;
        }else if (NestSensorMcuErrCode.ERROR_CODE_4040200D == errCode) {
            resId = R.string.Msg_ErrorCode_4040200D;
        }
        return resId;
    }
}
