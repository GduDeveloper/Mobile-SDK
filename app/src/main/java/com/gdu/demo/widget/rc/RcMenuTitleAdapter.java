package com.gdu.demo.widget.rc;

import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;

/**
 * @Author: lixiqiang
 * @Date: 2022/08/05
 */
public class RcMenuTitleAdapter extends BaseQuickAdapter<SettingMenuItem, BaseViewHolder> {

    private int selectedPosition;

    public RcMenuTitleAdapter() {
        super(R.layout.item_rc_custom_key_title_menu);
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
        holder.setImageResource(R.id.iv_menu, menuItem.getIconRes());
        holder.getView(R.id.bot_line).setVisibility(getItemPosition(menuItem) == selectedPosition ? View.VISIBLE : View.GONE);

    }
}

