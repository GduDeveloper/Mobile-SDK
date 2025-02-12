package com.gdu.demo.flight.msgbox;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.errreport.ErrCodeGrade;

public class MessageBoxAdapter extends BaseQuickAdapter<MsgBoxBean, BaseViewHolder> {


    public MessageBoxAdapter() {
        super(R.layout.item_message_box);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, MsgBoxBean bean) {
        if(bean == null) {
            return;
        }
        TextView tvContent = holder.getView(R.id.tvContent);
        tvContent.setText(bean.getMsgContent());
        tvContent.setTag(bean);

        if(bean.getWarnLevel() == ErrCodeGrade.ErrCodeGrade_1) {
            holder.setImageResource(R.id.tipIcon, R.drawable.icon_tip_warn_2);
        } else {
            holder.setImageResource(R.id.tipIcon, R.drawable.icon_tip_warn_1);
        }
    }
}
