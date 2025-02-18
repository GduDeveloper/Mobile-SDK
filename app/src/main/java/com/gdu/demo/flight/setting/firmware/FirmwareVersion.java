package com.gdu.demo.flight.setting.firmware;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.gdu.api.upgrade.UpgradeUtils;
import com.gdu.beans.FirmwareTypeAndVersionBean;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.drone.FirmwareType;
import com.gdu.drone.GimbalType;
import com.gdu.drone.ObstacleType;
import com.gdu.drone.PlanType;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.GduSocketManager;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.CollectionUtils;
import com.gdu.util.DataUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.GimbalUtil;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.UpgradeLog2File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 飞机和遥控器版本管理类
 * 如果获取失败，则重试3次
 */
public class FirmwareVersion implements IFirmwareVersion {
    private PlanType mCurrentFlyType;

    /** 已应答的请求数(最大数为请求固件类型总数) */
    private final AtomicInteger mCounter = new AtomicInteger(0);
    /** 是否正在获取固件中 */
    private volatile boolean isGetVersion;

    private List<OnFirmwareVersionGetListener> mOnFirmwareVersionGetListenerList = new ArrayList<>();
    private Disposable mDisposable;

    private static FirmwareVersion mInstance;
    private static final Lock mLock = new ReentrantLock();
    /** 超时判断时间(ms) */
    private final int OVER_TIME = 6000;
    /** 超时处理的KEY */
    private final int KEY_OVER_TIME_MSG = 101;

    /** 最后一次收到到请求回调的时间 */
    private long lastReceiveTime;

    private final Handler mHandle = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == KEY_OVER_TIME_MSG) {
                printLog("handleMessage() KEY_OVER_TIME_MSG handle");
                isGetVersion = false;
                onVersionReqEnd();
            }
        }
    };

    private FirmwareVersion() {
    }

    public static FirmwareVersion getInstance() {
        if (mInstance == null) {
            mLock.lock();
            try {
                if (mInstance == null) {
                    mInstance = new FirmwareVersion();
                }
            } finally {
                mLock.unlock();
            }
        }
        return mInstance;
    }

    @Override
    public void setOnFirmwareGetListener(OnFirmwareVersionGetListener onFirmwareGetListener) {
        printLog("setOnFirmwareGetListener()");
        if (mOnFirmwareVersionGetListenerList == null) {
            mOnFirmwareVersionGetListenerList = new ArrayList<>();
        }
        CollectionUtils.listAddAvoidNull(mOnFirmwareVersionGetListenerList, onFirmwareGetListener);
    }

    @Override
    public void unRegisterListener(OnFirmwareVersionGetListener onFirmwareGetListener) {
        printLog("unRegisterListener()");
        Optional.ofNullable(mOnFirmwareVersionGetListenerList).ifPresent(list -> list.remove(onFirmwareGetListener));
    }

    @Override
    public void getFlyFirmwareVersion() {
        printLog("getFlyFirmwareVersion() isGetVersion = " + isGetVersion);
        long curTime = System.currentTimeMillis();
        if (isGetVersion) {
            printLog("getFlyFirmwareVersion() lastReceiveTime = " + lastReceiveTime + "; curTime = " + curTime);
            // 添加一种超时处理，避免有某个请求偶尔没有回调导致后继请求一直无法继续
            if (lastReceiveTime != 0 && curTime - lastReceiveTime > OVER_TIME) {
                isGetVersion = false;
                continueGetFWVersion();
            } else {
                onFirmwareStatusCallback();
            }
            return;
        }
        continueGetFWVersion();
    }

    private void continueGetFWVersion() {
        if (!CollectionUtils.isEmptyList(GlobalVariable.sTypeVersionList)) {
            GlobalVariable.sTypeVersionList.clear();
        } else {
            if (GlobalVariable.sTypeVersionList == null) {
                GlobalVariable.sTypeVersionList = new ArrayList<>();
            }
        }
        isGetVersion = true;
        mCounter.set(0);
        lastReceiveTime = 0;
        getPlanType();
        // 获取系统的版本
        mDisposable = Completable.concatArray(
                        getFlightVersion(),
                        getOTAVersion(),
                        getRTKVersion(),
                        getACVersion(),
                        getSetTimeVersion(),
                        getUpgradeCompVersion(),
                        getImageTransmissionVersion(),
                        getPicTransmissionComponentsVersion(),
                        getFCCoprocessorVersion(),
                        getTaskManagerVersion(),
                        getFileTransmissionVersion(),
                        getVisionVersion(),
                        // 获取电池的版本
                        getBatteryVersion(),
                        getRCAVersion(),
                        getGimbalVersion(),
                        getImageTransmissionRelayVersion(),
                        getMCUVersion(),
                        get5GVersion(),
                        getObstacleVersion(),
                        getBatteryVendorInfo(),
                        getNoFlyZoneFwVersion(),
                        getGimbalVisionVersion(),
                        getRemoteIDVersion(),
                        getAllMountGimbalInfo(),
                        getNewAdapterRingVersion(),
                        getSystemApplicationVersion(),
                        getOnboardITSystemVersion(),
                        getCompRelayITSystemVersion(),
                        getGNSSVersions(),
                        getElectronicVersion(1),
                        getElectronicVersion(2),
                        getElectronicVersion(3),
                        getElectronicVersion(4),
                        getFCAppVersion(),
                        getFCBootVersion(),
                        getRTKVersionNew(),
                        getAIBoxVersion(),
                        getAIBoxSN(),
                        getLockBatteryMCUVersion(),
                        getRTKType())
                .subscribe(() -> {});
    }

    private void onFirmwareStatusCallback() {
        Optional.ofNullable(mOnFirmwareVersionGetListenerList).ifPresent(list -> {
            for (OnFirmwareVersionGetListener mListener : list) {
                mListener.onFirmwareStatusCallback(1);
            }
        });
    }

    /**
     * 获取上一次飞机的连接类型
     */
    private void getPlanType() {
        printLog("getPlanType()");
        int planType = SPUtils.getInt(GduAppEnv.application, SPUtils.USER_LAST_PLANTYPE);
        mCurrentFlyType = PlanType.get(planType);
        printLog("getPlanType() mCurrentFlyType = " + mCurrentFlyType);
    }

    /**
     * 获取飞机大版本
     */
    private Completable getFlightVersion() {
        printLog("getFlightVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            GduSocketManager.getInstance().getGduCommunication().getFlightVersion((code, bean) -> {
                printLog("getFlightVersion callBack() code = " + code);
                isRequestFinish();
                parseFlightVersion(bean);
            });
        }).subscribeOn(Schedulers.io());
    }


    /**
     * 解析出系统大版本号
     *
     * @param bean 待解析对象
     */
    private void parseFlightVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 4) {
            GlobalVariable.IS_CAN_UPGRADE = GlobalVariable.IS_CAN_UPGRADE == 2 ? 2 : 1;
            cacheFlyVersion(FirmwareType.FLIGHT_VERSION.getEnValue(), "");
            return;
        }
        printLog("parseFlightVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String versionStr = ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[1])
                        + "." + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[2])
                        + "." + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[3]);
        GlobalVariable.IS_CAN_UPGRADE = GlobalVariable.IS_CAN_UPGRADE == 2 ? 2 : bean.frameContent[0];
        addNewFirmwareTypeAndVersion(FirmwareType.FLIGHT_VERSION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.FLIGHT_VERSION.getEnValue(), versionStr);
    }

    private void addNewFirmwareTypeAndVersion(String firmwareType, String versionStr) {
        MyLogUtils.i("addNewFirmwareTypeAndVersion() firmwareType = " + firmwareType + "; versionStr = " + versionStr);
        FirmwareTypeAndVersionBean versionBean = new FirmwareTypeAndVersionBean();
//        String typeStr = CommonUtils.getFirmwareNameByFirmwareType(GduAppEnv.application, firmwareType);
//        versionBean.setType(typeStr);
        versionBean.setVersion(versionStr);
        CollectionUtils.listAddAvoidNull(GlobalVariable.sTypeVersionList, versionBean);
    }

    /**
     * 获取系统DB和升级包版本号（3403系统）
     */
    private Completable getOTAVersion() {
        printLog("getOTAVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            GduSocketManager.getInstance().getGduCommunication().getOTAVersions((code, bean) -> {
                printLog("getOTAVersion callBack() code = " + code);
                isRequestFinish();
                parseOTAVersion(code, bean);
            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取RTK版本号
     */
    private Completable getRTKVersion() {
        printLog("getRTKVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            GduSocketManager.getInstance().getGduCommunication().getRTKVersion((code, bean) -> {
                printLog("getRTKVersion callBack() code = " + code);
                isRequestFinish();
                parseRTKVersion(bean);
            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取综控版本
     */
    private Completable getACVersion() {
        printLog("getACVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getACVersion((code, bean) -> {
//                printLog("getACVersion callBack() code = " + code);
//                isRequestFinish();
//                parseACVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取授时组件版本
     */
    private Completable getSetTimeVersion() {
        printLog("getSetTimeVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getSetTimeVersion((code, bean) -> {
//                printLog("getSetTimeVersion callBack() code = " + code);
//                isRequestFinish();
//                parseSetTimeVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取升级组件版本
     */
    private Completable getUpgradeCompVersion() {
        printLog("getUpgradeCompVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getUpgradeCompVersion((code, bean) -> {
//                printLog("getUpgradeCompVersion callBack() code = " + code);
//                isRequestFinish();
//                parseUpgradeCompVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取图传升级包版本号（9201应用程序）
     */
    private Completable getImageTransmissionVersion() {
        printLog("getImageTransmissionVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getPicTransmissionApplicationVersion((code, bean) -> {
//                printLog("getPicTransmissionApplicationVersion callBack() code = " + code);
//                isRequestFinish();
//                parseImageTransmissionApplicationVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析图传应用程序版本号
     *
     * @param bean 待解析对象
     */
    private void parseImageTransmissionApplicationVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 9) {
            cacheFlyVersion(FirmwareType.IMAGE_TRANSMISSION_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseImageTransmissionApplicationVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String itVersion = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2) + "."
                + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 4)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 6)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 8);
        printLog("parseImageTransmissionApplicationVersion() itVersion = " + itVersion);
        addNewFirmwareTypeAndVersion(FirmwareType.IMAGE_TRANSMISSION_FIRMWARE.getEnValue(), itVersion);
        cacheFlyVersion(FirmwareType.IMAGE_TRANSMISSION_FIRMWARE.getEnValue(), itVersion);
    }

    /**
     * 获取图传组件版本号
     */
    private Completable getPicTransmissionComponentsVersion() {
        printLog("getPicTransmissionComponentsVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getPicTransmissionComponentsVersion((code, bean) -> {
//                printLog("getPicTransmissionComponentsVersion callBack() code = " + code);
//                isRequestFinish();
//                parsePicTransmissionComponentVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析图传组件版本
     *
     * @param bean 待解析对象
     */
    private void parsePicTransmissionComponentVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.PIC_TRANSMISSION_COMPONENTS.getEnValue(), "");
            return;
        }
        printLog("parsePicTransmissionComponentVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String itVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.PIC_TRANSMISSION_COMPONENTS.getEnValue(), itVersion);
        cacheFlyVersion(FirmwareType.PIC_TRANSMISSION_COMPONENTS.getEnValue(), itVersion);
    }

    /**
     * 飞控协处理器组件版本号
     */
    private Completable getFCCoprocessorVersion() {
        printLog("getFCCoprocessorVersion() planType = " + GlobalVariable.planType);
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            if (DroneUtil.isSmallFlight()) {
                cacheFlyVersion(FirmwareType.FC_COPROCESSOR_FIRMWARE.getEnValue(), "");
                isRequestFinish();
                return;
            }
//            GduApplication.getSingleApp().gduCommunication.getFCCoprocessorVersion((code, bean) -> {
//                printLog("getFCCoprocessorVersion callBack() code = " + code);
//                isRequestFinish();
//                parseFCCoprocessorVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 文件传输组件版本号
     */
    private Completable getFileTransmissionVersion() {
        printLog("getFileTransmissionVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getFileTransmissionVersion((code, bean) -> {
//                printLog("getFileTransmissionVersion callBack() code = " + code);
//                isRequestFinish();
//                parseFileTransmissionVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取图传中继版本
     */
    private Completable getImageTransmissionRelayVersion() {
        printLog("getImageTransmissionRelayVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getImageTransmissionRelayVersion((code, bean) -> {
//                printLog("getImageTransmissionRelayVersion callBack() code = " + code);
//                isRequestFinish();
//                parseImageTransmissionRelayVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取任务组件版本
     */
    private Completable getTaskManagerVersion() {
        printLog("getTaskManagerVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getTaskManagerVersion((code, bean) -> {
//                printLog("getTaskManagerVersion callBack() code = " + code);
//                isRequestFinish();
//                parseTaskCompVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取遥控器版本
     */
    private Completable getRCAVersion() {
        printLog("getRCAVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getRCAVersion((code, bean) -> {
//                printLog("getRCAVersion callBack() code = " + code);
//                isRequestFinish();
//                parseRCAVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取视觉版本号
     */
    private Completable getVisionVersion() {
        printLog("getVisionVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getVisionVersion((code, bean) -> {
//                printLog("getVisionVersion callBack() code = " + code);
//                isRequestFinish();
//                parseVisionVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取电池版本号
     */
    private Completable getBatteryVersion() {
        printLog("getBatteryVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getBatterInfo((code, bean) -> {
//                printLog("getBatteryVersion callBack() code = " + code);
//                isRequestFinish();
//                parseBatteryVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取云台版本和SN(主要是获取SN，旧云台则获取版本号)
     * 其他云台通过0x0027 获取版本号
     */
    private Completable getGimbalVersion() {
        printLog("getGimbalVersion() sCameraIsStart = " + GlobalVariable.sCameraIsStart);
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            if (!GlobalVariable.sCameraIsStart) {
                isRequestFinish();
                return;
            }
//            GduApplication.getSingleApp().gduCommunication.getGimbalSNAndVersion((code, bean) -> {
//                printLog("getGimbalVersion callBack() code = " + code);
//                isRequestFinish();
//                parseGimbalVersion(code, bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取MCU版本号
     */
    private Completable getMCUVersion() {
        printLog("getMCUVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getMCUVersion((code, bean) -> {
//                printLog("getMCUVersion callBack() code = " + code);
//                isRequestFinish();
//                parseMCUVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取5G图传程序版本
     */
    private Completable get5GVersion() {
        printLog("get5GVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.get5GFirmwareVersion((code, bean) -> {
//                printLog("get5GVersion callBack() code = " + code);
//                isRequestFinish();
//                parse5GVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    private Completable getObstacleVersion() {
        printLog("getObstacleVersion() gimbalType = " + GlobalVariable.gimbalType);
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            if (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                    || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL_S220_IR640) {
                getObstacleFwVersionNew();
            } else {
                getObstacleFwVersion();
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取视觉避障固件版本
     */
    private void getObstacleFwVersion() {
        printLog("getObstacleFwVersion()");
//        GduApplication.getSingleApp().gduCommunication.getObstacleFwVersion((code, bean) -> {
//            printLog("getObstacleFwVersion callBack() code = " + code);
//            isRequestFinish();
//            parseObstacleVersion(bean);
//        });
    }

    /**
     * 获取视觉避障固件版本(新)
     */
    private void getObstacleFwVersionNew() {
        printLog("getObstacleFwVersionNew()");
//        GduApplication.getSingleApp().gduCommunication.getObstacleFwVersionNew((code, bean) -> {
//            printLog("getObstacleFwVersionNew callBack() code = " + code);
//            isRequestFinish();
//            parseObstacleVersionNew(bean);
//        });
    }

    /**
     * 获取电池厂商信息
     */
    private Completable getBatteryVendorInfo() {
        printLog("getBatteryVendorInfo()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getBatterFactoryInfo((code, bean) -> {
//                printLog("getBatteryVendorInfo callBack() code = " + code);
//                isRequestFinish();
//                parserBatteryVendorInfo(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    private void parserBatteryVendorInfo(GduFrame3 bean) {
        printLog("parserBatteryVendorInfo()");
        if (bean != null && bean.frameContent != null && bean.frameContent.length >= 20) {
            final String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
            printLog("parserBatteryVendorInfo() hexStr = " + hexStr);
            // 制造商代码
            final byte vendorCode = bean.frameContent[0];
            String vendorStr;
            if (vendorCode < 10) {
                vendorStr = "Battery_0" + vendorCode;
            } else {
                vendorStr = "Battery_" + vendorCode;
            }
            GlobalVariable.CUR_BATTERY_VENDOR_CODE = vendorStr;
            printLog("parserBatteryVendorInfo() vendorStr = " + vendorStr);
        } else {
            GlobalVariable.CUR_BATTERY_VENDOR_CODE = "";
        }
    }

    /**
     * 解析避障固件版本相关信息版本(无Code返回解析方式)
     *
     * @param bean 待解析对象
     */
    private void parseObstacleVersion(GduFrame3 bean) {
        printLog("parseObstacleVersion()");
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheObstacleInfo(ObstacleType.PRE_VISUAL, "");
            cacheObstacleInfo(ObstacleType.BACK_VISUAL, "");
            cacheObstacleInfo(ObstacleType.BOT_VISUAL, "");
            cacheObstacleInfo(ObstacleType.PRE_RADAR, "");
            cacheObstacleInfo(ObstacleType.LEFT_RADAR, "");
            cacheObstacleInfo(ObstacleType.RIGHT_RADAR, "");
            return;
        }
        final String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
        printLog("parseObstacleVersion() hexStr = " + hexStr);
        final String preVisualVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
        final int preVisualNum = Integer.parseInt(preVisualVersion.replace(".", ""));
        if (preVisualNum > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.PRE_VISUAL.getValue(), preVisualVersion);
            cacheObstacleInfo(ObstacleType.PRE_VISUAL, preVisualVersion);
        }
        if (bean.frameContent.length < 6) {
            return;
        }
        final String backVisualVersion = bean.frameContent[3] + "." + bean.frameContent[4] + "." + bean.frameContent[5];
        final int backVisualNum = Integer.parseInt(backVisualVersion.replace(".", ""));
        if (backVisualNum > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.BACK_VISUAL.getValue(), backVisualVersion);
            cacheObstacleInfo(ObstacleType.BACK_VISUAL, backVisualVersion);
        }
        final long botVisualVersion = ByteUtilsLowBefore.byte2UnsignedInt(bean.frameContent, 6);
        if (botVisualVersion > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.BOT_VISUAL.getValue(), String.valueOf(botVisualVersion));
            cacheObstacleInfo(ObstacleType.BOT_VISUAL, String.valueOf(botVisualVersion));
        }
        final long preRadarVersion = ByteUtilsLowBefore.byte2UnsignedInt(bean.frameContent, 10);
        if (preRadarVersion > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.PRE_RADAR.getValue(), String.valueOf(preRadarVersion));
            cacheObstacleInfo(ObstacleType.PRE_RADAR, String.valueOf(preRadarVersion));
        }
        final long leftRadarVersion = ByteUtilsLowBefore.byte2UnsignedInt(bean.frameContent, 14);
        if (leftRadarVersion > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.LEFT_RADAR.getValue(), String.valueOf(leftRadarVersion));
            cacheObstacleInfo(ObstacleType.LEFT_RADAR, String.valueOf(leftRadarVersion));
        }
        final long rightRadarVersion = ByteUtilsLowBefore.byte2UnsignedInt(bean.frameContent, 18);
        if (rightRadarVersion > 0) {
            addNewFirmwareTypeAndVersion(ObstacleType.RIGHT_RADAR.getValue(), String.valueOf(rightRadarVersion));
            cacheObstacleInfo(ObstacleType.RIGHT_RADAR, String.valueOf(rightRadarVersion));
        }
    }

    /**
     * 解析新避障固件版本相关信息版本(无Code返回解析方式)
     *
     * @param bean 待解析对象
     */
    private void parseObstacleVersionNew(GduFrame3 bean) {
        printLog("parseObstacleVersionNew()");
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 18) {
            cacheObstacleInfo(ObstacleType.PRE_VISUAL, "");
            cacheObstacleInfo(ObstacleType.BACK_VISUAL, "");
            cacheObstacleInfo(ObstacleType.LEFT_VISUAL, "");
            cacheObstacleInfo(ObstacleType.RIGHT_VISUAL, "");
            cacheObstacleInfo(ObstacleType.TOP_VISUAL, "");
            cacheObstacleInfo(ObstacleType.BOT_VISUAL, "");
            return;
        }
        final String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
        printLog("parseObstacleVersionNew() hexStr = " + hexStr);
        // 前视双目
        visionVersionGet(ObstacleType.PRE_VISUAL, bean, 0);
        // 后视双目
        visionVersionGet(ObstacleType.BACK_VISUAL, bean, 3);
        // 左视双目
        visionVersionGet(ObstacleType.LEFT_VISUAL, bean, 6);
        // 右视双目
        visionVersionGet(ObstacleType.RIGHT_VISUAL, bean, 9);
        // 上视双目
        visionVersionGet(ObstacleType.TOP_VISUAL, bean, 12);
        // 下视双目
        visionVersionGet(ObstacleType.BOT_VISUAL, bean, 15);
    }

    private void visionVersionGet(ObstacleType obstacleType, GduFrame3 bean, int startIndex) {
        MyLogUtils.i("visionVersionGet() obstacleType = " + obstacleType + "; startIndex = " + startIndex);
        final String versionStr = bean.frameContent[startIndex++] + "."
                + bean.frameContent[startIndex++] + "." + bean.frameContent[startIndex];
        MyLogUtils.i("visionVersionGet() versionStr = " + versionStr);
        final int visionNum = Integer.parseInt(versionStr.replace(".", ""));
        if (visionNum > 0) {
            addNewFirmwareTypeAndVersion(obstacleType.getValue(), versionStr);
            cacheObstacleInfo(obstacleType, versionStr);
        }
    }

    /**
     * 获取禁飞区版本号
     */
    private Completable getNoFlyZoneFwVersion() {
        printLog("getNoFlyZoneFwVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getNoFlyZoneVersion(((code, bean) -> {
//                printLog("getNoFlyZoneFwVersion callBack() code = " + code);
//                isRequestFinish();
//                parserNoFlyZoneVersion(code, bean);
//            }));
        }).subscribeOn(Schedulers.io());
    }

    private void parserNoFlyZoneVersion(int code, GduFrame3 bean) {
        printLog("parserNoFlyZoneVersion()");
        if (code != GduConfig.OK || bean == null || bean.frameContent == null
                || bean.frameContent.length < 5) {
            GlobalVariable.flightNoFlyZoneVersion = "";
            return;
        }
        final String hexStr = DataUtil.bytes2HexAddSplit(bean.frameContent);
        printLog("parserNoFlyZoneVersion() hexStr = " + hexStr);
        if (bean.frameContent.length < 5) {
            GlobalVariable.flightNoFlyZoneVersion = "";
            return;
        }
        String noFlyZoneVersionStr;
        if (bean.frameContent[4] < 10) {
            noFlyZoneVersionStr = bean.frameContent[2] + "." + bean.frameContent[3]
                    + ".0" + bean.frameContent[4];
        } else {
            noFlyZoneVersionStr = bean.frameContent[2] + "." + bean.frameContent[3]
                    + "." + bean.frameContent[4];
        }
        GlobalVariable.flightNoFlyZoneVersion = noFlyZoneVersionStr;
        addNewFirmwareTypeAndVersion(FirmwareType.NO_FLY_ZONE_VERSION.getEnValue(), noFlyZoneVersionStr);
        printLog("parserNoFlyZoneVersion() noFlyZoneVersionStr = " + noFlyZoneVersionStr);
        if (bean.frameContent.length < 7) {
            GlobalVariable.flightNoFlyZoneSavePath = "";
            return;
        }
        int filePathLength = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 5);
        byte[] pathArr = new byte[filePathLength];
        System.arraycopy(bean.frameContent, 7, pathArr, 0, filePathLength);
        printLog("parserNoFlyZoneVersion() filePathLength = " + filePathLength
                + "; pathArrHexStr = " + DataUtil.bytes2HexAddPlaceHolder(pathArr));
        GlobalVariable.flightNoFlyZoneSavePath = new String(pathArr);
        printLog("parserNoFlyZoneVersion() pathStr = " + GlobalVariable.flightNoFlyZoneSavePath);
    }

    /**
     * 获取云台视觉系统版本
     */
    private Completable getGimbalVisionVersion() {
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            boolean isUnSupportGimbal = GimbalUtil.isUnImagingGimbal();
            printLog("getGimbalVisionVersion() sCameraIsStart = " + GlobalVariable.sCameraIsStart
                    + "; isUnSupportGimbal = " + isUnSupportGimbal);
            if (!GlobalVariable.sCameraIsStart || isUnSupportGimbal) {
                isRequestFinish();
                return;
            }
//            GduApplication.getSingleApp().gduCommunication.getGimbalVisionVersion(((code, bean) -> {
//                printLog("getGimbalVisionVersion callBack() code = " + code);
//                isRequestFinish();
//                parserGimbalVisionVersion(bean);
//            }));
        }).subscribeOn(Schedulers.io());
    }

    private void parserGimbalVisionVersion(GduFrame3 bean) {
        printLog("parserGimbalVisionVersion()");
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            GlobalVariable.gimbalVisionVersion = "";
            printLog("parserGimbalVisionVersion() gimbalVisionVersion = " + GlobalVariable.gimbalVisionVersion);
            return;
        }
        final String hexStr = DataUtil.bytes2HexAddSplit(bean.frameContent);
        printLog("parserGimbalVisionVersion() hexStr = " + hexStr);
        final String gimbalVisionVersion = "V" + bean.frameContent[0] + "." + bean.frameContent[1]
                + "." + bean.frameContent[2];
        GlobalVariable.gimbalVisionVersion = gimbalVisionVersion;
        addNewFirmwareTypeAndVersion(FirmwareType.GIMBAL_VISION_VERSION.getEnValue(), gimbalVisionVersion);
        printLog("parserGimbalVisionVersion() gimbalVisionVersion = " + gimbalVisionVersion);
    }

    /**
     * 获取remote id广播组件版本
     */
    private Completable getRemoteIDVersion() {
        printLog("getRemoteIDVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getRemoteIDVersion((code, bean) -> {
//                printLog("getRemoteIDVersion callBack() code = " + code);
//                isRequestFinish();
//                parseRemoteIDVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析remote id广播组件版本号
     *
     * @param bean 待解析对象
     */
    private void parseRemoteIDVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            GlobalVariable.sRemoteIDVersion = "";
            return;
        }
        printLog("parseRemoteIDVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.sRemoteIDVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.REMOTE_ID_VERSION.getEnValue(), GlobalVariable.sRemoteIDVersion);
    }

    private Completable getAllMountGimbalInfo() {
        printLog("getAllMountGimbalInfo()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GetMountVersionTool.getInstance().getMountVersions(data -> {
//                printLog("getAllMountGimbalInfo onVersionCallback()");
//                if (!CommonUtils.isEmptyList(data)) {
//                    printLog("getAllMountGimbalInfo onVersionCallback() dataSize = " + data.size());
//                }
//                isRequestFinish();
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取新转接环版本
     */
    private Completable getNewAdapterRingVersion() {
        printLog("getNewAdapterRingVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getNewAdapterRingVersion((code, bean) -> {
//                printLog("getNewAdapterRingVersion callBack() code = " + code);
//                isRequestFinish();
//                parseNewAdapterRingVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析新转接环版本信息
     *
     * @param bean 待解析对象
     */
    private void parseNewAdapterRingVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            cacheFlyVersion(FirmwareType.NEW_ADAPTER_RING_VERSION.getEnValue(), "");
            return;
        }
        printLog("parseNewAdapterRingVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = bean.frameContent[2]  + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.NEW_ADAPTER_RING_VERSION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.NEW_ADAPTER_RING_VERSION.getEnValue(), versionStr);
    }

    /**
     * 获取(3402)系统应用程序版本
     */
    private Completable getSystemApplicationVersion() {
        printLog("getSystemApplicationVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getSystemApplicationVersion((code, bean) -> {
//                printLog("getSystemApplicationVersion callBack() code = " + code);
//                isRequestFinish();
//                parseSystemApplicationVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析(3402)系统应用程序版本信息
     *
     * @param bean 待解析对象
     */
    private void parseSystemApplicationVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 9) {
            cacheFlyVersion(FirmwareType.A2_SYSTEM_APPLICATION.getEnValue(), "");
            cacheFlyVersion(FirmwareType.S2_SYSTEM_APPLICATION.getEnValue(), "");
            return;
        }
        printLog("parseSystemApplicationVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String dbVerStr = bean.frameContent[2]  + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.S2_SYSTEM_APPLICATION.getEnValue(), dbVerStr);
        cacheFlyVersion(FirmwareType.S2_SYSTEM_APPLICATION.getEnValue(), dbVerStr);

        String versionStr = bean.frameContent[5]  + "." + bean.frameContent[6] + "." + bean.frameContent[7]
                + "." + ByteUtilsLowBefore.byte2short(bean.frameContent, 8);
        addNewFirmwareTypeAndVersion(FirmwareType.A2_SYSTEM_APPLICATION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.A2_SYSTEM_APPLICATION.getEnValue(), versionStr);
    }

    /**
     * 获取飞机图传(9201)系统程序版本
     */
    private Completable getOnboardITSystemVersion() {
        printLog("getOnboardITSystemVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getOnboardITSystemVersion((code, bean) -> {
//                printLog("getOnboardITSystemVersion callBack() code = " + code);
//                isRequestFinish();
//                parseOnboardITSystemVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析飞机图传(9201)系统程序版本信息
     *
     * @param bean 待解析对象
     */
    private void parseOnboardITSystemVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 9) {
            cacheFlyVersion(FirmwareType.S3_SYSTEM_APPLICATION.getEnValue(), "");
            return;
        }
        printLog("parseOnboardITSystemVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 4)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 6);
        addNewFirmwareTypeAndVersion(FirmwareType.S3_SYSTEM_APPLICATION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.S3_SYSTEM_APPLICATION.getEnValue(), versionStr);
    }

    /**
     * 获取遥控器图传(9201)系统程序版本
     */
    private Completable getCompRelayITSystemVersion() {
        printLog("getCompRelayITSystemVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getCompRelayITSystemVersion((code, bean) -> {
//                printLog("getCompRelayITSystemVersion callBack() code = " + code);
//                isRequestFinish();
//                parseCompRelayITSystemVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析遥控器图传(9201)系统程序版本信息
     *
     * @param bean 待解析对象
     */
    private void parseCompRelayITSystemVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 9) {
            cacheFlyVersion(FirmwareType.RC_S2_SYSTEM_APPLICATION.getEnValue(), "");
            return;
        }
        printLog("parseCompRelayITSystemVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 4)
                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 6);
        addNewFirmwareTypeAndVersion(FirmwareType.RC_S2_SYSTEM_APPLICATION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.RC_S2_SYSTEM_APPLICATION.getEnValue(), versionStr);
    }

    /**
     * 获取单点GPS(GNSS)版本号
     */
    private Completable getGNSSVersions() {
        printLog("getGNSSVersions()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getGNSSVersions((code, bean) -> {
//                printLog("getGNSSVersions callBack() code = " + code);
//                isRequestFinish();
//                parseGNSSVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析单点GPS(GNSS)信息
     *
     * @param bean 待解析对象
     */
    private void parseGNSSVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            cacheFlyVersion(FirmwareType.FC_GPS_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseGNSSVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.FC_GPS_FIRMWARE.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.FC_GPS_FIRMWARE.getEnValue(), versionStr);
    }

    /**
     * 获取电调电机固件版本
     * @param num 电机编号
     * 电机1 (左前)
     * 电机2 (右前)
     * 电机3 (左后)
     * 电机4 (右后)
     */
    private Completable getElectronicVersion(int num) {
        printLog("getElectronicVersion() num = " + num);
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            if (!DroneUtil.isSmallFlight()) {
                isRequestFinish();
                return;
            }
//            GduApplication.getSingleApp().gduCommunication.getElectronicVersion((byte) num, (code, bean) -> {
//                printLog("getElectronicVersion callBack() num = " + num + "; code = " + code);
//                isRequestFinish();
//                parseElectronicVersion(num, bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析电调电机版本信息
     *
     * @param bean 待解析对象
     */
    private void parseElectronicVersion(int num, GduFrame3 bean) {
        printLog("parseElectronicVersion() num = " + num);
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 6) {
            switch (num) {
                case 1:
                    cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_1.getEnValue(), "");
                    break;

                case 2:
                    cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_2.getEnValue(), "");
                    break;

                case 3:
                    cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_3.getEnValue(), "");
                    break;

                case 4:
                    cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_4.getEnValue(), "");
                    break;

                default:
                    break;
            }
            return;
        }
        printLog("parseElectronicVersion() num = " + num
                + "; hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr;
        switch (num) {
            case 1:
                versionStr = bean.frameContent[3] + "." + bean.frameContent[4] + "." + bean.frameContent[5];
                addNewFirmwareTypeAndVersion(FirmwareType.ELECTRONIC_VERSION_1.getEnValue(), versionStr);
                cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_1.getEnValue(), versionStr);
                break;
            case 2:
                versionStr = bean.frameContent[3] + "." + bean.frameContent[4] + "." + bean.frameContent[5];
                addNewFirmwareTypeAndVersion(FirmwareType.ELECTRONIC_VERSION_2.getEnValue(), versionStr);
                cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_2.getEnValue(), versionStr);
                break;
            case 3:
                versionStr = bean.frameContent[3] + "." + bean.frameContent[4] + "." + bean.frameContent[5];
                addNewFirmwareTypeAndVersion(FirmwareType.ELECTRONIC_VERSION_3.getEnValue(), versionStr);
                cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_3.getEnValue(), versionStr);
                break;
            case 4:
                versionStr = bean.frameContent[3] + "." + bean.frameContent[4] + "." + bean.frameContent[5];
                addNewFirmwareTypeAndVersion(FirmwareType.ELECTRONIC_VERSION_4.getEnValue(), versionStr);
                cacheFlyVersion(FirmwareType.ELECTRONIC_VERSION_4.getEnValue(), versionStr);
                break;

            default:
                break;
        }
    }

    /**
     * 获取飞控App版本
     */
    private Completable getFCAppVersion() {
        printLog("getFCAppVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getFCAppVersion((code, bean) -> {
//                printLog("getFCAppVersion callBack() code = " + code);
//                isRequestFinish();
//                parseFCAppVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析飞控App版本
     *
     * @param bean 待解析对象
     */
    private void parseFCAppVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            cacheFlyVersion(FirmwareType.FC_APP_VERSION.getEnValue(), "");
            return;
        }
        printLog("parseFCAppVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.FC_APP_VERSION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.FC_APP_VERSION.getEnValue(), versionStr);
    }

    /**
     * 获取飞控Boot版本
     */
    private Completable getFCBootVersion() {
        printLog("getFCBootVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getFCBootVersion((code, bean) -> {
//                printLog("getFCBootVersion callBack() code = " + code);
//                isRequestFinish();
//                parseFCBootVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析飞控Boot版本
     *
     * @param bean 待解析对象
     */
    private void parseFCBootVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            cacheFlyVersion(FirmwareType.FC_BOOT_VERSION.getEnValue(), "");
            return;
        }
        printLog("parseFCBootVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String versionStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.FC_BOOT_VERSION.getEnValue(), versionStr);
        cacheFlyVersion(FirmwareType.FC_BOOT_VERSION.getEnValue(), versionStr);
    }

    /**
     * 缓存相机版本信息
     */
    private void cacheObstacleInfo(ObstacleType type, String version) {
        cacheFlyVersion(FirmwareType.VISION_FIRMWARE.getValue() + "_" + type.getKey(), version);
    }

    /**
     * 清空飞机端历史版本
     */
    @Override
    public void initFlyVersionCache() {
        for (FirmwareType firmwareType : FirmwareType.values()) {
            clearFlyVersion(firmwareType.getEnValue());
        }
        clearFlyVersion(GduConfig.BATTERY_CODE);
    }

    /**
     * 根据固件类型清除历史版本
     *
     * @param firmwareType 固件类型
     */
    @Override
    public void initFlyVersionCacheByType(FirmwareType firmwareType) {
        clearFlyVersion(firmwareType.getEnValue());
    }

    /**
     * 存储云台版本信息
     */
    private void cacheGimbalVersionOld(GimbalType gimbalType) {
        if (GlobalVariable.gimbelVersion == 0) {
            cacheFlyVersion(gimbalType.getValue(), "");
            return;
        }
        cacheFlyVersion(gimbalType.getValue(), String.valueOf(GlobalVariable.gimbelVersion));
    }

    /**
     * 解析5G组件版本号
     *
     * @param bean 待解析对象
     */
    private void parse5GVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            GlobalVariable.s5GVersion = "";
            cacheFlyVersion(FirmwareType.FIFTH_GENERATION_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parse5GVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.s5GVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.FIFTH_GENERATION_FIRMWARE.getEnValue(), GlobalVariable.s5GVersion);
        cacheFlyVersion(FirmwareType.FIFTH_GENERATION_FIRMWARE.getEnValue(), GlobalVariable.s5GVersion);
    }

    /**
     * 解析图传中继版本
     */
    private void parseImageTransmissionRelayVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.AP12_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseImageTransmissionRelayVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String imageTransmissionRelay = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.AP12_FIRMWARE.getEnValue(), imageTransmissionRelay);
        cacheFlyVersion(FirmwareType.AP12_FIRMWARE.getEnValue(), imageTransmissionRelay);
    }

    /**
     * 解析文件传输组件
     *
     * @param bean 待解析对象
     */
    private void parseFileTransmissionVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.FILE_TRANSMISSION_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseFileTransmissionVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        String fileTransmission = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.FILE_TRANSMISSION_FIRMWARE.getEnValue(), fileTransmission);
        cacheFlyVersion(FirmwareType.FILE_TRANSMISSION_FIRMWARE.getEnValue(), fileTransmission);
    }

    /**
     * 解析控协处理器组件
     *
     * @param bean 待解析对象
     */
    private void parseFCCoprocessorVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.FC_COPROCESSOR_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseFCCoprocessorVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String coprocessorVersion =
                bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.FC_COPROCESSOR_FIRMWARE.getEnValue(), coprocessorVersion);
        cacheFlyVersion(FirmwareType.FC_COPROCESSOR_FIRMWARE.getEnValue(), coprocessorVersion);
    }

    /**
     * 解析航迹管理组件版本
     *
     * @param bean 待解析对象
     */
    private void parseTaskCompVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.TASK_MANAGER_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseTaskCompVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String taskVersion = ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[0]) + "."
                + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[1]) + "." + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[2]);
        addNewFirmwareTypeAndVersion(FirmwareType.TASK_MANAGER_FIRMWARE.getEnValue(), taskVersion);
        cacheFlyVersion(FirmwareType.TASK_MANAGER_FIRMWARE.getEnValue(), taskVersion);
    }

    /**
     * 解析授时组件版本
     *
     * @param bean 待解析对象
     */
    private void parseSetTimeVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.SET_TIME_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseSetTimeVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String setTimeVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.SET_TIME_FIRMWARE.getEnValue(), setTimeVersion);
        cacheFlyVersion(FirmwareType.SET_TIME_FIRMWARE.getEnValue(), setTimeVersion);
    }

    /**
     * 解析RTK版本
     *
     * @param bean 待解析对象
     */
    private void parseRTKVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            cacheFlyVersion(FirmwareType.RTCM_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseRTKVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String rtkVersion = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
        addNewFirmwareTypeAndVersion(FirmwareType.RTCM_FIRMWARE.getEnValue(), rtkVersion);
        cacheFlyVersion(FirmwareType.RTCM_FIRMWARE.getEnValue(), rtkVersion);
    }

    /**
     * 解析升级组件版本
     *
     * @param bean 待解析对象
     */
    private void parseUpgradeCompVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.UPGRADE_COMP_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseUpgradeCompVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String upgradeCompVersion = ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[0]) + "."
                + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[1])
                + "." + ByteUtilsLowBefore.byte2UnsignedByte(bean.frameContent[2]);
        addNewFirmwareTypeAndVersion(FirmwareType.UPGRADE_COMP_FIRMWARE.getEnValue(), upgradeCompVersion);
        cacheFlyVersion(FirmwareType.UPGRADE_COMP_FIRMWARE.getEnValue(), upgradeCompVersion);
    }

    /**
     * 解析综控版本
     *
     * @param bean 待解析对象
     */
    private void parseACVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            GlobalVariable.sACVersion = "";
            cacheFlyVersion(FirmwareType.AC_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseACVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.sACVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.AC_FIRMWARE.getEnValue(), GlobalVariable.sACVersion);
        cacheFlyVersion(FirmwareType.AC_FIRMWARE.getEnValue(), GlobalVariable.sACVersion);
    }

    /**
     * 解析视觉组件版本号
     *
     * @param bean 待解析对象
     */
    private void parseVisionVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            GlobalVariable.sVisionVersion = "";
            cacheFlyVersion(FirmwareType.VISION_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseVisionVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.sVisionVersion = bean.frameContent[0] + "."
                + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.VISION_FIRMWARE.getEnValue(), GlobalVariable.sVisionVersion);
        cacheFlyVersion(FirmwareType.VISION_FIRMWARE.getEnValue(), GlobalVariable.sVisionVersion);
    }

    /**
     * 解析MCU组件版本号
     *
     * @param bean 待解析对象
     */
    private void parseMCUVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 4) {
            GlobalVariable.sMCUVersion = "";
            cacheFlyVersion(FirmwareType.MCU_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseMCUVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.sMCUVersion = bean.frameContent[1] + "." + bean.frameContent[2] + "." + bean.frameContent[3];
        addNewFirmwareTypeAndVersion(FirmwareType.MCU_FIRMWARE.getEnValue(), GlobalVariable.sMCUVersion);
        cacheFlyVersion(FirmwareType.MCU_FIRMWARE.getEnValue(), GlobalVariable.sMCUVersion);
    }


    /**
     * 解析出电池厂家和电池版本
     *
     * @param bean 待解析对象
     */
    private void parseBatteryVersion(GduFrame3 bean) {
        printLog("parseBatteryVersion()");
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            printLog("parseBatteryVersion() null data");
            cacheFlyVersion(FirmwareType.BATTER_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseBatteryVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String batteryVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." + (bean.frameContent[2] & 0xff);
        addNewFirmwareTypeAndVersion(FirmwareType.BATTER_FIRMWARE.getEnValue(), batteryVersion);
        cacheFlyVersion(FirmwareType.BATTER_FIRMWARE.getEnValue(), batteryVersion);
    }

    /**
     * 解析RCA版本
     *
     * @param bean 待解析对象
     */
    private void parseRCAVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.SAGA_RCA_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseRCAVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String version = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.SAGA_RCA_FIRMWARE.getEnValue(), version);
        cacheFlyVersion(FirmwareType.SAGA_RCA_FIRMWARE.getEnValue(), version);
    }

    /**
     * 解析云台信息
     *
     * @param code 状态码
     * @param bean 待解析对象
     */
    private void parseGimbalVersion(int code, GduFrame3 bean) {
        if (code != GduConfig.OK || bean == null || bean.frameContent == null || bean.frameContent.length < 5) {
            GlobalVariable.gimbelVersion = 0;
            GlobalVariable.sGimbalSN = "";
            cacheGimbalVersionOld(GlobalVariable.gimbalType);
            return;
        }
        String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
        printLog("parseGimbalVersion() hexStr = " + hexStr);
        boolean isNewGimbal = GimbalUtil.isNewCmdGetVersionGimbal();
        printLog("parseGimbalVersion() isNewGimbal = " + isNewGimbal);
        if (!isNewGimbal) {
            GlobalVariable.gimbelVersion = bean.frameContent[2] / 100.0;
            addNewFirmwareTypeAndVersion(GlobalVariable.gimbalType.getValue(), String.valueOf(GlobalVariable.gimbelVersion));
            cacheGimbalVersionOld(GlobalVariable.gimbalType);
        }
        if (!GimbalUtil.isUseGetSNNewCmdGimbal()) {
//            GlobalVariable.sGimbalSN = CommonUtils.getGimbalSN(GlobalVariable.gimbalType, bean.frameContent[4],
//                    ByteUtilsLowBefore.byte2short(bean.frameContent, 5));
        }
    }

    /**
     * 解析出综控控版本和系统版本
     *
     * @param code 状态码
     * @param bean 待解析对象
     */
    private void parseOTAVersion(int code, GduFrame3 bean) {
        if (code != GduConfig.OK || bean == null || bean.frameContent == null || bean.frameContent.length < 9) {
            cacheFlyVersion(FirmwareType.OTA_FIRMWARE.getEnValue(), "");
            cacheFlyVersion(FirmwareType.SVN_FIRMWARE.getEnValue(), "");
            cacheFlyVersion(FirmwareType.UPGRADE_PATCH_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseOTAVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        byte dbY = bean.frameContent[2];
        byte dbM = bean.frameContent[3];
        byte dbD = bean.frameContent[4];

        byte mainVersion = bean.frameContent[5];
        byte bigVersion = bean.frameContent[6];
        byte smallVersion = bean.frameContent[7];

        short svnVersion = ByteUtilsLowBefore.byte2short(bean.frameContent, 8);

        String otaVersion = dbY + "." + dbM + "." + dbD;
        cacheFlyVersion(FirmwareType.OTA_FIRMWARE.getEnValue(), otaVersion);
        String upgradeVersion = mainVersion + "." + bigVersion + "." + smallVersion + "." + svnVersion;
        printLog("parseOTAVersion() upgradeVersion = " + upgradeVersion);
        cacheFlyVersion(FirmwareType.UPGRADE_PATCH_FIRMWARE.getEnValue(), upgradeVersion);
        addNewFirmwareTypeAndVersion(FirmwareType.SVN_FIRMWARE.getEnValue(), String.valueOf(svnVersion));
        cacheFlyVersion(FirmwareType.SVN_FIRMWARE.getEnValue(), String.valueOf(svnVersion));
    }

    /**
     * 获取RTK版本号(新)
     */
    private Completable getRTKVersionNew() {
        printLog("getRTKVersionNew()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getRTKVersionNew((code, bean) -> {
//                printLog("getRTKVersionNew callBack() code = " + code);
//                isRequestFinish();
//                parseRTKVersionNew(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析RTK版本(新)
     *
     * @param bean 待解析对象
     */
    private void parseRTKVersionNew(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.RTK_980_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseRTK980Version() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String rtkVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." +
                ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2);
        addNewFirmwareTypeAndVersion(FirmwareType.RTK_980_FIRMWARE.getEnValue(), rtkVersion);
        cacheFlyVersion(FirmwareType.RTK_980_FIRMWARE.getEnValue(), rtkVersion);
    }

    /**
     * 获取AI盒子版本号
     */
    private Completable getAIBoxVersion() {
        printLog("getAIBoxVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getAIBoxVersion((code, bean) -> {
//                printLog("getAIBoxVersion callBack() code = " + code);
//                isRequestFinish();
//                parseAIBoxVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析AI盒子版本号
     *
     * @param bean 待解析对象
     */
    private void parseAIBoxVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.GIMBAL_AI_BOX_VERSION.getEnValue(), "");
            return;
        }
        printLog("parseAIBoxVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String rtkVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.GIMBAL_AI_BOX_VERSION.getEnValue(), rtkVersion);
        cacheFlyVersion(FirmwareType.GIMBAL_AI_BOX_VERSION.getEnValue(), rtkVersion);
    }

    /**
     * 获取AI盒子SN号
     */
    private Completable getAIBoxSN() {
        printLog("getAIBoxSN()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                MyLogUtils.e("获取AI盒子SN等待出错", e);
            }
//            GduApplication.getSingleApp().gduCommunication.getAiBoxSNAndType((code, bean) -> {
//                printLog("getAIBoxSN callBack() code = " + code);
//                isRequestFinish();
//                parseAIBoxSN(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 解析AI盒子SN号
     *
     * @param bean 待解析对象
     */
    private void parseAIBoxSN(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 20) {
            cacheFlyVersion(FirmwareType.GIMBAL_AI_BOX_SN.getEnValue(), "");
            return;
        }
        String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
        MyLogUtils.i("parseAIBoxSN() hexStr = " + hexStr);
        byte[] snBytes = new byte[17];
        System.arraycopy(bean.frameContent, 2, snBytes, 0, 17);
        String snStr = new String(snBytes);
        byte type = bean.frameContent[19];
        MyLogUtils.i("parseAIBoxSN() snStr = " + snStr + "; type = " + type);
        String endSnStr = "";
        if (type == 1) {
            endSnStr = "A-AI01-" + snStr;
        } else if(type == 2) {
            endSnStr = "A-AI02-" + snStr;
        }
        addNewFirmwareTypeAndVersion(FirmwareType.GIMBAL_AI_BOX_SN.getEnValue(), endSnStr);
        cacheFlyVersion(FirmwareType.GIMBAL_AI_BOX_SN.getEnValue(), endSnStr);
    }

    private Completable getLockBatteryMCUVersion() {
        printLog("getLockBatteryMCUVersion()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getLockBatteryCheckMcuVersion((code, bean) -> {
//                printLog("getLockBatteryMCUVersion callBack() code = " + code);
//                isRequestFinish();
//                parseLockBatteryMcuVersion(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    private void parseLockBatteryMcuVersion(GduFrame3 bean) {
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            cacheFlyVersion(FirmwareType.LOCK_BATTERY_CHECK_MCU_FIRMWARE.getEnValue(), "");
            return;
        }
        printLog("parseLockBatteryMcuVersion() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        final String mcuVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." +
                bean.frameContent[2];
        addNewFirmwareTypeAndVersion(FirmwareType.LOCK_BATTERY_CHECK_MCU_FIRMWARE.getEnValue(), mcuVersion);
        cacheFlyVersion(FirmwareType.LOCK_BATTERY_CHECK_MCU_FIRMWARE.getEnValue(), mcuVersion);
    }

    private Completable getRTKType() {
        printLog("getRTKType()");
        return Completable.fromAction(() -> {
            mCounter.incrementAndGet();
//            GduApplication.getSingleApp().gduCommunication.getRTKModelType((code, bean) -> {
//                printLog("getRTKType callBack() code = " + code);
//                isRequestFinish();
//                parseRTKType(bean);
//            });
        }).subscribeOn(Schedulers.io());
    }

    private void parseRTKType(GduFrame3 bean) {
        printLog("parseRTKType()");
        if (bean == null || bean.frameContent == null || bean.frameContent.length < 3) {
            return;
        }
        printLog("parseRTKType() rtkTypeStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
        GlobalVariable.sRTKModeType = bean.frameContent[2];
    }

    /**
     * 将飞机端版本号存储到sharedpreference
     *
     * @param key key
     * @param value value
     */
    private void cacheFlyVersion(String key, String value) {
        boolean haveEmptyData = StringUtils.isEmptyString(key)
                || mCurrentFlyType == null;
        if (haveEmptyData) {
            printLog("cacheFlyVersion() is empty key or type");
            return;
        }
        String codeKey = UpgradeUtils.getVersionKey(GduConfig.FLY_TYPE, mCurrentFlyType.getKey(), key);
        SPUtils.put(GduAppEnv.application, codeKey, CommonUtils.convertNull2EmptyStr(value));
    }

    /**
     * 清空飞机端固件版本信息
     *
     * @param key key
     */
    private void clearFlyVersion(String key) {
        for (PlanType planType : PlanType.values()) {
            String flyCodeKey = UpgradeUtils.getVersionKey(GduConfig.FLY_TYPE, planType.getKey(), key);
            SPUtils.put(GduAppEnv.application, flyCodeKey, "");
        }
    }

    public void isRequestFinish() {
        int reqFinishNum = mCounter.decrementAndGet();
        printLog("isRequestFinish() reqFinishNum = " + reqFinishNum);
        lastReceiveTime = System.currentTimeMillis();
        if (reqFinishNum != 0) {
            mHandle.removeMessages(KEY_OVER_TIME_MSG);
            mHandle.sendEmptyMessageDelayed(KEY_OVER_TIME_MSG, OVER_TIME);
            return;
        }
        mHandle.removeMessages(KEY_OVER_TIME_MSG);
        isGetVersion = false;
        onVersionReqEnd();
    }

    @Override
    public void dispose() {
        printLog("FirmwareVersion dispose()");
        isGetVersion = false;
        Optional.ofNullable(mDisposable).ifPresent(Disposable::dispose);
//        GetMountVersionTool.getInstance().dispose();
    }

    private void onVersionReqEnd() {
        printLog("FirmwareVersion onVersionReqEnd() mOnFirmwareVersionGetListenerListSize = "
                + mOnFirmwareVersionGetListenerList.size());
        Optional.ofNullable(mOnFirmwareVersionGetListenerList).ifPresent(list -> {
            for (OnFirmwareVersionGetListener mListener : list) {
                mListener.onVersionReqEnd();
            }
        });
    }

    public List<OnFirmwareVersionGetListener> getOnFirmwareVersionGetListenerList() {
        return mOnFirmwareVersionGetListenerList;
    }

    private void printLog(String logStr) {
        MyLogUtils.i(logStr);
        UpgradeLog2File.getSingle().saveData(logStr);
    }
}
