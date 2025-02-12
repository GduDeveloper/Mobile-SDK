package com.gdu.demo.flight.setting.viewmodel;

import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.gdu.airlink.FrequencyBandwidth;
import com.gdu.common.error.GDUError;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.flight.base.BaseViewModel;
import com.gdu.sdk.airlink.GDUAirLink;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.DroneUtil;
import com.gdu.util.SPUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author wuqb
 * @date 2025/2/10
 * @description TODO
 */
public class SettingSDRViewModel extends BaseViewModel {

    private GDUAirLink mGDUAirLink;
    private final MutableLiveData<Integer> visibleLightStreamLiveData;  //获取可见光视频码流设置反馈
    private final MutableLiveData<Byte> steamSwitchLiveData;  //设置码流变换开关
    private final MutableLiveData<Boolean> outputStreamLiveData;  //设置可见光相机视频输出码流值
    private final MutableLiveData<String> serviceIpLiveData;  //设置云台推流地址
    private final MutableLiveData<Boolean> p301DLiveData;  //设置遥控器P301D模块
    private final MutableLiveData<String> frequencyBandwidthLiveData;  //获取图传频率带宽
    private final MutableLiveData<Boolean> ltePushStreamTypeLiveData;  //获取图传频率带宽
    private final MutableLiveData<Byte> imageChannelLiveData;  //图传信道信息


    /**
     * S200系列飞机当前设置的信道   0:2.4g  1:5.8g  2:auto
     */
    private int mS200SetChannel = 0;

    private boolean isImgChannelSwitching;   //信道切换中

    public SettingSDRViewModel() {
        mGDUAirLink = SdkDemoApplication.getAircraftInstance().getAirLink();
        visibleLightStreamLiveData = new MutableLiveData<>();
        steamSwitchLiveData = new MutableLiveData<>();
        outputStreamLiveData = new MutableLiveData<>();
        serviceIpLiveData = new MutableLiveData<>();
        p301DLiveData = new MutableLiveData<>();
        frequencyBandwidthLiveData = new MutableLiveData<>();
        ltePushStreamTypeLiveData = new MutableLiveData<>();
        imageChannelLiveData = new MutableLiveData<>();
    }

    /**
     *获取可见光视频码流设置反馈
     * */
    public void getVisibleLightStream(){
        mGDUAirLink.getVisibleLightStream(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                visibleLightStreamLiveData.postValue(integer);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置码流变换开关
     * @param switchType 1 开 0 关
     * */
    public void setChangeSteamSwitch(byte switchType){
        mGDUAirLink.setChangeSteamSwitch(switchType, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                steamSwitchLiveData.postValue(switchType);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置可见光相机视频输出码流值
     * @param stream   编码格式   0：0.5M  1：1M  2：1.5M  3：2M  4：4M  5：8M
     */
    public void setOutputStream(byte stream){
        mGDUAirLink.setOutputStream(stream, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                outputStreamLiveData.postValue(true);
            }

            @Override
            public void onFailure(GDUError gduError) {
                outputStreamLiveData.postValue(false);
            }
        });
    }

    /**
     * 设置云台推流地址
     * */
    public void set4GServiceIp(String setIp){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            toastLiveData.setValue(R.string.fly_no_conn);
            return;
        }
        try {
            byte[] ipBytes = setIp.getBytes(StandardCharsets.UTF_8);
            if (ipBytes.length > 128) {
                toastLiveData.setValue(R.string.Label_SettingFail);
                return;
            }
            byte[] bytes = new byte[130];
            int index = 0;
            bytes[index++] = (byte) ipBytes.length;
            bytes[index++] = 1;
            System.arraycopy(ipBytes, 0, bytes, index, ipBytes.length);
            mGDUAirLink.set4GServiceIp(bytes, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    GlobalVariable.BackAirLinkUrl = setIp;
                    SPUtils.put(GduAppEnv.application, SPUtils.BACK_AIR_LINK_URL,  GlobalVariable.BackAirLinkUrl);
                    toastLiveData.postValue(R.string.string_set_success);
                }

                @Override
                public void onFailure(GDUError gduError) {
                    serviceIpLiveData.postValue(GlobalVariable.BackAirLinkUrl);
                    toastLiveData.postValue(R.string.Label_SettingFail);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置遥控器P301D模块
     * @param type  1 设备重启  2 设置HDMI输出状态 3 获取HDMI输出状态
     * @param value  type为2时：HDMI输出状态  0：关 1：
     */
    public void setP301D(byte type, byte value){
        mGDUAirLink.setP301D(type, value, new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                p301DLiveData.postValue(integer == 1);
            }

            @Override
            public void onFailure(GDUError gduError) {
                if (type!=3) { //非获取模式
                    toastLiveData.postValue(R.string.Label_SettingFail);
                }
            }
        });
    }

    /**
     * 设置图传信道信息
     * */
    public void setImageTransmissionInfo(int position){
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
        mGDUAirLink.setImageTransmissionInfo(channel, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte integer) {
                isImgChannelSwitching = false;
                toastLiveData.postValue(R.string.string_set_success);
                if (DroneUtil.isS200Serials()) {
                    mS200SetChannel = finalChannel;
                }
                imageChannelLiveData.postValue((byte) finalChannel);
            }

            @Override
            public void onFailure(GDUError gduError) {
                isImgChannelSwitching = false;
                toastLiveData.postValue(R.string.Label_SettingFail);
            }
        });
    }

    public void getImageTransmissionInfo(int channel){
        if (DroneUtil.isS200Serials()) {
            mGDUAirLink.setImageTransmissionInfo(channel, new CommonCallbacks.CompletionCallbackWith<Byte>() {
                @Override
                public void onSuccess(Byte value) {
                    mS200SetChannel = value;
                    imageChannelLiveData.postValue(value);
                }

                @Override
                public void onFailure(GDUError gduError) {

                }
            });
        }
    }

    /**
     * 获取图传频率带宽
     * */
    public void getITFrequencyBandwidth(){
        mGDUAirLink.getITFrequencyBandwidth(new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                frequencyBandwidthLiveData.postValue(FrequencyBandwidth.get(aByte).getValue());
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 请求切换推流方式
     * @param type  1: rtmp   2: webrtc
     * */
    public void setLTEPushStreamType(byte type){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            toastLiveData.setValue(R.string.fly_no_conn);
            ltePushStreamTypeLiveData.postValue(true);
            return;
        }
        if (GlobalVariable.sFourthGStatus == null) {
            toastLiveData.setValue(R.string.string_5g_info_not_obtained);
            return;
        }
        if (type == GlobalVariable.sFourthGStatus.pushStreamType) {
            toastLiveData.setValue(R.string.string_config_not_changed);
            return;
        }
        mGDUAirLink.setLTEPushStreamType(type, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                GlobalVariable.sFourthGStatus.pushStreamType = type;
                toastLiveData.postValue(R.string.string_set_success);
                ltePushStreamTypeLiveData.postValue(true);
            }

            @Override
            public void onFailure(GDUError gduError) {
                toastLiveData.postValue(R.string.Label_SettingFail);
            }
        });
    }

    public int getS200SetChannel() {
        return mS200SetChannel;
    }

    public boolean isImgChannelSwitching() {
        return isImgChannelSwitching;
    }

    public MutableLiveData<Integer> getVisibleLightStreamLiveData() {
        return visibleLightStreamLiveData;
    }

    public MutableLiveData<Byte> getSteamSwitchLiveData() {
        return steamSwitchLiveData;
    }

    public MutableLiveData<Boolean> getOutputStreamLiveData() {
        return outputStreamLiveData;
    }

    public MutableLiveData<String> getServiceIpLiveData() {
        return serviceIpLiveData;
    }

    public MutableLiveData<Boolean> getP301DLiveData() {
        return p301DLiveData;
    }

    public MutableLiveData<String> getFrequencyBandwidthLiveData() {
        return frequencyBandwidthLiveData;
    }

    public MutableLiveData<Boolean> getLtePushStreamTypeLiveData() {
        return ltePushStreamTypeLiveData;
    }

    public MutableLiveData<Byte> getImageChannelLiveData() {
        return imageChannelLiveData;
    }
}
