package com.gdu.demo.flight.pre.bean;

import androidx.annotation.StringRes;

/**
 * @author wuqb
 * @date 2025/1/10
 * @description 基础飞行检查状态属性
 */
public class BaseFlightStatusBean {
    public static final int STATUS_TYPE_MODE = 1; //飞行模式
    public static final int STATUS_TYPE_FLY_BATTERY = 2; //飞行器电池
    public static final int STATUS_TYPE_RC_BATTERY = 3; //遥控器电池
    public static final int STATUS_TYPE_RTK = 4; //RTK
    public static final int STATUS_TYPE_SDCARD = 5; //SD卡
    public static final int STATUS_TYPE_RC_CONTROL = 6; //遥控器控制
    public static final int STATUS_TYPE_SDR = 7; //图传状态

    /** 状态类型 */
    private int type;
    /** 需更新的内容位置 */
    private int position = -1;
    /** 状态标题名称 */
    private @StringRes int title;
    /** 显示内容 */
    private String content;
    /** 内容资源 */
    private @StringRes int contentStrId;
    /** 控制内容控件是否可用 */
    private boolean contentEnable;
    /** 内容显示颜色控制 */
    private int contentTextColor;
    /** 内容是否可选 */
    private boolean contentSelect;

    public BaseFlightStatusBean(){
    }
    public BaseFlightStatusBean(int type, @StringRes int title){
        this.type = type;
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getContentStrId() {
        return contentStrId;
    }

    public void setContentStrId(int contentStrId) {
        this.contentStrId = contentStrId;
    }

    public boolean isContentEnable() {
        return contentEnable;
    }

    public void setContentEnable(boolean contentEnable) {
        this.contentEnable = contentEnable;
    }

    public int getContentTextColor() {
        return contentTextColor;
    }

    public void setContentTextColor(int contentTextColor) {
        this.contentTextColor = contentTextColor;
    }

    public boolean isContentSelect() {
        return contentSelect;
    }

    public void setContentSelect(boolean contentSelect) {
        this.contentSelect = contentSelect;
    }
}
