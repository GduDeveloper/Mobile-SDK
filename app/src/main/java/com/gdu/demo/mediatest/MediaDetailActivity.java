package com.gdu.demo.mediatest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.gdu.common.error.Error;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.ActivityMediaDetailBinding;
import com.gdu.lib.util.GlideUtils;
import com.gdu.lib.util.ThreadHelper;
import com.gdu.sdk.camera.Camera;
import com.gdu.sdk.camera.MediaManager;
import com.gdu.sdk.products.Aircraft;
import com.gdu.sdk.util.FileDownCallback;

import java.io.File;
import java.text.DecimalFormat;

public class MediaDetailActivity extends Activity {

    private ActivityMediaDetailBinding viewBinding;
    private Handler handler;
    private String path = "";

    DecimalFormat format = new DecimalFormat("#0.00");

    MediaManager manager;

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

        Log.d("MediaDetail", "path = " + path);

        viewBinding.tvGetThumb.setOnClickListener(listener);
        viewBinding.tvGetPreview.setOnClickListener(listener);
        viewBinding.tvGetRaw.setOnClickListener(listener);
        viewBinding.tvGetVideo.setOnClickListener(listener);
    }

    private void initData() {
        Camera camera = ((Camera) ((Aircraft) SdkDemoApplication.getProductInstance()).getCamera());
        if (camera != null) {
            manager = camera.getMediaManager();
        }
        viewBinding.tvPath.setText(path);
        getImagePreview();
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

                default:
                    break;
            }

        }
    };


    /**
     * 原图
     */
    private void getImageRaw() {
        if (manager == null) {
            toastText("飞行器未连接");
            return;
        }
        manager.getRawImage(path, "", new FileDownCallback.OnMediaFileCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long position, boolean isLastPack) {

            }

            @Override
            public void onProgress(long total, long current) {

                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String progress = format.format((current / (total * 1.0)) * 100);
                            viewBinding.tvRawProgress.setText(progress + "%");
                        }
                    });
                }
            }

            @Override
            public void onSuccess(Bitmap result, String path) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ImagePath ", "path = " + path);
                            String localPath = "file://" + path;
                            Glide.with(MediaDetailActivity.this).load(localPath).into(viewBinding.ivRaw);
                        }
                    });
                }
            }


            @Override
            public void onFail(Error error) {

            }

        });
    }


    /**
     * 预览图
     */
    private void getImagePreview() {
        if (manager == null) {
            toastText("飞行器未连接");
            return;
        }
        manager.getPreview(path, "", new FileDownCallback.OnMediaFileCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

            }

            @Override
            public void onSuccess(Bitmap bitmap, String path) {
                ThreadHelper.runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(path)) {
                        GlideUtils.loadImage(MediaDetailActivity.this, new File(path), viewBinding.ivPreview);
                    } else {
                        viewBinding.ivPreview.setImageResource(R.drawable.default_photo_video);
                    }
                });
            }


            @Override
            public void onProgress(long l, long l1) {

            }


            @Override
            public void onFail(Error error) {

            }

        });
    }


    /**
     * 缩略图
     */
    private void getImageThumb() {
        if (manager == null) {
            toastText("飞行器未连接");
            return;
        }
        manager.getThumbnail(path, "",new FileDownCallback.OnMediaFileCallBack() {


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
            public void onSuccess(Bitmap bitmap, String path) {
                ThreadHelper.runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(path)) {
                        GlideUtils.loadImage(MediaDetailActivity.this, new File(path), viewBinding.ivThumb);
                    } else {
                        viewBinding.ivThumb.setImageResource(R.drawable.default_photo_video);
                    }
                });
            }


            @Override
            public void onFail(Error error) {

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
