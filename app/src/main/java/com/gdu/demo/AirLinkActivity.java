package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.sdk.airlink.GDUAirLink;
import com.gdu.sdk.airlink.SignalQualityCallback;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * 图传测试
 */
public class AirLinkActivity extends Activity implements View.OnClickListener {

    private GDUAirLink mGDUAirLink;
    private TextView mAirLinkSignalQualityTextView;
    private TextView mAirLinkVersionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airlink);
        initView();
        initData();
    }

    private void initData() {
        mGDUAirLink = (GDUAirLink) SdkDemoApplication.getAircraftInstance().getAirLink();
        mGDUAirLink.setUplinkSignalQualityCallback(new SignalQualityCallback() {
            @Override
            public void onUpdate(int uplinkQuality, int downlinkQuality) {
                showText(mAirLinkSignalQualityTextView, "上行信号质量 " + uplinkQuality + " 下行信号质量 " + downlinkQuality);
            }
        });
    }

    private void initView() {
        mAirLinkSignalQualityTextView = findViewById(R.id.tv_show_msg);
        mAirLinkVersionTextView = findViewById(R.id.version_textview);
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

    private void showText(final TextView textView, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.get_airlink_version_button:
                if (mGDUAirLink != null) {
                    mGDUAirLink.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                        @Override
                        public void onSuccess(String version) {
                            showText(mAirLinkVersionTextView, version);
                        }

                        @Override
                        public void onFailure(GDUError var1) {
                            showText(mAirLinkVersionTextView, "fail");
                        }
                    });
                }
                break;
        }
    }
}
