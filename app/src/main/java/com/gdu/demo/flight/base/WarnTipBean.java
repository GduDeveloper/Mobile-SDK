package com.gdu.demo.flight.base;

/**
 * 告警
 */
public class WarnTipBean {


    /**
     *  1: 限高设置；2：限距设置；3：设置返航高度
     */
    private int type;

    /**
     * 1: dialog 2: toast
     */
    private int warnType;

    /**
     * 告警值
     */
    private int intValue;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWarnType() {
        return warnType;
    }

    public void setWarnType(int warnType) {
        this.warnType = warnType;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}
