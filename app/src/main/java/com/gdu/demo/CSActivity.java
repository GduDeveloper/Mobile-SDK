package com.gdu.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gdu.api.GduControlManager;
import com.gdu.api.GduDroneApi;
import com.gdu.api.GduInfoManager;
import com.gdu.api.GduPlayView;
import com.gdu.api.listener.OnDroneConnectListener;
import com.gdu.api.listener.OnGduInfoListener;
import com.gdu.api.listener.OnGetH264CallBack;
import com.gdu.api.listener.OnLowBatteryReturnListener;
import com.gdu.api.listener.OnPreviewListener;
import com.gdu.api.listener.OnRecordListener;
import com.gdu.api.listener.OnTakePictureListener;
import com.gdu.demo.util.SeniorPlanningUtils;
import com.gdu.drone.DroneException;
import com.gdu.drone.DroneInfo;


/**
 * Created by zhangzhilai on 2018/5/31.
 * 2. 图传测试界面
 */

public class CSActivity extends Activity {

    private GduPlayView mGduPlayView;
    private TextView mConnectStatusTextView;
    private TextView mInfoTextView;
    private TextView mRecordInfoTextView;
    private TextView mFlyInfoTextView;
    private TextView warnInfo;
    private Context mContext;
    private GduInfoManager mGduInfoManager;
    private GduControlManager mGduControlManager;
    private TextView mVideoPicTextView;
    private SeniorPlanningUtils seniorPlanningUtils;
    private TextView textView_planningInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_cs);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        GduDroneApi gduDroneApi = GduDroneApi.getInstance();
        mGduInfoManager = GduInfoManager.getInstance(gduDroneApi);
        mGduControlManager = new GduControlManager();
        seniorPlanningUtils = new SeniorPlanningUtils(this,textView_planningInfo);
    }

    private void initListener() {
        GduDroneApi.getInstance().setOnDroneConnectListener(new OnDroneConnectListener() {
            @Override
            public void onConnectSucc() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接成功");
                    }
                });
            }

            @Override
            public void onConnectFail() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接失敗");
                    }
                });
            }

            @Override
            public void onDisConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接斷開");
                    }
                });
            }

            @Override
            public void onConnectMore() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接斷開");
                    }
                });
            }
        });
        mGduPlayView.setOnTakePictureListener(new OnTakePictureListener() {
            @Override
            public void onTakePictureSucceed(String name) {
                Log.d("test", "test onTakePictureSucceed " + name);
                toast("onTakePictureSucceed" + name);
            }

            @Override
            public void onTakePictureFailed(int errorCode) {
                toast("onTakePictureFailed" + errorCode);
            }

            @Override
            public String getPictureName() {
                return "";
            }
        });
        mGduPlayView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onRecordStart(String videoName) {
                Log.d("test", "test onRecordStart " + videoName);
                toast("onRecordStart:" + videoName);
            }

            @Override
            public void onRecording(short time) {
                toast("onRecording:"+ time+","+ System.currentTimeMillis());
            }

            @Override
            public void onRecordEnd(String videoName) {
                toast("onRecordEnd:"+ System.currentTimeMillis());

            }

            @Override
            public void onRecordError(int i) {
                toast("onRecordError" + i);
            }
        });
        mGduInfoManager.setGduInfoListener(new OnGduInfoListener() {
            @Override
            public void onInfoUpdate(final DroneInfo droneInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFlyInfoTextView.setText(droneInfo.getString());
                    }
                });
                Log.d("test", "test onInfoUpdate1 " + droneInfo.getString());
            }

            @Override
            public void onExceptionUpdate(final DroneException droneException) {
                Log.d("test", "test onExceptionUpdate1 " + droneException.getString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        warnInfo.setText(droneException.getString());
                    }
                });
            }
        });
        mGduInfoManager.setOnLowBatteryReturnListener(new OnLowBatteryReturnListener() {
            @Override
            public void onReciprocal(int i) {
                toast("低电量返航倒计时： " + i);
            }

            @Override
            public void onSmartBack(int i) {

            }
        });
    }

    private void initView() {
        textView_planningInfo = (TextView)findViewById(R.id.tv_planningInfo);
        mConnectStatusTextView = (TextView) findViewById(R.id.connect_status_textview);
        mGduPlayView = (GduPlayView) findViewById(R.id.surface_view);
        mGduPlayView.setOpaque(false);
        mInfoTextView = (TextView) findViewById(R.id.info_textview);
        mVideoPicTextView = (TextView) findViewById(R.id.video_pic_textview);
        mRecordInfoTextView = (TextView) findViewById(R.id.record_info_textview);
        mFlyInfoTextView = (TextView) findViewById(R.id.fly_info_textview);
        warnInfo = (TextView) findViewById(R.id.pps_textview);
        mGduPlayView.init(new OnPreviewListener() {
            @Override
            public void onStartPreviewSucceed() {
                toast("onStartPreviewSucceed");
            }

            @Override
            public void onStartPreviewFailed(int errorCode) {
                toast("onStartPreviewFailed " + errorCode);
            }

            @Override
            public void onStopPreviewSucceed() {
                toast("onStopPreviewSucceed");
            }

            @Override
            public void onStopPreviewFailed(int errorCode) {
                toast("onStopPreviewFailed " + errorCode);
            }
        });
        mGduPlayView.setOnGetH264Cb(new OnGetH264CallBack() {
            @Override
            public void onGetH264(byte[] data, int length) {
                Log.d("test", "test onGetH264 length: " + length);
//                mCustomRTMPSender.sendVideoData(data, length);
            }

            @Override
            public void onGetSpsPps(byte[] spsData, byte[] ppsData) {
                Log.d("test", "test onGetSpsPps: " + spsData);
//                List<byte[]> spsPps = new ArrayList<>();
//                spsPps.add(spsData);
//                spsPps.add(ppsData);
//                mCustomRTMPSender.setSpsAndPps(spsPps);
//                mCustomRTMPSender.connectRTMP("rtmp://www.easydss.com:10085/hls/HJpyzYLSm?sign=rkeaJGK8HQ");
            }
        });
    }

    public void toast(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mVideoPicTextView.setText(toast);
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void start_preview(View view) {
        mGduPlayView.startPreview();
    }

    public void stop_preview(View view) {
        mGduPlayView.stopPreview();
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_beginRecord:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                        // No explanation needed, we can request the permission.
                        Log.e("eeeeeeee","录像没有读写权限");
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                101);
                        return;
                }
                mGduPlayView.takeVideo();
                break;
            case R.id.btn_stopRecord:
                mGduPlayView.stopVideo();
                break;
            case R.id.routePlan:
                Intent intent1 = new Intent(this, SeniorPlanningActivity.class);
                startActivity(intent1);
                break;
            case R.id.oneKeyFly:
                mGduControlManager.oneKeyFly(new GduControlManager.OnControlListener() {
                    @Override
                    public void onControlSucceed(Object o) {

                    }

                    @Override
                    public void onControlFailed(int i) {

                    }
                });
                break;
            case R.id.oneKeyLand:
                mGduControlManager.oneKeyLand(new GduControlManager.OnControlListener() {
                    @Override
                    public void onControlSucceed(Object o) {

                    }

                    @Override
                    public void onControlFailed(int i) {

                    }
                });
                break;
            case R.id.oneKeyBack:
                mGduControlManager.oneKeyBack(new GduControlManager.OnControlListener() {
                    @Override
                    public void onControlSucceed(Object o) {

                    }

                    @Override
                    public void onControlFailed(int i) {

                    }
                });
                break;
            case R.id.cancelBack:
                mGduControlManager.cancelBack(new GduControlManager.OnControlListener() {
                    @Override
                    public void onControlSucceed(Object o) {

                    }

                    @Override
                    public void onControlFailed(int i) {

                    }
                });
                break;
            case R.id.btn_takePicture:
                mGduPlayView.setPicPath("/storage/emulated/0/LandUAV/测试/media/photo");
                mGduPlayView.takePicture();
                break;

            case R.id.gimbalAngle:
//                mGduControlManager.setGimbalAngle((byte) -60, new GduControlManager.OnControlListener() {
//                    @Override
//                    public void onControlSucceed(Object o) {
//                        toast("setGimbalAngle succeed");
//                    }
//
//                    @Override
//                    public void onControlFailed(int i) {
//                        toast("setGimbalAngle failed " + i);
//                    }
//                });
                break;
                default:
                    seniorPlanningUtils.onClick(view);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101 )
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mGduPlayView.beginRecord("/mnt/sdcard/gdu","ron.mp4");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGduPlayView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGduPlayView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGduPlayView.onDestroy();
    }
}
