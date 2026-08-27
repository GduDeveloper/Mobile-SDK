package com.gdu.demo.ai;

import static java.lang.Math.abs;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.ai.util.DetectPositionCalUtil;
import com.gdu.demo.ai.view.SelectTargetView;
import com.gdu.demo.ai.view.TargetContainerView;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.core.GsonUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.ai.TargetMode;
import com.gdu.sdk.vision.bean.AlgorithmType;
import com.gdu.sdk.vision.bean.TrackMsg;
import com.gdu.sdk.vision.bean.TrackPointInfo;
import com.gdu.sdk.vision.listener.OnTargetTrackListener;
import com.gdu.videoprocess.QOSInfoCollector;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhangzhilai on 2018/9/5.
 * 目标检测和跟踪类
 */
public class TargetDetectHelper {
    private final static String TAG = TargetDetectHelper.class.getSimpleName();
    private List<TargetMode> mTargetModeList;
    private Context context;
    /** 循环检测状态标示 */
    private boolean isTargetDetect;

    private static TargetDetectHelper mTargetDetectHelper;
    private TargetContainerView targetContainerView;

    public static TargetDetectHelper getInstance(){
        if (mTargetDetectHelper == null) {
            mTargetDetectHelper = new TargetDetectHelper();
        }
        return mTargetDetectHelper;
    }

    public void init(Context context, TargetContainerView targetContainerView){
        this.context = context;
        log("init()");
        mTargetModeList = new CopyOnWriteArrayList<>();
        this.targetContainerView = targetContainerView;
        initListener();
    }

    private void initListener(){
        targetContainerView.getSelectTargetView().setOnSelectCallBack(onSelectCb);
        targetContainerView.getSelectTargetView().setViewType(SelectTargetView.DRAW_TARGET_FRAME);
        setSelectViewVisible(false);
        targetContainerView.setOnTargetViewListener(new TargetContainerView.OnTargetContainerViewListener() {
            @Override
            public void onTargetClick(TargetMode targetMode) {
                Log.d(TAG, ("onTrackClick onTargetClick  id =" + targetMode.getId() + "  [" + targetMode.getLeftX() + "," + targetMode.getLeftY()
                        + "," + targetMode.getWidth() + "," + targetMode.getHeight() + "]"));
                int pointX = targetMode.getLeftX();
                int pointY = targetMode.getLeftY();
                int width = targetMode.getWidth();
                int height = targetMode.getHeight();
                pointX = DetectPositionCalUtil.dTransX(QOSInfoCollector.VideoSizeMode.MODE_16_9, pointX);
                pointY = DetectPositionCalUtil.dTransY(QOSInfoCollector.VideoSizeMode.MODE_16_9, pointY);
                width = DetectPositionCalUtil.dTransWidth(QOSInfoCollector.VideoSizeMode.MODE_16_9, width);
                height = DetectPositionCalUtil.dTransHeight(QOSInfoCollector.VideoSizeMode.MODE_16_9, height);
                Log.d(TAG, ("onTrackClick params  id =" + targetMode.getId() + "  [" + pointX + "," + pointY
                        + "," + width + "," + height + "] TargetType: " + targetMode.getTargetType()));
                SdkDemoApplication.getAircraftInstance().getVision().detectTarget((byte) 1, (short) targetMode.getId(), pointX, pointY, width, height, (byte) targetMode.getTargetType(), error -> {
                    Log.d(TAG, "点击目标开始跟踪是否成功: " + (error == null));//error == null则为开启成功
                });
            }

            @Override
            public void onTargetDoubleClick(TargetMode targetMode) {

            }

            @Override
            public void onDoubleClick(float leftX, float leftY) {

            }

            @Override
            public void onLocateRegionSelect(Point one, Point two) {

            }

            @Override
            public void onLocateAim() {

            }
        });
        SdkDemoApplication.getAircraftInstance().getVision().setOnTargetTrackListener(new OnTargetTrackListener() {
            @Override
            public void onTargetDetecting(List<TargetMode> targetModes) {
//                Log.d(TAG, "智能跟踪多目标检测: " + ((targetModes != null) ? GsonUtils.toJson(targetModes) : 0));//error == null则为开启成功
                mTargetModeList.clear();
                mTargetModeList.addAll(targetModes);
//                if (targetContainerView != null) {
//                    targetContainerView.post(()->{
//                        targetContainerView.addTargetViewList(targetModes);
//                    });
//                }
            }

            @Override
            public void onTargetTracking(TrackPointInfo trackPointInfo) {
                Log.d("START_FOLLOW", "onTargetTracking receive - ");
                if (targetContainerView != null) {
                    targetContainerView.post(()->{
                        Log.d("START_FOLLOW", "onTargetTracking update ui - ");
                        updateFollowTarget(trackPointInfo);
                    });
                }
            }

            @Override
            public void onTargetLosePoint(TrackPointInfo trackPointInfo) {

            }

            @Override
            public void onTargetTrackState(TrackMsg trackMsg) {
                updateTrackState(trackMsg);
            }
        });

        targetContainerView.getSmartButtonView().getStopButton().setOnClickListener(view->{
            stopSmartFunction();
        });
        targetContainerView.getSmartButtonView().getTv_follow().setOnClickListener(view->{
            changeFollow();
        });
        targetContainerView.getSmartButtonView().getTv_stop_follow().setOnClickListener(view->{
            stopFollow();
        });

    }

    /**
     * 选择的目标View
     */

    private SelectTargetView.OnSelectCallBack onSelectCb = new SelectTargetView.OnSelectCallBack() {
        @Override
        public void onSelect(Point one, Point two) {
            if (abs((two.x - one.x)) < 50 || abs((two.y - one.y)) < 50) {
                targetContainerView.getSelectTargetView().clearDraw();
                return;
            }
            SdkDemoApplication.getAircraftInstance().getVision().detectTarget((byte) 0, (short) 0, one.x, one.y, two.x, two.y, (byte) 0, error -> {
                Log.d(TAG, "智能跟踪开启是否成功：" + (error  == null)); //error == null则为开启成功
            });
        }

        @Override
        public void onClick(Point point) {

        }

        @Override
        public void onDoubleClick(Point point) {

        }

        @Override
        public void regionTempIsShow(boolean isDown, boolean isClose) {

        }
    };

    /**
     * 根据智能跟踪状态，更新不同UI
     * @param trackMsg
     */
    private void updateTrackState(TrackMsg trackMsg){
        switch (trackMsg){
            case WhatCloseObstacle:
                break;
            case SMART_FOLLOW_MSG:
                log("进入到视频跟踪" );
                setSelectViewVisible(true);
                DroneUtils.setDiscernIsOpen(true);
                DroneUtils.setTargetDetectMode(true);
                sendShowFlow(true);
                if (targetContainerView != null) {
                    targetContainerView.post(()->{
                        Toast.makeText(context, "请选择或框选目标", Toast.LENGTH_SHORT).show();
                    });
                }
                //手动开启的
                setStopButtonVisible(false);
                //进入跟踪后，提示点选或框选
                setViewType(SelectTargetView.DRAW_TARGET_FRAME);
                break;
            case SMART_FOLLOW_GPS_MSG:
                log("------SMART_FOLLOW_GPS_MSG--- ");
                sendClearFlow();   // 关闭流程----ron
                clearALGUI();
                break;
            case STATE_START_VIDEO_TRACK:
                //开始跟踪后，提示云台已锁定
                sendClearFlow();
                setStopButtonVisible(true);
                setViewType(SelectTargetView.TARGET_FRAME_MOVE);
                break;
            case STATE_STOP_VIDEO_TRACK :
                //停止跟踪后，提示点选或框选
                setViewType(SelectTargetView.DRAW_TARGET_FRAME);
                sendClearFlow();
                log("------STATE_STOP_VIDEO_TRACK ----");
                setStopButtonVisible(false);
                setFollowButtonVisible(false);
                setStopFollowButtonVisible(false);
                break;
            case CLOSE_VIDEO_TRACK_SUCCEED:
            case STATE_QUIT_VIDEO_TRACK:
                setSelectViewVisible(false);
                sendClearFlow();
                sendShowFlow(false);
                sendClearDetectTargetFlow();

                log( "------CLOSE_VIDEO_TRACK_SUCCEED or STATE_QUIT_VIDEO_TRACK");
                clearALGUI();
                setFollowButtonVisible(false);
                setStopFollowButtonVisible(false);
                break;
            case FOLLOWING:
                sendShowFlow(true);
                sendRemoveAllTargetFlow();

                log("------FOLLOWING: isNewSmallPlane()");
                setStopButtonVisible(false);
                //进入跟随后，不提示
                setStopFollowButtonVisible(true);
                break;
            case TRACK_CMD_SEND_FAIL:
                sendClearFlow();
                if (targetContainerView != null) {
                    targetContainerView.post(()->{
                        Toast.makeText(context, "跟踪目标失败", Toast.LENGTH_SHORT).show();
                    });
                }
                break;
            case MULTIPLE_TARGET_TRACK:
//                if (BizSmartTrackManager.nowTargetType == TrackMsg.START_FOLLOW) {
//                    log("当前为单目标跟踪，不添加多目标数据（疑似handler缓存了数据）");
//                    return;
//                }
//                log("FOLLOW_TARGETS: " + mTargetModeList.size());
                if (targetContainerView != null) {
                    targetContainerView.post( ()->{
                        targetContainerView.addTargetViewList(mTargetModeList);
                    });
                }
                DroneUtils.setTargetDetectMode(true);
                break;
            case MULTIPLE_TARGET_FAIL:
                sendRemoveAllTargetFlow();
                break;
            case START_FOLLOW:
                log("START_FOLLOW: 目标已锁定");
                sendShowFlow(true);
                sendRemoveAllTargetFlow();
                setViewType(SelectTargetView.TARGET_FRAME_MOVE);
                setStopButtonVisible(true);
                setFollowButtonVisible(true);
                setStopFollowButtonVisible(false);
                break;
            case STOP_FOLLOW:
                //停止跟随后，进入点选/框选目标状态
                sendClearFlow();
                setViewType(SelectTargetView.DRAW_TARGET_FRAME);
                setFollowButtonVisible(false);
                setStopFollowButtonVisible(false);
                setStopButtonVisible(false);
                break;
            default:
                break;
        }
    }

    /**
     * 停止智能功能
     */
    private void stopSmartFunction() {
        AlgorithmType algorithmType = SdkDemoApplication.getAircraftInstance().getVision().getAlgorithmType();
        log(("点击 but_stop 按钮，stopSmartFunction() algorithmType = " + algorithmType + "; connStateEnum = " + IGduDroneDevice.get().isConnected()));
        if (algorithmType == AlgorithmType.Track_VIDEO) {
            SdkDemoApplication.getAircraftInstance().getVision().cancelSmartTrack(var1 -> log("停止智能功能指令发送成功：" + (var1 == null)));
        }
    }

    /**
     * GO - 进入跟随
     */
    private void changeFollow() {
        log("点击tv_follow，准备进入跟随模式 ");
        SdkDemoApplication.getAircraftInstance().getVision().changeFollow(var1 -> log("进入跟随指令发送成功：" + (var1 == null)));
    }

    /**
     * 停止跟随
     */
    private void stopFollow() {
        log("stopFollow  0x04");
        SdkDemoApplication.getAircraftInstance().getVision().stopFollow(var1 -> log("停止跟随指令发送成功：" + (var1 == null)));
    }

    private void sendClearFlow(){
        if (targetContainerView != null) {
            targetContainerView.post(()->{
                targetContainerView.getSelectTargetView().clearDraw();
            });
        }
    }

    private void sendShowFlow(boolean value){
        if (targetContainerView != null) {
            targetContainerView.post(()-> {
                targetContainerView.getSelectTargetView().setVisibility(value ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void sendRemoveAllTargetFlow(){
        if (targetContainerView != null) {
            targetContainerView.post(()-> {
                targetContainerView.removeAllTargetView();
            });
        }
    }

    private void sendClearDetectTargetFlow(){
        if (targetContainerView != null) {
            targetContainerView.post(()-> {
                targetContainerView.clearDetectTargetView();
            });
        }
    }

    private void setViewType(int viewType){
        if (targetContainerView != null) {
            targetContainerView.post(()-> {
                targetContainerView.getSelectTargetView().setViewType(viewType);
            });
        }
    }

    private void setSelectViewVisible(boolean isShow){
        if (targetContainerView != null) {
            targetContainerView.post(()-> {
                targetContainerView.getSelectTargetView().setVisibility(isShow ? View.VISIBLE : View.GONE);
            });
        }
    }

    /**
     * 更新跟踪时单目标状态
     */
    private void updateFollowTarget(TrackPointInfo info){
        int nowViewType = targetContainerView.getSelectTargetView().getViewType();
        Log.d(TAG, "drawType: " + nowViewType +", updateFollowTarget : " + GsonUtils.toJson(info));
        // 画框/点选模式下忽略位置更新，防止 updateUi() 强制将 viewType 覆盖为 TARGET_FRAME_MOVE
//        if (nowViewType == SelectTargetView.DRAW_TARGET_FRAME) return;
        List<Short> points = DetectPositionCalUtil.transXYWH2XYXY("0", info.getPointX(), info.getPointY(), info.getWidth(), info.getHeight());
        switch (info.getType()) {
            case 0:
                if (points != null && !CollectionUtils.isEmptyList(points)) {
                    targetContainerView.getSelectTargetView().updateUi(points.get(0), points.get(1), points.get(2), points.get(3));
                } else {
                    targetContainerView.getSelectTargetView().updateUi(0, 0, 0, 0);
                }
                break;
            case 1:
                if (points != null && !CollectionUtils.isEmptyList(points)) {
                    targetContainerView.getSelectTargetView().updateUiWithRed(points.get(0), points.get(1), points.get(2), points.get(3));
                }
                break;
            case 2:
                if (points != null && !CollectionUtils.isEmptyList(points)) {
                    targetContainerView.getSelectTargetView().updateTargetLoseUi(points.get(0), points.get(1), points.get(2), points.get(3));
                }
                break;
        }
    }

    /**
     * 清除掉当前显示的算法UI ron
     */
    private void clearALGUI() {
        log("clearALGUI()");
        setStopButtonVisible(false);
        setStopFollowButtonVisible(false);
        setFollowButtonVisible(false);
    }

    private void setStopButtonVisible(boolean visible) {
        if (targetContainerView != null) {
            targetContainerView.post(()->{
                targetContainerView.getSmartButtonView().getStopButton().setVisibility(visible ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void setStopFollowButtonVisible(boolean visible) {
        if (targetContainerView != null) {
            targetContainerView.post(()->{
                targetContainerView.getSmartButtonView().getTv_stop_follow().setVisibility(visible ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void setFollowButtonVisible(boolean visible) {
        if (targetContainerView != null) {
            targetContainerView.post(()->{
                targetContainerView.getSmartButtonView().getTv_follow().setVisibility(visible ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void log(String logStr){
        XLogger.INSTANCE.getAPP().d(TAG, logStr);
        Log.d(TAG, logStr);
    }

    public void onDestroy() {
        XLogger.INSTANCE.getAPP().i("onDestroy()");
    }

}
