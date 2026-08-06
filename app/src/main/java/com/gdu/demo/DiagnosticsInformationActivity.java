package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.flight.msgbox.ErrCodeGetStringUtils;
import com.gdu.lib.util.GsonUtils;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.sdk.base.Diagnostics;

import java.util.List;

/**
 * 健康管理
 */
public class DiagnosticsInformationActivity extends Activity {

    private TextView mDiagnosticsInfoTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics_info);
        initView();
        initData();
    }

    private void initData() {
        SdkDemoApplication.getAircraftInstance().setDiagnosticsInformationCallback(new Diagnostics.DiagnosticsInformationCallback() {
            @Override
            public void onUpdate(List<Diagnostics> diagnostics) {
                showText(mDiagnosticsInfoTextView, getDiagnostics(diagnostics));
            }
        });
    }

    private String getDiagnostics(List<Diagnostics> diagnostics){
        if (diagnostics != null && diagnostics.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Diagnostics diagnostic : diagnostics) {
                sb.append(diagnostic.getCode());
                int warnResId = ErrCodeGetStringUtils.getErrCodeStringResId(diagnostic.getHealthInformation().getComponentId(), diagnostic.getHealthInformation().getFunctionId(), diagnostic.getCode());
                if (warnResId != 0){
                  sb.append(":").append(ResourceUtils.getString(warnResId));
                }

                sb.append(";\r\n");
            }
            return sb.toString();
        }
        return "";
    }

    private void initView() {
        mDiagnosticsInfoTextView = findViewById(R.id.tv_show_msg);
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
}
