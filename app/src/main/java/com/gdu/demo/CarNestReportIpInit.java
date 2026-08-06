package com.gdu.demo;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gdu.lib.util.RCUtils;
import com.gdu.lib.util.core.NetworkUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.interfaces.IGduCarNestDevice;
import com.gdu.msdk.key.callback.MSdkCallback;
import com.gdu.msdk.key.error.MError;
import com.gdu.msdk.key.value.common.EmptyMsg;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

public class CarNestReportIpInit {

    @SuppressLint("CheckResult")
    public static void initCarNestReportIp() {
        XLogger.APP.i("ReportWifiIp start ");
            Observable.interval(0, 3, TimeUnit.SECONDS)
                    .subscribe(aLong -> {
                        XLogger.APP.i("ReportWifiIp wifiConnect  = " + NetworkUtils.isWifiConnected());
                        if (NetworkUtils.isWifiConnected()) {
                            String ip = NetworkUtils.getIpAddressByWifi();
                            boolean needPushStream = !RCUtils.INSTANCE.isRCSEE();
                            byte devicesType = (byte) (RCUtils.INSTANCE.isRCSEE() ? 2 : 1);
                            XLogger.APP.i("ReportWifiIp ip = " + ip + ", needPushStream = " + needPushStream + ", needPushData = " + true + ",type = " + devicesType);
                            //20260306 根据@志来@秋斌@黎锡强 决策，因车机遥控器当平板用方案，暂时将needPushStream 直接改为true
                            IGduCarNestDevice.get().getCarNest().reportCurrentIp(ip, true, true, devicesType,
                                    new MSdkCallback.ActionCallback<EmptyMsg>() {
                                        @Override
                                        public void onSuccess(@Nullable EmptyMsg result) {
                                            XLogger.APP.i("ReportWifiIp result = " + result.getCode());
                                        }

                                        @Override
                                        public void onFailure(@NonNull MError error) {
                                            XLogger.APP.i("ReportWifiIp result = fail");
                                        }
                                    });
                        }
                    }, throwable -> {
                        XLogger.APP.i("ReportWifiIp error e   = " + throwable.toString());
                    });


    }

}
