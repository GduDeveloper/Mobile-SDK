package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingImageChannelBinding;
import com.gdu.demo.flight.airlink.BackupsAirlinkManager;
import com.gdu.demo.flight.airlink.MqttConnectCallback;
import com.gdu.drone.AirlinkType;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.util.CollectionUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rxjava.rxlife.RxLife;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingImageChannelFragment extends Fragment {

    private final static String TAG = "SettingImageChannelFragment";

    private FragmentSettingImageChannelBinding mViewBinding;
    private Handler mHandler;
    private boolean isAttached = false;

    private boolean isShowPushType;
    private CombinedData mCombinedData;
    /** 信号强度列表 */
//    private byte[] mSignalInfoArray;
    /**
     * 上一次的图传类型
     */
    private byte mLastAirlinkType = AirlinkType.AUTO.getKey();
    private long lastChangeTime;

    private final int AUTO_AIRLINK_POSITION = 0;  //自动模式
    private final int IM_AIRLINK_POSITION = 1;    //图传模式
    private final int CELLULAR_MOBILE_AIRLINK_POSITION = 2;  //4G图传模式

    private boolean isImgChannelSwitching;   //信道切换中

    /**
     * S200系列飞机当前设置的信道   0:2.4g  1:5.8g  2:auto
     */
    private int mS200SetChannel = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingImageChannelBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }



    private void initView() {
        mHandler = new Handler(Looper.getMainLooper());
//        mViewBinding.currentUsedAirlinkTextview.setText(getString(R.string.current_used_airlink, ""));
        showCurrentAirlinkType();
        mViewBinding.tvChartLabel.setVisibility(View.INVISIBLE);
        mViewBinding.ivChartIcon.setVisibility(View.INVISIBLE);
        mViewBinding.combinedChart.setVisibility(View.INVISIBLE);

        String[] channelNames;
        if (DroneUtil.isS200Serials()) {
            channelNames = getResources().getStringArray(R.array.array_channel_s200);
        } else {
            channelNames = getResources().getStringArray(R.array.array_channel);
        }
        mViewBinding.ovSwitchImgChannel.setData(channelNames);

        String rtspUrl = SPUtils.getString(getContext(), SPUtils.BACK_AIR_LINK_RTSP_URL,  "");
        mViewBinding.etCurrentRtspAddress.setText(rtspUrl);

        if (GlobalVariable.isRCSEE) {
            mViewBinding.groupHdmi.setVisibility(View.GONE);
        } else {
            mViewBinding.groupHdmi.setVisibility(View.VISIBLE);
        }

        if (UavStaticVar.isOpenTextEnvironment) {
            mViewBinding.layoutServerUrl.setVisibility(View.VISIBLE);
        } else {
            mViewBinding.layoutServerUrl.setVisibility(View.GONE);
        }
        if (GlobalVariable.isUseBackupsAirlink) {
            mViewBinding.layoutStream.setVisibility(View.VISIBLE);
//            GduApplication.getSingleApp().gduCommunication.getCameraArgs((code, gduFrame3) -> {
//                if (code == GduConfig.OK && gduFrame3 != null && gduFrame3.frameContent != null && gduFrame3.frameContent.length > 34) {
//                    int stream = ByteUtils.getBits(gduFrame3.frameContent[34], 2, 6);
//
//                    if (isAdded() && mHandler != null && isAttached && stream < 6) {
//                        mHandler.post(() -> mViewBinding.ovSelectedStream.setIndex(stream));
//                    }
//                }
//
//            });
        } else {
            mViewBinding.layoutStream.setVisibility(View.GONE);
        }

        initChartView();

        setListener();
        int BandWidthSetVisibility = UavStaticVar.isOpenTextEnvironment ? View.VISIBLE : View.GONE;
        mViewBinding.tvBandWithSetLabel.setVisibility(BandWidthSetVisibility);
        mViewBinding.clBandWidthSetLayout.setVisibility(BandWidthSetVisibility);
        mViewBinding.viewLine2.setVisibility(BandWidthSetVisibility);
    }

    private void setListener() {
        mViewBinding.ovSwitchImgChannel.setOnOptionClickListener((parentId, view, position) -> {
            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }
            if (GlobalVariable.droneFlyState != 1) {
                Toast.makeText(getContext(), R.string.string_in_flight_not_change_channel, Toast.LENGTH_SHORT).show();
            } else if(NetworkingHelper.isNetworkingMode()){
                Toast.makeText(getContext(), R.string.string_not_change_in_group, Toast.LENGTH_SHORT).show();
            } else {
                if (getPositionFromChannel(GlobalVariable.singalChannel) != position) {
                    switchImgChannel(position);
                }
            }
        });

        mViewBinding.selectHdmiCast.setIndex(SPUtils.getInt(getContext(), SPUtils.BACK_HDMI_CAST_POSITION));
        mViewBinding.selectWifiCast.setIndex(SPUtils.getInt(getContext(), SPUtils.BACK_WIFI_CAST_POSITION));

        mViewBinding.selectHdmiCast.setOnOptionClickListener((parentId, view, position) -> {
            switchHdmiCastType(position);
        });

        mViewBinding.selectWifiCast.setOnOptionClickListener((parentId, view, position) -> {
            switchWifiCastType(position);
        });

        mViewBinding.rgChannelType.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.rb_channel_auto){
                setAirlinkType(AUTO_AIRLINK_POSITION);
            }else if (checkedId == R.id.rb_channel_airlink){
                setAirlinkType(IM_AIRLINK_POSITION);
            }else if (checkedId == R.id.rb_channel_link5g){
                setAirlinkType(CELLULAR_MOBILE_AIRLINK_POSITION);
            }
        });

        mViewBinding.ivHdmiSwitch.setOnClickListener(view -> changeHdmiSwitch());

        mViewBinding.etCurrentAddress.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                String setIp = textView.getText().toString();
                if (StringUtils.isEmptyString(setIp)) {
                    return true;
                }
                set4GService(setIp);
            }
            return false;
        });

        mViewBinding.etCurrentRtspAddress.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                String setIp = textView.getText().toString();
                if (StringUtils.isEmptyString(setIp)) {
                    return true;
                }
                setRtspAddress(setIp);
            }
            return false;
        });

        mViewBinding.ovSwitchPushType.setOnOptionClickListener((parentId, view, position) -> {
            if (position == 0) {
                setPushType(1);
            } else if (position == 1) {
                setPushType(2);
            }
        });
        mViewBinding.ovSelectedStream.setOnOptionClickListener((parentId, view, position) -> changeOutStream(position));

        mViewBinding.tvBandWith10MButton.setOnClickListener(view -> setITFrequencyBandwidth((byte) 2));

        mViewBinding.tvBandWith20MButton.setOnClickListener(view -> setITFrequencyBandwidth((byte) 3));
    }

    private void switchWifiCastType(int position) {
        mHandler.post(() -> {
            SPUtils.put(getContext(), SPUtils.BACK_WIFI_CAST_POSITION,position);
            changeSelectIndex(WIFI_CAST,position);
//            ((ZorroRealControlActivity) requireContext()).showPresentation(WIFI_CAST,position);
        });
    }

    private void changeSelectIndex(int type, int position) {
        if (type == HDMI_CAST) {
            mViewBinding.selectHdmiCast.setIndex(position);
        }else if (type == WIFI_CAST){
            mViewBinding.selectWifiCast.setIndex(position);
        }
    }

    private int NO_CAST= 0;
    private int HDMI_CAST = 1;
    private int WIFI_CAST = 2;

    private void switchHdmiCastType(int position) {
        mHandler.post(() -> {
            SPUtils.put(getContext(), SPUtils.BACK_HDMI_CAST_POSITION, position);
            changeSelectIndex(HDMI_CAST,position);
//            ((ZorroRealControlActivity) requireContext()).showPresentation(HDMI_CAST, position);
        });
    }

    /**
     *  设置码流 变码流时 需要先关变码流再设置
     *
     */
    private void changeOutStream(int position) {
        if (GlobalVariable.sVariableBitstream == 1) {
//            GduApplication.getSingleApp().gduCommunication.setChangeSteamSwitch((byte) 0, (code, bean) -> {
//                if (code == GduConfig.OK) {
//                    setStreamValue(position);
//                }
//            });
        } else {
            setStreamValue(position);
        }
    }

    public void setStreamValue(int position) {
//        GduApplication.getSingleApp().gduCommunication.setOutputStream((byte) position, (code, bean) -> {
//            if (mHandler != null && isAdded() && isAttached) {
//                mHandler.post(() -> {
//                    if (code == GduConfig.OK) {
//                        mViewBinding.ovSelectedStream.setIndex(position);
//                        if (GlobalVariable.isUseBackupsAirlink) {
//                            mViewBinding.layoutStream.setVisibility(View.VISIBLE);
//                        } else {
//                            mViewBinding.layoutStream.setVisibility(View.GONE);
//                        }
//                    }
//                });
//            }
//        });
    }


    /**
     * 设置云台的推流地址
     * @param setIp
     */
    private void setRtspAddress(String setIp){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] ipBytes = setIp.getBytes(StandardCharsets.UTF_8);
            if (ipBytes.length > 128) {
                Toast.makeText(getContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] bytes = new byte[130];
            int index = 0;
            bytes[index++] = (byte) ipBytes.length;
            bytes[index++] = 2;
            System.arraycopy(ipBytes, 0, bytes, index, ipBytes.length);
//            GduApplication.getSingleApp().gduCommunication.set4GServiceIp(bytes, (code, bean) -> {
//                if (code == GduConfig.OK) {
//                    MyLogUtils.d("setRtspAddress = " + setIp);
//                    GlobalVariable.BackAirLinkStreamUrl = setIp;
//                    SPUtils.put(GduApplication.context, SPUtils.BACK_AIR_LINK_RTSP_URL,  GlobalVariable.BackAirLinkStreamUrl);
//                }
//                if (mHandler != null && isAdded() && isAttached) {
//                    mHandler.post(() -> {
//                        if (isAdded()) {
//                            if (code == GduConfig.OK) {
//                                Toaster.show(getString(R.string.Label_SettingSuccess));
//                            } else {
//                                mViewBinding.etCurrentRtspAddress.setText( GlobalVariable.BackAirLinkStreamUrl);
//                                Toaster.show(getString(R.string.Label_SettingFail));
//                            }
//                        }
//                    });
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置服务器MQTT的地址
     * @param setIp
     */
    private void set4GService(String setIp)  {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();

            return;
        }
        try {
            byte[] ipBytes = setIp.getBytes(StandardCharsets.UTF_8);
            if (ipBytes.length > 128) {
                Toast.makeText(getContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] bytes = new byte[130];
            int index = 0;
            bytes[index++] = (byte) ipBytes.length;
            bytes[index++] = 1;
            System.arraycopy(ipBytes, 0, bytes, index, ipBytes.length);
//            GduApplication.getSingleApp().gduCommunication.set4GServiceIp(bytes, (code, bean) -> {
//                if (code == GduConfig.OK) {
//                    if (!setIp.equals(GlobalVariable.BackAirLinkUrl)) {
//                        // 地址不一样，要把之前的断开，会有定时检查自动重连
//                        printLog("[lte] set url not equals!! disconnect! wait reconnect!! " + setIp);
//                        BackupsAirlinkManager.getInstance().disconnect();
//                    }
//                    GlobalVariable.BackAirLinkUrl = setIp;
//                    SPUtils.put(GduApplication.context, SPUtils.BACK_AIR_LINK_URL,  GlobalVariable.BackAirLinkUrl);
//                }
//                if (mHandler != null && isAdded() && isAttached) {
//                    mHandler.post(() -> {
//                        if (isAdded()) {
//                            if (code == GduConfig.OK) {
//                                Toaster.show(getString(R.string.Label_SettingSuccess));
//                            } else {
//                                mViewBinding.etCurrentAddress.setText( GlobalVariable.BackAirLinkUrl);
//                                Toaster.show(getString(R.string.Label_SettingFail));
//                            }
//                        }
//                    });
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        mViewBinding.tvTvBandWithContent.setText("20MHz");
        getITFrequencyBandwidth();
        getIMChannel();
        if (GlobalVariable.sFourthGStatus != null) {
            mLastAirlinkType = GlobalVariable.sFourthGStatus.airlink_type;
            showAirlinkTypeView();
            showPushTypeView();
        } else {
            mViewBinding.rbChannelAuto.setChecked(true);
            mViewBinding.ovSwitchPushType.setIndex(0);
        }

//        GduApplication.getSingleApp().gduCommunication.setP301D((byte) 3, (byte) 0, (code, bean) -> {
//            if (mHandler == null || !isAdded() || !isAttached) {
//                return;
//            }
//            if ( code == GduConfig.OK && bean.frameContent != null) {
//                int states = bean.frameContent[2];
//                mHandler.post(() -> mViewBinding.ivHdmiSwitch.setSelected(states == 1));
//            }
//        });

        mViewBinding.etCurrentAddress.setText(GlobalVariable.BackAirLinkUrl);

        //获取MQTT地址
//        GduApplication.getSingleApp().gduCommunication.get4GServiceIp(false, (byte) 1, (code, bean) -> {
//            if (bean != null && bean.frameContent != null && bean.frameContent.length > 0) {
//                int length = bean.frameContent[0] & 0xff;
//                byte[] bytes = new byte[length];
//                System.arraycopy(bean.frameContent, 1, bytes, 0, length);
//                String serviceAddress = new String(bytes);
//                if (!TextUtils.isEmpty(serviceAddress)) {
//                    if (!serviceAddress.equals(GlobalVariable.BackAirLinkUrl)) {
//                        // 地址不一样，要把之前的断开，会有定时检查自动重连
//                        printLog("[lte] url not equals!! disconnect! wait reconnect!! " + serviceAddress);
//                        BackupsAirlinkManager.getInstance().disconnect();
//                    }
//                    GlobalVariable.BackAirLinkUrl = serviceAddress;
//                    SPUtils.put(GduApplication.context, SPUtils.BACK_AIR_LINK_URL, serviceAddress);
//                    if (mHandler != null && isAdded() && isAttached) {
//                        mHandler.post(() -> mViewBinding.etCurrentAddress.setText(serviceAddress));
//                    }
//                }
//                MyLogUtils.d("Get Service = " + serviceAddress);
//            }
//        });

        int firstVersion = 0, secondVersion = 0, thirdVersion = 0;
        if (!TextUtils.isEmpty(GlobalVariable.s5GVersion)) {
            try {
                String[] versions = GlobalVariable.s5GVersion.split("\\.");
                if (versions != null && versions.length >= 3) {
                    firstVersion = Integer.valueOf(versions[0]);
                    secondVersion = Integer.valueOf(versions[1]);
                    thirdVersion = Integer.valueOf(versions[2]);
                }
            } catch (Exception e) {
                MyLogUtils.d("splite s5GVersion exception = " + e);
            }
        }
        boolean supportNewProtocol = !(firstVersion == 0 && secondVersion == 0 && thirdVersion < 77);
//        GduApplication.getSingleApp().gduCommunication.get4GServiceIp(supportNewProtocol, (byte) 2, (code, bean) -> {
//            if (bean != null && bean.frameContent != null && bean.frameContent.length > 0) {
//                int length = bean.frameContent[0] & 0xff;
//                byte[] bytes = new byte[length];
//                System.arraycopy(bean.frameContent, 1, bytes, 0, length);
//                String serviceStreamAddress = new String(bytes);
//                if (!TextUtils.isEmpty(serviceStreamAddress)) {
//                    MyLogUtils.d("GduApplication.getSingleApp().gduCommunication.get4GServiceIp : " + serviceStreamAddress);
//                    GlobalVariable.BackAirLinkStreamUrl = serviceStreamAddress;
//                    SPUtils.put(GduApplication.context, SPUtils.BACK_AIR_LINK_RTSP_URL, serviceStreamAddress);
//                    if (mHandler != null && isAdded() && isAttached) {
//                        mHandler.post(() -> mViewBinding.etCurrentRtspAddress.setText(serviceStreamAddress));
//                    }
//                }
//                MyLogUtils.d("Get Service = " + serviceStreamAddress);
//            }
//        });

        Observable.interval(1, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(this))
                .subscribe(aLong -> {
                    showCurrentAirlinkType();
                    generateChartData(GlobalVariable.combinedChartPoints, GlobalVariable.currentPoint);
                });
    }

    private void getIMChannel() {
        MyLogUtils.d("test getIMChannel 111");
        if (DroneUtil.isS200Serials()) {
//            GduApplication.getSingleApp().gduCommunication.setImageTransmissionInfo((byte) 3, new SocketCallBack3() {
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//                    MyLogUtils.d("test getIMChannel callBack ");
//                    if (code == GduConfig.OK && bean != null && bean.frameContent != null) {
//                        if (bean.frameContent.length >= 4) {
//                            byte channel = bean.frameContent[3];
//                            mS200SetChannel = channel;
//                            setImgChannel(channel);
//                        }
//                    }
//                }
//            });
        }
    }

    private void changeHdmiSwitch() {
        byte setStates = (byte) (mViewBinding.ivHdmiSwitch.isSelected() ? 0 : 1);
//        GduApplication.getSingleApp().gduCommunication.setP301D((byte) 2, setStates, (code, bean) -> {
//            if (mHandler == null || !isAdded() || !isAttached) {
//                return;
//            }
//            mHandler.post(() -> {
//                if (isAdded()) {
//                    if (code == GduConfig.OK) {
//                        mViewBinding.ivHdmiSwitch.setSelected(setStates == 1);
//                    } else {
//                        Toaster.show(getString(R.string.Label_SettingFail));
//                    }
//                }
//            });
//        });
    }

    public void showCurrentAirlinkType(){
        String airlinkType;
        if (GlobalVariable.isUseBackupsAirlink) {
            if(!isShowPushType) {
                if(GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus.pushStreamType != 0) {
                    showPushTypeView();
                    isShowPushType = true;
                }
            }
            airlinkType = getString(R.string.link5g);
            if (mViewBinding.layoutStream.getVisibility() == View.GONE) {
                mViewBinding.layoutStream.setVisibility(View.VISIBLE);
                mViewBinding.groupPushType.setVisibility(View.VISIBLE);
            }
        } else {
            airlinkType = getString(R.string.airlink);
            if (mViewBinding.layoutStream.getVisibility() == View.VISIBLE) {
                mViewBinding.layoutStream.setVisibility(View.GONE);
                mViewBinding.groupPushType.setVisibility(View.GONE);
            }
        }
        if (!mViewBinding.currentUsedAirlinkTextview.getText().toString().endsWith(airlinkType)) {
            mViewBinding.currentUsedAirlinkTextview.setText(getString(R.string.current_used_airlink,airlinkType));
        }
    }

    private void switchImgChannel( int position) {
        if (isImgChannelSwitching) {
            return;
        }
        isImgChannelSwitching = true;
        int channel = 0;
        if (DroneUtil.isS200Serials()) {
            switch (position) {
                case  0:
                    channel = 2;
                    break;
                case 1:
                    channel = 0;
                    break;
                case 2:
                    channel = 1;
                    break;
            }
        } else {
            channel = position;
        }
        int finalChannel = channel;
//        GduApplication.getSingleApp().gduCommunication.setImageTransmissionInfo((byte) channel, (code, bean) -> {
//            if (mHandler == null || !isAdded() || !isAttached) {
//                isImgChannelSwitching = false;
//                return;
//            }
//            mHandler.postDelayed(() -> {
//                AppLog.d(TAG, "change channel to " + finalChannel + ",code=" + code);
//                if(code == GduConfig.OK) {
//                    if (DroneUtil.isS200Serials()) {
//                        mS200SetChannel = finalChannel;
//                    }
//                    setImgChannel(finalChannel);
//                    ToastUtil.show(R.string.Label_SettingSuccess);
//                } else {
//                    ToastUtil.show(R.string.Label_SettingFail);
//                }
//                isImgChannelSwitching = false;
//            }, 1000);
//        });
    }

    /**
     * 设置信道显示位置
     * @param channel
     */
    private void setImgChannel(int channel){
        MyLogUtils.d("test setImgChannel channel="+channel);
        if (mHandler != null && isAdded() && isAttached) {
            mHandler.post(() -> {
                if (isAdded() && isAttached) {
                    int position = getPositionFromChannel(channel);
                    mViewBinding.ovSwitchImgChannel.setIndex(position);
                }
            });
        }
    }

    /**
     * 根据信道获取当前显示位置
     * @param channel 当前工作频段 0：2.4G  1：5G，S200系列多了2自动3查询的设置功能
     * @return
     */
    private int getPositionFromChannel(int channel){
        int position = 0;
        if (DroneUtil.isS200Serials()) {
            channel = mS200SetChannel;
            switch (channel) {
                case 0:
                    position = 1;
                    break;
                case 1:
                    position = 2;
                    break;
                case 2:
                    position = 0;
                    break;
            }
        } else {
            position = channel;
        }
        return position;
    }

    /**
     * 显示图传模式
     */
    private void showAirlinkTypeView() {
        if (mLastAirlinkType == AirlinkType.CELLULAR_MOBILE_LINK.getKey()) {
            mViewBinding.rbChannelLink5g.setChecked(true);
        } else if (mLastAirlinkType == AirlinkType.IM_LINK.getKey()) {
            mViewBinding.rbChannelAirlink.setChecked(true);
        } else {
            mViewBinding.rbChannelAuto.setChecked(true);
        }
    }

    private void showPushTypeView() {
        if (mViewBinding != null && GlobalVariable.sFourthGStatus != null) {
            if (GlobalVariable.sFourthGStatus.pushStreamType == 1) {
                mViewBinding.ovSwitchPushType.setIndex(0);
            } else if (GlobalVariable.sFourthGStatus.pushStreamType == 2) {
                mViewBinding.ovSwitchPushType.setIndex(1);
            } else {
                MyLogUtils.w("showPushTypeView = " + GlobalVariable.sFourthGStatus.pushStreamType);
            }
        }
    }

    /**
     * 获取图传频率带宽
     * 1：5Mhz 2：10Mhz 3：20Mhz 4：40mhz
     */
    private void getITFrequencyBandwidth(){
//        GduApplication.getSingleApp().gduCommunication.getITFrequencyBandwidth((code, gduFrame3) -> {
//            if (code == GduConfig.OK && gduFrame3 != null && gduFrame3.frameContent != null && gduFrame3.frameContent.length > 4) {
//                if (mHandler != null && isAdded() && isAttached){
//                    mHandler.post(() -> {
//                        byte value = gduFrame3.frameContent[2];
//                        mViewBinding.tvTvBandWithContent.setText(FrequencyBandwidth.get(value).getValue());
//                    });
//                }
//            }
//        });
    }

    /**
     * 设置图传频率带宽
     * 1：5Mhz 2：10Mhz 3：20Mhz 4：40mhz
     */
    private void setITFrequencyBandwidth(byte value) {
        MyLogUtils.i("setITFrequencyBandwidth() value = " + value);
//        GduApplication.getSingleApp().gduCommunication.setITFrequencyBandwidth(value, (code, gduFrame3) -> {
//            MyLogUtils.i("setITFrequencyBandwidth callback() code = " + code);
//            if (mHandler != null && isAdded() && isAttached){
//                mHandler.post(() -> {
//                    if (code == GduConfig.OK) {
//                        mViewBinding.tvTvBandWithContent.setText(FrequencyBandwidth.get(value).getValue());
//                        ToastUtil.show(getString(R.string.Label_SettingSuccess));
//                    } else {
//                        ToastUtil.show(getString(R.string.Label_SettingFail));
//                    }
//                });
//            }
//        });
    }

    private void setPushType(int checkedId) {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
            showAirlinkTypeView();
            showPushTypeView();
            return;
        }
        if (GlobalVariable.sFourthGStatus == null) {
            Toast.makeText(getContext(), "未获取飞机5G信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkedId == GlobalVariable.sFourthGStatus.pushStreamType) {
            Toast.makeText(getContext(), "配置未改变", Toast.LENGTH_SHORT).show();
            return;
        }
        changeAirPushType((byte) checkedId);
    }

    /**
     * 设置图传类型
     * @param checkedId
     */
    private void setAirlinkType(int checkedId){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
            showAirlinkTypeView();
            showPushTypeView();
            return;
        }
        if (checkedId == CELLULAR_MOBILE_AIRLINK_POSITION && NetworkingHelper.isNetworkingMode()) {
            Toast.makeText(getContext(), R.string.string_not_change_in_group, Toast.LENGTH_SHORT).show();
            showAirlinkTypeView();
            showPushTypeView();
            return;
        }

        byte type;
        switch (checkedId){
            case AUTO_AIRLINK_POSITION:
                type = AirlinkType.AUTO.getKey();
                if (GlobalVariable.arlink_linkStatus == 1 && GlobalVariable.sRCConnState == ConnStateEnum.Conn_Sucess) {
                    checkChangeTime();
                    changeToImageAirLink(type);
                } else if (GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus.mqtt_connect_status == 1) {
                    checkChangeTime();
                    changeTo4GAirLink(type);
                }
                break;
            case IM_AIRLINK_POSITION:
                if (GlobalVariable.arlink_linkStatus == 1 && GlobalVariable.sRCConnState == ConnStateEnum.Conn_Sucess) {
                    type = AirlinkType.IM_LINK.getKey();
                    checkChangeTime();
                    changeToImageAirLink(type);
                } else {
                    Toast.makeText(getContext(), R.string.transmission_not_connect, Toast.LENGTH_SHORT).show();
                    showAirlinkTypeView();
                    showPushTypeView();
                }
                break;
            case CELLULAR_MOBILE_AIRLINK_POSITION:
                if (GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus.mqtt_connect_status == 1) {
                    if (BackupsAirlinkManager.getInstance().isConnected()) {
                        type = AirlinkType.CELLULAR_MOBILE_LINK.getKey();
                        checkChangeTime();
                        changeTo4GAirLink(type);
                    } else {
                        Toast.makeText(getContext(), R.string.rc_not_connect_server, Toast.LENGTH_SHORT).show();
                        showAirlinkTypeView();
                        showPushTypeView();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.drone_not_connect_server, Toast.LENGTH_SHORT).show();
                    showAirlinkTypeView();
                    showPushTypeView();
                }
                break;
            default:
                break;
        }
    }

    private void checkChangeTime() {
        MyLogUtils.d("checkChangeTime  time = " + (System.currentTimeMillis() - lastChangeTime));
        if (System.currentTimeMillis() - lastChangeTime < 3 * 1000) {
            Toast.makeText(getContext(), R.string.Msg_imgchannel_changing, Toast.LENGTH_SHORT).show();
            showAirlinkTypeView();
            showPushTypeView();
            return;
        }
        lastChangeTime = System.currentTimeMillis();
    }

    private void changeToImageAirLink(byte type) {
        GlobalVariable.isUseBackupsAirlink = false;
        GlobalVariable.sManualSwitchAirlinkTime = System.currentTimeMillis();
//        GduApplication.getSingleApp().gduCommunication.connectNPS((code, bean) -> {
//            printLog("changeToImageAirLink connectNPS code=" + code + ", bean=" + bean);
//            if (code == GduConfig.OK) {
//                setParam(type);
//                printLog("changeToImageAirLink setChangeSteamSwitch.");
//                GduApplication.getSingleApp().gduCommunication.setChangeSteamSwitch((byte) 1, (b, gduFrame3) -> {
//                    printLog("changeToImageAirLink setChangeSteamSwitch callBack code=" + b);
//                });
//                //切换后重置时间点，提升按钮点击频率
//                lastChangeTime = 0;
//            }
//        });
    }

    private void changeAirPushType(byte type) {
//        GduApplication.getSingleApp().gduCommunication.setLTEPushStreamType(type, (code, bean) -> {
//            if (code == GduConfig.OK) {
//                // {@link GetDroneInfoHelper3#get5GStatusInfo}获取pushStreamType有延迟导致后面切换选择状态失败，所以这里也需赋值
//                GlobalVariable.sFourthGStatus.pushStreamType = type;
//                ToastUtil.show(R.string.Label_SettingSuccess);
//                if (mHandler != null && isAdded() && isAttached) {
//                    mHandler.post(this::showPushTypeView);
//                }
//            } else {
//                ToastUtil.show(R.string.Label_SettingFail);
//            }
//        });
    }

    private void changeTo4GAirLink(byte type) {
        BackupsAirlinkManager.getInstance().initMqtt(new MqttConnectCallback() {
            @Override
            public void onConnectSuccess() {
                GlobalVariable.sManualSwitchAirlinkTime = System.currentTimeMillis();
                BackupsAirlinkManager.getInstance().registerNpsForLte((code, bean) -> {
                    if (code == GduConfig.OK) {
                        GlobalVariable.isUseBackupsAirlink = true;
                        setParam(type);
//                        changeOutStream(3);//会导致每次打开页面都被设置成2M的问题。
                        //切换后重置时间点，提升按钮点击频率
                        lastChangeTime = 0;
                    }
                    if (code == GduConfig.TIME_OUT) {
                        changeToImageAirLink(AirlinkType.IM_LINK.getKey());
                    }
                });
            }

            @Override
            public void onConnectFail() {
                if(!isAdded()){
                    return;
                }
                Toast.makeText(getContext(), R.string.not_connect_server, Toast.LENGTH_SHORT).show();
                showAirlinkTypeView();
                showPushTypeView();
            }
        });
    }


    private void setParam(byte type) {
//        GduApplication.getSingleApp().gduCommunication.setAirlinkType(type, (code, bean) -> {
//            printLog("setParam setAirlinkType callBack code=" + code + ", bean=" + bean);
//            if (mHandler == null || !isAdded() || !isAttached) {
//                return;
//            }
//            try {
//                mHandler.post(() -> {
//                    if (isAdded()) {
//                        if (code == GduConfig.OK) {
//                            mLastAirlinkType = type;
//                        } else {
//                            showAirlinkTypeView();
//                            showPushTypeView();
//                            Toaster.show(getString(R.string.Label_SettingFail));
//                        }
//                    }
//                });
//            } catch (Exception e){
//
//            }
//        });
//        GduApplication.getSingleApp().gduCommunication.setImageType((byte) (GlobalVariable.isUseBackupsAirlink ? 1 : 0),
//                (code, bean) -> {
//                printLog("setParam setImageType callBack code=" + code + ", bean=" + bean);
//                if (code == GduConfig.OK) {
//                    EventBus.getDefault().post(new AirlinkChangedEvent());
//                }
//        });
    }

    private void initChartView() {
        mCombinedData = new CombinedData();

        // 隐藏颜色含义控件
        mViewBinding.combinedChart.getLegend().setEnabled(false);
        mViewBinding.combinedChart.getDescription().setEnabled(false);
        mViewBinding.combinedChart.setNoDataText(getString(R.string.no_data_was_obtained));
        mViewBinding.combinedChart.setNoDataTextColor(getResources().getColor(R.color.color_EF4E22));
        mViewBinding.combinedChart.setDrawGridBackground(false);
        mViewBinding.combinedChart.setDrawBarShadow(false);
        mViewBinding.combinedChart.setHighlightPerTapEnabled(false);
        mViewBinding.combinedChart.setHighlightFullBarEnabled(false);
        mViewBinding.combinedChart.setDragEnabled(false);

        final YAxis rightYAxis = mViewBinding.combinedChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawAxisLine(true);
        rightYAxis.setEnabled(true);
        //网格虚线
        rightYAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        rightYAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        rightYAxis.setAxisMinimum(-110f);
        rightYAxis.setAxisMaximum(-50f);
        rightYAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        rightYAxis.setTextColor(getResources().getColor(R.color.black));

        //y轴配置
        final YAxis leftYAxis = mViewBinding.combinedChart.getAxisLeft();
        //网格虚线
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        leftYAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        leftYAxis.setAxisMinimum(-110f);
        leftYAxis.setAxisMaximum(-50f);
        leftYAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        leftYAxis.setTextColor(getResources().getColor(R.color.black));
//        leftYAxis.setLabelCount(5);

        //x轴配置
        final XAxis xAxis = mViewBinding.combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(1f);
        xAxis.setGranularity(1f);
        //网格虚线
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        xAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        xAxis.setTextColor(getResources().getColor(R.color.black));
    }

    /**
     * 添加柱状图数据
     */
    private void generateChartData(List<Short> shortList, byte selectNum) {
        if (!isImgChannelSwitching) {
            if (!DroneUtil.isS200Serials()) {
                setImgChannel(GlobalVariable.singalChannel);
                mViewBinding.tvCurrentChannel.setVisibility(View.GONE);
            }else {
                mViewBinding.tvCurrentChannel.setVisibility(View.VISIBLE);
                mViewBinding.tvCurrentChannel.setText(getString(R.string.current_channel_is,
                        getString(GlobalVariable.singalChannel == 1?
                                R.string.Label_channel_5_8:
                                R.string.Label_channel_2_4)));
            }
        }
        if (!isAdded() || !getUserVisibleHint()) {
            return;
        }
//        MyLogUtils.i("generateChartData() shortListSize = " + shortList.size()
//                + "; selectNum = " + selectNum);
        if (CollectionUtils.isEmptyList(shortList)) {
            return;
        }
        mViewBinding.tvChartLabel.setVisibility(View.VISIBLE);
        mViewBinding.ivChartIcon.setVisibility(View.VISIBLE);
        mViewBinding.combinedChart.setVisibility(View.VISIBLE);
        final BarData mBarData = new BarData();
        List<BarEntry> barEntryList = new ArrayList<>();

        final LineData mLineData = new LineData();
        final ArrayList<Entry> mLineEntryData = new ArrayList<>();

        for (int index = 0; index < shortList.size(); index++) {
            mLineEntryData.add(new Entry(index + 1, shortList.get(index)));
            if (selectNum == index) {
                barEntryList.add(new BarEntry(selectNum + 1, -110));
            }
        }

        final LineDataSet mLineDataSet = new LineDataSet(mLineEntryData, getString(R.string.channel_interference_reduction));
        mLineDataSet.setColor(getResources().getColor(R.color.color_EF4E22));
        mLineDataSet.setLineWidth(1f);
        mLineDataSet.setCircleColor(getResources().getColor(R.color.color_EF4E22));
        mLineDataSet.setCircleRadius(3f);
        mLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mLineDataSet.setDrawValues(false);
        mLineDataSet.setValueTextSize(10f);
        mLineDataSet.setValueTextColor(Color.WHITE);

        mLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        mLineData.addDataSet(mLineDataSet);
        mCombinedData.setData(mLineData);
        mViewBinding.combinedChart.setData(mCombinedData);


        final BarDataSet amountBar = new BarDataSet(barEntryList, getString(R.string.channel_interference_reduction));
        amountBar.setAxisDependency(YAxis.AxisDependency.LEFT);
        // 柱状颜色
        amountBar.setColor(Color.rgb(0,128,0));
        amountBar.setValueTextSize(10);
        amountBar.setValueTextColor(getResources().getColor(R.color.white));
        mBarData.addDataSet(amountBar);
        //设置柱状图显示的大小
        mBarData.setBarWidth(0.2f);
        //设置是否显示数据点的数值
        mBarData.setDrawValues(false);
        mCombinedData.setData(mBarData);
        //以下是为了解决 柱x状图 左右两边只显示了一半的问题 根据实际情况而定
        mViewBinding.combinedChart.getXAxis().setAxisMinimum(0.75f);
        mViewBinding.combinedChart.getXAxis().setAxisMaximum((float) (mLineEntryData.size() + 0.25));
        mViewBinding.combinedChart.invalidate();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
//        GduApplication.getSingleApp().gduCommunication.removeCycleACKCB(GduSocketConfig3.CYCLE_ACK_INTENSITY_INTERFERENCE_SIGNAL);
    }

    public static SettingImageChannelFragment newInstance() {
        Bundle args = new Bundle();
        SettingImageChannelFragment fragment = new SettingImageChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private boolean hasConnectPlane() {
        return !TextUtils.isEmpty(GlobalVariable.SN) && GlobalVariable.connStateEnum != ConnStateEnum.Conn_None;
    }
}
