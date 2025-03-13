package com.gdu.demo;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.gdu.AlgorithmMark;
import com.gdu.beans.WarnBean;
import com.gdu.common.error.GDUError;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.databinding.ActivityFlightBinding;
import com.gdu.demo.flight.aibox.helper.TargetDetectHelper;
import com.gdu.demo.flight.msgbox.MsgBoxBean;
import com.gdu.demo.flight.msgbox.MsgBoxManager;
import com.gdu.demo.flight.msgbox.MsgBoxPopView;
import com.gdu.demo.flight.msgbox.MsgBoxViewCallBack;
import com.gdu.demo.flight.pre.viewmodel.PreFlightInspectionViewModel;
import com.gdu.demo.flight.setting.fragment.SettingDialogFragment;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.GisUtil;
import com.gdu.demo.utils.LoadingDialogUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.utils.ToolManager;
import com.gdu.demo.viewmodel.FlightViewModel;
import com.gdu.demo.widget.TopStateView;
import com.gdu.demo.widget.zoomView.S220CustomSizeFocusHelper;
import com.gdu.drone.GimbalType;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.drone.ScreenContentType;
import com.gdu.drone.TargetMode;
import com.gdu.gimbal.GimbalState;
import com.gdu.radar.ObstaclePoint;
import com.gdu.radar.PerceptionInformation;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.gimbal.GDUGimbal;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.radar.GDURadar;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.CollectionUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.StatusBarUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.ThreadHelper;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FlightActivity extends FragmentActivity implements TextureView.SurfaceTextureListener, MsgBoxViewCallBack, View.OnClickListener {

    private ActivityFlightBinding viewBinding;
    private GDUCodecManager codecManager;
    private VideoFeeder.VideoDataListener videoDataListener ;

    private boolean showSuccess = false;
    private S220CustomSizeFocusHelper mCustomSizeFocusHelper;
    private FlightViewModel viewModel;/**
     * 目标检测类
     */
    private TargetDetectHelper mTargetDetectHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityFlightBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        viewModel = new ViewModelProvider(this).get(FlightViewModel.class);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        GDUFlightController mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        if (mGDUFlightController != null){
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
                        viewBinding.fpvRv.setReturnDistance(GlobalVariable.flyDistance +"m");
                    }else {
                        viewBinding.fpvRv.setReturnDistance(distance + "m");
                    }
                });
            });
        }

        GDURadar radar = (GDURadar) SdkDemoApplication.getAircraftInstance().getRadar();
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
                public void onFailure(GDUError var1) {
                }
            });
        }
        GDUGimbal gimbal = (GDUGimbal) SdkDemoApplication.getAircraftInstance().getGimbal();
        if (gimbal != null){
            gimbal.setStateCallback(state -> {
                float yaw = (float) state.getAttitudeInDegrees().yaw;
                runOnUiThread(() -> viewBinding.fpvRv.setGimbalAngle(yaw));
            });
        }

        viewModel.getToastLiveData().observe(this, data -> {
            if (data != 0){
                showToast(getResources().getString(data));
            }
        });
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
        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] bytes, int size) {
                if (null != codecManager) {
                    codecManager.sendDataToDecoder(bytes, size);
                }
            }
        };
        viewBinding.fpvRv.setShowObstacleOFF(!GlobalVariable.obstacleIsOpen);
        viewBinding.fpvRv.setObstacleMax(40);
        viewBinding.ivMsgBoxLabel.setOnClickListener(this);
        viewBinding.aiRecognizeImageview.setOnClickListener(this);
        ViewUtils.setViewShowOrHide(viewBinding.aiRecognizeImageview, viewModel.isShowAiBox());

        SettingDao settingDao = SettingDao.getSingle();
        boolean show = settingDao.getBooleanValue(settingDao.ZORRORLabel_Grid, false);
        showNineGridShow(show);

        mCustomSizeFocusHelper = new S220CustomSizeFocusHelper(viewBinding.zoomSeekBar);

        mTargetDetectHelper = TargetDetectHelper.getInstance();
        mTargetDetectHelper.init(this);
        mTargetDetectHelper.setOnTargetDetectListener(new TargetDetectHelper.OnTargetDetectListener() {
            @Override
            public void onTargetDetect(boolean isSuccess, List<TargetMode> targetModes) {
                LoadingDialogUtils.cancelLoadingDialog();
                //视频是主界面时，在视频上画框
                if (isSuccess && targetModes != null && !targetModes.isEmpty()) {
                    AppLog.e("TargetDetect", "onTargetDetect targetModes size = " + targetModes.size());
                    GlobalVariable.isTargetDetectMode = true;
                    mTargetDetectHelper.startShowTarget();
                    GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.DEVICE_RECOGNISE;
                    ThreadHelper.runOnUiThread(() -> Toast.makeText(FlightActivity.this, "识别到"+targetModes.size()+"个", Toast.LENGTH_SHORT).show());
                } else if (targetModes == null) {
                    AppLog.e("TargetDetect", "onTargetDetect targetModes size = 0");
                }
            }

            @Override
            public void onTargetDetectSend(boolean isSuccess) {
                MyLogUtils.d("mTargetDetectHelper onTargetDetectSend() isSuccess = " + isSuccess);
                if (isSuccess) {
                    GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.DEVICE_RECOGNISE;
                    GlobalVariable.discernIsOpen = true;
                    GlobalVariable.isTargetDetectMode = true;
                } else {
                    GlobalVariable.isTargetDetectMode = false;
                }
            }

            @Override
            public void onTargetLocateSend(boolean isSuccess) {
                MyLogUtils.d("mTargetDetectHelper onTargetLocateSend() isSuccess = " + isSuccess);
                if (isSuccess) {
//                    DialogUtils.createLoadDialog(ZorroRealControlActivity.this);
                } else {
                }
            }

            @Override
            public void onTargetLocate(boolean isSuccess, TargetMode targetMode) {
                MyLogUtils.d("mTargetDetectHelper onTargetLocate() isSuccess = " + isSuccess);
//                DialogUtils.cancelLoadDialog();
            }

            @Override
            public void onDetectClosed() {
                //收到关闭目标识别成功回调后再次重置状态，防止部分极端场景本地重置状态到发送关闭中间时间段又收到周期回调，将状态还原导致无法退出的问题
            }
        });
    }


    private void initData() {
        new MsgBoxManager(this, 1,this);
        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
    }


    public void showNineGridShow(boolean show) {
        viewBinding.nightGridView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public void beginCheckCloud() {
        showSuccess = false;
        GDUGimbal mGDUGimbal = (GDUGimbal) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getGimbal();
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
        if (codecManager != null) {
            codecManager.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (codecManager != null) {
            codecManager.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (codecManager != null) {
            codecManager.onDestroy();
        }
        if (mCustomSizeFocusHelper != null) {
            mCustomSizeFocusHelper.onDestroy();
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new GDUCodecManager(FlightActivity.this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
    private final List<MsgBoxBean> msgData = new ArrayList<>();
    private MsgBoxPopView mMsgBoxPopWin;

    private void showMsgBoxPopWindow(List<MsgBoxBean> data) {
        if (mMsgBoxPopWin == null) {
            mMsgBoxPopWin = new MsgBoxPopView(this, viewBinding.ivMsgBoxLabel);
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
            if (msgData.isEmpty() || StringUtils.isEmptyString(viewBinding.tvMsgBoxNum.getText().toString())
                    || Integer.parseInt(viewBinding.tvMsgBoxNum.getText().toString().trim()) == 0) {
                return;
            }
            showMsgBoxPopWindow(msgData);
            viewBinding.ivMsgBoxLabel.setSelected(!viewBinding.ivMsgBoxLabel.isSelected());
        }else if (v.getId() == R.id.ai_recognize_imageview){
            viewModel.switchAIRecognize();
        }
    }

    @Override
    public void updateTitleTvTxt(String title) {
        ThreadHelper.runOnUiThread(() -> viewBinding.topStateView.setStatusText(title));
    }

    @Override
    public void updateTitleTVColor(int txtColor) {
        ThreadHelper.runOnUiThread(() -> viewBinding.topStateView.setStatusTextColor(txtColor));
    }

    @Override
    public void updateHeadViewBg(int resId) {
        ThreadHelper.runOnUiThread(() -> {
            if (resId != 0) {
                viewBinding.topStateView.setStatusTextBackground(resId);
            }
        });
    }

    @Override
    public void updateWarnList(HashMap<Long, WarnBean> warnList) {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess || warnList.isEmpty()) {
            msgData.clear();
            ThreadHelper.runOnUiThread(() -> {
                viewBinding.tvMsgBoxNum.setText("0");
                viewBinding.tvMsgBoxNum.setVisibility(View.GONE);
            });
            return;
        }
        msgData.clear();
        for (Map.Entry<Long, WarnBean> warnBeanEntry : warnList.entrySet()) {
            if (!warnBeanEntry.getValue().isErr) {
                continue;
            }
            final MsgBoxBean bean = new MsgBoxBean();
            bean.setMsgContent(warnBeanEntry.getValue().warnStr);
            bean.setWarnLevel(warnBeanEntry.getValue().getWarnLevel());
            CollectionUtils.listAddAvoidNull(msgData, bean);
        }
        ThreadHelper.runOnUiThread(() -> {
            if (!CollectionUtils.isEmptyList(msgData)) {
                viewBinding.tvMsgBoxNum.setText(String.valueOf(msgData.size()));
                viewBinding.tvMsgBoxNum.setVisibility(View.VISIBLE);
            } else {
                viewBinding.tvMsgBoxNum.setText("0");
                viewBinding.tvMsgBoxNum.setVisibility(View.GONE);

            }
        });
    }

    public void showToast(String str) {
        ThreadHelper.runOnUiThread(() -> Toast.makeText(this, str, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.stopTarget((byte) 0x02, GlobalVariable.mCurrentLightType);
    }
}
