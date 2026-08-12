package com.gdu.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.gdu.common.error.Error
import com.gdu.demo.SdkDemoApplication
import com.gdu.drone.LocationCoordinate2D
import com.gdu.lib.util.core.ToastUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.enums.HeadingTypeEnum
import com.gdu.msdk.enums.OrbitHeadingEnum
import com.gdu.msdk.enums.OrbitPitchEnum
import com.gdu.msdk.enums.OrbitStartPointEnum
import com.gdu.msdk.key.value.common.EmptyMsg
import com.gdu.sdk.flightcontroller.FlightController
import com.gdu.sdk.flightcontroller.GPSFollowState
import com.gdu.sdk.flightcontroller.GPSSurroundState
import com.gdu.sdk.nest.NestController
import com.gdu.sdk.products.Aircraft
import com.gdu.sdk.util.CommonCallbacks
import com.gdu.sdk.util.CommonCallbacks.CompletionCallback
import kotlin.math.min
import kotlin.math.sqrt

/**
 * @author wuqb
 * @date 2026/8/7 20:22
 * @description 这里写描述
 */
class MissionOperatorViewModel : ViewModel() {

    private var mGDUFlightController: FlightController? = null
    private var mNestController: NestController? = null
    init {
        val product = SdkDemoApplication.getProductInstance()
        mGDUFlightController = (product as? Aircraft)?.flightController
        mNestController = (product as? Aircraft)?.nestController


        mGDUFlightController?.setGpsSurroundStateCallback(object : GPSSurroundState.Callback {
            override fun onUpdate(gpsSurroundState: GPSSurroundState?) {
                when (gpsSurroundState) {
                    GPSSurroundState.GO_TO_SURROUND_START, GPSSurroundState.SURROUNDING, GPSSurroundState.CONTINUE_SURROUND -> ToastUtils.showShort("环绕中")
                    GPSSurroundState.PAUSE_SURROUND -> ToastUtils.showShort("暂停环绕")
                    GPSSurroundState.COMPLETE -> ToastUtils.showShort("完成环绕")
                    else -> ToastUtils.showShort("停止环绕")
                }
            }
        })

        mGDUFlightController?.setGpsFollowStateCallback(object : GPSFollowState.Callback {
            override fun onUpdate(gpsFollowState: GPSFollowState?) {
                when (gpsFollowState) {
                    GPSFollowState.GO_TO_FOLLOW_START, GPSFollowState.FOLLOWING -> ToastUtils.showShort("跟随中")
                    GPSFollowState.COMPLETE -> ToastUtils.showShort("完成跟随")
                    else -> ToastUtils.showShort("停止跟随")
                }
            }
        })
    }

    fun startSurroundMission() {
        controlSurround(1.toByte(),object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("开始环绕发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("开始环绕发送失败");
            }
        })
        setSurroundHeadingType()
        setSurroundStartPoint()
    }

    fun pauseSurroundMission() {
        controlSurround(3.toByte(),object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("暂停环绕发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("暂停环绕发送失败");
            }
        })
    }

    fun resumeSurroundMission() {
        controlSurround(4.toByte(),object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("继续环绕发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("继续环绕发送失败");
            }
        })
    }

    fun stopSurroundMission() {
        controlSurround(2.toByte(),object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("停止环绕发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("停止环绕发送失败");
            }
        })
    }

    /**
     *  type : 1 开始环绕 2 停止环绕 3 暂停环绕 4 继续环绕
     */
    fun controlSurround(type: Byte,callback: CommonCallbacks.CompletionCallbackWith<EmptyMsg>?) {
        val point = LocationCoordinate2D(30.497323, 114.583995)
        XLogger.APP.d(" Surround control  type = " + type + ",lat = " + ", lng = " + ",speed = " + getSurroundSpeed())
        mGDUFlightController?.controlFlightSurround(
            type,
            point.latitude,
            point.longitude,
            getSurroundSpeed(),   //环绕角速度值范围 [-120,-5]U[5,120] 负值表示逆时针环绕，正直表示顺时针环绕（从上向下看）
            4000,  //环绕半径值范围 500-5000
            1.toShort(),
            1.toByte(),  //bit0:  0一直环绕   1执行圈数
            40,
            0,
            callback
        )
    }

    fun setSurroundHeadingType() {
        mGDUFlightController?.setSurroundHeadingType(
            OrbitHeadingEnum.AWAY_FROM_CIRCLE_CENTER,
            0,
            CompletionCallback { error: Error? ->
                if (error == null) {
                    ToastUtils.showShort("设置机头角度发送成功")
                } else {
                    ToastUtils.showShort("设置机头角度发送失败")
                }
            })
    }

    /**
     * 设置环绕起始点位置
     * */
    fun setSurroundStartPoint() {
        mGDUFlightController?.setSurroundStartPoint(OrbitStartPointEnum.NORTH, CompletionCallback { error: Error? ->
            if (error == null) {
                ToastUtils.showShort("设置环绕起始点位置发送成功")
            } else {
                ToastUtils.showShort("设置环绕起始点位置发送失败")
            }
        })
    }

    private fun getSurroundSpeed(): Byte {
        val maxSpeed = getMaxSurroundSpeed(40).toByte()
        val midSpeed = ((maxSpeed + 5) / 2).toByte()
        val speed = ((5 + midSpeed) / 2).toByte()
        return speed
    }

    private fun getMaxSurroundSpeed(radius: Int): Int {
        val max = sqrt(1960000 / (radius * 100.0f)).toInt()
        return min(max.toDouble(), 120.0).toInt()
    }


    /**
     * GPS跟随
     */

    private val ONE_METER_OFFSET: Double = 0.00000899322

    private var latitude = 0.0
    private var longitude = 0.0
    fun startFollow() {
        latitude = 30.497323
        longitude = 114.583995
        gpsFollow(true, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("开始跟随发送成功");
                var cnt = 0
                while (cnt < 100) {
                    latitude += 5 * ONE_METER_OFFSET
                    longitude += 5 * ONE_METER_OFFSET
                    mGDUFlightController?.updateGPSFollowPoint(
                        latitude, longitude,
                        object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                            override fun onSuccess(result: EmptyMsg?) {
                                ToastUtils.showShort("更新跟随点发送成功");
                            }

                            override fun onFailure(var1: Error?) {
                                ToastUtils.showShort("更新跟随点发送失败");
                            }
                        })
                    try {
                        Thread.sleep(1500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    cnt++
                }
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("开始跟随发送失败");
            }
        })
    }

    /**
     * 停止跟随
     * */

    fun stopFollow() {
        gpsFollow(false, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg?) {
                ToastUtils.showShort("停止跟随发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("停止跟随发送失败");
            }
        })
    }

    private fun gpsFollow(enable: Boolean, callback: CommonCallbacks.CompletionCallbackWith<EmptyMsg>?){
        mGDUFlightController?.controlFlightFollow(enable,
            30.497323,
            114.583995,
            5.0f,
            15,
            HeadingTypeEnum.POINT_TARGET,
            callback)
    }

    /**
     * 开启GPS差分跟随
     * */
    fun startGpsDifferentialFollow(isSetDistanceAndHeightEnable: Boolean) {
        val isSetHeightEnable = if (isSetDistanceAndHeightEnable) 1.toByte() else 0.toByte()
        val isSetDistanceEnable = if (isSetDistanceAndHeightEnable) 1.toByte() else 0.toByte()
        mNestController?.controlFlightFollow(true,
            30.497323,
            114.583995,
            HeadingTypeEnum.POINT_TARGET,
            isSetHeightEnable,
            3,
            0,
            isSetDistanceEnable,
            15,
            0,
            object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("开启GPS差分跟随发送成功");
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("开启GPS差分跟随发送失败");
                }
            })
    }

    /**
     * 关闭GPS差分跟随
     * */
    fun stopGpsDifferentialFollow() {
        mNestController?.controlFlightFollow(false,
            0.0,
            0.0,
            HeadingTypeEnum.POINT_TARGET,
            0.toByte(),
            0,
            0,
            0.toByte(),
            0,
            0,
            object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("关闭GPS差分跟随发送成功");
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("关闭GPS差分跟随发送失败");
                }
            })
    }

    /**
     * 设置GPS跟随机头朝向
     * */
    fun setFollowHeadingType() {
        mNestController?.setGimbalTargetHeadType(2.toByte(), 0, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(var1: EmptyMsg?) {
                ToastUtils.showShort("设置GPS跟随机头朝向发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("设置GPS跟随机头朝向发送失败");
            }
        })
    }

    /**
     * 设置GPS跟随云台角度
     * */
    fun setFollowGimbalAngle() {
        mGDUFlightController?.setGimbalFollowAngle(OrbitPitchEnum.GIMBAL, 0.toShort(), object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
            override fun onSuccess(var1: EmptyMsg?) {
                ToastUtils.showShort("设置GPS跟随云台角度发送成功");
            }

            override fun onFailure(var1: Error?) {
                ToastUtils.showShort("设置GPS跟随云台角度发送失败");
            }
        })
    }
}