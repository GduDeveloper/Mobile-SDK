//package com.gdu.demo.flight.msgbox;
//
//import androidx.fragment.app.FragmentActivity;
//
//import com.gdu.demo.R;
//import com.gdu.demo.SdkDemoApplication;
//import com.gdu.demo.utils.DroneUtils;
//import com.gdu.lib.util.CollectionUtils;
//import com.gdu.lib.util.ThreadHelper;
//import com.gdu.lib.util.core.XLogger;
//import com.gdu.msdk.device.interfaces.IGduDroneDevice;
//import com.gdu.sdk.base.Diagnostics;
//import com.gdu.sdk.manager.SDKManager;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
///**
// * @author wuqb
// * @date 2025/1/21
// * @Description 消息盒子内容管理
// */
//public class MsgBoxManager implements Diagnostics.DiagnosticsInformationCallback {
//
//    private final FragmentActivity mActivity;
//    private final MsgBoxViewCallBack mCallback;
//    private final List<Diagnostics> mOldWarnBeans = new ArrayList<>();
//    private final List<Diagnostics> mNewWarnBeans = new ArrayList<>();//获取当前最新警告信息列表
//    private HashMap<Long, Diagnostics> mWarnTable;
//    /** 当前语音提示byte对应值 */
//    private byte currentShowIndex = -1;
//    /** 当前任务类型 */
//    private int mImportType = 1;
//
//    public MsgBoxManager(FragmentActivity activity, int importType, MsgBoxViewCallBack callBack){
//        this.mActivity = activity;
//        this.mCallback = callBack;
//        this.mImportType = importType;
//        if (SDKManager.getInstance().getProduct() != null)
//            SDKManager.getInstance().getProduct().setDiagnosticsInformationCallback(this);
//    }
//
//    /**
//     * 获取告警或异常信息
//     */
//    private void getAlarmData() {
//        boolean hadErr;
//        if (IGduDroneDevice.get().isConnected()) {
//            mNewWarnBeans.clear();
//            mWarnTable = null;
////            mWarnTable = CommonUtils.initWarnTable(mActivity);//初始化警告列表集合
////            CommonUtils.updateWarnList(mActivity, mWarnTable);
//
//            //将异常信息集合添加到mNewWarnBeans中
////            for (Map.Entry<Long, Diagnostics> mEntry : mWarnTable.entrySet()) {
////                Diagnostics mWarnBean = mEntry.getValue();
////                if (mWarnBean.isErr) {
////                    CollectionUtils.listAddAvoidNull(mNewWarnBeans, mWarnBean);
////                }
////            }
//            if (CollectionUtils.isEmptyList(mOldWarnBeans)) {//如果展示的警告列表为空，将mNewWarnBeans添加到列表中
//                currentShowIndex = 0;
//                CollectionUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
//            } else {
////                if (mOldWarnBeans.size() == mNewWarnBeans.size()) {//新老列表长度一样
////                    for (int k = 0; k < mNewWarnBeans.size(); k++) {
////                        WarnBean newWarnBean = mNewWarnBeans.get(k);
////                        boolean isHaveWarn = false;
////                        for (WarnBean mBean : mOldWarnBeans) {
////                            if (newWarnBean.warnId == mBean.warnId) {
////                                isHaveWarn = true;
////                                mBean.warnStr = newWarnBean.warnStr;
////                                break;
////                            }
////                        }
////                        if (!isHaveWarn) {
////                            currentShowIndex = 0;
////                            mOldWarnBeans.clear();
////                            CollectionUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
////                            break;
////                        }
////                    }
////                } else {//新老列表长度不一样
////                    currentShowIndex = 0;
////                    mOldWarnBeans.clear();
////                    CollectionUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
////                }
//            }
//            hadErr = !CollectionUtils.isEmptyList(mOldWarnBeans);
//        } else {//飞行器未连接，认为没有警告信息，不展示警告
//            hadErr = false;
//        }
//        if (hadErr) {
//            Diagnostics showWarnBean = mOldWarnBeans.get(currentShowIndex++);
//            if (currentShowIndex >= mOldWarnBeans.size()) {
//                currentShowIndex = 0;
//            }
////            long errId = showWarnBean.warnId;
//            // 是否是警告类异常提示(靠近禁飞区 和 GPS>8&&<12的时候)
//            if (mCallback == null) {
//                XLogger.INSTANCE.getAPP().i("getAlarmData() mViewCallBack is null");
//                return;
//            }
////            if (errId == WarnBean.NEARNOFLY /*|| errId == WarnBean.GPS*/) {
////                mCallback.updateTitleTVColor(R.color.white);
////                if (mWarnTable.containsKey(errId)) {
////                    mCallback.updateTitleTvTxt(Objects.requireNonNull(mWarnTable.get(errId)).warnStr);
////                }
////                mCallback.updateHeadViewBg(R.drawable.shape_bg_f69d00_r2);
////                mCallback.updateWarnList(mWarnTable);
////            } else {
////                mCallback.updateTitleTVColor(R.color.white);
////                if (mWarnTable.containsKey(errId)) {
////                    mCallback.updateTitleTvTxt(Objects.requireNonNull(mWarnTable.get(errId)).warnStr);
////                }
////                mCallback.updateHeadViewBg(R.drawable.shape_bg_ff0000_r2);
////                mCallback.updateWarnList(mWarnTable);
////            }
//        } else {
//            noErrHandle();
//        }
//    }
//
//    private void noErrHandle() {
//        currentShowIndex = -1;
//        if (mCallback == null) {
//            XLogger.INSTANCE.getAPP().i("noErrHandle() mViewCallBack is null");
//            return;
//        }
//        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
//            ThreadHelper.runOnUiThread(() -> {
//                mCallback.updateTitleTvTxt(mActivity.getString(R.string.DeviceNoConn));
//                mCallback.updateHeadViewBg(android.R.color.transparent);
//            });
//        } else {
//            final StringBuilder sb = new StringBuilder();
//            if (DroneUtils.isGround()) {
//                sb.append(mActivity.getString(R.string.Label_Good2Go));
//            } else if (!DroneUtils.getPlaneHadLock()) { //bug-3911-shang-20171111 室内未解锁，状态栏显示“正常飞行中”
//                sb.append(mImportType == 2 ? mActivity.getString(R.string.Label_InFlight_Task) : mActivity.getString(R.string.Label_InFlight_Manual));
//            }
//            ThreadHelper.runOnUiThread(() -> {
//                mCallback.updateTitleTVColor(R.color.white);
//                mCallback.updateTitleTvTxt(sb.toString());
//                mCallback.updateHeadViewBg(android.R.color.transparent);
//            });
//        }
//    }
//
//    @Override
//    public void onUpdate(List<Diagnostics> list) {
//        getAlarmData();
//    }
//}
