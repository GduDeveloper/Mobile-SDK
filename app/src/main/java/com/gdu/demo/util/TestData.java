package com.gdu.demo.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.gdu.api.GduSettingManager;
import com.gdu.api._enum.EvValue;
import com.gdu.api._enum.ISO_Value;
import com.gdu.api._enum.PhotoDpi;
import com.gdu.api._enum.VideoDpi;
import com.gdu.api.gimbal.Gimbal;
import com.gdu.api.gimbal.GimbalParamManager;
import com.gdu.api.gimbal.Gimbalo2;
import com.gdu.drone.PreviewResolution;

/**
 * Created by Woo on 2018-11-13.
 */

public class TestData {
    private Gimbal gimbal;
    public String[] ev_str_o2 = {"0","-3","-2","-1","1","2","3"};
    public String[] wb_str ={"auto","白炽灯","荧光灯","日光","阴天"};
    public PhotoDpi[] photoDpis_o2 = {PhotoDpi.dpi_1920x1080, PhotoDpi.dpi_3840x2160,
            PhotoDpi.dpi_4208x3120};
    public VideoDpi[] videoDpis_o2 = {VideoDpi.dpi_720P_30, VideoDpi.dpi_1080P_30,
            VideoDpi.dpi_3840x2160_25};
    public PreviewResolution[] mPreviewResolution = {PreviewResolution.SMOOTH_MODE, PreviewResolution.SD_MODE,
    PreviewResolution.HD_MODE, PreviewResolution.FHD_MODE};


    public EvValue[] m4KEVValues = new EvValue[]{EvValue.Ev_2, EvValue.Ev_1P7, EvValue.Ev_1P3, EvValue.Ev_1,
            EvValue.Ev_P7, EvValue.Ev_P3, EvValue.Ev0, EvValue.EvP3, EvValue.EvP7,
            EvValue.Ev1, EvValue.Ev1P3, EvValue.Ev1P7, EvValue.Ev2};
    public ISO_Value[] m4KISOValues = new ISO_Value[]{ISO_Value.ISO_auto, ISO_Value.ISO_50,
            ISO_Value.ISO_100, ISO_Value.ISO_200, ISO_Value.ISO_400, ISO_Value.ISO_800,
            ISO_Value.ISO_1600, ISO_Value.ISO_3200, ISO_Value.ISO_6400};

    public VideoDpi[] m4k_videoSize = {VideoDpi.dpi_720P_60, VideoDpi.dpi_1080P_30, VideoDpi.dpi_1080P_60
    , VideoDpi.dpi_3840x2160_25, VideoDpi.dpi_3840x2160_30};

    public PhotoDpi[] m4k_photoSize = {PhotoDpi.dpi_4000x3000};



    public EvValue[] mZoom10xEVValues = new EvValue[]{EvValue.Ev_2, EvValue.Ev_1, EvValue.Ev0, EvValue.Ev1, EvValue.Ev2};
    public ISO_Value[] mZoom10xISOValues = new ISO_Value[]{ISO_Value.ISO_auto,
            ISO_Value.ISO_100, ISO_Value.ISO_200, ISO_Value.ISO_400, ISO_Value.ISO_800,
            ISO_Value.ISO_1600, ISO_Value.ISO_3200};
    public VideoDpi[] mZoom10_videoSize = {VideoDpi.dpi_720P_60, VideoDpi.dpi_720P_120, VideoDpi.dpi_1080P_30, VideoDpi.dpi_1080P_60
            , VideoDpi.dpi_3840x2160_25};


    public VideoDpi[] mZoom30_videoSize = {VideoDpi.dpi_720P_60, VideoDpi.dpi_1080P_30, VideoDpi.dpi_1080P_60
            , VideoDpi.dpi_3840x2160_30, VideoDpi.dpi_4096x2160_30};

    public PhotoDpi[] mZoom10_photoSize = {PhotoDpi.dpi_4000x3000, PhotoDpi.dpi_4608x3456, PhotoDpi.dpi_3840x2160};
    public PhotoDpi[] mZoom30_photoSize = {PhotoDpi.dpi_4000x3000, PhotoDpi.dpi_4608x3456, PhotoDpi.dpi_3840x2160, PhotoDpi.dpi_1920x1080};

    private GimbalParamManager gimbalParamManager;
    private Activity activity;
    public TestData(Gimbal gimbal, Activity activity)
    {
        this.activity = activity;
        this.gimbal = gimbal;
        gimbalParamManager = new GimbalParamManager();
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 999:
                    Toast.makeText(activity,msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public OnSelectValueListener onSelectValueListener = new OnSelectValueListener() {
        @Override
        public void onSelectValue(int positon, ValueType valueType) {
            switch (valueType)
            {
                case EV_4K:
                    gimbal.setEV(m4KEVValues[positon],onSettingListener);
                case EV_10ZOOM:
                    gimbal.setEV(mZoom10xEVValues[positon],onSettingListener);
                    break;
                case EV_O2:
                    ((Gimbalo2)gimbal).setEV_o2(gimbalParamManager.getEV_o2Byindex(positon),onSettingListener);
                    break;
                case WB:
                    gimbal.setWB(gimbalParamManager.getWB_ValueByIndex(positon),onSettingListener);
                    break;
                case ISO_4k:
                    gimbal.setISO(m4KISOValues[positon],onSettingListener);
                    break;
                case ISO_10Zoom:
                    gimbal.setISO(mZoom10xISOValues[positon],onSettingListener);
                    break;
                case VIDEOSIZE_o2:
                    gimbal.setVideoDpi(videoDpis_o2[positon],onSettingListener);
                    break;
                case PHOTOSIZE_o2:
                    gimbal.setPhotoDpi(photoDpis_o2[positon],onSettingListener);
                    break;
                case VIDEOSIZE_4k:
                    gimbal.setVideoDpi(m4k_videoSize[positon],onSettingListener);
                    break;
                case PHOTOSIZE_4k:
                    gimbal.setPhotoDpi(m4k_photoSize[positon],onSettingListener);
                    break;
                case VIDEOSIZE_Zoom10:
                    gimbal.setVideoDpi(mZoom10_videoSize[positon],onSettingListener);
                    break;
                case PHOTOSIZE_Zoom10:
                    gimbal.setPhotoDpi(mZoom10_photoSize[positon],onSettingListener);
                    break;
                case VIDEOSIZE_Zoom30:
                    gimbal.setVideoDpi(mZoom30_videoSize[positon],onSettingListener);
                    break;
                case PHOTOSIZE_Zoom30:
                    gimbal.setPhotoDpi(mZoom30_photoSize[positon],onSettingListener);
                    break;
                case PREVIEW_SIZE_O2:
                    gimbal.setPreViewDpi(mPreviewResolution[positon], onSettingListener);
                    break;
            }
        }
    };

    private GduSettingManager.OnSettingListener onSettingListener = new GduSettingManager.OnSettingListener() {
        @Override
        public void onSetSucceed(Object data) {
            handler.obtainMessage(999,"设置成功").sendToTarget();
        }

        @Override
        public void onSetFailed() {
            handler.obtainMessage(999,"设置失败").sendToTarget();
        }
    };


    public static interface OnSelectValueListener
    {
        public void onSelectValue(int positon, ValueType valueType);
    }
}
