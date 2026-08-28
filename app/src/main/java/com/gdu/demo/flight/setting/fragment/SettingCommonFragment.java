package com.gdu.demo.flight.setting.fragment;

import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.gdu.demo.flight.aibox.helper.DetectModelDBHelper;
import com.gdu.demo.flight.setting.adapter.TargetDetectModelAdapter;
import com.gdu.demo.flight.setting.bean.TargetDetectLabel;
import com.gdu.demo.flight.setting.bean.TargetDetectModel;
import com.gdu.demo.flight.setting.bean.TargetLabel;
import com.gdu.demo.utils.AnimationUtils;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.utils.SystemUtils;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.NorthPointerView;
import com.gdu.lib.base.GduEnvConfig;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.TimeUtil;
import com.gdu.lib.util.ViewUtils;
import com.gdu.lib.util.core.GsonUtils;
import com.gdu.lib.util.core.SPUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IBattery;
import com.gdu.msdk.device.component.interfaces.IRTK;
import com.gdu.msdk.device.component.interfaces.IVersion;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.AIModelState;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.lib.util.ThreadHelper;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
        if (DroneUtils.getFcInfo1() != null) {
            ViewUtils.setViewShowOrHide(mViewBinding.viewADSBGroup, DroneUtils.getFcInfo1().getAdsBOnline());
        }

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
        if (currentSecondLevelType == 1 || currentSecondLevelType == 3) {
            setSecondLevelView(mViewBinding.layoutDroneInfo, false, "");
            setSecondLevelView(mViewBinding.layoutTargetRecognition, false, "");
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
        mViewBinding.clBoxModels.setVisibility(View.VISIBLE);
        mViewBinding.tvAiBox.setVisibility(View.VISIBLE);
        mViewBinding.line13.setVisibility(View.VISIBLE);
        objectAnimator = ObjectAnimator.ofFloat(mViewBinding.ivLoading, "rotation", 0f, 360f);
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.start();

        modelAdapter = new TargetDetectModelAdapter(data -> {
            if (data != null) {
                ArrayList<AIModelState> onLineModels = (ArrayList<AIModelState>) IGduDroneDevice.get().getAiModel().getAiTargetDetectState().getValue().getTargetDetectModelState();
                if (CollectionUtils.isEmptyList(onLineModels)) {
                    Toast.makeText(requireContext(), R.string.Msg_no_ai_models_warn, Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < onLineModels.size(); i++) {
                    if (onLineModels.get(i).getModelId() == data.getId()) {
                        if (!CollectionUtils.isEmptyList(data.getLabels())) {
                            for (TargetDetectLabel item : data.getLabels()) {
                                int itemTypeId = Integer.parseInt(item.getId());
                                if (onLineModels.get(i).getTypeId() == itemTypeId) {
                                    onLineModels.get(i).setState(item.isChecked() ? (byte) 0x01 : (byte) 0x00);
                                }
                            }
                        }
                    } else {
                        //内置算力同时只能开启一个算法模型，因此当开启时候，默认把其他算法模型置为0
                        if (!IGduDroneDevice.get().getAiModel().isAiBoxPod()) {
                            onLineModels.get(i).setState((byte) 0x00);
                        }
                    }
                }
                XLogger.INSTANCE.getAPP().d(TAG, "modelAdapter点击打开或关闭算法后：" + GsonUtils.toJson(onLineModels));
                SdkDemoApplication.getAircraftInstance().getVision().setTargetType(onLineModels,
                        error -> XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "setAIBoxTargetType callBack() code = " + error));
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
    }

    private void getTargetDetectModels() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            cancelLoadingAnimator();
            return;
        }
        SdkDemoApplication.getAircraftInstance().getVision().setOnTargetDetectModelsListener(models -> {
            ThreadHelper.runOnUiThread(() -> {
                cancelLoadingAnimator();
                transModelData(models);
            });
        });
        SdkDemoApplication.getAircraftInstance().getVision().getTargetDetectModels(gduError -> XLogger.INSTANCE.getAPP().i("SettingCommonFragment", "getTargetDetectModels callBack() code = " + gduError));
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

    private void transModelData(List<AIModelState> data) {
        Log.d(TAG, "transModelData: " + GsonUtils.toJson(data));
        if (data == null) return;
        ArrayList<TargetDetectModel> models = new ArrayList<>();

        DetectModelDBHelper dbHelper = new DetectModelDBHelper(requireContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Cursor labelCursor = null;

        List<GroupVO> sourceList = groupItemById(data);
        for (int i = 0; i < sourceList.size(); i++) {
            GroupVO aiModel = sourceList.get(i);
            ArrayList<TargetDetectLabel> labels = new ArrayList<>();
            if (aiModel.getChildList() == null) continue;

            for (int j = 0; j < aiModel.getChildList().size(); j++) {
                int labelId = aiModel.getChildList().get(j).getTypeId();
                String labelName = "";
                try {
                    labelCursor = db.rawQuery("SELECT * FROM algo_class WHERE label_id = ?", new String[]{String.valueOf(labelId)});
                    if (labelCursor != null && labelCursor.moveToNext()) {
                        if (SystemUtils.isZh(requireContext())) {
                            labelName = labelCursor.getString(1);
                        } else {
                            labelName = labelCursor.getString(2);
                        }
                    }
                } catch (Exception exception) {
                    Log.e("SettingCommonFragment", exception.toString());
                }
//                TargetLabel targetLabel = TargetLabel.get(labelId);
//                if (targetLabel != null) {
//                    labelName = ResourceUtil.getStringById(targetLabel.getValue());
//                }
                Log.e("SettingCommonFragment", "transModelData labelId = " + labelId + ", labelName = " + labelName);
                labels.add(new TargetDetectLabel(j, String.valueOf(labelId), labelName, aiModel.getChildList().get(j).getState() == 1));
//                labels.add(new TargetDetectLabel(j, String.valueOf(labelId), labelName, getDetectLabelState(aiModel.getModelId(), j)));
            }
            String modelName = "";
            try {
                cursor = db.rawQuery("SELECT * FROM algo_label_ref WHERE algorithm_id = ?", new String[]{String.valueOf(aiModel.groupId)});
                if (cursor != null && cursor.moveToNext()) {
                    if (SystemUtils.isZh(requireContext())) {
                        modelName = cursor.getString(1);
                    } else {
                        modelName = cursor.getString(2);
                    }
                    modelName = cursor.getString(5); //烟雾、火点检测
                }
            } catch (Exception exception) {
                Log.e("SettingCommonFragment", exception.toString());
            }
            TargetDetectModel model = new TargetDetectModel(aiModel.groupId, modelName, labels);
            models.add(model);
        }
        XLogger.INSTANCE.getAPP().d(TAG, "当前算法列表：" + GsonUtils.toJson(models));
        Log.d("SettingCommonFragment", "当前算法列表：" + GsonUtils.toJson(models));
        if (cursor != null) cursor.close();
        if (labelCursor != null) labelCursor.close();
        db.close();
        modelAdapter.submitList(models);
    }

    public List<GroupVO> groupItemById(List<AIModelState> sourceList) {
        Map<Integer, List<AIModelState>> groupMap = sourceList.stream()
                .collect(Collectors.groupingBy(AIModelState::getModelId));
        return groupMap.entrySet().stream()
                .map(entry -> {
                    GroupVO vo = new GroupVO();
                    vo.setGroupId(entry.getKey());
                    vo.setChildList(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    class GroupVO {
        private int groupId;
        private List<AIModelState> childList; // 同ID的子数据集合

        // getter setter
        public int getGroupId() { return groupId; }
        public void setGroupId(int groupId) { this.groupId = groupId; }
        public List<AIModelState> getChildList() { return childList; }
        public void setChildList(List<AIModelState> childList) { this.childList = childList; }
    }

    public static SettingCommonFragment newInstance() {
        Bundle args = new Bundle();
        SettingCommonFragment fragment = new SettingCommonFragment();
        fragment.setArguments(args);
        return fragment;
    }

}