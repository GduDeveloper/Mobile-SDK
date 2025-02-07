package com.gdu.demo.flight.setting.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
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
import com.gdu.demo.flight.setting.adapter.TargetDetectModelAdapter;
import com.gdu.demo.flight.setting.bean.GetAiModel;
import com.gdu.demo.flight.setting.bean.TargetDetectLabel;
import com.gdu.demo.flight.setting.bean.TargetDetectModel;
import com.gdu.demo.flight.setting.bean.TargetLabel;
import com.gdu.demo.flight.setting.firmware.CycleFirmwareVersionManage;
import com.gdu.demo.flight.setting.firmware.ICycleGetFirmwareUpdate;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.NorthPointerView;
import com.gdu.demo.widget.VersionView;
import com.gdu.detect.AIModelState;
import com.gdu.drone.FirmwareType;
import com.gdu.drone.GimbalType;
import com.gdu.event.EventMessage;
import com.gdu.login.LoginType;
import com.gdu.login.UserInfoBeanNew;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.ChannelUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.GimbalUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.NetWorkUtils;
import com.gdu.util.ResourceUtil;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.ThreadHelper;
import com.gdu.util.TimeUtil;
import com.gdu.util.ViewUtils;
import com.gdu.util.eventbus.GlobalEventBus;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import cc.taylorzhang.singleclick.SingleClickUtil;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingCommonFragment extends Fragment {

    private static final String TAG = SettingCommonFragment.class.getSimpleName();
    private FragmentSettingCommonBinding mViewBinding;
    private int currentSecondLevelType = 0;

    private SettingDao mSettingDao;

    private boolean isDevResetSuc;
    private boolean isServiceResetSuc;

    private AppResetHelper mAppResetHelper;

    private CycleFirmwareVersionManage mCycleFirmwareVersionManage;
    private boolean viewIsAttached;

    private ObjectAnimator objectAnimator;

    private TargetDetectModelAdapter modelAdapter;

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
        //只有S200系列才显示地图投影, 否则不显示
        ViewUtils.setViewShowOrHide(mViewBinding.tvArMapProjectLabel, DroneUtil.isS200Serials());
        ViewUtils.setViewShowOrHide(mViewBinding.ivShowArMapProjection, DroneUtil.isS200Serials());
        ViewUtils.setViewShowOrHide(mViewBinding.viewLineMapProjection, DroneUtil.isS200Serials());

        ViewUtils.setViewShowOrHide(mViewBinding.viewStealthSwitchGroup, UavStaticVar.isOpenTextEnvironment);
        ViewUtils.setViewShowOrHide(mViewBinding.vvLockBatteryMcu,!DroneUtil.isS200Serials()
                && (GlobalVariable.armBatteryMcuOn == 1));
        final String serviceAuthCode = SPUtils.getString(requireContext(),
                MyConstants.SERVICE_AUTH_CODE, "");
        if (!StringUtils.isEmptyString(serviceAuthCode)) {
            mViewBinding.etRequestCode.setText(serviceAuthCode + " ");
        }

        if (GlobalVariable.accompanyingModel == 1) {
            mViewBinding.tvAircraftMode.setText(R.string.string_car_model);
        } else {
            mViewBinding.tvAircraftMode.setText(R.string.ordinary_mode);
        }

        initShow();
        initMapType();

        final String address = SPUtils.getString(requireContext(), SPUtils.MS_ADDRESS, "120.24.12.64");
        int port = SPUtils.getCustomInt(requireContext(), SPUtils.MS_PORT, 2883);
        int push_port = SPUtils.getCustomInt(requireContext(), SPUtils.PUSH_PORT, 40013);
        int organization_port = SPUtils.getCustomInt(requireContext(), SPUtils.ORGANIZATION_PORT, 40012);
        mViewBinding.editIp.setText(address + " ");
        mViewBinding.editPort.setText(port + " ");
        mViewBinding.pushPort.setText(push_port + " ");
        mViewBinding.organizationPort.setText(organization_port + " ");

//        mViewBinding.ivShowArMapProjection.setSelected(RouteManager.Companion.getInstance().getARMapProjectionSwitch(GduApplication.context));
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
        mViewBinding.ivSwitchMotorAdjust.setSelected(GlobalVariable.motor_adjust_voice == 1);

        boolean isShowActiveBtn = false;
        String loginTypeStr = SPUtils.getString(requireContext(), MyConstants.SAVE_LOGIN_TYPE);
        if (LoginType.TYPE_PHONE.getValue().equals(loginTypeStr)) {
            final UserInfoBeanNew mLoginInfo = new Gson().fromJson(SPUtils.getString(requireContext(), MyConstants.SAVE_NEW_USER_INFO), UserInfoBeanNew.class);
            if (mLoginInfo != null && mLoginInfo.getData() != null && mLoginInfo.getData().getAdmin() != null) {
                isShowActiveBtn = mLoginInfo.getData().getAdmin();
            }
        }

        MyLogUtils.i("onResume() isShowActiveBtn = " + isShowActiveBtn);
        ViewUtils.setViewShowOrHide(mViewBinding.clResetDevLayout, isShowActiveBtn);
        ViewUtils.setViewShowOrHide(mViewBinding.clResetLayout, isShowActiveBtn);
        ViewUtils.setViewShowOrHide(mViewBinding.assistantPodVersionView,
                GlobalVariable.sAssistantPodCompId != 0);
        ViewUtils.setViewShowOrHide(mViewBinding.fcCoprocessorVersionView, !CommonUtils.curPlanIsSmallFlight());
        ViewUtils.setViewShowOrHide(mViewBinding.adapterRingVersionView, !CommonUtils.curPlanIsSmallFlight());
        ViewUtils.setViewShowOrHide(mViewBinding.fifthGenerationVersionView, !CommonUtils.curPlanIsSmallFlight());
        getFirmwareVersion();

        boolean isOpenArmLamp = GlobalVariable.flight_arm_lamp_status == 0;
        boolean isOpenBatteryLight = GlobalVariable.battery_silence_status == 1;
        MyLogUtils.i("initView() isOpenArmLamp = " + isOpenArmLamp + "; isOpenBatteryLight = "
                + isOpenBatteryLight + "; flight_arm_lamp_status = "
                + GlobalVariable.flight_arm_lamp_status
                + "; battery_silence_status = "
                + GlobalVariable.battery_silence_status);
        boolean isSelect = isOpenArmLamp & isOpenBatteryLight;
        mViewBinding.ivStealthModeSwitch.setSelected(isSelect);
        final boolean showRouteHistory = SPUtils.getBoolean(requireContext(), MyConstants.SHOW_ROUTE_HISTORY);
        mViewBinding.ivShowRouteHistorySwitchBtn.setSelected(showRouteHistory);
        final boolean showImageDebugText = SPUtils.getCustomBoolean(GduAppEnv.application, MyConstants.SHOW_IMAGE_DEBUG_TEXT, true);
        mViewBinding.ivImageDebugTextLabel.setSelected(showImageDebugText);
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
            getTextValue();
        }
    };

    private void getTextValue() {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            return;
        }
//        GduApplication.getSingleApp().gduCommunication.getTextValue((byte) 0, (code, bean) -> {
//            if (bean != null && bean.frameContent != null && bean.frameContent.length > 1) {
//
//                int type = bean.frameContent[0];
//                byte[] connectData = new byte[254];
//                System.arraycopy(bean.frameContent, 1, connectData, 0, 254);
//                if ((connectData[0] == ((int) 'C') && connectData[1] == 'G' && connectData[2] == 'D')
//                        || (connectData[0] == ((int) 'Q') && connectData[1] == 'X' && connectData[2] == 'Y')) {
//                    String SN = "", DeviceType = "", DSK = "", DSS = "";
//                    StringBuilder stringBuilder = new StringBuilder();
//                    int textType = 0;
//
//                    for (byte data : connectData) {
//                        if (data != 0x20 && data != 0x0A) {
//                            stringBuilder.append((char) data);
//                        }
//                        // 空格代表一个值结束
//                        if (data == 0x20) {
//                            if (textType == 0) {
//                                SN = stringBuilder.toString();
//                            } else if (textType == 1) {
//                                DeviceType = stringBuilder.toString();
//                            } else if (textType == 2) {
//                                DSK = stringBuilder.toString();
//                            }
//                            stringBuilder.delete(0, stringBuilder.length());
//                            textType++;
//                            // 换行符表示结束
//                        } else if (data == 0x0A) {
//                            DSS = stringBuilder.toString();
//                            stringBuilder.delete(0, stringBuilder.length());
//                            break;
//                        }
//                    }
//                    String finalSN = SN;
//                    String finalDeviceType = DeviceType;
//                    String finalDSK = DSK;
//                    String finalDSS = DSS;
//                    String tvValueStr = "type = " + type + " , SN = " + finalSN
//                            + " , DT = " + finalDeviceType + ", DSK = " + finalDSK + " , DSS = " + finalDSS;
//
//                    MyLogUtils.d("DSK  = " + tvValueStr);
//                    uiThreadHandle(() -> mViewBinding.tvTextValue.setText(tvValueStr));
//                }
//            }
//        });
    }

    private void initTargetDetectView() {
        initTargetDetectType();

        if (GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX) {
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
//                    GduApplication.getSingleApp().gduCommunication.setAIBoxTargetType(data.getId(), detectType, (short) data.getLabels().size(), typeArray, (code, bean) -> {
//                        if (code == GduConfig.OK && bean != null) {
//                            AppLog.e("SettingCommonFragment", "setAIBoxTargetType success, callBack() code = " + code);
//                        } else {
//                            AppLog.e("SettingCommonFragment", "setAIBoxTargetType fail, callBack() code = " + code);
//                        }
//                    });
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
                        Object labelIdStr  = aiModel.getLabels().get(j);
                        if (aiModel.getLabels().get(j) instanceof String) {
                            labelId = Integer.parseInt((String) labelIdStr);
                        } else {
                            labelId = (int) (double) aiModel.getLabels().get(j);
                        }
                    } catch (Exception e) {
                        AppLog.e("SettingCommonFragment", "transModelData " + aiModel.getLabels().get(j));
                    }
                    String labelName = "";
                    TargetLabel targetLabel = TargetLabel.get(labelId);
                    if (targetLabel != null) {
                        labelName = ResourceUtil.getStringById(targetLabel.getValue());
                    }
                    AppLog.e("SettingCommonFragment", "transModelData labelId = " + labelId + ", labelName = " + labelName);
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
        if (GlobalVariable.targetDetectModelState != null && !GlobalVariable.targetDetectModelState.isEmpty()) {
            for (int i = 0; i < GlobalVariable.targetDetectModelState.size(); i++) {
                AIModelState model = GlobalVariable.targetDetectModelState.get(i);
                if (model.getModelId() == modelId) {
                    byte state = model.getLabelState()[index];
                    return state == 0x01;
                }
            }
        }
        return false;
    }

    private void getTargetDetectModels() {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            cancelLoadingAnimator();
            return;
        }
//        GduApplication.getSingleApp().gduCommunication.addCycleACKCB(GduSocketConfig3.CYCLE_ACK_TARGET_DETECT_MODELS, (code, bean) -> {
//            if (code == GduConfig.OK && bean != null) {
//                byte[] content = bean.frameContent;
//                int m = ByteUtilsLowBefore.byte2Int(content, 0);
//                String sJson = new String(bean.frameContent, 4, m);
//                AppLog.e("SettingCommonFragment", "getTargetDetectModels " + new String(sJson.getBytes(StandardCharsets.UTF_8)));
//                GetAiModelResponse response = new Gson().fromJson(sJson, GetAiModelResponse.class);
//                ThreadHelper.runOnUiThread(() -> {
//                    cancelLoadingAnimator();
//                    transModelData(response.getModels());
//                });
//            }
//        });
//        GduApplication.getSingleApp().gduCommunication.getTargetDetectModels((code, bean) -> {
//            if (code == GduConfig.OK && bean != null) {
//                AppLog.e("SettingCommonFragment", "getTargetDetectModels success, callBack() code = " + code);
//            } else {
//                AppLog.e("SettingCommonFragment", "getTargetDetectModels fail, callBack() code = " + code);
//            }
//        });
    }

    private void initTargetDetectType() {
        if (GlobalVariable.aiRecognitionSwitch.second != null && GlobalVariable.aiRecognitionSwitch.second.length == 3) {
            mViewBinding.cbPerson.setChecked(GlobalVariable.aiRecognitionSwitch.second[0] == 0x01);
            mViewBinding.cbCar.setChecked(GlobalVariable.aiRecognitionSwitch.second[1] == 0x01);
            mViewBinding.cbShip.setChecked(GlobalVariable.aiRecognitionSwitch.second[2] == 0x01);
        }
        mViewBinding.cbPerson.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
        mViewBinding.cbCar.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
        mViewBinding.cbShip.setOnCheckedChangeListener((buttonView, isChecked) -> setTargetType());
    }

    private void resetAiRecognitionSwitch() {
        if (GlobalVariable.aiRecognitionSwitch.second != null && GlobalVariable.aiRecognitionSwitch.second.length == 3) {
            mViewBinding.cbPerson.setChecked(GlobalVariable.aiRecognitionSwitch.second[0] == 0x01);
            mViewBinding.cbCar.setChecked(GlobalVariable.aiRecognitionSwitch.second[1] == 0x01);
            mViewBinding.cbShip.setChecked(GlobalVariable.aiRecognitionSwitch.second[2] == 0x01);
        } else {
            mViewBinding.cbPerson.setChecked(false);
            mViewBinding.cbCar.setChecked(false);
            mViewBinding.cbShip.setChecked(false);
        }
    }

    private void setTargetType() {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess){
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            resetAiRecognitionSwitch();
            return;
        }
        if (!GlobalVariable.isTargetDetectMode) {
            Toast.makeText(requireContext(), R.string.Msg_AI_Recoginition_Warn, Toast.LENGTH_SHORT).show();
            resetAiRecognitionSwitch();
            return;
        }
        if (GlobalVariable.aiRecognitionSwitch.first == 0x0C) {
//            GduApplication.getSingleApp().gduCommunication.setTargetType((byte) 0x01, (byte) 0x01, (short) 3, getCheckedState(), (code, bean) -> {
//                AppLog.e("SettingCommonFragment", "setTargetType " + Arrays.toString(getCheckedState()) + ", callBack() code = " + code);
//                if (code == GduConfig.OK && bean != null) {
//                    Toaster.show(GduAppEnv.application.getString(R.string.Label_SettingSuccess));
//                } else {
//                    Toaster.show(GduAppEnv.application.getString(R.string.Label_SettingFail));
//                }
//            });
        } else {
//            GduApplication.getSingleApp().gduCommunication.setAITargetType((byte) 0x00, (byte) 0x01, (short) 3, getCheckedState(), (code, bean) -> {
//                AppLog.e("SettingCommonFragment", "setAITargetType " + Arrays.toString(getCheckedState()) + ", callBack() code = " + code);
//                if (code == GduConfig.OK && bean != null) {
//                    Toaster.show(GduAppEnv.application.getString(R.string.Label_SettingSuccess));
//                } else {
//                    Toaster.show(GduAppEnv.application.getString(R.string.Label_SettingFail));
//                }
//            });
        }
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

    private void initMapType() {
        boolean isHideMapView = getArguments() != null && getArguments().getInt(MyConstants.IMPORT_TYPE_KEY, -1) == 2;
        ViewUtils.setViewShowOrHide(mViewBinding.tvMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.opMapModel, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.divMapType, !isHideMapView);
        ViewUtils.setViewShowOrHide(mViewBinding.viewUpMountGroup, !CommonUtils.curPlanIsSmallFlight());
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
        mViewBinding.ivShowArMapProjection.setOnClickListener(listener);
        mViewBinding.ivShowGrid.setOnClickListener(listener);
        mViewBinding.ivVoiceTip.setOnClickListener(listener);
        mViewBinding.ivNorthPointer.setOnClickListener(listener);
        mViewBinding.ivPoseModeSwitchBtn.setOnClickListener(listener);
        mViewBinding.ivLiveCompress.setOnClickListener(listener);
        mViewBinding.openLiveImageview.setOnClickListener(listener);
        mViewBinding.droneInfoItem.setOnClickListener(listener);
        mViewBinding.targetRecognitionItem.setOnClickListener(listener);
        mViewBinding.rlLightSet.setOnClickListener(listener);
        mViewBinding.btnIpConfirm.setOnClickListener(listener);
        mViewBinding.ivSwitchADSBBtn.setOnClickListener(listener);
        mViewBinding.ivSwitchMotorAdjust.setOnClickListener(listener);
        mViewBinding.ivStealthModeSwitch.setOnClickListener(listener);
        mViewBinding.tvRefreshText.setOnClickListener(listener);
        mViewBinding.ivShowRouteHistorySwitchBtn.setOnClickListener(listener);
        mViewBinding.tiilClearPlaneLog.setOnClickListener(listener);
        SingleClickUtil.onSingleClick(mViewBinding.tvResetActiveBtn, v -> {
            isDevResetSuc = false;
            isServiceResetSuc = false;
            sendUnActiveCmd();
        });

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

        SingleClickUtil.onSingleClick(mViewBinding.tvResetBtn, v -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess){
                Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                return;
            }
            if(GlobalVariable.droneFlyState != 1 || !GlobalVariable.planeHadLock) {
                Toast.makeText(requireContext(), R.string.string_flying_forbid, Toast.LENGTH_SHORT).show();
            }
            if (!NetworkingHelper.isRCHasControlPower()) {
                Toast.makeText(requireContext(), R.string.Label_NoControlPermissionTip, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!NetWorkUtils.checkNetwork(requireContext())) {
                Toast.makeText(requireContext(), R.string.Label_Record_List_noNetwork, Toast.LENGTH_SHORT).show();
                return;
            }
//            mDialogUtils.createDialogWith2Btn("",
//                    GduAppEnv.application.getString(R.string.Msg_ResetAppAndDroneTip),
//                    GduAppEnv.application.getString(R.string.Label_cancel),
//                    GduAppEnv.application.getString(R.string.Lable_Confirm), (v1 -> {
//                        switch (v1.getId()) {
//                            case R.id.dialog_btn_cancel:
//                                DialogUtils.dismissDialog();
//                                break;
//
//                            case R.id.dialog_btn_sure:
//                                DialogUtils.dismissDialog();
//                                mAppResetHelper = new AppResetHelper(requireActivity(), appResetCallback, this);
//                                mAppResetHelper.sendResetCmd();
//                                break;
//
//                            default:
//                                break;
//                        }
//            }));
        });
        mViewBinding.ivVisualSwitchLabel.setOnClickListener(v -> {
            if (mViewBinding.ivVisualSwitchLabel.isSelected()) {
                return;
            }
            mViewBinding.ivVisualSwitchLabel.setSelected(true);
//            GduApplication.getSingleApp().gduCommunication.targetLocate((short) 955,
//                    (short) 535, (short) 10, (short) 10, (byte) 0, (byte) 0x01, (short)0, (code, bean) -> {
//                        MyLogUtils.i("targetLocate callback() code = " + code);
//                    });
        });
        mViewBinding.ivImageDebugTextLabel.setOnClickListener(listener);
    }

    private final AppResetHelper.IViewCallback appResetCallback =  new AppResetHelper.IViewCallback() {

        @Override
        public void showOrHideLoadingDialog(boolean isShow) {
//                if (isShow) {
//                    LoadingDialogUtils.createLoadDialog(requireContext(), "");
//                } else {
//                    LoadingDialogUtils.cancelLoadingDialog();
//                }
        }

        @Override
        public void showToaster(int resId) {
            Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCmdSendFinish() {
            MyLogUtils.i("onCmdSendFinish()");
            ThreadHelper.runOnUiThread(() -> {
//                DialogUtils.dismissDialog();
                selectedMode();
            });
        }
    };


    private void selectedMode() {
        MyLogUtils.i("selectedMode()");
//        if (mDialogUtils == null) {
//            mDialogUtils = new DialogUtils(requireContext());
//        }
//        mDialogUtils.createFlyModelDialog(getContext(), false, type -> {
//            LoadingDialogUtils.createLoadDialog(requireContext());
//            MyLogUtils.i("createFlyModelDialog callback() type = " + type);
//            byte setState = (byte) (type == 1 ? 8 : 9);
//            GduApplication.getSingleApp().gduCommunication.change482RtkStates(setState, (code, bean) -> {
//                MyLogUtils.i("change482RtkStates callback() code = " + code);
//                uiThreadHandle(() -> {
////                    if (code == GduConfig.OK) {
////                        showToast(R.string.string_set_success_please_restart);
////                    } else {
////                        showToast(R.string.Label_SettingFail);
////                    }
//                    mDialogUtils.cancelDialog();
//                    LoadingDialogUtils.cancelLoadingDialog();
//                    resetDevState();
//                });
//            });
//        });
    }

    private void resetDevState() {
        Optional.ofNullable(mAppResetHelper).ifPresent(AppResetHelper::resetDevActiveStatus);
    }

    public View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.iv_back:
                    updateBackView();
                    break;
                case R.id.iv_show_ar_map_projection:
//                    RouteManager.Companion.getInstance().onSaveARMapProjectionSwitch(GduApplication.context, !mViewBinding.ivShowArMapProjection.isSelected());
                    mViewBinding.ivShowArMapProjection.setSelected(!mViewBinding.ivShowArMapProjection.isSelected());
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
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

                case R.id.open_live_imageview:
                    boolean isOpen = !mViewBinding.openLiveImageview.isSelected();
                    mViewBinding.openLiveImageview.setSelected(isOpen);
                    SPUtils.put(requireContext(), GduConfig.IS_OPEN_LIVE, isOpen);
                    ViewUtils.setViewShowOrHide(mViewBinding.liveStreamLayout, isOpen);
                    if (!isOpen) {
                        GlobalVariable.serverBusyType = 1;
                        SPUtils.put(requireContext(), SPUtils.serverTypeLabel, (int) GlobalVariable.serverBusyType);
                    }
                    break;
                case R.id.target_recognition_item:
                    initTargetDetectView();
                    setSecondLevelView(mViewBinding.layoutTargetRecognition, true, GduAppEnv.application.getString(R.string.Label_Visition_Target_Detect));
                    currentSecondLevelType = 3;
                    break;

                case R.id.drone_info_item:
                    setSecondLevelView(mViewBinding.layoutDroneInfo, true, GduAppEnv.application.getString(R.string.fly_info));
                    currentSecondLevelType = 1;
                    break;

                case R.id.rl_light_set:
                    setSecondLevelView(mViewBinding.vLightSetting, true, GduAppEnv.application.getString(R.string.light_setting));
                    currentSecondLevelType = 2;
                    break;

                case R.id.btn_ip_confirm:
                    configAuthCode();
                    break;

                case R.id.iv_switchADSBBtn:
                    mViewBinding.ivSwitchADSBBtn.setSelected(!mViewBinding.ivSwitchADSBBtn.isSelected());
                    SPUtils.put(requireContext(), MyConstants.IS_OPEN_ASD_B, mViewBinding.ivSwitchADSBBtn.isSelected());
                    break;

                case R.id.iv_switch_motor_adjust:
                    setMotorAdjustStatus();
                    break;

                case R.id.ivStealthModeSwitch:
//                    mViewBinding.ivStealthModeSwitch.setSelected(!mViewBinding.ivStealthModeSwitch.isSelected());
                    if (!isSwitchLight) {
                        switchStealthMode(mViewBinding.ivStealthModeSwitch.isSelected());
                    }
                    break;

                case R.id.tv_refresh_text:
                    getTextValue();
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
                case R.id.tiil_clear_plane_log:
                    showClearDroneLogDialog();
                    break;
                case R.id.ivImageDebugTextLabel:
                    mViewBinding.ivImageDebugTextLabel.setSelected(!mViewBinding.ivImageDebugTextLabel.isSelected());
                    SPUtils.put(requireContext(), MyConstants.SHOW_IMAGE_DEBUG_TEXT, mViewBinding.ivImageDebugTextLabel.isSelected());
                    break;

                default:
                    break;
            }

        }
    };

    private void showClearDroneLogDialog() {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess){
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            return;
        }
        if (GlobalVariable.droneFlyState != 1 || !GlobalVariable.planeHadLock){
            Toast.makeText(requireContext(), R.string.string_flying_forbid, Toast.LENGTH_SHORT).show();
            return;
        }

//        DialogUtil.showDlg2Btn(getContext(), R.string.Label_hint, R.string.weather_clear_log,
//                R.string.Label_cancel, R.string.Label_Sure, new CommonDialog2Btn.OnListener() {
//                    @Override
//                    public void onConfirm(BaseDialog dialog) {
//                        GduApplication.getSingleApp().gduCommunication.clearDroneCache((byte)0x01,(code, bean) -> {
//                            uiThreadHandle(() -> {
//                                showClearResultTip(code);
//                            });
//                        });
//                    }
//                });
    }

    private void showClearResultTip(int code) {
        if (code == GduConfig.OK) {
            showRestartDroneTipDialog();
        }else {
            Toast.makeText(requireContext(), R.string.Label_ClearFail, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestartDroneTipDialog() {
//        DialogUtil.showDlg1Btn(getContext(), R.string.Label_hint, R.string.restart_drone_tip, R.string.Label_IKnow,null);
    }

    private void sendUnActiveCmd() {
        MyLogUtils.i("sendUnActiveCmd()");
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            Toast.makeText(requireContext(), R.string.update_conn_err1, Toast.LENGTH_SHORT).show();
            return;
        }
        if (CommonUtils.isEmptyString(GlobalVariable.SN)) {
            Toast.makeText(requireContext(), R.string.Msg_unSNTip, Toast.LENGTH_SHORT).show();
            return;
        }
//        ActiveUtil.sendActiveCmd(false, new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                if(getActivity() == null) {
//                    return;
//                }
//                getActivity().runOnUiThread(()->{
//                    if(code == ActiveUtil.ActiveCode.Success) {
//                        isDevResetSuc = true;
//                        resetDevStatus();
//                    } else {
//                        judgeErrTip();
//                    }
//                });
//            }
//        });
    }

    private void resetDevStatus() {
        MyLogUtils.i("resetDevStatus()");
//        boolean isHaveError = CommonUtils.isEmptyString(GduApplication.getSingleApp().getToken())
//                || !NetWorkUtils.checkNetwork(requireContext());
//        if (isHaveError) {
//            judgeErrTip();
//            return;
//        }
        // 由于是在激活失败后自动重置所以不再次进行网络状态等异常状态判断
//        ActiveUtil.activeReset(LifeScope.of(this), new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                if(code == ActiveUtil.ActiveCode.Success){
//                    ActiveUtil.uploadActiveInfo(false, LifeScope.of(SettingCommonFragment.this), new ActiveUtil.ActiveCallBack() {
//                        @Override
//                        public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                            if(code == ActiveUtil.ActiveCode.Success) {
//                                isServiceResetSuc = true;
//                                // 测试模式提示字段可以不翻译
//                                if (!isDevResetSuc) {
//                                    judgeErrTip();
//                                } else {
//                                    String tipStr1 = "设备、服务器激活状态重置成功";
//                                    showToast(tipStr1);
//                                }
//                                GlobalEventBus.getBus().post(new EventMessage(MyConstants.DEV_RESET_ACTIVE_SUC_KEY));
//                            } else {
//                                restoreActive(s);
//                            }
//                        }
//                    });
//                } else {
//                    restoreActive(s);
//                }
//            }
//        });
    }
    /* 重置失败，恢复激活 */
    private void restoreActive(String e) {
        AppLog.e("重置激活设备状态出错", e);
//        ActiveUtil.sendActiveCmd(true, new ActiveUtil.ActiveCallBack() {
//            @Override
//            public void onCallBack(@NonNull ActiveUtil.ActiveCode code, @NonNull String s) {
//                MyLogUtils.i("resetDevStatus() activeFlight callback() code = " + code);
//            }
//        });
        judgeErrTip();
    }

    private void judgeErrTip() {
        // 测试模式提示字段可以不翻译
        String tipStr;
        if (!isDevResetSuc) {
            tipStr = "设备激活状态重置失败, 请重试";
        } else if (!isServiceResetSuc) {
            tipStr = "服务器激活状态重置失败, 请重试";
        } else if (!isDevResetSuc & !isServiceResetSuc) {
            tipStr = "设备、服务器激活状态重置失败, 请重试";
        } else {
            tipStr = "设备、服务器激活状态重置成功";
        }
        Toast.makeText(requireContext(), tipStr, Toast.LENGTH_SHORT).show();
    }

    private boolean isSwitchLight;
    private boolean isOpenBatLightSuc;
    private boolean isOpenArmLampSuc;
    private int sendCmdNum = 0;

    private void switchStealthMode(boolean isOpen) {
        MyLogUtils.i("switchStealthMode() isOpen = " + isOpen);
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            Toast.makeText(requireContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
            return;
        }
        isOpenBatLightSuc = false;
        isOpenArmLampSuc = false;
        sendCmdNum = 0;
        isSwitchLight = true;

        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.switchPowerSilenceModel(
//                mViewBinding.ivStealthModeSwitch.isSelected(), ((code, bean) -> {
//                    MyLogUtils.i("switchPowerSilenceModel callback() code = " + code);
//                    sendCmdNum--;
//                    if (code == GduConfig.OK) {
//                        isOpenBatLightSuc = true;
//                    }
//                    if (sendCmdNum == 0) {
//                        judgeSetResult();
//                    }
//                }));
        sendCmdNum++;
//        GduApplication.getSingleApp().gduCommunication.setFlightArmLamp(
//                mViewBinding.ivStealthModeSwitch.isSelected(), ((code, bean) -> {
//                    MyLogUtils.i("setFlightArmLamp callback() code = " + code);
//                    sendCmdNum--;
//                    if (code == GduConfig.OK) {
//                        isOpenArmLampSuc = true;
//                    }
//                    if (sendCmdNum == 0) {
//                        judgeSetResult();
//                    }
//                }));
    }

    private void judgeSetResult() {
        MyLogUtils.i("judgeSetResult() isOpenBatLightSuc = " + isOpenBatLightSuc
                + "; isOpenArmLampSuc = " + isOpenArmLampSuc
                + "; isSwitchLight = " + isSwitchLight);
        isSwitchLight = false;
        ThreadHelper.runOnUiThread(() -> {
            if (isOpenBatLightSuc && isOpenArmLampSuc) {
                mViewBinding.ivStealthModeSwitch.setSelected(!mViewBinding.ivStealthModeSwitch.isSelected());
                Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                return;
            }
            MyLogUtils.i("judgeSetResult() 错误处理");
            Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
//            mViewBinding.ivStealthModeSwitch.setSelected(!mViewBinding.ivStealthModeSwitch.isSelected());
        });
    }

    private void setMotorAdjustStatus() {
        if (!connStateToast()) {
            return;
        }
        boolean selected = mViewBinding.ivSwitchMotorAdjust.isSelected();
//        GduApplication.getSingleApp().gduCommunication.setMotorAdjustVoiceStatus((byte) (selected ? 0 : 1), (code, bean) -> {
//            uiThreadHandle(() -> {
//                if (code == 0) {
//                    GlobalVariable.motor_adjust_voice = (byte) (selected ? 0 : 1);
//                    mViewBinding.ivSwitchMotorAdjust.setSelected(GlobalVariable.motor_adjust_voice == 1);
//                } else {
//                    mDialogUtils.Toast(GduAppEnv.application.getString(R.string.Label_SettingFail));
//                }
//            });
//        });
    }

    private void setSecondLevelView(View view, boolean show, String title) {

//        MyAnimationUtils.animatorRightInOut(view, show);
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
            cancelLoadingAnimator();
        }
        currentSecondLevelType = 0;
    }

    private boolean connStateToast() {
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(requireContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_MoreOne:
                Toast.makeText(requireContext(), R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_Sucess:
                return true;

            default:
                break;
        }
        return false;
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

        updateFirmwareVersion(true);

        boolean isOpenLive = SPUtils.getFalseBoolean(requireContext(), GduConfig.IS_OPEN_LIVE);
        mViewBinding.openLiveImageview.setSelected(isOpenLive);
        ViewUtils.setViewShowOrHide(mViewBinding.liveStreamLayout, isOpenLive);

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
            if (isHaveArmGimbal) {
                setPTZArmVersion(isGetting);
            }
            if (isHaveArmSysGimbal) {
                setPTZArmSystemVersion(isGetting);
            }
            ViewUtils.setViewShowOrHide(mViewBinding.rlMountAiBoxLayout,
                    GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX);
            if (GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX) {
                setMountAiBoxVersion(isGetting);
            }

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

    private void setPTZArmVersion(boolean isGetting) {
        MyLogUtils.i("setPTZArmVersion()");
        ThreadHelper.runOnUiThread(() -> {
            String emptyStr;
            if (isGetting) {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GettingStatus);
            } else {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GetFailStatus);
            }
            mViewBinding.tvPtzArmVersion.setText(
                    CommonUtils.isEmptyString(GlobalVariable.GIMBAL_ARM_VERSION)
                            ? emptyStr : GlobalVariable.GIMBAL_ARM_VERSION);
        });
    }

    private void setPTZArmSystemVersion(boolean isGetting) {
        MyLogUtils.i("setPTZArmSystemVersion()");
        ThreadHelper.runOnUiThread(() -> {
            String emptyStr;
            if (isGetting) {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GettingStatus);
            } else {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GetFailStatus);
            }
            mViewBinding.tvPtzArmSystemVersion.setText(
                    CommonUtils.isEmptyString(GlobalVariable.GIMBAL_ARM_SYSTEM_VERSION)
                            ? emptyStr : GlobalVariable.GIMBAL_ARM_SYSTEM_VERSION);
        });
    }

    private void setMountAiBoxVersion(boolean isGetting) {
        MyLogUtils.i("setMountAiBoxVersion()");
        ThreadHelper.runOnUiThread(() -> {
            String emptyStr;
            if (isGetting) {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GettingStatus);
            } else {
                emptyStr = GduAppEnv.application.getString(R.string.Msg_GetFailStatus);
            }
            final String aiBoxVerStr = "V" + CommonUtils.getFlyVersion(requireContext(), FirmwareType.GIMBAL_AI_BOX_VERSION.getEnValue());
            mViewBinding.tvMountAiBoxVersion.setText(CommonUtils.isEmptyString(aiBoxVerStr) ? emptyStr : aiBoxVerStr);
        });
    }

    public static SettingCommonFragment newInstance(int importType) {
        Bundle args = new Bundle();
        args.putInt(MyConstants.IMPORT_TYPE_KEY, importType);
        SettingCommonFragment fragment = new SettingCommonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 设置授权码
     */
    private void configAuthCode() {
        final String code = mViewBinding.etRequestCode.getText().toString().trim();
        Map<String, String> params = new HashMap<>();
        params.put("token", code);
//        RetrofitClient.getApiAppConfig(SettingsService.class)
//                .setServerAuthCode(params).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                MyLogUtils.i("setServerAuthCode onResponse()");
//                try {
//                    String responseStr = response.body().string();
//                    if (responseStr.startsWith("[") && responseStr.endsWith("]")) {
//                        responseStr = responseStr.substring(1, responseStr.length() - 1);
//                        final Gson mGson = new Gson();
//                        final ServerAuthBean bean = mGson.fromJson(responseStr, ServerAuthBean.class);
//                        if (bean == null) {
//                            uiThreadHandle(() -> mDialogUtils.Toast(R.string.AuthorizationFailed));
//                            return;
//                        }
//                        SPUtils.put(requireActivity(), MyConstants.SERVICE_AUTH_CODE, code);
//                        GlobalVariable.serverBusyType =
//                                (byte) (Integer.valueOf(bean.getServerType()).intValue());
//                        SPUtils.put(requireActivity(), SPUtils.MS_ADDRESS, bean.getMqttHost());
//                        SPUtils.put(requireActivity(), SPUtils.ServerUseHttpIp, bean.getHttpIp());
//                        WebUrlConfig.BASEURL = bean.getHttpIp();
//                        SPUtils.put(requireActivity(), SPUtils.MS_PORT, bean.getMqttPort());
//                        SPUtils.put(requireActivity(), SPUtils.PUSH_PORT, bean.getPushPort());
//                        SPUtils.put(requireActivity(), SPUtils.ORGANIZATION_PORT, bean.getOrganizationPort());
//                        SPUtils.put(requireActivity(), SPUtils.serverTypeLabel, (int) GlobalVariable.serverBusyType);
//                        SPUtils.put(requireActivity(), SPUtils.ServerUseToken, code);
//
//                        uiThreadHandle(() -> mDialogUtils.Toast(R.string.AuthorizationSuccess));
//                    }
//                } catch (Exception e) {
//                    ThreadHelper.runOnUiThread(() -> mDialogUtils.Toast(R.string.AuthorizationFailed));
//                    MyLogUtils.e("设置授权码出错", e);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                MyLogUtils.i("setServerAuthCode onFailure()");
//                MyLogUtils.e("切换服务器授权出错", t);
//                //授权失败
//                ThreadHelper.runOnUiThread(() -> mDialogUtils.Toast(R.string.AuthorizationFailed));
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Optional.ofNullable(mCycleFirmwareVersionManage).ifPresent(manager -> {
            manager.unRegisterVersionCallback(statusCallback);
            manager.onDestroy();
        });
//        GduApplication.getSingleApp().gduCommunication.removeCycleACKCB(GduSocketConfig3.CYCLE_ACK_TARGET_DETECT_MODELS);
        cancelLoadingAnimator();
    }
}