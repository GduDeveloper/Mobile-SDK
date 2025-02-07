package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.api.GduRtkManager;
import com.gdu.api.rtk.QxSdkManager;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingRtkBinding;
import com.gdu.drone.PlanType;
import com.gdu.drone.RTKNetConnectStatus;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.util.ChannelUtils;
import com.gdu.util.LanguageUtil;
import com.gdu.util.NetWorkUtils;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.RonLog2FileRTK;
import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingRtkFragment extends Fragment {


    /**
     * 当前显示二级页面类型
     */
    private int currentSecondLevelType = 0;

    private FragmentSettingRtkBinding binding;

    private GduRtkManager mGduRtkManager;
    private Handler mHandler;
    private String[] mRTKServiceList;
    private int mLastRTKType;

    private String mIP;
    private String mPort;
    private String mMountPoint;
    private String mAccount;
    private String mPassword;

    private static final int RTK_TYPE_SET_SUCCEED = 100;
    private static final int RTK_TYPE_SET_FAILED = 101;
    private static final int SEND_CONNECT_DRONE_RTK_SUCCEED = 102;
    private static final int SEND_CONNECT_DRONE_RTK_FAILED = 103;
    private static final int GGA_DATA_UPDATE = 104;

    private long lastChangeRtkTime = 0L;

    private Disposable mDisposable;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingRtkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
        initHandler();

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(this))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long aLong) {
                        updateRtkStatus();
                        updateDRTKState();
                        if (binding.rtkStateView.getVisibility() == View.VISIBLE) {
                            binding.rtkStateView.updateRTKInfo();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateDRTKState() {
        if (GlobalVariable.sRTKType == 2 ) {
            if (GlobalVariable.drtkCompID == 0) {
                binding.tvDrtkState.setText(R.string.flight_loast_connect);
            }else {
                binding.tvDrtkState.setText(R.string.flight_connect);
            }

            if (GlobalVariable.drtkInformation != null) {
                binding.layoutDrtkInfo.tvInBatteryVol.setText(String.valueOf(GlobalVariable.drtkInformation.batteryVoltageIn));
                binding.layoutDrtkInfo.tvInBatteryCap.setText(String.valueOf(GlobalVariable.drtkInformation.batteryProgressIn));
                binding.layoutDrtkInfo.tvOutBatteryOneVol.setText(String.valueOf(GlobalVariable.drtkInformation.batteryVoltageOutOne));
                binding.layoutDrtkInfo.tvOutBatteryOneCap.setText(String.valueOf(GlobalVariable.drtkInformation.batteryProgressOutOne));
                binding.layoutDrtkInfo.tvOutBatteryTwoVol.setText(String.valueOf(GlobalVariable.drtkInformation.batteryVoltageOutTwo));
                binding.layoutDrtkInfo.tvOutBatteryTwoCap.setText(String.valueOf(GlobalVariable.drtkInformation.batteryProgressOutTwo));
                binding.layoutDrtkInfo.tvSdState.setText(GlobalVariable.drtkInformation.sdCardState == 1 ? R.string.string_insert : R.string.string_not_insert);
                binding.layoutDrtkInfo.tvTypeCState.setText(GlobalVariable.drtkInformation.typeCState == 1 ? R.string.string_insert : R.string.string_not_insert);

                binding.tvDrtkLon.setText(String.valueOf(GlobalVariable.drtkInformation.lonSatelliteSetValue));
                binding.tvDrtkLat.setText(String.valueOf(GlobalVariable.drtkInformation.latSatelliteSetValue));
                binding.tvDrtkHeight.setText(String.valueOf(GlobalVariable.drtkInformation.heightSatelliteSetValue));

                binding.tvExistDrtkLon.setText(String.valueOf(GlobalVariable.drtkInformation.lonSatelliteSetValue));
                binding.tvExistDrtkLat.setText(String.valueOf(GlobalVariable.drtkInformation.latSatelliteSetValue));
                binding.tvExistDrtkHeight.setText(String.valueOf(GlobalVariable.drtkInformation.heightSatelliteSetValue));
            } else {
                binding.layoutDrtkInfo.tvInBatteryVol.setText("--");
                binding.layoutDrtkInfo.tvInBatteryCap.setText("--");
                binding.layoutDrtkInfo.tvOutBatteryOneVol.setText("--");
                binding.layoutDrtkInfo.tvOutBatteryOneCap.setText("--");
                binding.layoutDrtkInfo.tvOutBatteryTwoVol.setText("--");
                binding.layoutDrtkInfo.tvOutBatteryTwoCap.setText("--");
                binding.layoutDrtkInfo.tvSdState.setText(R.string.string_not_insert);
                binding.layoutDrtkInfo.tvTypeCState.setText(R.string.string_not_insert);

                binding.tvDrtkLon.setText("--");
                binding.tvDrtkLat.setText("--");
                binding.tvDrtkHeight.setText("--");

                binding.tvExistDrtkLon.setText("--");
                binding.tvExistDrtkLat.setText("--");
                binding.tvExistDrtkHeight.setText("--");
            }
        }
    }



    private void initView() {
        mGduRtkManager = GduRtkManager.getInstance();
        initHandler();

        if (UavStaticVar.isOpenTextEnvironment) {
            binding.rlRtkHelp.setVisibility(View.VISIBLE);
        } else {
            binding.rlRtkHelp.setVisibility(View.GONE);
        }

        if (ChannelUtils.isQxChannel(getContext())) {
            binding.ipAddressEdit.setText("192.168.10.1");
            binding.portEdit.setText("9010");
            binding.mountPointEdit.setText("DL8");
            binding.accountEdit.setText("QXYX4");
            binding.passwordEdit.setText("QXYX4");
        }

        MyLogUtils.i("initListener()");
        binding.ivBack.setOnClickListener(listener);
        binding.rtkSwitchView.setOnClickListener(listener);
        binding.tvConnect.setOnClickListener(listener);
        binding.tvBreak.setOnClickListener(listener);
        binding.tvOpenRtkHelp.setOnClickListener(listener);
        binding.tvCloseRtkHelp.setOnClickListener(listener);
        binding.rtkSwitchView.setOnClickListener(listener);
        binding.tvRtkPermanentOpenSwitch.setOnClickListener(listener);
        binding.tvRtkPermanentCloseSwitch.setOnClickListener(listener);
        binding.layoutDrtkState.setOnClickListener(listener);

        binding.layoutDrtkInfo.ivDrtkLight.setOnClickListener(listener);

        binding.layoutDrtkInfo.tvCloseDrtk.setOnClickListener(listener);

        binding.layoutDrtkInfo.layoutSeniorSetting.setOnClickListener(listener);
        binding.tvEdit.setOnClickListener(listener);

        binding.tvOtherStation.setOnClickListener(listener);

        binding.tvOtherStation.setOnClickListener(listener);
        binding.tvConfirm.setOnClickListener(listener);
        binding.tvCancel.setOnClickListener(listener);
        binding.layoutDrtkInfo.tvFindStation.setOnClickListener(listener);


        if (GlobalVariable.sRTKType == 2) {
            if (GlobalVariable.drtkInformation != null) {
                binding.layoutDrtkInfo.ivDrtkLight.setSelected(GlobalVariable.drtkInformation.lightState == 0x01);
            }
        }

        mGduRtkManager.setOnRtkListener(new GduRtkManager.OnRtkListener() {
//            @Override
//            public void onGgaGot(byte[] data) {
//                MyLogUtils.i("mGduRtkManager onGgaGot()");
//                mHandler.sendEmptyMessage(GGA_DATA_UPDATE);
//            }

            @Override
            public void onServerConnected() {
                MyLogUtils.i("mGduRtkManager onServerConnected()");
                mHandler.sendEmptyMessage(RTKNetConnectStatus.SERVER_CONNECTED.getKey());
            }

            @Override
            public void onReconnect() {
                MyLogUtils.i("mGduRtkManager onReconnect()");
                mHandler.sendEmptyMessage(RTKNetConnectStatus.CONNECTING.getKey());
            }

            @Override
            public void onCommunicate() {
                MyLogUtils.i("mGduRtkManager onCommunicate()");
                mHandler.sendEmptyMessage(RTKNetConnectStatus.SERVER_COMMUNICATE.getKey());
            }

            @Override
            public void onDisConnect() {
                MyLogUtils.i("mGduRtkManager onDisConnect()");
                mHandler.sendEmptyMessage(RTKNetConnectStatus.DISCONNECT.getKey());
            }

            @Override
            public void onConnectFailed(RTKNetConnectStatus status) {
                MyLogUtils.i("mGduRtkManager onConnectFailed() status = " + status);
                mHandler.sendEmptyMessage(RTKNetConnectStatus.CONNECT_FAILED.getKey());
            }

//            @Override
//            public void onRTCMGot(byte[] data) {
//                MyLogUtils.i("mGduRtkManager onRTCMGot()");
//            }
//
//            @Override
//            public void onboardStatesChange(int state) {
//            }
        });
        binding.rtkServiceView.setOnOptionClickListener((parentId, view, position) -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {   //未连接飞机
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }
            setRTKType(position);
        });


        if (LanguageUtil.getLocal(requireContext()).getLanguage().equals("ru")) {
            binding.tvConnect.setTextSize(12);
            binding.tvConnectState.setTextSize(14);
            binding.rtkServiceView.setTextSize(14);
        } else  if (LanguageUtil.getLocal(requireContext()).getLanguage().equals("en")) {
            binding.tvConnect.setTextSize(12);
            binding.tvConnectState.setTextSize(14);
            binding.rtkServiceView.setTextSize(12);
        }

    }

    private void initData() {
        MyLogUtils.i("initData()");
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            GlobalVariable.sRTKType = 1;
            GlobalVariable.sDroneRTKStatus = 0;
            GlobalVariable.sPhoneRTKStatus = 0;
            GlobalVariable.sBSRTKStatus = 0;
        }

        boolean isOpen = SPUtils.getTrueBoolean(requireContext(), SPUtils.RTK_SWITCH);
        if (isOpen) {
            binding.rtkSwitchView.setSelected(true);
            binding.rlSelectRtkType.setVisibility(View.VISIBLE);
            binding.rtkStateView.setVisibility(View.VISIBLE);
            binding.rtkParamLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rtkSwitchView.setSelected(false);
            binding.rlSelectRtkType.setVisibility(View.GONE);
            binding.rtkStateView.setVisibility(View.GONE);
            binding.rtkParamLayout.setVisibility(View.GONE);
        }
        if (ChannelUtils.isQxChannel(getContext())) {
            mRTKServiceList = getResources().getStringArray(R.array.rtk_service_array_qx);
        } else {
            mRTKServiceList = getResources().getStringArray(R.array.rtk_service_array_onboard);
        }
        binding.rtkServiceView.setData(mRTKServiceList);

        mLastRTKType = GlobalVariable.sRTKType;
        if (isOpen) {
            setRTKTypeView((byte) mLastRTKType);
            switch (mLastRTKType) {
                case 0:
                case 1:
                    binding.rtkServiceView.setIndex(0);
                    binding.rtkStateView.setStationName(mRTKServiceList[0]);
                    binding.rtkStateView.setVisibility(View.VISIBLE);
                    binding.layoutDrtkState.setVisibility(View.GONE);
                    binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                    showConnectedStatus();
                    break;
                case 2:
                    binding.rtkServiceView.setIndex(1);
                    binding.rtkStateView.setStationName(mRTKServiceList[1]);
                    binding.layoutDrtkState.setVisibility(View.VISIBLE);
                    binding.layoutDrtkInfo.rootView.setVisibility(View.VISIBLE);
                    showNotConnectedStatus();
                    break;
                case 3:
                    binding.rtkServiceView.setIndex(2);
                    binding.rtkStateView.setStationName(mRTKServiceList[2]);
                    binding.rtkStateView.setVisibility(View.VISIBLE);
                    binding.layoutDrtkState.setVisibility(View.GONE);
                    binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                    showOnboardRtkConnect();
                    break;
                case 5:
                    if (ChannelUtils.isQxChannel(getContext())) {
                        binding.rtkServiceView.setIndex(3);
                        binding.rtkStateView.setStationName(mRTKServiceList[3]);

                        binding.rtkStateView.setVisibility(View.VISIBLE);
                        binding.layoutDrtkState.setVisibility(View.GONE);
                        binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                        showQxSdkRtkConnect(QxSdkManager.getInstance().getRTKState(), QxSdkManager.getInstance().getQxErrorCode());
                        break;
                    } else {
                        binding.rtkServiceView.setIndex(0);
                        binding.rtkStateView.setStationName(mRTKServiceList[0]);
                        binding.rtkStateView.setVisibility(View.VISIBLE);
                        binding.layoutDrtkState.setVisibility(View.GONE);
                        binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                        showConnectedStatus();
                        setRTKType(0);
                    }
                default:
                    binding.rtkServiceView.setIndex(0);
                    binding.rtkStateView.setStationName(mRTKServiceList[0]);
                    showConnectedStatus();
                    setRTKType(0);
                    break;
            }
        }

        initParam();
        updateBDTips();

    }

    public void updateRtkStatus() {

        if (System.currentTimeMillis() - lastChangeRtkTime < 2000) {
            return;
        }
        if (getContext() != null) {
            if (GlobalVariable.sRTKType == 1) {
                showConnectedStatus();
            } else if (GlobalVariable.sRTKType == 2) {
                binding.tvConnectState.setVisibility(View.GONE);
                binding.tvConnect.setVisibility(View.GONE);
                binding.tvBreak.setVisibility(View.GONE);
                binding.rtkParamLayout.setVisibility(View.GONE);
            } else if (GlobalVariable.sRTKType == 3) {
                showOnboardRtkConnect();
            } else if (GlobalVariable.sRTKType == 5) {
                showQxSdkRtkConnect(QxSdkManager.getInstance().getRTKState(),
                        QxSdkManager.getInstance().getQxErrorCode());
            }
        }
        updateBDTips();
    }


    private void initHandler() {
        MyLogUtils.i("initHandler()");
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int status = msg.what;
                if (status == RTKNetConnectStatus.SERVER_CONNECTED.getKey()) {
                    showConnectedStatus();
                } else if (status == RTKNetConnectStatus.SERVER_COMMUNICATE.getKey()) {
                    showConnectedStatus();
                } else if (status == RTKNetConnectStatus.CONNECT_FAILED.getKey()) {
                    showNotConnectedStatus();
                } else if (status == RTKNetConnectStatus.DISCONNECT.getKey()) {
                    showNotConnectedStatus();
                } else if (status == RTKNetConnectStatus.CONNECTING.getKey()) {
                    showConnectedStatus();
                } else if (status == RTK_TYPE_SET_SUCCEED) {
                    setRTKTypeView((Byte) msg.obj);
                    Toast.makeText(getContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                } else if (status == RTK_TYPE_SET_FAILED) {
                    setRTKTypeFailed();
                    Toast.makeText(getContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                } else if (status == SEND_CONNECT_DRONE_RTK_SUCCEED) {

                } else if (status == SEND_CONNECT_DRONE_RTK_FAILED) {
                    showNotConnectedStatus();
                }
            }
        };
    }

    /**
     * 显示连接的状态
     */
    private void showConnectedStatus() {
//        MyLogUtils.i("showConnectedStatus()");
        RTKNetConnectStatus status = mGduRtkManager.getConnectStatus();
        if (status == RTKNetConnectStatus.SERVER_CONNECTED || status == RTKNetConnectStatus.CONNECTING) {
            showConnectingView();
        } else if (status == RTKNetConnectStatus.SERVER_COMMUNICATE) {
            showConnectedView();
        } else {
            showNotConnectedStatus();
        }
    }

    /**
     * 显示连接中状态
     */
    private void showConnectingView() {
        if (getContext() == null) {
            return;
        }
        MyLogUtils.i("showConnectingView()");
        binding.tvConnectState.setText(getString(R.string.connecting));
        binding.tvConnectState.setTextColor(getResources().getColor(R.color.color_FF4E00));
        binding.tvConnectState.setVisibility(View.VISIBLE);
        binding.tvConnect.setVisibility(View.GONE);
        binding.tvBreak.setVisibility(View.VISIBLE);
    }


    /**
     * 显示已连接状态
     */
    private void showConnectedView() {

        if (getContext() == null) {
            return;
        }
        MyLogUtils.i("showConnectedView()");
        if (GlobalVariable.sRTKType == 1 && GlobalVariable.rtkIsLoading == 1) {
            binding.tvConnectState.setText(getString(R.string.string_converging));
        } else {
            binding.tvConnectState.setText(getString(R.string.connect_succeed));
        }
        binding.tvConnectState.setTextColor(getResources().getColor(R.color.color_47E943));
        binding.tvConnectState.setVisibility(View.VISIBLE);
        binding.tvConnect.setVisibility(View.GONE);
        binding.tvBreak.setVisibility(View.VISIBLE);
    }

    /**
     * 显示未连接的状态
     */
    private void showNotConnectedStatus() {
        if (getContext() == null) {
            return;
        }
        binding.tvConnectState.setVisibility(View.GONE);
        if (GlobalVariable.sRTKType == 2) {
            binding.tvConnect.setVisibility(View.GONE);
        } else {
            binding.tvConnect.setVisibility(View.VISIBLE);
        }
        binding.tvBreak.setVisibility(View.GONE);
    }





    /**
     * 初始化登陆参数
     */
    private void initParam() {
        MyLogUtils.i("initParam()");
        String ip = SPUtils.getString(getContext(), SPUtils.RTK_IP);
        if (!StringUtils.isEmptyString(ip)) {
            binding.ipAddressEdit.setText(ip);
        } else {
            binding.ipAddressEdit.setText("rtk.ntrip.qxwz.com");
        }
        String port = SPUtils.getString(getContext(), SPUtils.RTK_PORT);
        if (!StringUtils.isEmptyString(port)) {
            binding.portEdit.setText(port);
        } else {
            binding.portEdit.setText("8002");
        }
        String account = SPUtils.getString(getContext(), SPUtils.RTK_ACCOUNT);
        if (!StringUtils.isEmptyString(account)) {
            binding.accountEdit.setText(account);
        }
        String password = SPUtils.getString(getContext(), SPUtils.RTK_PASSWORD);
        if (!StringUtils.isEmptyString(password)) {
            binding.passwordEdit.setText(password);
        }
        String mp = SPUtils.getString(getContext(), SPUtils.RTK_MOUNT_POINT);
        if (!StringUtils.isEmptyString(mp)) {
            binding.mountPointEdit.setText(mp);
        } else {
            binding.mountPointEdit.setText("AUTO");
        }
    }

    private void showOnboardRtkConnect() {
        if (mLastRTKType == 3) {
            if (GlobalVariable.onDroneRtkState == 2) {
                showConnectedView();
            } else if (GlobalVariable.onDroneRtkState == 1) {
                showConnectingView();
            } else {
                showNotConnectedStatus();
            }
        }
    }

    private void showQxSdkRtkConnect(int state, int qxErrorCode) {

        if (mLastRTKType != 5) {
            return;
        }

        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            binding.tvConnectState.setVisibility(View.GONE);
            binding.tvConnect.setVisibility(View.VISIBLE);
            binding.tvBreak.setVisibility(View.GONE);
            return;
        }

        if (state == 0) {
            binding.tvConnectState.setVisibility(View.GONE);
            binding.tvConnect.setVisibility(View.VISIBLE);
            binding.tvBreak.setVisibility(View.GONE);
        } else if (state == 1) {
            binding.tvConnectState.setVisibility(View.VISIBLE);
            binding.tvConnectState.setTextColor(getResources().getColor(R.color.color_FF4E00));
            binding.tvConnectState.setText(getString(R.string.connecting));
            binding.tvConnect.setVisibility(View.GONE);
            binding.tvBreak.setVisibility(View.VISIBLE);
        } else if (state == 9 || state == 11) {
            binding.tvConnectState.setVisibility(View.VISIBLE);
            binding.tvConnectState.setTextColor(getResources().getColor(R.color.color_47E943));
            if (GlobalVariable.rtkIsLoading == 1) {
                binding.tvConnectState.setText(getString(R.string.string_converging));
            } else {
                binding.tvConnectState.setText(getString(R.string.connect_succeed));
            }
            binding.tvConnect.setVisibility(View.GONE);
            binding.tvBreak.setVisibility(View.VISIBLE);
        } else {
            binding.tvConnectState.setVisibility(View.VISIBLE);
            binding.tvConnectState.setTextColor(getResources().getColor(R.color.color_FF4E00));
            binding.tvConnectState.setText(getQxErrStr(state, qxErrorCode));
            binding.tvConnect.setVisibility(View.VISIBLE);
            binding.tvBreak.setVisibility(View.GONE);
        }
    }

    private String getQxErrStr(int state, int qxErrorCode) {

        String error = "";
        if (getContext() == null) {
            return error;
        }
        switch (state) {
            case 2:
            case 3:
                error = getContext().getString(R.string.string_not_dsk);
                break;
            case 4:
                error = getContext().getString(R.string.string_auth_fail);
                break;
            case 5:
                error = getContext().getString(R.string.string_active_fail);
                break;
            case 7:
                error = getContext().getString(R.string.string_startup_fail);
                break;
            case 8:
                error = getContext().getString(R.string.string_not_allow_connect_rtk);
                break;
            case 10:
                error = getContext().getString(R.string.string_sdk_state_error) + ":" + qxErrorCode;
                break;
            case 12:
                error = getContext().getString(R.string.string_net_error);
                break;
            case 13:
                error = getContext().getString(R.string.string_rtk_state_error);
                break;
            default:
                break;
        }
        return error;
    }



    /**
     * 设置RTK类型
     *
     * @param position 0 手机网络  1基站 2 机载 3qxSDK
     */
    private void setRTKType(int position) {
        switch (position) {
            case 0:

                if (mLastRTKType == 5) {
                    QxSdkManager.getInstance().close();
                }
                if (mLastRTKType != 1) {
                    closeRTK();
                    setRTKType((byte) 1, (byte) 1);
                }
                break;
            case 1:
                if (mLastRTKType == 5) {
                    QxSdkManager.getInstance().close();
                }
                if (mLastRTKType != 2) {
                    closeRTK();
                    setRTKType((byte) 1, (byte) 2);
                }
                break;
            case 2:
                if (mLastRTKType == 5) {
                    QxSdkManager.getInstance().close();
                }
                if (!aircraftIsConnectNet()) {
                    Toast.makeText(getContext(), R.string.drone_not_connect_server, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mLastRTKType != 3) {
                    closeRTK();
                    setRTKType((byte) 0, (byte) 3);
                }
                break;
            case 3:
                if (mLastRTKType != 5) {
                    closeRTK();
                    setRTKType((byte) 1, (byte) 5);
                }
                break;
            default:
                break;
        }
    }

    public boolean aircraftIsConnectNet() {
        return GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus._4g_net_status != 0;
    }

    /**
     * 设置RTK类型
     *
     * @param open 0 关 1 开
     * @param type 0：无效值
     *             1: 手机网络版RTK
     *             2: 基站版RTK
     *             3: 飞机板网络RTK
     */
    private void setRTKType(byte open, byte type) {
        MyLogUtils.i("setRTKType() type = " + type);
//        GduApplication.getSingleApp().gduCommunication.setRtkType(open, type, null, (byte) 0, null, null, null, (code, bean) -> {
//            MyLogUtils.i("setRTKType callBack() code = " + code);
//            final boolean isError = code != GduConfig.OK || bean == null || bean.frameContent == null || bean.frameContent.length < 3 || bean.frameContent[2] != 0;
//            if (isError) {
//                mHandler.sendEmptyMessage(RTK_TYPE_SET_FAILED);
//                return;
//            }
//            mHandler.obtainMessage(RTK_TYPE_SET_SUCCEED, type).sendToTarget();
//        });
    }

    /**
     * 设置RTK成功
     *
     * @param type 1 手机网络   2 基站  3 机载RTk
     */
    private void setRTKTypeView(byte type) {
        if (getContext() == null ) {
            return;
        }
        lastChangeRtkTime = System.currentTimeMillis();
        RonLog2FileRTK.getSingle().saveData("RTK setRTkType =  " + type);
        mLastRTKType = type;
        switch (type) {
            case 0:
            case 1:
                binding.rtkServiceView.setIndex(0);
                binding.rtkStateView.setStationName(mRTKServiceList[0]);
                binding.tvConnect.setVisibility(View.VISIBLE);
                binding.tvConnectState.setVisibility(View.GONE);
                binding.tvBreak.setVisibility(View.GONE);
                binding.rtkParamLayout.setVisibility(View.VISIBLE);

                binding.rtkStateView.setVisibility(View.VISIBLE);
                binding.layoutDrtkState.setVisibility(View.GONE);
                binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                binding.rtkStateView.updateShowView(mLastRTKType);
                break;
            case 2:
                binding.rtkServiceView.setIndex(1);
                binding.rtkStateView.setStationName(mRTKServiceList[1]);
                binding.tvConnectState.setVisibility(View.GONE);
                binding.tvConnect.setVisibility(View.GONE);
                binding.tvBreak.setVisibility(View.GONE);
                binding.rtkParamLayout.setVisibility(View.GONE);

                binding.rtkStateView.setVisibility(View.VISIBLE);
                binding.layoutDrtkState.setVisibility(View.VISIBLE);
                binding.layoutDrtkInfo.rootView.setVisibility(View.VISIBLE);
                binding.rtkStateView.updateShowView(mLastRTKType);

                break;
            case 3:
                binding.rtkServiceView.setIndex(2);
                binding.rtkStateView.setStationName(mRTKServiceList[2]);
                binding.tvConnect.setVisibility(View.VISIBLE);
                binding.tvConnectState.setVisibility(View.GONE);
                binding.tvBreak.setVisibility(View.GONE);
                binding.rtkParamLayout.setVisibility(View.VISIBLE);

                binding.rtkStateView.setVisibility(View.VISIBLE);
                binding.layoutDrtkState.setVisibility(View.GONE);
                binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                binding.rtkStateView.updateShowView(mLastRTKType);
                break;
            case 5:
                if (mRTKServiceList.length > 3) {
                    binding.rtkServiceView.setIndex(3);
                    binding.rtkStateView.setStationName(mRTKServiceList[3]);
                } else {
                    binding.rtkServiceView.setIndex(0);
                    binding.rtkStateView.setStationName(mRTKServiceList[0]);
                }
                binding.tvConnect.setVisibility(View.VISIBLE);
                binding.tvConnectState.setVisibility(View.GONE);
                binding.tvBreak.setVisibility(View.GONE);
                binding.rtkParamLayout.setVisibility(View.GONE);

                binding.rtkStateView.setVisibility(View.VISIBLE);
                binding.layoutDrtkState.setVisibility(View.GONE);
                binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
                binding.rtkStateView.updateShowView(mLastRTKType);
                break;
            default:
                break;
        }
    }

    /**
     * 设置RTK失败
     */
    private void setRTKTypeFailed() {
        if (isAdded() && getContext() != null) {
            setRTKTypeView((byte) mLastRTKType);
        }
    }

    private void closeRTK() {
//        if (mGduRtkManager != null) {
//            mGduRtkManager.close();
//        }
    }

    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.iv_back:
                    updateBackView();
                    break;
                case R.id.rtk_switch_view:
                    setRtkSwitch();
                    break;

                case R.id.tv_connect:
                    confirmRTK();
                    break;

                case R.id.tv_break:
                    breakRTK();
                    break;

                case R.id.tv_open_rtk_help:
                    changRtkHelp((byte) 1);
                    break;

                case R.id.tv_close_rtk_help:
                    changRtkHelp((byte) 0);
                    break;

                case R.id.tvRtkPermanentOpenSwitch:
                    changRtkHelp((byte) 5);
                    break;

                case R.id.tvRtkPermanentCloseSwitch:
                    changRtkHelp((byte) 4);
                    break;
                case R.id.layout_drtk_state:
                    currentSecondLevelType = 1;
                    setSecondLevelView(binding.layoutSelectedRtk, true, getString(R.string.string_selected_drtk));
                    break;
                case R.id.layout_senior_setting:
                    currentSecondLevelType = 2;
                    setSecondLevelView(binding.layoutDrtkSeniorSetting, true, getString(R.string.advanced_settings));
                    break;
                case R.id.tv_edit:
                    currentSecondLevelType = 3;
                    setSecondLevelView(binding.layoutDrtkEdit, true, getString(R.string.string_change_drtk_coor));
                    break;
                case R.id.tv_other_station:
//                    DialogUtil.showPwdDlg(getActivity(), R.string.pls_input_pwd, (dialog, pwd) ->{
//                                if (TextUtils.isEmpty(pwd)) {
//                                    ToastUtil.show(R.string.please_input_value);
//                                    return;
//                                }
//                                connectDRTK(pwd);
//                            });
                    break;
                case R.id.tv_close_drtk:
                    closeDrtk();
                    break;
                case R.id.iv_drtk_light:
                    changDRTKLight();
                    break;
                case R.id.tv_confirm:
                    changDRTKStationCoordinates();
                    break;
                case R.id.tv_cancel:
                    updateBackView();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     *  设置基站坐标
     */
    private void changDRTKStationCoordinates() {

        if (TextUtils.isEmpty(binding.tvEditDrtkLon.getText())) {
            return;
        }
        if (TextUtils.isEmpty(binding.tvEditDrtkLat.getText())) {
            return;
        }
        if (TextUtils.isEmpty(binding.tvEditDrtkHeight.getText())) {
            return;
        }

        try {
            Double lon = Double.parseDouble(binding.tvEditDrtkLon.getText().toString());
            Double lat = Double.parseDouble(binding.tvEditDrtkLat.getText().toString());
            Double height = Double.parseDouble(binding.tvEditDrtkHeight.getText().toString());
            MyLogUtils.d("changDRTK lon = " + lon + ", lat = " + lat + ",height = " + height);
//            GduApplication.getSingleApp().gduCommunication.setDrtkStationCoordinates(lon, lat, height, new SocketCallBack3() {
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//
//                    if (mHandler != null && isAdded()) {
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bean != null && bean.frameContent != null) {
//                                    byte result = bean.frameContent[4];
//                                    if (result == 0x01) {
//                                        Toaster.show(getString(R.string.Label_SettingSuccess));
//                                    } else{
//                                        Toaster.show(getString(R.string.Label_SettingFail));
//                                    }
//                                } else {
//                                    Toaster.show(getString(R.string.Label_SettingFail));
//                                }
//                            }
//                        });
//                    }
//                }
//            });
        } catch (Exception e) {

        }


    }

    private void closeDrtk() {

        if (GlobalVariable.drtkInformation != null) {
            MyLogUtils.d("closeDtrk id = " + GlobalVariable.drtkInformation.id);
//            GduApplication.getSingleApp().gduCommunication.closeDRTK(GlobalVariable.drtkInformation.id, new SocketCallBack3() {
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//
//                    if (mHandler != null&& isAdded() ) {
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bean != null && bean.frameContent != null) {
//                                    byte result = bean.frameContent[4];
//                                    if (result == 0x01) {
//                                        Toaster.show(getString(R.string.string_close_success));
//                                    } else {
//                                        Toaster.show(getString(R.string.string_close_fail));
//                                    }
//                                } else {
//                                    Toaster.show(getString(R.string.string_close_fail));
//                                }
//
//                            }
//                        });
//                    }
//                }
//            });
        } else {
            Toast.makeText(getContext(), R.string.string_drtk_not_connect, Toast.LENGTH_SHORT).show();
        }

    }

    private void changDRTKLight() {

        boolean isOpen = !binding.layoutDrtkInfo.ivDrtkLight.isSelected();
//        GduApplication.getSingleApp().gduCommunication.changDrtkLight(isOpen, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//
//                if (mHandler != null) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (bean != null && bean.frameContent != null && bean.frameContent.length > 4) {
//                                boolean result = bean.frameContent[4] == 0x01;
//                                if (result) {
//                                    binding.layoutDrtkInfo.ivDrtkLight.setSelected(isOpen);
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        });

    }

    private void connectDRTK(String passwordStr) {

        try {
            int password = Integer.parseInt(passwordStr);
            if (GlobalVariable.drtkInformation != null) {
                Short id = GlobalVariable.drtkInformation.id;
//                GduApplication.getSingleApp().gduCommunication.connectDrtk(id, password, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//
//                    }
//                });
            }
        } catch (Exception e) {

        }




    }

    private void updateBackView() {
        if (currentSecondLevelType == 1) {
            setSecondLevelView(binding.layoutSelectedRtk, false,"");
            currentSecondLevelType = 0;
        } else if (currentSecondLevelType == 2) {
            setSecondLevelView(binding.layoutDrtkSeniorSetting, false, "");
            currentSecondLevelType = 0;
        } else if (currentSecondLevelType == 3) {
//            MyAnimationUtils.animatorRightInOut(binding.layoutDrtkEdit, false);
            binding.ivBack.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(getString(R.string.advanced_settings));
            currentSecondLevelType = 2;
        }

    }


    private void setSecondLevelView(View view, boolean show, String title) {

//        MyAnimationUtils.animatorRightInOut(view, show);
        if (show) {
            binding.ivBack.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(title);
        } else {
            binding.ivBack.setVisibility(View.GONE);
            binding.tvTitle.setText(R.string.title_rtk);
        }
    }


    private void breakRTK() {
        // 千寻SDK
        if (mLastRTKType == 5) {
            QxSdkManager.getInstance().close();
        } else {
            closeRTK();
//            GduApplication.getSingleApp().gduCommunication.setRtkType((byte) 0, (byte) GlobalVariable.sRTKType, null, (byte) 0,
//                        null, null, null, (code, bean) -> {
//
//                        });
            if (GlobalVariable.planType == PlanType.S220
                    || GlobalVariable.planType == PlanType.S280
                    || GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220_SD
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S220BDS
                    || GlobalVariable.planType == PlanType.S280BDS
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS
                    || GlobalVariable.planType == PlanType.S220_SD_BDS
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {
//                GduApplication.getSingleApp().gduCommunication.changeRTKAccuracyKeep(false, (code, bean) -> {
//                });
            }
            SPUtils.put(getContext(), SPUtils.RTK_CONNECT_STATE, false);
        }
        lastChangeRtkTime = System.currentTimeMillis();
    }

    private void setRtkSwitch() {

        boolean isOpen = SPUtils.getTrueBoolean(getContext(), SPUtils.RTK_SWITCH);
        //当前开启则关闭
        if (isOpen) {
            binding.rtkSwitchView.setSelected(false);
            binding.rlSelectRtkType.setVisibility(View.GONE);
            binding.rtkStateView.setVisibility(View.GONE);
            binding.rtkParamLayout.setVisibility(View.GONE);

            binding.layoutDrtkState.setVisibility(View.GONE);
            binding.layoutDrtkInfo.rootView.setVisibility(View.GONE);
            closeRTK();
            QxSdkManager.getInstance().close();
            SPUtils.put(getContext(), SPUtils.RTK_SWITCH, false);
            if (GlobalVariable.planType == PlanType.S220
                    || GlobalVariable.planType == PlanType.S280
                    || GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220_SD
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S220BDS
                    || GlobalVariable.planType == PlanType.S280BDS
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS
                    || GlobalVariable.planType == PlanType.S220_SD_BDS
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {
//                GduApplication.getSingleApp().gduCommunication.changeRTKAccuracyKeep(false, (code, bean) -> {
//                });
            }
        } else {
            binding.rtkSwitchView.setSelected(true);
            binding.rlSelectRtkType.setVisibility(View.VISIBLE);
            binding.rtkStateView.setVisibility(View.VISIBLE);
            binding.rtkParamLayout.setVisibility(View.VISIBLE);

            if (mLastRTKType == 2) {
                binding.layoutDrtkState.setVisibility(View.VISIBLE);
                binding.layoutDrtkInfo.rootView.setVisibility(View.VISIBLE);
            }

            if (mLastRTKType == 1 || mLastRTKType == 3) {
                binding.rtkParamLayout.setVisibility(View.VISIBLE);
            } else {
                binding.rtkParamLayout.setVisibility(View.GONE);
            }
            SPUtils.put(getContext(), SPUtils.RTK_SWITCH, true);
        }

    }

    private void changRtkHelp(byte open) {
//        GduApplication.getSingleApp().gduCommunication.change482RtkStates(open, (code, bean) -> {
//            if (mHandler != null && isAdded()) {
//                if (getContext() != null) {
//                    mHandler.post(() -> {
//                        if (getContext() != null) {
//                            Toaster.show(getContext().getString(code == GduConfig.OK ?
//                                    R.string.Label_SettingSuccess : R.string.Label_SettingFail));
//                        }
//                    });
//                }
//            }
//        });
    }

    private void confirmRTK() {
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {   //未连接飞机
            Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CommonUtils.isSmallFlight(GlobalVariable.planType) && GlobalVariable.sGNSSType == 6) {//北斗模式下无法连接RTK
            Toast.makeText(getContext(), R.string.Msg_bds_connot_connect_rtk, Toast.LENGTH_SHORT).show();
            return;
        }


        boolean isHeight62 = false;
        if (!CommonUtils.isEmptyString(GlobalVariable.flyVersionStr) && GlobalVariable.flyVersionStr.contains(".")) {
            final String[] newVersion = CommonUtils.getVersionLongValue(GlobalVariable.flyVersionStr);
            final String[] oldVersion = CommonUtils.getVersionLongValue("0.0.61");
            if (newVersion == null || oldVersion == null || newVersion.length == 0) {
                isHeight62 = false;
            } else {
                isHeight62 = CommonUtils.judgeIsHaveNewVersion(oldVersion, newVersion);
            }
        }

        // 飞行中未fixed不能连接rtk
        if (!isHeight62 && GlobalVariable.droneFlyState != 1 && GlobalVariable.rtkIsLoading == 1) {
            Toast.makeText(getContext(), R.string.string_not_allow_connect_rtk, Toast.LENGTH_SHORT).show();
            return;
        }
        MyLogUtils.d("mLastRTKType  =" + mLastRTKType);

        if (mLastRTKType == 0 || mLastRTKType == 1) {
            if (GlobalVariable.planType == PlanType.S220
                    || GlobalVariable.planType == PlanType.S280
                    || GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220_SD
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S220BDS
                    || GlobalVariable.planType == PlanType.S280BDS
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS
                    || GlobalVariable.planType == PlanType.S220_SD_BDS
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {

                if (GlobalVariable.RTKOnline != 0) {
                    Toast.makeText(getContext(), R.string.String_no_rtk, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (mLastRTKType == 0) {
//            GduApplication.getSingleApp().gduCommunication.setRtkType((byte) 1, (byte) 1, "", (short) 0, "", "", "",
//                    new SocketCallBack3() {
//                        @Override
//                        public void callBack(int code, GduFrame3 bean) {
//
//                        }
//                    });
            confirmPhoneRTK();
        } else if (mLastRTKType == 1) {
            confirmPhoneRTK();
        } else if (mLastRTKType == 3) {
            confirmDroneRTK();
        } else if (mLastRTKType == 5) {
            confirmQXSdkRTK();
        }
    }

    private void confirmQXSdkRTK() {

        if (!NetworkingHelper.isRCHasControlPower()) {
            Toast.makeText(getContext(), R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetWorkUtils.checkNetwork(getContext())) {    //未连接网络
            Toast.makeText(getContext(), R.string.Label_Record_List_noNetwork, Toast.LENGTH_SHORT).show();
            return;
        }
        QxSdkManager.getInstance().startConnect(false);
    }


    /**
     * 连接飞机端RTK
     */
    private void confirmDroneRTK() {

        if (!aircraftIsConnectNet()) {
            Toast.makeText(getContext(), R.string.drone_not_connect_server, Toast.LENGTH_SHORT).show();
            return;
        }

        MyLogUtils.i("confirmDroneRTK()");
        binding.tvConnect.setVisibility(View.GONE);
        binding.tvConnectState.setVisibility(View.INVISIBLE);
        mIP =  binding.ipAddressEdit.getText().toString().trim();
        mPort = binding.portEdit.getText().toString().trim();
        mMountPoint = binding.mountPointEdit.getText().toString().trim();
        mAccount = binding.accountEdit.getText().toString().trim();
        mPassword = binding.passwordEdit.getText().toString().trim();
        final boolean haveEmptyData = CommonUtils.isEmptyString(mIP)
                || CommonUtils.isEmptyString(mPort)
                || CommonUtils.isEmptyString(mAccount)
                || CommonUtils.isEmptyString(mPassword)
                || CommonUtils.isEmptyString(mMountPoint);
        if (haveEmptyData) {
            Toast.makeText(getContext(), R.string.flight_error_7022, Toast.LENGTH_SHORT).show();
        } else {
            SPUtils.put(getContext(), SPUtils.RTK_IP, mIP);
            SPUtils.put(getContext(), SPUtils.RTK_PORT, mPort);
            SPUtils.put(getContext(), SPUtils.RTK_ACCOUNT, mAccount);
            SPUtils.put(getContext(), SPUtils.RTK_PASSWORD, mPassword);
            SPUtils.put(getContext(), SPUtils.RTK_MOUNT_POINT, mMountPoint);
            if (CommonUtils.isNumber(mPort)) {
                showConnectingView();

                short port = (short) Integer.parseInt(mPort);
//                GduApplication.getSingleApp().gduCommunication.setRtkType((byte) 1, (byte) 3, mIP, port, mMountPoint, mAccount, mPassword, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        if (code == GduConfig.OK) {
//                            mHandler.sendEmptyMessage(SEND_CONNECT_DRONE_RTK_SUCCEED);
//                        } else {
//                            mHandler.sendEmptyMessage(SEND_CONNECT_DRONE_RTK_FAILED);
//                        }
//                    }
//                });
            } else {
                Toast.makeText(getContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 连接手机端RTK
     */
    private void confirmPhoneRTK() {
        MyLogUtils.i("confirmPhoneRTK()");
        if (mGduRtkManager.getConnectStatus() == RTKNetConnectStatus.CONNECTED) {  //已连接千寻服务器
            Toast.makeText(getContext(), R.string.flight_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!NetWorkUtils.checkNetwork(getContext())) {    //未连接网络
            Toast.makeText(getContext(), R.string.Label_Record_List_noNetwork, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!NetworkingHelper.isRCHasControlPower()) {
            Toast.makeText(getContext(), R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
            return;
        }

        if (GlobalVariable.planType == PlanType.S220
                || GlobalVariable.planType == PlanType.S280
                || GlobalVariable.planType == PlanType.S200
                || GlobalVariable.planType == PlanType.S220Pro
                || GlobalVariable.planType == PlanType.S220ProS
                || GlobalVariable.planType == PlanType.S220ProH
                || GlobalVariable.planType == PlanType.S220_SD
                || GlobalVariable.planType == PlanType.S200_SD
                || GlobalVariable.planType == PlanType.S220BDS
                || GlobalVariable.planType == PlanType.S280BDS
                || GlobalVariable.planType == PlanType.S200BDS
                || GlobalVariable.planType == PlanType.S220ProBDS
                || GlobalVariable.planType == PlanType.S220ProSBDS
                || GlobalVariable.planType == PlanType.S220ProHBDS
                || GlobalVariable.planType == PlanType.S220_SD_BDS
                || GlobalVariable.planType == PlanType.S200_SD_BDS) {
//            GduApplication.getSingleApp().gduCommunication.changeRTKAccuracyKeep(true, (code, bean) -> {
//            });
        }
        //  482rtk
        if (GlobalVariable.rtkType == 3) {
            connect482Rtk();
            //  F9p rtk
        } else {
            connectRtk();
        }
    }

    /**
     * 482RTK连接
     */
    private void connect482Rtk() {
        mIP = binding.ipAddressEdit.getText().toString().trim();
        mPort = binding.portEdit.getText().toString().trim();
        mMountPoint = binding.mountPointEdit.getText().toString().trim();
        mAccount = binding.accountEdit.getText().toString().trim();
        mPassword = binding.passwordEdit.getText().toString().trim();
        final boolean haveEmptyData = CommonUtils.isEmptyString(mIP)
                || CommonUtils.isEmptyString(mPort)
                || CommonUtils.isEmptyString(mAccount)
                || CommonUtils.isEmptyString(mPassword)
                || CommonUtils.isEmptyString(mMountPoint);
        if (haveEmptyData) {
            Toast.makeText(getContext(), R.string.flight_error_7022, Toast.LENGTH_SHORT).show();
            binding.tvConnect.setVisibility(View.VISIBLE);
        } else {
            SPUtils.put(getContext(), SPUtils.RTK_IP, mIP);
            SPUtils.put(getContext(), SPUtils.RTK_PORT, mPort);
            SPUtils.put(getContext(), SPUtils.RTK_ACCOUNT, mAccount);
            SPUtils.put(getContext(), SPUtils.RTK_PASSWORD, mPassword);
            SPUtils.put(getContext(), SPUtils.RTK_MOUNT_POINT, mMountPoint);
//            mGduRtkManager.connect482Rtk(mIP, mPort, mAccount, mPassword, mMountPoint);
            showConnectingView();
        }
    }

    private void connectRtk() {
        mIP = binding.ipAddressEdit.getText().toString().trim();
        mPort = binding.portEdit.getText().toString().trim();
        mMountPoint = binding.mountPointEdit.getText().toString().trim();
        mAccount = binding.accountEdit.getText().toString().trim();
        mPassword = binding.passwordEdit.getText().toString().trim();
        final boolean haveEmptyData = CommonUtils.isEmptyString(mIP)
                || CommonUtils.isEmptyString(mPort)
                || CommonUtils.isEmptyString(mAccount)
                || CommonUtils.isEmptyString(mPassword)
                || CommonUtils.isEmptyString(mMountPoint);
        if (haveEmptyData) {
            Toast.makeText(getContext(), R.string.flight_error_7022, Toast.LENGTH_SHORT).show();
            binding.tvConnect.setVisibility(View.VISIBLE);
        } else {
            SPUtils.put(getContext(), SPUtils.RTK_IP, mIP);
            SPUtils.put(getContext(), SPUtils.RTK_PORT, mPort);
            SPUtils.put(getContext(), SPUtils.RTK_ACCOUNT, mAccount);
            SPUtils.put(getContext(), SPUtils.RTK_PASSWORD, mPassword);
            SPUtils.put(getContext(), SPUtils.RTK_MOUNT_POINT, mMountPoint);
//            mGduRtkManager.initNtrip(mIP, mPort, mAccount, mPassword, mMountPoint);
//            mGduRtkManager.connect();
            showConnectingView();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mGduRtkManager != null) {
//            mGduRtkManager.onDestroy();
//        }
    }

    public void updateBDTips() {
        if (!CommonUtils.isSmallFlight(GlobalVariable.planType) && GlobalVariable.sGNSSType == 6) {//北斗模式下无法连接RTK
            binding.tvGnssHint.setVisibility(View.VISIBLE);
        } else {
            binding.tvGnssHint.setVisibility(View.GONE);
        }
    }

    public void dispose() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }


    public static SettingRtkFragment newInstance() {
        Bundle args = new Bundle();
        SettingRtkFragment fragment = new SettingRtkFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
