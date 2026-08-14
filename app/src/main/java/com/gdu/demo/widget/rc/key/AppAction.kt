package com.gdu.demo.widget.rc.key

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
object AppAction : IKeyAction{

    override fun doAction(actionId: Int) {
        when(actionId) {
            // App动作
            ICustomAction.KEY_APP_CHANGE_MAP -> { // 地图/图传画面切换
//                ThreadHelper.runOnUiThread {
//                    if (ScreenModeManager.instance.isMapScreen()) {
//                        log("change screen mode -> video")
//                        TheRouter.build("screen").withString("type", "video").action()
//                    } else {
//                        log("change screen mode -> map")
//                        TheRouter.build("screen").withString("type", "map").action()
//                    }
//                }
            }
            ICustomAction.KEY_APP_ADD_TARGET_POINT -> { // 新增目标点
//                addTargetPoint()
            }
            ICustomAction.KEY_APP_DELETE_SELECTED_POINT -> { // 删除选中目标点
//                deleteTargetPoint()
            }
            ICustomAction.KEY_APP_SELECTED_NEXT_POINT -> { // 选中下一个目标点
//                selectNextTargetPoint()
            }
            ICustomAction.KEY_APP_SELECTED_PREVIOUS_POINT -> { // 选中上一个目标点
//                selectPrevTargetPoint()
            }
            ICustomAction.KEY_APP_LOOK_FOR_TARGET -> { // 看向目标
//                lookAtTarget()
            }
        }
    }


    private fun addTargetPoint() {
//        if (!ScreenModeManager.instance.isMapScreen()) {
//            GduToastUtils.instance.showToast(R.string.setting_rc_custom_key_switch_map)
//            return
//        }
//
//        ThreadHelper.runOnUiThread {
//            getMapElementPApi()?.addTargetPointByMapCenter()
//        }
    }

    private fun deleteTargetPoint() {
//        if (!ScreenModeManager.instance.isMapScreen()) {
//            GduToastUtils.instance.showToast(R.string.setting_rc_custom_key_switch_map)
//            return
//        }
//
//        ThreadHelper.runOnUiThread {
//            val iMapPApi = getMapElementPApi()
//            val marker = iMapPApi?.getSelectTargetMarker()
//            if (marker == null) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_select_target_point)
//                return@runOnUiThread
//            }
//            iMapPApi.removeTargetMarker(marker)
//        }
    }

    private fun selectNextTargetPoint() {
//        if (!ScreenModeManager.instance.isMapScreen()) {
//            GduToastUtils.instance.showToast(R.string.setting_rc_custom_key_switch_map)
//            return
//        }
//        ThreadHelper.runOnUiThread {
//            if (getMapElementPApi()?.selectNextTargetMarker() != true) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_no_target_point)
//            }
//        }
    }

    private fun selectPrevTargetPoint() {
//        if (!ScreenModeManager.instance.isMapScreen()) {
//            GduToastUtils.instance.showToast(R.string.setting_rc_custom_key_switch_map)
//            return
//        }
//        ThreadHelper.runOnUiThread {
//            if (getMapElementPApi()?.selectPrevTargetMarker() != true) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_no_target_point)
//            }
//        }
    }

    private fun lookAtTarget() {
//        if (!ScreenModeManager.instance.isMapScreen()) {
//            GduToastUtils.instance.showToast(R.string.setting_rc_custom_key_switch_map)
//            return
//        }
//        ThreadHelper.runOnUiThread {
//            val iMapPApi = getMapElementPApi()
//            val marker = iMapPApi?.getSelectTargetMarker()
//            if (marker == null) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_lock_at_point)
//                return@runOnUiThread
//            }
//
//            val fcInfo1 = IFlightController.get.fcInfo1.value
//            if (fcInfo1?.droneFlyState != DroneFlyState.HOVER) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_flight_hover)
//                return@runOnUiThread
//            }
//            if (!fcInfo1.validateDroneLocation()) {
//                GduToastUtils.instance.showToast(R.string.setting_rc_tips_no_location)
//                return@runOnUiThread
//            }
//            val droneLocation = GduLatLng(fcInfo1.latitude, fcInfo1.longitude)
//            val markerLocation = marker.position
//
//            val angle = calculateAngle(droneLocation, markerLocation)
//            val radValue: Short = (angle * 100 * (Math.PI / 180)).toInt().toShort()
//            log("[lookAtTarget] angle = $angle, radValue = $radValue")
//
//            IFlightController.get.setDroneAngle(0, radValue) {
//                GduToastUtils.instance.showToast(if (it.success) R.string.setting_rc_tips_look_at_success else R.string.setting_rc_tips_look_at_fail)
//            }
//        }
    }

//    private fun getMapElementPApi(): IMapElementPApi? {
//        val activity = ActivityManager.getInstance().flightHomeActivity?: return null
//        return HiltRouter.router(activity, IFlightActivityScopePApi::class.java).mapElementPApi()
//    }
//
//    /**
//     * 计算两个坐标点角度
//     */
//    private fun calculateAngle(startPoint: GduLatLng, endPoint: GduLatLng): Float {
//        if (startPoint.spatialReference != endPoint.spatialReference) return 0f
//        val x = endPoint.longitude.toBigDecimal().subtract(startPoint.longitude.toBigDecimal()).toDouble()
//        val y = endPoint.latitude.toBigDecimal().subtract(startPoint.latitude.toBigDecimal()).toDouble()
//        val angle = atan2(x, y) * 180f / Math.PI
//        return angle.toFloat()
//    }

}