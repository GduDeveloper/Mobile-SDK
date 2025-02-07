package com.gdu.demo.flight.setting.camera;

/**
 * Created by yuhao on 2017/4/13.
 */
public class CameraSetPresenter implements ICameraSetBiz{

    private final CameraSetBiz cameraSetBiz;
    private final ICamreaSetView iCamreaSetView;

    public CameraSetPresenter(ICamreaSetView iCamreaSetView){
        this.iCamreaSetView=iCamreaSetView;
        cameraSetBiz = new CameraSetBiz(this);
    }

    /**
     * 设置云台模式
     * @param mode
     */
    public void setGimbalMode(byte mode){
        cameraSetBiz.setGimbalMode(mode);
    }

    @Override
    public void setGimbalModeCallback(int type) {
        iCamreaSetView.setGimbalModeResult(type);
    }
}
