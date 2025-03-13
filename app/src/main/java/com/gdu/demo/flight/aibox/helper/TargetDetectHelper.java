package com.gdu.demo.flight.aibox.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.gdu.AlgorithmMark;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.drone.TargetMode;
import com.gdu.util.logger.MyLogUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhangzhilai on 2018/9/5.
 * 目标检测和跟踪类
 */
public class TargetDetectHelper {
    private final static String TAG = TargetDetectHelper.class.getSimpleName();
    private final int ADD_TARGET = 0x10;
    /** 目标检测成功 */
    private final int TARGET_DETECT_SUCCEED = 0x00;
    /** 未检测到目标 */
    private final int TARGET_DETECT_NULL = 0x01;
    /** 结束检测 */
    private final int TARGET_DETECT_FINISHED = 0x02;
    /** 目标检测失败 */
    private final int TARGET_DETECT_FAILED = 0x05;
    /** 框选定位成功 */
    private final int TARGET_LOCATE_SUCCEED = 0x06;
    /** 框选定位失败 */
    private final int TARGET_LOCATE_FAILED = 0x11;
    /** 目标检测发送成功 */
    private final int TARGET_DETECT_SEND_SUCCEED = 0x21;
    /** 目标检测发送失败 */
    private final int TARGET_DETECT_SEND_FAILED = 0x31;
    /** 目标检测退出成功 */
    private final int TARGET_DETECT_QUIT_SUCCEED = 0x22;
    /** 目标检测退出失败 */
    private final int TARGET_DETECT_QUIT_FAILED = 0x23;

    private final int TARGET_DETECT_CLEAN = 0x28;

    private final int TARGET_LOCATE_SEND_SUCCEED = 0x41;
    private final int TARGET_LOCATE_SEND_FAILED = 0x51;

    private OnTargetDetectListener mOnTargetDetectListener;
    private Context mContext;
    private Handler mHandler;
    private List<TargetMode> mTargetModeList;
    /** 循环检测状态标示 */
    private boolean isTargetDetect;

    private static TargetDetectHelper mTargetDetectHelper;

    public static TargetDetectHelper getInstance(){
        if (mTargetDetectHelper == null) {
            mTargetDetectHelper = new TargetDetectHelper();
        }
        return mTargetDetectHelper;
    }

    public void init(Context context){
        MyLogUtils.i("init()");
        mContext = context;
        mTargetModeList = new CopyOnWriteArrayList<>();

        initHandler();
        addTargetDetectACK();
    }

    public void setOnTargetDetectListener(OnTargetDetectListener onTargetDetectListener) {
        mOnTargetDetectListener = onTargetDetectListener;
    }

    private void initHandler() {
        MyLogUtils.i("initHandler()");
        mHandler = new Handler(msg -> {
            switch (msg.what) {
                case ADD_TARGET:
                    break;
                case TARGET_DETECT_FAILED:
                    String failedToast = msg.obj.toString();
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetDetect(false, null);
                    }
                    Toast.makeText(mContext, failedToast, Toast.LENGTH_SHORT).show();
                    break;
                case TARGET_DETECT_SUCCEED:
                    if (msg.obj == null || !isTargetDetect) {
                        mTargetModeList = new CopyOnWriteArrayList<>();
                        return true;
                    }
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetDetect(true, mTargetModeList);
                    }
                    break;
                case TARGET_LOCATE_FAILED:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetLocate(false, null);
                    }
                    Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case TARGET_LOCATE_SUCCEED:
//                    TargetMode tempTargetMode = TargetUtil.copyTarget(mLocateTarget);
//                    if (mOnTargetDetectListener != null) {
//                        mOnTargetDetectListener.onTargetLocate(true, tempTargetMode);
//                    }
                    break;
                case TARGET_DETECT_SEND_FAILED:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetDetectSend(false);
                    }
                    break;
                case TARGET_DETECT_SEND_SUCCEED:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetDetectSend(true);
                    }
                    break;
                case TARGET_LOCATE_SEND_FAILED:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetLocateSend(false);
                    }
                    break;
                case TARGET_LOCATE_SEND_SUCCEED:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetLocateSend(true);
                    }
                    break;
                case TARGET_DETECT_QUIT_SUCCEED:
                    GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.NONE;
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onDetectClosed();
                    }
                    break;
                case TARGET_DETECT_QUIT_FAILED:
                    break;
                case TARGET_DETECT_CLEAN:
                    if (mOnTargetDetectListener != null) {
                        mOnTargetDetectListener.onTargetDetect(false, null);
                    }
                    break;
                default:
                    break;
            }
            return true;
        });
    }

    /**
     * 添加目标检测和跟踪的长监听
     */
    private void addTargetDetectACK() {
        MyLogUtils.i("addTargetDetectACK()");
        SdkDemoApplication.getAircraftInstance().getGduVision().setOnTargetDetectListener(new com.gdu.sdk.vision.OnTargetDetectListener() {
            @Override
            public void onTargetDetecting(List<TargetMode> list) {
                if (list == null){
                    mTargetModeList.clear();
                    returnDetectResult(TARGET_DETECT_CLEAN, "");
                }else {
                    mTargetModeList.clear();
                    mTargetModeList.addAll(list);
                    isTargetDetect = true;
                    returnDetectResult(TARGET_DETECT_SEND_SUCCEED, "");
                    returnDetectResult(TARGET_DETECT_SUCCEED, "");
                }
            }

            @Override
            public void onTargetDetectFailed(int i) {
                Message message = Message.obtain();
                message.what = TARGET_DETECT_FAILED;
                mHandler.sendMessage(message);
            }

            @Override
            public void onTargetDetectStart() {

            }

            @Override
            public void onTargetDetectFinished() {

            }
        });
    }

    /**
     * 直接显示目标框
     */
    public void startShowTarget(){
        MyLogUtils.i("startShowTarget()");
        isTargetDetect = true;
        Message message = new Message();
        message.what = TARGET_DETECT_SEND_SUCCEED;
        mHandler.sendMessage(message);
    }

    private void returnDetectResult(int detectResult, String result){
        MyLogUtils.i("returnDetectResult() detectResult = " + detectResult + "; result = " + result);
        Message message = Message.obtain();
        message.what = detectResult;
        message.obj = result;
        if (mHandler != null) {
            mHandler.sendMessage(message);
        }
    }

    public void onDestroy() {
        MyLogUtils.i("onDestroy()");
        mContext = null;
        mOnTargetDetectListener = null;
    }

    public interface OnTargetDetectListener {
        void onTargetDetect(boolean isSuccess, List<TargetMode> targetList);  //目标检测回调

        void onTargetDetectSend(boolean isSuccess);  //目标检测指令发送回调

        void onTargetLocateSend(boolean isSuccess);  //目标定位指令发送回调

        void onTargetLocate(boolean isSuccess, TargetMode targetMode);      //目标定位回调

        void onDetectClosed();
    }
}
