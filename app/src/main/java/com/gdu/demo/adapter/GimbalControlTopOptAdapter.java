package com.gdu.demo.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.demo.widgetlist.camerapara.GimbalControlTopOptBean;

import java.util.List;


public class GimbalControlTopOptAdapter extends BaseQuickAdapter<GimbalControlTopOptBean, BaseViewHolder> {


    public GimbalControlTopOptAdapter(List<GimbalControlTopOptBean> data) {
        super(R.layout.item_top_camera_set, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, GimbalControlTopOptBean bean) {
        TextView contentTv = baseViewHolder.getView(R.id.tvItemContent);
        contentTv.setText(bean.getName());
        contentTv.setSelected(bean.isSelected());
    }
}
