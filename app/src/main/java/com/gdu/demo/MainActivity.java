package com.gdu.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gdu.common.error.GDUError;
import com.gdu.config.GlobalConfig;
import com.gdu.sdk.airlink.GDUAirLink;
import com.gdu.sdk.base.BaseComponent;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.manager.GDUSDKInitEvent;
import com.gdu.sdk.manager.GDUSDKManager;
import com.gdu.sdk.remotecontroller.GDURemoteController;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.logs.RonLog;

/**
 *
 */
public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getName();

    private Activity mContext;
    private Button mRegisterAppButton;
    private Button mOpenButton;
    private Button mPairingButton;
    private BaseProduct mProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        RonLog.showLog(true);
    }


    private void initView(){
        mRegisterAppButton = findViewById(R.id.register_app_button);
        mOpenButton = findViewById(R.id.open_button);
        mPairingButton = findViewById(R.id.pairing_button);
        ((TextView) findViewById(R.id.version_textview)).setText(getResources().getString(R.string.sdk_version,
                GDUSDKManager.getInstance().getSDKVersion(mContext)
                        + " Debug:"
                        + GlobalConfig.DEBUG));
    }

    private void initListener() {
        mRegisterAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
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
                GDURemoteController gduRemoteController = SdkDemoApplication.getAircraftInstance().getRemoteController();
                if (gduRemoteController != null) {
                    gduRemoteController.startPairing(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError var1) {
                            Log.d(TAG, "test startPairing: " + var1);
                        }
                    });
                }
            }
        });
    }

    public boolean checkAndRequestPermissions() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 1);
                return false;
        }
        return true;
    }

    private void startSDKRegistration(){
        GDUSDKManager.getInstance().registerApp(mContext.getApplicationContext(), new GDUSDKManager.SDKManagerCallback() {
            @Override
            public void onRegister(GDUError error) {
                if (error == GDUError.REGISTRATION_SUCCESS) {
                    GDUSDKManager.getInstance().startConnectionToProduct();
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
            public void onInitProcess(GDUSDKInitEvent initEvent, int totalProcess) {

            }
        });
    }

    private BaseComponent.ComponentListener mGDUComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
//            ((Activity)mContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext, "test " + isConnected, Toast.LENGTH_SHORT).show();
//                }
//            });
//            notifyStatusChange();
        }
    };

    private void refreshUI(){
        if (mProduct != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProduct.isConnected()) {
                        mOpenButton.setEnabled(true);
                        mPairingButton.setEnabled(false);
                    } else {
                        mOpenButton.setEnabled(false);
                    }
                }
            });
        }
    }

    private void refreshComponent(BaseComponent component){
        if (component instanceof GDURemoteController || component instanceof GDUAirLink) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPairingButton.setEnabled(true);
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

}
