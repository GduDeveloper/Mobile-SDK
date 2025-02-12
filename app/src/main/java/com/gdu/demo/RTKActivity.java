package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.rtk.NetworkServiceSettings;
import com.gdu.rtk.NetworkServiceState;
import com.gdu.rtk.RTKState;
import com.gdu.rtk.ReferenceStationSource;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.rtk.RTK;
import com.gdu.sdk.util.CommonCallbacks;


/**
 * RTK测试
 */
public class RTKActivity extends Activity implements View.OnClickListener {

   private Context mContext;
   private TextView mReadTextView;
   private TextView mWriteTextView;
   private TextView mRTKStatusTextView;
   private TextView mNetworkServiceTextView;
   private TextView mFlightControllerStateTextView;

   private RTK rtk;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtk);
        mReadTextView = findViewById(R.id.read_data_textview);
        mWriteTextView = findViewById(R.id.write_data_textview);
        mRTKStatusTextView = findViewById(R.id.rtk_status_textview);
        mNetworkServiceTextView = findViewById(R.id.network_service_state_textview);
        mFlightControllerStateTextView = findViewById(R.id.flight_controller_state_textview);
        mContext = this;
        initRTK();
    }

    private void initRTK() {
        rtk = SdkDemoApplication.getAircraftInstance().getFlightController().getRTK();
        rtk.setStateCallback(new RTKState.Callback() {
            @Override
            public void onUpdate(RTKState state) {
                StringBuilder s = new StringBuilder();
                s.append("heading: ");
                s.append(state.getHeadingSolution());
                s.append("positioning: ");
                s.append(state.getPositioningSolution());
                s.append("ellipsoidHeight: ");
                s.append(state.getEllipsoidHeight());
                showText(mRTKStatusTextView, s.toString());
            }
        });
        SdkDemoApplication.getAircraftInstance().getFlightController().setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                showText(mFlightControllerStateTextView, flightControllerState.getString());
            }
        });
    }

    private void initListener() {
//        mGduRtkManager.setOnRtkListener(new GduRtkManager.OnRtkListener() {
//            @Override
//            public void onServerConnected() {
//                showText(mRTKStatusTextView, "服务器连接成功");
//            }
//
//            @Override
//            public void onReconnect() {
//                showText(mRTKStatusTextView, "服务器重连中");
//            }
//
//            @Override
//            public void onCommunicate() {
//                showText(mRTKStatusTextView, "服务器数据通信中");
//            }
//
//            @Override
//            public void onDisConnect() {
//                showText(mRTKStatusTextView, "服务器数据断开连接");
//            }
//
//            @Override
//            public void onConnectFailed(RTKNetConnectStatus rtkNetConnectStatus) {
//                showText(mRTKStatusTextView, "服务器连接失败 " + rtkNetConnectStatus.getValue());
//            }
//
//        });
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
            case R.id.custom_network_service_button:
                if (rtk != null) {
                    rtk.setReferenceStationSource(ReferenceStationSource.CUSTOM_NETWORK_SERVICE, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                toastText("设置自定义网络RTK成功");
                            } else {
                                toastText("设置自定义网络RTK失败");
                            }
                        }
                    });
                }
                break;
            case R.id.onboard_rtk_button:
                if (rtk != null) {
                    rtk.setReferenceStationSource(ReferenceStationSource.ONBOARD_RTK, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                toastText("设置机载RTK成功");
                            } else {
                                toastText("设置机载RTK失败");
                            }
                        }
                    });
                }
                break;
            case R.id.set_qx_account_button:
                NetworkServiceSettings networkServiceSettings = new NetworkServiceSettings();
                networkServiceSettings.setMountPoint("RTCM32_GGB");
                networkServiceSettings.setPassword("4b28b6e");
                networkServiceSettings.setPort(8002);
                networkServiceSettings.setServerAddress("rtk.ntrip.qxwz.com");
                networkServiceSettings.setUserName("qxtdfk0016");
                break;
            case R.id.connect_custom_network_service_button:
                break;
        }
    }


    public void close(View view) {

    }

    private void showText(final TextView textView, final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
