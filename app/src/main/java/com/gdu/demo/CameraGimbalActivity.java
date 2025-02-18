package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.gdu.camera.Capabilities;
import com.gdu.camera.SettingsDefinitions;
import com.gdu.camera.StorageState;
import com.gdu.common.error.GDUError;
import com.gdu.config.GduConfig;
import com.gdu.gimbal.GimbalState;
import com.gdu.gimbal.Rotation;
import com.gdu.gimbal.RotationMode;
import com.gdu.sdk.camera.CameraMode;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.codec.ImageProcessingManager;
import com.gdu.sdk.gimbal.GDUGimbal;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.logs.RonLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 云台和相机测试
 */

public class CameraGimbalActivity extends Activity implements TextureView.SurfaceTextureListener {

    private final String OUTPATH = Environment.getExternalStorageDirectory() + "/gdu/sdk/local/";//本地副本的保存路径
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

    private ImageProcessingManager mImageProcessingManager;
    private ImageView mYUVImageView;

    private TextView tv_support_mode;
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

        File file = new File(OUTPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
        } catch (Exception ignored) {
        }
        initCamera();
        initGimbal();
        mImageProcessingManager = new ImageProcessingManager(mContext);
    }

    private void initGimbal() {
        mGDUGimbal = (GDUGimbal) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getGimbal();
        if (mGDUGimbal == null) {
            toast("云台未识别，相关功能可能出现异常");
            return;
        }
        List<SettingsDefinitions.DisplayMode> list = new ArrayList<>();
        String supportMode = "";
        for (int i = 0; i < list.size(); i++) {
            SettingsDefinitions.DisplayMode mode = list.get(i);
            if (mode == SettingsDefinitions.DisplayMode.THERMAL_ONLY) {
                supportMode += "红外;";
            } else if (mode == SettingsDefinitions.DisplayMode.VISUAL_ONLY) {
                supportMode += "可见光;";
            } else if (mode == SettingsDefinitions.DisplayMode.WAL) {
                supportMode += "广角;";
            } else if (mode == SettingsDefinitions.DisplayMode.ZL) {
                supportMode += "变焦;";
            } else if (mode == SettingsDefinitions.DisplayMode.PIP) {
                supportMode += "分屏;";
            }
        }
        tv_support_mode.setText("支持光类型：" + supportMode);
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

        mYUVImageView = findViewById(R.id.yuv_imageview);
        mGimbalStateTextView = (TextView) findViewById(R.id.gimbal_info_textview);
        tv_support_mode = findViewById(R.id.tv_support_mode);
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
            case R.id.btn_get_focal_length:
                mGDUCamera.getOpticalZoomFocalLength(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                    @Override
                    public void onSuccess(Integer focalLength) {
                        toast("获取焦距发送成功 " + focalLength);
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        toast("获取焦距发送失败");
                    }
                });
                break;
            case R.id.btn_start_continuous_optical_zoom:
                mGDUCamera.startContinuousOpticalZoom(SettingsDefinitions.ZoomDirection.ZOOM_IN, SettingsDefinitions.ZoomSpeed.SLOWEST, new CommonCallbacks.CompletionCallback() {
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
            case R.id.btn_stop_continuous_optical_zoom:
                mGDUCamera.stopContinuousOpticalZoom(new CommonCallbacks.CompletionCallback() {
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
            case R.id.btn_set_display_mode:
                mGDUCamera.setDisplayMode(SettingsDefinitions.DisplayMode.VISUAL_ONLY, new CommonCallbacks.CompletionCallback() {
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
            case R.id.btn_get_display_mode:
                mGDUCamera.getDisplayMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.DisplayMode>() {
                    @Override
                    public void onSuccess(SettingsDefinitions.DisplayMode displayMode) {
                        toast("发送成功 " + displayMode);
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        toast("发送失败");
                    }
                });
                break;
            case R.id.btn_set_digital_zoom:
                mGDUCamera.setDigitalZoomFactor(1, new CommonCallbacks.CompletionCallback() {
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
            case R.id.btn_get_digital_zoom:
                mGDUCamera.getDigitalZoomFactor(new CommonCallbacks.CompletionCallbackWith<Float>() {
                    @Override
                    public void onSuccess(Float var1) {
                        toast("发送成功 " + var1);
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        toast("发送失败 ");
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
            case R.id.btn_record_video_to_local:
                if (codecManager != null) {
                    codecManager.startStoreMp4ToLocal(OUTPATH, "test.mp4");
                    toast("开始保存预览流副本到本地");
                }
                break;
            case R.id.btn_stop_record_video_to_local:
                if (codecManager != null) {
                    codecManager.stopStoreMp4ToLocal();
                    toast("停止保存预览流副本到本地");
                }
                break;
            case R.id.btn_enabled_yuv_data:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    codecManager.enabledYuvData(true);
                }
                break;

            case R.id.btn_get_yuv_data:
                byte[] yuvData =  codecManager.getYuvData();
                Bitmap bitmap = mImageProcessingManager.convertYUVtoRGB(yuvData, codecManager.getVideoWidth(), codecManager.getVideoHeight());
//                Bitmap bitmap = mFastYUVtoRGB.test(yuvData, 1920, 1080);
                if (bitmap != null) {
                    mYUVImageView.setImageBitmap(bitmap);
                }
                break;
            case R.id.btn_get_rgba_data:
                byte[] rgbData = codecManager.getRgbaData();
                Bitmap bitmap1 = ImageProcessingManager.rgb2Bitmap(rgbData, codecManager.getVideoWidth(), codecManager.getVideoHeight());
                if (bitmap1 != null) {
                    mYUVImageView.setImageBitmap(bitmap1);
                }
                break;
            case R.id.btn_store_picture_to_local:
                if (codecManager != null) {
                    codecManager.storageCurrentStreamToPicture(OUTPATH, "test.png", new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                toast("存储成功");
                            } else {
                                toast("存储失败  ");
                            }
                        }
                    });
                }
                break;
            case R.id.btn_get_capabilities:
                Capabilities capabilities = mGDUCamera.getCapabilities();
                SettingsDefinitions.ExposureCompensation[] exposureCompensations = capabilities.exposureCompensationRange();
                String evS = new String();
                if (exposureCompensations != null) {
                    for (SettingsDefinitions.ExposureCompensation exposureCompensation : exposureCompensations) {
                        evS += exposureCompensation;
                        evS += " ";
                    }
                    System.out.println("test ev " + evS);
                }
                SettingsDefinitions.ISO[] isos = capabilities.ISORange();
                if (isos != null) {
                    String isoS = new String();
                    for (SettingsDefinitions.ISO iso : isos) {
                        isoS += iso;
                        isoS += " ";
                    }
                    System.out.println("test iso " + isoS);
                }
                break;
            case R.id.btn_set_ev:
                mGDUCamera.setExposureCompensation(SettingsDefinitions.ExposureCompensation.N_1_0, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置成功");
                        } else {
                            toast("设置失败");
                        }
                    }
                });
                break;
            case R.id.btn_get_ev:
                mGDUCamera.getExposureCompensation(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ExposureCompensation>() {
                    @Override
                    public void onSuccess(SettingsDefinitions.ExposureCompensation exposureCompensation) {
                        toast("获取成功： " + exposureCompensation);
                    }

                    @Override
                    public void onFailure(GDUError gduError) {
                        toast("获取失败： ");
                    }
                });
                break;
            case R.id.btn_set_hd_liveview_enabled:
                mGDUCamera.setHDLiveViewEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置成功");
                        } else {
                            toast("设置失败");
                        }
                    }
                });
                break;
            case R.id.btn_get_hd_liveview_enabled:
                mGDUCamera.getHDLiveViewEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        toast("获取成功： " + aBoolean);
                    }

                    @Override
                    public void onFailure(GDUError gduError) {
                        toast("获取失败： ");
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
        if (codecManager != null) {
            codecManager.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (codecManager != null) {
            codecManager.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (codecManager != null) {
            codecManager.onDestroy();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

//        if (codecManager != null) {
//            codecManager.enabledYuvData(true);
//            byte[] data = codecManager.getRgbaData();
//            RonLog.LogD("test onSurfaceTextureUpdated " + (data != null ? data.length : 0));
//        }
    }
}
