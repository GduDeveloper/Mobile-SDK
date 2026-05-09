package com.gdu.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdu.demo.databinding.ActivityPsdkCustomViewBinding;
import com.gdu.demo.widget.psdk.PSDKCustomViewManager;

public class PsdkCustomViewActivity extends AppCompatActivity {

    private ActivityPsdkCustomViewBinding viewBinding;


    private PSDKCustomViewManager mCustomViewManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPsdkCustomViewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initData();
    }

    private void initData() {
        mCustomViewManager = new PSDKCustomViewManager(this);
        mCustomViewManager.addView(viewBinding.rootLayout, viewBinding.layoutCustomRoot);
    }
}
