package com.gdu.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.gdu.common.ConnectScene;
import com.gdu.common.error.Error;
import com.gdu.demo.config.MyConstants;
import com.gdu.demo.utils.SPUtils;
import com.gdu.demo.widget.rc.key.RCCustomKeyActionManager;
import com.gdu.demo.widget.rc.key.RCCustomKeyRepository;
import com.gdu.lib.util.TimeUtil;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.key.value.bean.GimbalType;
import com.gdu.sdk.airlink.AirLink;
import com.gdu.sdk.base.BaseComponent;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.gimbal.Gimbal;
import com.gdu.sdk.manager.SDKInitEvent;
import com.gdu.sdk.manager.SDKManager;
import com.gdu.sdk.remotecontroller.RemoteController;
import com.gdu.sdk.util.CommonCallbacks;
import com.rxjava.rxlife.RxLife;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 *
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getName();

    private Activity mContext;
    private Button mRegisterAppButton;
    private Button mOpenButton;
    private Button mPairingButton;
    private TextView tvConnectState;
    private BaseProduct mProduct;
    private TextView tv_gimbal_type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        //如果是需要连机库时，需要设置一次ConnectScene，如果是使用遥控器直连无人机则无需设置
//        SDKManager.getInstance().setConnectScene(ConnectScene.HANGAR);
        copyAIBoxDataDb2Local();
    }


    private void initView(){
        mRegisterAppButton = findViewById(R.id.register_app_button);
        mOpenButton = findViewById(R.id.open_button);
        mPairingButton = findViewById(R.id.pairing_button);
        tvConnectState = findViewById(R.id.tv_connect_state);
        tv_gimbal_type = findViewById(R.id.tv_gimbal_type);
        mOpenButton.setEnabled(false);
        ((TextView) findViewById(R.id.version_textview)).setText(getResources().getString(R.string.sdk_version,
                SDKManager.getInstance().getSDKVersion()));
    }

    private void initListener() {
        mRegisterAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    XLogger.INSTANCE.init(mContext);
                    // 自定义按键初始化
                    RCCustomKeyRepository.INSTANCE.init();
                    RCCustomKeyActionManager.INSTANCE.init();

                    startSDKRegistration();
                }
            }
        });
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, DemoListActivity.class);
                startActivity(intent);
            }
        });
        mPairingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoteController gduRemoteController = SdkDemoApplication.getAircraftInstance().getRemoteController();
                gduRemoteController.startPairing(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(Error var1) {
                        Log.d(TAG, "test startPairing: " + var1);
                    }
                });
            }
        });
    }

    public boolean checkAndRequestPermissions() {
        //判断是否已经赋予权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
                return false;
            }
        }
        return true;
    }

    private void startSDKRegistration(){
        SDKManager.getInstance().registerApp(mContext.getApplicationContext(), new SDKManager.SDKManagerCallback() {
            @Override
            public void onRegister(Error error) {
                if (error == Error.REGISTRATION_SUCCESS) {
                    SDKManager.getInstance().startConnectionToProduct();
                }
            }

            @Override
            public void onProductDisconnect() {
                refreshUI();
            }

            @Override
            public void onProductConnect(BaseProduct product) {
                mProduct = product;
                refreshUI();
            }

            @Override
            public void onProductChanged(BaseProduct product) {

            }

            @Override
            public void onComponentChange(BaseComponent oldComponent, BaseComponent newComponent) {
                if (newComponent != null) {
                    Log.d(TAG, "onComponentChange : " + newComponent.toString());
                    newComponent.setComponentListener(mGDUComponentListener);
                    refreshComponent(newComponent);
                }
            }

            @Override
            public void onInitProcess(SDKInitEvent initEvent, int totalProcess) {

            }
        });
    }

    private BaseComponent.ComponentListener mGDUComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
        }
    };

    private void refreshUI(){
        if (mProduct != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BaseProduct.Model model = mProduct.getModel();
                    if (mProduct.isConnected()) {
                        tvConnectState.setText("飞行器已连接 型号：" + model.name());
                        mOpenButton.setEnabled(true);
                        mPairingButton.setEnabled(false);

                    } else {
                        tvConnectState.setText("飞行器未连接");
                        mOpenButton.setEnabled(false);
                    }
                }
            });
        }
    }

    private void refreshComponent(BaseComponent component){
        if (component instanceof RemoteController || component instanceof AirLink) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPairingButton.setEnabled(true);
                }
            });
        }

        if (component instanceof Gimbal) {
            Gimbal gimbal = (Gimbal) component;
            GimbalType gimbalType = gimbal.getGimbalType();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_gimbal_type.setText("云台类型："+gimbalType.getValue());

                }
            });

        }
    }

    /**
     * <p>shang</p>
     * <p>请求权限回调</p>
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 1) {
            return;
        }
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startSDKRegistration();
        for (String permission : permissions) {
//            if (PERMISSIONDENIED) {
//                continue;
//            }
        }
    }

    public void onClick(View view) {
        Intent intent = null;

        switch (view.getId()) {

        }
    }

    private void copyAIBoxDataDb2Local() {
        Log.i(TAG,"copyAIBoxDataDb2Local()");
        boolean isCopyDbFile = SPUtils.getBoolean(this, MyConstants.COPY_AI_BOX_MODEL_DB_KEY);
        File databasePath = getDatabasePath("AIBoxData.db");
        Log.i(TAG,"copyAIBoxDataDb2Local() databasePath = " + databasePath);
        //根据最近修改时间决定考不拷贝（暂定时间为25年7月30号）
        long fileLastModify = 0;
        if (databasePath.exists()) {
            fileLastModify = databasePath.lastModified();
        }
        boolean isNotNeedUpdate = fileLastModify > TimeUtil.getTimeStamp("2025-10-17 23:59:59", "yyyy-MM-dd HH:mm:ss");

        if (isNotNeedUpdate && isCopyDbFile) {
            Log.i(TAG,"copyAIBoxDataDb2Local() db file is exit");
            return;
        }
        SPUtils.put(this, MyConstants.COPY_AI_BOX_MODEL_DB_KEY, true);
        Observable.create(emitter -> {
                    File dbShmPath = getApplicationContext().getDatabasePath("AIBoxData.db-shm");
                    boolean delResult;
                    if (dbShmPath.exists()) {
                        delResult = dbShmPath.delete();
                        Log.i(TAG,"copyAIBoxDataDb2Local() delShmResult = " + delResult);
                    }
                    File dbWalPath = getApplicationContext().getDatabasePath("AIBoxData.db-wal");
                    if (dbWalPath.exists()) {
                        delResult = dbWalPath.delete();
                        Log.i(TAG,"copyAIBoxDataDb2Local() delWalResult = " + delResult);
                    }
                    if (databasePath.exists()) {
                        delResult = databasePath.delete();
                        Log.i(TAG,"copyAIBoxDataDb2Local() delDBResult = " + delResult);
                    }
                    try (InputStream is = getAssets().open("database/AIBoxData.db");
                         FileOutputStream fos = new FileOutputStream(databasePath)) {
                        byte[] data = new byte[1024];
                        int len = 0;
                        while ((len = is.read(data)) > 0) {
                            fos.write(data, 0, len);
                        }
                        Log.i(TAG,"copyAIBoxDataDb2Local() 数据拷贝完成");
                    } catch (IOException e) {
                        Log.e(TAG,"拷贝数据库文件出错", e);
                    }
                }).subscribeOn(Schedulers.io())
                .to(RxLife.toMain(this))
                .subscribe(o -> {}, throwable -> Log.e(TAG,"拷贝数据库文件出错"));
    }

}
