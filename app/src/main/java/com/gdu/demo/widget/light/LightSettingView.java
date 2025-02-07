package com.gdu.demo.widget.light;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Action;

public class LightSettingView extends FrameLayout {
    private final Context mContext;
    private GduSpinner mSwitchNightLights;
    private AppCompatImageView iv_silenceSwitch;
    private RelativeLayout mRlTopGimbalPower;
    private ImageView mIvPowerSwitch;
    private RelativeLayout rl_subLayout2;

    private boolean isOpenBatLightSuc;
    private boolean isOpenArmLampSuc;
    private int sendCmdNum = 0;

    public LightSettingView(@NonNull Context context) {
        this(context, null);
    }

    public LightSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_light_setting, this, true);
        initView();
        initListener();
    }

    private void initView() {
        mSwitchNightLights = findViewById(R.id.switch_night_lights);
        iv_silenceSwitch = findViewById(R.id.iv_silenceSwitch);
        mRlTopGimbalPower = findViewById(R.id.rl_top_gimbal_power);
        mIvPowerSwitch = findViewById(R.id.iv_power_switch);
        rl_subLayout2 = findViewById(R.id.rl_subLayout2);

        ViewUtils.setViewShowOrHide(rl_subLayout2, UavStaticVar.isOpenTextEnvironment);

        if (GlobalVariable.extraDevInfo != -1) {
            mSwitchNightLights.setIndex(GlobalVariable.extraDevInfo);
        }
        mIvPowerSwitch.setSelected(GlobalVariable.topGimbalPower);

        boolean isOpenArmLamp = GlobalVariable.flight_arm_lamp_status == 0;
        boolean isOpenBatteryLight = GlobalVariable.battery_silence_status == 1;
        MyLogUtils.i("initView() isOpenArmLamp = " + isOpenArmLamp + "; isOpenBatteryLight = "
                + isOpenBatteryLight + "; flight_arm_lamp_status = "
                + GlobalVariable.flight_arm_lamp_status
                + "; battery_silence_status = "
                + GlobalVariable.battery_silence_status);
        boolean isSelect = isOpenArmLamp & isOpenBatteryLight;
        iv_silenceSwitch.setSelected(isSelect);
    }

    private void initListener() {
        mSwitchNightLights.setOnOptionClickListener((parentId, view, position) -> {
//            GduApplication.getSingleApp().gduCommunication.switchNightLight((byte) position,(byte)(GlobalVariable.topGimbalPower?1:0), (code, bean) -> {
//                MyLogUtils.i("switchNightLight() position = " + position + "; code = " + code);
//                if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                    return;
//                }
//                mSwitchNightLights.setIndex(position);
//            });
        });

        mIvPowerSwitch.setOnClickListener(v -> {
//            GduApplication.getSingleApp().gduCommunication.switchNightLight((byte) GlobalVariable.extraDevInfo,(byte) (GlobalVariable.topGimbalPower?0:1), (code, bean) -> {
//                MyLogUtils.i("mIvPowerSwitch() code = " + code);
//                if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                    return;
//                }
//                GlobalVariable.topGimbalPower = !GlobalVariable.topGimbalPower;
//                mIvPowerSwitch.setSelected(GlobalVariable.topGimbalPower);
//            });
        });

        iv_silenceSwitch.setOnClickListener(v -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                Toast.makeText(mContext, R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                return;
            }
            iv_silenceSwitch.setSelected(!iv_silenceSwitch.isSelected());
            isOpenBatLightSuc = false;
            isOpenArmLampSuc = false;
            sendCmdNum = 0;

            sendCmdNum++;
//            GduApplication.getSingleApp().gduCommunication.switchPowerSilenceModel(
//                    iv_silenceSwitch.isSelected(), ((code, bean) -> {
//                        MyLogUtils.i("switchPowerSilenceModel callback() code = " + code);
//                        sendCmdNum--;
//                        if (code == GduConfig.OK) {
//                            isOpenBatLightSuc = true;
//                        }
//                        if (sendCmdNum == 0) {
//                            judgeSetResult();
//                        }
//                    }));
            sendCmdNum++;
//            GduApplication.getSingleApp().gduCommunication.setFlightArmLamp(
//                    iv_silenceSwitch.isSelected(), ((code, bean) -> {
//                        MyLogUtils.i("setFlightArmLamp callback() code = " + code);
//                        sendCmdNum--;
//                        if (code == GduConfig.OK) {
//                            isOpenArmLampSuc = true;
//                        }
//                        if (sendCmdNum == 0) {
//                            judgeSetResult();
//                        }
//                    }));
        });
    }

    private void judgeSetResult() {
        MyLogUtils.i("judgeSetResult() isOpenBatLightSuc = " + isOpenBatLightSuc + "; isOpenArmLampSuc = " + isOpenArmLampSuc);
        uiThreadHandle(() -> {
            if (isOpenBatLightSuc && isOpenArmLampSuc) {
                Toast.makeText(mContext, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                return;
            }
            MyLogUtils.i("judgeSetResult() 错误处理");
            Toast.makeText(mContext, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
            iv_silenceSwitch.setSelected(!iv_silenceSwitch.isSelected());
        });
    }

    public void uiThreadHandle(Action action) {
        Observable.empty().to(RxLife.toMain(this)).subscribe(o -> {}, throwable -> {
            MyLogUtils.e("UI线程处理失败", throwable);
        }, action);
    }
}
