package com.gdu.demo;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdu.demo.databinding.ActivityFlightBinding;
import com.gdu.demo.setting.SettingDialogFragment;
import com.gdu.demo.widget.TopStateView;
import com.gdu.sdk.camera.VideoFeeder;
import com.gdu.sdk.codec.GDUCodecManager;

public class FlightActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private ActivityFlightBinding viewBinding;
    private GDUCodecManager codecManager;
    private VideoFeeder.VideoDataListener videoDataListener ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityFlightBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        initData();
    }


    private void initView() {
        viewBinding.topStateView.setViewClickListener(new TopStateView.OnClickCallBack() {
            @Override
            public void onLeftIconClick() {
                finish();
            }

            @Override
            public void onRightSettingIconCLick() {
                showSettingFragment();
            }
        });

        viewBinding.textureView.setSurfaceTextureListener(this);
        videoDataListener = new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] bytes, int size) {
                if (null != codecManager) {
                    codecManager.sendDataToDecoder(bytes, size);
                }
            }
        };

    }

    private void showSettingFragment() {
        SettingDialogFragment.Companion.show(getSupportFragmentManager());
    }

    private void initData() {

//        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
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
            codecManager = new GDUCodecManager(FlightActivity.this, surface, width, height);
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
