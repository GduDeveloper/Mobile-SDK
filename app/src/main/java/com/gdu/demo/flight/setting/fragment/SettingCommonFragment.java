package com.gdu.demo.flight.setting.fragment;

import android.animation.ObjectAnimator;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.gdu.common.error.Error;
import com.gdu.config.GduConfig;
import com.gdu.demo.FlightActivity;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.FragmentSettingCommonBinding;
import com.gdu.demo.flight.setting.adapter.TargetDetectModelAdapter;
import com.gdu.demo.flight.setting.bean.GetAiModel;
import com.gdu.demo.flight.setting.bean.GetAiModelResponse;
import com.gdu.demo.flight.setting.bean.TargetDetectLabel;
import com.gdu.demo.flight.setting.bean.TargetDetectModel;
import com.gdu.demo.utils.AnimationUtils;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.NorthPointerView;
import com.gdu.lib.base.GduEnvConfig;
import com.gdu.lib.util.StringUtils;
import com.gdu.lib.util.TimeUtil;
import com.gdu.lib.util.ViewUtils;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.lib.util.core.SPUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IBattery;
import com.gdu.msdk.device.component.interfaces.IRTK;
import com.gdu.msdk.device.component.interfaces.IVersion;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.lib.util.ThreadHelper;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingCommonFragment extends Fragment {

    /** 是否开启ADS_B. */
    public static final String IS_OPEN_ASD_B = "isOpenADS_B";
    /** 显示飞行轨迹key */
    public static final String SHOW_ROUTE_HISTORY = "ShowRouteHistory";
    /** 记录DeviceFragment进入拍摄界面类型的key. */
    public static final String IMPORT_TYPE_KEY = "importType";
    /** 地图类型 */
    public static final String MAP_TYPE = "mapType";

    private static final String TAG = SettingCommonFragment.class.getSimpleName();
    private FragmentSettingCommonBinding mViewBinding;
    private int currentSecondLevelType = 0;
    private SettingDao mSettingDao;
    private Handler handler;
    private ObjectAnimator objectAnimator;
    private TargetDetectModelAdapter modelAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingCommonBinding.inflate(LayoutInflater.from(requireContext()));
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        handler = new Handler();
        mSettingDao = SettingDao.getSingle();

        initListener();

        boolean accompanyingModel = false;
        if (IRTK.get().getFcCoprocessorRtk() != null) {
            accompanyingModel = IRTK.get().getFcCoprocessorRtk().getAccompanyingModel() == 1;
        }
        if (accompanyingModel) {
            mViewBinding.tvAircraftMode.setText(R.string.string_car_model);
        } else {
            mViewBinding.tvAircraftMode.setText(R.string.ordinary_mode);
        }
        initShow();
        initMapType();

        mViewBinding.ivShowGrid.setSelected(mSettingDao.getBooleanValue(mSettingDao.ZORRORLabel_Grid, true));

        int intUnitValue = mSettingDao.getIntValue(mSettingDao.Label_Unit, -1);
        if (intUnitValue == SettingDao.Unit_Merch) {
            mViewBinding.tvUnit.setIndex(0);
        } else if (intUnitValue == SettingDao.Unit_Inch) {
            mViewBinding.tvUnit.setIndex(1);
        }

        mViewBinding.gimbalVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.gimbal_version));
        mViewBinding.vlCameraVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.vl_camera_version));
        mViewBinding.irCameraVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.ir_camera_version));

        mViewBinding.upgradeVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.upgrade_package_version));
        mViewBinding.svnVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.svn_version));
        mViewBinding.acVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.center_control_version));
        mViewBinding.visionVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.vision_version));
        mViewBinding.rtcmVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.rtcm_version));
        mViewBinding.itCompVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.img_version));
        mViewBinding.vvPicTransFirmwareVersion.setFirmwareName(GduEnvConfig.application.getString(R.string.Label_PicTransFirmwareVersion));
        mViewBinding.fcCoprocessorVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.fly_control_coprocessor_version));
        mViewBinding.fifthGenerationVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.five_g_version));

        ViewUtils.setViewShowOrHide(mViewBinding.vvRtkVersionView, DroneUtils.getRtkOnline());
        mViewBinding.vvRtkVersionView.setFirmwareName(GduEnvConfig.application.getString(R.string.Label_RtkVersion));
        ViewUtils.setViewShowOrHide(mViewBinding.viewADSBGroup, DroneUtils.getFcInfo1().getAdsBOnline());

        final boolean isOpenADSB = SPUtils.getInstance().getBoolean(IS_OPEN_ASD_B);
        mViewBinding.ivSwitchADSBBtn.setSelected(isOpenADSB);

        boolean isShowActiveBtn = false;
//        String loginTypeStr = SPUtils.getInstance().getString(MyConstants.SAVE_LOGIN_TYPE);
//        if (LoginType.TYPE_PHONE.getValue().equals(loginTypeStr)) {
//            final UserInfoBeanNew mLoginInfo = new Gson().fromJson(SPUtils.getString(requireContext(), MyConstants.SAVE_NEW_USER_INFO), UserInfoBeanNew.class);
//            if (mLoginInfo != null && mLoginInfo.getData() != null && mLoginInfo.getData().getAdmin() != null) {
//                isShowActiveBtn = mLoginInfo.getData().getAdmin();
//            }
//        }

        XLogger.INSTANCE.getAPP().i("onResume() isShowActiveBtn = " + isShowActiveBtn);
        ViewUtils.setViewShowOrHide(mViewBinding.fcCoprocessorVersionView, !IGduDroneDevice.get().getPlanType().getValue().isS200Type());
        ViewUtils.setViewShowOrHide(mViewBinding.fifthGenerationVersionView, !IGduDroneDevice.get().getPlanType().getValue().isS200Type());

        int flightArmLampStatus = 0;
        if (IRTK.get().getFcCoprocessorRtk() != null) {
            flightArmLampStatus = IRTK.get().getFcCoprocessorRtk().getFlightArmLampStatus();
        }
        int batterySilenceStatus = 0;
        if (IBattery.get().getDroneBatteryInfo().getValue() != null) {
            batterySilenceStatus = IBattery.get().getDroneBatteryInfo().getValue().getSilenceStatus();
        }
        boolean isOpenArmLamp = flightArmLampStatus == 0;
        boolean isOpenBatteryLight = batterySilenceStatus == 1;
        XLogger.INSTANCE.getAPP().i("initView() isOpenArmLamp = " + isOpenArmLamp + "; isOpenBatteryLight = "
                + isOpenBatteryLight + "; flightArmLampStatus = " + flightArmLampStatus
                + "; battery_silence_status = " + batterySilenceStatus);
        final boolean showRouteHistory = SPUtils.getInstance().getBoolean(SHOW_ROUTE_HISTORY);
        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(showRouteHistory);
    }


    public void initData() {
        if (SdkDemoApplication.getAircraftInstance() != null) {
            SdkDemoApplication.getAircraftInstance().getProductSN(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String sn) {
                    if (handler != null && isAdded()) {
                        handler.post(() -> {
                            mViewBinding.tvSn.setText(sn);
                        });
                    }
                }

                @Override
                public void onFailure(Error var1) {
                }
            });
        }
        IVersion version = IVersion.get();
        String flyVersionStr = version.getFcVer().getValue() == null? "": version.getFcVer().getValue();
        mViewBinding.tvCurrentVersionFly.setText(flyVersionStr);
        String batteryVersion = version.getBatteryVersion().getValue() == null? "": version.getBatteryVersion().getValue();
        mViewBinding.tvCurrentVersionBatter.setText(batteryVersion);

        if (IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            String rtkVersion = version.getRtkVersionNew().getValue() == null? "": version.getRtkVersionNew().getValue();
            mViewBinding.vvRtkVersionView.setCurrentVersion(rtkVersion);
        } else {
            mViewBinding.vvRtkVersionView.setCurrentVersion(version.getRtkVersion());
        }

        final String onboardRTKVersion = version.getOnboardRTKVersion().getValue() == null? "": version.getOnboardRTKVersion().getValue();
        mViewBinding.rtcmVersionView.setCurrentVersion(onboardRTKVersion);


        String otaVersion = version.getSystemVersion().getValue() == null? "": version.getSystemVersion().getValue();
        mViewBinding.tvCurrentVersionOta.setText(otaVersion);

        if (!IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            String otaAppVersion = version.getSystemAppVersion().getValue() == null? "": version.getSystemAppVersion().getValue();
            mViewBinding.upgradeVersionView.setCurrentVersion(otaAppVersion);
        }

        if (IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            final String itVersion = version.getDroneSdrAppVersion().getValue() == null? "": version.getDroneSdrAppVersion().getValue();
            mViewBinding.upgradeVersionView.setCurrentVersion(itVersion);
        }

        String itVersion = version.getDroneSdrVersion().getValue() == null? "": version.getDroneSdrVersion().getValue();
        mViewBinding.itCompVersionView.setCurrentVersion(itVersion);

        String sdrSysVersion = version.getDroneSdrSysVersion().getValue() == null? "": version.getDroneSdrSysVersion().getValue();
        mViewBinding.vvPicTransFirmwareVersion.setCurrentVersion(sdrSysVersion);

        final String gimbalVersion = version.getGimbalVersion().getValue() == null? "": version.getGimbalVersion().getValue();
        mViewBinding.gimbalVersionView.setCurrentVersion(gimbalVersion);

        final String vlCameraVersion = version.getVlCameraVersion().getValue() == null? "": version.getVlCameraVersion().getValue();
        mViewBinding.vlCameraVersionView.setCurrentVersion(vlCameraVersion);

        final String irCameraVersion = version.getIrCameraVersion().getValue() == null? "": version.getIrCameraVersion().getValue();
        mViewBinding.irCameraVersionView.setCurrentVersion(irCameraVersion);

        String acVersion = version.getAcVersion().getValue() == null? "": version.getAcVersion().getValue();
        mViewBinding.acVersionView.setCurrentVersion(acVersion);

        final String rcVersion = version.getRcVersion().getValue() == null? "": version.getRcVersion().getValue();
        mViewBinding.tvCurrentVersionRCa.setText(rcVersion);

        final String visionVersion = version.getFlightAssistantVersion().getValue() == null? "": version.getFlightAssistantVersion().getValue();
        mViewBinding.visionVersionView.setCurrentVersion(visionVersion);

        String rcSdrVersion = version.getRcSdrVersion().getValue() == null? "": version.getRcSdrVersion().getValue();
        mViewBinding.tvAp12.setText(rcSdrVersion);

        if (SdkDemoApplication.getAircraftInstance() != null && SdkDemoApplication.getAircraftInstance().getRemoteController() != null) {
            String rcSn = SdkDemoApplication.getAircraftInstance().getRemoteController().getRCSN();
            mViewBinding.tvSnRC.setText(rcSn);
        }
        long allFlyTime = 0L;
        try {
            allFlyTime = DroneUtils.getFcInfo1().getTotalFlyTimeInMin();
        } catch (Exception ignore) {
        }
        if (allFlyTime == 0) {
            mViewBinding.tvTotalFlyTime.setText(GduEnvConfig.application.getString(R.string.Label_TextView_NA));
        } else {
            mViewBinding.tvTotalFlyTime.setText((TimeUtil.getHourAndMinute(allFlyTime * 1000 * 60)) + " ");
        }

    }
    private void initMapType() {
        boolean isHideMapView = getArguments() != null && getArguments().getInt(IMPORT_TYPE_KEY, -1) == 2;
        ViewUtils.setViewShowOrHide(mViewBinding.tvMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.opMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.divMapType, !isHideMapView);
        // 默认0 自动
        int type = SPUtils.getInstance().getInt(MAP_TYPE);
        mViewBinding.opMapModel.setIndex(type);
        XLogger.INSTANCE.getAPP().i("MapType  type = " + type);

        mViewBinding.opMapModel.setOnOptionClickListener((parentId, view, position) -> {
            XLogger.INSTANCE.getAPP().i("MapType  type = " + position);
            if (DroneUtils.isOpenFlightRoutePlan()) {
                Toast.makeText(requireContext(), R.string.please_exit_flight_route, Toast.LENGTH_SHORT).show();
                return;
            }
            if (DroneUtils.getFcInfo2().getFcTask() == 7) {
                Toast.makeText(requireContext(), R.string.please_exit_point_fly, Toast.LENGTH_SHORT).show();
                return;
            }
            if (position == SPUtils.getInstance().getInt(MAP_TYPE)) {
                return;
            }
            mViewBinding.opMapModel.setIndex(position);
            SPUtils.getInstance().put(MAP_TYPE, position);
            // 切换地图
        });

    }

    public void initListener() {
        mViewBinding.ivBack.setOnClickListener(listener);
        mViewBinding.ivShowGrid.setOnClickListener(listener);
        mViewBinding.ivVoiceTip.setOnClickListener(listener);
        mViewBinding.ivNorthPointer.setOnClickListener(listener);
        mViewBinding.ivPoseModeSwitchBtn.setOnClickListener(listener);
        mViewBinding.droneInfoItem.setOnClickListener(listener);
        mViewBinding.ivSwitchADSBBtn.setOnClickListener(listener);
        mViewBinding.targetRecognitionItem.setOnClickListener(listener);
        mViewBinding.ivShowRouteHistorySwitchBtn.setOnClickListener(listener);
        mViewBinding.tvUnit.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {
                int unit ;
                if (position == 0) {
                    unit = SettingDao.Unit_Merch;
                    mSettingDao.saveIntValue(mSettingDao.Label_Unit, unit);
                } else if (position == 1) {
                    unit = SettingDao.Unit_Inch;
                    mSettingDao.saveIntValue(mSettingDao.Label_Unit, unit);
                }
                mViewBinding.tvUnit.setIndex(position);
            }
        });
    }


    public View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_back:
                    updateBackView();
                    break;
                case R.id.iv_show_grid:
                    boolean show = !mViewBinding.ivShowGrid.isSelected();
                    mSettingDao.saveBooleanValue(mSettingDao.ZORRORLabel_Grid, show);
                    mViewBinding.ivShowGrid.setSelected(show);
                    if (getActivity() instanceof FlightActivity) {
                        ((FlightActivity) getActivity()).showNineGridShow(show);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.iv_voice_tip:  //音效提示
                    if (mViewBinding.ivVoiceTip.isSelected()) {
                        mViewBinding.ivVoiceTip.setSelected(false);
                        SPUtils.getInstance().put(GduConfig.VOICE, false);
                    } else {
                        mViewBinding.ivVoiceTip.setSelected(true);
                        SPUtils.getInstance().put(GduConfig.VOICE, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.iv_northPointer:  //指北针
                    boolean selected = !mViewBinding.ivNorthPointer.isSelected();
                    mViewBinding.ivNorthPointer.setSelected(selected);
                    SPUtils.getInstance().put(GduConfig.NORTH_POINTER, selected);
                    EventBus.getDefault().post(new NorthPointerView.EventNorthPointer(selected));
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                // 姿态模式持续语音提示
                case R.id.iv_poseModeSwitchBtn:
                    if (mViewBinding.ivPoseModeSwitchBtn.isSelected()) {
                        mViewBinding.ivPoseModeSwitchBtn.setSelected(false);
                        SPUtils.getInstance().put(GduConfig.POSE_TIP, false);
                    } else {
                        mViewBinding.ivPoseModeSwitchBtn.setSelected(true);
                        SPUtils.getInstance().put(GduConfig.POSE_TIP, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.target_recognition_item:
                    initTargetDetectView();
                    setSecondLevelView(mViewBinding.layoutTargetRecognition, true, GduEnvConfig.application.getString(R.string.Label_Visition_Target_Detect));
                    currentSecondLevelType = 3;
                    break;
                case R.id.drone_info_item:
                    setSecondLevelView(mViewBinding.layoutDroneInfo, true, getString(R.string.fly_info));
                    currentSecondLevelType = 1;
                    break;

                case R.id.iv_switchADSBBtn:
                    mViewBinding.ivSwitchADSBBtn.setSelected(!mViewBinding.ivSwitchADSBBtn.isSelected());
                    SPUtils.getInstance().put(IS_OPEN_ASD_B, mViewBinding.ivSwitchADSBBtn.isSelected());
                    break;

                // 显示飞行轨迹
                case R.id.iv_ShowRouteHistorySwitchBtn:
                    if (mViewBinding.ivShowRouteHistorySwitchBtn.isSelected()) {
                        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(false);
                        SPUtils.getInstance().put(SHOW_ROUTE_HISTORY, false);
                    } else {
                        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(true);
                        SPUtils.getInstance().put(SHOW_ROUTE_HISTORY, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };


    private void setSecondLevelView(View view, boolean show, String title) {

        Log.d("setSecondLevelView","setSecondLevelView  show = " + show + ", title = " + title);
        AnimationUtils.animatorRightInOut(view, show);
        ViewUtils.setViewShowOrHide(mViewBinding.ivBack, show);
        if (show) {
            mViewBinding.tvTitle.setText(title);
        } else {
            mViewBinding.tvTitle.setText(R.string.title_common);
        }
    }

    private void updateBackView() {
        if (currentSecondLevelType == 1) {
            setSecondLevelView(mViewBinding.layoutDroneInfo, false, "");
        }
        currentSecondLevelType = 0;
    }


    public void initShow() {
        //音效提示开关
        final boolean aBoolean = SPUtils.getInstance().getBoolean(GduConfig.VOICE, true);
        mViewBinding.ivVoiceTip.setSelected(aBoolean);
        // 指北针开关 默认不开启
        mViewBinding.ivNorthPointer.setSelected(SPUtils.getInstance().getBoolean(GduConfig.NORTH_POINTER));
        // 姿态模式持续语音提示开关
        final boolean isOpenPoseTip = SPUtils.getInstance().getBoolean(GduConfig.POSE_TIP, true);
        mViewBinding.ivPoseModeSwitchBtn.setSelected(isOpenPoseTip);

        final boolean isCompress = SPUtils.getInstance().getBoolean(GduConfig.Live_Compress);
//        ChannelUtils.setupSn(View.GONE, mViewBinding.rlSn, mViewBinding.rlSnRC, mViewBinding.gimbalSn, mViewBinding.batterySn);
    }

    private void initTargetDetectView() {
        initTargetDetectType();

        if (DroneUtils.getAiBoxOnline()) {
            mViewBinding.clBoxModels.setVisibility(View.VISIBLE);
            mViewBinding.tvAiBox.setVisibility(View.VISIBLE);
            mViewBinding.line13.setVisibility(View.VISIBLE);
            objectAnimator = ObjectAnimator.ofFloat(mViewBinding.ivLoading, "rotation", 0f, 360f);
            objectAnimator.setDuration(1000);
            objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            objectAnimator.start();

            modelAdapter = new TargetDetectModelAdapter(data -> {
                if (data != null) {
                    byte[] typeArray = new byte[data.getLabels().size()];
                    boolean hasChecked = false;
                    for (int i = 0; i < data.getLabels().size(); i++) {
                        if (data.getLabels().get(i).isChecked()) {
                            typeArray[i] = 0x01;
                            hasChecked = true;
                        } else {
                            typeArray[i] = 0x00;
                        }
                    }
                    byte detectType = 0x00;
                    if (hasChecked) detectType = 0x01;
//                    SdkDemoApplication.getAircraftInstance().getGduVision().setAIBoxTargetType(data.getId(), detectType, (short) data.getLabels().size(), typeArray,
//                            error -> XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "setAIBoxTargetType callBack() code = " + error));
                }
            });
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
            ShapeDrawable dividerDrawable = new ShapeDrawable();
            dividerDrawable.getPaint().setColor(getResources().getColor(R.color.color_D8D8D8, null));
            dividerDrawable.setIntrinsicHeight(1);
            dividerItemDecoration.setDrawable(dividerDrawable);
            mViewBinding.rvModels.addItemDecoration(dividerItemDecoration);
            mViewBinding.rvModels.setAdapter(modelAdapter);
            getTargetDetectModels();
        } else {
            mViewBinding.clBoxModels.setVisibility(View.GONE);
            mViewBinding.tvAiBox.setVisibility(View.GONE);
            mViewBinding.line13.setVisibility(View.GONE);
        }
    }

    private void initTargetDetectType() {
//        if (GlobalVariable.aiRecognitionSwitch.second != null && GlobalVariable.aiRecognitionSwitch.second.length == 3) {
//            mViewBinding.cbPerson.setChecked(GlobalVariable.aiRecognitionSwitch.second[0] == 0x01);
//            mViewBinding.cbCar.setChecked(GlobalVariable.aiRecognitionSwitch.second[1] == 0x01);
//            mViewBinding.cbShip.setChecked(GlobalVariable.aiRecognitionSwitch.second[2] == 0x01);
//        }
        mViewBinding.cbPerson.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
        mViewBinding.cbCar.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
        mViewBinding.cbShip.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
    }

    private void resetAiRecognitionSwitch() {
//        if (GlobalVariable.aiRecognitionSwitch.second != null && GlobalVariable.aiRecognitionSwitch.second.length == 3) {
//            mViewBinding.cbPerson.setChecked(GlobalVariable.aiRecognitionSwitch.second[0] == 0x01);
//            mViewBinding.cbCar.setChecked(GlobalVariable.aiRecognitionSwitch.second[1] == 0x01);
//            mViewBinding.cbShip.setChecked(GlobalVariable.aiRecognitionSwitch.second[2] == 0x01);
//        } else {
//            mViewBinding.cbPerson.setChecked(false);
//            mViewBinding.cbCar.setChecked(false);
//            mViewBinding.cbShip.setChecked(false);
//        }
    }

    private void setTargetType() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            resetAiRecognitionSwitch();
            return;
        }
        if (!DroneUtils.isTargetDetectMode()) {
            Toast.makeText(requireContext(), R.string.Msg_AI_Recoginition_Warn, Toast.LENGTH_SHORT).show();
            resetAiRecognitionSwitch();
            return;
        }
//        if (GlobalVariable.aiRecognitionSwitch.first == 0x0C) {
//            SdkDemoApplication.getAircraftInstance().getGduVision().setTargetType((byte) 0x01, (byte) 0x01, (short) 3, getCheckedState(), gduError -> {
//                if (gduError == null){
//                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            SdkDemoApplication.getAircraftInstance().getGduVision().setAITargetType((byte) 0x00, (byte) 0x01, (short) 3, getCheckedState(), gduError -> {
//                if (gduError == null){
//                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }

    private byte[] getCheckedState() {
        byte[] checkedArray = new byte[3];
        if (mViewBinding.cbPerson.isChecked()) {
            checkedArray[0] = 0x01;
        }
        if (mViewBinding.cbCar.isChecked()) {
            checkedArray[1] = 0x01;
        }
        if (mViewBinding.cbShip.isChecked()) {
            checkedArray[2] = 0x01;
        }
        return checkedArray;
    }

    private void getTargetDetectModels() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            cancelLoadingAnimator();
            return;
        }
//        SdkDemoApplication.getAircraftInstance().getGduVision().setOnTargetDetectModelsListener(sJson -> {
//            if (StringUtils.isEmptyString(sJson)) return;
//            GetAiModelResponse response = new Gson().fromJson(sJson, GetAiModelResponse.class);
//            ThreadHelper.runOnUiThread(() -> {
//                cancelLoadingAnimator();
//                transModelData(response.getModels());
//            });
//        });
//        SdkDemoApplication.getAircraftInstance().getGduVision().getTargetDetectModels(gduError -> XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "getTargetDetectModels callBack() code = " + gduError));
    }

    private void cancelLoadingAnimator() {
        if (objectAnimator != null) {
            objectAnimator.cancel();
            objectAnimator = null;
        }
        if (mViewBinding != null) {
            mViewBinding.llLoading.setVisibility(View.GONE);
        }
    }

    private void transModelData(List<GetAiModel> data) {
        if (data == null) return;
        ArrayList<TargetDetectModel> models = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            GetAiModel aiModel = data.get(i);
            ArrayList<TargetDetectLabel> labels = new ArrayList<>();
            if (aiModel.getLabels() == null) continue;

            for (int j = 0; j < aiModel.getLabels().size(); j++) {
                if (aiModel.getFlag() == 1) { // 自研模型
                    int labelId = -1;
                    try {
                        Object labelIdStr = aiModel.getLabels().get(j);
                        if (labelIdStr instanceof String) {
                            labelId = Integer.parseInt((String) labelIdStr);
                        } else {
                            labelId = (int) (double) labelIdStr;
                        }
                    } catch (Exception e) {
                        XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "transModelData " + aiModel.getLabels().get(j));
                    }
                    String labelName = "";
                    // todo fuchi aibox待迁移
//                    TargetLabel targetLabel = TargetLabel.get(labelId);
//                    if (targetLabel != null) {
//                        labelName = ResourceUtils.getString(targetLabel.getValue());
//                    }
                    XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "transModelData labelId = " + labelId + ", labelName = " + labelName);
                    labels.add(new TargetDetectLabel(j, String.valueOf(labelId), labelName, getDetectLabelState(aiModel.getId(), j)));
                } else { // 自定义模型
                    Object labelObject = aiModel.getLabels().get(j);
                    if (labelObject instanceof LinkedTreeMap<?, ?>) {
                        String type = (String) ((LinkedTreeMap<?, ?>) labelObject).get("type");
                        String name = "";
                        Object nameObject = ((LinkedTreeMap<?, ?>) labelObject).get("extend");
                        if (nameObject instanceof LinkedTreeMap<?, ?>) {
                            Locale local = Locale.getDefault();
                            String language = local.getLanguage();
                            if (language.equals("zh")) {
                                name = (String) ((LinkedTreeMap<?, ?>) nameObject).get("cnName");
                            }
                            if (name == null || name.equals(""))
                                name = (String) ((LinkedTreeMap<?, ?>) nameObject).get("enName");
                        }
                        if (name == null || name.equals("")) name = type;
                        if (type != null) {
                            labels.add(new TargetDetectLabel(j, type, name, getDetectLabelState(aiModel.getId(), j)));
                        }
                    }
                }
            }
            TargetDetectModel model = new TargetDetectModel(aiModel.getId(), labels);
            models.add(model);
        }
        modelAdapter.setNewInstance(models);
    }

    private boolean getDetectLabelState(int modelId, int index) {
//        if (GlobalVariable.targetDetectModelState != null && !GlobalVariable.targetDetectModelState.isEmpty()) {
//            for (int i = 0; i < GlobalVariable.targetDetectModelState.size(); i++) {
//                AIModelState model = GlobalVariable.targetDetectModelState.get(i);
//                if (model.getModelId() == modelId) {
//                    byte state = model.getLabelState()[index];
//                    return state == 0x01;
//                }
//            }
//        }
        return false;
    }

    private String showTwoPoint(String number) {
        String[] tempS = number.split("\\.");
        if (tempS.length == 1) {
            return number + ".00";
        }
        char[] tempC = tempS[1].toCharArray();
        int resultNum = tempC.length;
        if (resultNum == 1) {
            return number + "0";
        } else if (resultNum == 2) {
            return number;
        }
        return number;
    }


    public static SettingCommonFragment newInstance() {
        Bundle args = new Bundle();
        SettingCommonFragment fragment = new SettingCommonFragment();
        fragment.setArguments(args);
        return fragment;
    }
}