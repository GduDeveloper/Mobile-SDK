//package com.gdu.demo.utils;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.gdu.config.ConnStateEnum;
//import com.gdu.config.GlobalVariable;
//import com.gdu.drone.FirmwareType;
//import com.gdu.util.MD5Util;
//import com.gdu.util.MyConstants;
//import com.gdu.util.NetWorkUtils;
//import com.gdu.util.logs.AppLog;
//import com.google.gson.Gson;
//import com.rxjava.rxlife.RxLife;
//
//import java.util.HashMap;
//
//import io.reactivex.rxjava3.schedulers.Schedulers;
//
///**
// * @author dingwenxiang
// * @date 2024/7/17 10:38
// * @desc 统一管理无人机激活/重置接口，与业务隔离方便维护
// */
//public class ActiveUtil {
//    private static final String TAG = ActiveUtil.class.getSimpleName();
//    private static FlightRecordService mFlightRecordService = null;
//    private static LoginService mLoginService = null;
//
//    /* 激活回调code */
//    public enum ActiveCode {
//        Success, Fail, Error, NoBean,
//        NoAvailable, NoConnect, NoSN, NoToken, NoControl, NoNetwork, NoUserInfo
//    }
//
//    public interface ActiveCallBack {
//        void onCallBack(@NonNull ActiveCode code, @NonNull String s);
//    }
//
//    /* 获取接口，方便更改来源 */
//    private static FlightRecordService getFlightRecordService() {
//        if (mFlightRecordService == null) {
//            mFlightRecordService = RetrofitClient.getApiPLM(FlightRecordService.class);
//        }
//        return mFlightRecordService;
//    }
//
//    private static LoginService getLoginService() {
//        if (mLoginService == null) {
//            mLoginService = RetrofitClient.getApiAppConfig(LoginService.class);
//        }
//        return mLoginService;
//    }
//
//    private static Context getApp() {
//        return GduApplication.context;
//    }
//
//    /* 激活/重置，前提检查，目前未使用供上层业务参考 */
//    public static boolean activeCheck(@Nullable ActiveCallBack callBack) {
//        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoConnect, getApp().getString(R.string.update_conn_err1));
//            }
//            return false;
//        }
//        if (CommonUtils.isEmptyString(GlobalVariable.SN)) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoSN, getApp().getString(R.string.Msg_unSNTip));
//            }
//            return false;
//        }
//        final String tokenStr = GduApplication.getSingleApp().getToken();
//        if (CommonUtils.isEmptyString(tokenStr)) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoToken, getApp().getString(R.string.string_please_login));
//            }
//            return false;
//        }
//        if (!NetworkingHelper.isRCHasControlPower()) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoControl, getApp().getString(R.string.Label_NoControlPermissionTip));
//            }
//            return false;
//        }
//        if (!NetWorkUtils.checkNetwork(getApp())) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoNetwork, getApp().getString(R.string.Label_Record_List_noNetwork));
//            }
//            return false;
//        }
//        String userId = UserUtil.userId();
//        if (userId == null || userId.isEmpty()) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.NoUserInfo, getApp().getString(R.string.string_please_login));
//            }
//            return false;
//        }
//        return true;
//    }
//
//    /* 激活设备 getLoginService().deviceBind */
//    public static void activeDev(@NonNull LifeScope holder, @NonNull ActiveCallBack callBack) {
//        String tokenStr = GduApplication.getSingleApp().getToken();
//        String userId = UserUtil.userId();
//        String flightVersionStr = CommonUtils.getFlyVersion(FirmwareType.FLIGHT_VERSION.getEnValue());
//
//        HashMap<String, String> params = new HashMap<>();
//        params.put("userId", userId);
//        params.put("deviceSn", GlobalVariable.SN);
//        params.put("firewareVersion", flightVersionStr);
//        AppLog.i(TAG, "activeDev() deviceBind input params--userid:" + userId + ";token:" + tokenStr
//                + ";firewareVersion:" + flightVersionStr + ";deviceSn:" + GlobalVariable.SN);
//        getLoginService().deviceBind("JSESSIONID=" + tokenStr, params)
//                .subscribeOn(Schedulers.newThread())
//                .to(holder.owner != null ? RxLife.toMain(holder.owner) : RxLife.toMain(holder.scope))
//                .subscribe(baseCallbackBean -> {
//                    if (baseCallbackBean == null) {
//                        AppLog.i(TAG, "deviceBind NoBean");
//                        callBack.onCallBack(ActiveCode.NoBean, getApp().getString(R.string.string_activate_fail));
//                        return;
//                    }
//                    if (baseCallbackBean.isSuccess()) {
//                        AppLog.i(TAG, "deviceBind Success");
//                        callBack.onCallBack(ActiveCode.Success, "");
//                    } else {
//                        String errTipStr = CommonUtils.isEmptyString(baseCallbackBean.getMsg()) ?
//                                getApp().getString(R.string.string_activate_fail) : baseCallbackBean.getMsg();
//                        AppLog.i(TAG, "deviceBind Fail " + errTipStr);
//                        callBack.onCallBack(ActiveCode.Fail, errTipStr);
//                    }
//                }, throwable -> {
//                    AppLog.i(TAG, "激活设备出错出错 throwableMsg = " + (throwable != null ? throwable.getMessage() : "throwable = null"));
//                    if (throwable instanceof ApiException) {
//                        if (CommonUtils.devHasActive(((ApiException) throwable).getCode())) {//设备已激活,流程和激活成功流程保持一致
//                            AppLog.i(TAG, "deviceBind Success");
//                            callBack.onCallBack(ActiveCode.Success, "");
//                        } else {
//                            String errStr = CommonUtils.getErrStr(getApp(),
//                                    ((ApiException) throwable).getCode(),
//                                    getApp().getString(R.string.Label_ActiveDevErr));
//                            AppLog.i(TAG, "deviceBind Error " + errStr);
//                            callBack.onCallBack(ActiveCode.Error, errStr);
//                        }
//                    } else {
//                        String err = "";
//                        if (throwable != null && throwable.getMessage() != null) {
//                            err = throwable.getMessage();
//                        }
//                        AppLog.i(TAG, "deviceBind Error " + err);
//                        callBack.onCallBack(ActiveCode.Error, err);
//                    }
//                });
//    }
//
//    /* 重置激活设备 getLoginService().activeReset */
//    public static void activeReset(@NonNull LifeScope holder, @NonNull ActiveCallBack callBack) {
//        HashMap<String, String> params = new HashMap<>();
//        String tokenStr = GduApplication.getSingleApp().getToken();
//        params.put("deviceSn", GlobalVariable.SN);
//        AppLog.i(TAG, "resetDevStatus() activeReset input params--token:" + tokenStr + ";deviceSn:" + GlobalVariable.SN);
//        getLoginService().activeReset("JSESSIONID=" + tokenStr, params)
//                .subscribeOn(Schedulers.newThread())
//                .to(holder.owner != null ? RxLife.toMain(holder.owner) : RxLife.toMain(holder.scope))
//                .subscribe(bean -> {
//                    if (bean == null) {
//                        AppLog.i(TAG, "activeReset() NoBean!");
//                        callBack.onCallBack(ActiveCode.NoBean, "");
//                        return;
//                    }
//                    if (bean.isSuccess()) {
//                        AppLog.i(TAG, "activeReset() Success!");
//                        callBack.onCallBack(ActiveCode.Success, "");
//                    } else {
//                        AppLog.i(TAG, "activeReset() Fail! " + bean.getMsg());
//                        callBack.onCallBack(ActiveCode.Fail, "");
//                    }
//                }, throwable -> {
//                    String err = "";
//                    if (throwable != null && throwable.getMessage() != null) {
//                        err = throwable.getMessage();
//                    }
//                    AppLog.i(TAG, "activeReset() Error!  " + err);
//                    callBack.onCallBack(ActiveCode.Error, err);
//                });
//    }
//
//    /* 生命周期激活/重置接口 getFlightRecordService().sendCustomUrlAndBody
//    WebUrlConfig.S220_UPLOAD_URL_AND_PORT + WebUrlConfig.SEND_UAV_ACTIVE */
//    public static void uploadActiveInfo(boolean isActive, @NonNull LifeScope holder, @NonNull ActiveCallBack callBack) {
//        AppLog.i(TAG, "uploadActiveInfo(), isActive=" + isActive);
//        long curTime = System.currentTimeMillis();
//        long curTimeSecond = curTime / 1000;
//        HashMap<String, Object> mParams = new HashMap<>();
//        mParams.put("userId", UserUtil.userId());
//        mParams.put("deviceSn", GlobalVariable.SN);
//        mParams.put("opTime", curTime);
//        mParams.put("longitude", String.valueOf(GlobalVariable.GPS_Lon));
//        mParams.put("latitude", String.valueOf(GlobalVariable.GPS_Lat));
//        mParams.put("opType", isActive ? "key_optype_active" : "key_optype_reset");
//        mParams.put("time", curTime);
//        String upBeanJsonStr = new Gson().toJson(mParams);
//        RequestBody contentBody = FormBody.create(upBeanJsonStr, MediaType.parse("application/json; charset=utf-8"));
//        String contentStr = MyConstants.S220_API_REQUEST_KEY + "#" + curTimeSecond + "#" + upBeanJsonStr + "#";
//        String signMd5 = MD5Util.stringToMD5(contentStr);
//        String url = WebUrlConfig.S220_UPLOAD_URL_AND_PORT + WebUrlConfig.SEND_UAV_ACTIVE;
//        //MyLogUtils.i("uploadActiveInfo(), 请求入参=" + upBeanJsonStr);
//        //MyLogUtils.i("uploadActiveInfo(), sign=" + signMd5+",timestamp="+curTimeSecond);
//        getFlightRecordService().sendCustomUrlAndBody(signMd5, String.valueOf(curTimeSecond), url, contentBody)
//                .subscribeOn(Schedulers.newThread())
//                .to(holder.owner != null ? RxLife.toMain(holder.owner) : RxLife.toMain(holder.scope))
//                .subscribe(data -> {
//                    if (data != null) {
//                        if (data.isSuccess()) {
//                            AppLog.i(TAG, "uploadActiveInfo() success!");
//                            callBack.onCallBack(ActiveCode.Success, "");
//                        } else {
//                            AppLog.i(TAG, "uploadActiveInfo() fail:" + data.getMsg());
//                            callBack.onCallBack(ActiveCode.Fail, data.getMsg());
//                        }
//                    } else {
//                        AppLog.i(TAG, "uploadActiveInfo() NoBean");
//                        callBack.onCallBack(ActiveCode.NoBean, "");
//                    }
//                }, throwable -> {
//                    String err = "";
//                    if (throwable != null && throwable.getMessage() != null) {
//                        err = throwable.getMessage();
//                    }
//                    callBack.onCallBack(ActiveCode.Error, err);
//                    AppLog.i(TAG, "uploadActiveInfo() error:" + err);
//                });
//    }
//
//    /** 生命周期查询激活状态接口
//     *   "code": 0,
//     *   "msg": "success",
//     *   "data": {
//     *     "userId": "531",
//     *     "deviceSn": "CGD4N1S423G100175",
//     *     "opTime": "1697857153413",
//     *     "longitude": "114.4220761",
//     *     "latitude": "30.4683625",
//     *     "province": "湖北省",
//     *     "city": "武汉市",
//     *     "formattedAddress": "湖北省武汉市江夏区关东街道黄龙山南路中冶南方连铸技术工程有限责任公司",
//     *     "opType": "op_type_active",
//     *     "time": "1697857153413",
//     *     "model": null
//     *   }
//     *   // 经测试发现 optype有两种：op_type_active 或 key_optype_active，需加上op_type_active兼容老版本的激活状态
//     */
//    public static void getUavActive(@NonNull LifeScope holder, @NonNull ActiveCallBack callBack) {
//        if(CommonUtils.isEmptyString(GlobalVariable.SN)) {
//            callBack.onCallBack(ActiveCode.NoSN, "");
//            return;
//        }
//        long curTime = System.currentTimeMillis();
//        long curTimeSecond = curTime / 1000;
//        HashMap<String, Object> mParams = new HashMap<>();
//        mParams.put("sn", GlobalVariable.SN);
//        String upBeanJsonStr = new Gson().toJson(mParams);
//        RequestBody contentBody = FormBody.create(upBeanJsonStr, MediaType.parse("application/json; charset=utf-8"));
//        String contentStr = MyConstants.S220_API_REQUEST_KEY + "#" + curTimeSecond + "#" + upBeanJsonStr + "#";
//        String signMd5 = MD5Util.stringToMD5(contentStr);
//        String url = WebUrlConfig.S220_UPLOAD_URL_AND_PORT + WebUrlConfig.GET_UAV_ACTIVE;
//        getFlightRecordService().sendCustomUrlAndBody(signMd5, String.valueOf(curTimeSecond), url, contentBody)
//                .subscribeOn(Schedulers.newThread())
//                .to(holder.owner != null ? RxLife.toMain(holder.owner) : RxLife.toMain(holder.scope))
//                .subscribe(data -> {
//                    if (data != null && data.isSuccess()) {
//                        AppLog.i(TAG, "getUavActive() success! ");
//                        boolean ret = false;
//                        if(data.getData() != null) {
//                            String res = new Gson().toJson(data.getData());
//                            ret = res.contains("key_optype_active") || res.contains("op_type_active");
//                        }
//                        callBack.onCallBack(ActiveCode.Success, Boolean.toString(ret));
//                    } else {
//                        AppLog.i(TAG, "getUavActive() fail");
//                        callBack.onCallBack(ActiveCode.Fail, "");
//                    }
//                }, throwable -> {
//                    String err = "";
//                    if (throwable != null && throwable.getMessage() != null) {
//                        err = throwable.getMessage();
//                    }
//                    callBack.onCallBack(ActiveCode.Error, err);
//                    AppLog.i(TAG, "getUavActive() error:" + err);
//                });
//    }
//
//    /* 激活飞机 gduCommunication.activeFlight 注意：此回调在子线程！ */
//    public static void sendActiveCmd(boolean isActive, @Nullable ActiveCallBack callBack) {
//        AppLog.i(TAG, "sendActiveCmd(), isActive=" + isActive);
//        if (GduApplication.getSingleApp().gduCommunication == null) {
//            if (callBack != null) {
//                callBack.onCallBack(ActiveCode.Error, "");
//            }
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.activeFlight((byte) (isActive ? 0 : 1), (code, bean) -> {
//            AppLog.i(TAG, "sendActiveCmd callback() code = " + code);
//            if (code == 0) {
//                if (callBack != null) {
//                    callBack.onCallBack(ActiveCode.Success, String.valueOf(code));
//                }
//            } else {
//                if (callBack != null) {
//                    callBack.onCallBack(ActiveCode.Fail, String.valueOf(code));
//                }
//            }
//        });
//    }
//
//
//    /* 千寻激活后，在回调应使用uploadActiveInfo上报生命周期；千寻目前无激活重置接口 */
//    public static void activeQxsdk(@NonNull ActiveCallBack callBack) {
//        QxSdkManager.getInstance().setOnActiveSDK(new QxSdkManager.OnActiveSDKCallBack() {
//            @Override
//            public void activeSuccess() {
//                AppLog.i(TAG, "QXSDK  activate  success ");
//                callBack.onCallBack(ActiveCode.Success, "");
//            }
//
//            @Override
//            public void activeOnFail() {
//                AppLog.i(TAG, "QXSDK  activate  fail ");
//                callBack.onCallBack(ActiveCode.Fail, "");
//            }
//        });
//        QxSdkManager.getInstance().activate();
//    }
//
//    public static void logoutHandle(@NonNull LifeScope holder, @Nullable ActiveCallBack callBack) {
//        AppLog.i(TAG, "logoutHandle()");
//        getLoginService().loginOut("JSESSIONID=" + GduApplication.getSingleApp().getToken())
//                .subscribeOn(Schedulers.io())
//                .to(holder.owner != null ? RxLife.toMain(holder.owner) : RxLife.toMain(holder.scope))
//                .subscribe(bean -> {
//                    if (bean == null || !bean.isSuccess()) {
//                        AppLog.i(TAG, "logoutHandle() Fail " + (bean != null? bean.getMsg() : ""));
//                        if (callBack != null) {
//                            callBack.onCallBack(ActiveCode.Fail,"");
//                        }
//                        return;
//                    }
//                    AppLog.i(TAG, "logoutHandle() Success");
//                    if (callBack != null) {
//                        callBack.onCallBack(ActiveCode.Success,"");
//                    }
//                }, throwable -> {
//                    String err = "";
//                    if (throwable != null && throwable.getMessage() != null) {
//                        err = throwable.getMessage();
//                    }
//                    AppLog.i(TAG, "logoutHandle() Error "+ err);
//                    if (callBack != null) {
//                        callBack.onCallBack(ActiveCode.Error, err);
//                    }
//                });
//    }
//}
