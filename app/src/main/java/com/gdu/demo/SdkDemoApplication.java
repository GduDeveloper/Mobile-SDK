package com.gdu.demo;

import android.app.Application;

import com.yolanda.nohttp.NoHttp;

/**
 * Created by Woo on 2018-12-6.
 */

public class SdkDemoApplication extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        NoHttp.initialize(this);
    }
}
