package com.gdu.demo.flight.base;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description 限距
 */
public class LimitDistanceBean {
    private boolean isOpen; //是否是开启限距

    private int distanceLimit; //限制值
    private boolean isSet; //是否是设置值

    private boolean isSuccess; //是否设置成功

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getDistanceLimit() {
        return distanceLimit;
    }

    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }
}
