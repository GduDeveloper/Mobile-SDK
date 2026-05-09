package com.gdu.demo.widget.psdk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gdu.demo.R;
import com.gdu.demo.widget.psdk.bean.WidgetItemBean;
import com.gdu.sdk.psdk.PSDKCacheManager;

import java.util.List;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/22
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private int selectedPosition;
    private  Context mContext;

    private List<WidgetItemBean.ListItemBean> mData;


    public ItemAdapter(Context context) {
        this.mContext = context.getApplicationContext(); // 防止内存泄漏
    }

    public void setData(List<WidgetItemBean.ListItemBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (this.selectedPosition != position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }
    }

    // 创建 ViewHolder
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_custom_recycler_view, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder  holder, int position) {
        WidgetItemBean.ListItemBean itemBean = mData.get(position);
        if (itemBean == null) return;

        // 文字
        holder.tvName.setText(itemBean.getItem_name());

        // 文字颜色：选中/未选中
        if (position == selectedPosition) {
            holder.tvName.setTextColor(ContextCompat.getColor(mContext, R.color.color_ff4e00));
        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }

        // 图标：选中/未选中
        String iconName;
        if (position == selectedPosition) {
            iconName = itemBean.getIcon_file_set().getIcon_file_name_selected();
        } else {
            iconName = itemBean.getIcon_file_set().getIcon_file_name_unselected();
        }

        // 安全设置图标
        if (PSDKCacheManager.getInstance().getIconsMap() != null) {
            Bitmap bitmap = PSDKCacheManager.getInstance().getIconsMap().get(iconName);
            if (bitmap != null) {
                holder.ivIcon.setImageBitmap(bitmap);
            } else {
                holder.ivIcon.setImageResource(R.drawable.bg_ffffff_radius_3);
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.bg_ffffff_radius_3);
        }

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    // 原生 ViewHolder 静态内部类
    public  class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
