package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.config.GduConfig;
import com.gdu.sdk.log.LogItemBean;
import com.gdu.sdk.log.LogManager;
import com.gdu.sdk.log.OnDownloadLogFileListener;
import com.gdu.sdk.log.OnGetAircraftLogListener;
import com.gdu.util.logs.AppLog;
import com.google.gson.Gson;

import java.util.List;

public class LogActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LogActivity";


    private LogManager mPresenter;

    private List<LogItemBean> mAircraftLogList;

    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        initView();
        initData();
    }

    private void initData() {
        mPresenter = new LogManager(this);
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
        textView.setText("测试");
        mTextView = findViewById(R.id.tv_show_msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showText(final TextView textView, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_aircraft_log_button:
                mPresenter.getAircraftLogList(new OnGetAircraftLogListener<LogItemBean>() {
                    @Override
                    public void onGetAircraftDataSuccess(List<LogItemBean> result) {
                        String content = "获取飞行器日志列表成功，result = " + new Gson().toJson(result);
                        AppLog.i(TAG, content);
                        mAircraftLogList = result;
                        showText(mTextView, content);
                    }

                    @Override
                    public void onGetAircraftDataFailed(int code, String errorMsg) {
                        AppLog.i(TAG, "获取飞行器日志列表失败，code = " + code + ", errorMsg = " + errorMsg);
                    }
                });
                break;
            case R.id.download_aircraft_button:
                LogItemBean bean = mAircraftLogList.get(0);
                String savePath = GduConfig.RcLogBaseDir + "/" + bean.path;
                mPresenter.downloadAircraftLog(bean.id, bean.path, savePath, new OnDownloadLogFileListener() {
                    @Override
                    public void onStart() {
                        AppLog.i(TAG, "开始下载日志");
                        showText(mTextView, "开始下载日志");
                    }

                    @Override
                    public void onProgress(int progress) {
                        AppLog.i(TAG, "onProgress");
                        showText(mTextView, "onProgress " + progress);
                    }

                    @Override
                    public void onSuccess(Object result) {
                        AppLog.i(TAG, "下载日志成功");
                        showText(mTextView, "下载日志成功");
                    }

                    @Override
                    public void onFailure(int error) {
                        AppLog.i(TAG, "下载日志失败，error = " + error);
                        showText(mTextView, "下载日志失败，error = " + error);
                    }

                });
                break;
            default:
                break;
        }
    }
}
