package com.gdu.demo.widget.live;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.R;


/**
 * Created by zhangzhilai on 2018/6/29.
 * 相机属性列表view  预览流分辨率，录像分辨率，照片分辨率
 */

public class CameraParamListView extends LinearLayout implements View.OnClickListener {

    private final Context mContext;
    private ListView mCameraParamListView;
    private String[] mDatas;
    private int mCurrentPosition;
    private OnCameraParamSelectListener mOnCameraParamSelectListener;
    private CameraParamAdapter mCameraParamAdapter;

    public CameraParamListView(Context context) {
        this(context, null);
    }

    public CameraParamListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraParamListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        setBackground(mContext.getDrawable(R.drawable.shape_3e505c_ffffff_r2));
        mCameraParamListView = new ListView(mContext);
        mCameraParamListView.setDividerHeight(0);
        mCameraParamListView.setVerticalScrollBarEnabled(false);
        mCameraParamAdapter = new CameraParamAdapter();
        mCameraParamListView.setAdapter(mCameraParamAdapter);
        addView(mCameraParamListView);
        mCameraParamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnCameraParamSelectListener.onCameraParamSelect(i, mDatas[i]);
                setVisibility(GONE);
            }
        });
    }

    public void setOnCameraParamSelectListener(OnCameraParamSelectListener onCameraParamSelectListener){
        mOnCameraParamSelectListener = onCameraParamSelectListener;
    }

    public void selectItem(int position){
        mCurrentPosition = position;
        mCameraParamAdapter.notifyDataSetChanged();
    }

    /**
     * 设置下拉列表内容
     *
     * @param datas
     */
//    public void setData(String[] datas) {
//        if (datas != null) {
//            mDatas = datas;
//            int length = datas.length;
//            mDataTextViews = new TextView[length];
//            for (int i = 0; i < mDatas.length; i++) {
//                String data = mDatas[i];
//                TextView textView = getTextView(i);
//                textView.setText(data);
//                mDataTextViews[i] = textView;
//                addView(textView);
//                textView.setTag(i);
//                textView.setOnClickListener(this);
//            }
//        }
//    }

    public void setData(String[] datas) {
        if (datas != null) {
            mDatas = datas;
            mCameraParamAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
//        mOnCameraParamSelectListener.onCameraParamSelect(position);
        mCurrentPosition = position;
    }

    public interface OnCameraParamSelectListener{
        void onCameraParamSelect(int position, String value);
    }

    public class CameraParamAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            if (mDatas == null) {
                return 0;
            }
            return mDatas.length;
        }

        @Override
        public Object getItem(int i) {
            return mDatas[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.view_camera_param_item, null);
                holder.mParamItemTextView = convertView.findViewById(R.id.camera_param_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int color;
            if (position == mCurrentPosition) {
                color = mContext.getResources().getColor(R.color.color_FF8400);
            } else {
                color = mContext.getResources().getColor(R.color.color_3E505C);
            }
            holder.mParamItemTextView.setText(mDatas[position]);
            holder.mParamItemTextView.setTextColor(color);
            return convertView;
        }

        class ViewHolder{
            TextView mParamItemTextView;
        }
    }

}
