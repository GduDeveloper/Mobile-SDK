
package com.gdu.demo.flight.calibration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.gdu.demo.R;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.demo.utils.ToolManager;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.bean.PlanType;

/**
 * 新指南针校磁界面
 */
public class RectifyMagnetometerActivityNew extends FragmentActivity {
    private static final String TAG = RectifyMagnetometerActivityNew.class.getSimpleName();
    RectifyDroneHelper mRectifyDroneHelper;
    private View llRectifyNew;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rectify_magnetometer_new);
        findViews();
        initViews();
        initLisenter();
    }

    public void findViews() {
        llRectifyNew = findViewById(R.id.llRectifyNew);

        mRectifyDroneHelper = new RectifyDroneHelper(this, llRectifyNew);
    }

    public void initViews() {
        solveNavigationBar();
    }

    public void initLisenter() {
        mRectifyDroneHelper.setOnCheckNorthListener(onCheckNorthListener);
    }

    private final RectifyDroneHelper.OnCheckNorthListener onCheckNorthListener = needShowRestart -> {
        XLogger.INSTANCE.getAPP().i("onCheckNorthOver() needShowRestart = " + needShowRestart);
        if(needShowRestart){
            showRestartDialog();
        }else{
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRectifyDroneHelper != null) {
            mRectifyDroneHelper.onDestory();
        }
    }

    private void showRestartDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder(getSupportFragmentManager());
        builder.setPositiveListener((dialogInterface, i) -> sucFinish())
                .setNegativeListener((dialogInterface, i) -> sucFinish())
                .setOnDismissListener(dialog -> {
                    if (!isFinishing()) {
                        sucFinish();
                    }
                });

        PlanType planType = IGduDroneDevice.get().getPlanType().getValue();
        if ((planType == PlanType.S220
                || planType == PlanType.S280
                || planType == PlanType.S200
                || planType == PlanType.S220Pro
                || planType == PlanType.S220ProS
                || planType == PlanType.S220ProH
                || planType == PlanType.S220_SD
                || planType == PlanType.S200_SD
                || planType == PlanType.S220BDS
                || planType == PlanType.S280BDS
                || planType == PlanType.S200BDS
                || planType == PlanType.S220ProBDS
                || planType == PlanType.S220ProSBDS
                || planType == PlanType.S220ProHBDS
                || planType == PlanType.S220_SD_BDS
                || planType == PlanType.S200_SD_BDS)) {
            builder.setContent(getResources().getString(R.string.string_calibration_completed));
        } else {
            builder.setContent(getResources().getString(R.string.string_please_restart_aerocraft));
        }
        builder.build().show();
    }

    private void sucFinish() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * 解决虚拟按键事件
     */
    private void solveNavigationBar() {
        XLogger.INSTANCE.getAPP().i("solveNavigationBar() isHasNavigationBar = " + DroneUtils.isHasNavigationBar);
        if (DroneUtils.isHasNavigationBar) {
            ToolManager.hideNavigationBar(getWindow());
        }
    }

    @Override
    public void onBackPressed() {
        if (mRectifyDroneHelper != null) {
            mRectifyDroneHelper.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
