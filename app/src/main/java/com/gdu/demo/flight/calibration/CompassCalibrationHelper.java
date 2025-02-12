package com.gdu.demo.flight.calibration;

import android.content.Context;
import android.content.Intent;

import com.gdu.config.GlobalVariable;
import com.gdu.util.DroneUtil;
import com.gdu.util.logger.MyLogUtils;

/**
 * @author wuqb
 * @date 2025/2/11
 * @description 指南针校磁帮助类
 */
public class CompassCalibrationHelper {
    public static void jumpMagnetometerActivity(Context context) {
        // 飞机解锁或不在地面上都不能进行校磁
        MyLogUtils.i("jumpMagnetometerActivity() planeHadLock = " + GlobalVariable.planeHadLock
                + "; droneFlyState = " + GlobalVariable.droneFlyState);
        if (!GlobalVariable.planeHadLock || GlobalVariable.droneFlyState != 1) {
            return;
        }
        Intent intent = getRectifyMagnetomterIntent(context);
        context.startActivity(intent);
    }

    public static Intent getRectifyMagnetomterIntent(Context context) {
        Intent intent;
        if (DroneUtil.isSmallFlight()) {
            intent = new Intent(context,
                    RectifyMagnetometerActivityNew.class);
        } else {
            intent = new Intent(context,
                    RectifyMagnetometerActivity.class);
        }
        return intent;
    }
}
