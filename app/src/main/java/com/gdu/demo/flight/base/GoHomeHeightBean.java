package com.gdu.demo.flight.base;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description 限高
 */
public class GoHomeHeightBean {

    private int goHomeHeight; //返航高度值
    private boolean isSet; //是否是设置值

    private boolean isSetSuccess; //设置是否成功

    public int getGoHomeHeight() {
        return goHomeHeight;
    }

    public void setGoHomeHeight(int goHomeHeight) {
        this.goHomeHeight = goHomeHeight;
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    public boolean isSetSuccess() {
        return isSetSuccess;
    }

    public void setSetSuccess(boolean setSuccess) {
        isSetSuccess = setSuccess;
    }
}
