package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.flightcontroller.ConnectionFailSafeBehavior;
import com.gdu.gimbal.RotationMode;
import com.gdu.rtk.PositioningSolution;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.flightcontroller.bean.LowBatteryWarnInfo;
import com.gdu.sdk.simulator.InitializationData;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * 飞控测试界面
 */
public class FlightControllerActivity extends Activity implements View.OnClickListener {

    private Context mContext;

    private GDUFlightController mGDUFlightController;

    private TextView mSimulatorStatusTextview;
    private TextView mFCStateInfoTextView;
    private TextView mMaxFlightRadiusTextView;
    private TextView mMaxFlightHeightTextView;
    private TextView mGoHomeHeightTextview;
    private TextView mFCVersionTextview;
    private Switch mSetMaxFlightRadiusLimitationSwitch;
    private TextView mConnectStatusTextview;

    private TextView mAircraftModelTextview;

    private TextView mAircraftSNTextview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_flight_controller);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        mSimulatorStatusTextview = findViewById(R.id.simulator_status_textview);
        mConnectStatusTextview = findViewById(R.id.connect_status_textview);
        mFCStateInfoTextView = findViewById(R.id.fc_state_info_textview);
        mMaxFlightRadiusTextView = findViewById(R.id.max_flight_radius_textview);
        mMaxFlightHeightTextView = findViewById(R.id.max_flight_height_textview);
        mGoHomeHeightTextview = findViewById(R.id.go_home_height_textview);
        mFCVersionTextview = findViewById(R.id.fc_version_textview);
        mAircraftModelTextview = findViewById(R.id.aircraft_model_textview);
        mSetMaxFlightRadiusLimitationSwitch = findViewById(R.id.set_max_flight_radius_limitation_switch);
        mAircraftSNTextview = findViewById(R.id.aircraft_sn_textview);
    }

    private void initListener() {
        mSetMaxFlightRadiusLimitationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setMaxFlightRadiusLimitationEnable(isChecked);
            }
        });
        mConnectStatusTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectStatusTextview.setText("连接" +  mGDUFlightController.isConnected());
            }
        });
    }

    private void initData() {
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        mGDUFlightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                showText(mFCStateInfoTextView, flightControllerState.getString());
            }
        });
        getMaxFlightRadiusLimitationEnable();
        if (mGDUFlightController.getSimulator().isSimulatorActive()) {
            mSimulatorStatusTextview.setText("已开启");
        } else {
            mSimulatorStatusTextview.setText("已关闭");
        }
    }

    /**
     * 设置限高
     */
    public void setMaxFlightHeight() {
        if (mGDUFlightController != null) {
            mGDUFlightController.setMaxFlightHeight(50, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        showText(mMaxFlightHeightTextView, "50");
                    } else {
                        showText(mMaxFlightHeightTextView, "fail");
                    }
                }
            });
        }
    }

    /**
     * 获取限高
     */
    public void getMaxFlightHeight() {
        if (mGDUFlightController != null) {
            mGDUFlightController.getMaxFlightHeight(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                @Override
                public void onSuccess(Integer height) {
                    showText(mMaxFlightHeightTextView, "" + height);
                }

                @Override
                public void onFailure(GDUError var1) {
                    showText(mMaxFlightHeightTextView, "fail");
                }
            });
        }
    }

    /**
     * 设置限距开关
     * @param isEnable
     */
    private void setMaxFlightRadiusLimitationEnable(boolean isEnable) {
        if (mGDUFlightController != null) {
            mGDUFlightController.setMaxFlightRadiusLimitationEnabled(isEnable, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        toastText("限距 " + isEnable);
                    } else {
                        toastText("限距设置失败");
                    }
                }
            });
        }
    }

    /**
     * 获取限距开关
     */
    private void getMaxFlightRadiusLimitationEnable() {
        if (mGDUFlightController != null) {
            mGDUFlightController.getMaxFlightRadiusLimitationEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                @Override
                public void onSuccess(Boolean isEnable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isEnable) {
                                    mSetMaxFlightRadiusLimitationSwitch.setChecked(true);
                                } else {
                                    mSetMaxFlightRadiusLimitationSwitch.setChecked(false);
                                }
                            }
                        });
                }

                @Override
                public void onFailure(GDUError var1) {

                }
            });
        }
    }

    /**
     * 设置限距
     */
    public void setMaxFlightRadius() {
        if (mGDUFlightController != null) {
            mGDUFlightController.setMaxFlightRadius(100, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        showText(mMaxFlightRadiusTextView, 100+"");
                    } else {
                        showText(mMaxFlightRadiusTextView, "fail");
                    }
                }
            });
        }
    }

    /**
     * 获取限距
     */
    public void getMaxFlightRadius() {
        if (mGDUFlightController != null) {
            mGDUFlightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                @Override
                public void onSuccess(Integer radius) {
                    showText(mMaxFlightRadiusTextView, radius + "");
                }

                @Override
                public void onFailure(GDUError var1) {
                    showText(mMaxFlightRadiusTextView, "fail");
                }
            });
        }
    }

    /**
     * 设置返航高度
     */
    public void setGoHomeHeightInMeters() {
        mGDUFlightController.setGoHomeHeightInMeters((short) 115, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    showText(mGoHomeHeightTextview, "115");
                } else {
                    showText(mGoHomeHeightTextview, "fail");
                }
            }
        });
    }

    /**
     * 获取返航高度
     */
    public void getGoHomeHeightInMeters() {
        mGDUFlightController.getGoHomeHeightInMeters(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer height) {
                showText(mGoHomeHeightTextview, "" + height);
            }

            @Override
            public void onFailure(GDUError var1) {
                showText(mGoHomeHeightTextview, "fail");
            }
        });
    }

    /**
     * 获取飞控版本
     */
    private void getFCVersion(){
        if (mGDUFlightController != null) {
            mGDUFlightController.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String version) {
                    showText(mFCVersionTextview, version);
                }

                @Override
                public void onFailure(GDUError var1) {
                    showText(mFCVersionTextview, "fail");
                }
            });
        }
    }

    /**
     * 开始模拟飞行
     */
    private void startSimulator() {
        if (null != mGDUFlightController) {
            LocationCoordinate3D locationCoordinate3D = new LocationCoordinate3D(30.471033,114.4280014, 10);
            InitializationData initializationData = new InitializationData(locationCoordinate3D, (short) 90, PositioningSolution.FIXED_POINT, (byte) 30);
            mGDUFlightController.getSimulator().start(initializationData, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError gduError) {
                    if (gduError == null) {

                    }
                }
            });
        }
    }

    private void startTakeoff(){
        // 1.5m
        mGDUFlightController.startTakeoff(150, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("起飞成功");
                } else {
                    toastText("起飞失败");
                }
            }
        });
    }

    private void startLanding(){
        mGDUFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("开始降落成功");
                } else {
                    toastText("开始降落失败");
                }
            }
        });
    }

    private void cancelLanding(){
        mGDUFlightController.cancelLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("取消降落成功");
                } else {
                    toastText("取消降落失败");
                }
            }
        });
    }

    private void startGoHome(){
        mGDUFlightController.startGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("开始返航成功");
                } else {
                    toastText("开始返航失败");
                }
            }
        });
    }

    private void cancelGoHome(){
        mGDUFlightController.cancelGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("取消返航成功");
                } else {
                    toastText("取消返航失败");
                }
            }
        });
    }

    private void startPrecisionGoHome() {
        mGDUFlightController.startPrecisionGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("开始精准返航成功");
                } else {
                    toastText("开始精准返航失败");
                }
            }
        });
    }


    private void cancelPrecisionGoHome() {
        mGDUFlightController.cancelPrecisionGoHome(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("取消精准返航成功");
                } else {
                    toastText("取消精准返航失败");
                }
            }
        });
    }

    private void setConnectionFailSafeBehavior(){
        mGDUFlightController.setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("设置失联行为成功");
                } else {
                    toastText("设置失联行为失败");
                }
            }
        });
    }

    private void getConnectionFailSafeBehavior(){
        mGDUFlightController.getConnectionFailSafeBehavior(new CommonCallbacks.CompletionCallbackWith<ConnectionFailSafeBehavior>() {
            @Override
            public void onSuccess(ConnectionFailSafeBehavior var1) {
                toastText("获取失联行为成功 " + var1);
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取失联行为失败 " + var1);
            }
        });
    }

    private void setLowBatteryWarningThreshold() {
        mGDUFlightController.setLowBatteryWarningThreshold(35, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("设置低电量阈值成功 ");
                } else {
                    toastText("设置低电量阈值失败 ");
                }
            }
        });
    }

    private void getLowBatteryWarningThreshold() {
        mGDUFlightController.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<LowBatteryWarnInfo>() {
            @Override
            public void onSuccess(LowBatteryWarnInfo var1) {
                toastText("获取低电量阈值成功 " + var1.getOneLevelWarn());
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取低电量阈值失败 ");
            }
        });
    }

    private void setSeriousLowBatteryWarningThreshold() {
        mGDUFlightController.setSeriousLowBatteryWarningThreshold(20, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("设置严重低电量阈值成功 ");
                } else {
                    toastText("设置严重低电量阈值失败 ");
                }
            }
        });
    }

    private void getSeriousLowBatteryWarningThreshold() {
        mGDUFlightController.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<LowBatteryWarnInfo>() {
            @Override
            public void onSuccess(LowBatteryWarnInfo var1) {
                toastText("获取严重低电量阈值成功 " + var1.getOneLevelWarn());
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取严重低电量阈值失败 ");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.simulator_button:
                startSimulator();
                break;
            case R.id.set_max_flight_radius_button:
                setMaxFlightRadius();
                break;
            case R.id.get_max_flight_radius_button:
                getMaxFlightRadius();
                break;
            case R.id.set_max_flight_height_button:
                setMaxFlightHeight();
                break;
            case R.id.get_max_flight_height_button:
                getMaxFlightHeight();
                break;
            case R.id.set_go_home_height_button:
                setGoHomeHeightInMeters();
                break;
            case R.id.get_go_home_height_button:
                getGoHomeHeightInMeters();
                break;
            case R.id.get_version_button:
                getFCVersion();
                break;
            case R.id.start_take_off_button:
                startTakeoff();
                break;

            case R.id.set_horizontal_speed:
                // 5m/s
                mGDUFlightController.setHorizontalSpeed((short) 500, (short) 0);
                break;
            case R.id.cancel_horizontal_speed:
                // 停止水平速度
                mGDUFlightController.stopHorizontalSpeed();
                break;
                // 垂直速度 1m/s
            case R.id.set_vertical_speed:
                mGDUFlightController.setVerticalSpeed((short) 100);
                break;
            case R.id.cancel_vertical_speed:
                // 取消垂直速度
                mGDUFlightController.stopVerticalSpeed();
                break;
            case R.id.set_drone_angle:
                mGDUFlightController.changYawAngular(RotationMode.ABSOLUTE_ANGLE, 90, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError gduError) {

                    }
                });
                break;

            case R.id.start_landing_button:
                startLanding();
                break;
            case R.id.cancel_landing_button:
                cancelLanding();
                break;
            case R.id.start_go_home_button:
                startGoHome();
                break;
            case R.id.cancel_go_home_button:
                cancelGoHome();
                break;
            case R.id.start_precision_go_home_button:
                startPrecisionGoHome();
                break;
            case R.id.cancel_precision_go_home_button:
                cancelPrecisionGoHome();
                break;
            case R.id.get_connection_fail_safe_behavior_button:
                getConnectionFailSafeBehavior();
                break;
            case R.id.set_connection_fail_safe_behavior_button:
                setConnectionFailSafeBehavior();
                break;
            case R.id.set_low_battery_warning_threshold_button:
                setLowBatteryWarningThreshold();
                break;
            case R.id.get_low_battery_warning_threshold_button:
                getLowBatteryWarningThreshold();
                break;
            case R.id.set_serious_low_battery_warning_threshold_button:
                setSeriousLowBatteryWarningThreshold();
                break;
            case R.id.get_serious_low_battery_warning_threshold_button:
                getSeriousLowBatteryWarningThreshold();
                break;
            case R.id.rtk_button:
                Intent intent = new Intent(mContext, RTKActivity.class);
                startActivity(intent);
                break;
            case R.id.assistant_button:
                Intent intentFlightAssistant = new Intent(mContext, FlightAssistantActivity.class);
                startActivity(intentFlightAssistant);
                break;
            case R.id.compass_button:
                Intent intentCompass = new Intent(mContext, CompassActivity.class);
                startActivity(intentCompass);
                break;
            case R.id.virtual_stick_button:
                Intent intentVirtualStick = new Intent(mContext, VirtualStickActivity.class);
                startActivity(intentVirtualStick);
                break;
            case R.id.get_aircraft_button:
                BaseProduct.Model model = SdkDemoApplication.getAircraftInstance().getModel();
                mAircraftModelTextview.setText(model.name());
                break;
            case R.id.get_aircraft_sn_button:
                SdkDemoApplication.getAircraftInstance().getProductSN(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String sn) {
                        showText(mAircraftSNTextview, sn);
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        showText(mAircraftSNTextview, var1.getDescription());
                    }
                });
                break;
        }
    }

}
