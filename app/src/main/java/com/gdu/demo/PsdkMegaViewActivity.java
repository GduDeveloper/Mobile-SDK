package com.gdu.demo;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdu.demo.databinding.ActivityPsdkMegaViewBinding;
import com.gdu.sdk.psdk.Megaphone;
import com.rxjava.rxlife.RxLife;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PsdkMegaViewActivity extends AppCompatActivity {


    ActivityPsdkMegaViewBinding viewBinding;

    Handler handler;

    Megaphone megaphone;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityPsdkMegaViewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        initView();
        initData();
    }

    private void initView() {

        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView textView = findViewById(R.id.tv_title);
        textView.setText("Mega Test");
        handler = new Handler();
        megaphone = SdkDemoApplication.getAircraftInstance().getMegaphone();

        viewBinding.tvPlayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playText();
            }
        });

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(this))
                .subscribe(aLong -> {

                    if (megaphone.isConnected()) {
                        viewBinding.tvConnect.setText("喊话器已连接");
                    } else {
                        viewBinding.tvConnect.setText("喊话器未连接");
                    }

                }, throwable ->{

                });


        if (megaphone != null) {
            megaphone.setStateListener(new Megaphone.OnMegaPhoneStateListener() {
                @Override
                public void stateUpdate(int code, int megaphoneWorkType, int megaphonePlayMode, int megaphoneVolume) {

                    if (handler != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder builder = new StringBuilder();
                                builder.append("播放状态：");
                                if (code == 0) {
                                    builder.append("空闲中");
                                } else if (code == 1) {
                                    builder.append("传输中");
                                } else if (code == 2) {
                                    builder.append("播放中");
                                } else if (code == 3) {
                                    builder.append("播放错误");
                                } else if (code == 4) {
                                    builder.append("文本转换中");
                                }
                                builder.append(", ");
                                builder.append("音量：" + megaphoneVolume);

                                viewBinding.tvState.setText(builder.toString());
                            }
                        });
                    }

                }
            });
        }


    }

    private void initData() {





    }

    private void playText() {
        String msg = viewBinding.etMessage.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }
        megaphone.playText(msg);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (megaphone != null) {
            megaphone.setStateListener(null);
        }
    }
}
