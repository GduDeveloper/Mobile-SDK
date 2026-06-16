package com.gdu.demo.flight.msgbox;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.sdk.base.Diagnostics;
import com.gdu.sdk.hms.WarningLevel;

public class MessageBoxAdapter extends BaseQuickAdapter<Diagnostics, BaseViewHolder> {


    public MessageBoxAdapter() {
        super(R.layout.item_message_box);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, Diagnostics bean) {
        if(bean == null) {
            return;
        }
        TextView tvContent = holder.getView(R.id.tvContent);
        tvContent.setText(bean.getReason());
        tvContent.setTag(bean);

        if(bean.getHealthInformation().getWarningLevel() == WarningLevel.WARNING) {
            holder.setImageResource(R.id.tipIcon, R.drawable.icon_tip_warn_2);
        } else {
            holder.setImageResource(R.id.tipIcon, R.drawable.icon_tip_warn_1);
        }
    }
}
