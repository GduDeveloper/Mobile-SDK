package com.gdu.demo.flight.msgbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.gdu.demo.R;
import com.gdu.demo.databinding.LayoutMessageBoxListBinding;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.logger.MyLogUtils;

import java.util.ArrayList;
import java.util.List;
/**
 * 消息盒子弹窗
 * */
public class MsgBoxPopView extends PopupWindow {
    //弹窗所在Activity上下文
    private Context mContext;
    //弹窗布局Binding
    private LayoutMessageBoxListBinding mViewBinding;

    private final List<MsgBoxBean> msgData = new ArrayList<>();
    private MessageBoxAdapter mBoxAdapter;

    public MsgBoxPopView(Context context, View view) {
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
        setWidth((int) mContext.getResources().getDimension(R.dimen.dp_188));
        setHeight((int) mContext.getResources().getDimension(R.dimen.dp_127));
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable(null, (Bitmap) null));
    }

    private void initData() {
        mBoxAdapter = new MessageBoxAdapter();
        mViewBinding.rvMsgBoxContent.setAdapter(mBoxAdapter);
    }

    public void updateMsgData(List<MsgBoxBean> data) {
        MyLogUtils.i("updateMsgData() dataSize = " + data.size());
        msgData.clear();
        CommonUtils.listAddAllAvoidNPE(msgData, data);
        if (mBoxAdapter == null) {
            return;
        }
        mBoxAdapter.setNewInstance(msgData);
    }
}
