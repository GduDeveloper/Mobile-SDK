package com.gdu.demo.viewmodel;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.GsonUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IGimbal;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.ai.TargetMode;
import com.gdu.msdk.key.value.bean.GimbalType;
import com.gdu.sdk.vision.listener.OnTargetDetectListener;
import java.util.List;

/**
 * @author wuqb
 * @date 2025/3/12
 * @description TODO
 */
public class FlightViewModel extends ViewModel {
    private static final String TAG = FlightViewModel.class.getSimpleName();
    private final MutableLiveData<Integer> toastLiveData = new MutableLiveData<>();

    public boolean isShowAiBox(){
        GimbalType gimbalType = IGimbal.get().getGimbalType();
        int lightType = DroneUtils.getLightType();
        // 仅可见光支持Ai识别的云台
        boolean isCustomSupportAiRecognizeGimbal1 = gimbalType == GimbalType.ByrdT_30X_Zoom
                || gimbalType == GimbalType.ByrdT_10X_Zoom
                || gimbalType == GimbalType.ByrdT_4k
                || gimbalType == GimbalType.ByrdT_10X_C_Zoom
                || gimbalType == GimbalType.GIMBAL_450J;
        // 仅可见光且在Debug模式才支持Ai识别的云台
        boolean isCustomSupportAiRecognizeGimbal2 = (gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || gimbalType == GimbalType.ByrT_6k
                || gimbalType == GimbalType.GIMBAL_8KC) && DroneUtils.isOpenTextEnvironment;
        // Debug模式才开放支持Ai识别的多光云台
        boolean isCustomSupportAiRecognizeGimbal3 = (gimbalType == GimbalType.Small_Double_Light
                || gimbalType == GimbalType.ByrT_IR_1K
                || gimbalType == GimbalType.ByrdT_TMS
                || gimbalType == GimbalType.GIMBAL_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW
                || gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PQL02_SE
                || gimbalType == GimbalType.GIMBAL_PTL600
                || gimbalType == GimbalType.GIMBAL_PDL_300C
                || gimbalType == GimbalType.GIMBAL_IR_1KG
                || gimbalType == GimbalType.GIMBAL_PWG01)
                && (lightType == 0x00 || lightType == 0x02 || lightType == 0x06)
                && DroneUtils.isOpenTextEnvironment;
        // 支持Ai识别的多光云台
        boolean isCustomSupportAiRecognizeGimbal4 = (gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_10X)
                && (lightType == 0x00 || lightType == 0x02 || lightType == 0x05 || lightType == 0x06);
        // 支持Ai识别的广角变焦双光云台(无红外)
        boolean isCustomSupportAiRecognizeGimbal5 = (gimbalType == GimbalType.GIMBAL_PDL_S200 || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640)
                && (lightType == 0x05 || lightType == 0x06);

        boolean hasAiBox = DroneUtils.getAiBoxOnline();
        // 支持AI识别云台
        final boolean isSupportAiRecognizeGimbal = isCustomSupportAiRecognizeGimbal1
                || isCustomSupportAiRecognizeGimbal2
                || isCustomSupportAiRecognizeGimbal3
                || isCustomSupportAiRecognizeGimbal4
                || isCustomSupportAiRecognizeGimbal5
                || hasAiBox;
        return !DroneUtils.isOpenFlightRoutePlan() && isSupportAiRecognizeGimbal;
    }

    public void switchAIRecognize() {
        boolean isAiOpen = false;
        if (IGduDroneDevice.get().getAiModel() != null && IGduDroneDevice.get().getAiModel().getAiTargetDetectState() != null
                && IGduDroneDevice.get().getAiModel().getAiTargetDetectState().getValue() != null) {
            isAiOpen = IGduDroneDevice.get().getAiModel().getAiTargetDetectState().getValue().isAiAlgorithmOpen();
        }
        if (isAiOpen) {
            stopTarget();
        } else {
            startTargetDetect();
        }
    }

    /**
     * 开始目标识别
     */
    public void startTargetDetect() {
        XLogger.INSTANCE.getAPP().i(TAG, "开始AI识别 - startTargetDetect() ");
        SdkDemoApplication.getAircraftInstance().getVision().startTargetDetect(errer -> {
            if (errer == null) {
                DroneUtils.setDiscernIsOpen(true);
                DroneUtils.setTargetDetectMode(true);
                toastLiveData.postValue(R.string.ai_box_open_success);
            } else {
                DroneUtils.setTargetDetectMode(false);
                toastLiveData.postValue(R.string.ai_box_open_fail);
            }
        });
    }

    public void stopTarget() {
        XLogger.INSTANCE.getAPP().i(TAG, "结束AI识别 -  stopTarget() ");
//        if (!DroneUtils.getAiBoxOnline()) {
//            return;
//        }
        SdkDemoApplication.getAircraftInstance().getVision().stopTargetDetect(error -> {
            if (null == error) {
                DroneUtils.setDiscernIsOpen(false);
                DroneUtils.setTargetDetectMode(false);
                toastLiveData.postValue(R.string.ai_detect_exit_success);
            } else {
                toastLiveData.postValue(R.string.ai_detect_exit_fail);
            }
        });
    }

    /**
     * 接收Ai识别SEI数据
     */
    public void registerSeiData() {
        log("开始接收Ai识别SEI数据 - registerSeiData() ");
        SdkDemoApplication.getAircraftInstance().getVision().setOnTargetDetectListener(new OnTargetDetectListener() {
            @Override
            public void onTargetDetecting(@Nullable List<TargetMode> list) {
                log("接收到SEI数据, size : " + (CollectionUtils.isEmptyList(list) ? 0 : list.size()) + ", data : "
                    + GsonUtils.toJson(list));
            }

            @Override
            public void onTargetDetectFailed(int i) {

            }

            @Override
            public void onTargetDetectStart() {

            }

            @Override
            public void onTargetDetectFinished() {

            }
        });
    }

    public MutableLiveData<Integer> getToastLiveData() {
        return toastLiveData;
    }

    private void log(String logStr){
        XLogger.INSTANCE.getAPP().i(TAG, logStr);
        Log.d(TAG,  logStr);
    }
}
