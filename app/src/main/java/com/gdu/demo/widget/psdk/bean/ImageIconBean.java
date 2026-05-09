package com.gdu.demo.widget.psdk.bean;

import android.widget.ImageView;

import com.gdu.demo.widget.psdk.widget.CustomProgressLayout;
import com.gdu.demo.widget.psdk.widget.CustomRecyclerView;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/28
 */
public class ImageIconBean {

    int id;

    /**
     *  1 按钮 2 开关 3 范围条 4 列表 5 数字输入框 6 文字输入框
     */

    int type;
    boolean isSelected;
    ImageView imageView;

    String unSelectedIconName;
    String selectedIconName;

    CustomProgressLayout progressLayout;
    CustomRecyclerView customRecyclerView;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getUnSelectedIconName() {
        return unSelectedIconName;
    }

    public void setUnSelectedIconName(String unSelectedIconName) {
        this.unSelectedIconName = unSelectedIconName;
    }

    public String getSelectedIconName() {
        return selectedIconName;
    }

    public void setSelectedIconName(String selectedIconName) {
        this.selectedIconName = selectedIconName;
    }

    public CustomProgressLayout getProgressLayout() {
        return progressLayout;
    }

    public void setProgressLayout(CustomProgressLayout progressLayout) {
        this.progressLayout = progressLayout;
    }

    public CustomRecyclerView getCustomRecyclerView() {
        return customRecyclerView;
    }

    public void setCustomRecyclerView(CustomRecyclerView customRecyclerView) {
        this.customRecyclerView = customRecyclerView;
    }
}
