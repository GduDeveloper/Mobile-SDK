package com.gdu.demo.flight.base;

/**
 * 错误提示
 */
public class ErrTipBean {

    /**
     *  1: 限高设置；2：限距设置；3：设置返航高度
     */
    private int setType;

    /**
     * 1: 飞行器未连接；2: 当前是系留模式；3：返航中；4：设置值超范围; 5: 设置限高值低于最小值 6: 设置限距值低于最小值  7:返航距离大于限制距离
     */
    private int type;

    public int getSetType() {
        return setType;
    }

    public void setSetType(int setType) {
        this.setType = setType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
