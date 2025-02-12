package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.TopStateViewLayoutBinding;

import java.util.concurrent.TimeUnit;

import cc.taylorzhang.singleclick.SingleClickUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class TopStateView  extends ConstraintLayout {

    private TopStateViewLayoutBinding binding;
    private OnClickCallBack clickCallBack;
    private Disposable disposable;

    public TopStateView(Context context) {
        this(context, null);
    }

    public TopStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initData();
    }



    private void initView(Context context) {
        binding = TopStateViewLayoutBinding.bind(View.inflate(context, R.layout.top_state_view_layout, this));
        SingleClickUtil.onSingleClick(binding.ivBack, view -> {
            if (clickCallBack != null) {
                clickCallBack.onLeftIconClick();
            }
        });
        SingleClickUtil.onSingleClick(binding.ivSetMenu, view -> {
            if (clickCallBack != null) {
                clickCallBack.onRightSettingIconCLick();
            }
        });
    }

    private void initData() {
        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    updateState();
                }, throwable -> {
                    Log.d("TopStateView", " TopStateView 更新出错 e =" + throwable);
                });

    }

    private void updateState() {

        // 更新遥控器图传信号
//        updateRcImageTransSignal();
        // 更新飞机图传信号
//        updateAircraftImageTransSignal();
        // 更新避障开关
        updateObstacleAvoidance();
        // 更新RTK
//        updateRtkState();
        // 更新解锁状态
        updateAircraftLockState();
        // 更新飞行模式
        updateFlyMode();
    }

    private void updateObstacleAvoidance() {

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.ivVision.setSelected(false);
            return;
        }
        //  需要显示避障的模式
        boolean isShowRadarModel = (GlobalVariable.flyMode == 1 && GlobalVariable.DroneFlyMode == 1)
                || GlobalVariable.flyMode == 4 || GlobalVariable.flyMode == 5
                || GlobalVariable.backState == 2 || GlobalVariable.backObstacleState == 1
                || GlobalVariable.backObstacleState == 2;
        boolean isDroneAttitudeModel = GlobalVariable.flyMode == 0;//是否是姿态模式
        isShowRadarModel = isShowRadarModel && !isDroneAttitudeModel;
        if (isShowRadarModel) {
            binding.ivVision.setSelected(GlobalVariable.obstacleIsOpen);
        } else {
            binding.ivVision.setSelected(false);
        }
    }

    private void updateAircraftLockState() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.ivLock.setVisibility(View.GONE);
            return;
        }
        binding.ivLock.setVisibility(View.VISIBLE);
        if (GlobalVariable.planeHadLock) {
            binding.ivLock.setImageResource(R.drawable.plane_lock);
        } else {
            binding.ivLock.setImageResource(R.drawable.plane_unlock);
        }
    }

    private void updateFlyMode() {

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.tvSportMode.setVisibility(GONE);
            return;
        }
        binding.tvSportMode.setVisibility(VISIBLE);
        if (GlobalVariable.flyMode == 0) {//调整到姿态模式了
            binding.tvSportMode.setText("A");
        } else if (GlobalVariable.flyMode == 4) {
            binding.tvSportMode.setText("V");
        } else if (GlobalVariable.flyMode == 5) {
            binding.tvSportMode.setText("T");
        } else if (GlobalVariable.flyMode == 1) {
            if (GlobalVariable.DroneFlyMode == 0) {
                binding.tvSportMode.setText("S");
            } else {
                binding.tvSportMode.setText("P");
            }
        } else if (GlobalVariable.flyMode == 6) {
            binding.tvSportMode.setText("VI");
        }
    }

    public void setStatusText(String title){
        binding.statusBarTitle.setText(title);
    }

    public void setStatusTextColor(int txtColor){
        binding.statusBarTitle.setTextColor(getResources().getColor(txtColor));
    }

    public void setStatusTextBackground(int resId){
        binding.statusBarTitle.setBackgroundResource(resId);
    }

    public void setViewClickListener(OnClickCallBack listener) {
        this.clickCallBack = listener;
    }

    public interface OnClickCallBack{

        void onLeftIconClick();

        void onRightSettingIconCLick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
            Log.d("TopStateView", "TopStateView  onDetachedFromWindow");
        }
    }
}
