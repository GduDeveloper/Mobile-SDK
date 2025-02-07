package com.gdu.demo.flight.setting.camera;

/**
 * Created by yuhao on 2017/4/13.
 */
public interface ICameraSetBiz {
    /**
     * 云台模式设置结果
     * @param type 结果类型(0: 成功; 1: 失败)
     */
    void setGimbalModeCallback(int type);
}
