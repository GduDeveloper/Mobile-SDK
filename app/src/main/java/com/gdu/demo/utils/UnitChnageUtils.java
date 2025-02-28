package com.gdu.demo.utils;

import android.text.TextUtils;
import android.widget.TextView;

import com.gdu.util.FormatConfig;

import java.text.DecimalFormat;

/**
 * Created by yuhao on 2017/4/11.
 */
public class UnitChnageUtils {

    private SettingDao settingDao;

    public static final DecimalFormat format_zeo = FormatConfig.format_2;
    public static final DecimalFormat format_one = FormatConfig.format_7;
    public static final DecimalFormat format_two = FormatConfig.format_10;
    public static final DecimalFormat format_three = FormatConfig.format_12;

    /**
     * 公制单位和英制单位的显示
     *
     * @param value
     * @param textView
     */
    public void showUnit(int value, TextView textView) {
        String s = null;
        if (settingDao == null) {
            settingDao = SettingDao.getSingle();
        }
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            s = (int) ((value) / 0.3048f) + "ft";
        } else if (intValue == SettingDao.Unit_Merch) {
            s = value + "m";
        }

        if (value == -1) {
            s = "INF";
        }
        textView.setText(s);
    }

    public static String getUnitString(float value) {
        String s = null;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            s = ((int) ((value) / 0.3048f)) + "ft";
        } else if (intValue == SettingDao.Unit_Merch) {
            s = (value) + "m";
        }
        if (value == -1) {
            s = "INF";
        }
        return s;
    }


    /**
     * 米转英寸(如果单位是英制,公制则保持原样)
     * @param value 米的数值
     * @return
     */
    public static int getUnitValue(int value) {
        int changeValue = 0;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            changeValue = ((int) ((value) / 0.3048f));
        } else if (intValue == SettingDao.Unit_Merch) {
            changeValue = value;
        }
        return changeValue;
    }

    /**
     * 英尺转米(如果单位是公制则保持原样)
     * @param value 米或英尺的数值
     * @return
     */
    public static float getUnitValueFloat(float value) {
        float changeValue = 0;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            changeValue = ((int) ((value) / 0.3048f));
        } else if (intValue == SettingDao.Unit_Merch) {
            changeValue = value;
        }
        return changeValue;
    }

    /**
     * 英寸转米(如果单位是英制,公制则保持原样)
     * @param inchValue 米的数值
     * @return
     */
    public static int inch2m(int inchValue) {
        int changeValue = 0;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            changeValue = Math.round(inchValue * 0.3048f);
        } else if (intValue == SettingDao.Unit_Merch) {
            changeValue = inchValue;
        }
        return changeValue;
    }

    /**
     * 获取带有单位的秒速值
     * @param value 秒速值
     * @return 带有单位的秒速值
     */
    public static  String getUnitSpeedString(int value) {
        String s = null;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);

        if (intValue == SettingDao.Unit_Inch) {
            s = (int) ((value) / 0.3048f) + "ft/s";
        } else if (intValue == SettingDao.Unit_Merch) {
            s = value + "m/s";
        }
        if (value == -1) {
            s = "INF";
        }
        return s;
    }

    /**
     * 获取带有单位的时速值
     * @param value 时速值
     * @return 带有单位的时速值
     */
    public static String getHourUnitSpeedString(int value) {
        String s = null;
        SettingDao settingDao = SettingDao.getSingle();
        DecimalFormat mDecimalFormat = FormatConfig.format_5;
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            s =  mDecimalFormat.format((value) / 0.621371) + "mph";
        } else if (intValue == SettingDao.Unit_Merch) {
            s = value + "km/h";
        }
        if (value == -1) {
            s = "INF";
        }
        return s;
    }


    public static String getDecimalFormatUnit(float value) {

        float result = 0f;
        String unit = "";
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            result = (value / 0.3048f);
            unit = "ft";
        } else if (intValue == SettingDao.Unit_Merch) {
            result = value;
            unit = "m";
        }
        return FormatConfig.format_7.format(result) + unit;
    }

    public static String getDecimalFormatUnit(float value, DecimalFormat format) {
        float result = 0f;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            result = (value / 0.3048f);
        } else if (intValue == SettingDao.Unit_Merch) {
            result = value;
        }
        return format.format(result);
    }

    public static String getDecimalFormatSpeedUnit(float value) {
        float result = 0f;
        String unit = "";
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            result = (value / 0.3048f);
            unit = "ft/s";
        } else if (intValue == SettingDao.Unit_Merch) {
            result = value;
            unit = "m/s";
        }
        return FormatConfig.format_7.format(result) + unit;
    }

    public static String getSpeedUnit() {
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            return "ft/s";
        } else {
            return "m/s";
        }
    }

    public static String getUnit() {
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            return "ft";
        } else {
            return "m";
        }
    }

    /**
     * 获取有单位的值，进1000，单位进k 例：10130m -> 10.1km
     * @param val 原始值 单位m
     * @return
     */
    public static String getShortValue(float val){
        String value = "";
        if (isMetricUnit()) {
            if (val >= 1000){
                float v = val / 1000;
                value = FormatConfig.format_3.format(v) + "km";
            }else {
                value = FormatConfig.format_3.format(val) + "m";
            }
        }else {
            val = val / 0.3048f;
            if (val >= 1000){
                float v = val / 1000;
                value = FormatConfig.format_3.format(v) + "kft";
            }else {
                value = FormatConfig.format_3.format(val) + "ft";
            }
        }
        return value;
    }

    public static boolean isMetricUnit(){
        return TextUtils.equals(getUnit(),"m");
    }

    public static String getAreaUnit() {
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            return "ft²";
        } else {
            return "m²";
        }
    }

    public static int getUnitType() {
        SettingDao settingDao = SettingDao.getSingle();
        return settingDao.getIntValue(settingDao.Label_Unit, 2);
    }


    public static String getUnitArea(float area, DecimalFormat format) {
        float result = 0f;
        SettingDao settingDao = SettingDao.getSingle();
        int intValue = settingDao.getIntValue(settingDao.Label_Unit, 2);
        if (intValue == SettingDao.Unit_Inch) {
            result = (area / 0.0929f);
        } else {
            result = area;
        }
        return format.format(result);
    }
}
