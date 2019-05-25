package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gdu.api.GduDroneApi;
import com.gdu.api.GduInfoManager;
import com.gdu.api.listener.OnGduInfoListener;
import com.gdu.demo.util.SeniorPlanningUtils;
import com.gdu.drone.DroneException;
import com.gdu.drone.DroneInfo;

public class SeniorPlanningActivity extends Activity
{
    private SeniorPlanningUtils seniorPlanningUtils;
    private GduInfoManager mGduInfoManager;
    private TextView textView;
    private TextView flyInfoView;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seniorplanning);
        textView  =(TextView) findViewById(R.id.txt_info);
        flyInfoView  =(TextView) findViewById(R.id.fly_info_textview);
        seniorPlanningUtils = new SeniorPlanningUtils(this,textView);

        initData();
    }

    private void initData() {
        mGduInfoManager = GduInfoManager.getInstance(GduDroneApi.getInstance());
        mGduInfoManager.setGduInfoListener(new OnGduInfoListener() {
            @Override
            public void onInfoUpdate(final DroneInfo droneInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        flyInfoView.setText(droneInfo.getString());
                    }
                });
            }

            @Override
            public void onExceptionUpdate(DroneException e) {

            }
        });
    }

    public void onClick(View view)
    {
        seniorPlanningUtils.onClick(view);
    }
}
