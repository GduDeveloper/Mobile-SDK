package com.gdu.demo.flight.setting.camera;

import android.os.Handler;
import android.os.Message;

import com.gdu.config.GduConfig;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.util.logger.MyLogUtils;

/**
 * Created by yuhao on 2017/4/13.
 */
public class CameraSetBiz {

    private final ICameraSetBiz iCameraSetBiz;

    private final int SET_GIMBAL_MODE_OK = 23;
    private final int SET_GIMBAL_MODE_FAILE = 24;

    public CameraSetBiz(ICameraSetBiz iCameraSetBiz) {
        this.iCameraSetBiz = iCameraSetBiz;
    }

    /**
     * 发送清楚数据的指令
     *
     *   type  0x01:格式化红外SD卡 0x02:格式化可见光SD卡；3：红外+可见光都格式化
     */
    public void clearMediaData(byte type, SocketCallBack3 callBack) {
//        GduApplication.getSingleApp().gduCommunication.clearSDMedia(callBack, type);
    }

    public void setGimbalMode(byte index){
//        GduApplication.getSingleApp().gduCommunication.setGimbalMode(index, sGimbalModeCallback);
    }

    private final SocketCallBack3 sGimbalModeCallback = new SocketCallBack3() {
        @Override
        public void callBack(int code, GduFrame3 bean) {
            MyLogUtils.i("setGimbalMode callBack() code = " + code);
            if (code == GduConfig.OK) {
                handler.sendEmptyMessage(SET_GIMBAL_MODE_OK);
            }else{
                handler.sendEmptyMessage(SET_GIMBAL_MODE_FAILE);
            }
        }
    };

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SET_GIMBAL_MODE_OK:
                    iCameraSetBiz.setGimbalModeCallback(0);
                    break;
                case SET_GIMBAL_MODE_FAILE:
                    iCameraSetBiz.setGimbalModeCallback(1);
                    break;
                default:
                    break;
            }
            return false;
        }
    });
}
