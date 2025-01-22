package com.gdu.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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
import com.gdu.common.mission.followme.FollowMeGimbalPitch;
import com.gdu.common.mission.followme.FollowMeHeading;
import com.gdu.common.mission.followme.FollowMeMission;
import com.gdu.common.mission.followme.FollowMeMissionEvent;
import com.gdu.common.mission.hotpoint.HotpointGimbalPitch;
import com.gdu.common.mission.hotpoint.HotpointHeading;
import com.gdu.common.mission.hotpoint.HotpointMission;
import com.gdu.common.mission.hotpoint.HotpointMissionEvent;
import com.gdu.common.mission.hotpoint.HotpointStartPoint;
import com.gdu.common.mission.waypoint.Waypoint;
import com.gdu.common.mission.waypoint.WaypointMission;
import com.gdu.config.GlobalVariable;
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
import com.gdu.sdk.mission.followme.FollowMeMissionOperator;
import com.gdu.sdk.mission.followme.FollowMeMissionOperatorListener;
import com.gdu.sdk.mission.hotpoint.HotpointMissionOperator;
import com.gdu.sdk.mission.hotpoint.HotpointMissionOperatorListener;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.simulator.InitializationData;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.logs.RonLog;

import java.util.ArrayList;
import java.util.List;

public class MissionOperatorActivity extends Activity implements LocationSource , View.OnClickListener {

    private static final double HORIZONTAL_DISTANCE = 30;
    private static final double VERTICAL_DISTANCE = 30;
    private static final double ONE_METER_OFFSET = 0.00000899322;

    private double latitude = 0;
    private double longitude = 0;

    private boolean isStartFollow;

    private TextView flyInfoView;
    private MapView mMapView;
    private AMap aMap;
    private Marker mPlaneMarker;
    private Marker mGPSTargetMarker;
    private MarkerOptions mPlaneMarkerOptions;
    private CoordinateConverter coordinateConverter;
    private Context mContext;
    private TextView mMissionInfoTextView;
    private CheckBox mSetDistanceAndHeightEnableCheckBox;

    private GDUFlightController mGDUFlightController;

    private GDUCamera mGDUCamera;

    private HotpointMissionOperator mHotpointMissionOperator;

    private FollowMeMissionOperator mFollowMeMissionOperator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_mission);
        flyInfoView  =(TextView) findViewById(R.id.fly_info_textview);
        mMapView = findViewById(R.id.map);
        mMissionInfoTextView = findViewById(R.id.mission_info_textview);
        mSetDistanceAndHeightEnableCheckBox = findViewById(R.id.set_height_distance_enable_checkbox);
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
                            flyInfoView.setText(flightControllerState.getString());
                        }
                    });
                }
            });

            mHotpointMissionOperator = getHotpointMissionOperator();

            mFollowMeMissionOperator = getFollowMeMissionOperator();
            setUpListener();

            mGDUCamera = (GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera();

        }
    }

    private void setUpListener() {
        mHotpointMissionOperator.addListener(new HotpointMissionOperatorListener() {
            @Override
            public void onExecutionUpdate(HotpointMissionEvent paramHotpointMissionEvent) {
                toast("环绕状态 " + paramHotpointMissionEvent.getCurrentState().getName());
            }

            @Override
            public void onExecutionStart() {
                toast("环绕状态 开始");
            }

            @Override
            public void onExecutionFinish(GDUError error) {
                toast("环绕状态 结束 " + error.getDescription());
            }
        });

        mFollowMeMissionOperator.addListener(new FollowMeMissionOperatorListener() {
            @Override
            public void onExecutionUpdate(FollowMeMissionEvent followMeMissionEvent) {
                toast("跟随状态 " + followMeMissionEvent.getCurrentState());
            }

            @Override
            public void onExecutionStart() {
                toast("跟随状态 开始");
            }

            @Override
            public void onExecutionFinish(GDUError gduError) {
                toast("跟随状态 结束" + gduError);
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

    private FollowMeMissionOperator getFollowMeMissionOperator() {
        if (null == mFollowMeMissionOperator) {
            if (null != MissionControl.getInstance()) {
                return MissionControl.getInstance().getFollowMeMissionOperator();
            }
        }
        return mFollowMeMissionOperator;
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
            mGDUFlightController.switchSmartBattery();
        }
    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.simulator_button:
                startSimulator();
                break;
            case R.id.set_home_point_button:
                LocationCoordinate2D coordinate2D = new LocationCoordinate2D(30.471033, 114.4280014);
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
                RonLog.LogD("test status " + MissionControl.getInstance().getHotpointMissionOperator().getCurrentState().getName());
                HotpointMission hotpointMission = new HotpointMission();
                LocationCoordinate2D hotpoint = new LocationCoordinate2D(30.471033, 114.4280014);
                hotpointMission.setHotpoint(hotpoint);
                hotpointMission.setAltitude(50);
                hotpointMission.setClockwise(true);
                hotpointMission.setAngularVelocity(0.10019f);
                hotpointMission.setRadius(80);
                hotpointMission.setHeading(HotpointHeading.TOWARDS_HOT_POINT);
                hotpointMission.setStartPoint(HotpointStartPoint.NORTH);
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

            case R.id.pause_hotpoint_button:
                mHotpointMissionOperator.pause(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("暂停环绕发送成功");
                        } else {
                            toast("暂停环绕发送失败");
                        }
                    }
                });
                break;

            case R.id.continue_hotpoint_button:
                mHotpointMissionOperator.resume(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError gduError) {

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
            case R.id.set_hotpoint_heading_button:
                mHotpointMissionOperator.setHotPointHeading(HotpointHeading.AWAY_FROM_HOT_POINT, 0, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置机头角度发送成功");
                        } else {
                            toast("设置机头角度发送失败");
                        }
                    }
                });
                break;
            case R.id.set_hotpoint_gimbal_pitch_button:
                mHotpointMissionOperator.setHotpointGimbalPitch(HotpointGimbalPitch.SET_GIMBAL_PITCH, 60, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置云台角度发送成功");
                        } else {
                            toast("设置云台角度发送失败");
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
            case R.id.start_follow_button:
                startFollow();
                break;
            case R.id.stop_follow_button:
                mFollowMeMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        isStartFollow = false;
                        if (error == null) {
                            toast("停止跟随发送成功");
                        } else {
                            toast("停止跟随发送失败");
                        }
                    }
                });
                break;
            case R.id.start_high_precision_follow_button:
                startHighPrecisionFollow();
                break;

            case R.id.stop_high_precision_follow_button:
                mFollowMeMissionOperator.stopMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        isStartFollow = false;
                        if (error == null) {
                            toast("停止跟随发送成功");
                        } else {
                            toast("停止跟随发送失败");
                        }
                    }
                });
                break;
            case R.id.set_follow_me_heading_button:
                mFollowMeMissionOperator.setFollowMeHeading(FollowMeHeading.SET_HEADING_ANGLE, 50, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置跟随机头发送成功");
                        } else {
                            toast("设置跟随机头发送失败");
                        }
                    }
                });
                break;
            case R.id.set_follow_me_gimbal_pitch_button:
                mFollowMeMissionOperator.setFollowMeGimbalPitch(FollowMeGimbalPitch.SET_GIMBAL_PITCH, 80, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("设置跟随云台发送成功");
                        } else {
                            toast("设置跟随云台发送失败");
                        }
                    }
                });
                break;
            case R.id.start_fly_button:
                mGDUFlightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("开始起飞发送成功");
                        } else {
                            toast("开始起飞发送失败");
                        }
                    }
                });
                break;
            case R.id.start_land_button:
                mGDUFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        if (error == null) {
                            toast("开始降落发送成功");
                        } else {
                            toast("开始降落发送失败");
                        }
                    }
                });
                break;
        }
    }

    /**
     * 开启高精度GPS跟随，需地面端安装RTK定位模块
     */
    private void startHighPrecisionFollow(){
        boolean isSetDistanceAndHeightEnable;
        if (mSetDistanceAndHeightEnableCheckBox.isChecked()) {
            isSetDistanceAndHeightEnable = true;
        } else {
            isSetDistanceAndHeightEnable = false;
        }
//        latitude = GlobalVariable.latitude;
//        longitude = GlobalVariable.longitude;
        FollowMeMission followMeMission = new FollowMeMission(FollowMeHeading.TOWARD_FOLLOW_POSITION, latitude, longitude, true, isSetDistanceAndHeightEnable, 15f,  isSetDistanceAndHeightEnable, 3, 0);
        mFollowMeMissionOperator.startMission(followMeMission, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    if (error == null) {
                        toast("开始跟随发送成功");
                    } else {
                        toast("开始跟随发送失败");
                    }
                }
            }
        });
    }

    private void startFollow(){
        latitude = 30.471033;
        longitude = 114.4280014;
        mFollowMeMissionOperator.startMission(new FollowMeMission(FollowMeHeading.TOWARD_FOLLOW_POSITION,
                latitude + 5 * ONE_METER_OFFSET, longitude + 5 * ONE_METER_OFFSET, 30f
        ), new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    toast("开始跟随发送成功");
                } else {
                    toast("开始跟随发送失败");
                }
                if (error == null) {
                    isStartFollow = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int cnt = 0;
                            while(cnt < 100 && isStartFollow) {
                                latitude = latitude + 5 * ONE_METER_OFFSET;
                                longitude = longitude + 5 * ONE_METER_OFFSET;
                                LocationCoordinate2D newLocation = new LocationCoordinate2D(latitude, longitude);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LatLng latLng = new LatLng(latitude, longitude);
                                        if (mGPSTargetMarker != null) {
                                            coordinateConverter.coord(latLng);
                                            mGPSTargetMarker.setPosition(coordinateConverter.convert());
                                        } else {
                                            MarkerOptions markerOptions = new MarkerOptions();
                                            markerOptions.position(latLng);
                                            mGPSTargetMarker = aMap.addMarker(markerOptions);
                                        }
                                    }
                                });
                                RonLog.LogD("test FollowingTarget " + newLocation.toString());
                                mFollowMeMissionOperator.updateFollowingTarget(newLocation, new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(GDUError error) {
                                        if (error == null) {
                                            toast("跟随目标点发送成功");
                                        } else {
                                            toast("跟随目标点发送失败");
                                        }
                                    }
                                });
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                cnt++;
                            }
                        }
                    }).start();
                }
            }
        });
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
