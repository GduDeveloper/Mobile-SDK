package com.gdu.demo.flight.msgbox;

import com.gdu.errreport.ErrCodeGrade;

public class MsgBoxBean {
    private String msgContent;
    private boolean isShow;
    private ErrCodeGrade warnLevel;

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public ErrCodeGrade getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(ErrCodeGrade warnLevel) {
        this.warnLevel = warnLevel;
    }
}
