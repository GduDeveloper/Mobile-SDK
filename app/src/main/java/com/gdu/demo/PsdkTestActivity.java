package com.gdu.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdu.demo.databinding.ActivityPsdkTestBinding;


public class PsdkTestActivity extends AppCompatActivity {
    private ActivityPsdkTestBinding viewBinding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPsdkTestBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        initData();
    }

    private void initView() {
        findViewById(R.id.psdk_mega_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, PsdkMegaViewActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.psdk_pipeline_button).setOnClickListener(v -> {
            Intent msgIntent = new Intent(this, PsdkPipelineActivity.class);
            startActivity(msgIntent);
        });

        findViewById(R.id.psdk_customview_button).setOnClickListener(v -> {
            Intent msgIntent = new Intent(this, PsdkCustomViewActivity.class);
            startActivity(msgIntent);
        });
    }

    private void initData() {

    }


}
