package com.gdu.demo.viewmodel;

import android.os.Message;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.AlgorithmMark;
import com.gdu.camera.LightType;
import com.gdu.common.error.GDUError;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.detect.AIModelState;
import com.gdu.drone.GimbalType;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author wuqb
 * @date 2025/3/12
 * @description TODO
 */
public class FlightViewModel extends ViewModel {
    private final MutableLiveData<Integer> toastLiveData = new MutableLiveData<>();

    public boolean isShowAiBox(){
        // 仅可见光支持Ai识别的云台
        boolean isCustomSupportAiRecognizeGimbal1 = GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrdT_10X_Zoom
                || GlobalVariable.gimbalType == GimbalType.ByrdT_4k
                || GlobalVariable.gimbalType == GimbalType.ByrdT_10X_C_Zoom
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_450J;
        // 仅可见光且在Debug模式才支持Ai识别的云台
        boolean isCustomSupportAiRecognizeGimbal2 = (GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom_NEW
                || GlobalVariable.gimbalType == GimbalType.ByrT_6k
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_8KC) && UavStaticVar.isOpenTextEnvironment;
        // Debug模式才开放支持Ai识别的多光云台
        boolean isCustomSupportAiRecognizeGimbal3 = (GlobalVariable.gimbalType == GimbalType.Small_Double_Light
                || GlobalVariable.gimbalType == GimbalType.ByrT_IR_1K
                || GlobalVariable.gimbalType == GimbalType.ByrdT_TMS
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_FOUR_LIGHT_NEW
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_MICRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PQL02_SE
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL600
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_300C
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_IR_1KG
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PWG01)
                && (GlobalVariable.sCameraLightType == 0x00 || GlobalVariable.sCameraLightType == 0x02
                || GlobalVariable.sCameraLightType == 0x06)
                && UavStaticVar.isOpenTextEnvironment;
        // 支持Ai识别的多光云台
        boolean isCustomSupportAiRecognizeGimbal4 = (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_SX_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S220PRO_IR640_FOUR_LIGHT
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PTL_S220_IR640
                || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_10X)
                && (GlobalVariable.sCameraLightType == 0x00
                || GlobalVariable.sCameraLightType == 0x02
                || GlobalVariable.sCameraLightType == 0x05
                || GlobalVariable.sCameraLightType == 0x06);
        // 支持Ai识别的广角变焦双光云台(无红外)
        boolean isCustomSupportAiRecognizeGimbal5 = (GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200 || GlobalVariable.gimbalType == GimbalType.GIMBAL_PDL_S200_IR640)
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
        MyLogUtils.i("startTargetDetect() lightType = " + lightType);
        setAIBoxTargetDetect((byte) 0x01);
        SdkDemoApplication.getAircraftInstance().getGduVision().startTargetDetect((byte) lightType.getKey(), gduError -> {
                    MyLogUtils.i("targetDetect callBack() code = " + gduError);
                    if (gduError == null) {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.DEVICE_RECOGNISE;
                        GlobalVariable.discernIsOpen = true;
                        GlobalVariable.isTargetDetectMode = true;
                        toastLiveData.postValue(R.string.ai_box_open_success);
                    }else {
                        GlobalVariable.isTargetDetectMode = false;
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
        MyLogUtils.i("stopTarget() stopType = " + stopType + "; lightType = " + lightType);
        setTargetDetect((byte) 0x00);
        if (GlobalVariable.otherCompId != GduSocketConfig3.AI_BOX) {
            return;
        }
        SdkDemoApplication.getAircraftInstance().getGduVision().stopTargetDetect((byte) lightType.getKey(), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError gduError) {
                if (gduError == null){
                    GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.NONE;
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
        AppLog.e("TargetDetectHelper", "setTargetDetect aiRecognitionSwitch.first = " + GlobalVariable.aiRecognitionSwitch.first);
        if (GlobalVariable.aiRecognitionSwitch.first == 0x0C) {
            SdkDemoApplication.getAircraftInstance().getGduVision().setTargetType((byte) 0x01, detectType, (short) 3, typeArray, gduError -> {
                if (null == gduError){
                    if (detectType == 0x01) {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.DEVICE_RECOGNISE;
                        GlobalVariable.discernIsOpen = true;
                        GlobalVariable.isTargetDetectMode = true;
                    }else {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.NONE;
                    }
                }else {
                    if (detectType == 0x01) {
                        GlobalVariable.isTargetDetectMode = false;
                    }
                }
            });
        } else {
            SdkDemoApplication.getAircraftInstance().getGduVision().setAITargetType((byte) 0x00, detectType, (short) 3, typeArray, gduError -> {
                if (null == gduError){
                    if (detectType == 0x01) {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.DEVICE_RECOGNISE;
                        GlobalVariable.discernIsOpen = true;
                        GlobalVariable.isTargetDetectMode = true;
                    }else {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.NONE;
                    }
                }else {
                    if (detectType == 0x01) {
                        GlobalVariable.isTargetDetectMode = false;
                    }
                }
            });
        }
    }

    public MutableLiveData<Integer> getToastLiveData() {
        return toastLiveData;
    }
}
