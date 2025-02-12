
package com.gdu.demo.flight.calibration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.ToolManager;
import com.gdu.drone.PlanType;
import com.gdu.util.StatusBarUtils;
import com.gdu.util.logger.MyLogUtils;

/**
 * 指南针校磁界面
 */
public class RectifyMagnetometerActivity extends FragmentActivity {

    RectifyDroneHelper mRectifyDroneHelper;
    private View ll_rectify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rectify_magnetometer);
        findViews();
        initViews();
        initLisenter();
    }

    public void findViews() {
        ll_rectify = findViewById(R.id.ll_rectify);
        mRectifyDroneHelper = new RectifyDroneHelper(this, ll_rectify);
    }

    public void initViews() {
        solveNavigationBar();
    }

    public void initLisenter() {
        mRectifyDroneHelper.setOnCheckNorthListener(onCheckNorthListener);
    }

    private final RectifyDroneHelper.OnCheckNorthListener onCheckNorthListener = needShowRestart -> {
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
        if ((GlobalVariable.planType == PlanType.S220
                || GlobalVariable.planType == PlanType.S280
                || GlobalVariable.planType == PlanType.S200
                || GlobalVariable.planType == PlanType.S220Pro
                || GlobalVariable.planType == PlanType.S220ProS
                || GlobalVariable.planType == PlanType.S220ProH
                || GlobalVariable.planType == PlanType.S220_SD
                || GlobalVariable.planType == PlanType.S200_SD
                || GlobalVariable.planType == PlanType.S220BDS
                || GlobalVariable.planType == PlanType.S280BDS
                || GlobalVariable.planType == PlanType.S200BDS
                || GlobalVariable.planType == PlanType.S220ProBDS
                || GlobalVariable.planType == PlanType.S220ProSBDS
                || GlobalVariable.planType == PlanType.S220ProHBDS
                || GlobalVariable.planType == PlanType.S220_SD_BDS
                || GlobalVariable.planType == PlanType.S200_SD_BDS)) {
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
        MyLogUtils.d("solveNavigationBar() isHasNavigationBar = " + UavStaticVar.isHasNavigationBar);
        if (UavStaticVar.isHasNavigationBar) {
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
