package com.gdu.demo.flight.setting.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.gdu.GlobalVariableTest;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingCommonBinding;
import com.gdu.demo.flight.setting.firmware.CycleFirmwareVersionManage;
import com.gdu.demo.flight.setting.firmware.ICycleGetFirmwareUpdate;
import com.gdu.demo.utils.AnimationUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.NorthPointerView;
import com.gdu.demo.widget.VersionView;
import com.gdu.drone.FirmwareType;
import com.gdu.drone.GimbalType;
import com.gdu.event.EventMessage;
import com.gdu.login.LoginType;
import com.gdu.login.UserInfoBeanNew;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.ChannelUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.GimbalUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.ThreadHelper;
import com.gdu.util.TimeUtil;
import com.gdu.util.ViewUtils;
import com.gdu.util.eventbus.GlobalEventBus;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Optional;


/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingCommonFragment extends Fragment {

    private static final String TAG = SettingCommonFragment.class.getSimpleName();
    private FragmentSettingCommonBinding mViewBinding;
    private int currentSecondLevelType = 0;

    private SettingDao mSettingDao;

    private CycleFirmwareVersionManage mCycleFirmwareVersionManage;
    private boolean viewIsAttached;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingCommonBinding.inflate(LayoutInflater.from(requireContext()));
        initViews();
        return mViewBinding.getRoot();
    }


    public void initViews() {
        mSettingDao = SettingDao.getSingle();

        ViewUtils.setViewShowOrHide(mViewBinding.testLayout, UavStaticVar.isOpenTextEnvironment);
        ViewUtils.setViewShowOrHide(mViewBinding.testVersionLayout, UavStaticVar.isOpenTextEnvironment);

        ViewUtils.setViewShowOrHide(mViewBinding.vvLockBatteryMcu,!DroneUtil.isS200Serials()
                && (GlobalVariable.armBatteryMcuOn == 1));

        if (GlobalVariable.accompanyingModel == 1) {
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

        mViewBinding.gimbalVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.gimbal_version));
        mViewBinding.vlCameraVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.vl_camera_version));
        mViewBinding.irCameraVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.ir_camera_version));
        mViewBinding.adapterRingVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.adapte_ring_version));

        mViewBinding.upgradeVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.upgrade_package_version));
        mViewBinding.svnVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.svn_version));
        mViewBinding.acVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.center_control_version));
        mViewBinding.visionVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.vision_version));
        mViewBinding.rtcmVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.rtcm_version));
        mViewBinding.mcuVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.mcu_version));
        mViewBinding.upgradeCompVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.upgrade_com_version));
        mViewBinding.itCompVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.img_version));
        mViewBinding.vvPicTransFirmwareVersion.setFirmwareName(GduAppEnv.application.getString(R.string.Label_PicTransFirmwareVersion));
        mViewBinding.setTimeVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.timer_version));
        mViewBinding.taskManagerVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.task_manager_version));
        mViewBinding.fcCoprocessorVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.fly_control_coprocessor_version));
        mViewBinding.fileTransmissionVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.file_transfer_version));
        mViewBinding.fifthGenerationVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.five_g_version));
        mViewBinding.vvObPreVision.setFirmwareName(GduAppEnv.application.getString(R.string.Label_PreVisualFwVersion));
        mViewBinding.vvObBackVision.setFirmwareName(GduAppEnv.application.getString(R.string.Label_BackVisualFwVersion));
        mViewBinding.vvAiBoxTypeAndSN.setFirmwareName(GduAppEnv.application.getString(R.string.Label_AI_Box_SN));
        mViewBinding.vvLockBatteryMcu.setFirmwareName(GduAppEnv.application.getString(R.string.label_lock_check_mcu));

        if (DroneUtil.isSmallFlight()) {
            String leftVisionLabel = GduAppEnv.application.getString(R.string.LRadarAbnormal) + GduAppEnv.application.getString(R.string.Label_BinocularFwVersion);
            mViewBinding.vvObBotVision.setFirmwareName(leftVisionLabel);
            String rightVisionLabel = GduAppEnv.application.getString(R.string.RRadarAbnormal) + GduAppEnv.application.getString(R.string.Label_BinocularFwVersion);
            mViewBinding.vvObPreRadar.setFirmwareName(rightVisionLabel);
            String topVisionLabel = GduAppEnv.application.getString(R.string.UpTOFAbnormal) + GduAppEnv.application.getString(R.string.Label_BinocularFwVersion);
            mViewBinding.vvObLeftRadar.setFirmwareName(topVisionLabel);
            String botVisionLabel = GduAppEnv.application.getString(R.string.DownTOFAbnormal) + GduAppEnv.application.getString(R.string.Label_BinocularFwVersion);
            mViewBinding.vvObRightRadar.setFirmwareName(botVisionLabel);
        } else {
            mViewBinding.vvObBotVision.setFirmwareName(GduAppEnv.application.getString(R.string.Label_BotVisualFwVersion));
            mViewBinding.vvObPreRadar.setFirmwareName(GduAppEnv.application.getString(R.string.Label_PreRadarFwVersion));
            mViewBinding.vvObLeftRadar.setFirmwareName(GduAppEnv.application.getString(R.string.Label_LeftRadarFwVersion));
            mViewBinding.vvObRightRadar.setFirmwareName(GduAppEnv.application.getString(R.string.Label_RightRadarFwVersion));
        }
        mViewBinding.vvRtkVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.Label_RtkVersion));
        mViewBinding.assistantPodVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.string_assistant_pod_version));
        mViewBinding.remoteidVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.remoteid_version));
//        mViewBinding.gnssVersionView.setFirmwareName(CommonUtils.getLabelGNSSVersionString(GduAppEnv.application));
        ViewUtils.setViewShowOrHide(mViewBinding.gnssVersionView, CommonUtils.curPlanIsSmallFlight()
                && !DroneUtil.isBDSOnlyDrone());

        mViewBinding.fcAppVersionView.setFirmwareName(getString(R.string.Label_FlyControl_AppVersion));
        mViewBinding.fcBootVersionView.setFirmwareName(getString(R.string.Label_FlyControl_BootloaderVersion));

        ViewUtils.setViewShowOrHide(mViewBinding.viewADSBGroup, GlobalVariable.ads_b_state == 1);

        final boolean isOpenADSB = SPUtils.getBoolean(requireContext(), MyConstants.IS_OPEN_ASD_B);
        mViewBinding.ivSwitchADSBBtn.setSelected(isOpenADSB);

        boolean isShowActiveBtn = false;
        String loginTypeStr = SPUtils.getString(requireContext(), MyConstants.SAVE_LOGIN_TYPE);
        if (LoginType.TYPE_PHONE.getValue().equals(loginTypeStr)) {
            final UserInfoBeanNew mLoginInfo = new Gson().fromJson(SPUtils.getString(requireContext(), MyConstants.SAVE_NEW_USER_INFO), UserInfoBeanNew.class);
            if (mLoginInfo != null && mLoginInfo.getData() != null && mLoginInfo.getData().getAdmin() != null) {
                isShowActiveBtn = mLoginInfo.getData().getAdmin();
            }
        }

        MyLogUtils.i("onResume() isShowActiveBtn = " + isShowActiveBtn);
        ViewUtils.setViewShowOrHide(mViewBinding.assistantPodVersionView,
                GlobalVariable.sAssistantPodCompId != 0);
        ViewUtils.setViewShowOrHide(mViewBinding.fcCoprocessorVersionView, !CommonUtils.curPlanIsSmallFlight());
        ViewUtils.setViewShowOrHide(mViewBinding.adapterRingVersionView, !CommonUtils.curPlanIsSmallFlight());
        ViewUtils.setViewShowOrHide(mViewBinding.fifthGenerationVersionView, !CommonUtils.curPlanIsSmallFlight());
//        getFirmwareVersion();

        boolean isOpenArmLamp = GlobalVariable.flight_arm_lamp_status == 0;
        boolean isOpenBatteryLight = GlobalVariable.battery_silence_status == 1;
        MyLogUtils.i("initView() isOpenArmLamp = " + isOpenArmLamp + "; isOpenBatteryLight = "
                + isOpenBatteryLight + "; flight_arm_lamp_status = "
                + GlobalVariable.flight_arm_lamp_status
                + "; battery_silence_status = "
                + GlobalVariable.battery_silence_status);
        final boolean showRouteHistory = SPUtils.getBoolean(requireContext(), MyConstants.SHOW_ROUTE_HISTORY);
        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(showRouteHistory);
        final boolean showImageDebugText = SPUtils.getCustomBoolean(GduAppEnv.application, MyConstants.SHOW_IMAGE_DEBUG_TEXT, true);
    }

    private void getFirmwareVersion() {
        MyLogUtils.i("getFirmwareVersion()");
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            return;
        }
        if (mCycleFirmwareVersionManage == null) {
            mCycleFirmwareVersionManage = new CycleFirmwareVersionManage();
            mCycleFirmwareVersionManage.setCycleGetVersionCallback(statusCallback);
            mCycleFirmwareVersionManage.startCycleGetFWVersion();
        }
    }

    private ICycleGetFirmwareUpdate statusCallback = new ICycleGetFirmwareUpdate() {
        @Override
        public void statusUpdate(int status) {
            MyLogUtils.i("statusUpdate() status = " + status);
            if (status == 1) {
                updateFirmwareVersion(true);
            } else if (status == 2) {
                updateFirmwareVersion(false);
            }
        }

        @Override
        public void updateVersionData() {
            MyLogUtils.i("updateVersionData()");
            updateFirmwareVersion(true);
        }
    };



    private void initMapType() {
        boolean isHideMapView = getArguments() != null && getArguments().getInt(MyConstants.IMPORT_TYPE_KEY, -1) == 2;
        ViewUtils.setViewShowOrHide(mViewBinding.tvMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.opMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.divMapType, !isHideMapView);
        // 默认0 自动
        int type = SPUtils.getInt(requireContext(), SPUtils.MAP_TYPE);
        mViewBinding.opMapModel.setIndex(type);
        MyLogUtils.d("MapType  type = " + type);

        mViewBinding.opMapModel.setOnOptionClickListener((parentId, view, position) -> {
            MyLogUtils.d("MapType  type = " + position);
            if (GlobalVariable.isOpenFlightRoutePlan) {
                Toast.makeText(requireContext(), R.string.please_exit_flight_route, Toast.LENGTH_SHORT).show();
                return;
            }
            if (GlobalVariable.fcTask == 7) {
                Toast.makeText(requireContext(), R.string.please_exit_point_fly, Toast.LENGTH_SHORT).show();
                return;
            }
//            if (RouteManager.Companion.getInstance().isRouteEdit()) {
//                Toast.makeText(requireContext(), R.string.route_edit_mode_change_map_warn, Toast.LENGTH_SHORT).show();
//                return;
//            }
            if (position == SPUtils.getInt(requireContext(), SPUtils.MAP_TYPE)) {
                return;
            }
            mViewBinding.opMapModel.setIndex(position);
            SPUtils.put(requireContext(), SPUtils.MAP_TYPE, position);
            int mapType = 0;
            // 自动 中文高德 其他语音 mapbox
            if (position == 0) {
//                if (CountryUtils.isZh(requireContext())) {
//                    mapType = 0;
//                } else {
//                    mapType = 1;
//                }
            } else if (position == 1) {
                mapType = 0;
            } else {
                mapType = 1;
            }
            GlobalEventBus.getBus().post(new EventMessage(MyConstants.SWITCH_MAP_KEY));
//            RouteManager.Companion.getInstance().switchMapType(mapType);
        });

    }

    public void initListener() {
        mViewBinding.ivBack.setOnClickListener(listener);
        mViewBinding.ivShowGrid.setOnClickListener(listener);
        mViewBinding.ivVoiceTip.setOnClickListener(listener);
        mViewBinding.ivNorthPointer.setOnClickListener(listener);
        mViewBinding.ivPoseModeSwitchBtn.setOnClickListener(listener);
        mViewBinding.ivLiveCompress.setOnClickListener(listener);
        mViewBinding.droneInfoItem.setOnClickListener(listener);
        mViewBinding.ivSwitchADSBBtn.setOnClickListener(listener);
        mViewBinding.ivShowRouteHistorySwitchBtn.setOnClickListener(listener);
        mViewBinding.tvUnit.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {
//                if (RouteManager.Companion.getInstance().isRouteEdit()) {
//                    int lastPosition = 0;
//                    if (GlobalVariable.showAsInch) {
//                        lastPosition = 1;
//                    }
//                    if (lastPosition != position) {
//                        showToast(GduAppEnv.application.getString(R.string.route_edit_mode_change_unit_warn));
//                        //mViewBinding.tvUnit.setIndex(lastPosition);
//                    }
//                    return;
//                }
                int unit = 0;
                if (position == 0) {
                    unit = SettingDao.Unit_Merch;
                    GlobalVariable.showAsInch = false;
                    mSettingDao.saveIntValue(mSettingDao.Label_Unit, unit);
//                    EventBus.getDefault().post(new ChangeUnitEvent(true));
                } else if (position == 1) {
                    unit = SettingDao.Unit_Inch;
                    GlobalVariable.showAsInch = true;
                    mSettingDao.saveIntValue(mSettingDao.Label_Unit, unit);
//                    EventBus.getDefault().post(new ChangeUnitEvent(false));
                }
                mViewBinding.tvUnit.setIndex(position);
                if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
//                    GduApplication.getSingleApp().gduCommunication.setCameraWatermaskUnit(GlobalVariable.showAsInch ? (byte) 1 : 0, (code, bean) -> {
//                        AppLog.i(TAG, "setCameraWatermaskUnit callBack() code = " + code);
//                    });
                }
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
                    mSettingDao.saveBooleanValue(mSettingDao.ZORRORLabel_Grid, !mViewBinding.ivShowGrid.isSelected());
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    mViewBinding.ivShowGrid.setSelected(!mViewBinding.ivShowGrid.isSelected());
//                    if (getActivity() instanceof ZorroRealControlActivity) {
//                        ((ZorroRealControlActivity) getActivity()).initSudoku();
//                    }
                    break;

                case R.id.iv_voice_tip:  //音效提示
                    if (mViewBinding.ivVoiceTip.isSelected()) {
                        mViewBinding.ivVoiceTip.setSelected(false);
                        SPUtils.put(requireContext(), GduConfig.VOICE, false);
                    } else {
                        mViewBinding.ivVoiceTip.setSelected(true);
                        SPUtils.put(requireContext(), GduConfig.VOICE, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.iv_northPointer:  //指北针
                    boolean selected = !mViewBinding.ivNorthPointer.isSelected();
                    mViewBinding.ivNorthPointer.setSelected(selected);
                    SPUtils.put(requireContext(), GduConfig.NORTH_POINTER, selected);
                    EventBus.getDefault().post(new NorthPointerView.EventNorthPointer(selected));
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                // 姿态模式持续语音提示
                case R.id.iv_poseModeSwitchBtn:
                    if (mViewBinding.ivPoseModeSwitchBtn.isSelected()) {
                        mViewBinding.ivPoseModeSwitchBtn.setSelected(false);
                        SPUtils.put(requireContext(), GduConfig.POSE_TIP, false);
                    } else {
                        mViewBinding.ivPoseModeSwitchBtn.setSelected(true);
                        SPUtils.put(requireContext(), GduConfig.POSE_TIP, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.iv_live_compress:
                    if (mViewBinding.ivLiveCompress.isSelected()) {
                        mViewBinding.ivLiveCompress.setSelected(false);
                        SPUtils.put(requireContext(), GduConfig.Live_Compress, false);
                    } else {
                        mViewBinding.ivLiveCompress.setSelected(true);
                        SPUtils.put(requireContext(), GduConfig.Live_Compress, true);
                    }
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;


                case R.id.drone_info_item:
                    setSecondLevelView(mViewBinding.layoutDroneInfo, true, getString(R.string.fly_info));
                    currentSecondLevelType = 1;
                    break;

                case R.id.iv_switchADSBBtn:
                    mViewBinding.ivSwitchADSBBtn.setSelected(!mViewBinding.ivSwitchADSBBtn.isSelected());
                    SPUtils.put(requireContext(), MyConstants.IS_OPEN_ASD_B, mViewBinding.ivSwitchADSBBtn.isSelected());
                    break;

                // 显示飞行轨迹
                case R.id.iv_ShowRouteHistorySwitchBtn:
                    if (mViewBinding.ivShowRouteHistorySwitchBtn.isSelected()) {
                        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(false);
                        SPUtils.put(requireContext(), MyConstants.SHOW_ROUTE_HISTORY, false);
                        GlobalVariable.showRouteHistory = false;
                    } else {
                        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(true);
                        SPUtils.put(requireContext(), MyConstants.SHOW_ROUTE_HISTORY, true);
                        GlobalVariable.showRouteHistory = true;
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
        } else if (currentSecondLevelType == 2) {
            setSecondLevelView(mViewBinding.vLightSetting, false, "");
        } else if (currentSecondLevelType == 3) {
            setSecondLevelView(mViewBinding.layoutTargetRecognition, false, "");
        }
        currentSecondLevelType = 0;
    }


    public void initShow() {
        //音效提示开关
        final boolean aBoolean = SPUtils.getTrueBoolean(requireContext(), GduConfig.VOICE);
        mViewBinding.ivVoiceTip.setSelected(aBoolean);
        // 指北针开关 默认不开启
        mViewBinding.ivNorthPointer.setSelected(SPUtils.getBoolean(requireContext(), GduConfig.NORTH_POINTER));
        // 姿态模式持续语音提示开关
        final boolean isOpenPoseTip = SPUtils.getTrueBoolean(requireContext(), GduConfig.POSE_TIP);
        mViewBinding.ivPoseModeSwitchBtn.setSelected(isOpenPoseTip);

        final boolean isCompress = SPUtils.getBoolean(requireContext(), GduConfig.Live_Compress);
        mViewBinding.ivLiveCompress.setSelected(isCompress);
//        updateFirmwareVersion(true);
        ChannelUtils.setupSn(View.GONE, mViewBinding.rlSn, mViewBinding.rlSnRC, mViewBinding.gimbalSn, mViewBinding.batterySn);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewIsAttached = true;
    }

    public void updateFirmwareVersion(boolean isGetting) {
        boolean isCanNotUpdate = !isAdded() || getContext() == null || !viewIsAttached
                || GlobalVariable.connStateEnum == ConnStateEnum.Conn_None;
        MyLogUtils.i("updateFirmwareVersion() isGetting = " + isGetting
                + "; viewIsAttached = " + viewIsAttached
                + "; connStateEnum = " + GlobalVariable.connStateEnum
                + "; isCanNotUpdate = " + isCanNotUpdate);
        if (isCanNotUpdate) {
            return;
        }
        boolean isHaveArmGimbal = GimbalUtil.haveArmMount();
        boolean isHaveArmSysGimbal = GimbalUtil.haveArmSystemMount();
        boolean isIrGimbal = GimbalUtil.haveIRGimbal();
        MyLogUtils.i("updateFirmwareVersion() isHaveArmGimbal = " + isHaveArmGimbal
                + "; isHaveArmSysGimbal = " + isHaveArmSysGimbal + "; isIrGimbal = " + isIrGimbal);
        ThreadHelper.runOnUiThread(() -> {
            ViewUtils.setViewShowOrHide(mViewBinding.rlPTZArmLayout, isHaveArmGimbal);
            ViewUtils.setViewShowOrHide(mViewBinding.rlPTZArmSystemLayout, isHaveArmSysGimbal);
            ViewUtils.setViewShowOrHide(mViewBinding.irCameraVersionView, isIrGimbal);

            ViewUtils.setViewShowOrHide(mViewBinding.rlMountAiBoxLayout,
                    GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX);

            for (FirmwareType firmwareType : FirmwareType.values()) {
                initPlaneVersion(firmwareType, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.SN)) {
                mViewBinding.tvSn.setText(GlobalVariable.SN + " ");
            } else {
                showGettingOrFailStatus2(mViewBinding.tvSn, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.sBatterySN)) {
                mViewBinding.tvBatterySn.setText(GlobalVariable.sBatterySN + " ");
            } else {
                showGettingOrFailStatus2(mViewBinding.tvBatterySn, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.sGimbalSN)) {
                mViewBinding.tvGimbalSn.setText(GlobalVariable.sGimbalSN + " ");
            } else {
                showGettingOrFailStatus2(mViewBinding.tvGimbalSn, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.SN_CODE_RN)) {
                mViewBinding.tvSnRC.setText(GlobalVariable.SN_CODE_RN + " ");
            } else {
                showGettingOrFailStatus2(mViewBinding.tvSnRC, isGetting);
            }
            ViewUtils.setViewShowOrHide(mViewBinding.rlSnRC,
                    !CommonUtils.isEmptyString(GlobalVariable.SN_CODE_RN));

            if (GlobalVariableTest.AllFlyTime == 0) {
                mViewBinding.tvTotalFlyTime.setText(GduAppEnv.application.getString(R.string.Label_TextView_NA));
            } else {
                mViewBinding.tvTotalFlyTime.setText((TimeUtil.getHourAndMinute(GlobalVariableTest.AllFlyTime * 1000 * 60)) + " ");
            }

            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
                return;
            }
            if (GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom
                    || GlobalVariable.gimbalType == GimbalType.ByrdT_35X_Zoom) {
                if (GlobalVariable.gimbelVersion != 0) {
                    mViewBinding.gimbalVersionView.setCurrentVersion("V" + GlobalVariable.gimbelVersion);
                } else {
                    showGettingOrFailStatus1(mViewBinding.gimbalVersionView, isGetting);
                }
            } else {
                if (!CommonUtils.isEmptyString(GlobalVariable.gimbalVersion)) {
                    mViewBinding.gimbalVersionView.setCurrentVersion("V" + GlobalVariable.gimbalVersion);
                } else {
                    showGettingOrFailStatus1(mViewBinding.gimbalVersionView, isGetting);
                }
            }
            if (!CommonUtils.isEmptyString(GlobalVariable.camerVersion)) {
                mViewBinding.vlCameraVersionView.setCurrentVersion("V" + GlobalVariable.camerVersion);
            } else {
                showGettingOrFailStatus1(mViewBinding.vlCameraVersionView, isGetting);
            }
            if (!CommonUtils.isEmptyString(GlobalVariable.irCameraVersion)) {
                mViewBinding.irCameraVersionView.setCurrentVersion("V" + GlobalVariable.irCameraVersion);
            } else {
                showGettingOrFailStatus1(mViewBinding.irCameraVersionView, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.sAssistantPodVersion)) {
                mViewBinding.assistantPodVersionView.setCurrentVersion("V" + GlobalVariable.sAssistantPodVersion);
            } else {
                showGettingOrFailStatus1(mViewBinding.assistantPodVersionView, isGetting);
            }

            if (!CommonUtils.isEmptyString(GlobalVariable.sRemoteIDVersion)) {
                mViewBinding.remoteidVersionView.setCurrentVersion("V" + GlobalVariable.sRemoteIDVersion);
            } else {
                showGettingOrFailStatus1(mViewBinding.remoteidVersionView, isGetting);
            }

            showObstacleVersion(isGetting);

            showAdapterRingVersion(isGetting);
            showAIBoxSN(isGetting);
        });
    }

    private void showAdapterRingVersion(boolean isGetting) {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            showGettingOrFailStatus1(mViewBinding.adapterRingVersionView, isGetting);
            return;
        }
        boolean hasAdapterRing = GlobalVariable.sCanUpgradeMount.containsKey(GduSocketConfig3.POD_ADAPTER_RING_ID);
        MyLogUtils.i("showAdapterRingVersion() hasAdapterRing = " + hasAdapterRing);
        ViewUtils.setViewShowOrHide(mViewBinding.adapterRingVersionView, hasAdapterRing);
        if (hasAdapterRing) {
//            GduApplication.getSingleApp().gduCommunication.getAdapterRingVersion((code, bean) -> uiThreadHandle(() -> {
//                if (code == GduConfig.OK && bean.frameContent != null) {
//                    String version = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
//                    mViewBinding.adapterRingVersionView.setCurrentVersion(version);
//                } else {
//                    showGettingOrFailStatus1(mViewBinding.adapterRingVersionView, isGetting);
//                }
//            }));
        }
    }

    private void showAIBoxSN(boolean isGetting) {
        MyLogUtils.i("showAIBoxSN() isGetting = " + isGetting);
        boolean haveAiBox = GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX;
        MyLogUtils.i("showAIBoxSN() haveAiBox = " + haveAiBox);
        ViewUtils.setViewShowOrHide(mViewBinding.vvAiBoxTypeAndSN, haveAiBox);
        String aiBoxSnStr = CommonUtils.getFlyVersion(requireContext(), FirmwareType.GIMBAL_AI_BOX_SN.getEnValue());
        if (CommonUtils.isEmptyString(aiBoxSnStr)) {
            showGettingOrFailStatus1(mViewBinding.vvAiBoxTypeAndSN, isGetting);
        } else {
            mViewBinding.vvAiBoxTypeAndSN.setCurrentVersion(aiBoxSnStr);
        }
        ViewUtils.setViewShowOrHide(mViewBinding.vvAiBoxTypeAndSN, haveAiBox);
    }

    private void showGettingOrFailStatus1(VersionView view, boolean isGetting) {
        if (isGetting) {
            view.setCurrentVersion(GduAppEnv.application.getString(R.string.Msg_GettingStatus));
        } else {
            view.setCurrentVersion(GduAppEnv.application.getString(R.string.Msg_GetFailStatus));
        }
    }

    private void showGettingOrFailStatus2(TextView view, boolean isGetting) {
        if (isGetting) {
            view.setText(GduAppEnv.application.getString(R.string.Msg_GettingStatus));
        } else {
            view.setText(GduAppEnv.application.getString(R.string.Msg_GetFailStatus));
        }
    }

    private void initPlaneVersion(FirmwareType firmwareType, boolean isGetting) {
        String planeFirmwareVersion;
//        String netFirmwareVersion;
        if (!planeVersionJudge(firmwareType)) {
            return;
        }
        if (firmwareType == FirmwareType.FLY_FIRMWARE) {
            planeFirmwareVersion = GlobalVariable.flyVersionStr;
        } else {
            planeFirmwareVersion = CommonUtils.getFlyVersion(requireContext(), firmwareType.getEnValue());
        }
        if (CommonUtils.isEmptyString(planeFirmwareVersion)) {
            if (isGetting) {
                planeFirmwareVersion = GduAppEnv.application.getString(R.string.Msg_GettingStatus);
            } else {
                planeFirmwareVersion = GduAppEnv.application.getString(R.string.Msg_GetFailStatus);
            }
        } else {
            if(firmwareType == FirmwareType.COMPUTE_STICK_FIRMWARE) {
                planeFirmwareVersion = "V" + showTwoPoint(planeFirmwareVersion);
            } else {
                planeFirmwareVersion = "V" + planeFirmwareVersion;
            }
        }
        // 飞机当前版本
        showCurrentVersion(firmwareType, planeFirmwareVersion);
    }

    private void showCurrentVersion(FirmwareType firmwareType, String currentVersion) {
        if (CommonUtils.isEmptyString(currentVersion)) {
            return;
        }
        switch (firmwareType) {
            case UPGRADE_PATCH_FIRMWARE:
                if (CommonUtils.curPlanIsSmallFlight()) {
                    return;
                }
                mViewBinding.upgradeVersionView.setCurrentVersion(currentVersion);
                break;
            case SVN_FIRMWARE:
                mViewBinding.svnVersionView.setCurrentVersion(currentVersion);
                break;
            case AC_FIRMWARE:
                mViewBinding.acVersionView.setCurrentVersion(currentVersion);
                break;
            case VISION_FIRMWARE:
                mViewBinding.visionVersionView.setCurrentVersion(currentVersion);
                break;
            case RTCM_FIRMWARE:
                mViewBinding.rtcmVersionView.setCurrentVersion(currentVersion);
                break;
            case MCU_FIRMWARE:
                mViewBinding.mcuVersionView.setCurrentVersion(currentVersion);
                break;
            case FLY_FIRMWARE:
                mViewBinding.tvCurrentVersionFly.setText(currentVersion);
                break;
            case UPGRADE_COMP_FIRMWARE:
                mViewBinding.upgradeCompVersionView.setCurrentVersion(currentVersion);
                break;
            case PIC_TRANSMISSION_COMPONENTS:
                mViewBinding.itCompVersionView.setCurrentVersion(currentVersion);
                break;
            case S3_SYSTEM_APPLICATION:
                mViewBinding.vvPicTransFirmwareVersion.setCurrentVersion(currentVersion);
                break;
            case IMAGE_TRANSMISSION_FIRMWARE:
                if (CommonUtils.curPlanIsSmallFlight()) {
                    mViewBinding.upgradeVersionView.setCurrentVersion(currentVersion);
                }
                break;
            case SET_TIME_FIRMWARE:
                mViewBinding.setTimeVersionView.setCurrentVersion(currentVersion);
                break;
            case TASK_MANAGER_FIRMWARE:
                mViewBinding.taskManagerVersionView.setCurrentVersion(currentVersion);
                break;
            case FC_COPROCESSOR_FIRMWARE:
                mViewBinding.fcCoprocessorVersionView.setCurrentVersion(currentVersion);
                break;
            case FILE_TRANSMISSION_FIRMWARE:
                mViewBinding.fileTransmissionVersionView.setCurrentVersion(currentVersion);
                break;
            case BATTER_FIRMWARE:
                mViewBinding.tvCurrentVersionBatter.setText(currentVersion);
                break;
            case OTA_FIRMWARE:
                mViewBinding.tvCurrentVersionOta.setText(currentVersion);
                break;
            case AP12_FIRMWARE:
                mViewBinding.tvAp12.setText(currentVersion);
                break;
            case SAGA_RCA_FIRMWARE:
                mViewBinding.tvCurrentVersionRCa.setText(currentVersion);
                break;
            case COMPUTE_STICK_FIRMWARE:
                mViewBinding.tvCurrentVersionComputeStick.setText(currentVersion);
                break;
            case FIFTH_GENERATION_FIRMWARE:
                mViewBinding.fifthGenerationVersionView.setCurrentVersion(currentVersion);
                break;
            case FC_GPS_FIRMWARE:
                mViewBinding.gnssVersionView.setCurrentVersion(currentVersion);
                break;
            case FC_APP_VERSION:
                mViewBinding.fcAppVersionView.setCurrentVersion(currentVersion);
                break;
            case FC_BOOT_VERSION:
                mViewBinding.fcBootVersionView.setCurrentVersion(currentVersion);
                break;
            case LOCK_BATTERY_CHECK_MCU_FIRMWARE:
                mViewBinding.vvLockBatteryMcu.setCurrentVersion(currentVersion);
                break;
            default:
                break;
        }
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

    private boolean planeVersionJudge(FirmwareType firmwareType) {
        if (firmwareType == FirmwareType.AP12_FIRMWARE) {
            return ConnectUtil.getConnectType() == GlobalVariable.ConnType.MGP03_RC_USB;
        }
        return true;
    }

    private void showObstacleVersion(boolean isGetting) {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            return;
        }
        final int mCurrentPlaneType = SPUtils.getInt(requireContext(), SPUtils.USER_LAST_PLANTYPE);
//        final String preVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.PRE_VISUAL, mCurrentPlaneType);
//        if (CommonUtils.isEmptyString(preVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObPreVision, isGetting);
//        } else {
//            mViewBinding.vvObPreVision.setCurrentVersion("V" + preVisualVersion);
//        }
//
//        final String backVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.BACK_VISUAL, mCurrentPlaneType);
//        if (CommonUtils.isEmptyString(backVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObBackVision, isGetting);
//        } else {
//            mViewBinding.vvObBackVision.setCurrentVersion("V" + backVisualVersion);
//        }

        if (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL_S220_IR640) {
            newResidueVersionGet(mCurrentPlaneType, isGetting);
        } else {
            oldResidueVersionGet(mCurrentPlaneType, isGetting);
        }
        if (!CommonUtils.curPlanIsSmallFlight()) {
            if (GlobalVariable.rtkVersion > 0) {
                mViewBinding.vvRtkVersionView.setCurrentVersion("V" + GlobalVariable.rtkVersion);
            } else {
                showGettingOrFailStatus1(mViewBinding.vvRtkVersionView, isGetting);
            }
        } else {
            ViewUtils.setViewShowOrHide(mViewBinding.vvRtkVersionView, GlobalVariable.RTKOnline == 0);
            if (GlobalVariable.RTKOnline != 0) {
                return;
            }
            final String rtkVerStr = CommonUtils.getFlyVersion(requireContext(), FirmwareType.RTK_980_FIRMWARE.getEnValue());
            if (CommonUtils.isEmptyString(rtkVerStr)) {
                showGettingOrFailStatus1(mViewBinding.vvRtkVersionView, isGetting);
            } else {
                mViewBinding.vvRtkVersionView.setCurrentVersion("V" + rtkVerStr);
            }
        }
    }

    private void newResidueVersionGet(int currentPlaneType, boolean isGetting) {
//        final String leftVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.LEFT_VISUAL, currentPlaneType);
//        if (CommonUtils.isEmptyString(leftVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObBotVision, isGetting);
//        } else {
//            mViewBinding.vvObBotVision.setCurrentVersion("V" + leftVisualVersion);
//        }
//
//        final String rightVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.RIGHT_VISUAL, currentPlaneType);
//        if (CommonUtils.isEmptyString(rightVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObPreRadar, isGetting);
//        } else {
//            mViewBinding.vvObPreRadar.setCurrentVersion("V" + rightVisualVersion);
//        }
//
//        final String topVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.TOP_VISUAL, currentPlaneType);
//        if (CommonUtils.isEmptyString(topVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObLeftRadar, isGetting);
//        } else {
//            mViewBinding.vvObLeftRadar.setCurrentVersion("V" + topVisualVersion);
//        }
//
//        final String botVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.BOT_VISUAL, currentPlaneType);
//        if (CommonUtils.isEmptyString(botVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObRightRadar, isGetting);
//        } else {
//            mViewBinding.vvObRightRadar.setCurrentVersion("V" + botVisualVersion);
//        }
    }

    private void oldResidueVersionGet(int currentPlaneType, boolean isGetting) {
//        final String botVisualVersion = CommonUtils.getObstacleVersion(ObstacleType.BOT_VISUAL, currentPlaneType);
//        if (CommonUtils.isEmptyString(botVisualVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObBotVision, isGetting);
//        } else {
//            mViewBinding.vvObBotVision.setCurrentVersion("V" + botVisualVersion);
//        }
//
//        final String preRadarVersion = CommonUtils.getObstacleVersion(ObstacleType.PRE_RADAR, currentPlaneType);
//        if (CommonUtils.isEmptyString(preRadarVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObPreRadar, isGetting);
//        } else {
//            mViewBinding.vvObPreRadar.setCurrentVersion("V" + preRadarVersion);
//        }
//
//        final String leftRadarVersion = CommonUtils.getObstacleVersion(ObstacleType.LEFT_RADAR, currentPlaneType);
//        if (CommonUtils.isEmptyString(leftRadarVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObLeftRadar, isGetting);
//        } else {
//            mViewBinding.vvObLeftRadar.setCurrentVersion("V" + leftRadarVersion);
//        }
//
//        final String rightRadarVersion = CommonUtils.getObstacleVersion(ObstacleType.RIGHT_RADAR, currentPlaneType);
//        if (CommonUtils.isEmptyString(rightRadarVersion)) {
//            showGettingOrFailStatus1(mViewBinding.vvObRightRadar, isGetting);
//        } else {
//            mViewBinding.vvObRightRadar.setCurrentVersion("V" + rightRadarVersion);
//        }
    }



    public static SettingCommonFragment newInstance() {
        Bundle args = new Bundle();
        SettingCommonFragment fragment = new SettingCommonFragment();
        fragment.setArguments(args);
        return fragment;
    }
}