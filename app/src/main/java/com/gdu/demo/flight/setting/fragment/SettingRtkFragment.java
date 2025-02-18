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
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.FragmentSettingRtkBinding;
import com.gdu.drone.RTKNetConnectStatus;
import com.gdu.rtk.ReferenceStationSource;
import com.gdu.sdk.flightcontroller.rtk.RTK;
import com.gdu.sdk.util.CommonUtils;
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

    private RTK rtk;

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



    private void initView() {
        rtk = SdkDemoApplication.getAircraftInstance().getFlightController().getRTK();
        initHandler();
        MyLogUtils.i("initListener()");
        binding.ivBack.setOnClickListener(listener);
        binding.rtkSwitchView.setOnClickListener(listener);
        binding.tvConnect.setOnClickListener(listener);
        binding.tvBreak.setOnClickListener(listener);
        binding.rtkSwitchView.setOnClickListener(listener);

        binding.rtkServiceView.setOnOptionClickListener((parentId, view, position) -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {   //未连接飞机
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }
            ReferenceStationSource stationSource = ReferenceStationSource.CUSTOM_NETWORK_SERVICE;
            if (position == 0) {
                stationSource = ReferenceStationSource.CUSTOM_NETWORK_SERVICE;
            } else if (position == 1) {
                stationSource = ReferenceStationSource.BASE_STATION;
            } else if (position == 2) {
                stationSource = ReferenceStationSource.ONBOARD_RTK;
            }
           rtk.setReferenceStationSource(stationSource, gduError -> {
               if (mHandler != null) {
                   mHandler.post(() -> {
                       if (gduError == null) {
                           binding.rtkServiceView.setIndex(position);
                           switch (position) {
                               case 0:
                                   binding.rtkServiceView.setIndex(0);
                                   binding.rtkStateView.setStationName(mRTKServiceList[0]);
                                   binding.rtkStateView.setVisibility(View.VISIBLE);
                                   binding.rtkParamLayout.setVisibility(View.VISIBLE);
                                   showNotConnectedStatus();
                                   break;
                               case 1:
                                   binding.rtkServiceView.setIndex(1);
                                   binding.rtkStateView.setStationName(mRTKServiceList[1]);
                                   binding.rtkParamLayout.setVisibility(View.GONE);
                                   showNotConnectedStatus();
                                   break;
                               case 2:
                                   binding.rtkServiceView.setIndex(2);
                                   binding.rtkStateView.setStationName(mRTKServiceList[2]);
                                   binding.rtkStateView.setVisibility(View.VISIBLE);
                                   binding.rtkParamLayout.setVisibility(View.VISIBLE);
                                   showNotConnectedStatus();
                                   break;
                               default:
                                   binding.rtkServiceView.setIndex(0);
                                   binding.rtkStateView.setStationName(mRTKServiceList[0]);
                                   binding.rtkParamLayout.setVisibility(View.VISIBLE);
                                   showNotConnectedStatus();
                                   break;
                           }

                       } else {
                           Toast.makeText(getContext(), "设置失败", Toast.LENGTH_SHORT).show();
                       }
                   });
               }
           });
        });

    }

    private void initData() {
        MyLogUtils.i("initData()");
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            GlobalVariable.sRTKType = 1;
            GlobalVariable.sDroneRTKStatus = 0;
            GlobalVariable.sPhoneRTKStatus = 0;
            GlobalVariable.sBSRTKStatus = 0;
        }
        mRTKServiceList = getResources().getStringArray(R.array.rtk_service_array_onboard);
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
                    showNotConnectedStatus();
                    break;
                case 2:
                    binding.rtkServiceView.setIndex(1);
                    binding.rtkStateView.setStationName(mRTKServiceList[1]);
                    showNotConnectedStatus();
                    break;
                case 3:
                    binding.rtkServiceView.setIndex(2);
                    binding.rtkStateView.setStationName(mRTKServiceList[2]);
                    binding.rtkStateView.setVisibility(View.VISIBLE);
                    showOnboardRtkConnect();
                    break;
                default:
                    binding.rtkServiceView.setIndex(0);
                    binding.rtkStateView.setStationName(mRTKServiceList[0]);
                    showNotConnectedStatus();
                    break;
            }
        }
        initParam();
        updateBDTips();

    }



    private void initHandler() {
        MyLogUtils.i("initHandler()");
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                int status = msg.what;
                if (status == RTKNetConnectStatus.SERVER_CONNECTED.getKey()) {
                } else if (status == RTKNetConnectStatus.SERVER_COMMUNICATE.getKey()) {
                } else if (status == RTKNetConnectStatus.CONNECT_FAILED.getKey()) {
                    showNotConnectedStatus();
                } else if (status == RTKNetConnectStatus.DISCONNECT.getKey()) {
                    showNotConnectedStatus();
                } else if (status == RTKNetConnectStatus.CONNECTING.getKey()) {
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


    private void showNotConnectedStatus() {
        if (getContext() == null) {
            return;
        }
        binding.tvConnectState.setVisibility(View.GONE);
        binding.tvConnect.setVisibility(View.VISIBLE);
        binding.tvBreak.setVisibility(View.GONE);
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


    public boolean aircraftIsConnectNet() {
        return GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus._4g_net_status != 0;
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
                default:
                    break;
            }
        }
    };


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
        rtk.disconnectRtk();
        SPUtils.put(getContext(), SPUtils.RTK_CONNECT_STATE, false);
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
            closeRTK();
            SPUtils.put(getContext(), SPUtils.RTK_SWITCH, false);
        } else {
            binding.rtkSwitchView.setSelected(true);
            binding.rlSelectRtkType.setVisibility(View.VISIBLE);
            binding.rtkStateView.setVisibility(View.VISIBLE);
            binding.rtkParamLayout.setVisibility(View.VISIBLE);
            if (mLastRTKType == 1 || mLastRTKType == 3) {
                binding.rtkParamLayout.setVisibility(View.VISIBLE);
            } else {
                binding.rtkParamLayout.setVisibility(View.GONE);
            }
            SPUtils.put(getContext(), SPUtils.RTK_SWITCH, true);
        }
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
        // 飞行中未fixed不能连接rtk
        if ( GlobalVariable.droneFlyState != 1 && GlobalVariable.rtkIsLoading == 1) {
            Toast.makeText(getContext(), R.string.string_not_allow_connect_rtk, Toast.LENGTH_SHORT).show();
            return;
        }
        mIP =  binding.ipAddressEdit.getText().toString().trim();
        mPort = binding.portEdit.getText().toString().trim();
        mMountPoint = binding.mountPointEdit.getText().toString().trim();
        mAccount = binding.accountEdit.getText().toString().trim();
        mPassword = binding.passwordEdit.getText().toString().trim();

        boolean haveEmptyData = CommonUtils.isEmptyString(mIP)
                || CommonUtils.isEmptyString(mPort)
                || CommonUtils.isEmptyString(mAccount)
                || CommonUtils.isEmptyString(mPassword)
                || CommonUtils.isEmptyString(mMountPoint);

        if (haveEmptyData) {
            Toast.makeText(getContext(), "请输入正确的参数", Toast.LENGTH_SHORT).show();
            return;
        }

        MyLogUtils.d("mLastRTKType  =" + mLastRTKType);
        ReferenceStationSource stationSource = ReferenceStationSource.CUSTOM_NETWORK_SERVICE;
        if (mLastRTKType == 1) {
            stationSource = ReferenceStationSource.CUSTOM_NETWORK_SERVICE;
        } else if (mLastRTKType == 2) {
            stationSource = ReferenceStationSource.BASE_STATION;
        } else if (mLastRTKType == 3) {
            stationSource = ReferenceStationSource.ONBOARD_RTK;
        }

        rtk.connectRtk(stationSource, null, new GduRtkManager.OnRtkConnectListener() {
            @Override
            public void onStartConnect() {
                if (mHandler != null) {
                    mHandler.post(() -> {
                        binding.tvConnectState.setVisibility(View.VISIBLE);
                        binding.tvConnectState.setText("连接中");
                        binding.tvConnect.setVisibility(View.GONE);
                        binding.tvBreak.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onConnectSuccess() {
                if (mHandler != null) {
                    mHandler.post(() -> {
                        binding.tvConnectState.setText("连接成功");
                        binding.tvConnect.setVisibility(View.GONE);
                        binding.tvBreak.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onDisConnect() {
                if (mHandler != null) {
                    mHandler.post(() -> {
                        binding.tvConnectState.setVisibility(View.GONE);
                        binding.tvConnect.setVisibility(View.VISIBLE);
                        binding.tvBreak.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onConnectFailed(RTKNetConnectStatus rtkNetConnectStatus) {
                if (mHandler != null) {
                    mHandler.post(() -> {
                        binding.tvConnectState.setVisibility(View.GONE);
                        binding.tvConnect.setVisibility(View.VISIBLE);
                        binding.tvBreak.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onGgaGot(byte[] bytes) {

            }

            @Override
            public void onRtcmGot(byte[] bytes) {

            }
        });
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
