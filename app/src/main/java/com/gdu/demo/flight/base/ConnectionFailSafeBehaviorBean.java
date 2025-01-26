package com.gdu.demo.flight.base;

/**
 * 失联行为
 */
public class ConnectionFailSafeBehaviorBean {

    private int position;

    private boolean isSet;

    private boolean isSuccess;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }
}
