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
 * 装网模式下飞机列表
 */
public class NetworkingDroneAdapter extends BaseAdapter {

    private final Context mContext;
    private List<IMChildPointInfo> mIMChildPointInfoList;

    public NetworkingDroneAdapter(Context context) {
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
            view = View.inflate(mContext, R.layout.item_networking_drone, null);
            viewHolder.droneLayout = view.findViewById(R.id.drone_layout);
            viewHolder.droneNameTextView = view.findViewById(R.id.drone_name_textview);
            viewHolder.typeImageView = view.findViewById(R.id.type_imageview);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }
        IMChildPointInfo info = mIMChildPointInfoList.get(i);
        if (info.id == 0) {
            viewHolder.typeImageView.setImageResource(R.drawable.icon_ap);
        } else {
            viewHolder.typeImageView.setImageResource(R.drawable.icon_sta);
        }
        viewHolder.droneNameTextView.setText(mContext.getString(R.string.aircraft) + (i + 1));
        if (info.connectStatus == 1) {
            viewHolder.droneLayout.setBackgroundResource(R.drawable.shape_networking_online);
            viewHolder.droneNameTextView.setTextColor(mContext.getColor(R.color.color_09C73A));
        } else if(info.connectStatus == 0) {
            viewHolder.droneLayout.setBackgroundResource(R.drawable.shape_networking_offline);
            viewHolder.droneNameTextView.setTextColor(mContext.getColor(R.color.color_C8D2D6));
        } else {
            viewHolder.droneLayout.setBackgroundResource(R.drawable.shape_networking_offline);
        }
        return view;
    }
    class ViewHolder {
        private RelativeLayout droneLayout;
        private TextView droneNameTextView;
        private ImageView typeImageView;
    }

    interface OnDeviceListener {
        void onSpeedChoose();
    }
}



