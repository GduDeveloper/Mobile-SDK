package com.gdu.demo.flight.msgbox;

import com.gdu.beans.WarnBean;

import java.util.HashMap;

/**
 * @author wuqb
 * @date 2025/1/21
 * @description TODO
 */
public interface MsgBoxViewCallBack {

    void updateTitleTvTxt(String title);

    void updateTitleTVColor(int txtColor);

    void updateHeadViewBg(int resId);

    void updateWarnList(HashMap<Long, WarnBean> warnList);
}
