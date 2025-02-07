package com.gdu.demo.flight.setting.firmware;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.gdu.util.CollectionUtils;
import com.gdu.util.logger.MyLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author wixche
 */
public class CycleFirmwareVersionManage {
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private IFirmwareVersion mFirmwareVersion;
    private List<ICycleGetFirmwareUpdate> callbackList = new ArrayList<>();
    /** 是否自动循环请求固件版本信息 */
    private boolean mIsAutoCycle = true;
    private int reqNum;
    /** 是否有去获取版本 */
    private boolean isHaveGetFwVersion = false;

    public CycleFirmwareVersionManage() {
        mFirmwareVersion = FirmwareVersion.getInstance();
        mFirmwareVersion.setOnFirmwareGetListener(mVersionGetListener);
        mHandlerThread = new HandlerThread("CycleGetFwVersion");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                MyLogUtils.i("handleMessage() msgWhat = " + msg.what + "; mFirmwareVersion = " + mFirmwareVersion);
                if (msg.what != 1) {
                    return;
                }
                statusUpdate(1);
                Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::getFlyFirmwareVersion);
            }
        };
    }

    /**
     * 更新中记录
     * @param status 0：默认；1:更新中；2：更新完成
     */
    private void statusUpdate(int status) {
        MyLogUtils.i("statusUpdate() status = " + status + "; callbackListSize = " + callbackList.size());
        Optional.ofNullable(callbackList).ifPresent(list -> {
            for (ICycleGetFirmwareUpdate mUpdate : list) {
                mUpdate.statusUpdate(status);
            }
        });
    }

    private final IFirmwareVersion.OnFirmwareVersionGetListener mVersionGetListener =
            new IFirmwareVersion.OnFirmwareVersionGetListener() {
        @Override
        public void onFirmwareStatusCallback(int status) {
            MyLogUtils.i("CycleFirmwareVersionManage onFirmwareStatusCallback() status = " + status +
                    "; isHaveGetFwVersion = " + isHaveGetFwVersion);
            if (status == 1 && isHaveGetFwVersion) {
                isHaveGetFwVersion = false;
            }
        }

        @Override
        public void onVersionReqEnd() {
            MyLogUtils.i("CycleFirmwareVersionManage onVersionReqEnd() isHaveGetFwVersion = " + isHaveGetFwVersion +
                    "; mIsAutoCycle = " + mIsAutoCycle);
            if (!isHaveGetFwVersion) {
                return;
            }
            Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::dispose);
            reqNum++;
            MyLogUtils.i("onVersionReqEnd() reqNum = " + reqNum);
            if (reqNum >= 3) {
                if (isHaveGetFwVersion) {
                    isHaveGetFwVersion = false;
                }
                statusUpdate(2);
            } else {
                if (mIsAutoCycle) {
                    mHandler.sendEmptyMessageDelayed(1, 2000);
                }
                updateVersionData();
            }
        }
    };

    private void updateVersionData() {
        MyLogUtils.i("updateVersionData() callbackListSize = " + callbackList.size());
        Optional.ofNullable(callbackList).ifPresent(list -> {
            for (ICycleGetFirmwareUpdate mUpdate : list) {
                mUpdate.updateVersionData();
            }
        });
    }

    public void setCycleGetVersionCallback(ICycleGetFirmwareUpdate callback) {
        MyLogUtils.i("setCycleGetVersionCallback()");
        CollectionUtils.listAddAvoidNull(callbackList, callback);
    }

    public void startCycleGetFWVersion() {
        MyLogUtils.i("CycleFirmwareVersionManage startCycleGetFWVersion()");
        startCycleGetFWVersion(true);
    }

    public void startCycleGetFWVersion(boolean isAutoCycle) {
        MyLogUtils.i("CycleFirmwareVersionManage startCycleGetFWVersion() isAutoCycle = " + isAutoCycle);
        isHaveGetFwVersion = true;
        mIsAutoCycle = isAutoCycle;
        if (mIsAutoCycle) {
            reqNum = 0;
        }
        mHandler.sendEmptyMessage(1);
    }

    public void unRegisterVersionCallback(ICycleGetFirmwareUpdate callback) {
        MyLogUtils.i("unRegisterVersionCallback()");
        Optional.ofNullable(callbackList).ifPresent(list -> list.remove(callback));
    }

    public void stopGetFwVersion() {
        MyLogUtils.i("stopGetFwVersion()");
        mHandler.removeMessages(1);
        mIsAutoCycle = true;
        isHaveGetFwVersion = false;
        Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::dispose);
    }

    public void onDestroy() {
        MyLogUtils.i("onDestroy()");
        stopGetFwVersion();
        Optional.ofNullable(mFirmwareVersion).ifPresent(version -> version.unRegisterListener(mVersionGetListener));
    }

}
