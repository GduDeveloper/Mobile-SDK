package com.gdu.demo.widget.rc.key

import com.gdu.demo.R
import com.gdu.lib.util.ActivityManager
import com.gdu.lib.util.core.ToastUtils
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.interfaces.IGduDroneDevice

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
object FCAction: IKeyAction {

    // 飞控动作
    override fun doAction(actionId: Int) {
        when(actionId) {
            ICustomAction.KEY_FC_SWITCH_NIGHT_LIGHT -> { // 夜航灯开启/关闭
                if (!IGduDroneDevice.get.isConnected) {
                    ToastUtils.showShort(R.string.aircraft_not_connect)
                    return
                }
                log("changeNightlight")
                IFlightController.get.changeNightLightStatus(null)
            }

            ICustomAction.KEY_FC_UPDATE_HOME_AIR -> { // 更新HOME点（当前飞行器位置）

                val droneLat = IFlightController.get.fcInfo1.value?.latitude?: 0.0
                val droneLng = IFlightController.get.fcInfo1.value?.longitude?: 0.0
                log("UPDATE_HOME_AIR, droneLat = $droneLat, droneLng = $droneLng")
//                FlightControllerBox.setHomePoint(LatLon(droneLat, droneLng), FlightControllerBox.HOME_POINT_PLANET)
            }

            ICustomAction.KEY_FC_UPDATE_HOME_RC -> { // 更新HOME点（当前遥控器位置）
                ActivityManager.getInstance().flightHomeActivity?.let {
//                    val viewModel = ViewModelProvider(it)[FlightMapWidgetViewModel::class.java]
//                    log("UPDATE_HOME_RC, viewModel = $viewModel, activity = $it")
//                    it.lifecycleScope.launch {
//                        val latLng = viewModel.controllerLocationFlow.firstOrNull()
//                        log("UPDATE_HOME_RC, latitude = ${latLng?.latitude}, longitude = ${latLng?.longitude}")
//                        if (latLng == null) {
//                            return@launch
//                        }
//                        FlightControllerBox.setHomePoint(LatLon(latLng.latitude, latLng.longitude), FlightControllerBox.HOME_POINT_CONTROL)
//                    }
                }
            }
        }
    }


}