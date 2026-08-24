package com.gdu.demo.mediatest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.Error;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.ActivityMediaVideoBinding;
import com.gdu.lib.util.core.XLogger;
//import com.gdu.media.VideoBackPlayState;
import com.gdu.sdk.camera.Camera;
import com.gdu.sdk.camera.MediaManager;
//import com.gdu.sdk.camera.VideoFeeder;
//import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.products.Aircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.FileDownCallback;

import java.text.DecimalFormat;
import com.gdu.sdk.codec.CodecManager;
import com.gdu.sdk.manager.SDKManager;
import com.gdu.sdk.products.Aircraft;

public class MediaVideoPlayActivity extends Activity implements TextureView.SurfaceTextureListener {
    private CodecManager mCodecManager;

//    ActivityMediaVideoBinding viewBinding;
//
//    private VideoFeeder.VideoDataListener videoDataListener = null;
//    private GDUCodecManager codecManager = null;
//
//    private Handler handler;
//
//    private String path = "";
//    private int type = 1;
//    private String duration;
//
//    int length;
//    VideoBackPlayState state;
//
//
//    int currentPosition;
//
//    DecimalFormat format = new DecimalFormat("#0.00");
//
//
//    MediaManager manager;
//
//    private String saveVideoPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_video);
        
         // 初始化CodecManager
        Aircraft aircraft = (Aircraft) SDKManager.getInstance().getProduct();
        if (aircraft != null) {
            mCodecManager = aircraft.getCodecManager();
        }

        // 实时画面
        TextureView textureView = findViewById(R.id.video_texture_view);
        textureView.setSurfaceTextureListener(this);

//        viewBinding = ActivityMediaVideoBinding.inflate(getLayoutInflater());
//        setContentView(viewBinding.getRoot());
//        handler = new Handler();
//        initView();
//
//        initData();
    }

//    private void initView() {
//        ImageView imageView = findViewById(R.id.iv_back);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        TextView textView = findViewById(R.id.tv_title);
//
//        handler = new Handler();
//        Intent intent = getIntent();
//        if (intent != null) {
//            path = intent.getStringExtra("path");
//            type = intent.getIntExtra("type", 1);
//            duration = intent.getStringExtra("duration");
//        }
//        Log.d("MediaDetail", "path = " + path + ", type = " + type);
//        viewBinding.tvPath.setText(path);
//        viewBinding.tvDuration.setText(duration + "s");
//
//        viewBinding.tvVideoPlay.setOnClickListener(listener);
//        viewBinding.tvVideoPlayPause.setOnClickListener(listener);
//        viewBinding.tvGetVideo.setOnClickListener(listener);
//        viewBinding.tvVideoPlayStop.setOnClickListener(listener);
//        viewBinding.tvVideoPlayResume.setOnClickListener(listener);
//
//
//        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
//
//                if (fromUser) {
//                    int progress = seekBar.getProgress();
//                    int seekPosition = (int) ((progress / 100.0) * length);
//                    viewBinding.tvPositionTime.setText("跳转到：" + seekPosition);
//                }
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//                int progress = seekBar.getProgress();
//
//                int seekPosition = (int) ((progress / 100.0) * length);
//
//                videoPlaySeek(seekPosition);
//                viewBinding.tvPositionTime.setText("跳转到：" + seekPosition );
//
//            }
//        });
//
//    }
//
//    private void initData() {
//
//        Camera camera = ((Camera) ((Aircraft) SdkDemoApplication.getProductInstance()).getCamera());
//
//        if (camera == null) {
//            return;
//        }
//        manager = camera.getMediaManager();
//        if (manager == null) {
//            return;
//        }
//
//
//        viewBinding.videoTextureView.setSurfaceTextureListener(this);
//        videoDataListener = new VideoFeeder.VideoDataListener() {
//            @Override
//            public void onReceive(byte[] bytes, int size) {
//                Log.d("VideoFeeder", "codecManager =" + (codecManager == null) + ",state = " + state);
//
//                if (state == VideoBackPlayState.VIDEO_PLAYING) {
//                    if (null != codecManager) {
//                        codecManager.sendDataToDecoder(bytes, size);
////                    printData("VideoFeeder", bytes, size);
//                    }
//                }
//            }
//        };
//
//        try {
//            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
//        } catch (Exception exception) {
//
//        }
//
//        manager.setVideoPlaybackListener(new FileDownCallback.VideoPlaybackStateListener() {
//            @Override
//            public void onUpdate(VideoBackPlayState playState, int progress, int videoLength, int current) {
//
//                length = videoLength;
//                state = playState;
//
//                Log.d("onUpdate", "onUpdate = " + state);
//                if (handler != null) {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            currentPosition = current;
//                            updateProgress(playState, progress, videoLength, current);
//                        }
//                    });
//                }
//            }
//        });
//
//    }
//
//    private void printData(String type, byte[] datas, int length) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            sb.append(Integer.toHexString(datas[i] & 0xff)).append(",");
//        }
//        Log.d("GduCESocket3 ", "type =" + type + "   " + sb);
//    }
//
//    private void updateProgress(VideoBackPlayState state, int progress, int length, int current) {
//        String stateStr = "";
//        if (state == VideoBackPlayState.VIDEO_PLAYING) {
//            stateStr = "播放中";
//        } else if (state == VideoBackPlayState.VIDEO_PAUSE) {
//            stateStr = "暂停";
//        } else if (state == VideoBackPlayState.VIDEO_STOP) {
//            stateStr = "停止";
//        }
//        viewBinding.tvState.setText(stateStr + ", progress = " + progress + ",length = " + (length) + ", current = " + (current ));
//        viewBinding.seekBar.setProgress(progress);
//    }
//
//
//    private View.OnClickListener listener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            switch (view.getId()) {
//                case R.id.tv_video_play:
//                    videoPlayStart();
//                    break;
//                case R.id.tv_video_play_pause:
//                    videoPlayPause();
//                    break;
//                case R.id.tv_video_play_resume:
//                    videoPlayResume();
//                    break;
//                case R.id.tv_get_video:
//                    getVideo();
//                    break;
//                case R.id.tv_video_play_stop:
//                    videoPlayStop();
//                    break;
//            }
//        }
//    };
//
//
//    private void videoPlayStart() {
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//        manager.playVideo(path, (byte) type, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(Error var1) {
//                if (var1 == null) {
//                    toastText("开始播放成功");
//                } else {
//                    toastText("开始播放失败");
//                }
//            }
//        });
//    }
//
//    private void getVideo() {
//
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//
//
//    }
//
//    private void videoPlayResume() {
//
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//
//        videoPlaySeek(currentPosition);
//
//        Log.d("MediaVideoPlay", "currentTime = " + currentPosition);
//
//    }
//
//    private void videoPlayPause() {
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//        manager.pauseVideo(path, (byte) type, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(Error var1) {
//
//            }
//        });
//
//    }
//
//    private void videoPlaySeek(int positionTime) {
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//        Log.d("MediaVideoPlay", "videoPlaySeek  currentTime = " + positionTime);
//
//        manager.seekVideo(path, (byte) type, positionTime, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(Error var1) {
//                if (var1 == null) {
//                    toastText("发送跳转成功");
//                } else {
//                    toastText("发送跳转失败");
//                }
//            }
//        });
//
//    }
//
//
//
//    private void videoPlayStop() {
//        if (manager == null) {
//            toastText("云台未连接");
//            return;
//        }
//        manager.stopVideo(path, (byte) type, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(Error var1) {
//
//                if (var1 == null) {
//                    toastText("停止播放成功");
//                } else {
//                    toastText("停止播放失败");
//                }
//
//            }
//        });
//    }
//
//
//    private void toastText(final String content){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MediaVideoPlayActivity.this, content, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (codecManager != null) {
//            codecManager.onResume();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (codecManager != null) {
//            codecManager.onPause();
//        }
////        mGduPlayView.setVisibility(View.GONE);
//        XLogger.INSTANCE.getAPP().i("test decoder onPause");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (codecManager != null) {
//            codecManager.onDestroy();
//        }
//
//        if (videoDataListener != null) {
//            VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(videoDataListener);
//        }
//
//
//        XLogger.INSTANCE.getAPP().i("test decoder onStop");
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//
//        if (manager != null) {
//            manager.stopVideo(path, (byte) type, new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onResult(Error var1) {
//
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//        if (codecManager == null) {
//            codecManager = new GDUCodecManager(this, viewBinding.videoTextureView, width, height);
//            Log.d("SurfaceTextureListener", "onSurfaceTextureAvailable with = " + width + ", height = " + height);
//        }
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//        Log.d("SurfaceTextureListener", "onSurfaceTextureDestroyed ");
//        return false;
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//    }

   @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager != null) {
            mCodecManager.startPreview(0, surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mCodecManager != null) {
            mCodecManager.onSurfaceLayoutChanged(0, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.stopPreview(0);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
