package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
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

import com.gdu.GlobalVariableTest;
import com.gdu.common.error.GDUError;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.FlightActivity;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.FragmentSettingCommonBinding;
import com.gdu.demo.utils.AnimationUtils;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.NorthPointerView;
import com.gdu.drone.FirmwareType;
import com.gdu.login.LoginType;
import com.gdu.login.UserInfoBeanNew;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socket.GduSocketManager;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.ChannelUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.TextUtil;
import com.gdu.util.TimeUtil;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;



/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingCommonFragment extends Fragment {

    private static final String TAG = SettingCommonFragment.class.getSimpleName();
    private FragmentSettingCommonBinding mViewBinding;
    private int currentSecondLevelType = 0;
    private SettingDao mSettingDao;
    private Handler handler;


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

        mViewBinding.upgradeVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.upgrade_package_version));
        mViewBinding.svnVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.svn_version));
        mViewBinding.acVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.center_control_version));
        mViewBinding.visionVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.vision_version));
        mViewBinding.rtcmVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.rtcm_version));
        mViewBinding.itCompVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.img_version));
        mViewBinding.vvPicTransFirmwareVersion.setFirmwareName(GduAppEnv.application.getString(R.string.Label_PicTransFirmwareVersion));
        mViewBinding.fcCoprocessorVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.fly_control_coprocessor_version));
        mViewBinding.fifthGenerationVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.five_g_version));

        ViewUtils.setViewShowOrHide(mViewBinding.vvRtkVersionView, GlobalVariable.RTKOnline == 0);
        mViewBinding.vvRtkVersionView.setFirmwareName(GduAppEnv.application.getString(R.string.Label_RtkVersion));
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
        ViewUtils.setViewShowOrHide(mViewBinding.fcCoprocessorVersionView, !CommonUtils.curPlanIsSmallFlight());
        ViewUtils.setViewShowOrHide(mViewBinding.fifthGenerationVersionView, !CommonUtils.curPlanIsSmallFlight());

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
                public void onFailure(GDUError var1) {
                }
            });
        }

        mViewBinding.tvCurrentVersionFly.setText(GlobalVariable.flyVersionStr);

        GduSocketManager.getInstance().getGduCommunication().getBatterInfo((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String batteryVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." + (bean.frameContent[2] & 0xff);
                        mViewBinding.tvCurrentVersionBatter.setText(batteryVersion);
                    }
                });
            }
        });
        if (CommonUtils.curPlanIsSmallFlight()) {
            GduSocketManager.getInstance().getGduCommunication().getRTKVersionNew((code, bean) -> {
                if (handler != null && isAdded()) {
                    handler.post(() -> {
                        if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                            final String rtkVersion = bean.frameContent[0] + "." + bean.frameContent[1] + "." +
                                    ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2);
                            mViewBinding.vvRtkVersionView.setCurrentVersion(rtkVersion);
                        }
                    });
                }
            });
        } else {
            if (!TextUtil.isEmptyString(GlobalVariable.rtkVersion + "")) {
                mViewBinding.vvRtkVersionView.setCurrentVersion("V" + GlobalVariable.rtkVersion);
            }
        }


        GduSocketManager.getInstance().getGduCommunication().getRTKVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String rtkVersion = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
                        mViewBinding.rtcmVersionView.setCurrentVersion(rtkVersion);
                    }
                });
            }
        });


        GduSocketManager.getInstance().getGduCommunication().getOTAVersions((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 5) {
                        String otaVersion = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
                        mViewBinding.tvCurrentVersionOta.setText(otaVersion);
                        if (!CommonUtils.curPlanIsSmallFlight()) {
                            String version = bean.frameContent[5] + "." + bean.frameContent[6] + "." + bean.frameContent[7];
                            mViewBinding.upgradeVersionView.setCurrentVersion(version);
                        }
                    }
                });
            }
        });

        if (CommonUtils.curPlanIsSmallFlight()) {
            GduSocketManager.getInstance().getGduCommunication().getPicTransmissionApplicationVersion((code, bean) -> {
                if (handler != null && isAdded()) {
                    handler.post(() -> {
                        if (bean != null && bean.frameContent != null && bean.frameContent.length >= 9) {
                            final String itVersion = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2) + "."
                                    + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 4)
                                    + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 6)
                                    + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 8);
                            mViewBinding.upgradeVersionView.setCurrentVersion(itVersion);
                        }
                    });
                }
            });
        }

        GduSocketManager.getInstance().getGduCommunication().getPicTransmissionComponentsVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String itVersion = bean.frameContent[0] + "."
                                + bean.frameContent[1] + "." + bean.frameContent[2];
                        mViewBinding.itCompVersionView.setCurrentVersion(itVersion);
                    }
                });
            }
        });

        GduSocketManager.getInstance().getGduCommunication().getOnboardITSystemVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 9) {
                        String versionStr = ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 2)
                                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 4)
                                + "." + ByteUtilsLowBefore.byte2UnsignedShort(bean.frameContent, 6);
                        mViewBinding.vvPicTransFirmwareVersion.setCurrentVersion(versionStr);
                    }
                });
            }
        });

        GduSocketManager.getInstance().getGduCommunication().getGimbalVersionById(GlobalVariable.mGimbalCompId, ((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 5) {
                        final String mountSnStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
                        mViewBinding.gimbalVersionView.setCurrentVersion(mountSnStr);
                    }
                });
            }
        }));

        GduSocketManager.getInstance().getGduCommunication().getCameraVersionByCompID(GlobalVariable.mGimbalCompId,(byte) 1, ((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 5) {
                        final String mountSnStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
                        mViewBinding.vlCameraVersionView.setCurrentVersion(mountSnStr);
                    }
                });
            }
        }));

        GduSocketManager.getInstance().getGduCommunication().getCameraVersionByCompID(GlobalVariable.mGimbalCompId,(byte) 0, ((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 5) {
                        final String mountSnStr = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
                        mViewBinding.irCameraVersionView.setCurrentVersion(mountSnStr);
                    }
                });
            }
        }));


        GduSocketManager.getInstance().getGduCommunication().getACVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        String versionStr =  bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
                        mViewBinding.acVersionView.setCurrentVersion(versionStr);
                    }
                });
            }
        });

        GduSocketManager.getInstance().getGduCommunication().getRCAVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String version = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
                        mViewBinding.tvCurrentVersionRCa.setText(version);
                    }
                });
            }
        });

        GduSocketManager.getInstance().getGduCommunication().getVisionVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String version = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
                        mViewBinding.visionVersionView.setCurrentVersion(version);
                    }
                });
            }
        });

        GduSocketManager.getInstance().getGduCommunication().getImageTransmissionRelayVersion((code, bean) -> {
            if (handler != null && isAdded()) {
                handler.post(() -> {
                    if (bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
                        final String version = bean.frameContent[0] + "." + bean.frameContent[1] + "." + bean.frameContent[2];
                        mViewBinding.tvAp12.setText(version);
                    }
                });
            }
        });
        if (SdkDemoApplication.getAircraftInstance() != null && SdkDemoApplication.getAircraftInstance().getRemoteController() != null) {
            String rcSn = SdkDemoApplication.getAircraftInstance().getRemoteController().getRCSN();
            mViewBinding.tvSnRC.setText(rcSn);
        }
        if (GlobalVariableTest.AllFlyTime == 0) {
            mViewBinding.tvTotalFlyTime.setText(GduAppEnv.application.getString(R.string.Label_TextView_NA));
        } else {
            mViewBinding.tvTotalFlyTime.setText((TimeUtil.getHourAndMinute(GlobalVariableTest.AllFlyTime * 1000 * 60)) + " ");
        }

    }
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
            if (position == SPUtils.getInt(requireContext(), SPUtils.MAP_TYPE)) {
                return;
            }
            mViewBinding.opMapModel.setIndex(position);
            SPUtils.put(requireContext(), SPUtils.MAP_TYPE, position);
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
        mViewBinding.ivShowRouteHistorySwitchBtn.setOnClickListener(listener);
        mViewBinding.tvUnit.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {
                int unit ;
                if (position == 0) {
                    unit = SettingDao.Unit_Merch;
                    GlobalVariable.showAsInch = false;
                    mSettingDao.saveIntValue(mSettingDao.Label_Unit, unit);
                } else if (position == 1) {
                    unit = SettingDao.Unit_Inch;
                    GlobalVariable.showAsInch = true;
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
        ChannelUtils.setupSn(View.GONE, mViewBinding.rlSn, mViewBinding.rlSnRC, mViewBinding.gimbalSn, mViewBinding.batterySn);
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



    public static SettingCommonFragment newInstance() {
        Bundle args = new Bundle();
        SettingCommonFragment fragment = new SettingCommonFragment();
        fragment.setArguments(args);
        return fragment;
    }
}