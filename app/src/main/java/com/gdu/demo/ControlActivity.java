package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.api.GduControlManager;
import com.gdu.drone.CameraMode;
import com.gdu.drone.SwitchType;

/**
 * Created by zhangzhilai on 2018/6/5.
 * 控制类测试
 */

public class ControlActivity extends Activity {

    private Context mContext;
    private GduControlManager mGduControlManager;
    private TextView mRecordTextView;
    private TextView mObstacleTypeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_control);
        initView();
        init();
    }

    private void initView() {
        mRecordTextView = (TextView) findViewById(R.id.record_textview);
        mObstacleTypeTextView = (TextView) findViewById(R.id.obstacle_type_textview);
    }

    private void init(){
        mGduControlManager = new GduControlManager();
//        mGduControlManager.setOnRecordListener(new OnRecordListener() {
//            @Override
//            public void onRecordStart() {
//                show(mRecordTextView, "onRecordStart");
//            }
//
//            @Override
//            public void onRecording() {
//                show(mRecordTextView, "onRecording");
//            }
//
//            @Override
//            public void onRecordEnd() {
//                show(mRecordTextView, "onRecordEnd");
//            }
//
//            @Override
//            public void onRecordError(int errorCode) {
//                show(mRecordTextView, "onRecordError " + errorCode);
//            }
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

    public void startCamera(View view) {
//        mGduControlManager.takeVideo();
    }

    public void takePicture(View view) {
        mGduControlManager.takePicture(new GduControlManager.OnControlListener(){

            @Override
            public void onControlSucceed(Object type) {
                show("onControlSucceed" +  type);
            }

            @Override
            public void onControlFailed(int errorCode) {
                show("onControlFailed" +  errorCode);
            }
        });
    }

    public void stopCamera(View view) {
//        mGduControlManager.stopVideo();
    }

    public void switchCamera(View view) {
        mGduControlManager.setCameraMode(CameraMode.PICTURE, new GduControlManager.OnControlListener() {
            @Override
            public void onControlSucceed(Object type) {
                show("onControlSucceed");
            }

            @Override
            public void onControlFailed(int errorCode) {
                show("onControlFailed");
            }
        });
    }

    public void show(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void show(final TextView textView, final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }


    public void oneKeyFly(View view) {
        mGduControlManager.oneKeyFly();
    }

    public void oneKeyDown(View view) {
        mGduControlManager.oneKeyLand();
    }

    public void oneKeyLock(View view) {
        mGduControlManager.oneKeyLock();
    }

    public void holderBackCenter(View view) {
        mGduControlManager.backHolderToCenter(new GduControlManager.OnControlListener() {
            @Override
            public void onControlSucceed(Object type) {
                toast("test onControlSucceed " + type);
            }

            @Override
            public void onControlFailed(int errorCode) {
                toast("test onControlFailed " + errorCode);
            }
        });
    }

    private void toast(final String test){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, test, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 上仰云台
     * @param view
     */
    public void riseHolder(View view) {
        byte speed = 10;
        mGduControlManager.riseHolder(speed);
    }

    /**
     * 下俯云台
     * @param view
     */
    public void downHolder(View view) {
        byte speed = 10;
        mGduControlManager.downHolder(speed);
    }

    /**
     * 停止云台转动
     * @param view
     */
    public void fixedHolder(View view) {
        mGduControlManager.fixedHolder();
    }

    public void openObstacleALG(View view) {
        mGduControlManager.toggleObstacleALG(true, SwitchType.OBSTACLE_TYPE_MAIN, new GduControlManager.OnControlListener() {
            @Override
            public void onControlSucceed(Object type) {
                final boolean isOpen = (boolean) type;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mObstacleTypeTextView.setText(isOpen + "");
                    }
                });

            }

            @Override
            public void onControlFailed(int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mObstacleTypeTextView.setText("fail");
                    }
                });
            }
        });
    }

    /******************************
     *
     * @param view
     */
    public void onClick(View view)
    {
        switch ( view.getId() )
        {
            case R.id.btn_checkRc_Impeller:
                mGduControlManager.calibrationRcHolderControler(new GduControlManager.OnControlListener() {
                    @Override
                    public void onControlSucceed(Object o) {
                        handler.obtainMessage(990,"波轮校准的结果:" + o.toString()).sendToTarget();
                    }

                    @Override
                    public void onControlFailed(int i) {

                    }
                });
                break;
        }
    }



    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 990:
                    Toast.makeText(ControlActivity.this,msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public void setPitchAngle(View view) {
        mGduControlManager.setGimbalPitchAngle(-30, new GduControlManager.OnControlListener() {
            @Override
            public void onControlSucceed(Object type) {
                toast("onControlSucceed " + type);
            }

            @Override
            public void onControlFailed(int errorCode) {
                toast("onControlFailed " + errorCode);
            }
        });
    }
}
