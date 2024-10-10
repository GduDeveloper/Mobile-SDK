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

import com.bumptech.glide.Glide;
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
    private String raw = "";
    private String thum= "";
    private String preview = "";


    DecimalFormat format = new DecimalFormat("#0.00");

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
            raw = intent.getStringExtra("raw");
            thum = intent.getStringExtra("thum");
            preview = intent.getStringExtra("preview");
        }

        Log.d("MediaDetail", "path = " + path);

        viewBinding.tvGetThumb.setOnClickListener(listener);
        viewBinding.tvGetPreview.setOnClickListener(listener);
        viewBinding.tvGetRaw.setOnClickListener(listener);
        viewBinding.tvGetVideo.setOnClickListener(listener);
//
//        viewBinding.tvVideoPlay.setOnClickListener(listener);
//        viewBinding.tvVideoPlayPause.setOnClickListener(listener);
//        viewBinding.tvVideoPlaySeek.setOnClickListener(listener);
//        viewBinding.tvVideoPlayStop.setOnClickListener(listener);
    }

    private void initData() {
        GDUCamera camera = ((GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera());
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
        manager.getRawImage(raw, "ddd", new FileDownCallback.OnMediaFileCallBack() {
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
            public void onSuccess(String result) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("ImagePath ", "path = " + result);
                            Glide.with(MediaDetailActivity.this).load(result).into(viewBinding.ivRaw);
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
     * 预览图
     */
    private void getImagePreview() {
        if (manager == null) {
            toastText("飞行器未连接");
            return;
        }
        manager.getPreview(preview, "", new FileDownCallback.OnMediaImageCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onGetMediaImage(Bitmap bitmap, byte[] bytes) {

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
            public void onProgress(long l, long l1) {

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
            toastText("飞行器未连接");
            return;
        }
        manager.getThumbnail(thum, "",new FileDownCallback.OnMediaImageCallBack() {


            @Override
            public void onStart() {

            }

            @Override
            public void onGetMediaImage(Bitmap bitmap, byte[] bytes) {
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
            public void onProgress(long total, long current) {

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
