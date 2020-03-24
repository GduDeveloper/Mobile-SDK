package com.gdu.demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.api.GduControlManager;
import com.gdu.api.GduDroneApi;
import com.gdu.api.GduPlayView;
import com.gdu.api.GduSettingManager;
import com.gdu.api.gimbal.GimbalBean;
import com.gdu.api.gimbal.GimbalZoom30x;
import com.gdu.api.listener.OnDroneConnectListener;
import com.gdu.api.listener.OnPreviewListener;
import com.gdu.demo.util.SelectListDialog;
import com.gdu.demo.util.TestData;
import com.gdu.demo.util.ValueType;
import com.gdu.drone.FocusMotion;
import com.gdu.drone.FocusType;
import com.gdu.drone.GimbalType;
import com.gdu.drone.ZoomMotion;
import com.gdu.gdusocketmodel.GlobalVariable;
import com.gdu.util.logs.RonLog;


/**
 * Created by zhangzhilai on 2018/11/8.
 */

public class GimbalSettingZoom30Activity extends Activity implements View.OnClickListener {

    private GduPlayView mGduPlayView;
    private TextView mConnectStatusTextView;
    private TextView mInfoTextView;
    private Activity mContext;
    private GduSettingManager mGduSettingManager;

    private Button btn_ev;
    private Button btn_wb;
    private Button btn_iso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_gimbal_zoom_10);
        initView();
        initData();
        initListener();
    }

    private GimbalZoom30x gimbal;

    private void initData() {
        mGduSettingManager = new GduSettingManager();
        if (GlobalVariable.gimbalType == GimbalType.ByrdT_30X_Zoom)
            gimbal = (GimbalZoom30x) mGduSettingManager.getGimbal();
    }

    private void initListener() {
        GduDroneApi.getInstance().setOnDroneConnectListener(new OnDroneConnectListener() {
            @Override
            public void onConnectSucc() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接成功");
                    }
                });
            }

            @Override
            public void onConnectFail() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接失敗");
                    }
                });
            }

            @Override
            public void onDisConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接斷開");
                    }
                });
            }

            @Override
            public void onConnectMore() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("連接斷開");
                    }
                });
            }
        });
    }

    private void initView() {
        mConnectStatusTextView = (TextView) findViewById(R.id.connect_status_textview);
        mGduPlayView = (GduPlayView) findViewById(R.id.surface_view);
        mInfoTextView = (TextView) findViewById(R.id.info_textview);
        mGduPlayView.init(new OnPreviewListener() {
            @Override
            public void onStartPreviewSucceed() {
                toast("onStartPreviewSucceed");
            }

            @Override
            public void onStartPreviewFailed(int errorCode) {
                toast("onStartPreviewFailed " + errorCode);
            }

            @Override
            public void onStopPreviewSucceed() {
                toast("onStopPreviewSucceed");
            }

            @Override
            public void onStopPreviewFailed(int errorCode) {
                toast("onStopPreviewFailed " + errorCode);
            }
        });

        btn_ev = (Button) findViewById(R.id.btn_ev);
        btn_wb = (Button) findViewById(R.id.btn_wb);
        btn_iso = (Button) findViewById(R.id.btn_iso);

        btn_ev.setOnClickListener(this);
        btn_wb.setOnClickListener(this);
        btn_iso.setOnClickListener(this);

    }

    private SelectListDialog selectListDialog;
    private TestData testData;

    /****************
     * 云台调整
     * @param view
     */
    public void adjustGimbal(View view) {
        switch (view.getId()) {
            case R.id.btn_pitch_down:
                RonLog.LogE("=====btn_pitch_down");
                gimbal.adjustGimal((short) 250, (short) 128);
                break;
            case R.id.btn_pitch_up:
                gimbal.adjustGimal((short) 25, (short) 128);
                break;
            case R.id.btn_yaw_left:
                gimbal.adjustGimal((short) 128, (short) 28);
                break;
            case R.id.btn_yaw_right:
                gimbal.adjustGimal((short) 128, (short) 228);
                break;
            case R.id.btn_stopHolderControl:
                gimbal.stopGimal();
                break;
            case R.id.btn_fitch_set://先设置 俯仰30度
                gimbal.setGimbalPitchAngle((byte) -30, onControlListener);
                break;
            case R.id.btn_yaw_set://先设置，方位角度20度
                gimbal.setGimbalYawAngle((byte) 20, onControlListener);
                break;
            case R.id.btn_zoom_set:
                gimbal.setZoomValue(5, null);
                break;
        }

    }


    private GduControlManager.OnControlListener onControlListener = new GduControlManager.OnControlListener() {
        @Override
        public void onControlSucceed(Object type) {

        }

        @Override
        public void onControlFailed(int errorCode) {

        }
    };


    public void ZoomStop(View view) {
        gimbal.zoom(ZoomMotion.STOP_ZOOM, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {

            }

            @Override
            public void onSetFailed() {

            }
        });
    }


    @Override
    public void onClick(View view) {

        if (selectListDialog == null) {
            selectListDialog = new SelectListDialog();
            testData = new TestData(gimbal, mContext);
        }
        switch (view.getId()) {
            case R.id.btn_getCameraInfo:
                gimbal.getCameraArgs(getCameraInfo);
                break;
            case R.id.btn_ev:
                String strs[] = new String[testData.mZoom10xEVValues.length];
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = testData.mZoom10xEVValues[i].getValue();
                }
                selectListDialog.createDialog(strs,
                        mContext, ValueType.EV_10ZOOM, testData.onSelectValueListener);
                break;
            case R.id.btn_wb:
                selectListDialog.createDialog(testData.wb_str,
                        mContext, ValueType.WB, testData.onSelectValueListener);
                break;
            case R.id.btn_iso:
                String strs_iso[] = new String[testData.mZoom10xISOValues.length];
                for (int i = 0; i < strs_iso.length; i++) {
                    strs_iso[i] = testData.mZoom10xISOValues[i].getValue();
                }
                selectListDialog.createDialog(strs_iso,
                        mContext, ValueType.ISO_10Zoom, testData.onSelectValueListener);
                break;
            case R.id.btn_photoSize:
                String[] photosize = new String[testData.mZoom30_photoSize.length];
                for (int i = 0; i < photosize.length; i++) {
                    photosize[i] = testData.mZoom30_photoSize[i].getValue();
                }
                selectListDialog.createDialog(photosize, mContext, ValueType.PHOTOSIZE_Zoom30, testData.onSelectValueListener);
                break;

            case R.id.btn_videoSize:
                String[] videosize = new String[testData.mZoom30_videoSize.length];
                for (int i = 0; i < videosize.length; i++) {
                    videosize[i] = testData.mZoom30_videoSize[i].getValue();
                }
                selectListDialog.createDialog(videosize, mContext, ValueType.VIDEOSIZE_Zoom30, testData.onSelectValueListener);
                break;
        }
    }

    private GduSettingManager.OnSettingListener getCameraInfo = new GduSettingManager.OnSettingListener() {
        @Override
        public void onSetSucceed(final Object data) {
            if (data != null) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String Str = ((GimbalBean) data).toString() + ",zoom:" + gimbal.getZoomValue();
                        mInfoTextView.setText(Str);
                    }
                });
            }
        }

        @Override
        public void onSetFailed() {
            toast("获取相机参数失败");
        }
    };

    public void start_preview(View view) {
        mGduPlayView.startPreview();
    }

    public void stop_preview(View view) {
        mGduPlayView.stopPreview();
    }


    public void toast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGduPlayView.beginRecord("/mnt/sdcard/gdu", "ron.mp4");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGduPlayView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGduPlayView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGduPlayView.onDestroy();
    }


    public void facusByZone(View view) {
        if (gimbal != null) {
            gimbal.focusByZone((byte) 0, (byte) 0, (byte) 50, (byte) 50, new GduSettingManager.OnSettingListener() {
                @Override
                public void onSetSucceed(Object data) {

                }

                @Override
                public void onSetFailed() {

                }
            });
        }
    }

    public void ZoomAdd_X(View view) {
        zoom(ZoomMotion.ADD_ZOOM);
    }

    public void ZoomSub_X(View view) {
        zoom(ZoomMotion.SUB_ZOOM);
    }

    private void zoom(ZoomMotion zoomMotion) {
        if (gimbal != null) {
            gimbal.zoom(zoomMotion, new GduSettingManager.OnSettingListener() {
                @Override
                public void onSetSucceed(Object data) {
                    toast("变倍成功 " + data.toString());
                }

                @Override
                public void onSetFailed() {
                    toast("变倍失败");
                }
            });
        }

    }

    public void FocusAdd_X(View view) {
        focus(FocusMotion.ADD_FOCUS);
    }

    public void FocusSub_X(View view) {
        focus(FocusMotion.SUB_FOCUS);
    }

    private void focus(FocusMotion focusMotion) {
        if (gimbal != null) {
            gimbal.focus(FocusType.MANUAL_FOCUS, focusMotion, new GduSettingManager.OnSettingListener() {
                @Override
                public void onSetSucceed(Object data) {
                    toast("变焦成功 " + data.toString());
                }

                @Override
                public void onSetFailed() {
                    toast("变焦失败");
                }
            });
        }
    }
}
