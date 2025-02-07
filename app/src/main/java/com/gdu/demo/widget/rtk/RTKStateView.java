package com.gdu.demo.widget.rtk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.ViewRtkStateBinding;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * RTK 的设置界面
 */
public class RTKStateView extends LinearLayout {

    private final Context mContext;

    private ViewRtkStateBinding viewBinding;


    public RTKStateView(Context context) {
        this(context, null);
    }

    public RTKStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTKStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initUpdate() {
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .to(RxLife.toMain(this, true))
                .subscribe(aLong -> {
                    if (getVisibility() != View.VISIBLE) {
                        return;
                    }
                    updateRTKInfo();
                }, throwable -> {
                    MyLogUtils.e("更新RTK信息出错", throwable);
                }, () -> {});

    }

    /**
     * 设置基站名称
     * @param name
     */
    public void setStationName(String name){
        viewBinding.stationNameTextview.setText(name);
    }

    private void initView() {
        MyLogUtils.i("initView()");
        viewBinding = ViewRtkStateBinding.inflate(LayoutInflater.from(mContext), this, true);
        if(DroneUtil.showBDSOrGNSS()){
            viewBinding.layoutGalileo.setVisibility(View.GONE);
            viewBinding.layoutGps.setVisibility(View.GONE);
            viewBinding.layoutGlonass.setVisibility(View.GONE);
        }else{
            viewBinding.layoutGalileo.setVisibility(View.VISIBLE);
            viewBinding.layoutGps.setVisibility(View.VISIBLE);
            viewBinding.layoutGlonass.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新RTK相关数据
     */
    public void updateRTKInfo(){
        if (GlobalVariable.rtk_model.isPpsSignalIsNormal()) {
            viewBinding.tvPPSStatusContent.setText(mContext.getString(R.string.flight_connect));
            viewBinding.tvPPSStatusContent.setTextColor(ContextCompat.getColor(mContext, R.color.color_0BE25C));
        } else {
            viewBinding.tvPPSStatusContent.setText(mContext.getString(R.string.flight_loast_connect));
            viewBinding.tvPPSStatusContent.setTextColor(ContextCompat.getColor(mContext, R.color.color_EB4242));
        }

        viewBinding.tvDroneRtkState.setText(GlobalVariable.rtk_model.getRtk1_status());
        viewBinding.tvDroneLng.setText(new BigDecimal(GlobalVariable.GPS_Lon).setScale(8, RoundingMode.HALF_UP).toString());
        viewBinding.tvDroneLat.setText(new BigDecimal(GlobalVariable.GPS_Lat).setScale(8, RoundingMode.HALF_UP).toString());

        String ellipsoidalHeight = UnitChnageUtils.getDecimalFormatUnit((float) (GlobalVariable.altitude_drone / 100.0), UnitChnageUtils.format_three);
        viewBinding.tvEllipsoidalHeight.setText(ellipsoidalHeight);
        String altitude = UnitChnageUtils.getDecimalFormatUnit((float) (GlobalVariable.asl_drone / 100.0), UnitChnageUtils.format_three);
        viewBinding.tvAltitude.setText(altitude);

        viewBinding.tvDroneStationNum.setText(GlobalVariable.satellite_drone + "");
        viewBinding.tvBdNum.setText(GlobalVariable.bdsNum + "");
        viewBinding.tvGpsNum.setText(GlobalVariable.gpsNum + "");
        viewBinding.tvGalileoNum.setText(GlobalVariable.galileoNum + "");
        viewBinding.tvGlonassNum.setText(GlobalVariable.glonaNum + "");

        // 基站RTK
        if (GlobalVariable.sRTKType == 2) {
            if (GlobalVariable.drtkInformation != null) {

                String drtkState = "";
                if (GlobalVariable.drtkInformation.positionState == 4) {
                    drtkState = "fixed";
                } else if (GlobalVariable.drtkInformation.positionState == 5) {
                    drtkState = "Float";
                } else {
                    drtkState = GlobalVariable.drtkInformation.positionState + "";
                }

                viewBinding.tvStateRtkStatus.setText(drtkState);
                viewBinding.tvStationLng.setText(GlobalVariable.drtkInformation.lon + "");
                viewBinding.tvStationLat.setText(GlobalVariable.drtkInformation.lat + "");
                viewBinding.tvStationEllipsoidalHeight.setText(GlobalVariable.drtkInformation.ellipsoidHeight + "");
                viewBinding.tvStationAltitude.setText(GlobalVariable.drtkInformation.altitudeHeight + "");
                viewBinding.tvStationNum.setText(GlobalVariable.drtkInformation.satelliteNum + "");
                viewBinding.tvStationBdNum.setText(GlobalVariable.drtkInformation.bdSatelliteNum + "");
                viewBinding.tvStationGpsNum.setText(GlobalVariable.drtkInformation.gpsSatelliteNum + "");
                viewBinding.tvStationGalileoNum.setText(GlobalVariable.drtkInformation.galileoSatelliteNum + "");
                viewBinding.tvStationGlonassNum.setText(GlobalVariable.drtkInformation.glonassSatelliteNum + "");
                viewBinding.tvStationQzssNum.setText(GlobalVariable.drtkInformation.qzssSatelliteNum + "");
                viewBinding.tvStationLonDeviation.setText(GlobalVariable.drtkInformation.lonStandardDeviation + "");
                viewBinding.tvStationLatDeviation.setText(GlobalVariable.drtkInformation.latStandardDeviation + "");
                viewBinding.tvStationHeightDeviation.setText(GlobalVariable.drtkInformation.heightStandardDeviation + "");
            } else {
                viewBinding.tvStateRtkStatus.setText("N/A");
                viewBinding.tvStationLng.setText("");
                viewBinding.tvStationLat.setText("");
                viewBinding.tvStationEllipsoidalHeight.setText( "");
                viewBinding.tvStationAltitude.setText("");
                viewBinding.tvStationNum.setText( "");
                viewBinding.tvStationBdNum.setText( "");
                viewBinding.tvStationGpsNum.setText("");
                viewBinding.tvStationGalileoNum.setText("");
                viewBinding.tvStationGlonassNum.setText("");
                viewBinding.tvStationQzssNum.setText("");
                viewBinding.tvStationLonDeviation.setText( "");
                viewBinding.tvStationLatDeviation.setText("");
                viewBinding.tvStationHeightDeviation.setText("");
            }
        } else {
            viewBinding.tvStateRtkStatus.setText("");
            viewBinding.tvStationLng.setText(String.valueOf(GlobalVariable.sStationLng));
            viewBinding.tvStationLat.setText(String.valueOf(GlobalVariable.sStationLat));
        }

    }


    public void updateShowView(int type) {

        if (type == 2) {
            viewBinding.layoutDrtkView.setVisibility(VISIBLE);
        } else {
            viewBinding.layoutDrtkView.setVisibility(GONE);
        }

    }

}
