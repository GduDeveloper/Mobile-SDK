package com.gdu.demo.flight.setting.firmware;


import com.gdu.drone.FirmwareType;

/**
 * Created by zhangzhilai on 2018/1/10.
 */

public interface IFirmwareVersion {

    /**
     * 获取飞行器所有固件版本信息
     */
    void getFlyFirmwareVersion();

    /**
     * 清空飞机端历史版本
     */
    void initFlyVersionCache();

    void initFlyVersionCacheByType(FirmwareType firmwareType);

    /**
     * 注册固件版本获取状态监听
     * @param onFirmwareGetListener 状态监听对象
     */
    void setOnFirmwareGetListener(OnFirmwareVersionGetListener onFirmwareGetListener);

    /**
     * 注销固件版本获取状态监听
     * @param onFirmwareGetListener 状态监听对象
     */
    void unRegisterListener(OnFirmwareVersionGetListener onFirmwareGetListener);

    /**
     * 取消固件版本获取逻辑
     */
    void dispose();

    interface OnFirmwareVersionGetListener{
        /**
         * 正在获取固件版本中
         * @param status 1: 正在获取固件版本中
         */
        void onFirmwareStatusCallback(int status);

        /**
         * 版本请求流程完成
         */
        void onVersionReqEnd();
    }
}
