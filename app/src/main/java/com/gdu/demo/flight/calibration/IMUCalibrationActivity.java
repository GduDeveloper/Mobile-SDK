package com.gdu.demo.flight.calibration;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;

import com.gdu.util.logs.AppLog;

import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * IMU校准
 */
public class IMUCalibrationActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = IMUCalibrationActivity.class.getSimpleName();
    private static final int STEP_NONE = -1;
    private static final int STEP1 = 0;
    private static final int STEP2 = 1;
    private static final int STEP3 = 2;
    private static final int STEP4 = 3;
    private static final int STEP5 = 4;
    private static final int STEP6 = 5;
    private static final int STEP_DONE = 6;
    //最大校准面
    private static int MAX_CHECK = 6;
    //校准初始位置-1
    private static final int INIT_CHECK_POS = -1;
    private Button btnStart;
    private Handler mHandler;
    private Context mContext;
    private ImageView ivImuGuide;

    private static final int STATUS_CHANGED = 0x03;
    private static final int STATUS_RECOVERY = 0x1000;

    private int mCurrentPosition = INIT_CHECK_POS;//校准初始位置

    private Integer[] photosID;
    /** 默认没有校准,当收到状态是校准中则为true */
    private boolean isChecking = false;
    private List<Boolean> checks = null;
    private String calibrationStatus;
    private AppCompatTextView tvStep1Num, tvStep2Num, tvStep3Num, tvStep4Num, tvStep5Num, tvStep6Num;
    private AppCompatTextView tvStep1Content, tvStep2Content, tvStep3Content, tvStep4Content, tvStep5Content, tvStep6Content;
    private AppCompatTextView tvStep1Checking, tvStep2Checking, tvStep3Checking, tvStep4Checking, tvStep5Checking, tvStep6Checking;
    private View viewStep1Done, viewStep2Done, viewStep3Done, viewStep4Done, viewStep5Done;

    //经飞控确认，最大超时时长为300秒
    private static final int MAX_TIMEOUT = 305;

    private Disposable checkDispose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_imu_calibration);
        findViews();
        initViews();
    }

    public void findViews() {
        mContext = this;
//        btnStart = findViewById(R.id.btn_start);
////        mGridView = findViewById(R.id.calibration_gridview);
//        ivImuGuide = findViewById(R.id.iv_imu_guide);
//
//        tvStep1Num = findViewById(R.id.tv_step1_num);
//        tvStep2Num = findViewById(R.id.tv_step2_num);
//        tvStep3Num = findViewById(R.id.tv_step3_num);
//        tvStep4Num = findViewById(R.id.tv_step4_num);
//        tvStep5Num = findViewById(R.id.tv_step5_num);
//        tvStep6Num = findViewById(R.id.tv_step6_num);
//        tvStep1Content = findViewById(R.id.tv_step1_content);
//        tvStep2Content = findViewById(R.id.tv_step2_content);
//        tvStep3Content = findViewById(R.id.tv_step3_content);
//        tvStep4Content = findViewById(R.id.tv_step4_content);
//        tvStep5Content = findViewById(R.id.tv_step5_content);
//        tvStep6Content = findViewById(R.id.tv_step6_content);
//        tvStep1Checking = findViewById(R.id.tv_step1_checking);
//        tvStep2Checking = findViewById(R.id.tv_step2_checking);
//        tvStep3Checking = findViewById(R.id.tv_step3_checking);
//        tvStep4Checking = findViewById(R.id.tv_step4_checking);
//        tvStep5Checking = findViewById(R.id.tv_step5_checking);
//        tvStep6Checking = findViewById(R.id.tv_step6_checking);
//        viewStep1Done = findViewById(R.id.view_step1_done);
//        viewStep2Done = findViewById(R.id.view_step2_done);
//        viewStep3Done = findViewById(R.id.view_step3_done);
//        viewStep4Done = findViewById(R.id.view_step4_done);
//        viewStep5Done = findViewById(R.id.view_step5_done);

    }

    public void initViews() {
//        initData();
//        setTitle(mContext.getString(R.string.imu_calibration));
    }

    private void initData() {
        AppLog.i(TAG, "MAX_CHECK:" + MAX_CHECK);
        initIMUPhotosData();
        initHandler();
//        resetStatus();
//        GduApplication.getSingleApp().gduCommunication.addCycleACKCB(GduSocketConfig3.CYCLE_ACK_IMU_CALIBRATION, (code, bean) -> {
//            MyLogUtils.i("IMU cycle callback() code = " + code);
//            if (isFinishing() || isDestroyed()) {
//                return;
//            }
//            boolean isEmptyData = bean == null || bean.frameContent == null || bean.frameContent.length == 0;
//            if (isEmptyData) {
//                return;
//            }
//            String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("IMU cycle callback() hexStr = " + hexStr);
//            int pos = bean.frameContent[0];
//            MyLogUtils.i("IMU cycle callback() pos = " + pos + "; mCurrentPosition = " + mCurrentPosition + "; isChecking = " + isChecking);
//            if (!isChecking) {//本地状态和飞行器状态不一致，需要恢复状态
//                mCurrentPosition = pos;
//                if (mCurrentPosition > 0 && mCurrentPosition < 13 || mCurrentPosition == -1) {//如果处于校准过程中，则直接进入进行时
//                    isChecking = true;
//                    mHandler.obtainMessage(STATUS_RECOVERY).sendToTarget();
//                }
//                mHandler.obtainMessage(STATUS_CHANGED, code, 0).sendToTarget();
//            }
//            //因为目前是被动接受消息，飞行器会按照指定频率发，所以这里优化处理下，状态不一致时发送状态变更
//            if (pos == mCurrentPosition) {
//                return;
//            }
//            //状态变更的时候再发通知消息
//            mCurrentPosition = pos;
//            mHandler.obtainMessage(STATUS_CHANGED, code, 0).sendToTarget();
//        });

    }

    /**
     * 初始化图片的数据
     */
    private void initIMUPhotosData() {
        //初始化IMU图片数据
//        photosID = CameraUtil.getIMUPhotos();
//        checks = new ArrayList<>();
//        for (int i = 0; i < MAX_CHECK; i++) {
//            checks.add(i == 0);
//        }
    }

    private void initHandler() {
//        calibrationStatus = mContext.getString(R.string.calibration_status);
//        mHandler = new MyHandler(this);
    }

    @Override
    public void onClick(View view) {

    }

//    private void showRestartDialog() {
//        if (restartDialog == null) {
//            AppLog.i(TAG, "showRestartDialog init");
//            restartDialog = new GeneralDialog(this, R.style.NormalDialog) {
//                @Override
//                public void positiveOnClick() {
//                    AppLog.i(TAG,"imu complete.click positive");
//                    sucFinish();
//
//                }
//
//                @Override
//                public void negativeOnClick() {
//                    AppLog.i(TAG,"imu complete.click negative");
//                    sucFinish();
//                }
//            };
//            restartDialog.setOnDismissListener(dialog -> {
//                AppLog.i(TAG, "imu complete.onDismiss listener");
//                if (!isFinishing()) {
//                    sucFinish();
//                }
//            });
//            restartDialog.setNoTitle();
//
//            if ((GlobalVariable.planType == PlanType.S220
//                    || GlobalVariable.planType == PlanType.S280
//                    || GlobalVariable.planType == PlanType.S200
//                    || GlobalVariable.planType == PlanType.S220Pro
//                    || GlobalVariable.planType == PlanType.S220ProS
//                    || GlobalVariable.planType == PlanType.S220ProH
//                    || GlobalVariable.planType == PlanType.S220_SD
//                    || GlobalVariable.planType == PlanType.S200_SD
//                    || GlobalVariable.planType == PlanType.S220BDS
//                    || GlobalVariable.planType == PlanType.S280BDS
//                    || GlobalVariable.planType == PlanType.S200BDS
//                    || GlobalVariable.planType == PlanType.S220ProBDS
//                    || GlobalVariable.planType == PlanType.S220ProSBDS
//                    || GlobalVariable.planType == PlanType.S220ProHBDS
//                    || GlobalVariable.planType == PlanType.S220_SD_BDS
//                    || GlobalVariable.planType == PlanType.S200_SD_BDS)) {
//
//                restartDialog.setContentText(R.string.string_calibration_completed);
//            } else {
//                restartDialog.setContentText(R.string.string_please_restart_aerocraft);
//            }
//        }
//        if (!restartDialog.isShowing()) {
//            restartDialog.show();
//        }
//    }
//
//    private void sucFinish() {
//        setResult(Activity.RESULT_OK);
//        finish();
//    }
//
//    @Override
//    public void initLisenter() {
//        btnStart.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.btn_start) {//开始校准
//            switchCalibration((byte) 1);
//        }
//    }
//
//    /**
//     * 9
//     * 开始校磁
//     */
//    private void switchCalibration(byte status) {
//        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
//            showToast(getString(R.string.fly_no_conn));
//            return;
//        }
//        if (!GlobalVariable.planeHadLock) {
//            showToast(getString(R.string.canNotCheckOnUnLockFly));
//            return;
//        }
//        sendCheckIMUCmd(status);
//    }
//
//    /**
//     * status 0 停止  1 开始
//     */
//    private void sendCheckIMUCmd(byte status) {
//        GduApplication.getSingleApp().gduCommunication.checkIMU(status, (byte) 1, (byte) 6, (code, bean) -> {
//            MyLogUtils.i("checkIMU callback() code = " + code);
//            if (isFinishing() || isDestroyed()) {
//                return;
//            }
//            uiThreadHandle(() -> {
//                if (code == GduConfig.OK && bean != null) {
//                    //初始化成功，开始校准流程
//                    if (status == 0) {
//                        // 停止校准
//                        resetStatus();
//                    } else if (status == 1) {
//                        // 开始校准
//                        isChecking = true;
//                        btnStart.setEnabled(false);
//                        btnStart.setBackgroundResource(R.drawable.shape_bg_d5d8db_r3);
//                        btnStart.setTextColor(getResources().getColor(R.color.color_ABABAB));
//                        checkTimeout();
//                        updateStepView(STEP1);
//                    }
//                } else {
//                    //初始化失败，重置校准状态
//                    showToast("code:" + code + ";" + calibrationStatus + ":" + mContext.getString(R.string.init_fail));
//                    resetStatus();
//                }
//            });
//        });
//    }
//
//    @Override
//    public void onHeadBack(View v) {
//        // 校准中  取消
//        if (isChecking) {
//            GeneralDialog dialog = new GeneralDialog(this, R.style.NormalDialog) {
//                @Override
//                public void positiveOnClick() {
//                    //确认退出，发送停止校准指令
//                    switchCalibration((byte) 0);
//                    finish();
//                }
//
//                @Override
//                public void negativeOnClick() {
//
//                }
//            };
//            dialog.setNoTitle();
//            dialog.setContentText(R.string.back_mui_check_hint);
//            dialog.show();
//        } else {
//            finish();
//        }
//    }
//
//    /**
//     * 恢复初始状态
//     */
//    private void resetStatus() {
//        if (isFinishing() || isDestroyed()) {
//            return;
//        }
//        isChecking = false;
//        btnStart.setEnabled(true);
//        btnStart.setBackgroundResource(R.drawable.shape_imu_start_board);
//        btnStart.setTextColor(getResources().getColor(R.color.color_FF5800));
//        ivImuGuide.setImageResource(photosID[0]);
//        mCurrentPosition = INIT_CHECK_POS;
//        updateStepView(STEP_NONE);
//        stopCheckTimeout();
//    }
//
//    /**
//     * 校准完成
//     */
//    private void imuCheckSuccess() {
//        if (isFinishing() || isDestroyed()) {
//            return;
//        }
//        isChecking = false;
//        btnStart.setBackgroundResource(R.drawable.shape_bg_d5d8db_r3);
//        btnStart.setTextColor(getResources().getColor(R.color.color_ABABAB));
//        btnStart.setText(getResources().getString(R.string.imu_step_check_done));
//        stopCheckTimeout();
//    }
//
//    private void updateStepView(int step) {
//        if (step == STEP1) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep2Checking.setVisibility(View.GONE);
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep3Checking.setVisibility(View.GONE);
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep4Checking.setVisibility(View.GONE);
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep5Checking.setVisibility(View.GONE);
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        } else if (step == STEP2) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep3Checking.setVisibility(View.GONE);
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep4Checking.setVisibility(View.GONE);
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep5Checking.setVisibility(View.GONE);
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        } else if (step == STEP3) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep3Checking.setVisibility(View.VISIBLE);
//            tvStep3Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep3Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep4Checking.setVisibility(View.GONE);
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep5Checking.setVisibility(View.GONE);
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        } else if (step == STEP4) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Checking.setVisibility(View.VISIBLE);
//            tvStep3Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep3Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep4Checking.setVisibility(View.VISIBLE);
//            tvStep4Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep4Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep5Checking.setVisibility(View.GONE);
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        } else if (step == STEP5) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Checking.setVisibility(View.VISIBLE);
//            tvStep3Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep3Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Checking.setVisibility(View.VISIBLE);
//            tvStep4Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep4Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Checking.setVisibility(View.VISIBLE);
//            tvStep5Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep5Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        } else if (step == STEP6) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Checking.setVisibility(View.VISIBLE);
//            tvStep3Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep3Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Checking.setVisibility(View.VISIBLE);
//            tvStep4Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep4Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep5Checking.setVisibility(View.VISIBLE);
//            tvStep5Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep5Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_select_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Checking.setVisibility(View.VISIBLE);
//            tvStep6Checking.setText(getResources().getString(R.string.imu_step_checking));
//            tvStep6Checking.setTextColor(getResources().getColor(R.color.color_FF5800));
//        } else if (step == STEP_DONE) {
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep1Checking.setVisibility(View.VISIBLE);
//            tvStep1Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep1Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep2Checking.setVisibility(View.VISIBLE);
//            tvStep2Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep2Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep3Checking.setVisibility(View.VISIBLE);
//            tvStep3Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep3Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep4Checking.setVisibility(View.VISIBLE);
//            tvStep4Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep4Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep5Checking.setVisibility(View.VISIBLE);
//            tvStep5Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep5Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.color_09D93E));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_select_09d93e_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.white));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_09D93E));
//            tvStep6Checking.setVisibility(View.VISIBLE);
//            tvStep6Checking.setText(getResources().getString(R.string.imu_step_check_done));
//            tvStep6Checking.setTextColor(getResources().getColor(R.color.color_09D93E));
//        } else {//初始状态
//            tvStep1Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep1Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep1Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep1Checking.setVisibility(View.GONE);
//            viewStep1Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep2Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep2Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep2Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep2Checking.setVisibility(View.GONE);
//            viewStep2Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep3Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep3Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep3Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep3Checking.setVisibility(View.GONE);
//            viewStep3Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep4Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep4Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep4Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep4Checking.setVisibility(View.GONE);
//            viewStep4Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep5Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep5Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep5Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep5Checking.setVisibility(View.GONE);
//            viewStep5Done.setBackgroundColor(getResources().getColor(R.color.pf_color_d8d8d8));
//            tvStep6Num.setBackgroundResource(R.drawable.shape_bg_unselect_ff5800_r12);
//            tvStep6Num.setTextColor(getResources().getColor(R.color.color_FF5800));
//            tvStep6Content.setTextColor(getResources().getColor(R.color.color_5C5C5C));
//            tvStep6Checking.setVisibility(View.GONE);
//        }
//    }
//
//    private static class MyHandler extends Handler {
//        private final WeakReference<IMUCalibrationActivity> mActivity;
//
//        public MyHandler(IMUCalibrationActivity activity) {
//            mActivity = new WeakReference<IMUCalibrationActivity>(activity); //获取弱引用Activity对象
//        }
//
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            if (mActivity.get() != null) {
//                IMUCalibrationActivity act=mActivity.get();
//                //利用弱引用来获取UI控件，不会对回收造成影响
//                switch (msg.what) {
//                    case IMUCalibrationActivity.STATUS_CHANGED:
//                        if (act.mCurrentPosition > 0 && act.mCurrentPosition < 13) {//校准中
//                            int pos = act.mCurrentPosition / 2;
//                            act.ivImuGuide.setImageResource(act.photosID[Math.min(pos, MAX_CHECK - 1)]);
//                            act.updateStepView(pos);
//                        } else if (act.mCurrentPosition == 14) {//校准成功
//                            // 添加延时1s更新处理，以用来给状态更新一点时间
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                MyLogUtils.e("IMU校准成功弹窗延时弹出处理出错", e);
//                            }
//                            act.imuCheckSuccess();
//                            act.showRestartDialog();
//                        } else if (act.mCurrentPosition == 15 || act.mCurrentPosition == 16) {//校准失败
//                            act.showToast(act.calibrationStatus + "：" + act.getString(R.string.calibration_fail));
//                            act.resetStatus();
//                        } else if (act.mCurrentPosition == 21) {//指令退出
//                            act.resetStatus();
//                        }
////                    mCalibrationAdapter.notifyDataSetChanged();
//                        break;
//                    case IMUCalibrationActivity.STATUS_RECOVERY:
//                        act.btnStart.setEnabled(false);
//                        act.btnStart.setBackgroundResource(R.drawable.shape_bg_d5d8db_r3);
//                        act.btnStart.setTextColor(act.getResources().getColor(R.color.color_ABABAB));
//                        act.checkTimeout();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    }
//
//    private void checkTimeout() {
//        stopCheckTimeout();
//        AppLog.i(TAG, "checkTimeout()");
//        checkDispose = Observable.interval(0,1, TimeUnit.SECONDS)
//                .take(MAX_TIMEOUT)
//                .map(l -> MAX_TIMEOUT - l)
//                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .to(RxLife.to(IMUCalibrationActivity.this))
//                .subscribe(l -> {
//                            btnStart.setText(getResources().getString(R.string.imu_checking_progress, (l - 1)));
//                        },
//                        throwable -> {
//                        },
//                        () -> {
//                            AppLog.i(TAG, "checkTimeout finish");
//                            resetStatus();
//                        });
//    }
//
//    private void stopCheckTimeout() {
//        try {
//            if (checkDispose != null && !checkDispose.isDisposed()) {
//                checkDispose.dispose();
//            }
//            checkDispose = null;
//        } catch (Exception e) {
//
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        // 校准中  取消
//        if (isChecking) {
//            GeneralDialog dialog = new GeneralDialog(this, R.style.NormalDialog) {
//                @Override
//                public void positiveOnClick() {
//                    //确认退出，发送停止校准指令
//                    switchCalibration((byte) 0);
//                    finish();
//                }
//
//                @Override
//                public void negativeOnClick() {
//
//                }
//            };
//            dialog.setNoTitle();
//            dialog.setContentText(R.string.back_mui_check_hint);
//            dialog.show();
//        } else {
//            super.onBackPressed();
//        }
//    }
}
