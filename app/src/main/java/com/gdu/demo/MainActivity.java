package com.gdu.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gdu.api.GduDroneApi;
import com.gdu.api.GduInfoManager;
import com.gdu.api.listener.OnDroneConnectListener;
import com.gdu.api.listener.OnGduInfoListener;
import com.gdu.drone.DroneException;
import com.gdu.drone.DroneInfo;
import com.gdu.drone.GimbalType;
import com.gdu.gdusocketmodel.GlobalVariable;

public class MainActivity extends Activity {

    private GduDroneApi mGduDroneApi;
    private GduInfoManager mGduInfoManager;
    private TextView mConnectStatusTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ce_saga);
        mConnectStatusTextView = (TextView) findViewById(R.id.connect_status_textview);
        requestStoragePermission();
        init();
        initListener();
        GduDroneApi.getInstance().registerSomething(this);
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
                    Log.d("gdu", "gdu onInfoUpdate2 " + droneInfo.getString());
                }
            }

            @Override
            public void onExceptionUpdate(DroneException droneException) {
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

    public boolean requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGduDroneApi.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGduDroneApi.unregisterSomethiong(this);
        mGduDroneApi.onDestroy();
    }

    public void startConnect(View view) {
        switch (view.getId())
        {
            default:
                if(mGduDroneApi != null )
                {
                    mGduDroneApi.connectUSB2();
                }
        }
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



    public void calibrate(View view) {//校磁
        Intent intent = new Intent(this, CalibrateActivity.class);
        startActivity(intent);
    }

    public void map(View view) {//航迹规划,简单版的航迹规划，
        switch (view.getId())
        {
            case R.id.btn_seniorPlanning:
                //高级航迹规划
                Intent intent1 = new Intent(this, SeniorPlanningActivity.class);
                startActivity(intent1);
                break;
        }
    }


    public void gimbal4k(View view) {
        if(GlobalVariable.gimbalType != GimbalType.ByrdT_4k)
        {
            Toast.makeText(this,"当前挂载的云台，非4k云台",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent  intent = new Intent(this, GimbalSetting4kActivity.class);
        startActivity(intent);
    }

    public void gimbal10X(View view) {
//        if(GlobalVariable.gimbalType == GimbalType.ByrdT_10X_Zoom || GlobalVariable.gimbalType == GimbalType.ByrdT_10X_C_Zoom) {
        if(true) {
            Intent  intent = new Intent(this, GimbalSettingZoom10Activity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this,"当前挂载的云台，非10倍变倍云台",Toast.LENGTH_SHORT).show();
        }
    }

    public void gimbal30X(View view) {
        if(GlobalVariable.gimbalType != GimbalType.ByrdT_30X_Zoom)
        {
            Toast.makeText(this,"当前挂载的云台，非30倍变倍云台",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent  intent = new Intent(this, GimbalSettingZoom30Activity.class);
        startActivity(intent);
    }

    public void targetRecognize(View view) {
        Intent intent = new Intent(this, AIActivity.class);
        startActivity(intent);
    }

    public void upgrade(View view) {
        Intent intent = new Intent(this, UpgradeActivity.class);
        startActivity(intent);
    }
}
