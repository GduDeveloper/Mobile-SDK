package com.gdu.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.services.core.ServiceSettings;
import com.gdu.common.error.GDUError;
import com.gdu.demo.databinding.ActivityGeoBinding;
import com.gdu.drone.LocationCoordinate3D;
import com.gdu.misc.NoFlyZonePoint;
import com.gdu.misc.NoFlyZoneState;
import com.gdu.misc.VirtualFencePoint;
import com.gdu.misc.VirtualFenceState;
import com.gdu.sdk.base.BaseProduct;
import com.gdu.sdk.flightcontroller.FlightControllerState;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.misc.GeofencingOperator;
import com.gdu.sdk.misc.GeofencingOperator.GeofencingStateListener;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;

import java.util.ArrayList;
import java.util.List;

/**
 * 地理围栏测试
 */
public class GEOTestActivity extends Activity {

    ActivityGeoBinding binding;
    private GeofencingOperator virtualFenceOperator;
    private MapView mMapView;
    private AMap aMap;
    private Marker mPlaneMarker;
    private Polygon mNoFlyPolygon;
    private Polygon mVirtualFencePolygon;
    private CoordinateConverter coordinateConverter;
    private GDUFlightController mGDUFlightController;

    private static final double HORIZONTAL_DISTANCE = 1000;
    private static final double VERTICAL_DISTANCE = 1000;
    private static final double ONE_METER_OFFSET = 0.00000899322;
    private static final int TEMP_NO_FLY_ZONE_ID = 1;

    private final GeofencingStateListener geofencingStateListener = new GeofencingStateListener() {
        @Override
        public void onVirtualFenceStateChanged(VirtualFenceState state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.tvVirtualFenceState.setText("虚拟围栏状态：" + (state == null ? "未知" : state.name()));
                }
            });
        }

        @Override
        public void onNoFlyZoneStateChanged(NoFlyZoneState state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.tvNoFlyZone.setText("禁飞区状态：" + (state == null ? "未知" : state.name()));
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGeoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initMap(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        virtualFenceOperator = new GeofencingOperator();
        virtualFenceOperator.addListener(geofencingStateListener);

        BaseProduct product = SdkDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            mGDUFlightController = ((GDUAircraft) product).getFlightController();
            mGDUFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState flightControllerState) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LocationCoordinate3D location = flightControllerState.getAircraftLocation();
                                    if (location != null) {
                                        updateAircraftMarker(location.getLatitude(), location.getLongitude());
                                    }
                                    binding.tvFlightControllerState.setText(flightControllerState.getString());

                                }
                            });
                }
            });
        }
    }

    private void initView() {
        mMapView = findViewById(R.id.map);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);

        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView textView = findViewById(R.id.tv_title);
        textView.setText("禁飞区");

        binding.btGetNoFlyList.setOnClickListener(listener);
        binding.btSetNoFly.setOnClickListener(listener);
        binding.btGetRemovedNoFly.setOnClickListener(listener);
        binding.btGetVirtualFence.setOnClickListener(listener);
        binding.btSetVirtualFence.setOnClickListener(listener);
        binding.btCloseVirtualFence.setOnClickListener(listener);
        binding.tvRemovedNoFly.setOnClickListener(listener);
        binding.tvNoFlyZone.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.tvRemovedNoFly.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.tvVirtualFenceState.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.tvFlightControllerState.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        coordinateConverter = new CoordinateConverter(this);
        coordinateConverter.from(CoordinateConverter.CoordType.GPS);
    }

    private void updateAircraftMarker(double latitude, double longitude) {
        if (aMap == null) {
            return;
        }
        com.amap.api.maps.model.LatLng amLatLng = new com.amap.api.maps.model.LatLng(latitude, longitude);
        if (mPlaneMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(amLatLng);
            markerOptions.anchor(0.5f, 0.5f);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_plane).copy(Bitmap.Config.ARGB_8888, true);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            mPlaneMarker = aMap.addMarker(markerOptions);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amLatLng, 16));
        } else {
            mPlaneMarker.setPosition(amLatLng);
        }
    }

    private void showFenceOnMap(List<com.gdu.map.LatLng> pointList, boolean isNoFlyZone) {
        if (aMap == null || pointList == null || pointList.isEmpty()) {
            return;
        }
        List<com.amap.api.maps.model.LatLng> mapPoints = new ArrayList<>();
        for (com.gdu.map.LatLng point : pointList) {
            mapPoints.add(new com.amap.api.maps.model.LatLng(point.latitude, point.longitude));
        }
        if (mapPoints.size() >= 3) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(mapPoints);
            polygonOptions.strokeColor(Color.RED);
            polygonOptions.fillColor(Color.argb(80, 255, 0, 0));
            polygonOptions.strokeWidth(4f);
            Polygon polygon = aMap.addPolygon(polygonOptions);
            if (isNoFlyZone) {
                clearNoFlyPolygon();
                mNoFlyPolygon = polygon;
            } else {
                clearVirtualFencePolygon();
                mVirtualFencePolygon = polygon;
            }
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(com.amap.api.maps.model.LatLngBounds.builder()
                    .include(mapPoints.get(0)).include(mapPoints.get(mapPoints.size() - 1)).build(), 100));
        }
    }

    private void clearNoFlyPolygon() {
        if (mNoFlyPolygon != null) {
            mNoFlyPolygon.remove();
            mNoFlyPolygon = null;
        }
    }

    private void clearVirtualFencePolygon() {
        if (mVirtualFencePolygon != null) {
            mVirtualFencePolygon.remove();
            mVirtualFencePolygon = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mGDUFlightController != null) {
            mGDUFlightController.setStateCallback(null);
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if(id == R.id.bt_set_no_fly){
                uploadNoFlyZone();
            } else if (id == R.id.bt_get_no_fly_list) {
                getNoFlyZoneList();
            } else if (id == R.id.bt_get_removed_no_fly) {
                virtualFenceOperator.closeNoFlyZone((byte) TEMP_NO_FLY_ZONE_ID, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.tvRemovedNoFly.setText("删除临时禁飞区：" + (error == null ? "成功" : error.getDescription()));
                                if (error == null) {
                                    clearNoFlyPolygon();
                                }
                            }
                        });
                    }
                });

            } else if (id == R.id.bt_get_virtual_fence) {
                getVirtualFence();
            } else if (id == R.id.bt_set_virtual_fence) {
                uploadVirtualFence();
            } else if (id == R.id.bt_close_virtual_fence) {
                if (virtualFenceOperator == null) {
                    virtualFenceOperator = new GeofencingOperator();
                }
                virtualFenceOperator.closeVirtualFences(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.tvVirtualFenceState.setText("关闭虚拟围栏：" + (error == null ? "成功" : error.getDescription()));
                                if (error == null) {
                                    clearVirtualFencePolygon();
                                }
                            }
                        });
                    }
                });
            }
        }
    };

    /**
     * 上传禁飞区
     */
    public void uploadNoFlyZone() {
        NoFlyZonePoint noFlyZonePoint = new NoFlyZonePoint();
        noFlyZonePoint.setType(1);
        noFlyZonePoint.setId(TEMP_NO_FLY_ZONE_ID);
        double baseLatitude = 30.499853;
        double baseLongitude = 114.5785446;
        List<com.gdu.map.LatLng> path = new ArrayList<>();
        path.add(new com.gdu.map.LatLng(baseLatitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude));
        path.add(new com.gdu.map.LatLng(baseLatitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        noFlyZonePoint.setPointList(path);
        showFenceOnMap(path, true);
        virtualFenceOperator.uploadNoFlyZone(noFlyZonePoint, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvNoFlyZone.setText("设置临时禁飞区：" + (error == null ? "成功" : error.getDescription()));
                    }
                });
            }
        });
    }

    /**
     * 上传电子围栏
     */
    public void uploadVirtualFence(){
        VirtualFencePoint fencePoint = new VirtualFencePoint();
        fencePoint.setType(1);

        double baseLatitude = 30.499853;
        double baseLongitude = 114.5785446;
        List<com.gdu.map.LatLng> path = new ArrayList<>();
        path.add(new com.gdu.map.LatLng(baseLatitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude + HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        path.add(new com.gdu.map.LatLng(baseLatitude + VERTICAL_DISTANCE * ONE_METER_OFFSET, baseLongitude));
        path.add(new com.gdu.map.LatLng(baseLatitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET, baseLongitude - HORIZONTAL_DISTANCE * ONE_METER_OFFSET));
        fencePoint.setPointList(path);
        showFenceOnMap(path, false);
        if (virtualFenceOperator == null) {
            virtualFenceOperator = new GeofencingOperator();
        }
        virtualFenceOperator.uploadVirtualFences(fencePoint, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvVirtualFenceState.setText("设置虚拟围栏：" + (error == null ? "成功" : error.getDescription()));
                    }
                });
            }
        });
    }

    /**
     * 获取电子围栏
     */
    public void getVirtualFence(){
        virtualFenceOperator.getVirtualFences(new CommonCallbacks.CompletionCallbackWith<VirtualFencePoint>() {
            @Override
            public void onSuccess(VirtualFencePoint result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder builder = new StringBuilder();
                        if (result == null) {
                            binding.tvVirtualFenceState.setText("未获取到虚拟围栏");
                            return;
                        }
                        builder.append("类型：").append(result.getType()).append("\n");
                        List<com.gdu.map.LatLng> pointList = result.getPointList();
                        if (pointList != null && !pointList.isEmpty()) {
                            showFenceOnMap(pointList, false);
                            for (com.gdu.map.LatLng latLng : pointList) {
                                builder.append(latLng.latitude).append(", ").append(latLng.longitude).append("\n");
                            }
                        } else {
                            builder.append("当前围栏为空");
                        }
                        binding.tvVirtualFenceState.setText(builder.toString());
                    }
                });
            }

            @Override
            public void onFailure(GDUError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvVirtualFenceState.setText("获取虚拟围栏失败：" + (error == null ? "未知错误" : error.getDescription()));
                    }
                });
            }
        });
    }


    /**
     * 获取禁飞区列表ID
     */
    public void getNoFlyZoneList() {
        virtualFenceOperator.getNoFlyZoneList(new CommonCallbacks.CompletionCallbackWith<List<Byte>>() {
            @Override
            public void onSuccess(List<Byte> noFlyZoneBeans) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder builder = new StringBuilder("禁飞区ID列表：\n");
                        for (Byte id : noFlyZoneBeans) {
                            builder.append(id).append("\n");
                        }
                        binding.tvNoFlyZone.setText(builder.toString());
                    }
                });

            }

            @Override
            public void onFailure(GDUError var1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvNoFlyZone.setText("暂无禁飞区列表");
                    }
                });
            }
        });
    }
}
