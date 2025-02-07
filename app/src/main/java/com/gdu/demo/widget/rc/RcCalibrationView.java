package com.gdu.demo.widget.rc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;
import com.gdu.demo.widget.RockerFourProgressView;

import cc.taylorzhang.singleclick.SingleClickUtil;

/**
 * @Author fuchi
 * @Date 2023/4/23-9:44
 * @Package com.gdu.setting.rocker
 * @Description
 */
public class RcCalibrationView extends RelativeLayout  implements View.OnClickListener {

    private Context mContext;

    private LinearLayout rc_calibration_lin;  //摇杆lin
    private TextView left_rocker_tv;    //左摇杆检测状态
    private TextView right_rocker_tv;   //右摇杆检测状态
    private RockerFourProgressView left_rc_four_progress_vew;  //左摇杆progress
    private RockerFourProgressView right_rc_four_progress_vew; //右摇杆progress

    private LinearLayout wave_calibration_lin;  //波轮lin
    private TextView left_wave_wheel_check_tv;    //左波轮检测状态
    private TextView right_wave_wheel_check_tv;    //右波轮检测状态
    private ProgressBar left_left_wave_progress;    //左波轮-left progress
    private ProgressBar left_right_wave_progress;    //左波轮-right progress
    private ProgressBar right_left_wave_progress;    //右波轮-left progress
    private ProgressBar right_right_wave_progress;    //右波轮-right progress

    private TextView check_btn;//校准按钮

//    private int selectType = 0; //默认显示摇杆检测

    public RcCalibrationView(Context context) {
        this(context, null);
    }

    public RcCalibrationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RcCalibrationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initData();
        initListener();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_rc_calibration, this);

        rc_calibration_lin = findViewById(R.id.rc_calibration_lin);
        left_rocker_tv = findViewById(R.id.left_rocker_tv);
        right_rocker_tv = findViewById(R.id.right_rocker_tv);
        left_rc_four_progress_vew = findViewById(R.id.left_rc_four_progress_vew);
        right_rc_four_progress_vew = findViewById(R.id.right_rc_four_progress_vew);

        wave_calibration_lin = findViewById(R.id.wave_calibration_lin);
        left_wave_wheel_check_tv = findViewById(R.id.left_wave_wheel_check_tv);
        right_wave_wheel_check_tv = findViewById(R.id.right_wave_wheel_check_tv);
        left_left_wave_progress = findViewById(R.id.left_left_wave_progress);
        left_right_wave_progress = findViewById(R.id.left_right_wave_progress);
        right_left_wave_progress = findViewById(R.id.right_left_wave_progress);
        right_right_wave_progress = findViewById(R.id.right_right_wave_progress);

        check_btn = findViewById(R.id.check_btn);//校准按钮
    }

    private void initData() {
    }

    private void initListener(){
        check_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SingleClickUtil.determineTriggerSingleClick(v, v1 -> {
            switch (v.getId()) {
                case R.id.check_btn:
                    if(null != listener){
                        listener.onStartCalibrate();
                    }
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 设置摇杆top
     */
    public void setTopValue(boolean isFirstView, int value, int progress){
        if (isFirstView){
            left_rc_four_progress_vew.setTopValue(value, progress);
        }else{
            right_rc_four_progress_vew.setTopValue(value, progress);
        }
    }

    /**
     * 设置摇杆bottom
     */
    public void setBottomValue(boolean isFirstView, int value, int progress){
        if (isFirstView){
            left_rc_four_progress_vew.setBottomValue(value, progress);
        }else{
            right_rc_four_progress_vew.setBottomValue(value, progress);
        }
    }

    /**
     * 设置摇杆left
     */
    public void setLeftValue(boolean isFirstView, int value, int progress){
        if (isFirstView){
            left_rc_four_progress_vew.setLeftValue(value, progress);
        }else{
            right_rc_four_progress_vew.setLeftValue(value, progress);
        }
    }

    /**
     * 设置摇杆right
     */
    public void setRightValue(boolean isFirstView, int value, int progress){
        if (isFirstView){
            left_rc_four_progress_vew.setRightValue(value, progress);
        }else{
            right_rc_four_progress_vew.setRightValue(value, progress);
        }
    }

    /**
     * 设置左波轮
     * @param isAdd
     * @param value
     * @param progress
     */
    public void setLeftWaveValue(boolean isAdd, int value, int progress){
        if (isAdd) {
            int realValue = progress;
            left_right_wave_progress.setProgress( realValue);
            left_left_wave_progress.setProgress( 0);
        } else {
            left_left_wave_progress.setProgress( progress);
            left_right_wave_progress.setProgress(0);
        }
    }

    /**
     * 设置右波轮
     * @param isAdd
     * @param value
     * @param progress
     */
    public void setRightWaveValue(boolean isAdd, int value, int progress){
        if (isAdd) {
            int realValue = progress;
            right_right_wave_progress.setProgress( realValue);
            right_left_wave_progress.setProgress( 0);
        } else {
            right_left_wave_progress.setProgress( progress);
            right_right_wave_progress.setProgress(0);
        }
    }

    public void resetView(){
        left_rc_four_progress_vew.resetAllView();
        right_rc_four_progress_vew.resetAllView();
        left_left_wave_progress.setProgress(0);
        left_right_wave_progress.setProgress(0);
        right_left_wave_progress.setProgress(0);
        right_right_wave_progress.setProgress(0);
    }

    /**
     * 设置按钮文字
     * @param text
     */
    public void setBtnText(String text, int color){
        check_btn.setText(text);
        check_btn.setTextColor(mContext.getResources().getColor(color));
    }


    public interface CalibrateViewListener{
        void onStartCalibrate();
    }

    public CalibrateViewListener listener;

    public void setOnCalibrateViewListener(CalibrateViewListener listener){
        this.listener = listener;
    }
}
