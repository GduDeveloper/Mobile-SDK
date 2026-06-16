//package com.gdu.demo.flight.setting.firmware;
//
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Message;
//
//import androidx.annotation.NonNull;
//
//import com.gdu.lib.util.CollectionUtils;
//import com.gdu.lib.util.core.XLogger;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * @author wixche
// */
//public class CycleFirmwareVersionManage {
//    private HandlerThread mHandlerThread;
//    private Handler mHandler;
//    private IFirmwareVersion mFirmwareVersion;
//    private List<ICycleGetFirmwareUpdate> callbackList = new ArrayList<>();
//    /** 是否自动循环请求固件版本信息 */
//    private boolean mIsAutoCycle = true;
//    private int reqNum;
//    /** 是否有去获取版本 */
//    private boolean isHaveGetFwVersion = false;
//
//    public CycleFirmwareVersionManage() {
//        mFirmwareVersion = FirmwareVersion.getInstance();
//        mFirmwareVersion.setOnFirmwareGetListener(mVersionGetListener);
//        mHandlerThread = new HandlerThread("CycleGetFwVersion");
//        mHandlerThread.start();
//
//        mHandler = new Handler(mHandlerThread.getLooper()) {
//            @Override
//            public void handleMessage(@NonNull Message msg) {
//                XLogger.INSTANCE.getAPP().i("handleMessage() msgWhat = " + msg.what + "; mFirmwareVersion = " + mFirmwareVersion);
//                if (msg.what != 1) {
//                    return;
//                }
//                statusUpdate(1);
//                Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::getFlyFirmwareVersion);
//            }
//        };
//    }
//
//    /**
//     * 更新中记录
//     * @param status 0：默认；1:更新中；2：更新完成
//     */
//    private void statusUpdate(int status) {
//        XLogger.INSTANCE.getAPP().i("statusUpdate() status = " + status + "; callbackListSize = " + callbackList.size());
//        Optional.ofNullable(callbackList).ifPresent(list -> {
//            for (ICycleGetFirmwareUpdate mUpdate : list) {
//                mUpdate.statusUpdate(status);
//            }
//        });
//    }
//
//    private final IFirmwareVersion.OnFirmwareVersionGetListener mVersionGetListener =
//            new IFirmwareVersion.OnFirmwareVersionGetListener() {
//        @Override
//        public void onFirmwareStatusCallback(int status) {
//            XLogger.INSTANCE.getAPP().i("CycleFirmwareVersionManage onFirmwareStatusCallback() status = " + status +
//                    "; isHaveGetFwVersion = " + isHaveGetFwVersion);
//            if (status == 1 && isHaveGetFwVersion) {
//                isHaveGetFwVersion = false;
//            }
//        }
//
//        @Override
//        public void onVersionReqEnd() {
//            XLogger.INSTANCE.getAPP().i("CycleFirmwareVersionManage onVersionReqEnd() isHaveGetFwVersion = " + isHaveGetFwVersion +
//                    "; mIsAutoCycle = " + mIsAutoCycle);
//            if (!isHaveGetFwVersion) {
//                return;
//            }
//            Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::dispose);
//            reqNum++;
//            XLogger.INSTANCE.getAPP().i("onVersionReqEnd() reqNum = " + reqNum);
//            if (reqNum >= 3) {
//                if (isHaveGetFwVersion) {
//                    isHaveGetFwVersion = false;
//                }
//                statusUpdate(2);
//            } else {
//                if (mIsAutoCycle) {
//                    mHandler.sendEmptyMessageDelayed(1, 2000);
//                }
//                updateVersionData();
//            }
//        }
//    };
//
//    private void updateVersionData() {
//        XLogger.INSTANCE.getAPP().i("updateVersionData() callbackListSize = " + callbackList.size());
//        Optional.ofNullable(callbackList).ifPresent(list -> {
//            for (ICycleGetFirmwareUpdate mUpdate : list) {
//                mUpdate.updateVersionData();
//            }
//        });
//    }
//
//    public void setCycleGetVersionCallback(ICycleGetFirmwareUpdate callback) {
//        XLogger.INSTANCE.getAPP().i("setCycleGetVersionCallback()");
//        CollectionUtils.listAddAvoidNull(callbackList, callback);
//    }
//
//    public void startCycleGetFWVersion() {
//        XLogger.INSTANCE.getAPP().i("CycleFirmwareVersionManage startCycleGetFWVersion()");
//        startCycleGetFWVersion(true);
//    }
//
//    public void startCycleGetFWVersion(boolean isAutoCycle) {
//        XLogger.INSTANCE.getAPP().i("CycleFirmwareVersionManage startCycleGetFWVersion() isAutoCycle = " + isAutoCycle);
//        isHaveGetFwVersion = true;
//        mIsAutoCycle = isAutoCycle;
//        if (mIsAutoCycle) {
//            reqNum = 0;
//        }
//        mHandler.sendEmptyMessage(1);
//    }
//
//    public void unRegisterVersionCallback(ICycleGetFirmwareUpdate callback) {
//        XLogger.INSTANCE.getAPP().i("unRegisterVersionCallback()");
//        Optional.ofNullable(callbackList).ifPresent(list -> list.remove(callback));
//    }
//
//    public void stopGetFwVersion() {
//        XLogger.INSTANCE.getAPP().i("stopGetFwVersion()");
//        mHandler.removeMessages(1);
//        mIsAutoCycle = true;
//        isHaveGetFwVersion = false;
//        Optional.ofNullable(mFirmwareVersion).ifPresent(IFirmwareVersion::dispose);
//    }
//
//    public void onDestroy() {
//        XLogger.INSTANCE.getAPP().i("onDestroy()");
//        stopGetFwVersion();
//        Optional.ofNullable(mFirmwareVersion).ifPresent(version -> version.unRegisterListener(mVersionGetListener));
//    }
//
//}
