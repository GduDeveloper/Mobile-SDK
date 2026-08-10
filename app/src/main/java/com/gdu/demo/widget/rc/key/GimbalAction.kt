package com.gdu.demo.widget.rc.key

import com.gdu.demo.R
import com.gdu.lib.util.core.ToastUtils
import com.gdu.msdk.device.component.interfaces.IRoute
import com.gdu.msdk.device.interfaces.IGduDroneDevice

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
object GimbalAction: IKeyAction {

    override fun doAction(actionId: Int) {
        if (!IGduDroneDevice.get.gimbal.isConnected()) {
            return // 云台都没连，直接返回
        }

        val routeIng = (IRoute.get.routeMissionStateInfo.value?.state?:-1).toInt() == 1
        val subRouteState = (IRoute.get.runningRouteMissionInfo.value?.taskState?:-1).toInt()
        if (routeIng && (subRouteState == 1 || subRouteState == 2)) {
            log("stop! route is exec, cannot use gimbal!!")
            ToastUtils.showShort(R.string.setting_rc_route_open_cannot_use_gimbal)
            return
        }

        when(actionId) {
            // 云台
            ICustomAction.KEY_GIMBAL_RECENTER -> { // 云台回中
                log("gimbalToCenter")
                IGduDroneDevice.get.gimbal.back2Center(null)
            }
            ICustomAction.KEY_GIMBAL_DOWN -> { // 云台朝下
                log("gimbalToDown")
                IGduDroneDevice.get.gimbal.gimbalAngle(-90, 1000, null)
            }
            ICustomAction.KEY_GIMBAL_CENTER_DOWN -> { // 云台回中朝下
                log("gimbalCenterOrDown")
                IGduDroneDevice.get.gimbal.gimbalAngle(-90, 0, null)
            }
            ICustomAction.KEY_GIMBAL_PITCH_ANGLE_RECENTER -> { // 云台俯仰回中
                log("gimbalPitchCenter")
                IGduDroneDevice.get.gimbal.gimbalAngle(0, 1000, null)
            }
            ICustomAction.KEY_GIMBAL_YAW_ANGLE_RECENTER -> { // 云台偏航回中
                log("gimbalCourseCenter")
                IGduDroneDevice.get.gimbal.gimbalAngle(1000, 0, null)
            }
        }
    }

}