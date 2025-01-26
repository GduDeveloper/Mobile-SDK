package com.gdu.demo.flight.base;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description 限高
 */
public class LimitHeightBean {
    private boolean isOpen; //是否是开启限高
    private int heightLimit; //限制值
    private boolean isSet; //是否是设置值

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getHeightLimit() {
        return heightLimit;
    }

    public void setHeightLimit(int heightLimit) {
        this.heightLimit = heightLimit;
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }
}
