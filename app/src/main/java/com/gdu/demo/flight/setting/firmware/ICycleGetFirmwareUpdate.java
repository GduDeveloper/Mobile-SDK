package com.gdu.demo.flight.setting.firmware;

public interface ICycleGetFirmwareUpdate {
    /**
     * 更新中记录
     * @param status 0：默认；1:更新中；2：更新完成
     */
    void statusUpdate(int status);

    /**
     * 请求一轮后的界面更新
     */
    void updateVersionData();
}
