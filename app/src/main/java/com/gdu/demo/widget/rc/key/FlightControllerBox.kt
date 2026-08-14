package com.gdu.demo.widget.rc.key

import androidx.annotation.IntDef
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.gdu.demo.R
import com.gdu.lib.util.core.ToastUtils
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.gdu.msdk.key.value.bean.LatLon
import com.gdu.sdk.sound.engine.GduSoundManager
import com.gdu.sdk.sound.engine.SoundConst

/**
 * 【业务聚合】飞控相关业务开箱即用
 *
 * @author: guoyang
 * @date: 2025/7/29
 */
object FlightControllerBox {

    /**
     * 设置返航点
     *
     * latLngType: 1：飞行器；2：遥控器; 3: 打点
     * pointType 返航点类型(0: 正常返航点；1: 失联返航点)
     */
    fun setHomePoint(latLng: LatLon, @ReturnHomeLatLngTypeDef latLngType: Int, pointType: Byte = 0) {
        if (!IGduDroneDevice.get.isConnected) {
            ToastUtils.showShort(R.string.aircraft_not_connect)
            return
        }
        if (latLng.latitude == 0.toDouble() || latLng.longitude == 0.toDouble()) {
            return
        }
        val droneLat = IFlightController.get.fcInfo1.value?.latitude?: 0.0
        val droneLng = IFlightController.get.fcInfo1.value?.longitude?: 0.0
        if (droneLat == 0.toDouble() || droneLng == 0.toDouble()) {
            return
        }

        val homeLatLng = LatLng(latLng.latitude, latLng.longitude)
        val currentLatLng = LatLng(droneLat, droneLng)
        val distance = AMapUtils.calculateLineDistance(homeLatLng, currentLatLng)

        // 返航点设置不能大于15000米
        if (distance > 15 * 1000) {
            ToastUtils.showShort(R.string.bizflight_backhome_latlng_with_aircraft_too_far)
            return
        }

        // 返航点设置不能大于限距距离
        val limitDistance = IFlightController.get.fcInfo1.value?.limitDistance?: -1
        if (limitDistance > 0 && distance > limitDistance) {
            ToastUtils.showShort(R.string.bizflight_backhome_latlng_without_limit_distance)
            return
        }

        IGduDroneDevice.get.flightController.setBackHomeLatLng(latLng, pointType) { result ->
            if (!result.success) {
                ToastUtils.showShort(R.string.home_point_set_failed)
                return@setBackHomeLatLng
            }
            stopAllHomePointMusic()
            playHomePointByReturnType(latLngType)
            ToastUtils.showShort(R.string.home_point_set_successfully)
        }
    }


    private fun stopAllHomePointMusic() {
        GduSoundManager.stopPlaySoundAndNotPlaySound(SoundConst.SET_CURRENT_HOME_POINT_PLANET)
        GduSoundManager.stopPlaySoundAndNotPlaySound(SoundConst.SET_CURRENT_HOME_POINT_CONTROL)
        GduSoundManager.stopPlaySoundAndNotPlaySound(SoundConst.SET_CURRENT_HOME_POINT_CURRENT_POINTER)
        GduSoundManager.stopPlaySoundAndNotPlaySound(SoundConst.MUSIC_HOME_UPDATE_SUCCESS)
    }

    private fun playHomePointByReturnType(@ReturnHomeLatLngTypeDef latLngType: Int) {
        //根据产品需求，设置了返航点类型需要提示对应语音 http://zentao.gdu-tech.com/zentao/bug-view-16555.html
        when(latLngType) {
            HOME_POINT_CONTROL -> {
                GduSoundManager.playSound(SoundConst.SET_CURRENT_HOME_POINT_CONTROL, 0, false)
            }
            HOME_POINT_POINTER -> {
                GduSoundManager.playSound(SoundConst.SET_CURRENT_HOME_POINT_CURRENT_POINTER, 0, false)
            }
            HOME_POINT_PLANET -> {
                GduSoundManager.playSound(SoundConst.SET_CURRENT_HOME_POINT_PLANET, 0, false)
            }
        }
    }

    /** 飞行器 */
    const val HOME_POINT_PLANET = 1
    /** 遥控器 */
    const val HOME_POINT_CONTROL = 2
    /** 打点 */
    const val HOME_POINT_POINTER = 3

    @IntDef(
        HOME_POINT_PLANET,
        HOME_POINT_CONTROL,
        HOME_POINT_POINTER
    )
    @Retention(AnnotationRetention.SOURCE)
    private annotation class ReturnHomeLatLngTypeDef

}