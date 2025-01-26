package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.gdu.demo.flight.pre.PreFlightInspectionActivity;
import com.gdu.demo.mediatest.MediaTestActivity;


/**
 * 测试组件列表
 */
public class DemoListActivity extends Activity implements View.OnClickListener {

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_demo_list);
        initView();
        initListener();
    }


    private void initView() {

    }

    private void initListener() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.bt_ui_simple:
                Intent flightIntent = new Intent(mContext, PreFlightInspectionActivity.class);
                startActivity(flightIntent);
                break;
            case R.id.mission_operator_button:
                Intent missionIntent = new Intent(mContext, MissionOperatorActivity.class);
                startActivity(missionIntent);
                break;
            case R.id.waypoint_mission_operator_button:
                Intent waypointIntent = new Intent(mContext, WaypointMissionOperatorActivity.class);
                startActivity(waypointIntent);
                break;
            case R.id.flight_controller_button:
                Intent fcIntent = new Intent(mContext, FlightControllerActivity.class);
                startActivity(fcIntent);
                break;
            case R.id.camera_button:
                Intent cameraIntent = new Intent(mContext, CameraGimbalActivity.class);
                startActivity(cameraIntent);
                break;
            case R.id.remote_controller_button:
                Intent rcIntent = new Intent(mContext, RemoteControllerActivity.class);
                startActivity(rcIntent);
                break;
            case R.id.battery_button:
                Intent batteryIntent = new Intent(mContext, BatteryActivity.class);
                startActivity(batteryIntent);
                break;
            case R.id.airlink_button:
                Intent airLinkIntent = new Intent(mContext, AirLinkActivity.class);
                startActivity(airLinkIntent);
                break;
            case R.id.diagnostics_button:
                Intent diagnosticsIntent = new Intent(mContext, DiagnosticsInformationActivity.class);
                startActivity(diagnosticsIntent);
                break;
            case R.id.media_button:

                Intent mediaIntent = new Intent(mContext, MediaTestActivity.class);
                startActivity(mediaIntent);
                break;

        }
    }
}
