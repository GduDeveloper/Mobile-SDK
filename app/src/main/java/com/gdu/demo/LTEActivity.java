package com.gdu.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.demo.databinding.ActivityLteBinding;
import com.gdu.drone.AirlinkType;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.lte.LTELinkInfo;
import com.gdu.sdk.lte.LTEManager;
import com.gdu.sdk.util.CommonCallbacks;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * LTE备份链路
 */
public class LTEActivity extends Activity {
    private ActivityLteBinding viewBinding;
    private Disposable mDisposable;
    private LTEManager mLTEManager;
    private GDUFlightController mGDUFlightController;
    private FlightControllerState.Callback mFCStateCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityLteBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        initData();
    }

    private void initView() {
        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView textView = findViewById(R.id.tv_title);
        textView.setText("链路切换");

        viewBinding.btAuto.setOnClickListener(listener);
        viewBinding.btLte.setOnClickListener(listener);
        viewBinding.btImage.setOnClickListener(listener);
        viewBinding.btnSetMqttServiceUrl.setOnClickListener(listener);
        viewBinding.btnGetMqttServiceUrl.setOnClickListener(listener);
        viewBinding.btnGetVideoServiceUrl.setOnClickListener(listener);
        viewBinding.btnSetVideoServiceUrl.setOnClickListener(listener);
        viewBinding.btnInitMqtt.setOnClickListener(listener);

    }

    @SuppressLint("CheckResult")
    private void initData() {
        mLTEManager = SdkDemoApplication.getAircraftInstance().getLTE();
        mLTEManager.setLTELinkInfoCallback(new LTELinkInfo.Callback() {
            @Override
            public void onUpdate(LTELinkInfo state) {
                runOnUiThread(() -> {
                    if (state != null) {
                        viewBinding.tvLteState.setText(state.toString());
                    }
                });
            }
        });

        // Flight controller status callback
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        if (mGDUFlightController != null) {
            mFCStateCallback = new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                    runOnUiThread(() -> {
                        if (flightControllerState != null) {
                            // Use getString() for a readable summary
                            viewBinding.tvFcState.setText(flightControllerState.getString());
                        }
                    });
                }
            };
            mGDUFlightController.setStateCallback(mFCStateCallback);
        }

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_auto:
                    changeLink(AirlinkType.AUTO);
                case R.id.bt_lte:
                    changeLink(AirlinkType.CELLULAR_MOBILE_LINK);
                    break;
                case R.id.bt_image:
                    changeLink(AirlinkType.IM_LINK);
                    break;
                case R.id.btn_set_mqtt_service_url:
                    setMqttServiceUrl("");
                    break;
                case R.id.btn_get_mqtt_service_url:
                    getMqttServiceUrl();
                    break;
                case R.id.btn_get_video_service_url:
                    getVideoServiceUrl();
                    break;
                case R.id.btn_set_video_service_url:
                    setVideoServiceUrl();
                    break;
                case R.id.btn_init_mqtt:
                    connectMqtt();
                    break;
            }
        }
    };

    private void changeLink(AirlinkType airlinkType) {
        mLTEManager.setAirlinkType(airlinkType, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            Toast.makeText(LTEActivity.this, "切换链路成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LTEActivity.this, "切换链路失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void setMqttServiceUrl(String url) {
        mLTEManager.setMqttServiceUrl(url, error -> runOnUiThread(() -> {
            if (error == null) {
                Toast.makeText(this, "设置MQTT地址成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置MQTT地址失败", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void getMqttServiceUrl() {
        mLTEManager.getMqttServiceUrl(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                runOnUiThread(() -> Toast.makeText(LTEActivity.this, "MQTT地址: " + s, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(com.gdu.common.error.GDUError error) {
                runOnUiThread(() -> Toast.makeText(LTEActivity.this, "获取MQTT地址失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void getVideoServiceUrl() {
        mLTEManager.getVideoServiceUrl(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                runOnUiThread(() -> Toast.makeText(LTEActivity.this, "视频地址: " + s, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(com.gdu.common.error.GDUError error) {
                runOnUiThread(() -> Toast.makeText(LTEActivity.this, "获取视频地址失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setVideoServiceUrl() {
        mLTEManager.setVideoServiceUrl("", error -> runOnUiThread(() -> {
            if (error == null) {
                Toast.makeText(this, "设置视频地址成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置视频地址失败", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void connectMqtt() {
        mLTEManager.connectMqttService(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    runOnUiThread(() -> Toast.makeText(LTEActivity.this, "MQTT连接成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(LTEActivity.this, "MQTT连接失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        // Unregister flight controller callback to avoid leaks
        try {
            if (mGDUFlightController != null && mFCStateCallback != null) {
                mGDUFlightController.setStateCallback(null);
            }
        } catch (Exception ignored) {

        }
    }
}
