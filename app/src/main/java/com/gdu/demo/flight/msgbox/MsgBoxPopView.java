package com.gdu.demo.flight.msgbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.gdu.demo.R;
import com.gdu.demo.databinding.LayoutMessageBoxListBinding;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.hms.bean.ErrorCodeDisplayForm;
import com.gdu.sdk.base.Diagnostics;

import java.util.ArrayList;
import java.util.List;

public class MsgBoxPopView extends PopupWindow {
    private Context mContext;
    private LayoutMessageBoxListBinding mViewBinding;

    private final List<Diagnostics> msgData = new ArrayList<>();
    private MessageBoxAdapter mBoxAdapter;
    private final int maxHeight = (int) ResourceUtils.getDimension(R.dimen.dp_127);

    public MsgBoxPopView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        initView();
        initData();
    }

    private void initView() {
        mViewBinding = LayoutMessageBoxListBinding.inflate(LayoutInflater.from(mContext));
        setContentView(mViewBinding.getRoot());
        setWidth((int) mContext.getResources().getDimension(R.dimen.dp_213));
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable(null, (Bitmap) null));
    }

    private void initData() {
        mBoxAdapter = new MessageBoxAdapter();
        mViewBinding.rvMsgBoxContent.setAdapter(mBoxAdapter);
    }

    public void updateMsgData(List<Diagnostics> data) {
        if (mBoxAdapter == null || data.isEmpty()) {
            dismiss();
            return;
        }
        onUpdateRecycler(data, false);
    }

    /**
     * 更新数据
     * */
    private void onUpdateRecycler(List<Diagnostics> data, Boolean showAdjust){
        boolean adjustHeight = msgData.size() != data.size();
        XLogger.APP.i("updateMsgData() dataSize = " + data.size()+",adjustHeight="+adjustHeight);
        msgData.clear();
        CollectionUtils.listAddAllAvoidNPE(msgData, data);
        mBoxAdapter.setList(msgData);
        if (adjustHeight) {
            adjustHeight(showAdjust);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }



    /**
     * 动态调整高度
     */
    private void adjustHeight(Boolean showAdjust) {
        // 测量内容实际需要的高度
        mViewBinding.rvMsgBoxContent.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int contentHeight = mViewBinding.rvMsgBoxContent.getMeasuredHeight();
        // 超过最大高度，设置为最大高度，并确保内容可滚动
        // 未超过最大高度，使用实际高度
        int finalHeight = Math.min(contentHeight, maxHeight);
        if (showAdjust){
            // 更新PopupWindow高度
            update(getWidth(), finalHeight);
        }else {
            setHeight(finalHeight);
        }
    }
}
