package com.gdu.demo.utils;

import android.content.Context;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.drone.GimbalType;
import com.gdu.util.logger.MyLogUtils;

import java.util.Objects;

/**
 * @author wuqb
 * @date 2025/2/6
 * @description TODO
 */
public class CameraUtil {


    protected static final Integer[] mNormalESValues = new Integer[]{0, 3, 4, 6, 8, 12, 15, 19, 23, 28, 30, 32, 34, 36,
            38, 39};
    protected static final Integer[] m4KC_Photo_ESValues = new Integer[]{0, 3, 4, 8, 12, 15, 19, 23, 28, 30, 32, 34, 36,
            39};
    protected static final Integer[] m4KC_Video_ESValues = new Integer[]{0, 3, 4, 8, 12, 15, 19, 23, 28, 30};
    /** 1k电子快门录像ES对应的值 */
    protected static final Integer[] m1k_Video_ESValues = new Integer[] {0x00, 0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08};
    /** 1k电子快门拍照ES对应的值 */
    protected static final Integer[] m1k_Photo_ESValues = new Integer[] {0x00, 0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x13, 0x04, 0x03, 0x02, 0x01};
    /** S220电子快门拍照ES对应的值 */
    protected static final Integer[] mS220_Photo_ESValues = new Integer[] {0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x13, 0x04, 0x03, 0x02, 0x01};
    /** S220电子快门录像ES对应的值 */
    protected static final Integer[] mS220_Video_ESValues = new Integer[] {0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08};
    /** 四光电子快门拍照ES对应的值 */
    protected static final Integer[] mFourLight_Photo_ESValues = new Integer[]{0x00, 0x12, 0x11, 0x0F, 0x0E, 0x0D, 0x0C,
            0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01};
    /** 四光电子快门录像ES对应的值 */
    protected static final Integer[] mFourLight_Video_ESValues = new Integer[] {0x00, 0x12, 0x11, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08};
    /** 新30X电子快门拍照ES对应的值 */
    protected static final Integer[] m30XNew_Photo_ESValues = new Integer[]{0x00, 0x11, 0x0F, 0x0E, 0x0D, 0x0C, 0x0B,
            0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x13, 0x04};
    /** 新30X电子快门录像ES对应的值 */
    protected static final Integer[] m30XNew_Video_ESValues = new Integer[] {0x00, 0x11, 0x0F, 0x0E, 0x0D, 0x0C, 0x0B,
            0x0A, 0x09, 0x08};
    /** 8KC电子快门拍照ES对应的值 */
    protected static final Integer[] m8KC_Photo_ESValues = new Integer[] {0x00, 0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x13, 0x04, 0x03, 0x02, 0x01};
    /** 8KC电子快门录像ES对应的值 */
    protected static final Integer[] m8KC_video_ESValues = new Integer[] {0x00, 0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08};
    /** PWG01电子快门拍照ES对应的值 */
    protected static final Integer[] mPWG01_Photo_ESValues = new Integer[] {0x12, 0x11, 0x10, 0x0F, 0x0E, 0x0D,
            0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x13, 0x04, 0x03};

    protected static final String[] mESStrArray = new String[]{"Auto", "1/6000", "1/4000", "1/3000", "1/2000", "1/1000", "1/500", "1/250",
            "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2","1"};
    protected static final String[] m4KCESStrArray = new String[]{"Auto", "1/6000", "1/4000",  "1/2000", "1/1000", "1/500", "1/250",
            "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1"};
    protected static final String[] m4KCESStrArrayVideo = new String[]{"Auto", "1/6000", "1/4000",  "1/2000", "1/1000", "1/500", "1/250",
            "1/125", "1/60", "1/30"};
    protected static final String[] m6KCameraESStrArray = new String[]{"Auto", "1/8000", "1/6000", "1/5000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1", "2", "4", "8"};
    protected static final String[] m6KRecordESStrArray = new String[]{"Auto", "1/8000", "1/6000", "1/5000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30"};
    protected static final String[] m4LightPhotoESStrArray = new String[]{"Auto", "1/8000", "1/6000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1", "2", "4", "8"};
    protected static final String[] m4LightRecordESStrArray = new String[]{"Auto", "1/8000", "1/6000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30"};

    protected static final String[] m30XNewtPhotoESStrArray = new String[]{"Auto", "1/6000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1"};
    protected static final String[] m30XNewRecordESStrArray = new String[]{"Auto", "1/6000", "1/4000", "1/2000", "1/1000",
            "1/500", "1/250", "1/125", "1/60", "1/30"};
    protected static final String[] mS220CameraPhotoESStrArray = new String[]{"1/8000", "1/6000", "1/5000",
            "1/4000", "1/2000", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1",
            "2", "4", "8"};
    protected static final String[] mS220CameraVideoESStrArray = new String[]{"1/8000", "1/6000", "1/5000",
            "1/4000", "1/2000", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30"};

    protected static final String[] mPWG01PhotoESStrArray = new String[]{"1/8000", "1/6000", "1/5000",
            "1/4000", "1/2000", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1",
            "2"};


    protected static final Integer[] m10X_EVValues = new Integer[]{18, 15, 0, 11, 14};
    /** 10C相机EV值集合 */
    protected static final Integer[] m10X_C_EVValues = new Integer[]{21, 18, 15, 0, 11, 14, 20};
    /** 4K相机EV值集合 */
    protected static final Integer[] m4K_EVValues = new Integer[]{18, 17, 16, 15, 10, 9, 0, 7, 8, 11, 12, 13, 14};
    /** 正常EV值集合 */
    protected static final Integer[] mNormal_EVValues = new Integer[]{1, 2, 3, 0, 4, 5, 6};
    /** 4KC相机EV值集合 */
    protected static final Integer[] m4KC_EVValues = new Integer[]{21, 18, 15, 0, 11, 14, 20};
    /** 4光相机EV值集合 */
    protected static final Integer[] m4Light_EVValues = new Integer[]{0x23, 0x1C, 0x16, 0x10, 0x0A};
    /** 30倍相机自动模式EV值集合 */
    protected static final Integer[] m30X_EVValuesAuto = new Integer[]{0x16};
    /** 30倍相机手动模式EV值集合 */
    protected static final Integer[] m30X_EVValuesManual = new Integer[]{0x29, 0x23, 0x1C, 0x16, 0x10, 0x0A, 0x04};

    /** 1K相机EV值集合 */
    protected static final Integer[] m1K_EVValues = new Integer[]{0x29, 0x23, 0x1C, 0x16, 0x10, 0x0A, 0x04};
    /** S220相机EV值集合 */
    protected static final Integer[] mS220_EVValues = new Integer[]{0x29, 0x26, 0x23, 0x20, 0x1C, 0x19, 0x16, 0x13,
            0x10, 0x0D, 0x0A, 0x07, 0x04};
    /** PDL_300相机EV值集合 */
    protected static final Integer[] mPDL300_EVValues = new Integer[]{0x29, 0x26, 0x23, 0x20, 0x1C, 0x19, 0x16, 0x13,
            0x10, 0x0D, 0x0A, 0x07, 0x04};
    /** 6K相机EV值集合 */
    protected static final Integer[] m6KEVValues = new Integer[]{0x29, 0x23, 0x1C, 0x16, 0x10, 0x0A, 0x04};
    /** 8KC相机EV值集合 */
    protected static final Integer[] m8KCEVValues = new Integer[]{0x29, 0x26, 0x23, 0x20, 0x1C, 0x19, 0x16, 0x13, 0x10, 0x0D, 0x0A, 0x07, 0x04};

    private static final String[] mNormalEVStrArray = new String[]{"-3", "-2", "-1", "0", "1", "2", "3"};
    private static final String[] mByrdTZoom10XEVStrArray = new String[]{"-2", "-1",  "0", "1", "2"};
    private static final String[] mByrdTZoom30XEVStrArray = new String[]{"-3", "-2", "-1",  "0", "1", "2", "3"};
    private static final String[] mByrdT4KEVStrArray = new String[]{"-2", "-1.7", "-1.3", "-1", "-0.7", "-0.3", "0",
            "0.3", "0.7", "1", "1.3", "1.7", "2"};
    private static final String[] mByrdT4LightEVStrArray = new String[]{"-2", "-1",  "0", "1", "2"};
    private static final String[] m8KCEVStrArray = new String[]{"-3", "-2.5", "-2", "-1.5", "-1", "-0.5", "0", "0.5",
            "1", "1.5", "2", "2.5", "3"};
    private static final String[] mS220EVStrArray = new String[]{"-3", "-2.5", "-2", "-1.5", "-1", "-0.5", "0", "0.5",
            "1", "1.5", "2", "2.5", "3"};


    protected static final Integer[] mNormalISOValues = new Integer[]{0, 2, 3, 4, 5, 6};
    protected static final Integer[] m10X_ISOValues = new Integer[]{0, 1, 2, 3, 4, 5, 6};
    protected static final Integer[] m30X_ISOValues = new Integer[]{0, 2, 3, 4, 5, 6, 7, 8, 9};
    protected static final Integer[] m4K_ISOValues = new Integer[]{0, 1, 2, 3, 4, 5, 6};
    protected static final Integer[] m1K_ISOValues = new Integer[]{0x00, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    protected static final Integer[] mPDL300_ISOValues = new Integer[]{0x00, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
    protected static final Integer[] mS220_ISOValues = new Integer[]{0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A};
    protected static final Integer[] mMicroFourLightPhoto_ISOValues = new Integer[]{0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    protected static final Integer[] mMicroFourLightVideo_ISOValues = new Integer[]{0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
    protected static final Integer[] m6KISOValues = new Integer[]{0x00, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
    protected static final Integer[] m8KCISOValues = new Integer[]{0x00, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};


    private static final String[] mNormalISOArray = new String[]{"Auto",  "200", "400", "800", "1600", "3200"};
    private static final String[] m30XISOArray = new String[]{"Auto", "100", "200", "400", "800", "1600", "3200",
            "6400", "12800"};
    private static final String[] m8KCISOArray = new String[]{"Auto", "100", "200", "400", "800", "1600", "3200"};
    private static final String[] m4KISOArray = new String[]{"Auto", "100", "200", "400", "800", "1600", "3200"};
    private static final String[] mS220ISOArray = new String[]{"100", "200", "400", "800", "1600", "3200",
            "6400", "12800", "25600"};

    private static final String[] mMicroFourLightPhotoISOArray = new String[]{"100", "200", "400", "800", "1600", "3200",
            "6400", "12800"};
    private static final String[] mMicroFourLightVideoISOArray = new String[]{"100", "200", "400", "800", "1600", "3200",
            "6400"};
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




    public static boolean isSupportISOGimbal(GimbalType gimbalType) {
        return gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S200
                || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.ByrdT_4k
                || gimbalType == GimbalType.ByrdT_4kc
                || gimbalType == GimbalType.ByrdT_10X_Zoom
                || gimbalType == GimbalType.ByrdT_10X_C_Zoom
                || gimbalType == GimbalType.ByrdT_30X_Zoom
                || gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || gimbalType == GimbalType.ByrdT_35X_Zoom
                || gimbalType == GimbalType.ByrdT_Loudspeaker
                || gimbalType == GimbalType.ByrdT_Searchlight
                || gimbalType == GimbalType.ByrdT_Gas_Detector
                || gimbalType == GimbalType.GIMBAL_PWG01;
    }

    /**
     * 云台是否支持ES设置
     *
     * @return
     */
    public static boolean isSupportESGimbal(GimbalType gimbalType) {
        return gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S200
                || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.ByrdT_30X_Zoom
                || gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || gimbalType == GimbalType.ByrdT_35X_Zoom
                || gimbalType == GimbalType.ByrdT_4kc
                || gimbalType == GimbalType.GIMBAL_PWG01;
    }

    /**
     * 云台是否支持AELock设置
     *
     * @return
     */
    public static boolean isSupportAELockGimbal(GimbalType gimbalType) {
        return gimbalType == GimbalType.GIMBAL_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S200
                || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_PWG01;
    }



    /**
     * 将ISO值映射为展示值
     * @param context
     * @return
     */
    public static String getISODisplayByValue(Context context) {
//        MyLogUtils.i("getISODisplayByValue() lightISOValue = " + DataUtil.byte2Hex((byte) GlobalVariable.lightISOValue));
        String isoDisplay = "N/A";
        if (context == null) {
//            MyLogUtils.i("getISODisplayByValue() isoDisplay = " + isoDisplay);
            return isoDisplay;
        }
        int[] gimbalISOSet = getISOSetByGimbal(context);
        String[] gimbalDisplayISOSet = getISODisplaySetByGimbal(context);
        boolean isEmptyData = gimbalISOSet == null || gimbalISOSet.length == 0 || gimbalDisplayISOSet == null
                || gimbalDisplayISOSet.length == 0 || gimbalISOSet.length != gimbalDisplayISOSet.length;
        if (isEmptyData) {
            MyLogUtils.i("getISODisplayByValue() isEmptyData");
            return isoDisplay;
        }
        MyLogUtils.i("getISODisplayByValue() lightISOValue = " + GlobalVariable.lightISOValue);
        int pos = -1;
        for (int i = 0; i < gimbalISOSet.length; i++) {
            if (GlobalVariable.lightISOValue == gimbalISOSet[i]) {
                pos = i;
                break;
            }
        }
        MyLogUtils.i("getISODisplayByValue() pos = " + pos);
        if (pos >= 0) {
            isoDisplay = gimbalDisplayISOSet[pos];
        }
        MyLogUtils.i("getISODisplayByValue() isoDisplay = " + isoDisplay);
        return isoDisplay;
    }

    private static int[] getISOSetByGimbal(Context context) {
        if (context == null) {
            return null;
        }
        if (GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || GlobalVariable.gimbalType == GimbalType.ByrdT_35X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrT_IR_1K
                || GlobalVariable.gimbalType == GimbalType.Small_Double_Light
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT) {
            return context.getResources().getIntArray(R.array.visible_light_iso_set_ByrT_IR_1K);
        } else if (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PQL02_SE
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PWG01
        ) {
            return context.getResources().getIntArray(R.array.visible_light_iso_set_S220);
        } else {
            return context.getResources().getIntArray(R.array.visible_light_iso_set_normal);
        }
    }

    /**
     * 获取ISO值展示集合
     * @param context
     * @return
     */
    private static String[] getISODisplaySetByGimbal(Context context) {
        if (context == null) {
            return null;
        }
        if (GlobalVariable.gimbalType == GimbalType.ByrdT_4k || GlobalVariable.gimbalType == GimbalType.ByrdT_4kc
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_8KC
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_300C) {
            return context.getResources().getStringArray(R.array.visiblelight_iso_set_display_ByrdT_4k);
        } else if (GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || GlobalVariable.gimbalType == GimbalType.ByrdT_35X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrT_6k
                || GlobalVariable.gimbalType == GimbalType.ByrT_IR_1K
                || GlobalVariable.gimbalType == GimbalType.Small_Double_Light
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT) {
            return context.getResources().getStringArray(R.array.visiblelight_iso_set_display_30x);
        } else if (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PQL02_SE
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PWG01) {
            return context.getResources().getStringArray(R.array.visiblelight_iso_set_display_S220);
        } else {
            return context.getResources().getStringArray(R.array.visiblelight_iso_set_display_normal);
        }
    }

    /**
     * 通过ES的值获取ES值对应的名称
     * @param gimbalType
     * @param value
     * @param isFromCycleUpload 是否为周期上报的值
     * @return
     */
    public static String getESNameByValue(GimbalType gimbalType, int value,boolean isFromCycleUpload) {
        MyLogUtils.i("getESNameIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        if ((gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.GIMBAL_PDL_S200
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || gimbalType == GimbalType.GIMBAL_PWG01) && isFromCycleUpload) {
            return getS220ESNameByValue(value);
        } else {
            final int index = getESValueIndexByValue(gimbalType, value);
            MyLogUtils.i("getESNameIndexByValue() index = " + index);
            final String[] mNames = getESNamesByGimbalType(gimbalType);
            return mNames[index];
        }
    }

    public static int getESValueIndexByValue(GimbalType gimbalType, int value) {
        MyLogUtils.i("getESValueIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        int index;
        switch (gimbalType) {
            case ByrdT_4k:
            case ByrdT_4kc:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(m4KC_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(m4KC_Video_ESValues, value);
                }
                break;

            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(m30XNew_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(m30XNew_Video_ESValues, value);
                }
                break;

            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_PDL_300C:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(m1k_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(m1k_Video_ESValues, value);
                }
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(mFourLight_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(mFourLight_Video_ESValues, value);
                }
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(mS220_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(mS220_Video_ESValues, value);
                }
                break;

            case GIMBAL_PWG01:
                if (GlobalVariable.isPhoto) {
                    index = getIntArrayValueIndexByValue(mPWG01_Photo_ESValues, value);
                } else {
                    index = getIntArrayValueIndexByValue(mS220_Video_ESValues, value);
                }
                break;

            default:
                index = getIntArrayValueIndexByValue(mNormalESValues, value);
                break;
        }
        return index;
    }

    public static String[] getESNamesByGimbalType(GimbalType gimbalType) {
//        MyLogUtils.d("getESNamesByGimbalType() isPhoto = " + GlobalVariable.isPhoto);
        String[] names;
        switch (gimbalType) {
            case ByrdT_4kc:
                if (GlobalVariable.isPhoto) {
                    names = m4KCESStrArray;
                } else {
                    names = m4KCESStrArrayVideo;
                }
                break;

            case ByrT_IR_1K:
            case Small_Double_Light:
            case ByrT_6k:
            case GIMBAL_8KC:
            case GIMBAL_PDL_300C:
                if (GlobalVariable.isPhoto) {
                    names = m6KCameraESStrArray;
                } else {
                    names = m6KRecordESStrArray;
                }
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                if (GlobalVariable.isPhoto) {
                    names = m4LightPhotoESStrArray;
                } else {
                    names = m4LightRecordESStrArray;
                }
                break;

            case ByrdT_30X_Zoom_NEW:
                if (GlobalVariable.isPhoto) {
                    names = m30XNewtPhotoESStrArray;
                } else {
                    names = m30XNewRecordESStrArray;
                }
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    names = mS220CameraPhotoESStrArray;
                } else {
                    names = mS220CameraVideoESStrArray;
                }
                break;

            case GIMBAL_PWG01:
                if (GlobalVariable.isPhoto) {
                    names = mPWG01PhotoESStrArray;
                } else {
                    names = mS220CameraVideoESStrArray;
                }
                break;

            default:
                names = mESStrArray;
                break;
        }
        return names;
    }

    private static int getIntArrayValueIndexByValue(Integer[] valueArray, int value) {
        int index = 0;
        for (int i = 0; i < valueArray.length; i++) {
            if (valueArray[i] == value) {
                index = i;
            }
        }
        return index;
    }



    /**
     * 通过ES的值获取ES值对应的名称-S220
     * @param esValue
     * @return
     */
    public static String getS220ESNameByValue(int esValue) {
        boolean isLarge = (esValue & 0x8000) == 0x8000;
        String esStr;
        int orgValue = esValue & 0x7FFF;
        if (isLarge) {
            esStr = String.valueOf(orgValue);
        } else {
            if (esValue == 0) {
                esStr = String.valueOf(orgValue);
            } else {
                esStr = "1/" + orgValue;
            }
        }
        return esStr;
    }

    /**
     * 通过EV的值获取EV值对应的名称
     * @param context
     * @param gimbalType
     * @param value
     * @return
     */
    public static String getEVNameByValue(Context context, GimbalType gimbalType, int value) {
//        MyLogUtils.i("getEVNameIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        final int index = getEVValueIndexByValue(gimbalType, value);
//        MyLogUtils.i("getEVNameIndexByValue() index = " + index);
        final String[] mNames = getEVNamesByGimbalType(context, gimbalType);
        if(mNames.length<index){

        }
        return mNames[index];
    }
    public static int getEVValueIndexByValue(GimbalType gimbalType, int value) {
//        MyLogUtils.i("getEVValueIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        int index = 0;
        switch (gimbalType) {

            case ByrdT_TMS:
            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_PTL600:
            case GIMBAL_PDL_10X:
            case GIMBAL_450J:
                index = getIntArrayValueIndexByValue(m1K_EVValues, value);
                break;

            case GIMBAL_PDL_300C:
                index = getIntArrayValueIndexByValue(mPDL300_EVValues, value);
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                index = getIntArrayValueIndexByValue(m4Light_EVValues, value);
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
            case GIMBAL_PWG01:
                index = getIntArrayValueIndexByValue(mS220_EVValues, value);
                break;

            case GIMBAL_8KC:
                index = getIntArrayValueIndexByValue(m8KCEVValues, value);
                break;

            case ByrT_6k:
            case ORTHO_CAMERA:
            case GIMBAL_IR_1KG:
                index = getIntArrayValueIndexByValue(m6KEVValues, value);
                break;

            default:
                index = getIntArrayValueIndexByValue(mNormal_EVValues, value);
                break;
        }
        return index;
    }

    public static String[] getEVNamesByGimbalType(Context context, GimbalType gimbalType) {
        String[] names;
        switch (gimbalType) {
            case ByrdT_10X_Zoom:
                names = mByrdTZoom10XEVStrArray;
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                names = mByrdT4LightEVStrArray;
                break;

            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
                names = mByrdTZoom30XEVStrArray;
                break;

            case ByrdT_4k:
                names = mByrdT4KEVStrArray;
                break;

            case GIMBAL_8KC:
            case GIMBAL_PDL_300C:
                names = m8KCEVStrArray;
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
            case GIMBAL_PWG01:
                names = mS220EVStrArray;
                break;

            default:
                names = mNormalEVStrArray;
                break;
        }
        return names;
    }

    public static Integer[] getISOValuesByGimbalType(GimbalType gimbalType) {
        Integer[] values;
        switch (gimbalType) {
            case ByrdT_10X_Zoom:
                values = m10X_ISOValues;
                break;

            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
                values = m30X_ISOValues;
                break;

            case ByrdT_4k:
            case ByrdT_4kc:
                values = m4K_ISOValues;
                break;

            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                values = m1K_ISOValues;
                break;

            case GIMBAL_PDL_300C:
                values = mPDL300_ISOValues;
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_PWG01:
                values = mS220_ISOValues;
                break;

            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    values = mMicroFourLightPhoto_ISOValues;
                } else {
                    values = mMicroFourLightVideo_ISOValues;
                }
                break;

            case GIMBAL_8KC:
                values = m8KCISOValues;
                break;

            case ByrT_6k:
            case ORTHO_CAMERA:
            case GIMBAL_IR_1KG:
                values = m6KISOValues;
                break;

            default:
                values = mNormalISOValues;
                break;
        }
        return values;
    }


    public static String[] getISONamesByGimbalType( GimbalType gimbalType) {
        String[] names;
        switch (gimbalType) {
            case ByrdT_4k:
            case ByrdT_4kc:
                names = m4KISOArray;
                break;

            case ByrT_6k:
            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
            case GIMBAL_PDL_300C:
                names = m30XISOArray;
                break;

            case GIMBAL_8KC:
                names = m8KCISOArray;
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_PWG01:
                names = mS220ISOArray;
                break;

            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    names = mMicroFourLightPhotoISOArray;
                } else {
                    names = mMicroFourLightVideoISOArray;
                }
                break;

            default:
                names = mNormalISOArray;
                break;
        }
        return names;
    }

    public static int getISONameIndexByValue(GimbalType gimbalType, String value) {
        MyLogUtils.i("getISONameIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        int index;
        switch (gimbalType) {
            case ByrdT_4k:
            case ByrdT_4kc:
                index = getStringArrayValueIndexByValue(m4KISOArray, value);
                break;

            case ByrT_6k:
            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
            case GIMBAL_PDL_300C:
                index = getStringArrayValueIndexByValue(m30XISOArray, value);
                break;

            case GIMBAL_8KC:
                index = getStringArrayValueIndexByValue(m8KCISOArray, value);
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_PWG01:
                index = getStringArrayValueIndexByValue(mS220ISOArray, value);
                break;

            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(mMicroFourLightPhotoISOArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(mMicroFourLightVideoISOArray, value);
                }
                break;

            default:
                index = getStringArrayValueIndexByValue(mNormalISOArray, value);
                break;
        }
        return index;
    }

    private static int getStringArrayValueIndexByValue(String[] valueArray, String value) {
        int index = 0;
        for (int i = 0; i < valueArray.length; i++) {
            if (value.equals(valueArray[i])) {
                index = i;
            }
        }
        return index;
    }

    public static Integer[] getESValuesByGimbalType(GimbalType gimbalType) {
        Integer[] values;
        switch (gimbalType) {
            case ByrdT_4k:
            case ByrdT_4kc:
                if (GlobalVariable.isPhoto) {
                    values = m4KC_Photo_ESValues;
                } else {
                    values = m4KC_Video_ESValues;
                }
                break;

            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
                if (GlobalVariable.isPhoto) {
                    values = m30XNew_Photo_ESValues;
                } else {
                    values = m30XNew_Video_ESValues;
                }
                break;

            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_PDL_300C:
                if (GlobalVariable.isPhoto) {
                    values = m1k_Photo_ESValues;
                } else {
                    values = m1k_Video_ESValues;
                }
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                if (GlobalVariable.isPhoto) {
                    values = mFourLight_Photo_ESValues;
                } else {
                    values = mFourLight_Video_ESValues;
                }
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    values = mS220_Photo_ESValues;
                } else {
                    values = mS220_Video_ESValues;
                }
                break;

            case GIMBAL_PWG01:
                if (GlobalVariable.isPhoto) {
                    values = mPWG01_Photo_ESValues;
                } else {
                    values = mS220_Video_ESValues;
                }
                break;

            default:
                values = mNormalESValues;
                break;
        }
        return values;
    }

    /**
     * 通过ES的值的名称获取ES值的名称对应的index
     * @param gimbalType
     * @param value
     * @return
     */
    public static int getESNameIndexByValue(GimbalType gimbalType, String value) {
        MyLogUtils.i("getESNameIndexByValue() gimbalType = " + gimbalType + "; value = " + value);
        int index;
        switch (gimbalType) {
            case ByrdT_4kc:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(m4KCESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(m4KCESStrArrayVideo, value);
                }
                break;

            case ByrT_IR_1K:
            case Small_Double_Light:
            case ByrT_6k:
            case GIMBAL_8KC:
            case GIMBAL_PDL_300C:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(m6KCameraESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(m6KRecordESStrArray, value);
                }
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(m4LightPhotoESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(m4LightRecordESStrArray, value);
                }
                break;

            case ByrdT_30X_Zoom_NEW:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(m30XNewtPhotoESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(m30XNewRecordESStrArray, value);
                }
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(mS220CameraPhotoESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(mS220CameraVideoESStrArray, value);
                }
                break;

            case GIMBAL_PWG01:
                if (GlobalVariable.isPhoto) {
                    index = getStringArrayValueIndexByValue(mPWG01PhotoESStrArray, value);
                } else {
                    index = getStringArrayValueIndexByValue(mS220CameraVideoESStrArray, value);
                }
                break;

            default:
                index = getStringArrayValueIndexByValue(mESStrArray, value);
                break;
        }
        return index;
    }

    public static Integer[] getEVValuesByGimbalType(GimbalType gimbalType) {
        Integer[] values;
        switch (gimbalType) {
            case ByrdT_10X_Zoom:
                values = m10X_EVValues;
                break;

            case ByrdT_10X_C_Zoom:
                values = m10X_C_EVValues;
                break;

            case ByrdT_4k:
                values = m4K_EVValues;
                break;

            case ByrdT_4kc:
                values = m4KC_EVValues;
                break;

            case ByrdT_30X_Zoom:
            case ByrdT_30X_Zoom_NEW:
            case ByrdT_35X_Zoom:
                values = m30X_EVValuesManual;
                break;

            case ByrdT_TMS:
            case ByrT_IR_1K:
            case Small_Double_Light:
            case GIMBAL_PTL600:
            case GIMBAL_PDL_10X:
            case GIMBAL_450J:
                values = m1K_EVValues;
                break;

            case GIMBAL_PDL_300C:
                values = mPDL300_EVValues;
                break;

            case GIMBAL_FOUR_LIGHT:
            case GIMBAL_FOUR_LIGHT_NEW:
                values = m4Light_EVValues;
                break;

            case GIMBAL_PDL_S220:
            case GIMBAL_PDL_S200:
            case GIMBAL_PDL_S220PRO_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT:
            case GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT:
            case GIMBAL_PTL_S220_IR640:
            case GIMBAL_PDL_S200_IR640:
            case GIMBAL_MICRO_FOUR_LIGHT:
            case GIMBAL_PQL02_SE:
            case GIMBAL_PWG01:
                values = mS220_EVValues;
                break;

            case GIMBAL_8KC:
                values = m8KCEVValues;
                break;

            case ByrT_6k:
            case ORTHO_CAMERA:
            case GIMBAL_IR_1KG:
                values = m6KEVValues;
                break;

            default:
                values = mNormal_EVValues;
                break;
        }
        return values;
    }

    public static boolean checkISOAndShutterUnableSet(boolean isAutoMode, int aeLockValue) {
        if (isSupportAutoSwitchGimbal(GlobalVariable.gimbalType)) {
            return isAutoMode || aeLockValue == 1;
        } else if (isSupportAELockGimbal(GlobalVariable.gimbalType)) {
            return aeLockValue == 1;
        } else {
            return false;
        }
    }

    public static boolean checkEVUnableSet(boolean isAutoMode, int aeLockValue) {
        if (isSupportAutoSwitchGimbal(GlobalVariable.gimbalType)) {
            return aeLockValue == 1 || !isAutoMode;
        } else if (isSupportAELockGimbal(GlobalVariable.gimbalType)) {
            return aeLockValue == 1;
        } else {
            return false;
        }
    }

    public static boolean isSupportAutoSwitchGimbal(GimbalType gimbalType) {
        return gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S200
                || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.GIMBAL_PWG01;
    }



    public static boolean isSupportMultiSDCardGimbal(GimbalType gimbalType) {
        return gimbalType == GimbalType.ByrdT_TMS
                || gimbalType == GimbalType.ByrdT_GTIR800
                || gimbalType == GimbalType.ByrT_IR_1K
                || gimbalType == GimbalType.Small_Double_Light
                || gimbalType == GimbalType.GIMBAL_PTL600
                || gimbalType == GimbalType.GIMBAL_PDL_300C;
    }

}
