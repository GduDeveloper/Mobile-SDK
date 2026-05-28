package com.gdu.demo.widget;

import static com.gdu.demo.utils.MultiTimerManager.NORMAL_TIMER;
import static com.gdu.demo.utils.MultiTimerManager.QUICK_TIMER;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.TopStateViewLayoutBinding;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.demo.utils.MultiTimerManager;
import com.gdu.msdk.device.component.interfaces.IVision;
import com.gdu.msdk.key.value.CycleRadarInfo;
import com.gdu.msdk.key.value.bean.FlyMode;

import cc.taylorzhang.singleclick.SingleClickUtil;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
        MultiTimerManager.Companion.getInstance().createTimer(NORMAL_TIMER, 1000);
        MultiTimerManager.Companion.getInstance().createTimer(QUICK_TIMER, 200);
        MultiTimerManager.Companion.getInstance().startAllTimers();
        disposable = MultiTimerManager.Companion.getInstance()
                .getTimerObservable(NORMAL_TIMER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> updateState());
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

        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            binding.ivVision.setSelected(false);
            return;
        }
        //  需要显示避障的模式
        boolean isShowRadarModel = (DroneUtils.getFlyModel().isGpsMode() && DroneUtils.getFlyModel() == FlyMode.GPS_NORMAL)
                || DroneUtils.getFlyModel() == FlyMode.VISION || DroneUtils.getFlyModel() == FlyMode.TRIPOD
                || GlobalVariable.backState == 2 || GlobalVariable.backObstacleState == 1
                || GlobalVariable.backObstacleState == 2;
        boolean isDroneAttitudeModel = DroneUtils.getFlyModel() == FlyMode.ATTITUDE;//是否是姿态模式
        isShowRadarModel = isShowRadarModel && !isDroneAttitudeModel;
        if (isShowRadarModel) {
            CycleRadarInfo radarInfo = IVision.get().getRadarInfo().getValue();
            boolean obstacleIsOpen = radarInfo != null && radarInfo.getObstacleIsOpen();
            binding.ivVision.setSelected(obstacleIsOpen);
        } else {
            binding.ivVision.setSelected(false);
        }
    }

    private void updateAircraftLockState() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            binding.ivLock.setVisibility(View.GONE);
            return;
        }
        binding.ivLock.setVisibility(View.VISIBLE);
        if (DroneUtils.getPlaneHadLock()) {
            binding.ivLock.setImageResource(R.drawable.plane_lock);
        } else {
            binding.ivLock.setImageResource(R.drawable.plane_unlock);
        }
    }

    private void updateFlyMode() {

        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            binding.tvSportMode.setVisibility(GONE);
            return;
        }
        binding.tvSportMode.setVisibility(VISIBLE);
        if (DroneUtils.getFlyModel() == FlyMode.ATTITUDE) {//调整到姿态模式了
            binding.tvSportMode.setText("A");
        } else if (DroneUtils.getFlyModel() == FlyMode.VISION) {
            binding.tvSportMode.setText("V");
        } else if (DroneUtils.getFlyModel() == FlyMode.TRIPOD) {
            binding.tvSportMode.setText("T");
        } else if (DroneUtils.getFlyModel().isGpsMode()) {
            if (DroneUtils.getFlyModel() == FlyMode.GPS_SPORT) {
                binding.tvSportMode.setText("S");
            } else {
                binding.tvSportMode.setText("P");
            }
        } else if (DroneUtils.getFlyModel() == FlyMode.VI) {
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
        MultiTimerManager.Companion.getInstance().stopAllTimers();
    }
}
