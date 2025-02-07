package com.gdu.demo.utils;

import android.content.Context;

import com.gdu.demo.R;
import com.gdu.drone.GimbalType;

import java.util.Objects;

/**
 * @author wuqb
 * @date 2025/2/6
 * @description TODO
 */
public class CameraUtil {

    /**
     * 获取云台模式
     * @param context
     * @param gimbalType
     * @return
     */
    public static String[] getGimbalModes(Context context, GimbalType gimbalType){
        int gimbalModeId;
        String[] gimbalModes;
        if (Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S220
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S200
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PTL_S220_IR640
                || Objects.requireNonNull(gimbalType) == GimbalType.GIMBAL_PDL_S200_IR640) {
            gimbalModeId = R.array.gimbal_mode_S220;
        } else {
            gimbalModeId = R.array.gimbal_mode;
        }
        gimbalModes = context.getResources().getStringArray(gimbalModeId);
        return gimbalModes;
    }

    /**
     * 根据云台类型及位置获取值
     *
     * @param gimbalType
     * @param position
     * @return
     */
    public static int getGimbalModeValueByPosition(GimbalType gimbalType, int position) {
        int value = 0;
        switch (gimbalType) {
            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
                if (position == 0) {
                    value = 0;
                } else if (position == 1) {
                    value = 2;
                }
                break;
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
            case GIMBAL_PWG01:
            default:
                value = position;
                break;
        }
        return value;
    }
}
