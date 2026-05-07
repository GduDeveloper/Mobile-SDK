package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gdu.common.error.GDUError;
import com.gdu.demo.databinding.ActivityPsdkPipelineBinding;
import com.gdu.sdk.mop.Pipeline;
import com.gdu.sdk.util.CommonCallbacks;

public class PsdkPipelineActivity extends Activity {

    private ActivityPsdkPipelineBinding binding;

    private Handler mHandler;

    private Pipeline mPipeline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPsdkPipelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mHandler = new Handler();
        initView();
        initData();
    }

    private void initView() {
        TextView textView = findViewById(R.id.tv_title);
        textView.setText("Pipeline Test");
        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.tvSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCustomMsg();
            }
        });
    }

    private void initData() {
        mPipeline = new Pipeline();
        mPipeline.setOnPipelineDataListener(new Pipeline.PipelineDataListener() {
            @Override
            public void onDataReceived(byte[] data) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (byte data : data) {
                                stringBuilder.append(Integer.toHexString(data & 0xff)).append(",");
                            }
                            binding.tvReceiveMsg.setText(stringBuilder);
                        }
                    });
                }
            }

            @Override
            public void onError(String msg) {

            }

            @Override
            public void onDisconnected() {

            }
        });
    }

    private void sendCustomMsg() {
        byte[] connect = new byte[10];
        connect[0] = 0x01;
        connect[1] = 0x02;
        connect[2] = 0x03;
        connect[3] = 0x04;
        connect[4] = 0x05;
        connect[5] = 0x10;
        connect[6] = 0x20;
        connect[7] = 0x30;
        connect[8] = 0x40;
        connect[9] = 0x66;
        mPipeline.writeData(connect, 0, 10, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toast("发送成功");
                } else {
                    toast("发送失败");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPipeline.setOnPipelineDataListener(null);
    }

    private void toast(final String toast){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PsdkPipelineActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
