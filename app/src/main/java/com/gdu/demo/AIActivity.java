package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gdu.api.GduAIManager;
import com.gdu.api.GduPlayView;
import com.gdu.api.listener.OnPreviewListener;
import com.gdu.drone.TargetMode;

import java.util.List;

public class AIActivity extends Activity {

    private GduAIManager mGduAIManager;
    private TextView mInfoTextView;

    private GduPlayView mGduPlayView;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);
        mInfoTextView = findViewById(R.id.detect_status_textview);
        mGduPlayView = findViewById(R.id.surface_view);
        initData();
    }

    private void initData() {
        mGduPlayView.init(new OnPreviewListener() {
            @Override
            public void onStartPreviewSucceed() {
            }

            @Override
            public void onStartPreviewFailed(int errorCode) {
            }

            @Override
            public void onStopPreviewSucceed() {
            }

            @Override
            public void onStopPreviewFailed(int errorCode) {
            }
        });
        mGduAIManager = new GduAIManager();
        mGduAIManager.setOnTargetDetectListener(new GduAIManager.OnTargetDetectListener() {
            @Override
            public void onTargetDetecting(List<TargetMode> list) {
                if (list != null) {
                    showMessage("test onTargetDetecting " + list.size());
                }
            }

            @Override
            public void onTargetDetectFailed(int i) {
                showMessage("test onTargetDetectFailed " + i);
            }

            @Override
            public void onTargetDetectStart(boolean b) {
                showMessage("test onTargetDetectStart " + b);
            }

            @Override
            public void onTargetDetectFinished(boolean b) {
                showMessage("test onTargetDetectFinished " + b);
            }
        });
    }

    private void showMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInfoTextView.setText(message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGduPlayView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGduPlayView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGduPlayView.onDestroy();
    }

    public void startDetect(View view) {
        mGduAIManager.startTargetDetect();
    }

    public void stopDetect(View view) {
        mGduAIManager.stopTargetDetect();
    }

    public void startPreview(View view) {
        mGduPlayView.startPreview();
    }
}
