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
import com.gdu.healthmanager.MessageBean;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

/**
 * 自定义布局，图片
 */
public class TextAdapter extends BannerAdapter<MessageBean, TextAdapter.ViewHolder> {
    private final Context mContext;

    public TextAdapter(Context context, List<MessageBean> mDatas) {
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
    public void onBindView(ViewHolder holder, MessageBean data, int position, int size) {
        holder.contentTv.setText(data.getMsg());
        switch (data.getAlarmLevel()) {
            case 1:
                holder.iconIv.setImageResource(0);
                break;

            case 2:
                holder.iconIv.setImageResource(R.drawable.icon_flight_alarm_level1);
                break;

            case 3:
                holder.iconIv.setImageResource(0);
                break;

            default:
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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