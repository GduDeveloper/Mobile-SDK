package com.gdu.demo.widget.rc;

/**
 * @Author: lixiqiang
 * @Date: 2022/5/11
 */
public class SettingMenuItem {

    private int menuType;
    private int iconRes;
    private String title;

    public SettingMenuItem(int menuType, int iconRes, String title) {
        this.menuType = menuType;
        this.iconRes = iconRes;
        this.title = title;
    }

    public int getMenuType() {
        return menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


