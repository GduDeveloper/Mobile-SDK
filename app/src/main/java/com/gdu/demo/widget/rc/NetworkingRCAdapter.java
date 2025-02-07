package com.gdu.demo.widget.rc;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;
import com.gdu.remotecontroller.IMChildPointInfo;

import java.util.List;

/**
 * 装网模式下遥控器列表
 */
public class NetworkingRCAdapter extends BaseAdapter {

    private final Context mContext;
    private List<IMChildPointInfo> mIMChildPointInfoList;

    public NetworkingRCAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<IMChildPointInfo> imChildPointInfoList) {
        mIMChildPointInfoList = imChildPointInfoList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mIMChildPointInfoList == null) {
            return 0;
        }
        return mIMChildPointInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        if (mIMChildPointInfoList == null) {
            return null;
        }
        return mIMChildPointInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = View.inflate(mContext, R.layout.item_networking_rc, null);
            viewHolder.rcLayout = view.findViewById(R.id.rc_layout);
            viewHolder.rcNameTextView = view.findViewById(R.id.rc_name_textview);
            viewHolder.typeImageView = view.findViewById(R.id.type_imageview);
            viewHolder.currentDeviceImageView = view.findViewById(R.id.current_device_imageview);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }
        IMChildPointInfo info = mIMChildPointInfoList.get(i);
        viewHolder.rcNameTextView.setText(mContext.getString(R.string.remote_control) + (i + 1));
        if (info.type == 0) {
            viewHolder.typeImageView.setImageAlpha(R.drawable.icon_ap);
        } else {
            viewHolder.typeImageView.setImageAlpha(R.drawable.icon_sta);
        }
        if (info.connectStatus == 1) {
            viewHolder.rcLayout.setBackgroundResource(R.drawable.shape_networking_rc_online);
            viewHolder.rcNameTextView.setTextColor(mContext.getColor(R.color.color_09C73A));
        } else if(info.connectStatus == 0) {
            viewHolder.rcLayout.setBackgroundResource(R.drawable.shape_networking_rc_offline);
            viewHolder.rcNameTextView.setTextColor(mContext.getColor(R.color.color_C8D2D6));
        } else {
            viewHolder.rcLayout.setBackgroundResource(R.drawable.shape_networking_rc_offline);
        }
        return view;
    }
    class ViewHolder {
        private RelativeLayout rcLayout;
        private TextView rcNameTextView;
        private ImageView typeImageView;
        private ImageView currentDeviceImageView;
    }

    interface OnDeviceListener {
        void onSpeedChoose();
    }
}



