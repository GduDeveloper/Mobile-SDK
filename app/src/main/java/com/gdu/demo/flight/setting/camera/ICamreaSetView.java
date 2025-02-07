package com.gdu.demo.flight.setting.camera;

/**
 * Created by yuhao on 2017/4/13.
 */
public interface ICamreaSetView {
    /**
     * 设置云台模式结果返回
     * @param type 结果类型: 0:成功; 1:失败
     */
    void setGimbalModeResult(int type);

    /*******************
     * 眉头上的Back键倍按下
     */
    void onBackPress();
}
