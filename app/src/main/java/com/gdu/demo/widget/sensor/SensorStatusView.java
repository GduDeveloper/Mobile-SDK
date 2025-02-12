package com.gdu.demo.widget.sensor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.flight.calibration.CompassCalibrationHelper;
import com.gdu.demo.flight.calibration.IMUCalibrationActivity;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

public class SensorStatusView extends FrameLayout implements View.OnClickListener {


    private SegmentTabLayout mTabSensor;
    private final Context mContext;
    private View vIMU;
    private View vCompass;
    //------------------------------------展示IMU数值的TextView----------------------------------
    private TextView mTvImu1AccelerometerValue;
    private TextView mTvImu2AccelerometerValue;
    private TextView mTvImu1GyroValue;
    private TextView mTvImu2GyroValue;
    private ProgressBar mPbImu1AccelerometerValue;
    private ProgressBar mPbImu2AccelerometerValue;
    private ProgressBar mPbImu1GyroValue;
    private ProgressBar mPbImu2GyroValue;
    private TextView mTvCompass1DisturbanceValue;
    private TextView mTvCompass2DisturbanceValue;
    private TextView tv_compass1_label;
    private TextView tv_compass2_label;
    private ProgressBar mPbCompass1DisturbanceValue;
    private ProgressBar mPbCompass2DisturbanceValue;
    private AppCompatButton mBtnCalibrateIMU;
    private AppCompatButton mBtnCalibrateCompass;

    public SensorStatusView(@NonNull Context context) {
        this(context, null);
    }

    public SensorStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_sensor_status, this, true);
        initView();
        initListener();
    }

    private void initView() {
        String[] titles = new String[]{
                mContext.getString(R.string.imu),
                mContext.getString(R.string.compass)
        };
        mTabSensor = findViewById(R.id.tab_sensor);
        vIMU = findViewById(R.id.v_imu);
        vCompass = findViewById(R.id.v_compass);
        mTabSensor.setTabData(titles);
        initIMUView();
        initCompassView();
        initTimer();
    }

    private void initListener() {
        mTabSensor.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vIMU.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                vCompass.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        mBtnCalibrateIMU.setOnClickListener(this);
        mBtnCalibrateCompass.setOnClickListener(this);
    }

    private void initIMUView() {
        //------------------------------------展示IMU数值的TextView----------------------------------
        mTvImu1AccelerometerValue = vIMU.findViewById(R.id.tv_imu1_accelerometer_value);
        mTvImu2AccelerometerValue = vIMU.findViewById(R.id.tv_imu2_accelerometer_value);
        mTvImu1GyroValue = vIMU.findViewById(R.id.tv_imu1_gyro_value);
        mTvImu2GyroValue = vIMU.findViewById(R.id.tv_imu2_gyro_value);

        //----------------------------------展示IMU数值的ProgressBar----------------------------------
        mPbImu1AccelerometerValue = vIMU.findViewById(R.id.pb_imu1_accelerometer_value);
        mPbImu2AccelerometerValue = vIMU.findViewById(R.id.pb_imu2_accelerometer_value);
        mPbImu1GyroValue = vIMU.findViewById(R.id.pb_imu1_gyro_value);
        mPbImu2GyroValue = vIMU.findViewById(R.id.pb_imu2_gyro_value);

        mBtnCalibrateIMU = vIMU.findViewById(R.id.btn_calibrate_imu);

    }

    private void initCompassView() {
        //------------------------------------展示指南针数值的TextView---------------------------------
        mTvCompass1DisturbanceValue = vCompass.findViewById(R.id.tv_compass1_disturbance_value);
        mTvCompass2DisturbanceValue = vCompass.findViewById(R.id.tv_compass2_disturbance_value);
        tv_compass1_label = vCompass.findViewById(R.id.tv_compass1_label);
        tv_compass2_label = vCompass.findViewById(R.id.tv_compass2_label);
        tv_compass1_label.setText(mContext.getString(R.string.compass) + "1");
        tv_compass2_label.setText(mContext.getString(R.string.compass) + "2");
        //---------------------------------展示指南针数值的ProgressBar---------------------------------
        mPbCompass1DisturbanceValue = vCompass.findViewById(R.id.pb_compass1_disturbance_value);
        mPbCompass2DisturbanceValue = vCompass.findViewById(R.id.pb_compass2_disturbance_value);
        mBtnCalibrateCompass = vCompass.findViewById(R.id.btn_calibrate_compass);
    }

    /**
     * 初始化定时任务
     */
    private void initTimer() {
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .to(RxLife.toMain(this, true))
                .subscribe(aLong -> {
                    setIMUValue();
                    setCompassValue();

                }, throwable -> {
                    MyLogUtils.e("获取传感器状态失败", throwable);
                });
    }

    /**
     * 设置IMU的状态值
     */
    @SuppressLint("DefaultLocale")
    private void setIMUValue() {
        mTvImu1AccelerometerValue.setText(String.format("%.3f", GlobalVariable.imu1Accelerometer));
        mTvImu2AccelerometerValue.setText(String.format("%.3f", GlobalVariable.imu2Accelerometer));
        mTvImu1GyroValue.setText(String.format("%.3f", GlobalVariable.imu1Gyro));
        mTvImu2GyroValue.setText(String.format("%.3f", GlobalVariable.imu2Gyro));
        mPbImu1AccelerometerValue.setProgress((int) (GlobalVariable.imu1Accelerometer * 100));
        mPbImu2AccelerometerValue.setProgress((int) (GlobalVariable.imu2Accelerometer * 100));
        mPbImu1GyroValue.setProgress((int) (GlobalVariable.imu1Gyro * 100));
        mPbImu2GyroValue.setProgress((int) (GlobalVariable.imu2Gyro * 100));

        changeProgressColor(mPbImu1AccelerometerValue, mPbImu2AccelerometerValue, mPbImu1GyroValue, mPbImu2GyroValue);
    }

    /**
     * 设置指南针的状态值
     */
    private void setCompassValue() {
        mTvCompass1DisturbanceValue.setText(String.valueOf(GlobalVariable.compass1Disturbance));
        mTvCompass2DisturbanceValue.setText(String.valueOf(GlobalVariable.compass2Disturbance));
        mPbCompass1DisturbanceValue.setProgress(GlobalVariable.compass1Disturbance);
        mPbCompass2DisturbanceValue.setProgress(GlobalVariable.compass2Disturbance);
        changeProgressColor(mPbCompass1DisturbanceValue, mPbCompass2DisturbanceValue);
    }

    private void changeProgressColor(ProgressBar... progressBars) {
        for (ProgressBar progressBar : progressBars) {
            int max = progressBar.getMax();
            int average = max / 3;
            LayerDrawable progressDrawable = (LayerDrawable) progressBar.getProgressDrawable();
            Drawable drawable = progressDrawable.getDrawable(1);

            if (progressBar.getProgress() <= average) {
                //绿色
                drawable.setColorFilter(mContext.getColor(R.color.color_BAE131), PorterDuff.Mode.SRC_ATOP);
            } else if (progressBar.getProgress() <= average * 2) {
                //黄色
                drawable.setColorFilter(mContext.getColor(R.color.color_FFC741), PorterDuff.Mode.SRC_ATOP);
            } else {
                //红色
                drawable.setColorFilter(mContext.getColor(R.color.color_FE3B3B), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_calibrate_imu:
                if (!connStateToast()){
                    return;
                }
                if (!GlobalVariable.planeHadLock) {
                    Toast.makeText(mContext, R.string.canNotCheckOnUnLockFly, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent1 = new Intent(mContext, IMUCalibrationActivity.class);
                mContext.startActivity(intent1);
                break;

            case R.id.btn_calibrate_compass:
                if (!connStateToast()){
                    return;
                }
                if (!GlobalVariable.planeHadLock) {
                    Toast.makeText(mContext, R.string.canNotCheckOnUnLockFly, Toast.LENGTH_SHORT).show();
                    return;
                }
                // 飞机解锁或不在地面上都不能进行校磁
                CompassCalibrationHelper.jumpMagnetometerActivity(mContext);
                break;

            default:
                break;
        }
    }


    /**
     * <P>shang</P>
     * <P>无人机连接状态提示</P>
     */
    private boolean connStateToast() {
        MyLogUtils.i("connStateToast()");
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(mContext, R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_MoreOne:
                Toast.makeText(mContext, R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_Sucess:
                return true;

            default:
                break;
        }
        return false;
    }
}
