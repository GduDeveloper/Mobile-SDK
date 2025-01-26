package com.gdu.demo.flight.base;

/**
 * 四向避障
 */
public class HorizontalVisionObstacleAvoidanceBean {

    private boolean isSet;

    private boolean isSuccess;

    private boolean enable;

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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
