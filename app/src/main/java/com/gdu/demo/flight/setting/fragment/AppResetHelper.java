package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.lifecycle.LifecycleOwner;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.util.CmdUtil;
import com.gdu.util.NetWorkUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.BaseScope;
import com.rxjava.rxlife.RxLife;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AppResetHelper extends BaseScope {
    private static final String TAG = AppResetHelper.class.getSimpleName();
    private final Context mContext;

    private final IViewCallback mViewCallback;

    public AppResetHelper(Context context, IViewCallback viewCallback, LifecycleOwner owner) {
        super(owner);
        mContext = context;
        mViewCallback = viewCallback;
    }

    private int sendCmdNum = 0;

    public void sendResetCmd() {
        MyLogUtils.i("sendResetCmd()");
        showOrHideLoadingDialog(true);
        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.resetDroneSet(((code, bean) -> {
//            MyLogUtils.i("resetDroneSet callback() code = " + code);
//            sendCmdNum--;
//            cheCmdIsSendFinish();
//        }));

        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.resetVisionParam(((code, bean) -> {
//            MyLogUtils.i("resetVisionParam callback() code = " + code);
//            sendCmdNum--;
//            cheCmdIsSendFinish();
//        }));

        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.clearSDMedia((code, bean) -> {
//            MyLogUtils.i("clearSDMedia callback() code = " + code);
//            sendCmdNum--;
//            cheCmdIsSendFinish();
//        }, (byte) 3);

        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.clearDroneCache((byte) 1, (code, bean) -> {
//            MyLogUtils.i("clearDroneCache callback() code = " + code);
//            sendCmdNum--;
//            cheCmdIsSendFinish();
//        });

        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.resetCoprocessor((byte) 1, (code, bean) -> {
//            MyLogUtils.i("resetCoprocessor callback() code = " + code);
//            sendCmdNum--;
//            cheCmdIsSendFinish();
//        });
    }

    private void cheCmdIsSendFinish() {
        if (sendCmdNum != 0) {
            return;
        }
        if (mViewCallback != null) {
            showOrHideLoadingDialog(false);
            mViewCallback.onCmdSendFinish();
        }
    }

    /* 退出登录操作应该放在最后，因为重置接口需要token */
    private void logoutHandle() {
        MyLogUtils.i("logoutHandle()");
//        ActiveUtil.logoutHandle(LifeScope.of(this), new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                cleanAppCache();
//            }
//        });
    }

    /* 流程：sendActiveCmd -> activeReset -> uploadResetDevInfo -> logoutHandle -> cleanAppCache*/
    public void resetDevActiveStatus() {
        MyLogUtils.i("resetDevActiveStatus()");
        showOrHideLoadingDialog(true);

        boolean isHaveError = StringUtils.isEmptyString(GlobalVariable.SN)
                || GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess
//                || StringUtils.isEmptyString(GduApplication.getSingleApp().getToken())
                || !NetWorkUtils.checkNetwork(mContext);
        MyLogUtils.i("resetDevActiveStatus() isHaveError = " + isHaveError);
        if (isHaveError) {
            showResetFail();
            return;
        }
//        ActiveUtil.sendActiveCmd(false, new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                if(code == ActiveUtil.ActiveCode.Success) {
//                    ActiveUtil.activeReset(LifeScope.of(AppResetHelper.this), new ActiveUtil.ActiveCallBack() {
//                        @Override
//                        public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                            if(code == ActiveUtil.ActiveCode.Success) {
//                                uploadResetDevInfo();
//                            } else {
//                                showResetFail();
//                            }
//                        }
//                    });
//                } else {
//                    showResetFail();
//                }
//            }
//        });
    }

    private void showResetFail() {
        showOrHideLoadingDialog(false);
        showToast(R.string.Label_ResetDroneFail);
    }

    /* cleanAppCache里面最终会关闭loading进度条 */
    public void cleanAppCache() {
        MyLogUtils.i("cleanAppCache() in sub thread");
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    boolean flag = true;
                    String delPath =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                    if(!StringUtils.isEmptyString(delPath)) {
                        // GlobalVariable.sFlavor渠道可能为gdu
//                        flag &= FileUtil.deleteDirByFile1(new File(delPath + File.separator + GlobalVariable.sFlavor));
//                        flag &= FileUtil.deleteDirByFile1(new File(delPath + File.separator + "gdu"));
                    }
                    // 清除 SharedPreferences 数据
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    flag &= preferences.edit().clear().commit();
                    // 清除登录信息
//                    CommonUtils.logoutDetailHandle(GduAppEnv.application);
                    // 命令行清除，可能失败
                    clearAppUserData(mContext.getPackageName());
                    emitter.onNext(flag);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io())
                .to(RxLife.toMain(this))
                .subscribe(delSuc -> {
                    MyLogUtils.i("cleanAppCache sucess");
                    showOrHideLoadingDialog(false);
                    tryGotoLogin();
                }, throwable -> {
                    MyLogUtils.e("cleanAppCache error", throwable);
                    showOrHideLoadingDialog(false);
                    tryGotoLogin();
                });
    }

    /* 命令行清除应用缓存,此方法会立即终止app进程，需放在最后 */
    private void clearAppUserData(String packageName) {
        MyLogUtils.i("clearAppUserData() packageName = " + packageName);
        String cmdStr = "pm clear " + packageName;
        CmdUtil.excCommand(cmdStr);
    }

    public void uploadResetDevInfo() {
        MyLogUtils.i("uploadResetDevInfo()");
        boolean isNotUpload = !NetWorkUtils.checkNetwork(GduAppEnv.application)
                || GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess
                || StringUtils.isEmptyString(GlobalVariable.SN);
        if (isNotUpload) {
            MyLogUtils.i("uploadResetDevInfo() judge can not upload");
            logoutHandle();
            return;
        }
//        ActiveUtil.uploadActiveInfo(false, LifeScope.of(this), new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                logoutHandle();
//            }
//        });
    }

    /* 清除登录信息后 重启应用 */
    private void tryGotoLogin() {
        //if (GlobalVariable.isUnUseLoginMode) return
//        GduActivityManager.getInstance().finishAllActivities();
//        Intent mIntent = new Intent(mContext, SplashActivity.class);
//        mContext.startActivity(mIntent);
    }



    private void showOrHideLoadingDialog(boolean isShow) {
        if (mViewCallback != null) {
            mViewCallback.showOrHideLoadingDialog(isShow);
        }
    }

    private void showToast(int id) {
        if (mViewCallback != null) {
            mViewCallback.showToaster(id);
        }
    }

    public interface IViewCallback {
        /**
         * 显示/隐藏加载窗
         * @param isShow 是否显示
         */
        void showOrHideLoadingDialog(boolean isShow);

        /**
         * 显示提示信息
         * @param resId String的id
         */
        void showToaster(int resId);

        /**
         * 指令集发送完成
         */
        void onCmdSendFinish();
    }

}
