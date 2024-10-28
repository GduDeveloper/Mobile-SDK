package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.gdu.common.error.GDUError;
import com.gdu.common.mission.waypoint.Waypoint;
import com.gdu.common.mission.waypoint.WaypointAction;
import com.gdu.common.mission.waypoint.WaypointActionType;
import com.gdu.common.mission.waypoint.WaypointMission;
import com.gdu.common.mission.waypoint.WaypointMissionExecutionEvent;
import com.gdu.common.mission.waypoint.WaypointMissionFinishedAction;
import com.gdu.common.mission.waypoint.WaypointMissionHeadingMode;
import com.gdu.common.mission.waypoint.WaypointMissionState;
import com.gdu.common.mission.waypoint.WaypointMissionUploadEvent;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.rtk.PositioningSolution;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.mission.MissionControl;
import com.gdu.sdk.mission.waypoint.WaypointMissionOperator;
import com.gdu.sdk.mission.waypoint.WaypointMissionOperatorListener;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.simulator.InitializationData;
import com.gdu.sdk.util.CommonCallbacks;

import java.util.ArrayList;
import java.util.List;

/**
 * 航点任务测试
 */
public class WaypointMissionOperatorActivity extends Activity implements LocationSource , View.OnClickListener {

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
    private WaypointMissionOperator waypointMissionOperator = null;
    private WaypointMission mission = null;
    private WaypointMissionOperatorListener listener;
    private GDUCamera mGDUCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_waypoint_mission);
        flyInfoView  =(TextView) findViewById(R.id.fly_info_textview);
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
                            if (mPlaneMarker != null) {
                                LocationCoordinate3D locationCoordinate3D  = flightControllerState.getAircraftLocation();
                                LatLng latLng = new LatLng(locationCoordinate3D.getLatitude(), locationCoordinate3D.getLongitude());
                                coordinateConverter.coord(latLng);
                                mPlaneMarker.setPosition(coordinateConverter.convert());
                            }
                            flyInfoView.setText(flightControllerState.getString());
                        }
                    });
                }
            });
            waypointMissionOperator = getWaypointMissionOperator();
            setUpListener();

            mGDUCamera = (GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera();

        }
    }

    private WaypointMissionOperator getWaypointMissionOperator() {
        if (null == waypointMissionOperator) {
            if (null != MissionControl.getInstance()) {
                return MissionControl.getInstance().getWaypointMissionOperator();
            }
        }
        return waypointMissionOperator;
    }

    private void setUpListener() {
        listener = new WaypointMissionOperatorListener() {

            @Override
            public void onUploadUpdate(WaypointMissionUploadEvent waypointMissionUploadEvent) {
                // Example of Upload Listener
//                if (waypointMissionUploadEvent.getProgress() != null
//                        && waypointMissionUploadEvent.getProgress().isSummaryUploaded
//                        && waypointMissionUploadEvent.getProgress().uploadedWaypointIndex == (WAYPOINT_COUNT - 1)) {
//                    toast("Mission is uploaded successfully");
//                }
                show("Mission is uploaded successfully Progress " + waypointMissionUploadEvent.getProgress());
//                updateWaypointMissionState();
            }

            @Override
            public void onExecutionUpdate(WaypointMissionExecutionEvent waypointMissionExecutionEvent) {
                // Example of Execution Listener
                show(waypointMissionExecutionEvent.getCurrentState().getName()
                        + (waypointMissionExecutionEvent.getProgress() == null
                        ? ""
                        : waypointMissionExecutionEvent.getProgress().targetWaypointIndex));
//                updateWaypointMissionState();
            }

            @Override
            public void onExecutionStart() {
                toast("Mission started");
//                updateWaypointMissionState();
            }

            @Override
            public void onExecutionFinish(GDUError djiError) {
//                show("Mission finished");
                toast("Mission finished");
//                updateWaypointMissionState();
            }
        };

        if (waypointMissionOperator != null && listener != null) {
            // Example of adding listeners
            waypointMissionOperator.addListener(listener);
        }
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

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.mipmap.icon_plane).copy(Bitmap.Config.ARGB_8888, true);
        mPlaneMarkerOptions.anchor(0.5f, 0.5f);
        mPlaneMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mPlaneMarker = aMap.addMarker(mPlaneMarkerOptions);
        aMap.addPolyline(new PolylineOptions().addAll(latLngs).width(width).color(color));
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
            case R.id.load_waypoint_button:
                mission = createWaypointMission();
                addPolyline(mission);
                waypointMissionOperator.loadMission(mission);
                break;
            case R.id.upload_waypoint_button:
                waypointMissionOperator.uploadMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("上传航迹发送成功");
                        } else {
                            toast("上传航迹发送失败");
                        }
                    }
                });
                break;
            case R.id.start_waypoint_button:
                if (waypointMissionOperator.getCurrentState() == WaypointMissionState.READY_TO_EXECUTE) {
                    waypointMissionOperator.startMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                toast("开始航迹成功");
                            } else {
                                toast("开始航迹失败");
                            }
                        }
                    });
                }
                break;
            case R.id.resume_waypoint_button:
                waypointMissionOperator.resumeMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("继续航迹成功");
                        } else {
                            toast("继续航迹失败");
                        }
                    }
                });
                break;
            case R.id.pause_waypoint_button:
                    waypointMissionOperator.pauseMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(GDUError error) {
                            if (error == null) {
                                toast("暂停航迹成功");
                            } else {
                                toast("暂停航迹失败");
                            }
                        }
                    });
                break;
            case R.id.stop_waypoint_button:
                waypointMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("停止航迹成功");
                        } else {
                            toast("停止航迹失败");
                        }
                    }
                });
                break;
        }
    }

    private WaypointMission createWaypointMission() {
        WaypointMission waypointMission = new WaypointMission();
        double baseLatitude = 30.471033;
        double baseLongitude = 114.4280014;


        final float baseAltitude = 30.0f;
        waypointMission.setAutoFlightSpeed(5f);
        waypointMission.setMaxFlightSpeed(10f);
        waypointMission.setResponseLostActionOnRCSignalLost(false);
        waypointMission.setFinishedAction(WaypointMissionFinishedAction.GO_HOME);
        waypointMission.setHeadingMode(WaypointMissionHeadingMode.AUTO);
//      builder.gotoFirstWaypointMode(WaypointMissionGotoWaypointMode.SAFELY);  waypointMission.setGotoFirstWaypointMode(WaypointMissionFlightPathMode.NORMAL);
//      builder.setPointOfInterest(new LocationCoordinate2D(15, 15));
//      builder.headingMode(WaypointMissionHeadingMode.TOWARD_POINT_OF_INTEREST);
        waypointMission.setGimbalPitchRotationEnabled(true);

        List<Waypoint> waypointList = new ArrayList<>();

        // Waypoint 0: (0,0)
        Waypoint waypoint0 = new Waypoint(baseLatitude, baseLongitude, baseAltitude);
        waypoint0.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,0 + calculateTurnAngle()));
        waypoint0.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        waypoint0.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));
        waypoint0.setSpeed(5);
        waypoint0.setGimbalPitch(-90);
        waypointList.add(waypoint0);

        // Waypoint 1: (0,30)
        Waypoint waypoint1 = new Waypoint(baseLatitude, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseAltitude);
        waypoint1.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, 0 - calculateTurnAngle()));
        waypoint1.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        waypoint1.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -45));
        waypoint1.setSpeed(5);
        waypoint1.setGimbalPitch(-45);
        waypointList.add(waypoint1);

        // Waypoint 2: (30,30)
        Waypoint waypoint2 = new Waypoint(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseAltitude);
        waypoint2.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, -180 + calculateTurnAngle()));
        waypoint2.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        waypoint2.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));
        waypoint2.setSpeed(5);
        waypoint2.setGimbalPitch(-90);
        waypointList.add(waypoint2);

        // Waypoint 3: (30,0)
        Waypoint waypoint3 = new Waypoint(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude, baseAltitude);
        waypoint3.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, 180 - calculateTurnAngle()));
        waypoint3.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 0));
        waypoint3.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));
        waypoint3.setSpeed(5);
        waypoint3.setGimbalPitch(0);
        waypointList.add(waypoint3);

        waypointMission.setWaypointCount(waypointList.size());
        waypointMission.setWaypointList(waypointList);
        return waypointMission;
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
