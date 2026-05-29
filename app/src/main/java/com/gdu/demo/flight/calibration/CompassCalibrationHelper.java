package com.gdu.demo.flight.calibration;

import android.content.Context;
import android.content.Intent;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;

/**
 * @author wuqb
 * @date 2025/2/11
 * @description 指南针校磁帮助类
 */
public class CompassCalibrationHelper {
    public static void jumpMagnetometerActivity(Context context) {
        // 飞机解锁或不在地面上都不能进行校磁
        XLogger.INSTANCE.getAPP().i("jumpMagnetometerActivity() planeHadLock = " + DroneUtils.getPlaneHadLock()
                + "; droneFlyState = " + DroneUtils.getDroneFlyState());
        if (!DroneUtils.getPlaneHadLock() || !DroneUtils.isGround()) {
            return;
        }
        Intent intent = getRectifyMagnetomterIntent(context);
        context.startActivity(intent);
    }

    public static Intent getRectifyMagnetomterIntent(Context context) {
        Intent intent;
        if (IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            intent = new Intent(context,
                    RectifyMagnetometerActivityNew.class);
        } else {
            intent = new Intent(context,
                    RectifyMagnetometerActivity.class);
        }
        return intent;
    }
}
