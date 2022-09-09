package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.gdu.common.error.GDUError;
import com.gdu.common.mission.hotpoint.HotpointMission;
import com.gdu.common.mission.hotpoint.HotpointMissionEvent;
import com.gdu.common.mission.hotpoint.HotpointStartPoint;
import com.gdu.common.mission.waypoint.Waypoint;
import com.gdu.common.mission.waypoint.WaypointMission;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.flightcontroller.TapFlyState;
import com.gdu.rtk.PositioningSolution;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.mission.MissionControl;
import com.gdu.sdk.mission.hotpoint.HotpointMissionOperator;
import com.gdu.sdk.mission.hotpoint.HotpointMissionOperatorListener;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.simulator.InitializationData;
import com.gdu.sdk.util.CommonCallbacks;

import java.util.ArrayList;
import java.util.List;

public class MissionOperatorActivity extends Activity implements LocationSource , View.OnClickListener {

    private static final double HORIZONTAL_DISTANCE = 30;
    private static final double VERTICAL_DISTANCE = 30;
    private static final double ONE_METER_OFFSET = 0.00000899322;

    private TextView flyInfoView;
    private MapView mMapView;
    private AMap aMap;
    private Marker mPlaneMarker;
    private MarkerOptions mPlaneMarkerOptions;
    private CoordinateConverter coordinateConverter;
    private Context mContext;
    private TextView mMissionInfoTextView;

    private GDUFlightController mGDUFlightController;

    private GDUCamera mGDUCamera;

    private HotpointMissionOperator mHotpointMissionOperator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_mission);
        flyInfoView  = (TextView) findViewById(R.id.fly_info_textview);
        mMapView = findViewById(R.id.map);
        mMissionInfoTextView = findViewById(R.id.mission_info_textview);
        initMap(savedInstanceState);
        initData();
        initListener();
    }

    private void initData() {
        BaseProduct product = SdkDemoApplication.getProductInstance();
        if (product == null || !product.isConnected()) {
            return;
        } else {
            mGDUFlightController = ((GDUAircraft) product).getFlightController();
            mGDUFlightController.setStateCallback(new FlightControllerState.Callback() {
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
                            } else {
                                mPlaneMarkerOptions = new MarkerOptions();
                                mPlaneMarkerOptions.position(latLng);
                                mPlaneMarker = aMap.addMarker(mPlaneMarkerOptions);
                            }
                            flyInfoView.setText(flightControllerState.getString());
                        }
                    });
                }
            });

            mHotpointMissionOperator = getHotpointMissionOperator();
            setUpListener();

            mGDUCamera = (GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera();

        }
    }

    private void setUpListener() {
        mHotpointMissionOperator.addListener(new HotpointMissionOperatorListener() {
            @Override
            public void onExecutionUpdate(HotpointMissionEvent paramHotpointMissionEvent) {
                toast("环绕状态 " + paramHotpointMissionEvent);
            }

            @Override
            public void onExecutionStart() {
                toast("环绕状态 开始");
            }

            @Override
            public void onExecutionFinish(GDUError error) {
                toast("环绕状态 结束 " + error);
            }
        });
    }


    private HotpointMissionOperator getHotpointMissionOperator() {
        if (null == mHotpointMissionOperator) {
            if (null != MissionControl.getInstance()) {
                return MissionControl.getInstance().getHotpointMissionOperator();
            }
        }
        return mHotpointMissionOperator;
    }

    private void initListener() {
        if (mGDUCamera != null) {
            mGDUCamera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState systemState) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" isPhotoStored ");
                    sb.append(systemState.isPhotoStored());
                    sb.append(" hasError ");
                    sb.append(systemState.isHasError());
                    sb.append(" isRecording ");
                    sb.append(systemState.isRecording());
                    sb.append(" mode ");
                    sb.append(systemState.getMode());
                    sb.append(" time ");
                    sb.append(systemState.getCurrentVideoRecordingTimeInSeconds());
                    if (systemState.isPhotoStored()) {
                        toast(sb.toString());
                    }
                }
            });
        }
        if (mGDUFlightController != null) {
            mGDUFlightController.setTapFlyStateCallback(new TapFlyState.Callback() {
                @Override
                public void onUpdate(TapFlyState state) {
                    show("指点飞行状态： " +  state);
                }
            });
        }
    }

    int index = 0;
    private void addPolyline(WaypointMission waypointMission){
        if(index == 0){
            index++;
            addPolyline(waypointMission.getWaypointList(), Color.argb(255, 1, 1, 1), 10);
        } else {
            addPolyline(waypointMission.getWaypointList(), Color.argb(255, 255, 1, 1), 10);
        }

    }

    private void addPolyline(List<Waypoint> planPoints, int color, int width){
        List<LatLng> latLngs = new ArrayList<>();
        if (planPoints == null) {
            return;
        }
        for (Waypoint waypoint : planPoints) {
            LocationCoordinate2D locationCoordinate2D = waypoint.getCoordinate();
            LatLng latLng = new LatLng(locationCoordinate2D.getLatitude(), locationCoordinate2D.getLongitude());
            coordinateConverter.coord(latLng);
            latLngs.add(coordinateConverter.convert());
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 16));
        mPlaneMarkerOptions = new MarkerOptions();
        mPlaneMarkerOptions.position(latLngs.get(0));
        mPlaneMarker = aMap.addMarker(mPlaneMarkerOptions);
        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(width).color(color));
    }

    private Marker addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        return aMap.addMarker(markerOptions);
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        coordinateConverter = new CoordinateConverter(this);
        coordinateConverter.from(CoordinateConverter.CoordType.GPS);
    }

    private void startSimulator() {
        if (null != mGDUFlightController) {
            LocationCoordinate3D locationCoordinate3D = new LocationCoordinate3D(30.471033,114.4280014, 10);
            InitializationData initializationData = new InitializationData(locationCoordinate3D, (short) 90, PositioningSolution.FIXED_POINT, (byte) 30);
            mGDUFlightController.getSimulator().start(initializationData, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError gduError) {
                    if (gduError == null) {

                    }
                }
            });
        }
    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.simulator_button:
                startSimulator();
                break;
            case R.id.set_home_point_button:
                LocationCoordinate2D coordinate2D = new LocationCoordinate2D(30.471038, 114.4280024);
                coordinateConverter.coord(new LatLng(coordinate2D.getLatitude(), coordinate2D.getLongitude()));
                LatLng latLng = coordinateConverter.convert();
                addMarker(latLng);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                mGDUFlightController.setHomeLocation(coordinate2D, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("home点设置成功");
                        } else {
                            toast("home点设置失败");
                        }
                    }
                });
//                mission = createWaypointMission();
//                addPolyline(mission);
//                waypointMissionOperator.loadMission(mission);
                break;
            case R.id.start_hotpoint_button:
                HotpointMission hotpointMission = new HotpointMission();
                LocationCoordinate2D hotpoint = new LocationCoordinate2D(30.471033, 114.4280014);
                hotpointMission.setHotpoint(hotpoint);
                hotpointMission.setAltitude(50);
                hotpointMission.setClockwise(true);
                hotpointMission.setAngularVelocity(30);
                hotpointMission.setRadius(30);
                hotpointMission.setStartPoint(HotpointStartPoint.NEAREST);
                mHotpointMissionOperator.startMission(hotpointMission, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("开始环绕发送成功");
                        } else {
                            toast("开始环绕发送失败");
                        }
                    }
                });
                break;
            case R.id.stop_hotpoint_button:
                mHotpointMissionOperator.stop(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("结束环绕发送成功");
                        } else {
                            toast("结束环绕发送失败");
                        }
                    }
                });
                break;
            case R.id.start_tapfly_button:

//                LocationCoordinate2D target2D = new LocationCoordinate2D(30.471038, 114.4280024);
                coordinateConverter.coord(new LatLng(30.471043, 114.4290814));
                LatLng latLng1 = coordinateConverter.convert();
                addMarker(latLng1);

                LocationCoordinate3D targetPoint = new LocationCoordinate3D(30.471043, 114.4290814, 50);
                float hSpeed = 15;
                float vSpeed = 5;
                mGDUFlightController.startTapFly(targetPoint, hSpeed, vSpeed, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("开始指点飞行发送成功");
                        } else {
                            toast("开始指点飞行发送失败");
                        }
                    }
                });
                break;
            case R.id.stop_tapfly_button:
                mGDUFlightController.stopTapFly(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("停止指点飞行发送成功");
                        } else {
                            toast("停止指点飞行发送失败");
                        }
                    }
                });
                break;
            case R.id.stop_waypoint_button:
//                waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
//                    @Override
//                    public void onResult(GDUError error) {
//                        if (error == null) {
//                            toast("停止航迹成功");
//                        } else {
//                            toast("停止航迹失败");
//                        }
//                    }
//                });
                break;
        }
    }


    private int calculateTurnAngle() {
        return Math.round((float)Math.toDegrees(Math.atan(VERTICAL_DISTANCE/ HORIZONTAL_DISTANCE)));
    }


    public void toast(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mVideoPicTextView.setText(toast);
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMissionInfoTextView.setText(toast);
//                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
