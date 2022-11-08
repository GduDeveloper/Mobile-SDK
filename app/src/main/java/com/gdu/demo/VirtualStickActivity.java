package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.gdu.common.error.GDUError;
import com.gdu.demo.views.JoystickView;
import com.gdu.demo.views.OnJoystickListener;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.flightcontroller.FlightControlData;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.logs.RonLog;

import java.util.Timer;
import java.util.TimerTask;

public class VirtualStickActivity extends Activity {

    private JoystickView mLeftJoystickView;
    private JoystickView mRightJoystickView;
    private TextView mFCInfoTextView;
    private MapView mMapView;
    private Context mContext;

    private GDUFlightController mFlightController;
    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;

    private AMap aMap;
    private Marker mPlaneMarker;
    private CoordinateConverter coordinateConverter;
    private MarkerOptions mPlaneMarkerOptions;

    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_stick);
        mContext = this;
        initView();
        initMap(savedInstanceState);
        initData();
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        coordinateConverter = new CoordinateConverter(this);
        coordinateConverter.from(CoordinateConverter.CoordType.GPS);
    }


    private void initData() {
        mFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        mFlightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(FlightControllerState flightControllerState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LocationCoordinate3D locationCoordinate3D  = flightControllerState.getAircraftLocation();
                        LatLng latLng = new LatLng(locationCoordinate3D.getLatitude(), locationCoordinate3D.getLongitude());
                        if (mPlaneMarker != null) {
                            coordinateConverter.coord(latLng);
                            mPlaneMarker.setPosition(coordinateConverter.convert());
                            mPlaneMarker.setRotateAngle(-(float) flightControllerState.getAttitude().yaw);
                        } else {
                            mPlaneMarkerOptions = new MarkerOptions();
                            mPlaneMarkerOptions.position(latLng);
                            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.mipmap.icon_plane).copy(Bitmap.Config.ARGB_8888, true);
                            mPlaneMarkerOptions.anchor(0.5f, 0.5f);
                            mPlaneMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                            mPlaneMarker = aMap.addMarker(mPlaneMarkerOptions);
                            mPlaneMarker.setRotateAngle(-(float) flightControllerState.getAttitude().yaw);
                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }
                        mFCInfoTextView.setText(flightControllerState.getString());
                    }
                });
            }
        });
        mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {

            }
        });
    }

    private void initView() {
        mMapView = findViewById(R.id.map);
        mFCInfoTextView = findViewById(R.id.tv_show_msg);
        mLeftJoystickView = findViewById(R.id.joystick_view_left);
        mRightJoystickView = findViewById(R.id.joystick_view_right);
        mLeftJoystickView.setJoystickListener(new OnJoystickListener() {
            @Override
            public void onTouch(JoystickView joystick, float pX, float pY) {
                Log.d("test ", "test left pX " + pX + " pY " + pY);
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

//                if (horizontalCoordinateFlag) {
//                    if (rollPitchControlModeFlag) {
//                        pitch = (float) (pitchJoyControlMaxSpeed * pX);
//                        roll = (float) (rollJoyControlMaxSpeed * pY);
//                    } else {
//                        pitch = - (float) (pitchJoyControlMaxSpeed * pY);
//                        roll = (float) (rollJoyControlMaxSpeed * pX);
//                    }
//                }

                pitch = (float) (pitchJoyControlMaxSpeed * pY);
                roll = (float) (rollJoyControlMaxSpeed * pX);

                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
                }
            }
        });
        mRightJoystickView.setJoystickListener(new OnJoystickListener() {
            @Override
            public void onTouch(JoystickView joystick, float pX, float pY) {
                Log.d("test ", "test right pX " + pX + " pY " + pY);

                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float verticalJoyControlMaxSpeed = 2;
                float yawJoyControlMaxSpeed = 20;

                yaw = yawJoyControlMaxSpeed * pX;
                throttle = verticalJoyControlMaxSpeed * pY;

                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
                }
            }
        });
    }

    private class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            RonLog.LogD("test FlightControlData pitch " + pitch + " roll " + roll + " yaw " + yaw + " throttle " + throttle);
            FlightControlData flightControlData = new FlightControlData(pitch, roll, yaw, throttle);
            SdkDemoApplication.getAircraftInstance()
                    .getFlightController()
                    .sendVirtualStickFlightControlData(flightControlData,
                            new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(GDUError gduError) {

                                }
                            });
        }
    }

    private void showText(final TextView textView, final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
                textView.setText(content);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
