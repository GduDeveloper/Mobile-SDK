package com.gdu.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdu.demo.databinding.ActivityPsdkCustomViewBinding;
import com.gdu.sdk.psdk.GDUPsdkCustomView;

import java.util.HashMap;

public class PsdkCustomViewActivity extends AppCompatActivity {

    ActivityPsdkCustomViewBinding viewBinding;

    Handler handler;

    GDUPsdkCustomView customView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPsdkCustomViewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        initView();
        initData();
    }

    private void initData() {
    }

    private void initView() {
        customView = new GDUPsdkCustomView();

        customView.setOnPSDKCustomMsgChangeListener(new GDUPsdkCustomView.OnPSDKCustomMsgChangeListener() {
            @Override
            public void onMsgChange(String msg) {

            }
        });
        customView.getCustomView(new GDUPsdkCustomView.OnPsdkCustomViewListener() {
            @Override
            public void onDownloadStart() {

            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFinished(String json, HashMap<String, Bitmap> icons) {

            }

            @Override
            public void onDownloadError(int error) {

            }
        });
    }
}
