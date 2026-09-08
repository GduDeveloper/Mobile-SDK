package com.gdu.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.gdu.camera.Capabilities;
import com.gdu.camera.SettingsDefinitions;
import com.gdu.camera.StorageState;
import com.gdu.common.error.GDUError;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.views.IRTempOverlayView;
import com.gdu.drone.TMSGimbalState;
import com.gdu.gimbal.GimbalState;
import com.gdu.gimbal.Rotation;
import com.gdu.gimbal.RotationMode;
import com.gdu.sdk.camera.CameraMode;
import com.gdu.sdk.camera.CameraStreamSettings;
import com.gdu.sdk.camera.CameraThermalPalette;
import com.gdu.sdk.camera.CameraVideoStreamSource;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.camera.ThermalTemperatureMeasureMode;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.codec.ImageProcessingManager;
import com.gdu.sdk.gimbal.GDUGimbal;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.FileSaveUtil;
import com.gdu.util.RectUtil;
import com.gdu.util.ThreadHelper;
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

    private FrameLayout mVideoContainerLayout;
    private TextureView mGduPlayView;
    private TextView mInfoTextView;
    private TextView mStorageInfoTextView;
    private TextView mVersionTextView;


    private TextView mGimbalStateTextView;
    private Context mContext;
    private GDUCamera mGDUCamera;

    private GDUGimbal mGDUGimbal;

    private Handler mHandler;

    /**
     * 红外测温叠加层（实时显示测温点和测温框）
     */
    private IRTempOverlayView mIRTempOverlayView;

    /**
     * 测温叠加层刷新周期（毫秒）
     */
    private static final long IR_TEMP_REFRESH_INTERVAL = 500L;

    /**
     * 用户最近一次设置的测温点（协议坐标），-1 表示未设置
     */
    private int mLastSpotX = -1;

    private int mLastSpotY = -1;

    /**
     * 用户最近一次设置的测温区域（协议坐标），-1 表示未设置
     */
    private int mLastAreaCenterX = -1;

    private int mLastAreaCenterY = -1;

    private int mLastAreaWidth = 0;

    private int mLastAreaHeight = 0;

    /**
     * 测温叠加层刷新任务
     */
    private final Runnable mIRTempRefreshTask = new Runnable() {
        @Override
        public void run() {
            updateIRTempOverlay();
            if (mHandler != null) {
                mHandler.postDelayed(this, IR_TEMP_REFRESH_INTERVAL);
            }
        }
    };

    private ImageProcessingManager mImageProcessingManager;
    private ImageView mYUVImageView;

    private TextView tv_support_mode;
    private TextView tvPreviewFormat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_camera_gimbal);
        initView();
        initData();
        initListener();
        initHandler();
        mHandler.postDelayed(mIRTempRefreshTask, IR_TEMP_REFRESH_INTERVAL);
//        timeShow();
    }

    private void initHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

            }
        };
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
        List<SettingsDefinitions.DisplayMode> list = mGDUGimbal.getSupportDisplayMode();
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
        mVideoContainerLayout = findViewById(R.id.video_container_layout);
        mGduPlayView = findViewById(R.id.video_texture_view);
        mGduPlayView.setOpaque(false);

        // 红外测温叠加层：覆盖在视频画面之上，实时显示测温点和测温框，支持触摸选取
        mIRTempOverlayView = new IRTempOverlayView(mContext);
        mVideoContainerLayout.addView(mIRTempOverlayView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mIRTempOverlayView.setOnTempSelectListener(new IRTempOverlayView.OnTempSelectListener() {
            @Override
            public void onPointSelected(final int protoX, final int protoY) {
                // 点击画面选取测温点：屏幕坐标已由 RectUtil.screenPoint2VideoArg 转为协议坐标
                runOnUiThread(() -> {
                    if (mGDUCamera == null) {
                        toast("相机未连接");
                        return;
                    }
                    mGDUCamera.setThermalSpotMeteringTargetPoint((short) protoX, (short) protoY, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                mLastSpotX = protoX;
                                mLastSpotY = protoY;
                                toast("设置测温点成功：" + protoX + "," + protoY);
                            } else {
                                toast("设置测温点失败：" + error.getDescription());
                            }
                        }
                    });
                });
            }

            @Override
            public void onAreaSelected(final int centerX, final int centerY, final int width, final int height) {
                // 拖动画面选取测温区域：屏幕坐标已由 RectUtil.screenPoint2VideoArg 转为协议坐标
                runOnUiThread(() -> {
                    if (mGDUCamera == null) {
                        toast("相机未连接");
                        return;
                    }
                    mGDUCamera.setThermalMeteringArea((short) width, (short) height, (short) centerX, (short) centerY, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                mLastAreaCenterX = centerX;
                                mLastAreaCenterY = centerY;
                                mLastAreaWidth = width;
                                mLastAreaHeight = height;
                                toast("设置测温区域成功：" + width + "," + height + "," + centerX + "," + centerY);
                            } else {
                                toast("设置测温区域失败：" + error.getDescription());
                            }
                        }
                    });
                });
            }
        });

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
        tvPreviewFormat = findViewById(R.id.preview_format);
        tvPreviewFormat.setText(GlobalVariable.sCodingFormat == 0 ? "H264" : "H265");
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
                mGDUCamera.setZoom(10, new CommonCallbacks.CompletionCallback() {
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
               float zoom =  mGDUCamera.getCurrentZoom();
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

                String path = OUTPATH + "test.text";

                if (codecManager != null) {
                    codecManager.enabledYuvData(true);
                    codecManager.setYuvDataCallback(new GDUCodecManager.YuvDataCallback() {
                        @Override
                        public void onYuvDataReceived(byte[] bytes, int i, int i1, int i2) {
                            FileSaveUtil.getSingle().saveData(bytes, path);
                        }
                    });
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
            case R.id.preview_format_264:
                mGDUCamera.setVideoCodingFormat(0, new CommonCallbacks.CompletionCallbackWith<Integer>() {
                    @Override
                    public void onSuccess(Integer format) {
                        toast("成功设置为H264");
                        ThreadHelper.runOnUiThread(() -> tvPreviewFormat.setText("H264"));
                    }

                    @Override
                    public void onFailure(GDUError gduError) {
                        toast("设置失败： ");
                    }
                });
                break;
            case R.id.preview_format_265:
                mGDUCamera.setVideoCodingFormat(1, new CommonCallbacks.CompletionCallbackWith<Integer>() {
                    @Override
                    public void onSuccess(Integer format) {
                        toast("成功设置为H265");
                        ThreadHelper.runOnUiThread(() -> tvPreviewFormat.setText("H265"));
                    }

                    @Override
                    public void onFailure(GDUError gduError) {
                        toast("设置失败： ");
                    }
                });
                break;
            case  R.id.btn_set_capture_storage:
                    showStorageConfigDialog(true);
                    break;
            case R.id.btn_set_record_storage:
                    showStorageConfigDialog(false);
                break;
            case R.id.btn_set_thermal_palette:
                showThermalPaletteDialog();
                break;
            case R.id.btn_get_thermal_palette:
                mGDUCamera.getThermalPalette(new CommonCallbacks.CompletionCallbackWith<CameraThermalPalette>() {
                    @Override
                    public void onSuccess(CameraThermalPalette palette) {
                        toast("获取伪彩成功：" + palette.description());
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        toast("获取伪彩失败：" + var1.getDescription());
                    }
                });
                break;
            case R.id.btn_set_thermal_measure_mode:
                showThermalMeasureModeDialog();
                break;
            case R.id.btn_set_thermal_spot_point:
                showThermalSpotPointDialog();
                break;
            case R.id.btn_set_thermal_area:
                showThermalAreaDialog();
                break;
            case R.id.btn_enable_thermal_temp:
                mGDUCamera.setThermalTemperatureDataEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("开启测温成功");
                        } else {
                            toast("开启测温失败：" + error.getDescription());
                        }
                    }
                });
                break;
            case R.id.btn_disable_thermal_temp:
                mGDUCamera.setThermalTemperatureDataEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("关闭测温成功");
                        } else {
                            toast("关闭测温失败：" + error.getDescription());
                        }
                    }
                });
                break;
            case R.id.btn_get_thermal_temp_data:
                mGDUCamera.getThermalTemperatureData(new CommonCallbacks.CompletionCallbackWith<TMSGimbalState>() {
                    @Override
                    public void onSuccess(TMSGimbalState state) {
                        toast("最高温：" + state.getHighestTemp()
                                + " 最低温：" + state.getLowestTemp()
                                + " 中心温：" + state.getCenterTemp()
                                + " 光标温：" + state.getCursorPointTemp()
                                + " 区域平均温：" + state.getAreaAvgTemp()
                        );
                    }

                    @Override
                    public void onFailure(GDUError var1) {
                        toast("获取测温数据失败：" + var1.getDescription());
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
        if (mHandler != null) {
            mHandler.removeCallbacks(mIRTempRefreshTask);
        }
        if (codecManager != null) {
            codecManager.onDestroy();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new GDUCodecManager(mContext, mGduPlayView, width, height);
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

    private void showStorageConfigDialog(final boolean isCapture) {
        if (mGDUCamera == null) {
            toast("相机未连接");
            return;
        }

        // 根据是拍照还是录像，异步获取当前设置并在回调中显示对话框
        CommonCallbacks.CompletionCallbackWith<CameraStreamSettings> callback = new CommonCallbacks.CompletionCallbackWith<CameraStreamSettings>() {
            @Override
            public void onSuccess(CameraStreamSettings currentSettings) {
                runOnUiThread(() -> {
                    // 构造初始选中项
                    final String[] items = new String[]{"广角 (WIDE)", "变焦 (ZOOM)", "红外 (INFRARED_THERMAL)", "保存当前图传画面"};
                    final boolean[] checked = new boolean[]{false, false, false, false};

                    if (currentSettings != null) {
                        List<CameraVideoStreamSource> sources = currentSettings.getCameraVideoStreamSources();
                        if (sources != null) {
                            for (CameraVideoStreamSource src : sources) {
                                if (src == CameraVideoStreamSource.WIDE) checked[0] = true;
                                if (src == CameraVideoStreamSource.ZOOM) checked[1] = true;
                                if (src == CameraVideoStreamSource.INFRARED_THERMAL) checked[2] = true;
                            }
                        }
                        checked[3] = currentSettings.needCurrentLiveViewStream();
                    } else {
                        // 如果未能获取到当前设置，使用默认值（保持兼容旧行为）
                        checked[0] = true;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(isCapture ? "设置拍照存储配置" : "设置录像存储配置");
                    builder.setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
                    builder.setPositiveButton("确定", (dialog, which) -> {
                        List<CameraVideoStreamSource> sources = new ArrayList<>();
                        if (checked[0]) sources.add(CameraVideoStreamSource.WIDE);
                        if (checked[1]) sources.add(CameraVideoStreamSource.ZOOM);
                        if (checked[2]) sources.add(CameraVideoStreamSource.INFRARED_THERMAL);

                        CameraStreamSettings.Builder b = new CameraStreamSettings.Builder();
                        b.setCameraVideoStreamSources(sources);
                        b.setNeedCurrentLiveViewStream(checked[3]);
                        CameraStreamSettings settings = b.build();

                        if (isCapture) {
                            mGDUCamera.setCaptureCameraStreamSettings(settings, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(GDUError error) {
                                    if (error == null) {
                                        toast("设置拍照存储配置成功");
                                    } else {
                                        toast("设置拍照存储配置失败: " + error.getDescription());
                                    }
                                }
                            });
                        } else {
                            mGDUCamera.setRecordCameraStreamSettings(settings, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(GDUError error) {
                                    if (error == null) {
                                        toast("设置录像存储配置成功");
                                    } else {
                                        toast("设置录像存储配置失败: " + error.getDescription());
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                });
            }

            @Override
            public void onFailure(GDUError error) {
                // 获取失败时仍然展示对话框，使用默认选项
                runOnUiThread(() -> {
                    final String[] items = new String[]{"广角 (WIDE)", "变焦 (ZOOM)", "红外 (INFRARED_THERMAL)", "保存当前图传画面"};
                    final boolean[] checked = new boolean[]{true, false, false, false};

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(isCapture ? "设置拍照存储配置" : "设置录像存储配置");
                    builder.setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
                    builder.setPositiveButton("确定", (dialog, which) -> {
                        List<CameraVideoStreamSource> sources = new ArrayList<>();
                        if (checked[0]) sources.add(CameraVideoStreamSource.WIDE);
                        if (checked[1]) sources.add(CameraVideoStreamSource.ZOOM);
                        if (checked[2]) sources.add(CameraVideoStreamSource.INFRARED_THERMAL);

                        CameraStreamSettings.Builder b = new CameraStreamSettings.Builder();
                        b.setCameraVideoStreamSources(sources);
                        b.setNeedCurrentLiveViewStream(checked[3]);
                        CameraStreamSettings settings = b.build();

                        if (isCapture) {
                            mGDUCamera.setCaptureCameraStreamSettings(settings, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(GDUError error) {
                                    if (error == null) {
                                        toast("设置拍照存储配置成功");
                                    } else {
                                        toast("设置拍照存储配置失败: " + error.getDescription());
                                    }
                                }
                            });
                        } else {
                            mGDUCamera.setRecordCameraStreamSettings(settings, new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(GDUError error) {
                                    if (error == null) {
                                        toast("设置录像存储配置成功");
                                    } else {
                                        toast("设置录像存储配置失败: " + error.getDescription());
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                });
            }
        };

        if (isCapture) {
            mGDUCamera.getCaptureCameraStreamSettings(callback);
        } else {
            mGDUCamera.getRecordCameraStreamSettings(callback);
        }
    }

    private void showThermalPaletteDialog() {
        if (mGDUCamera == null) {
            toast("相机未连接");
            return;
        }
        final List<CameraThermalPalette> palettes = new ArrayList<>();
        for (CameraThermalPalette palette : CameraThermalPalette.values()) {
            if (palette != CameraThermalPalette.UNKNOWN) {
                palettes.add(palette);
            }
        }
        String[] items = new String[palettes.size()];
        for (int i = 0; i < palettes.size(); i++) {
            items[i] = palettes.get(i).value() + " " + palettes.get(i).description();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("选择红外伪彩");
        builder.setItems(items, (dialog, which) -> {
            final CameraThermalPalette palette = palettes.get(which);
            mGDUCamera.setThermalPalette(palette, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        toast("设置伪彩成功：" + palette.description());
                    } else {
                        toast("设置伪彩失败：" + error.getDescription());
                    }
                }
            });
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 选择红外测温模式
     */
    private void showThermalMeasureModeDialog() {
        if (mGDUCamera == null) {
            toast("相机未连接");
            return;
        }
        final List<ThermalTemperatureMeasureMode> modes = new ArrayList<>();
        for (ThermalTemperatureMeasureMode mode : ThermalTemperatureMeasureMode.values()) {
            if (mode != ThermalTemperatureMeasureMode.UNKNOWN) {
                modes.add(mode);
            }
        }
        String[] items = new String[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            items[i] = modes.get(i).value() + " " + modes.get(i).description();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("选择红外测温模式");
        builder.setItems(items, (dialog, which) -> {
            final ThermalTemperatureMeasureMode mode = modes.get(which);
            mGDUCamera.setThermalMeasurementMode(mode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    if (error == null) {
                        toast("设置测温模式成功：" + mode.description());
                    } else {
                        toast("设置测温模式失败：" + error.getDescription());
                    }
                }
            });
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 设置红外测温光标点位置（点测温）
     * 输入屏幕坐标，经 RectUtil.screenPoint2VideoArg 转换为协议坐标后发送
     */
    private void showThermalSpotPointDialog() {
        if (mGDUCamera == null) {
            toast("相机未连接");
            return;
        }
        final EditText xEt = new EditText(mContext);
        xEt.setHint("屏幕X坐标");
        xEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText yEt = new EditText(mContext);
        yEt.setHint("屏幕Y坐标");
        yEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(xEt);
        layout.addView(yEt);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置测温点（输入屏幕坐标）");
        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                int screenX = Integer.parseInt(xEt.getText().toString().trim());
                int screenY = Integer.parseInt(yEt.getText().toString().trim());
                // 屏幕坐标 → 协议坐标（与 ZorroRealControlActivity 选取逻辑一致，经 RectUtil 转换）
                List<Short> proto = RectUtil.screenPoint2VideoArg(screenX, screenX, screenY, screenY);
                if (proto == null || proto.size() < 2) {
                    toast("坐标转换失败");
                    return;
                }
                short x = proto.get(0);
                short y = proto.get(1);
                mGDUCamera.setThermalSpotMeteringTargetPoint(x, y, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            mLastSpotX = x;
                            mLastSpotY = y;
                            toast("设置测温点成功：" + x + "," + y);
                        } else {
                            toast("设置测温点失败：" + error.getDescription());
                        }
                    }
                });
            } catch (NumberFormatException e) {
                toast("请输入数字");
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 设置红外测温矩形区域（区域测温）
     * 输入屏幕坐标（左上角 + 右下角），经 RectUtil.screenPoint2VideoArg 转换为协议坐标后发送
     */
    private void showThermalAreaDialog() {
        if (mGDUCamera == null) {
            toast("相机未连接");
            return;
        }
        final EditText leftXEt = new EditText(mContext);
        leftXEt.setHint("左上角X（屏幕坐标）");
        leftXEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText leftYEt = new EditText(mContext);
        leftYEt.setHint("左上角Y（屏幕坐标）");
        leftYEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText rightXEt = new EditText(mContext);
        rightXEt.setHint("右下角X（屏幕坐标）");
        rightXEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText rightYEt = new EditText(mContext);
        rightYEt.setHint("右下角Y（屏幕坐标）");
        rightYEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(leftXEt);
        layout.addView(leftYEt);
        layout.addView(rightXEt);
        layout.addView(rightYEt);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("设置测温区域（输入屏幕坐标）");
        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                int leftX = Integer.parseInt(leftXEt.getText().toString().trim());
                int leftY = Integer.parseInt(leftYEt.getText().toString().trim());
                int rightX = Integer.parseInt(rightXEt.getText().toString().trim());
                int rightY = Integer.parseInt(rightYEt.getText().toString().trim());
                // 屏幕坐标 → 协议坐标（与 ZorroRealControlActivity irSetTempArea 逻辑一致，经 RectUtil 转换）
                List<Short> proto = RectUtil.screenPoint2VideoArg(leftX, rightX, leftY, rightY);
                if (proto == null || proto.size() < 4) {
                    toast("坐标转换失败");
                    return;
                }
                short width = proto.get(2);
                short height = proto.get(3);
                short centerX = (short) (proto.get(0) + width / 2);
                short centerY = (short) (proto.get(1) + height / 2);
                mGDUCamera.setThermalMeteringArea(width, height, centerX, centerY, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            mLastAreaCenterX = centerX;
                            mLastAreaCenterY = centerY;
                            mLastAreaWidth = width;
                            mLastAreaHeight = height;
                            toast("设置测温区域成功：" + width + "," + height + "," + centerX + "," + centerY);
                        } else {
                            toast("设置测温区域失败：" + error.getDescription());
                        }
                    }
                });
            } catch (NumberFormatException e) {
                toast("请输入数字");
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 更新红外测温叠加层：从 GlobalVariable.sInfraredCameraStatus 读取
     * 实时温度点/区域数据，叠加显示在视频画面上。
     * <p>
     * 测温点取"光标点"（用户设置的测温点），测温框取"区域平均温坐标"（区域测温时由飞控上报）；
     * 用户手动设置的测温区域参数作为框的兜底。
     */
    private void updateIRTempOverlay() {
        if (mIRTempOverlayView == null) {
            return;
        }
        TMSGimbalState state = GlobalVariable.sInfraredCameraStatus;
        if (state == null) {
            return;
        }
        boolean hasSpot = mLastSpotX >= 0 && mLastSpotY >= 0;
        boolean hasArea = mLastAreaCenterX >= 0 && mLastAreaCenterY >= 0 && mLastAreaWidth > 0 && mLastAreaHeight > 0;

        if (hasSpot) {
            // 光标点：优先用飞控上报的光标坐标，否则用用户设置的坐标
            int spotX = state.getCursorPoint_X() > 0 ? state.getCursorPoint_X() : mLastSpotX;
            int spotY = state.getCursorPoint_Y() > 0 ? state.getCursorPoint_Y() : mLastSpotY;
            mIRTempOverlayView.setSpotPoint(spotX, spotY, state.getCursorPointTemp());
        } else {
            mIRTempOverlayView.setSpotPoint(-1, -1, 0f);
        }

        if (hasArea) {
            // 区域：优先用飞控上报的区域平均温坐标作为中心，否则用用户设置的区域参数
            int areaCenterX = state.getAreaAvg_X() > 0 ? state.getAreaAvg_X() : mLastAreaCenterX;
            int areaCenterY = state.getAreaAvg_Y() > 0 ? state.getAreaAvg_Y() : mLastAreaCenterY;
            mIRTempOverlayView.setThermalArea(areaCenterX, areaCenterY, mLastAreaWidth, mLastAreaHeight, state.getAreaAvgTemp());
        } else {
            mIRTempOverlayView.setThermalArea(-1, -1, 0, 0, 0f);
        }

        // 最高温/最低温点
        if (state.getHighestTempPoint_X() > 0 && state.getHighestTempPoint_Y() > 0) {
            mIRTempOverlayView.setHighestTempPoint(state.getHighestTempPoint_X(), state.getHighestTempPoint_Y(), state.getHighestTemp());
        } else {
            mIRTempOverlayView.setHighestTempPoint(-1, -1, 0f);
        }
        if (state.getLowestTempPoint_X() > 0 && state.getLowestTempPoint_Y() > 0) {
            mIRTempOverlayView.setLowestTempPoint(state.getLowestTempPoint_X(), state.getLowestTempPoint_Y(), state.getLowestTemp());
        } else {
            mIRTempOverlayView.setLowestTempPoint(-1, -1, 0f);
        }

        mIRTempOverlayView.postInvalidate();
    }
}
