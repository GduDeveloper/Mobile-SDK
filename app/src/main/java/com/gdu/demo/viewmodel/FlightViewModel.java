package com.gdu.demo.viewmodel;

import android.os.Message;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.AlgorithmMark;
import com.gdu.camera.LightType;
import com.gdu.common.error.Error;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.detect.AIModelState;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IGimbal;
import com.gdu.msdk.key.value.bean.GimbalType;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.socketmodel.GduSocketConfig3;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author wuqb
 * @date 2025/3/12
 * @description TODO
 */
public class FlightViewModel extends ViewModel {
    private final MutableLiveData<Integer> toastLiveData = new MutableLiveData<>();

    public boolean isShowAiBox(){
        GimbalType gimbalType = IGimbal.get().getGimbalType();
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
                && (GlobalVariable.sCameraLightType == 0x00 || GlobalVariable.sCameraLightType == 0x02
                || GlobalVariable.sCameraLightType == 0x06)
                && DroneUtils.isOpenTextEnvironment;
        // 支持Ai识别的多光云台
        boolean isCustomSupportAiRecognizeGimbal4 = (gimbalType == GimbalType.GIMBAL_PDL_S220
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || gimbalType == GimbalType.GIMBAL_PDL_10X)
                && (GlobalVariable.sCameraLightType == 0x00
                || GlobalVariable.sCameraLightType == 0x02
                || GlobalVariable.sCameraLightType == 0x05
                || GlobalVariable.sCameraLightType == 0x06);
        // 支持Ai识别的广角变焦双光云台(无红外)
        boolean isCustomSupportAiRecognizeGimbal5 = (gimbalType == GimbalType.GIMBAL_PDL_S200 || gimbalType == GimbalType.GIMBAL_PDL_S200_IR640)
                && (GlobalVariable.sCameraLightType == 0x05
                || GlobalVariable.sCameraLightType == 0x06);

        boolean hasAiBox = GlobalVariable.otherCompId == GduSocketConfig3.AI_BOX;
        // 支持AI识别云台
        final boolean isSupportAiRecognizeGimbal = isCustomSupportAiRecognizeGimbal1
                || isCustomSupportAiRecognizeGimbal2
                || isCustomSupportAiRecognizeGimbal3
                || isCustomSupportAiRecognizeGimbal4
                || isCustomSupportAiRecognizeGimbal5
                || hasAiBox;
        return !GlobalVariable.isOpenFlightRoutePlan && isSupportAiRecognizeGimbal;
    }

    public void switchAIRecognize(){
        startTargetDetect(GlobalVariable.mCurrentLightType);
    }

    /**
     * 开始目标识别
     * @param lightType
     */
    public void startTargetDetect(LightType lightType) {
        XLogger.INSTANCE.getAPP().i("startTargetDetect() lightType = " + lightType);
        setAIBoxTargetDetect((byte) 0x01);
        SdkDemoApplication.getAircraftInstance().getGduVision().startTargetDetect((byte) lightType.getKey(), gduError -> {
                    XLogger.INSTANCE.getAPP().i("targetDetect callBack() code = " + gduError);
                    if (gduError == null) {
                        GlobalVariable.discernIsOpen = true;
                        DroneUtils.setTargetDetectMode(true);
                        toastLiveData.postValue(R.string.ai_box_open_success);
                    }else {
                        DroneUtils.setTargetDetectMode(false);
                        toastLiveData.postValue(R.string.ai_box_open_fail);
                    }
                });
    }

    /**
     * 开关AI盒子算法模型 0x02840038
     * @param detectType
     */
    public void setAIBoxTargetDetect(byte detectType) {
//        for (int i = 0; i < GlobalVariable.targetDetectModelState.size(); i++) {
//            AIModelState modelState = GlobalVariable.targetDetectModelState.get(i);
//            SdkDemoApplication.getAircraftInstance().getGduVision().setAIBoxTargetType(modelState.getModelId(), detectType, (short) modelState.getCount(), modelState.getLabelState(), new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(GDUError gduError) {
//                    AppLog.e("TargetDetectHelper", "setAIBoxTargetDetect modelId " + modelState.getModelId() + " detectType " + detectType + "  callBack() code = " + gduError);
//                }
//            });
//        }
    }

    public void stopTarget(byte stopType, LightType lightType) {
        XLogger.INSTANCE.getAPP().i("stopTarget() stopType = " + stopType + "; lightType = " + lightType);
        setTargetDetect((byte) 0x00);
        if (GlobalVariable.otherCompId != GduSocketConfig3.AI_BOX) {
            return;
        }
        SdkDemoApplication.getAircraftInstance().getGduVision().stopTargetDetect((byte) lightType.getKey(), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(Error error) {
                if (error == null){
                }
            }
        });
    }

    private void setTargetDetect(byte detectType) {
        byte[] typeArray;
        if (detectType == 0x01) { // 打开时默认全开
            typeArray = new byte[3];
            for (int i = 0; i < 3; i++) {
                typeArray[i] = 0x01;
            }
        } else {
            typeArray = new byte[3];
        }
        XLogger.INSTANCE.getAPP().i("TargetDetectHelper", "setTargetDetect aiRecognitionSwitch.first = " + GlobalVariable.aiRecognitionSwitch.first);
        if (GlobalVariable.aiRecognitionSwitch.first == 0x0C) {
            SdkDemoApplication.getAircraftInstance().getGduVision().setTargetType((byte) 0x01, detectType, (short) 3, typeArray, gduError -> {
                if (null == gduError){
                    if (detectType == 0x01) {
                        GlobalVariable.discernIsOpen = true;
                        DroneUtils.setTargetDetectMode(true);
                    }else {
                    }
                }else {
                    if (detectType == 0x01) {
                        DroneUtils.setTargetDetectMode(false);
                    }
                }
            });
        } else {
            SdkDemoApplication.getAircraftInstance().getGduVision().setAITargetType((byte) 0x00, detectType, (short) 3, typeArray, gduError -> {
                if (null == gduError){
                    if (detectType == 0x01) {
                        GlobalVariable.discernIsOpen = true;
                        DroneUtils.setTargetDetectMode(true);
                    }else {
                    }
                }else {
                    if (detectType == 0x01) {
                        DroneUtils.setTargetDetectMode(false);
                    }
                }
            });
        }
    }

    public MutableLiveData<Integer> getToastLiveData() {
        return toastLiveData;
    }
}
