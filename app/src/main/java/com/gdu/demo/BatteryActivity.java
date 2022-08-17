package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.battery.BatteryState;
import com.gdu.common.error.GDUError;
import com.gdu.sdk.battery.GDUBattery;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * 电池组件测试
 */
public class BatteryActivity extends Activity implements View.OnClickListener {

    private GDUBattery mBattery;

    private TextView mBatteryStateTextView;
    private TextView mBatterySNTextView;
    private TextView mBatteryVersionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        initView();
        initData();
    }

    private void initView() {
        mBatteryStateTextView = findViewById(R.id.tv_show_msg);
        mBatterySNTextView = findViewById(R.id.sn_textview);
        mBatteryVersionTextView = findViewById(R.id.version_textview);
    }

    private void initData() {
        mBattery = SdkDemoApplication.getAircraftInstance().getBattery();
        if (mBattery != null) {
            mBattery.setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState state) {
                    showText(mBatteryStateTextView, state.getString());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.get_battery_sn_button:
                if (mBattery != null) {
                    mBattery.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                        @Override
                        public void onSuccess(String sn) {
                            showText(mBatterySNTextView, sn);
                        }

                        @Override
                        public void onFailure(GDUError var1) {
                            showText(mBatterySNTextView, "fail");
                        }
                    });
                }
                break;
            case R.id.get_battery_version_button:
                if (mBattery != null) {
                    mBattery.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                        @Override
                        public void onSuccess(String version) {
                            showText(mBatteryVersionTextView, version);
                        }

                        @Override
                        public void onFailure(GDUError var1) {
                            showText(mBatteryVersionTextView, "fail");
                        }
                    });
                }
                break;
        }
    }

    private void showText(final TextView textView, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }
}
