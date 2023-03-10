package com.gdu.demo.mediatest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.ActivityMediaVideoBinding;
import com.gdu.gdusocket.GduCommunication3;
import com.gdu.gdusocket.GduSocketManager;
import com.gdu.gdusocket.SocketCallBack3;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.GDUMediaManager;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.socketmodel.GduFrame3;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.logs.RonLog;

public class MediaVideoPlayActivity extends Activity implements TextureView.SurfaceTextureListener {


    private ActivityMediaVideoBinding viewBinding;
    private VideoFeeder.VideoDataListener videoDataListener = null;
    private GDUCodecManager codecManager = null;

    private Handler handler;

    private String path;

    int length = 100;

    int current;


    GDUMediaManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMediaVideoBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        handler = new Handler();
        initView();

        initData();
    }

    private void initView() {
        handler = new Handler();
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
        }
        Log.d("MediaDetail", "path = " + path);
        viewBinding.tvPath.setText(path);

        viewBinding.tvVideoPlay.setOnClickListener(listener);
        viewBinding.tvVideoPlayPause.setOnClickListener(listener);
        viewBinding.tvGetVideo.setOnClickListener(listener);
        viewBinding.tvVideoPlayStop.setOnClickListener(listener);
        viewBinding.tvVideoPlayResume.setOnClickListener(listener);

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int progress = seekBar.getProgress();

                int seekPosition = (int) ((progress / 100.0) * length);
                videoPlaySeek(seekPosition);
                viewBinding.tvPositionTime.setText("跳转到：" + seekPosition/1000);

            }
        });

    }

    private void initData() {

        GDUCamera camera = ((GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera());
        if (camera != null) {
            manager = camera.getMediaManager();
        }

        viewBinding.videoTextureView.setSurfaceTextureListener(this);
        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] bytes, int size) {
                if (null != codecManager) {
                    codecManager.sendDataToDecoder(bytes, size);
                    Log.d("VideoFeeder", "length = " + size);
                }
            }
        };

        try {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
        } catch (Exception exception) {

        }

    }

    private void updateProgress(int state, int progress, int length, int current) {
        viewBinding.tvState.setText("状态:" + state + ", progress = " + progress + ",length = " + (length / 1000) + ", current = " +( current / 1000));
        viewBinding.seekBar.setProgress(progress);
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_video_play:
                    videoPlayStart();
                    break;
                case R.id.tv_video_play_pause:
                    videoPlayPause();
                    break;
                case R.id.tv_video_play_resume:
                    videoPlayResume();
                    break;
                case R.id.tv_get_video:

                    break;
                case R.id.tv_video_play_stop:
                    videoPlayStop();
                    break;
            }
        }
    };

    private void videoPlayResume() {

        if (manager == null) {
            toastText("云台未连接");
            return;
        }

        videoPlaySeek(current);

        Log.d("MediaVideoPlay", "currentTime = " + current);

    }


    private void videoPlayPause() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.pauseVideo(path, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {

            }
        });

    }
    private void videoPlaySeek(int positionTime) {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        Log.d("MediaVideoPlay", "videoPlaySeek  currentTime = " + current);

        manager.seekVideo(path, positionTime, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {

            }
        });

    }

    private void videoPlayStart() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.playVideo(path, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {

            }
        });
    }

    private void videoPlayStop() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.stopVideo(path, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {

            }
        });
    }


    private void toastText(final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MediaVideoPlayActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
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
        RonLog.LogD("test decoder onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (codecManager != null) {
            codecManager.onDestroy();
        }
        RonLog.LogD("test decoder onStop");
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (codecManager == null) {
            codecManager = new GDUCodecManager(this, viewBinding.videoTextureView, width, height);
            codecManager.setYuvDataCallback(new GDUCodecManager.YuvDataCallback() {
                @Override
                public void onYuvDataReceived(byte[] byteBuffer, int len, int width, int height) {
                }
            });
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
