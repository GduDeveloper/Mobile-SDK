package com.gdu.demo.flight.msgbox;


import com.gdu.demo.R;
import com.gdu.lib.base.GduEnvConfig;
import com.gdu.msdk.device.component.hms.DroneFlightControlErrCode;
import com.gdu.msdk.device.component.hms.bean.ErrorCodeDialogBean;

/**
 * @author wuqb
 * @date 2025/1/15
 * @description 临时工具类，为了解决底层获取不到string资源而添加的工具类方法
 *           后续建议将整个健康管理都从socket组件中移出，形成SDK组件，在SDK组件中添加相关的文案信息，解决这类问题
 */
public class MsgBoxDialogUtil {


    /**
     * 根据弹窗类型设置返回弹窗相应属性
     * */
    public static ErrorCodeDialogBean getErrCodeDialogBean(long type, long errCode) {
        if (type == DroneFlightControlErrCode.type) { //代号0x101
            return getFlightControlDialogBean(errCode);
        }
        return null;
    }

    private static ErrorCodeDialogBean getFlightControlDialogBean(long errCode){
//        if (DroneFlightControlErrCode.ERROR_CODE_10104008 == errCode){
//            return new ErrorCodeDialogBean.Builder()
//                    .setContentStr(String.format(GduEnvConfig.application.getString(R.string.Msg_ErrorCode_10104018_tips), 15))
//                    .setPositiveStr(R.string.Label_GoHomeNow).setFunctionId(ErrorCodeDialogBean.FUNCTION_RETURN_IMMEDIATELY).build();
//        } else if (DroneFlightControlErrCode.ERROR_CODE_10104009 == errCode){
//            return new ErrorCodeDialogBean.Builder()
//                    .setContentStr(String.format(GduEnvConfig.application.getString(R.string.Msg_ErrorCode_10104009_tips), 15))
//                    .setPositiveStr(R.string.Label_GoHomeNow).setFunctionId(ErrorCodeDialogBean.FUNCTION_RETURN_IMMEDIATELY).build();
//        } else if (DroneFlightControlErrCode.ERROR_CODE_10107011 == errCode){
//            return new ErrorCodeDialogBean.Builder()
//                    .setPositiveStr(R.string.Label_GoHomeNow).setFunctionId(ErrorCodeDialogBean.FUNCTION_RETURN_IMMEDIATELY).build();
//        }
        return null;
    }
}
