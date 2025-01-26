package com.gdu.demo.utils;

import android.content.SharedPreferences;

import com.gdu.demo.SdkDemoApplication;

/**
 * 设置相关缓存数据
 */
public class SettingDao
{
    private final String Sp_Name = "SettingDao";

    /**
     * 目标识别地形高度
     */
    public final String Label_Altitude = "Label_Altitude";

    /**
     * <p>参数单位：英制</p>
     */
    public static final int Unit_Inch = 0x01;

    /**
     * <p>参数单位：公制</p>
     */
    public static final int Unit_Merch = 0x02;

    /**
     * <p>是否能使用wifi网络</p>
     */
    public static String Label_UserPhoneWifi = "Label_UserPhoneWifi";

    /**
     * <p>参数单位</p>
     */
    public final String Label_Unit ="Label_Unit";
    /**
     * <p>参数单位</p>
     */
    public final String Label_Environment ="Label_environment";

    /**
     * <p>label_grid 9宫格</p>
     */
    public final String ALOHALabel_Grid = "Aloha_Label_Grid";
    public final String ZORRORLabel_Grid = "zorro_Label_Grid";

    /**
     * 限高、距 开关
     */
    public final String ALOHALabel_DISTANCE_HEIGHT = "Aloha_distance_height";
    public final String ZORRORLabel_DISTANCE_HEIGHT = "zorro_distance_height";

    /**
     * <p>是否是第一次使用</p>
     */
    public final String Label_FirstUsed = "Label_FirstUsed";

    /**
     * <p>飞行设置</p>
     */
    public final String Label_FlySpeed = "Label_FlySpeed";
    /**
     * <p>返航高度</p>
     */
    public final String Label_backHeight = "Label_backHeight";

    /**
     * <p>视频尺寸</p>
     */
    public final String Label_VideoSize = "Label_VideoSize";

    /**
     * <p>闪光灯</p>
     */
    public final String Label_FlashLight = "Label_FlashLight";

    /**
     * <p>延迟拍照的时间</p>
     */
    public final String Label_delayTime = "Label_delayTime";

    /**
     * 虚拟操作杆
     */
//    public final String ZORRO_APP_TELECONTROL = "zorro_app_telecontrol";

    /**
     * 遥控</p>
     */
//    public final String ZORRO_TELECONTROL = "zorro_telecontrol";

    /******ron******
     *  保存本地的控制手
     ***************/
    public final String Label_ControlHand = "Label_ControlHand";

    /**
     * 遥控器连接
     */
    public final String Label_showHTDIS = "Label_showHTDIS";

    /**
     * zorro返航高度
     */
    public final String ZORRO_HEIGHT = "zorro_height";

    /***********
     * 是否显示 避障雷达信息
     */
    public final String Label_obstacle = "Label_obstacle";

    /***************
     * 是否启动手势
     */
    public final String Label_gesture = "Label_gesture";

    private final SharedPreferences sharedPreferences;

    private SettingDao()
    {
        sharedPreferences = SdkDemoApplication.getSingleApp().getSharedPreferences(Sp_Name,0);
    }

    public static SettingDao settingDao;

    public static SettingDao getSingle()
    {
        if(settingDao == null)
        {
            settingDao = new SettingDao();
        }
        return settingDao;
    }

    /**
     * <p>保存布尔类型的变量</p>
     * @param key
     * @param value
     */
    public void saveBooleanValue(String key,boolean value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }


    /**
     * <p>保存Int类型的变量</p>
     * @param key
     * @param value
     */
    public void saveIntValue(String key,int value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }



    /**
     * <p>保存String类型的变量</p>
     * @param key
     * @param value
     */
    public void saveStringValue(String key,String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * <p>获取 boolean类型的变量</p>
     * @param key
     * @param defaultv 默认值
     * @return
     */
    public boolean getBooleanValue(String key,boolean defaultv)
    {
       return sharedPreferences.getBoolean(key,defaultv);
    }

    /**
     * <p>获取 int类型的变量</p>
     * @param key
     * @param defaultv 默认值
     * @return
     */
    public int getIntValue(String key,int defaultv)
    {
       return sharedPreferences.getInt(key, defaultv);
    }

    /**
     * <p>获取 String类型的变量</p>
     * @param key
     * @return
     */
    public String getStringValue(String key)
    {
       return sharedPreferences.getString(key, "");
    }

    /**
     * <p>获取 String类型的变量</p>
     * @param key
     * @return
     */
    public String getStringValue(String key,String defaultStr)
    {
        return sharedPreferences.getString(key,defaultStr);
    }

}
