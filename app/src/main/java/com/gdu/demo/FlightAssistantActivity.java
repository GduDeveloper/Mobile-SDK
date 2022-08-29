package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.radar.GDUFlightAssistantObstacleSensingDirection;
import com.gdu.sdk.flightcontroller.flightassistant.FillLightMode;
import com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * 辅助飞行
 */
public class FlightAssistantActivity extends Activity implements View.OnClickListener{

    private Context mContext;
    private FlightAssistant mFlightAssistant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_flight_assistant);
        initData();
    }

    private void initData() {
        mFlightAssistant = SdkDemoApplication.getAircraftInstance().getFlightController().getFlightAssistant();
        mFlightAssistant.getDownwardFillLightMode(new CommonCallbacks.CompletionCallbackWith<FillLightMode>() {
            @Override
            public void onSuccess(FillLightMode fillLightMode) {
                toastText("获取下视灯成功 " + fillLightMode.name());
            }

            @Override
            public void onFailure(GDUError error) {
                toastText("获取下视灯失败 ");
            }
        });
        mFlightAssistant.getLandingProtectionEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean enable) {
                toastText("获取降落保护成功 " + enable);
            }

            @Override
            public void onFailure(GDUError error) {
                toastText("获取降落保护失败 ");
            }
        });
        mFlightAssistant.getRTHObstacleAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean enable) {
                toastText("获取返航避障成功 " + enable);
            }

            @Override
            public void onFailure(GDUError error) {
                toastText("获取返航避障失败 ");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.downward_fill_light_button:
                mFlightAssistant.setDownwardFillLightMode(FillLightMode.ON, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("下视灯操作成功");
                        } else {
                            toastText("下视灯操作失败");
                        }
                    }
                });
                break;
            case R.id.landing_protection_button:
                mFlightAssistant.setLandingProtectionEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("降落保护操作成功");
                        } else {
                            toastText("降落保护操作失败");
                        }
                    }
                });
                break;
            case R.id.rth_button:
                mFlightAssistant.setRTHObstacleAvoidanceEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("返航避障操作成功");
                        } else {
                            toastText("返航避障操作失败");
                        }
                    }
                });
                break;
            case R.id.vision_sensing_button:
                mFlightAssistant.setVisionSensingEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("视觉感知操作成功");
                        } else {
                            toastText("视觉感知操作失败");
                        }
                    }
                });
                break;
            case R.id.obstacle_avoidance_strategy_button:
                mFlightAssistant.setObstacleAvoidanceStrategyEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("避障策略操作成功");
                        } else {
                            toastText("避障策略操作失败");
                        }
                    }
                });
                break;
            case R.id.horizontal_vision_obstacle_avoidance_button:
                mFlightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("水平避障操作成功");
                        } else {
                            toastText("水平避障操作失败");
                        }
                    }
                });
                break;
            case R.id.horizontal_visual_obstacles_avoidance_distance_button:
                mFlightAssistant.setVisualObstaclesAvoidanceDistance(3, GDUFlightAssistantObstacleSensingDirection.Horizontal, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("水平避障距离操作成功");
                        } else {
                            toastText("水平避障距离操作失败");
                        }
                    }
                });
                break;
            case R.id.upward_vision_obstacle_avoidance_button:
                mFlightAssistant.setUpwardVisionObstacleAvoidanceEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("上视避障操作成功");
                        } else {
                            toastText("上视避障操作失败");
                        }
                    }
                });
                break;
            case R.id.upward_visual_obstacles_avoidance_distance_button:
                mFlightAssistant.setVisualObstaclesAvoidanceDistance(2, GDUFlightAssistantObstacleSensingDirection.Upward, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toastText("上视避障距离操作成功");
                        } else {
                            toastText("上视避障距离操作失败");
                        }
                    }
                });
                break;
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
