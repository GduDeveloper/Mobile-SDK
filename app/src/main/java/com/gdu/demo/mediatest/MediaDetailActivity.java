package com.gdu.demo.mediatest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.ActivityMediaDetailBinding;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.GDUMediaManager;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.FileDownCallback;

import java.text.DecimalFormat;

public class MediaDetailActivity extends Activity {

    private ActivityMediaDetailBinding viewBinding;
    private Handler handler;
    private String path = "";

    DecimalFormat format = new DecimalFormat("#.00");
    GDUMediaManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMediaDetailBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        handler = new Handler();
        initView();

        initData();
    }


    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
        }
        Log.d("MediaDetailActivity", "path = " + path);

        viewBinding.tvGetThumb.setOnClickListener(listener);
        viewBinding.tvGetPreview.setOnClickListener(listener);
        viewBinding.tvGetRaw.setOnClickListener(listener);
        viewBinding.tvGetVideo.setOnClickListener(listener);
    }

    private void initData() {
        GDUCamera camera = ((GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera());
        if (camera != null) {
            manager = camera.getMediaManager();
        }
        viewBinding.tvPath.setText(path);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.tv_get_thumb:
                    getImageThumb();
                    break;
                case R.id.tv_get_preview:
                    getImagePreview();
                    break;

                case R.id.tv_get_raw:
                    getImageRaw();
                    break;
            }
        }
    };


    /**
     * 原图
     */
    private void getImageRaw() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.getRawImage(path,"", new FileDownCallback.OnFileDownCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

            }

            @Override
            public void onProgress(long l, long l1) {

            }

            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onFail(GDUError gduError) {

            }
        });
    }


    /**
     * 预览图
     */
    private void getImagePreview() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.getPreview(path, new FileDownCallback.OnFileDownCallBack<Bitmap>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

            }

            @Override
            public void onProgress(long total, long current) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewBinding.tvPreviewProgress.setText((current / total * 1.0) * 100 + "");
                        }
                    });
                }
            }

            @Override
            public void onSuccess(Bitmap bitmap) {

                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewBinding.ivPreview.setImageBitmap(bitmap);
                        }
                    });
                }

            }

            @Override
            public void onFail(GDUError error) {

            }
        });
    }

    /**
     * 缩略图
     */
    private void getImageThumb() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.getThumbnail(path, new FileDownCallback.OnFileDownCallBack<Bitmap>() {

            @Override
            public void onStart() {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

            }

            @Override
            public void onProgress(long total, long current) {

            }

            @Override
            public void onSuccess(Bitmap bitmap) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewBinding.ivThumb.setImageBitmap(bitmap);
                        }
                    });
                }

            }

            @Override
            public void onFail(GDUError error) {

            }
        });
    }

    private void toastText(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MediaDetailActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

