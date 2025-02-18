package com.gdu.demo.flight.setting.camera;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.gdu.event.GimbalEvent;
import com.lib.model.LiveType;


/**
 * Created by yuhao on 2017/4/12.
 */
public class CameraSetHelper {

    protected View mView;
    protected FragmentActivity mActivity;
    protected TxVideoLiveListener txVideoLiveListener;

    public CameraSetHelper() {

    }

    public CameraSetHelper(View view, FragmentActivity activity) {
        mView = view;
        this.mActivity = activity;
    }


    public void onDestory() {
        mView = null;
        mActivity = null;
    }

    public void connGimbalListener(GimbalEvent event) {

    }

    /**
     * 设置推流地址
     * @param url
     */
    public void setRtmpUrl(String url){

    }

    public interface TxVideoLiveListener {
        void openTxVideoLive(LiveType liveType, String rtmpUrl);
        void isliveUIShow( boolean isShow );

        void openQRCode();
    }

    public void showToast(int strId){
        if (mActivity == null) {
            return;
        }
       showToast(mActivity.getString(strId));
    }

    public void showToast(String str){
        if (mActivity == null) {
            return;
        }
        mActivity.runOnUiThread(() -> Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show());
    }
}
