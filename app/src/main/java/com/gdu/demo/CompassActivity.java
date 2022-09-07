package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.flightcontroller.CompassCalibrationState;
import com.gdu.sdk.flightcontroller.Compass;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * 指南针校准
 */
public class CompassActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private Compass mCompass;
    private TextView mCalibrationStateTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_compass);
        mCalibrationStateTextView = findViewById(R.id.calibration_state_info_textview);
        initData();
    }

    private void initData() {
        mCompass = SdkDemoApplication.getAircraftInstance().getFlightController().getCompass();
        mCompass.setCalibrationStateCallback(new CompassCalibrationState.Callback() {
            @Override
            public void onUpdate(CompassCalibrationState state) {
                showText(mCalibrationStateTextView, state.name());
            }
        });
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
            case R.id.start_calibration_button:
                mCompass.startCalibration(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("指南针校准开始成功");
                        } else {
                            toastText("指南针校准开始失败");
                        }
                    }
                });
                break;
            case R.id.stop_calibration_button:
                mCompass.stopCalibration(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("指南针校准停止成功");
                        } else {
                            toastText("指南针校准停止失败");
                        }
                    }
                });
                break;
        }
    }

    private void showText(final TextView textView, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
                textView.setText(content);
            }
        });
    }

    private void toastText(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
