package com.gdu.demo.flight.msgbox;

import androidx.fragment.app.FragmentActivity;

import com.gdu.beans.WarnBean;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author wuqb
 * @date 2025/1/21
 * @Description 消息盒子内容管理
 */
public class MsgBoxManager {

    private final FragmentActivity mActivity;
    private final MsgBoxViewCallBack mCallback;
    private Disposable getAlarmDispose = null;
    private final List<WarnBean> mOldWarnBeans = new ArrayList<>();
    private final List<WarnBean> mNewWarnBeans = new ArrayList<>();//获取当前最新警告信息列表
    private HashMap<Long, WarnBean> mWarnTable;
    /** 当前语音提示byte对应值 */
    private byte currentShowIndex = -1;
    /** 当前任务类型 */
    private int mImportType = 1;

    public MsgBoxManager(FragmentActivity activity, int importType, MsgBoxViewCallBack callBack){
        this.mActivity = activity;
        this.mCallback = callBack;
        this.mImportType = importType;
        initAlarmObservable();
    }

    /**
     * 初始化getAlarmData观察者
     */
    public void initAlarmObservable() {
        if (getAlarmDispose == null || getAlarmDispose.isDisposed()) {
            getAlarmDispose = Observable.interval(0, 3000, TimeUnit.MILLISECONDS)
                    .to(RxLife.to(mActivity))
                    .subscribe(l -> getAlarmData(), throwable -> MyLogUtils.e("获取告警信息出错", throwable));
        }
    }

    /**
     * 获取告警或异常信息
     */
    private void getAlarmData() {
        boolean hadErr;
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            mNewWarnBeans.clear();
            mWarnTable = null;
            mWarnTable = CommonUtils.initWarnTable(mActivity);//初始化警告列表集合
            CommonUtils.updateWarnList(mActivity, mWarnTable);

            //将异常信息集合添加到mNewWarnBeans中
            for (Map.Entry<Long, WarnBean> mEntry : mWarnTable.entrySet()) {
                WarnBean mWarnBean = mEntry.getValue();
                if (mWarnBean.isErr) {
                    CommonUtils.listAddAvoidNull(mNewWarnBeans, mWarnBean);
                }
            }
            if (CommonUtils.isEmptyList(mOldWarnBeans)) {//如果展示的警告列表为空，将mNewWarnBeans添加到列表中
                currentShowIndex = 0;
                CommonUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
            } else {
                if (mOldWarnBeans.size() == mNewWarnBeans.size()) {//新老列表长度一样
                    for (int k = 0; k < mNewWarnBeans.size(); k++) {
                        WarnBean newWarnBean = mNewWarnBeans.get(k);
                        boolean isHaveWarn = false;
                        for (WarnBean mBean : mOldWarnBeans) {
                            if (newWarnBean.warnId == mBean.warnId) {
                                isHaveWarn = true;
                                mBean.warnStr = newWarnBean.warnStr;
                                break;
                            }
                        }
                        if (!isHaveWarn) {
                            currentShowIndex = 0;
                            mOldWarnBeans.clear();
                            CommonUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
                            break;
                        }
                    }
                } else {//新老列表长度不一样
                    currentShowIndex = 0;
                    mOldWarnBeans.clear();
                    CommonUtils.listAddAllAvoidNPE(mOldWarnBeans, mNewWarnBeans);
                }
            }
            hadErr = !CommonUtils.isEmptyList(mOldWarnBeans);
        } else {//飞行器未连接，认为没有警告信息，不展示警告
            hadErr = false;
        }
        if (hadErr) {
            WarnBean showWarnBean = mOldWarnBeans.get(currentShowIndex++);
            if (currentShowIndex >= mOldWarnBeans.size()) {
                currentShowIndex = 0;
            }
            long errId = showWarnBean.warnId;
            // 是否是警告类异常提示(靠近禁飞区 和 GPS>8&&<12的时候)
            if (mCallback == null) {
                MyLogUtils.i("getAlarmData() mViewCallBack is null");
                return;
            }
            if (errId == WarnBean.NEARNOFLY || errId == WarnBean.GPS) {
                mCallback.updateTitleTVColor(R.color.white);
                if (mWarnTable.containsKey(errId)) {
                    mCallback.updateTitleTvTxt(Objects.requireNonNull(mWarnTable.get(errId)).warnStr);
                }
                mCallback.updateHeadViewBg(R.drawable.shape_bg_f69d00_r2);
                mCallback.updateWarnList(mWarnTable);
            } else {
                mCallback.updateTitleTVColor(R.color.white);
                if (mWarnTable.containsKey(errId)) {
                    mCallback.updateTitleTvTxt(Objects.requireNonNull(mWarnTable.get(errId)).warnStr);
                }
                mCallback.updateHeadViewBg(R.drawable.shape_bg_ff0000_r2);
                mCallback.updateWarnList(mWarnTable);
            }
        } else {
            noErrHandle();
        }
    }

    private void noErrHandle() {
        currentShowIndex = -1;
        if (mCallback == null) {
            MyLogUtils.i("noErrHandle() mViewCallBack is null");
            return;
        }
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                mCallback.updateTitleTvTxt(mActivity.getString(R.string.DeviceNoConn));
                mCallback.updateHeadViewBg(android.R.color.transparent);
                mCallback.updateWarnList(mWarnTable);
                break;

            case Conn_MoreOne:
                mCallback.updateTitleTvTxt(mActivity.getString(R.string.Label_ConnMore));
                mCallback.updateHeadViewBg(android.R.color.transparent);
                mCallback.updateWarnList(mWarnTable);
                break;

            case Conn_Sucess:
                final StringBuilder sb = new StringBuilder();
                if (GlobalVariable.droneFlyState == 1) {
                    sb.append(mActivity.getString(R.string.Label_Good2Go));
                } else if (!GlobalVariable.planeHadLock) { //bug-3911-shang-20171111 室内未解锁，状态栏显示“正常飞行中”
                    sb.append(mImportType == 2 ? mActivity.getString(R.string.Label_InFlight_Task) : mActivity.getString(R.string.Label_InFlight_Manual));
                }

                mCallback.updateTitleTVColor(R.color.white);
                mCallback.updateTitleTvTxt(sb.toString());
                mCallback.updateWarnList(mWarnTable);
                mCallback.updateHeadViewBg(android.R.color.transparent);
                break;
            default:
                break;
        }
    }
}
