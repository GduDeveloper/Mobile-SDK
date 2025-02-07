package com.gdu.demo.flight.setting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.demo.utils.ScreenUtils;
import com.gdu.demo.widget.rc.SettingMenuItem;

/**
 * @Author: lixiqiang
 * @Date: 2022/5/11
 */
public class SettingLeftAdapter extends BaseQuickAdapter<SettingMenuItem, BaseViewHolder> {

    private int selectedPosition;

    public SettingLeftAdapter() {
        super(R.layout.item_setting_left);
    }

    public void setSelectPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, SettingMenuItem menuItem) {
        if (menuItem == null) {
            return;
        }
        View rootView = holder.getView(R.id.root_view);
        Context context = rootView.getContext();
        ViewGroup.LayoutParams params = rootView.getLayoutParams();
        int height = Math.min(ScreenUtils.getScreenHeight(context), ScreenUtils.getScreenWidth(context));
        params.height = height / getDefItemCount();
        rootView.setLayoutParams(params);
        holder.setImageResource(R.id.iv_setting_icon, menuItem.getIconRes());
        holder.getView(R.id.iv_setting_icon).setSelected(getItemPosition(menuItem) == selectedPosition);

        if (getItemPosition(menuItem) == selectedPosition) {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_FF4E00));
        } else {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }
    }
}

