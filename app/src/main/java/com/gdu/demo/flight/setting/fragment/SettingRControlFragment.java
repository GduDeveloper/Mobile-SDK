package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentRcControlBinding;
import com.gdu.drone.ControlHand;
import com.gdu.event.EventConnState;
import com.gdu.event.EventMessage;
import com.gdu.event.GimbalEvent;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.socket.UICallBack;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.DroneUtil;
import com.gdu.util.MD5Util;
import com.gdu.util.MyConstants;
import com.gdu.util.NetWorkUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.eventbus.GlobalEventBus;
import com.gdu.util.logger.MyLogUtils;
import com.google.gson.Gson;
import com.rxjava.rxlife.RxLife;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/23
 */
public class SettingRControlFragment extends Fragment {


    private FragmentRcControlBinding mViewBinding;
    /**
     * 当前显示二级页面类型
     */
    private int currentSecondLevelType = 0;

    private ControlHand selectHand = GlobalVariable.controlHand;

    /** 是否发送飞机对频指令成功 */
    private boolean isClickConnect = false;
    private boolean isRCControlCamera = true; //1:控制云台    2：控制相机
//
//    private RcCalibrationHelper mRcCalibrationHelper;
//    private FlightRecordService mFlightRecordService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentRcControlBinding.inflate(LayoutInflater.from(requireContext()));
        initViews();
        initData();
        return mViewBinding.getRoot();
    }


    public void initViews() {
        mViewBinding.tvNetworkingModel.setText(NetworkingHelper.getCurrentNetworkingName(getContext()));
        mViewBinding.ivBack.setOnClickListener(listener);
        mViewBinding.tvRcModel.setOnClickListener(listener);
        mViewBinding.tvRcCustomKeyView.setOnClickListener(listener);
        mViewBinding.tvNetworkingLabel.setOnClickListener(listener);
        mViewBinding.tvNetworkingModel.setOnClickListener(listener);
        mViewBinding.tvRcMatch.setOnClickListener(listener);
        mViewBinding.ivBolunSetting.setOnClickListener(listener);
        mViewBinding.tvRcControlCheck.setOnClickListener(listener);
        mViewBinding.rcCheckLin.setVisibility(GlobalVariable.isRCSEE ? View.VISIBLE : View.GONE);

        if (DroneUtil.isS200Serials()) {
            mViewBinding.controlModeGroup.setVisibility(View.GONE);
        } else {
            mViewBinding.controlModeGroup.setVisibility(View.VISIBLE);
        }

        mViewBinding.controlHandView.setOnControlHandModeListener(this::setControlHand);

        mViewBinding.advancedNetworkingView.setOnANListener(name -> mViewBinding.tvNetworkingModel.setText(name));
    }


    private void setControlHand(int type, ControlHand controlHand) {
        selectHand = controlHand;
//        if (type == 01) {
//            GduApplication.getSingleApp().controlServer.setControlHand(controlHand,sHandCallback);
//        } else if (type == 02) {
//            GduApplication.getSingleApp().controlServer.setControlHand(controlHand,sHandCallback);
//        } else {
//            GduApplication.getSingleApp().controlServer.setControlHand(controlHand,sHandCallback);
//        }
    }

    private final UICallBack sHandCallback = new UICallBack() {
        @Override
        public void cb(int code, Object obj) {
            MyLogUtils.i("sHandCallback callback() code = " + code);
//            uiThreadHandle(() -> {
//                if (code == GduConfig.OK) {
//                    GlobalVariable.controlHand = selectHand;
//                    mDialogUtils.Toast(R.string.Label_SettingSuccess);
//                    mViewBinding.controlHandView.setControlHandPic();
//                    GduApplication.getSingleApp().wifiConnServer.playSound(WifiConnServer.CONTROL_HAND_HAS_SWITCHED, 0, false);
//                    ToolManager.VibratoSet(getActivity()); //marker Amap
//                } else {
//                    mDialogUtils.Toast(R.string.Label_SettingFail);
//                }
//            });
        }
    };

    public void initData() {
        GlobalEventBus.getBus().register(this);

        mViewBinding.rcCustomKeyView.initC1C2Event();
        switchControlEnable();
        getRCControlValue();

//        mRcCalibrationHelper = new RcCalibrationHelper(getActivity(), mViewBinding.rcCalibrationLayout);
    }

    private void getRCControlValue(){
//        GduApplication.getSingleApp().gduCommunication.getRCControl((code, bean) -> {
//            if(code == GduConfig.OK && bean.frameContent != null && bean.frameContent.length > 2){
//                int value = ByteUtilsLowBefore.byte2Int(bean.frameContent,2);
//                isRCControlCamera = (value == 1);
//                uiThreadHandle(() -> mViewBinding.ivBolunSetting.setSelected(isRCControlCamera));
//            }
//        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void commonEventMsgHandle(EventMessage msg) {
        MyLogUtils.i("commonEventMsgHandle() msgType = " + msg.getMsgType());
        boolean isUpload = msg.getMsgType() == MyConstants.GET_DEV_SN_SUC && isClickConnect
                && GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess;
        if (isUpload) {
            isClickConnect = false;
            uploadNetworkingInfo(requireContext());
        } else if (msg.getMsgType() == MyConstants.GET_DEV_SN_FAIL && isClickConnect) {
            isClickConnect = false;
        }
    }

    /**
     * 上报组网(配对)信息
     * @param context
     */
    public void uploadNetworkingInfo(Context context) {
        MyLogUtils.i("uploadNetworkingInfo()");
        boolean isNotUpload = !NetWorkUtils.checkNetwork(context)
                || GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess
                || StringUtils.isEmptyString(GlobalVariable.SN);
        MyLogUtils.i("uploadNetworkingInfo() connStateEnum = " + GlobalVariable.connStateEnum + "; GlobalVariableSN = " + GlobalVariable.SN
                + "; isNotUpload = " + isNotUpload);
        if (isNotUpload) {
            return;
        }
//        if (mFlightRecordService == null) {
//            mFlightRecordService = RetrofitClient.getApiPLM(FlightRecordService.class);
//        }
        long curTime = System.currentTimeMillis();
        long curTimeSecond = curTime / 1000;

        HashMap<String, Object> mParams = new HashMap<>();

        // 遥控器SN
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mParams.put("nestSn", Build.getSerial());
//        }
//        mParams.put("uavSn", GlobalVariable.SN);
//        mParams.put("composeTime", curTime);
//        mParams.put("longitude", String.valueOf(GlobalVariable.GPS_Lon));
//        mParams.put("latitude", String.valueOf(GlobalVariable.GPS_Lat));
//        mParams.put("time", curTime);
//        String upBeanJsonStr = new Gson().toJson(mParams);
//        RequestBody contentBody = FormBody.create(upBeanJsonStr, MediaType.parse("application/json; charset=utf-8"));
//        String contentStr = MyConstants.S220_API_REQUEST_KEY + "#" + curTimeSecond + "#" + upBeanJsonStr + "#";
//        String signMd5 = MD5Util.stringToMD5(contentStr);
//        String url = WebUrlConfig.S220_UPLOAD_URL_AND_PORT + WebUrlConfig.SEND_NEST_COMPOSE;
//        MyLogUtils.i("uploadNetworkingInfo() url = " + url);
//        mFlightRecordService.sendCustomUrlAndBody(signMd5, String.valueOf(curTimeSecond), url, contentBody)
//                .subscribeOn(Schedulers.newThread())
//                .to(RxLife.toMain(this))
//                .subscribe(data -> {
//                    final boolean isUnEmptyData = data != null && (data.getCode() == 0 || data.getCode() == 200);
//                    MyLogUtils.i("uploadNetworkingInfo() isUnEmptyData = " + isUnEmptyData);
//                }, throwable -> {
//                    MyLogUtils.e("上报组网信息出错", throwable);
//                });
    }

    public View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_back:
                    updateBackView();
                    break;
                case R.id.tv_rc_model:
                    setSecondLevelView(mViewBinding.controlHandView, true,getString(R.string.rc_direction_schematically));
                    currentSecondLevelType = 2;
                    mViewBinding.controlHandView.setControlHandPic();
                    break;
                case R.id.tv_rc_custom_key_view:

                    if (GlobalVariable.isRCSEE) {
                        setSecondLevelView(mViewBinding.rcCustomKeyNewView, true, getString(R.string.rc_custom_key));
                        currentSecondLevelType = 4;
                    } else {
                        setSecondLevelView(mViewBinding.rcCustomKeyView, true, getString(R.string.rc_custom_key));
                        currentSecondLevelType = 3;
                    }
                    break;
                case R.id.tv_rc_match:
                    rcMatch();
                    break;
                case R.id.tv_networking_model:
                case R.id.tv_networking_label:
                    if (GlobalVariable.droneFlyState == 1) {
                        setSecondLevelView(mViewBinding.advancedNetworkingView, true, getString(R.string.advanced_networking_mode));
                        currentSecondLevelType = 1;
                    } else {
                        Toast.makeText(getContext(), R.string.rtk_forbid_change, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.iv_bolun_setting:
                    boolean isSelect = mViewBinding.ivBolunSetting.isSelected();
//                    GduApplication.getSingleApp().gduCommunication.setRCControl(!isSelect, new SocketCallBack3() {
//                        @Override
//                        public void callBack(int code, GduFrame3 bean) {
//                            if(code == GduConfig.OK){
//                                Toaster.show(R.string.Label_SettingSuccess);
//                                isRCControlCamera = !isSelect;
//                                uiThreadHandle(() -> mViewBinding.ivBolunSetting.setSelected(isRCControlCamera));
//                            }
//                        }
//                    });
                    break;
                case R.id.tv_rc_control_check://遥控器校准
                    if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                        setSecondLevelView(mViewBinding.rcCalibrationLayout, true, getString(R.string.string_remote_cortrol_check));
//                        mRcCalibrationHelper.startMediumCalibration();
                        currentSecondLevelType = 5;
                    } else {
                        Toast.makeText(getContext(), R.string.string_unable_calibration_rocker, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void switchControlEnable() {
        mViewBinding.controlHandView.switchControlEnable();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void deviceConnChange(EventConnState event) {
        switchControlEnable();
        boolean isShow = isClickConnect && event.connStateEnum == ConnStateEnum.Conn_Sucess;
        if (isShow) {
            GlobalEventBus.getBus().post(new GimbalEvent(GlobalVariable.gimbalType));
            GlobalEventBus.getBus().post(new EventMessage(MyConstants.GET_CONTROL_PERMISSION_SUC));
//            uiThreadHandle(() -> showToast(R.string.match_success));
//            AirlinkUtils.getUnique();
        }
    }

    private void updateBackView() {
        if (currentSecondLevelType == 1) {
            setSecondLevelView(mViewBinding.advancedNetworkingView, false,"");
        } else if (currentSecondLevelType == 2) {
            setSecondLevelView(mViewBinding.controlHandView, false, "");
        } else if (currentSecondLevelType == 3) {
            setSecondLevelView(mViewBinding.rcCustomKeyView, false, "");
        } else if (currentSecondLevelType == 4) {
            setSecondLevelView(mViewBinding.rcCustomKeyNewView, false, "");
        } else if (currentSecondLevelType == 5){
            setSecondLevelView(mViewBinding.rcCalibrationLayout, false, "");
//            mRcCalibrationHelper.endCalibration();
        }
        currentSecondLevelType = 0;
    }

    private void setSecondLevelView(View view, boolean show, String title) {

//        MyAnimationUtils.animatorRightInOut(view, show);
        if (show) {
            mViewBinding.ivBack.setVisibility(View.VISIBLE);
            mViewBinding.tvTitle.setText(title);
        } else {
            mViewBinding.ivBack.setVisibility(View.GONE);
            mViewBinding.tvTitle.setText(R.string.title_control);
        }
    }

    private void rcMatch() {
        if (GlobalVariable.droneFlyState != 1 && GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            Toast.makeText(getContext(), R.string.Label_CannotMatchRc, Toast.LENGTH_SHORT).show();
            return;
        }

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess && GlobalVariable.isUseBackupsAirlink) {
            Toast.makeText(getContext(), R.string.string_plz_switch_image_transmission_match, Toast.LENGTH_SHORT).show();
            return;
        }

//        GeneralDialog dialog = new GeneralDialog(getContext(), R.style.NormalDialog) {
//            @Override
//            public void positiveOnClick() {
//                confirmMatch();
//                dismiss();
//            }
//
//            @Override
//            public void negativeOnClick() {
//                this.dismiss();
//            }
//        };
//        String content = getString(R.string.match_content);
//        dialog.setTitleText(getString(R.string.match_title));
//        dialog.setContentText(content);
//        dialog.show();
    }

    private void confirmMatch() {
//        GduApplication.getSingleApp().gduCommunication.setNetworking((byte) 0x08, (byte) 0x01, (code, bean) -> {
//            uiThreadHandle(() -> {
//                if (code == GduConfig.OK) {
//                    isClickConnect = true;
//                    Toaster.show(R.string.match_commond_success);
//                } else {
//                    Toaster.show(R.string.match_commond_faile);
//                }
//            });
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalEventBus.getBus().unregister(this);
//        if(mRcCalibrationHelper != null){
//            mRcCalibrationHelper.removeCallback();
//            mRcCalibrationHelper = null;
//        }
    }

    public static SettingRControlFragment newInstance() {
        Bundle args = new Bundle();
        SettingRControlFragment fragment = new SettingRControlFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
