package com.gdu.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.gdu.common.error.Error;
import com.gdu.demo.viewmodel.MissionOperatorViewModel;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.flightcontroller.TapFlyState;
import com.gdu.msdk.enums.OrbitPitchEnum;
import com.gdu.rtk.PositioningSolution;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.camera.Camera;
import com.gdu.sdk.camera.SystemState;
import com.gdu.sdk.flightcontroller.FlightController;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.products.Aircraft;
import com.gdu.sdk.simulator.InitializationData;
import com.gdu.sdk.util.CommonCallbacks;

public class MissionOperatorActivity extends FragmentActivity /*implements LocationSource , View.OnClickListener*/ {

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

    private FlightController mGDUFlightController;

    private Camera mGDUCamera;
    private MissionOperatorViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_mission);

        viewModel = new ViewModelProvider(this).get(MissionOperatorViewModel.class);

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
            mGDUFlightController = ((Aircraft) product).getFlightController();
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
            mGDUCamera = (Camera) ((Aircraft) SdkDemoApplication.getProductInstance()).getCamera();
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
            mGDUFlightController.getSimulator().start(initializationData, new CommonCallbacks.CompletionCallback<Error>() {
                @Override
                public void onResult(Error var1) {
                    if (var1 == null) {
                        toast("开启模拟飞行成功");
                    } else {
                        toast("开启模拟飞行失败");
                    }
                }
            });
            mGDUFlightController.switchSmartBattery();
        }
    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.simulator_button:  //开启模拟飞行
                startSimulator();
                break;
            case R.id.set_home_point_button: //设置home点
                LocationCoordinate2D coordinate2D = new LocationCoordinate2D(30.471033, 114.4280014);
                coordinateConverter.coord(new LatLng(coordinate2D.getLatitude(), coordinate2D.getLongitude()));
                LatLng latLng = coordinateConverter.convert();
                addMarker(latLng);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                mGDUFlightController.setHomeLocation(coordinate2D, error -> {
                    if (error == null) {
                        toast("home点设置成功");
                    } else {
                        toast("home点设置失败");
                    }
                });
                break;
            case R.id.start_hotpoint_button: //开始环绕
                viewModel.startSurroundMission();
                break;
            case R.id.pause_hotpoint_button: //暂停环绕
                viewModel.pauseSurroundMission();
                break;

            case R.id.continue_hotpoint_button: //继续环绕
                viewModel.resumeSurroundMission();
                break;
            case R.id.stop_hotpoint_button: //结束环绕
                viewModel.stopSurroundMission();
                break;
            case R.id.set_hotpoint_heading_button:
                viewModel.setSurroundHeadingType();
                break;
            case R.id.set_hotpoint_gimbal_pitch_button:
                mGDUFlightController.setSurroundGimbalAngle(OrbitPitchEnum.GIMBAL,60, error -> {
                    if (error == null) {
                        toast("设置云台角度发送成功");
                    } else {
                        toast("设置云台角度发送失败");
                    }
                });
                break;

            case R.id.start_tapfly_button: //开始指点飞行
                coordinateConverter.coord(new LatLng(30.471043, 114.4290814));
                LatLng latLng1 = coordinateConverter.convert();
                addMarker(latLng1);

                LocationCoordinate3D targetPoint = new LocationCoordinate3D(30.471043, 114.4290814, 50);
                float hSpeed = 15;
                float vSpeed = 5;
                mGDUFlightController.startTapFly(targetPoint, hSpeed, vSpeed, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(Error error) {
                        if (error == null) {
                            toast("开始指点飞行发送成功");
                        } else {
                            toast("开始指点飞行发送失败");
                        }
                    }
                });
                break;
            case R.id.stop_tapfly_button: //停止指点飞行
                mGDUFlightController.stopTapFly(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(Error error) {
                        if (error == null) {
                            toast("停止指点飞行发送成功");
                        } else {
                            toast("停止指点飞行发送失败");
                        }
                    }
                });
                break;
            case R.id.start_follow_button:
                viewModel.startFollow();
                break;
            case R.id.stop_follow_button:
                viewModel.stopFollow();
                break;
            case R.id.start_high_precision_follow_button:
                startHighPrecisionFollow();
                break;

            case R.id.stop_high_precision_follow_button:
                viewModel.stopGpsDifferentialFollow();
                break;
            case R.id.set_follow_me_heading_button:
                viewModel.setFollowHeadingType();
                break;
            case R.id.set_follow_me_gimbal_pitch_button:
                viewModel.setFollowGimbalAngle();
                break;
            case R.id.start_fly_button: //开始起飞
                mGDUFlightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(Error error) {
                        if (error == null) {
                            toast("开始起飞发送成功");
                        } else {
                            toast("开始起飞发送失败");
                        }
                    }
                });
                break;
            case R.id.start_land_button: //开始降落
                mGDUFlightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(Error error) {
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
        viewModel.startGpsDifferentialFollow(isSetDistanceAndHeightEnable);
    }


    public void toast(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMissionInfoTextView.setText(toast);
            }
        });
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
