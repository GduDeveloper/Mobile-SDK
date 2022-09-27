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
import com.gdu.sdk.remotecontroller.GDURemoteController;
import com.gdu.sdk.util.CommonCallbacks;

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

    private void toastText(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
