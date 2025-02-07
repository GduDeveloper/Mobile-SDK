package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.R;

import cc.taylorzhang.singleclick.SingleClickUtil;

//加载超时组件
public class PageLoadingView extends LinearLayout {

    private static final String TAG = "PageLoadingView";
    private LinearLayout llLoadingTimeout;
    private TextView tvRefresh;

    private Context mContext;
    private OnRefreshClickListener listener;

    public PageLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PageLoadingView(Context context, Context mContext) {
        super(context);
        init(context);
    }

    public PageLoadingView(Context context, @Nullable AttributeSet attrs, Context mContext) {
        super(context, attrs);
        init(context);
    }

    public PageLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, Context mContext) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.view_page_loading, this);
        llLoadingTimeout = findViewById(R.id.ll_loading_timeout);
        tvRefresh = findViewById(R.id.tv_refresh);
        tvRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleClickUtil.determineTriggerSingleClick(v, 500, v1 -> {
                    if (listener != null) {
                        listener.onClickRefresh();
                    }
                    llLoadingTimeout.setVisibility(View.GONE);
                });
            }
        });
    }

    public void setClickRefreshListener(OnRefreshClickListener listener) {
        this.listener = listener;
    }

    //是否显示加载超时样式
    public void showLoadingTimeout(boolean show) {
        llLoadingTimeout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public interface OnRefreshClickListener {
        void onClickRefresh();
    }
}
