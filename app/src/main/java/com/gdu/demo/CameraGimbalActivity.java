package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.camera.StorageState;
import com.gdu.common.error.GDUError;
import com.gdu.gimbal.GimbalState;
import com.gdu.gimbal.Rotation;
import com.gdu.gimbal.RotationMode;
import com.gdu.sdk.camera.CameraMode;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.gimbal.GDUGimbal;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * Created by zhangzhilai on 2018/5/31.
 * 2. 图传测试界面
 */

public class CameraGimbalActivity extends Activity implements TextureView.SurfaceTextureListener {


    private VideoFeeder.VideoDataListener videoDataListener = null;
    private GDUCodecManager codecManager = null;

    private TextureView mGduPlayView;
    private TextView mInfoTextView;
    private TextView mStorageInfoTextView;
    private TextView mVersionTextView;


    private TextView mGimbalStateTextView;
    private Context mContext;
    private GDUCamera mGDUCamera;

    private GDUGimbal mGDUGimbal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_camera_gimbal);
        initView();
        initData();
        initListener();
//        timeShow();
    }

    private void initData() {
        try {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
        } catch (Exception ignored) {
        }
        initCamera();
        initGimbal();
    }

    private void initGimbal() {
        mGDUGimbal = (GDUGimbal) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getGimbal();
        mGDUGimbal.setStateCallback(new GimbalState.Callback() {
            @Override
            public void onUpdate(GimbalState state) {
                StringBuilder s = new StringBuilder();
                s.append(" Attitude pitch ");
                s.append(state.getAttitudeInDegrees().pitch);
                s.append(" Attitude roll ");
                s.append(state.getAttitudeInDegrees().roll);
                s.append(" Attitude yaw ");
                s.append(state.getAttitudeInDegrees().yaw);
                s.append(" isCalibrating ");
                s.append(state.isCalibrating());
                show(mGimbalStateTextView, s.toString());
            }
        });
    }

    private void initCamera() {
        mGDUCamera = (GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera();
        if (mGDUCamera != null) {
            mGDUCamera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState systemState) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" isPhotoStored ");
                    sb.append(systemState.isPhotoStored());
                    sb.append(" hasError ");
                    sb.append(systemState.isHasError());
                    sb.append(" isRecording ");
                    sb.append(systemState.isRecording());
                    sb.append(" mode ");
                    sb.append(systemState.getMode());
                    sb.append(" time ");
                    sb.append(systemState.getCurrentVideoRecordingTimeInSeconds());
                    show(mInfoTextView, sb.toString());
                }
            });
            mGDUCamera.setStorageStateCallBack(new StorageState.Callback() {
                @Override
                public void onUpdate(StorageState state) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" isFormatting ");
                    sb.append(state.isFormatting());
                    sb.append(" isFormatted ");
                    sb.append(state.isFormatted());
                    sb.append(" TotalSpace ");
                    sb.append(state.getTotalSpace());
                    sb.append(" RemainingSpace ");
                    sb.append(state.getRemainingSpace());
                    show(mStorageInfoTextView, sb.toString());
                }
            });
        }
    }


    private void initListener() {

    }


    private void initView() {
        mGduPlayView = findViewById(R.id.video_texture_view);
        mGduPlayView.setOpaque(false);
        mInfoTextView = (TextView) findViewById(R.id.camera_info_textview);
        mStorageInfoTextView = (TextView) findViewById(R.id.camera_storage_info_textview);
        mVersionTextView = (TextView) findViewById(R.id.version_textview);


        mGimbalStateTextView = (TextView) findViewById(R.id.gimbal_info_textview);

        if (mGduPlayView != null) {
            mGduPlayView.setSurfaceTextureListener(this);
            videoDataListener = new VideoFeeder.VideoDataListener() {
                @Override
                public void onReceive(byte[] bytes, int size) {
                    if (null != codecManager) {
                        codecManager.sendDataToDecoder(bytes, size);
                    }
                }
            };
        }

    }

    public void toast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(TextView textView, final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(toast);
//                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record_video:
                mGDUCamera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {
                        toast("开始录像成功");
                    }
                });
                break;
            case R.id.btn_stop_record_video:
                mGDUCamera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {
                        toast("停止录像成功");
                    }
                });
                break;
            case R.id.btn_single_take_picture:
                mGDUCamera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {
                        toast("拍照发送成功");
                    }
                });
                break;
            case R.id.btn_model_change:
                mGDUCamera.setMode(CameraMode.RECORD_VIDEO, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {
                        toast("模式发送成功");
                    }
                });
                break;
            case R.id.btn_format_sd_card:
                mGDUCamera.formatSDCard(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {
                        toast("格式化SD发送成功");
                    }
                });
                break;
            case R.id.btn_get_version:
                mGDUCamera.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String version) {
                        show(mVersionTextView, version);
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        show(mVersionTextView, "fail");
                    }
                });
                break;
            case R.id.btn_reset:
                mGDUGimbal.reset(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("发送成功");
                        } else {
                            toast("发送失败");
                        }
                    }
                });
                break;
            case R.id.btn_rotate:  //TODO 俯仰，方位会变
                Rotation rotation = new Rotation();
                rotation.setMode(RotationMode.ABSOLUTE_ANGLE);
                rotation.setPitch(90);
//                rotation.set
                mGDUGimbal.rotate(rotation, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("发送成功");
                        } else {
                            toast("发送失败");
                        }
                    }
                });
                break;
            case R.id.btn_get_sn:
                mGDUGimbal.getGimbalSN(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String sn) {
                        toast("sn：" + sn);
                    }

                    @Override
                    public void onFailure(GDUError var1) {

                    }
                });
                break;
            case R.id.btn_get_gimbal_version:
                mGDUGimbal.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                    @Override
                    public void onSuccess(String version) {
                        toast("version：" + version);
                    }

                    @Override
                    public void onFailure(GDUError var1) {

                    }
                });
                break;
            case R.id.btn_start_calibration:
                mGDUGimbal.startCalibration(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("发送成功");
                        } else {
                            toast("发送失败");
                        }
                    }
                });
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mGduPlayView.beginRecord("/mnt/sdcard/gdu","ron.mp4");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new GDUCodecManager(mContext, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
