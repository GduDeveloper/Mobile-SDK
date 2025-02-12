
package com.gdu.demo.flight.calibration;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.drone.PlanType;
import com.gdu.util.logger.MyLogUtils;

/**
 * Created by yuhao on 2017/7/4.
 */
public class RectifyDroneHelper implements View.OnClickListener {

    private final FragmentActivity mActivity;
    private ImageView iv_rectifyClose;
    private ImageView iv_rectifyPic;
    private TextView tv_rectifyFail;
    private LinearLayout ll_rectifyState;
    private TextView tv_rectify_again;
    private RelativeLayout rl_rectify;
    private LinearLayout ll_rectifying;
    private LinearLayout ll_step2;
    private TextView mStepInfoTextView;
    private TextView mRectifyCountDownTextView;
    private TextView tvStepLabel;
    private TextView tvStep2Label;
    /** 校磁总时间 */
    private final int RECTIFY_ALL_TIME = 120;
    /** 开始校磁时间 */
    private long mRectifyStartTime;

    /** 是否校磁成功 */
    private boolean isRectifyingSuccess = false;

    /*********************************
     * 校磁的状态: -------ron
     *
     * 1.XY正在校次
     * 2：XY校磁成功
     * 3：XY校磁失败
     * 4.Z轴正字校磁
     * 5：Z校磁成功
     * 6：Z校磁失败
     */
    private byte rectyfyStep;


    private CompassCalibrationViewModel viewModel;


    /**
     * <P>shang</P>
     * <P>操作重新校准/校准中</P>
     *
     * @param isShow
     */
    private void show2HideLayout(boolean isShow) {
        MyLogUtils.i("show2HideLayout() isShow = " + isShow);
        ll_rectifying.setVisibility(isShow ? View.VISIBLE : View.GONE);
        tv_rectify_again.setVisibility(isShow ? View.GONE : View.VISIBLE);
        mRectifyCountDownTextView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_horizontal_ing));
        iv_rectifyPic.setImageResource(viewModel.getDroneMagneticHorizontalIcon());
    }

    public RectifyDroneHelper(FragmentActivity context, View view) {
        this.mActivity = context;
        viewModel = new ViewModelProvider(context).get(CompassCalibrationViewModel.class);
        initView(view);
    }

    private void initView(View view) {
        iv_rectifyClose = view.findViewById(R.id.iv_RectifyExit);
        mStepInfoTextView = view.findViewById(R.id.step_info_textview);
        mRectifyCountDownTextView = view.findViewById(R.id.rectify_count_down_textview);
        iv_rectifyPic = view.findViewById(R.id.iv_planstate_rectify_PIC);
        tv_rectifyFail = view.findViewById(R.id.tv_planstate_rectifyFail);
        ll_rectifyState = view.findViewById(R.id.ll_rectify_state);
        tv_rectify_again = view.findViewById(R.id.tv_planstate_rectify_again);
        rl_rectify = view.findViewById(R.id.rl_planstate_rectify);
        ll_rectifying = view.findViewById(R.id.ll_planstate_rectifying);
        ll_step2 = view.findViewById(R.id.step2_layout);
        tvStepLabel = view.findViewById(R.id.tvStepLabel);
        tvStep2Label = view.findViewById(R.id.tvStep2Label);

        tvStepLabel.setText("STEP1");
        tvStep2Label.setText("STEP2");

        initClickListener();
        iv_rectifyPic.setImageResource(viewModel.getDroneMagneticHorizontalIcon());
        mRectifyCountDownTextView.setVisibility(View.GONE);
        tv_rectify_again.setVisibility(View.VISIBLE);
        rectyfyStep = 0;

    }

    private void initClickListener() {
        iv_rectifyClose.setOnClickListener(this);
        rl_rectify.setOnClickListener(this);
        tv_rectify_again.setOnClickListener(this);

        viewModel.getRectifyUpdateLiveData().observe(mActivity, data-> onRectifyUpdate());
        viewModel.getRectifySuccessLiveData().observe(mActivity, data-> onRectifySuccess());
        viewModel.getXyRectifySuccessLiveData().observe(mActivity, data->onXyRectifySuccess());
        viewModel.getRectifyFailLiveData().observe(mActivity, data->onRectifyFail(data));
        viewModel.getRectifyStopLiveData().observe(mActivity, data->onRectifyStop());
        viewModel.getXyRectifyStartLiveData().observe(mActivity, data->onXyRectifyStart());
        viewModel.addCompassCalibrationCallback();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_RectifyExit:
                if (viewModel.isRectifying()) {//正在校磁中需要弹窗提醒
                    onCompassDialog();
                } else {
                    if(isRectifyingSuccess){
                        checkNorthIsOver(true);
                        stopRectify();   //停止校磁
                    }else{
                        checkNorthIsOver(false);
                        stopRectify();   //停止校磁
                    }
                }
                break;

            case R.id.tv_planstate_rectify_again:
                if (viewModel.isRectifying()) {
                    return;
                }
                viewModel.startCompassCalibration((byte) 0x01);
                break;

            default:
                break;
        }
    }

    /**
     * 校磁弹窗
     * */
    private void onCompassDialog(){
        new CommonDialog.Builder(mActivity.getSupportFragmentManager())
                .setTitle(mActivity.getResources().getString(R.string.WarmPrompt))
                .setContent(mActivity.getResources().getString(R.string.Label_CheckNotOver_IsExit))
                .setCancelableOutside(false)
                .setPositiveListener((dialogInterface, i) -> {
                    checkNorthIsOver(false);
                    stopRectify();   //停止校磁
                }).build().show();
    }

    /************************************8
     * 校磁已经完成了
     */
    private void checkNorthIsOver(boolean needShowRestart) {
        // 弹窗时不需要隐藏界面
        /***************停止屏蔽稳像功能******************/
        if (mOnCheckNorthListener != null) {
            mOnCheckNorthListener.onCheckNorthOver(needShowRestart);
        }
    }

    private OnCheckNorthListener mOnCheckNorthListener;

    public void setOnCheckNorthListener(OnCheckNorthListener onCheckNorthListener) {
        this.mOnCheckNorthListener = onCheckNorthListener;
    }

    /***********************
     * 校磁的监听事件----ron
     */
    public interface OnCheckNorthListener {
        void onCheckNorthOver(boolean needShowRestart);
    }

    /**
     * @author yuhao
     * <p/>
     * 发送停止校磁指令
     */
    public void stopRectify() {
        viewModel.stopCompassCalibration();
    }

    /********************************
     * 回收数据用---防止在这个页面退出App了---ron
     ********************************/
    public void onDestory() {
        viewModel.cancelCompassCalibrationCallback();
    }

    public void onBackPressed() {
        if (viewModel.isRectifying()) {//正在校磁中需要弹窗提醒
            onCompassDialog();
        } else {
            if (isRectifyingSuccess) {
                checkNorthIsOver(true);
                stopRectify();   //停止校磁
            } else {
                checkNorthIsOver(false);
                stopRectify();   //停止校磁
            }
        }
    }

    /**
     * 校磁状态更新
     * */
    private void onRectifyUpdate(){
        long currentTime = 0;
        if (!viewModel.isRectifying()) {
            ll_rectifying.setVisibility(View.VISIBLE);
            ll_rectifyState.setVisibility(View.GONE);
            tv_rectify_again.setVisibility(View.GONE);
            mRectifyStartTime = System.currentTimeMillis() / 1000;
            if (rectyfyStep == 4) {
                ll_step2.setVisibility(View.VISIBLE);
                mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_vertical_ing));
            } else {//第一步XY水平校磁
                mRectifyCountDownTextView.setVisibility(View.VISIBLE);
                mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_horizontal_ing));
            }
            currentTime = mRectifyStartTime;
        } else {
            currentTime = System.currentTimeMillis() / 1000;
        }
        long countDownTime = RECTIFY_ALL_TIME - (currentTime - mRectifyStartTime);
        if(countDownTime < 0){
            countDownTime = 0;
        }
        String countDownStr = mActivity.getString(R.string.rectify_count_down);
        String hint = countDownStr + "(" + countDownTime + ")";
        SpannableStringBuilder mSSBuilder = new SpannableStringBuilder();
        mSSBuilder.append(hint);
        ForegroundColorSpan foregroundColorSpan;
        if (GlobalVariable.planType == PlanType.S220
                || GlobalVariable.planType == PlanType.S280
                || GlobalVariable.planType == PlanType.S200
                || GlobalVariable.planType == PlanType.S220Pro
                || GlobalVariable.planType == PlanType.S220ProS
                || GlobalVariable.planType == PlanType.S220ProH
                || GlobalVariable.planType == PlanType.S220_SD
                || GlobalVariable.planType == PlanType.S200_SD
                || GlobalVariable.planType == PlanType.S220BDS
                || GlobalVariable.planType == PlanType.S280BDS
                || GlobalVariable.planType == PlanType.S200BDS
                || GlobalVariable.planType == PlanType.S220ProBDS
                || GlobalVariable.planType == PlanType.S220ProSBDS
                || GlobalVariable.planType == PlanType.S220ProHBDS
                || GlobalVariable.planType == PlanType.S220_SD_BDS
                || GlobalVariable.planType == PlanType.S200_SD_BDS) {
            foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.color_ADADAD));
        } else {
            foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.color_EF4E22));
        }
        mSSBuilder.setSpan(foregroundColorSpan, countDownStr.length(), hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mRectifyCountDownTextView.setText(mSSBuilder);
    }

    /**
     * 校磁成功
     * */
    private void onRectifySuccess(){
        if (isRectifyingSuccess) {
            return;
        }
        checkNorthIsOver(true);
        show2HideLayout(true);//复原校磁 界面
        Toast.makeText(mActivity, R.string.Label_CheckSuccess, Toast.LENGTH_SHORT).show();
        isRectifyingSuccess = true;
    }

    private void onXyRectifyStart(){
        ll_rectifying.setVisibility(View.VISIBLE);
        ll_rectifyState.setVisibility(View.GONE);
        tv_rectify_again.setVisibility(View.GONE);
        mRectifyStartTime = System.currentTimeMillis() / 1000;
        mRectifyCountDownTextView.setVisibility(View.VISIBLE);
        mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_horizontal_ing));
    }

    /** XY校磁成功 */
    private void onXyRectifySuccess(){
        ll_step2.setVisibility(View.VISIBLE);
        mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_vertical_ing));
        iv_rectifyPic.setImageResource(viewModel.getDroneMagneticVerticalIcon());
        Toast.makeText(mActivity, R.string.Label_ZChecking, Toast.LENGTH_SHORT).show();
    }

    /** 校磁失败 */
    private void onRectifyFail(int data){
        ll_rectifyState.setVisibility(View.VISIBLE);
        tv_rectifyFail.setVisibility(View.VISIBLE);
        tv_rectify_again.setText(R.string.Label_PlaneState_Calibration_again);
        if (0!=data){
            tv_rectifyFail.setText(data);
        }
        mStepInfoTextView.setText(mActivity.getString(R.string.Label_PlaneState_Calibration_horizontal_ing));
        iv_rectifyPic.setImageResource(viewModel.getDroneMagneticHorizontalIcon());
        show2HideLayout(false);// 校磁失败
        Toast.makeText(mActivity, R.string.Label_CheckFail, Toast.LENGTH_SHORT).show();
        isRectifyingSuccess = false;
    }

    /** 校磁暂停 */
    private void onRectifyStop(){
        ll_rectifyState.setVisibility(View.GONE);
        show2HideLayout(false);//复原校磁 界面
    }
}
