package com.gdu.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.api.GduDroneApi;
import com.gdu.api.GduInfoManager;
import com.gdu.api.Util.GPSTranslateGuide;
import com.gdu.api.listener.OnDroneConnectListener;
import com.gdu.api.listener.OnGduInfoListener;
import com.gdu.drone.DroneException;
import com.gdu.drone.DroneInfo;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.map.LatLng;
import com.gdu.util.logs.RonLog;

import java.util.Calendar;

/**
 * Created by zhangzhilai on 2018/5/31.
 * 1. 数传测试界面
 */

public class CE_02Activity extends Activity {

    private GduDroneApi mGduDroneApi;
    private GduInfoManager mGduInfoManager;
    private TextView mConnectStatusTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ce_o2);
        mConnectStatusTextView = (TextView) findViewById(R.id.connect_status_textview);
        init();
        initListener();
    }

    private void initListener() {
        mGduDroneApi.setOnDroneConnectListener(new OnDroneConnectListener() {
            @Override
            public void onConnectSucc() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("连接成功");
                    }
                });
            }

            @Override
            public void onConnectFail() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("连接失败");
                    }
                });
            }

            @Override
            public void onDisConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("连接断开");
                    }
                });
            }

            @Override
            public void onConnectMore() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("无控制权限");
                    }
                });
            }
        });
        mGduInfoManager.setGduInfoListener(new OnGduInfoListener() {
            @Override
            public void onInfoUpdate(DroneInfo droneInfo) {
                if (droneInfo != null) {
//                    Log.d("test", "test info " + droneInfo.getString());
                    Log.d("test", "test onInfoUpdate2 " + droneInfo.getString());
//                    Log.d("test", "test info " + droneInfo.getString());
                }
            }

            @Override
            public void onExceptionUpdate(DroneException droneException) {
//                Log.d("test", "test Exception " + droneException.getString());
                Log.d("test", "test onExceptionUpdate2 " + droneException.getString());
            }
        });
    }

    private void init(){
        mGduDroneApi = GduDroneApi.getInstance();
        mGduDroneApi.init(getApplicationContext());
        mGduInfoManager = GduInfoManager.getInstance(mGduDroneApi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGduDroneApi.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGduDroneApi.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGduDroneApi.onDestroy();
    }

    public void startConnect(View view)
    {
        Calendar calendar = Calendar.getInstance();
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);    //时区
        int dstOffset = calendar.get(Calendar.DST_OFFSET);    //夏时令
        RonLog.LogE("calendar:" + zoneOffset + ","+ dstOffset );
        System.out.println("calendar:" + zoneOffset + ","+ dstOffset + ","+ calendar.getTimeZone().getRawOffset());
        mGduDroneApi.connectUSB();

    }

    public void startConnectWifi(View view) {
        mGduDroneApi.connectWifi();
    }

    public void preview(View view) {
        Intent intent = new Intent(this, CSActivity.class);
        startActivity(intent);
    }

    public void setting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void control(View view) {
        Intent intent = new Intent(this, ControlActivity.class);
        startActivity(intent);
    }

    public void multimedia(View view) {
//        Intent intent = new Intent(this, MultimediaActivity.class);
//        startActivity(intent);
    }

    public void test(View view) {
        LatLng latLng = GPSTranslateGuide.gcj2wgs(114.42733,30.465919);
        latLng = GPSTranslateGuide.transform2Mars(30.46811899999999,114.42191800000002);
        latLng = GPSTranslateGuide.transform2Mars(30.465794183607734,114.42743501226126);
        Log.d("test", "test lng " + latLng.longitude + " lat " + latLng.latitude);
        Log.d("test", "test " + GlobalVariable.pitchAngle);
        Log.d("test", "test " + GlobalVariable.HolderSmallRoll);
    }

    public void upgrade(View view) {

    }

    public void calibrate(View view) {
        Intent intent = new Intent(this, CalibrateActivity.class);
        startActivity(intent);
    }


    public void saveLog(View view)
    {
        if(mGduDroneApi != null )
        {
            mGduDroneApi.isRecordLog2SD(true);
            Toast.makeText(this,"开启保存日志",Toast.LENGTH_SHORT).show();
        }
    }

    public void map(View view) {
//        Intent intent = new Intent(this, MapActivity.class);
//        startActivity(intent);
    }

    public void custom(View view) {
//        Intent intent = new Intent(this, CustomSocketActivity.class);
//        startActivity(intent);
    }

    public void gimbal(View view) {
        Intent intent = new Intent(this, GimbalSettingO2Activity.class);
        startActivity(intent);
    }


}
