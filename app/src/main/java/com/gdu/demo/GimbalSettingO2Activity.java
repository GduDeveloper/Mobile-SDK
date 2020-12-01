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
import com.gdu.api.gimbal.Gimbal;
import com.gdu.api.gimbal.Gimbal4K;
import com.gdu.api.gimbal.GimbalBean;
import com.gdu.api.gimbal.GimbalZoom10x;
import com.gdu.api.gimbal.GimbalZoom30x;
import com.gdu.api.gimbal.Gimbalo2;
import com.gdu.api.listener.OnDroneConnectListener;
import com.gdu.api.listener.OnPreviewListener;
import com.gdu.api.listener.OnTakePictureListener;
import com.gdu.demo.util.SelectListDialog;
import com.gdu.demo.util.TestData;
import com.gdu.demo.util.ValueType;
import com.gdu.util.logs.RonLog;


/**
 * Created by zhangzhilai on 2018/11/8.
 */

public class GimbalSettingO2Activity extends Activity  implements View.OnClickListener{

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
        setContentView(R.layout.activity_gimbal_setting);
        initView();
        initData();
        initListener();
    }

    private Gimbal gimbal;
    private void initData() {
        mGduSettingManager = new GduSettingManager();
         gimbal = mGduSettingManager.getGimbal();
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

        mGduPlayView.setOnTakePictureListener(new OnTakePictureListener() {
            @Override
            public void onTakePictureSucceed(final String name) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusTextView.setText("拍照成功:"+ name);
                    }
                });
            }

            @Override
            public void onTakePictureFailed(int errorCode) {

            }
            @Override
            public String getPictureName() {
                return null;
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

        btn_ev =(Button)findViewById(R.id.btn_ev);
        btn_wb =(Button)findViewById(R.id.btn_wb);
        btn_iso =(Button)findViewById(R.id.btn_iso);

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
    public void adjustGimbal(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_pitch_down://设置云台向下俯仰的速度
                RonLog.LogE("=====btn_pitch_down");
                gimbal.adjustGimal((short) 250,(short) 128);
                break;
            case R.id.btn_pitch_up:
                gimbal.adjustGimal((short) 25,(short) 128);
                break;
            case R.id.btn_stopHolderControl:
                gimbal.stopGimal();
                break;
            case R.id.btn_fitch_set:
                gimbal.setGimbalPitchAngle((byte) -30,onControlListener);
                break;
            case R.id.btn_photo_zoom:
                ((Gimbalo2)gimbal).takePhoto_Scale(null);
                break;
            case R.id.btn_photo_5:
                ((Gimbalo2)gimbal).takePhoto_continuous((byte)5,null);
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


        @Override
        public void onClick(View view)
        {
            if(selectListDialog == null )
            {
                selectListDialog = new SelectListDialog();
                testData = new TestData(gimbal,mContext);
            }
            switch (view.getId())
            {
                case R.id.btn_getCameraInfo:
                    gimbal.getCameraArgs(getCameraInfo);
                    break;
                case R.id.btn_ev:
                    selectListDialog.createDialog(testData.ev_str_o2,
                            mContext, ValueType.EV_O2,testData.onSelectValueListener);
                    break;
                case R.id.btn_wb:
                    selectListDialog.createDialog(testData.wb_str,
                            mContext, ValueType.WB,testData.onSelectValueListener);
                    break;
                case R.id.btn_iso:
                    //O2云台 不支持 ISO设置
                    break;
                case R.id.btn_photoSize:
                    String[] photosize = new String[testData.photoDpis_o2.length];
                    for (int i = 0; i < photosize.length ; i ++ )
                    {
                        photosize[i] = testData.photoDpis_o2[i].getValue();
                    }
                    selectListDialog.createDialog(photosize,mContext,ValueType.PHOTOSIZE_o2,testData.onSelectValueListener);
                    break;

                case R.id.btn_videoSize:
                    String[] videosize = new String[testData.videoDpis_o2.length];
                    for (int i = 0; i < videosize.length ; i ++ )
                    {
                        videosize[i] = testData.videoDpis_o2[i].getValue();
                    }
                    selectListDialog.createDialog(videosize,mContext,ValueType.VIDEOSIZE_o2,testData.onSelectValueListener);
                    break;
                case  R.id.btn_previewSize:
                    String[] preSize = new String[4];
                    preSize[0] = "流畅";
                    preSize[1] = "标清";
                    preSize[2] = "高清";
                    preSize[3] = "超清";
                    selectListDialog.createDialog(preSize, mContext, ValueType.PREVIEW_SIZE_O2,testData.onSelectValueListener);
                    break;

            }
        }

    private GduSettingManager.OnSettingListener getCameraInfo = new GduSettingManager.OnSettingListener() {
        @Override
        public void onSetSucceed(final Object data) {
            if(data != null)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       mInfoTextView.setText(((GimbalBean)data).toString());
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

}
