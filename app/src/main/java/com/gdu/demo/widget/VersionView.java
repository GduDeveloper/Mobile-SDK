package com.gdu.demo.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;


/**
 * 丢包测试view
 */
public class VersionView extends RelativeLayout implements View.OnClickListener {

    private final Context mContext;
    private TextView mFirmwareVersionTextView;
    private TextView mCurrentVersionTextView;
    private TextView mNewVersionTextView;

    private Handler mHandler;


    public VersionView(Context context) {
        this(context, null);
    }

    public VersionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VersionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initHandler();
        initListener();
    }

    private void initListener() {
        mNewVersionTextView.setOnClickListener(this);
        mCurrentVersionTextView.setOnClickListener(this);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_version, this);
        mFirmwareVersionTextView = findViewById(R.id.firmware_version_textview);
        mCurrentVersionTextView = findViewById(R.id.tv_current_version);
        mNewVersionTextView = findViewById(R.id.tv_new_version);
    }

    /**
     * 设置固件名称
     * @param name
     */
    public void setFirmwareName(String name){
        mFirmwareVersionTextView.setText(name);
    }

    /**
     * 设置固件新版本
     */
    public void setNewVersion(String version){
        mNewVersionTextView.setText(version);
    }

    /**
     * 设置固件当前版本
     */
    public void setCurrentVersion(String version){
        mCurrentVersionTextView.setText(version + " ");
    }


    private void initHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){

                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_new_version:
                break;
            case R.id.tv_current_version:
                break;
        }
    }
}
