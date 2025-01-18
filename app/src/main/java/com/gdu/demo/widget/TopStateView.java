package com.gdu.demo.widget;

import android.content.Context;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.gdu.common.ConnStateEnum;
import com.gdu.common.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.TopStateViewLayoutBinding;
import com.gdu.util.logs.RonLog2File;
import com.rxjava.rxlife.RxLife;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class TopStateView  extends ConstraintLayout {


    private Context context;
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
        this.context = context;
        initView(context);
        initData();
    }



    private void initView(Context context) {
        binding = TopStateViewLayoutBinding.bind(View.inflate(context, R.layout.top_state_view_layout, this));
        binding.ivBack.setOnClickListener(listener);
        binding.ivSetMenu.setOnClickListener(listener);
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
        Log.d("TopStateView", "TopStateView  update");

        // 更新遥控器电池
        updateRcBattery();
        //  更新飞机电量
        updateAircraftBattery();
        // 更新遥控器图传信号
        updateRcImageTransSignal();
        // 更新飞机图传信号
        updateAircraftImageTransSignal();
        // 更新避障开关
        updateObstacleAvoidance();
        // 更新RTK
        updateRtkState();
        // 更新解锁状态
        updateAircraftLockState();
        // 更新飞行模式
        updateFlyMode();
    }


    private void updateRcBattery() {
        if (GlobalVariable.sRCConnState == ConnStateEnum.Conn_Sucess) {
            int battery = GlobalVariable.power_rc;
            binding.tvElectricityControl.setText(battery + "%");
            if (battery <= 20) {
                binding.tvElectricityControl.setTextColor(ContextCompat.getColor(context, R.color.color_ff0000));
                binding.ivRemoteRc.setImageResource(R.drawable.top_remot_rc_low);
            } else {
                binding.tvElectricityControl.setTextColor(ContextCompat.getColor(context, R.color.color_ffffff));
                binding.ivRemoteRc.setImageResource(R.drawable.top_remot_rc);
            }
        } else {
            binding.tvElectricityControl.setText(context.getString(R.string.Label_N_A));
        }

    }
    private void updateAircraftBattery() {

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.ivAircraft.setImageResource(R.drawable.top_aircraft_electricity);
            if(binding.ivAircraft.getVisibility() == View.INVISIBLE) {
                binding.ivAircraft.setVisibility(View.VISIBLE);
            }
            binding.tvAircraft.setTextColor(ContextCompat.getColor(context, R.color.color_ffffff));
            binding.tvAircraftEnergyV.setTextColor(ContextCompat.getColor(context, R.color.color_ffffff));
            binding.tvAircraftEnergyV.setBackgroundResource(R.drawable.stroke_ffffff_radius_2_bg);
            binding.tvAircraft.setText(context.getString(R.string.Label_N_A));
            binding.tvAircraftEnergyV.setText(context.getString(R.string.Label_N_A));
        } else {
            int dronePowerPercent = GlobalVariable.power_drone;
            int dronePowerVoltage = GlobalVariable.flight_voltage;
            binding.tvAircraft.setText(dronePowerPercent + "%");
            BigDecimal mBigDecimal = new BigDecimal(dronePowerVoltage / 1000f);
            BigDecimal voltage = mBigDecimal.setScale(1, RoundingMode.HALF_UP);
            binding.tvAircraftEnergyV.setText(voltage + "V");

            switch (GlobalVariable.batteryAbnormalCode) {
                case 1:
                case 4:
                    binding.ivAircraft.setImageResource(R.drawable.top_aircraft_electricity_low);
                    binding.tvAircraft.setTextColor(ContextCompat.getColor(context, R.color.color_ff0000));
                    binding.tvAircraftEnergyV.setTextColor(ContextCompat.getColor(context, R.color.color_ff0000));
                    binding.tvAircraftEnergyV.setBackgroundResource(R.drawable.stroke_ef4e22_radius_2_bg);
                    break;
                case 2:
                    binding.ivAircraft.setImageResource(R.drawable.top_aircraft_electricity_low_one);
                    binding.tvAircraft.setTextColor(ContextCompat.getColor(context, R.color.color_FFC600));
                    binding.tvAircraftEnergyV.setTextColor(ContextCompat.getColor(context, R.color.color_FFC600));
                    binding.tvAircraftEnergyV.setBackgroundResource(R.drawable.stroke_feb431_radius_2_bg);
                    break;

                default:
                    binding.ivAircraft.setImageResource(R.drawable.top_aircraft_electricity);
                    if (binding.ivAircraft.getVisibility() == View.INVISIBLE) {
                        binding.ivAircraft.setVisibility(View.VISIBLE);
                    }
                    binding.tvAircraft.setTextColor(ContextCompat.getColor(context, R.color.color_ffffff));
                    binding.tvAircraftEnergyV.setTextColor(ContextCompat.getColor(context, R.color.color_ffffff));
                    binding.tvAircraftEnergyV.setBackgroundResource(R.drawable.stroke_ffffff_radius_2_bg);
                    break;
            }
        }
    }

    private void updateRcImageTransSignal() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.tvGtQuality.setMCSQuality(-1);
            return;
        }
        binding.tvGtQuality.setMCSQuality(GlobalVariable.arlink_grdMcs);

    }

    private void updateAircraftImageTransSignal() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.tvStQuality.setMCSQuality(-1);
            return;
        }
        binding.tvStQuality.setMCSQuality(GlobalVariable.arlink_skyMcs);
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

    private void updateRtkState() {

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.tvRtk1Satellite.setText(context.getString(R.string.Label_N_A));
            binding.tvRtk1Status.setText(context.getString(R.string.Label_N_A));
            return;
        }
        byte currentSatellite = GlobalVariable.satellite_drone;
        String tkStatus = GlobalVariable.rtk_model.getRtk1_status();
        binding.tvRtk1Satellite.setText(currentSatellite+"");
        binding.tvRtk1Status.setText(tkStatus);
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



    public View.OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:
                    if (clickCallBack != null) {
                        clickCallBack.onLeftIconClick();
                    }
                    break;

                case R.id.iv_set_menu:;
                    if (clickCallBack != null) {
                        clickCallBack.onRightSettingIconCLick();
                    }
                    break;
                default:
                    break;
            }

        }
    };


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
