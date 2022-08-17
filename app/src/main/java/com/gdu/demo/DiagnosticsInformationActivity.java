package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.sdk.base.GDUDiagnostics;

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
        SdkDemoApplication.getAircraftInstance().setDiagnosticsInformationCallback(new GDUDiagnostics.DiagnosticsInformationCallback() {
            @Override
            public void onUpdate(List<GDUDiagnostics> diagnostics) {
                showText(mDiagnosticsInfoTextView, getDiagnostics(diagnostics));
            }
        });
    }

    private String getDiagnostics(List<GDUDiagnostics> diagnostics){
        if (diagnostics != null && diagnostics.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (GDUDiagnostics diagnostic : diagnostics) {
                sb.append(diagnostic.getReason());
                sb.append(";");
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
