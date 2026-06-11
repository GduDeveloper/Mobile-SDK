package com.gdu.demo;

import android.app.Application;

import com.gdu.lib.base.GduEnvConfig;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.manager.SDKManager;
import com.gdu.sdk.products.Aircraft;
import com.yolanda.nohttp.NoHttp;


public class SdkDemoApplication extends Application {

    private static BaseProduct product;
    private static SdkDemoApplication gduApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        gduApplication = this;
        GduEnvConfig.application = gduApplication;
        GduEnvConfig.FLAVOR = "MobileSdkV4";
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        NoHttp.initialize(this);
    }


    public static synchronized BaseProduct getProductInstance() {
        product = SDKManager.getInstance().getProduct();
        return product;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }
    
    public static SdkDemoApplication getSingleApp() {
        return gduApplication;
    }
}
