package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.remotecontroller.AircraftMappingStyle;
import com.gdu.remotecontroller.MultiControlInfo;
import com.gdu.remotecontroller.MultiControlMode;
import com.gdu.sdk.remotecontroller.GDURemoteController;
import com.gdu.sdk.util.CommonCallbacks;

import java.util.List;

/**
 * 遥控器测试
 */
public class RemoteControllerActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private GDURemoteController mGDURemoteController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_controller);
        mContext = this;
        initView();
        initData();
    }


    private void initView() {

    }

    private void initData() {
        mGDURemoteController = SdkDemoApplication.getAircraftInstance().getRemoteController();
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_control_hand_button:
                setControlHand();
                break;
            case R.id.get_control_hand_button:
                getControlHand();
                break;
            case R.id.start_pairing_button:
                startPairing();
                break;
            case R.id.get_rc_version_button:
                getRCVersion();
                break;
            case R.id.get_rc_sn_button:
                getRCSN();
                break;
            case R.id.get_multi_control_mode_button:
                getMultiControlMode();
                break;
            case R.id.set_multi_control_mode_button:
                setMultiControlMode();
                break;
            case R.id.get_drone_list_button:
                getDroneList();
                break;
            case R.id.get_rc_list_button:
                getRCList();
                break;
            case R.id.set_drone_control_enable_button:
                setDroneControlEnable();
                break;
            case R.id.get_drone_control_enable_button:
                getDroneControlEnable();
                break;
            case R.id.set_drone_living_enable_button:
                setDroneLivingEnable();
                break;
            case R.id.get_drone_living_enable_button:
                getDroneLivingEnable();
                break;
            case R.id.set_continue_send_rc_control_mid_value_enable_button:
                setContinueSendRCControlMidValueEnable();
                break;
            case R.id.get_continue_send_rc_control_mid_value_enable_button:
                getContinueSendRCControlMidValueEnable();
                break;
        }
    }

    int type = 1;
    private void setControlHand() {
        AircraftMappingStyle aircraftMappingStyle;
        if (type % 3 == 0) {
            aircraftMappingStyle = AircraftMappingStyle.STYLE_1;
            type++;
        } else if (type % 3 == 1) {
            aircraftMappingStyle = AircraftMappingStyle.STYLE_2;
            type++;
        } else {
            aircraftMappingStyle = AircraftMappingStyle.STYLE_3;
            type++;
        }
        if (mGDURemoteController != null) {
            mGDURemoteController.setAircraftMappingStyle(aircraftMappingStyle, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        toastText("设置控制手成功");
                    } else {
                        toastText("设置控制手失败");
                    }
                }
            });
        }
    }

    private void setContinueSendRCControlMidValueEnable(){
        mGDURemoteController.setContinueSendRCControlMidValueEnable(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    toastText("设置持续发送中值成功：");
                } else {
                    toastText("设置持续发送中值失败：");
                }
            }
        });
    }

    private void getContinueSendRCControlMidValueEnable(){
        mGDURemoteController.getContinueSendRCControlMidValueEnable(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                toastText("获取持续发送中值成功：" + aBoolean);
            }

            @Override
            public void onFailure(GDUError gduError) {
                toastText("获取持续发送中值失败：");
            }
        });
    }

    private void getMultiControlMode(){
        mGDURemoteController.getMultiControlMode(new CommonCallbacks.CompletionCallbackWith<MultiControlMode>() {
            @Override
            public void onSuccess(MultiControlMode controlMode) {
                toastText("获取多控模式成功：" + controlMode);
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取多控模式失败：");
            }
        });
    }

    private void setMultiControlMode(){
        mGDURemoteController.setMultiControlMode(MultiControlMode.TWO_C_ONE, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    toastText("设置多控模式成功：");
                } else {
                    toastText("设置多控模式失败：");
                }
            }
        });
    }

    private void getDroneList(){
        mGDURemoteController.getDroneList(new CommonCallbacks.CompletionCallbackWith<List<MultiControlInfo>>() {
            @Override
            public void onSuccess(List<MultiControlInfo> controlInfos) {
                toastText("获取多控飞机列表成功：" + controlInfos.size());
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取多控飞机列表失败：");
            }
        });
    }

    private void getRCList(){
        mGDURemoteController.getRCList(new CommonCallbacks.CompletionCallbackWith<List<MultiControlInfo>>() {
            @Override
            public void onSuccess(List<MultiControlInfo> controlInfos) {
                toastText("获取多控遥控器列表成功：" + controlInfos.size());
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取多控遥控器列表失败：");
            }
        });
    }

    private void setDroneControlEnable(){
        mGDURemoteController.setDroneControlEnable(0, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    toastText("设置飞机控制权开关成功：");
                } else {
                    toastText("设置飞机控制权开关失败：");
                }
            }
        });
    }

    private void getDroneControlEnable(){
        mGDURemoteController.getDroneControlEnable(0, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean var1) {
                toastText("获取飞机控制权开关成功：" + var1);
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取飞机控制权开关失败：" + var1);
            }
        });
    }

    private void setDroneLivingEnable(){
        mGDURemoteController.setDroneLivingEnable(0, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    toastText("设置飞机视频流开关成功：");
                } else {
                    toastText("设置飞机视频流开关失败：");
                }
            }
        });
    }

    private void getDroneLivingEnable(){
        mGDURemoteController.getDroneLivingEnable(0, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean var1) {
                toastText("获取飞机视频流开关成功：" + var1);
            }

            @Override
            public void onFailure(GDUError var1) {
                toastText("获取飞机视频流开关失败：" + var1);
            }
        });
    }

    private void getControlHand() {
        if (mGDURemoteController != null) {
            mGDURemoteController.getAircraftMappingStyle(new CommonCallbacks.CompletionCallbackWith<AircraftMappingStyle>() {
                @Override
                public void onSuccess(AircraftMappingStyle mappingStyle) {
                    toastText("获取控制手 " + mappingStyle);
                }

                @Override
                public void onFailure(GDUError error) {
                    toastText("获取控制手失败");
                }
            });
        }
    }

    private void startPairing(){
        if (mGDURemoteController != null) {
            mGDURemoteController.startPairing(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        toastText("对频指令发送成功");
                    } else {
                        toastText("对频指令发送失败");
                    }
                }
            });
        }
    }

    private void getRCVersion(){
        if (mGDURemoteController != null) {
            mGDURemoteController.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String version) {
                    toastText("版本号：" + version);
                }

                @Override
                public void onFailure(GDUError error) {
                    toastText("版本号：" + error);
                }
            });
        }
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


    private void getRCSN(){
        if (mGDURemoteController != null) {
            String sn = mGDURemoteController.getRCSN();
            toastText("SN：" + sn);
        }
    }

    private void toastText(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
