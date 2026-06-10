package com.gdu.demo.flight.pre.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gdu.demo.R;
import com.gdu.sdk.base.Diagnostics;
import com.gdu.sdk.hms.WarningLevel;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;


/**
 * 自定义布局，图片
 */
public class TextAdapter extends BannerAdapter<Diagnostics, TextAdapter.ViewHolder> {
    private final Context mContext;

    public TextAdapter(Context context, List<Diagnostics> mDatas) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_notify_banner, parent,
                false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindView(ViewHolder holder, Diagnostics data, int position, int size) {
        holder.contentTv.setText(data.getCode()+"");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconIv;
        public TextView contentTv;
        public TextView lockBtnTv;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.iconIv = view.findViewById(R.id.iv_icon);
            this.contentTv = view.findViewById(R.id.tv_content);
            this.lockBtnTv = view.findViewById(R.id.tv_lockDetail);
        }
    }
}