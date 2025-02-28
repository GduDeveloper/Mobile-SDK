package com.gdu.demo.widgetlist.camerapara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gdu.demo.R;
import com.gdu.demo.adapter.GimbalControlTopOptAdapter;
import com.gdu.demo.databinding.LayoutCameraParamSetPopBinding;
import com.gdu.sdk.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class GimbalControlTopOptItemPopView extends PopupWindow {
    private Context mContext;
    private LayoutCameraParamSetPopBinding mViewBinding;
    private int type;

    private final List<GimbalControlTopOptBean> mBeanList = new ArrayList<>();
    private GimbalControlTopOptAdapter mGimbalControlTopOptAdapter;

    private OnPopViewItemClickListener listener;

    public GimbalControlTopOptItemPopView(Context context, int type) {
        super(context);
        init(context);
        this.type = type;
    }

    private void init(Context context) {
        mContext = context;
        initView();
        initData();
    }

    private void initView() {
        mViewBinding = LayoutCameraParamSetPopBinding.inflate(LayoutInflater.from(mContext));
        setContentView(mViewBinding.getRoot());
        setWidth((int) mContext.getResources().getDimension(R.dimen.dp_30));
        setHeight((int) mContext.getResources().getDimension(R.dimen.dp_150));
        setFocusable(true);// 设置弹出窗口可
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable(null, (Bitmap) null));
    }

    private void initData() {
        mGimbalControlTopOptAdapter = new GimbalControlTopOptAdapter(mBeanList);
        mViewBinding.rvCameraTopOpt.setAdapter(mGimbalControlTopOptAdapter);
        mGimbalControlTopOptAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (listener != null) {
                listener.onItemClick(adapter,type, position);
            }
        });
    }


    public void updateData(List<GimbalControlTopOptBean> data, int selectIndex) {
        Log.i("updateData","updateData() dataSize = " + data.size() + "; selectIndex = " + selectIndex);
        mBeanList.clear();
        CommonUtils.listAddAllAvoidNPE(mBeanList, data);
        if (mGimbalControlTopOptAdapter == null || CommonUtils.isEmptyList(mBeanList)) {
            return;
        }
        for (int i = 0; i < mBeanList.size(); i++) {
            mBeanList.get(i).setSelected(i == selectIndex);
        }
        mViewBinding.rvCameraTopOpt.smoothScrollToPosition(selectIndex);
        mGimbalControlTopOptAdapter.notifyDataSetChanged();
    }

    public void setListener(OnPopViewItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnPopViewItemClickListener{
        void onItemClick(BaseQuickAdapter adapter, int type, int position);
    }



}
