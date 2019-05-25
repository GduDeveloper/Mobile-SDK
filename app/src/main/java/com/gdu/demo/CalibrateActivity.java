package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gdu.api.CalibrationGeomagneticManager;
import com.gdu.drone.CalibrationState;

/**
 * Created by zhangzhilai on 2018/8/9.
 */

public class CalibrateActivity extends Activity {

    private TextView mCalibrateStatusTextView;
    private CalibrationGeomagneticManager mCalibrationGeomagneticManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        mCalibrateStatusTextView = (TextView) findViewById(R.id.calibrate_status_textview);
        mCalibrationGeomagneticManager = new CalibrationGeomagneticManager();
        mCalibrationGeomagneticManager.setOnCalibrateGeomagneticListener(new CalibrationGeomagneticManager.OnCalibrateGeomagneticListener() {
            @Override
            public void onCalibrateStatusChanged(CalibrationState calibrationState) {
                mCalibrateStatusTextView.setText(calibrationState.getValue());
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

    public void startCalibrate(View view) {
        mCalibrationGeomagneticManager.startCalibrate();
    }

    public void stopCalibrate(View view) {
        mCalibrationGeomagneticManager.stopCalibrate();
    }
}
