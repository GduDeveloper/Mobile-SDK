package com.gdu.demo;

import android.content.res.ColorStateList;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.gdu.common.error.Error;
import com.gdu.demo.ai.AiDetectHelper;
import com.gdu.demo.databinding.ActivityFlightBinding;
import com.gdu.demo.ai.TargetDetectHelper;
import com.gdu.demo.flight.msgbox.MsgBoxManager;
import com.gdu.demo.flight.msgbox.MsgBoxPopView;
import com.gdu.demo.flight.setting.fragment.SettingDialogFragment;
import com.gdu.demo.utils.GisUtil;
import com.gdu.demo.utils.LoadingDialogUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.widget.TopStateView;
import com.gdu.demo.widget.zoomView.S220CustomSizeFocusHelper;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.gimbal.GimbalState;
import com.gdu.lib.util.StringUtils;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.msdk.device.component.interfaces.IVision;
import com.gdu.msdk.key.value.CycleFCInfo1;
import com.gdu.msdk.key.value.CycleRadarInfo;
import com.gdu.msdk.util.KVObserver;
import com.gdu.radar.ObstaclePoint;
import com.gdu.radar.PerceptionInformation;
import com.gdu.sdk.base.Diagnostics;
import com.gdu.sdk.codec.CodecManager;
import com.gdu.sdk.flightcontroller.FlightController;
import com.gdu.sdk.gimbal.Gimbal;
import com.gdu.sdk.hms.WarningLevel;
import com.gdu.sdk.manager.SDKManager;
import com.gdu.sdk.products.Aircraft;
import com.gdu.sdk.radar.Radar;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.lib.util.ThreadHelper;
import com.gdu.sdk.vision.bean.AlgorithmType;
import java.util.ArrayList;
import java.util.List;


public class FlightActivity extends FragmentActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private ActivityFlightBinding viewBinding;
//    private GDUCodecManager codecManager;
//    private VideoFeeder.VideoDataListener videoDataListener ;

     private CodecManager mCodecManager;

    private boolean showSuccess = false;
    private S220CustomSizeFocusHelper mCustomSizeFocusHelper;
    /**
     * 智能跟踪类
     */
    private TargetDetectHelper mTargetDetectHelper;

    /**
     * AI识别类
     */
    private AiDetectHelper aiDetectHelper;


    private KVObserver<Integer> mErrMsgSize;
    private KVObserver<Diagnostics> mErrRollMsg;
    private KVObserver<List<Diagnostics>> mErrMsgList;
    private MsgBoxManager msgBoxManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityFlightBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        Aircraft aircraft = (Aircraft) SDKManager.getInstance().getProduct();
        if (aircraft != null) {
            mCodecManager = aircraft.getCodecManager();
        }
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        FlightController mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        mGDUFlightController.setStateCallback(flightControllerState -> {
            //航向
            float yaw = (float) flightControllerState.getAttitude().yaw;
            float roll = (float) flightControllerState.getAttitude().roll;
            LocationCoordinate3D aircraftLocation = flightControllerState.getAircraftLocation();
            double uavLon = aircraftLocation.getLongitude();
            double uavLat = aircraftLocation.getLatitude();
            LocationCoordinate2D homeLocation = flightControllerState.getHomeLocation();
            double homeLon = homeLocation.getLongitude();
            double homeLat = homeLocation.getLatitude();
            int distance = (int) GisUtil.calculateDistance(uavLon, uavLat, homeLon, homeLat);
            runOnUiThread(()->{
                viewBinding.fpvRv.setHeadingAngle(yaw);
                viewBinding.fpvRv.setHorizontalDipAngle(roll);
                if (homeLon == 0 && homeLat == 0){
                    CycleFCInfo1 fcInfo1 = mGDUFlightController.getFcInfo1();
                    int flyDistance = 0;
                    if (fcInfo1 != null) {
                        flyDistance = fcInfo1.getFlyDistance();
                    }
                    viewBinding.fpvRv.setReturnDistance(flyDistance +"m");
                }else {
                    viewBinding.fpvRv.setReturnDistance(distance + "m");
                }
            });
        });

        Radar radar = (Radar) SdkDemoApplication.getAircraftInstance().getRadar();
        if (radar != null){
            radar.setRadarPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
                @Override
                public void onSuccess(PerceptionInformation information) {
                    List<ObstaclePoint> pointList = information.getObstaclePoints();
                    List<ObstaclePoint> showPointList = new ArrayList<>();
                    for (ObstaclePoint point : pointList) {
                        if (point.getDirection() <= 4) {
                            showPointList.add(point);
                        }

                    }
                    runOnUiThread(() -> viewBinding.fpvRv.setObstacle(showPointList,300));
                }

                @Override
                public void onFailure(Error var1) {
                }
            });
        }
        Gimbal gimbal = (Gimbal) SdkDemoApplication.getAircraftInstance().getGimbal();
        if (gimbal != null){
            gimbal.setStateCallback(state -> {
                float yaw = (float) state.getAttitudeInDegrees().yaw;
                runOnUiThread(() -> viewBinding.fpvRv.setGimbalAngle(yaw));
            });
        }

    }


    private void initView() {
        viewBinding.topStateView.setViewClickListener(new TopStateView.OnClickCallBack() {
            @Override
            public void onLeftIconClick() {
                finish();
            }

            @Override
            public void onRightSettingIconCLick() {
                SettingDialogFragment.show(getSupportFragmentManager());
            }
        });
        viewBinding.textureView.setSurfaceTextureListener(this);
//        videoDataListener = new VideoFeeder.VideoDataListener() {
//            @Override
//            public void onReceive(byte[] bytes, int size) {
//                if (null != codecManager) {
//                    codecManager.sendDataToDecoder(bytes, size);
//                }
//            }
//        };
        CycleRadarInfo radarInfo = IVision.get().getRadarInfo().getValue();
        boolean obstacleIsOpen = radarInfo != null && radarInfo.getObstacleIsOpen();
        viewBinding.fpvRv.setShowObstacleOFF(!obstacleIsOpen);
        viewBinding.fpvRv.setObstacleMax(40);
        viewBinding.ivMsgBoxLabel.setOnClickListener(this);

        SettingDao settingDao = SettingDao.getSingle();
        boolean show = settingDao.getBooleanValue(settingDao.ZORRORLabel_Grid, false);
        showNineGridShow(show);

        mCustomSizeFocusHelper = new S220CustomSizeFocusHelper(viewBinding.zoomSeekBar);

        mTargetDetectHelper = TargetDetectHelper.getInstance();
        mTargetDetectHelper.init(this, viewBinding.smartTargetContainer);
        viewBinding.smartTrackBtn.setOnClickListener(this);

        aiDetectHelper = new AiDetectHelper(this, viewBinding.aiTargetView);
        viewBinding.aiRecognizeImageview.setOnClickListener(this);
    }

    private void initData() {
        msgBoxManager = new MsgBoxManager();
        mErrMsgSize = this::onExceptionSize;
        mErrRollMsg = this::onExceptionMsg;
        mErrMsgList = diagnostics -> {
            if (null==mMsgBoxPopWin){
                msgData.size();
                msgData.addAll(diagnostics);
            }else {
                mMsgBoxPopWin.updateMsgData(diagnostics);
            }
        };
        msgBoxManager.getErrMsgSize().register(mErrMsgSize);
        msgBoxManager.getErrRollMsg().register(mErrRollMsg);
        msgBoxManager.getErrMsgList().register(mErrMsgList);
//        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
    }


    public void showNineGridShow(boolean show) {
        viewBinding.nightGridView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public void beginCheckCloud() {
        showSuccess = false;
        Gimbal mGDUGimbal = (Gimbal) ((Aircraft) SdkDemoApplication.getProductInstance()).getGimbal();
        if (mGDUGimbal == null) {
            return;
        }
        LoadingDialogUtils.createLoadDialog(this, getString(R.string.clound_checking), false);

        mGDUGimbal.setStateCallback(new GimbalState.Callback() {
            @Override
            public void onUpdate(GimbalState gimbalState) {
                runOnUiThread(() -> {
                    if (gimbalState.getCalibrationState() == 2) {
                        LoadingDialogUtils.cancelLoadingDialog();
                        if (!showSuccess) {
                            Toast.makeText(FlightActivity.this, "校飘完成", Toast.LENGTH_SHORT).show();
                            showSuccess = true;
                        }
                    } else if (gimbalState.getCalibrationState() == 3 || gimbalState.getCalibrationState() == 4) {
                        LoadingDialogUtils.cancelLoadingDialog();
                        if (!showSuccess) {
                            Toast.makeText(FlightActivity.this, "校飘失败", Toast.LENGTH_SHORT).show();
                            showSuccess = true;
                        }
                    }
                });
            }
        });
        mGDUGimbal.startCalibration(error -> {

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (codecManager != null) {
//            codecManager.onResume();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (codecManager != null) {
//            codecManager.onPause();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (codecManager != null) {
//            codecManager.onDestroy();
//        }
        if (mCustomSizeFocusHelper != null) {
            mCustomSizeFocusHelper.onDestroy();
        }
        if (null!=msgBoxManager){
            if (null!=mErrMsgSize)
                msgBoxManager.getErrMsgSize().unregister(mErrMsgSize);
            if (null!=mErrRollMsg)
                msgBoxManager.getErrRollMsg().unregister(mErrRollMsg);
            if (null!=mErrMsgList)
                msgBoxManager.getErrMsgList().unregister(mErrMsgList);
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager != null) {
            mCodecManager.startPreview(0, surface);
        }

//        if (codecManager == null) {
//            codecManager = new GDUCodecManager(FlightActivity.this, surface, width, height);
//        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mCodecManager != null) {
            mCodecManager.onSurfaceLayoutChanged(0, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.stopPreview(0);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
    private final List<Diagnostics> msgData = new ArrayList<>();
    private MsgBoxPopView mMsgBoxPopWin;

    private void showMsgBoxPopWindow(List<Diagnostics> data) {
        if (mMsgBoxPopWin == null) {
            mMsgBoxPopWin = new MsgBoxPopView(this);
        }
        final boolean isShow = viewBinding.ivMsgBoxLabel.isSelected();
        mMsgBoxPopWin.setOnDismissListener(() -> ThreadHelper.runOnUiThreadDelayed(()
                -> viewBinding.ivMsgBoxLabel.setSelected(false), 500));
        if (isShow) {
            mMsgBoxPopWin.dismiss();
        } else {
            mMsgBoxPopWin.updateMsgData(data);
            mMsgBoxPopWin.showAsDropDown(viewBinding.ivMsgBoxLabel, 0,
                    getResources().getDimensionPixelOffset(R.dimen.dp_6),
                    Gravity.BOTTOM);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_msgBoxLabel) {
            if (viewBinding.tvMsgBoxNum.getVisibility() == View.GONE) {
                return;
            }
            showMsgBoxPopWindow(msgData);
            viewBinding.ivMsgBoxLabel.setSelected(!viewBinding.ivMsgBoxLabel.isSelected());
        }else if (v.getId() == R.id.smart_track_btn) { //智能跟踪
            if (SdkDemoApplication.getAircraftInstance().getVision().getAlgorithmType() == AlgorithmType.NONE){
                SdkDemoApplication.getAircraftInstance().getVision().startSmartTrack(var1 -> {
                    Log.d("smartTrackBtn", "开启智能跟踪指令是否执行成功：" + (var1 == null));
                });
                viewBinding.smartTrackBtn.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_ef4e22_radius_3));
                viewBinding.smartTrackBtn.setTextColor(getResources().getColor(R.color.white));
            } else {
                SdkDemoApplication.getAircraftInstance().getVision().stopSmartTrack(var1 -> {
                    Log.d("smartTrackBtn", "关闭智能跟踪指令是否执行成功：" + (var1 == null));
                });
                viewBinding.smartTrackBtn.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_ffffff_radius_3));
                viewBinding.smartTrackBtn.setTextColor(getResources().getColor(R.color.black));
            }
        } else if (v.getId() == R.id.ai_recognize_imageview) {//AI识别
            if (SdkDemoApplication.getAircraftInstance().getVision().isAiDetectOpen()){
                viewBinding.aiRecognizeImageview.setActivated(false);
                aiDetectHelper.stopTargetDetect();
            } else {
                viewBinding.aiRecognizeImageview.setActivated(true);
                aiDetectHelper.startTargetDetect();
            }
        }
    }

    private void onExceptionSize(int size) {
        ThreadHelper.runOnUiThread(() -> {
            System.out.println("wuqb onExceptionSize size = " + size);
            if (size <= 0) {
                viewBinding.tvMsgBoxNum.setText(String.valueOf(0));
                viewBinding.tvMsgBoxNum.setVisibility(View.GONE);
            }else{
                viewBinding.tvMsgBoxNum.setText(String.valueOf(size));
                viewBinding.tvMsgBoxNum.setVisibility(View.VISIBLE);
            }
        });
    }
// java
    private int preMsgBoxBg = R.color.color_666666;
    private void onExceptionMsg(Diagnostics bean) {
        if (bean == null) return;
        if (StringUtils.isEmptyString(bean.getReason())) {
            ThreadHelper.runOnUiThread(() -> {
                if (viewBinding.topStateView.getStatusVisible() == View.VISIBLE) {
                    viewBinding.topStateView.setStatusVisible(View.GONE);
                }
                if (viewBinding.tvMsgBoxNum.getVisibility() == View.VISIBLE) {
                    viewBinding.tvMsgBoxNum.setVisibility(View.GONE);
                }
            });
            return;
        }

        int color = R.color.color_666666;
        WarningLevel warnLevel = bean.getHealthInformation().getWarningLevel();

        System.out.println("wuqb onExceptionMsg warnLevel = " + warnLevel + ",reason=" + bean.getReason());
        if (warnLevel == WarningLevel.WARNING) {
            color = R.color.red;
        } else if (warnLevel == WarningLevel.CAUTION || warnLevel == WarningLevel.NOTICE) {
            color = R.color.color_EAA300;
        }

        final int finalColor = color;
        final String finalWarnStr = bean.getReason();
        ThreadHelper.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (preMsgBoxBg != finalColor) {
                    viewBinding.topStateView.setStatusBackgroundTintList(ColorStateList.valueOf(ResourceUtils.getColor(finalColor)));
                    viewBinding.tvMsgBoxNum.setBackgroundTintList(ColorStateList.valueOf(ResourceUtils.getColor(finalColor)));
                    preMsgBoxBg = finalColor;
                }
                viewBinding.topStateView.setStatusVisible(View.VISIBLE);
                viewBinding.topStateView.setStatusText(finalWarnStr);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        aiDetectHelper.stopTargetDetect();
    }
}
