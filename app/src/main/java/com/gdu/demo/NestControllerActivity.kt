package com.gdu.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.gdu.common.error.Error
import com.gdu.drone.LocationCoordinate3D
import com.gdu.lib.util.GsonUtils
import com.gdu.lib.util.core.ToastUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.key.callback.MSdkCallback
import com.gdu.msdk.key.error.MError
import com.gdu.msdk.key.value.ParamCarNestFlightFollow
import com.gdu.msdk.key.value.ParamPointBean
import com.gdu.msdk.key.value.RespCarNestBleVersion
import com.gdu.msdk.key.value.RespCarNestGetGimbalTargetType
import com.gdu.msdk.key.value.RespCarNestGetImgBootTransVersion
import com.gdu.msdk.key.value.RespCarNestGetImgTransVersion
import com.gdu.msdk.key.value.RespCarNestGetNestSn
import com.gdu.msdk.key.value.RespCarNestGetSensorMcuAppVersion
import com.gdu.msdk.key.value.RespCarNestGetSensorMcuBootVersion
import com.gdu.msdk.key.value.RespCarNestGetSocSysVersion
import com.gdu.msdk.key.value.RespCarNestPowerOnOrOff
import com.gdu.msdk.key.value.RespCarNestProgramVersion
import com.gdu.msdk.key.value.RespCarNestSmartControl
import com.gdu.msdk.key.value.TakeOffResultBean
import com.gdu.msdk.key.value.common.EmptyMsg
import com.gdu.msdk.key.value.nest.ParamConfigParam
import com.gdu.msdk.key.value.nest.ParamNestWifi
import com.gdu.msdk.key.value.nest.ParamSetupServiceConfig
import com.gdu.msdk.key.value.ota.AckNestAllVersionInfo
import com.gdu.rtk.PositioningSolution
import com.gdu.sdk.flightcontroller.FlightController
import com.gdu.sdk.nest.NestController
import com.gdu.sdk.simulator.InitializationData
import com.gdu.sdk.sound.engine.GduSoundManager
import com.gdu.sdk.sound.engine.SoundConst
import com.gdu.sdk.util.CommonCallbacks
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 机库控制管理界面
 */
class NestControllerActivity : Activity(), View.OnClickListener {
    private var mContext: Context? = null
    private var mNestController: NestController? = null
    private var mGDUFlightController: FlightController? = null

    // 布局容器
    private var mPropertyLayout: LinearLayout? = null
    private var mContentLayout: FlexboxLayout? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_nest_controller)


        // 初始化布局容器
        mPropertyLayout = findViewById(R.id.nest_content_observable_property)
        mContentLayout = findViewById(R.id.nest_content_function)

        // 获取CarNestController实例（假设从应用中获取，实际实现可能不同）
        mNestController = SdkDemoApplication.getAircraftInstance().nestController
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().flightController

        initStaticProperties()
        initObservableProperties()
        initControlButtons()
        initDebugControlButtons()
    }

    /**
     * 初始化非Observable属性（一次性获取）
     */
    private fun initStaticProperties() {

    }

// ... 现有代码 ...
    /**
     * 初始化Observable属性（监听方式）
     */
    private fun initObservableProperties() {
        addSectionTitle("实时状态", mPropertyLayout)


        // nestInDroneBatteryInfo
        val nestInDroneBatteryInfoView =
            addObservablePropertyView("机库内无人机电池信息：", "等待更新...")
        // {{ 修改1：将setCallback替换为addObserver }}
        mNestController?.nestInDroneBatteryInfo?.register {
            updateTextView(nestInDroneBatteryInfoView, GsonUtils.toJson(it))
        }

        // nestInStandbyBattInfo
        val nestInStandbyBattInfoView =
            addObservablePropertyView("机库待机电池信息：", "等待更新...")
        // {{ 修改2：将setCallback替换为addObserver }}
        mNestController?.nestInStandbyBattInfo?.register {
            updateTextView(nestInStandbyBattInfoView, GsonUtils.toJson(it))
        }


        // nestState
        val nestStateView = addObservablePropertyView("机库状态：", "等待更新...")
        // {{ 修改3：将setCallback替换为addObserver }}
        mNestController?.nestState?.register {
            updateTextView(nestStateView, GsonUtils.toJson(it))
        }


        // notifyCarNestLocationChanged
        val locationChangedView = addObservablePropertyView("机库位置变化：", "等待更新...")
        // {{ 修改4：将setCallback替换为addObserver }}
        mNestController?.notifyCarNestLocationChanged?.register {
            updateTextView(locationChangedView, GsonUtils.toJson(it))
        }


        // notifyCarNestWindSpeedChanged
        val windSpeedChangedView = addObservablePropertyView("风速变化：", "等待更新...")
        // {{ 修改5：将setCallback替换为addObserver }}
        mNestController?.notifyCarNestWindSpeedChanged?.register {
            updateTextView(windSpeedChangedView, GsonUtils.toJson(it))
        }


        // notifyLowPowerState
        val lowPowerStateView = addObservablePropertyView("低电量状态：", "等待更新...")
        // {{ 修改6：将setCallback替换为addObserver }}
        mNestController?.notifyLowPowerState?.register {
            updateTextView(lowPowerStateView, GsonUtils.toJson(it))
        }


        // notifyNestDoorStateChanged
        val doorStateChangedView = addObservablePropertyView("机库舱门状态变化：", "等待更新...")
        // {{ 修改7：将setCallback替换为addObserver }}
        mNestController?.notifyNestDoorStateChanged?.register {
            updateTextView(doorStateChangedView, GsonUtils.toJson(it))
        }


        // notifyNestDroneInPlace
        val droneInPlaceView = addObservablePropertyView("无人机在位状态", "等待更新...")
        // {{ 修改8：将setCallback替换为addObserver }}
        mNestController?.notifyNestDroneInPlace?.register {
            updateTextView(droneInPlaceView, GsonUtils.toJson(it))
        }


        // notifyNestDronePower
        val dronePowerView = addObservablePropertyView("无人机电源状态", "等待更新...")
        // {{ 修改9：将setCallback替换为addObserver }}
        mNestController?.notifyNestDronePower?.register {
            updateTextView(dronePowerView, GsonUtils.toJson(it))
        }


        // notifyNestRightsState
        val rightsStateView = addObservablePropertyView("机库控制权状态", "等待更新...")
        // {{ 修改10：将setCallback替换为addObserver }}
        mNestController?.notifyNestRightsState?.register {
            updateTextView(rightsStateView, GsonUtils.toJson(it))
        }


        // notifyNestWorkMode
        val workModeView = addObservablePropertyView("机库工作模式", "等待更新...")
        // {{ 修改11：将setCallback替换为addObserver }}
        mNestController?.notifyNestWorkMode?.register {
            updateTextView(workModeView, GsonUtils.toJson(it))
        }


        // notifyOwnerNestControlFollow
        val controlFollowView = addObservablePropertyView("主人机库控制跟随", "等待更新...")
        // {{ 修改12：将setCallback替换为addObserver }}
        mNestController?.notifyOwnerNestControlFollow?.register {
            updateTextView(controlFollowView, GsonUtils.toJson(it))
        }


        // onboardCompTaskState
        val compTaskStateView = addObservablePropertyView("机载组件任务状态", "等待更新...")
        // {{ 修改13：将setCallback替换为addObserver }}
        mNestController?.onboardCompTaskState?.register {
            updateTextView(compTaskStateView, GsonUtils.toJson(it))
        }


        // outNestState
        val outNestStateView = addObservablePropertyView("出巢状态", "等待更新...")
        // {{ 修改14：将setCallback替换为addObserver }}
        mNestController?.outNestState?.register {
            updateTextView(outNestStateView, GsonUtils.toJson(it))
        }


        // periMcuInfo
        val periMcuInfoView = addObservablePropertyView("外围MCU信息", "等待更新...")
        // {{ 修改15：将setCallback替换为addObserver }}
        mNestController?.periMcuInfo?.register {
            updateTextView(periMcuInfoView, GsonUtils.toJson(it))
        }


        // sensorMcuInfo
        val sensorMcuInfoView = addObservablePropertyView("传感器MCU信息", "等待更新...")
        // {{ 修改16：将setCallback替换为addObserver（注意安全调用） }}
        mNestController?.sensorMcuInfo?.register {
            updateTextView(sensorMcuInfoView, GsonUtils.toJson(it))
        }
    }

    /**
     * 初始化控制按钮
     */


    private fun initControlButtons() {
        addSectionTitle("控制操作", mContentLayout)

        addSectionTitle("机库控制", mContentLayout)
        // 1. 一键开仓
        addButton("一键开仓") {
            mNestController?.nestActionOperation(24, 0, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("一键开仓成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("一键开仓失败")
                }
            })
        }
        // 2. 一键关仓
        addButton("一键关仓") {
            mNestController?.nestActionOperation(25, 0, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("一键关仓成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("一键关仓失败")
                }
            })
        }


        // 3. 机库无人机充电
        addButton("机库无人机充电") {
            mNestController?.smartControl(9, object : CommonCallbacks.CompletionCallbackWith<RespCarNestSmartControl> {
                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("机库无人机充电失败")
                }

                override fun onSuccess(result: RespCarNestSmartControl?) {
                    ToastUtils.showShort("机库无人机充电成功")
                }
            })
        }
        // 4. 机库无人机停止充电
        addButton("机库无人机停止充电") {
            mNestController?.smartControl(10, object : CommonCallbacks.CompletionCallbackWith<RespCarNestSmartControl> {
                override fun onSuccess(result: RespCarNestSmartControl?) {
                    ToastUtils.showShort("机库无人机停止充电成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("机库无人机停止充电失败")
                }
            })
        }

        // 5. 机库无人机唤醒
        addButton("无人机唤醒") {
            mNestController?.nestActionOperation(0x12, 0, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("无人机唤醒成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("无人机唤醒失败")
                }
            })
        }
        // 6. 机库无人机对频
        addButton("机库无人机对频") {
            mNestController?.nestActionOperation(22, 1, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("机库无人机对频成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("机库无人机对频失败")
                }
            })
        }
        // 7. 防盗模式
        addButton("防盗模式-开") {
            mNestController?.nestModelControl(
                0x02,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("防盗模式开设置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("防盗模式开设置失败")
                    }
                })
        }
        addButton("防盗模式-关") {
            mNestController?.nestModelControl(
                0x03,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("防盗模式关设置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("防盗模式关设置失败")
                    }
                })
        }







        // 8. 飞机电池开关机
        addButton("飞机关机") {
            mNestController?.droneBatteryPowerOnOff(
                0x00,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("飞机关机成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("飞机关机失败")
                    }
                })
        }
        addButton("飞机开机") {
            mNestController?.droneBatteryPowerOnOff(
                0x01,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("飞机开机成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("飞机开机失败")
                    }
                })
        }

        // 9. 飞机低功耗控制
        addButton("关闭无人机低功耗") {
            mNestController?.droneLowPowerControl(
                0x00,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("关闭无人机低功耗成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("关闭无人机低功耗失败")
                    }
                })
        }
        addButton("进入无人机低功耗") {
            mNestController?.droneLowPowerControl(
                0x01,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("进入无人机低功耗成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("进入无人机低功耗失败")
                    }
                })
        }
        // 18. 机库休眠
        addButton("机库休眠") {
            mNestController?.nestBatteryPowerOff(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestPowerOnOrOff> {
                override fun onSuccess(result: RespCarNestPowerOnOrOff?) {
                    ToastUtils.showShort("机库休眠成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("机库休眠失败")
                }
            })
        }

        // 27. WiFi STA开关
        addButton("开启WiFi STA") {
            mNestController?.openWifiSta(true, object : CommonCallbacks.CompletionCallbackWith<Error?> {
                override fun onSuccess(result: Error?) {
                    ToastUtils.showShort("openWifiSta isOpen = true; result = $result")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("openWifiSta isOpen = true; result = $var1")
                }
            })
        }
        addButton("关闭WiFi STA") {
            mNestController?.openWifiSta(false, object : CommonCallbacks.CompletionCallbackWith<Error?> {
                override fun onSuccess(result: Error?) {
                    ToastUtils.showShort("openWifiSta isClose = false; result = $result")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("openWifiSta isClose = false; result = $var1")
                }
            })
        }

        addButton("无人机模拟飞行") {
            val simulator = mGDUFlightController?.simulator
            if (simulator?.isSimulatorActive() == true) {
                simulator.stop(null)
                ToastUtils.showShort("已开启无人机模拟飞行")
            } else {
                val initializationData = InitializationData(
                    LocationCoordinate3D(
                        30.499853, 114.578548, 10f
                    ),
                    90.toShort(),
                    PositioningSolution.FIXED_POINT,
                    30.toByte()
                )
                simulator?.start(initializationData, object : MSdkCallback.ActionCallback<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("无人机模拟飞行开启成功")
                    }

                    override fun onFailure(error: MError) {
                        ToastUtils.showShort("无人机模拟飞行开启失败")
                    }
                })
            }
        }

        addButton("机库模拟飞行") {
            val simulator = mNestController?.simulator
            if (simulator?.isSimulatorActive() == true) {
                ToastUtils.showShort("已开启机库模拟飞行")
                simulator.stop(null)
            } else {
                val locationCoordinate3D = LocationCoordinate3D(30.499853, 114.578548, 10f)
                val initializationData = InitializationData(
                    locationCoordinate3D,
                    90.toShort(),
                    PositioningSolution.FIXED_POINT,
                    30.toByte()
                )
                XLogger.APP.i("NestSimulator", "start simulator")
                simulator?.start(initializationData, object : MSdkCallback.ActionCallback<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("机库模拟飞行开启成功")
                    }
                    override fun onFailure(error: MError) {
                        ToastUtils.showShort("机库模拟飞行开启失败")
                    }
                })
            }
        }


        addSectionTitle("版本信息", mContentLayout)
        // 5. 请求MCU程序版本
        addButton("获取MCU程序版本") {
            mNestController?.reqMcuProgramVersion(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestProgramVersion> {
                override fun onSuccess(result: RespCarNestProgramVersion?) {
                    ToastUtils.showShort("MCU版本: ${result?.nestOutMcuVersion}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取MCU版本失败")
                }
            })
        }
        // 10. 获取蓝牙版本
        addButton("获取蓝牙版本") {
            mNestController?.getBleVersion(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestBleVersion> {
                override fun onSuccess(result: RespCarNestBleVersion?) {
                    ToastUtils.showShort("蓝牙版本: ${result?.nestBleVersion}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取蓝牙版本失败")
                }
            })
        }

        // 14. 获取机库SN码
        addButton("获取机库SN") {
            mNestController?.getNestSnCode(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestGetNestSn> {
                override fun onSuccess(result: RespCarNestGetNestSn?) {
                    ToastUtils.showShort("SN: ${result?.carNestSn}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取SN失败")
                }
            })
        }
        // 17. 获取综控组件版本
        addButton("获取综控版本") {
            mNestController?.getSocSysVersion(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestGetSocSysVersion> {
                override fun onSuccess(result: RespCarNestGetSocSysVersion?) {
                    ToastUtils.showShort("综控版本: ${result?.nestSocSysVersion}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取综控版本失败")

                }
            })
        }
        // 19-22. 获取各个版本
        listOf(
            "图传版本" to {
                mNestController?.getImgTransVersion(object :
                    CommonCallbacks.CompletionCallbackWith<RespCarNestGetImgTransVersion> {
                    override fun onSuccess(result: RespCarNestGetImgTransVersion?) {
                        ToastUtils.showShort("图传版本: ${result?.nestImgTransVersion}")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("获取图传版本失败")
                    }
                })
            },
            "图传系统版本" to {
                mNestController?.getImgBootTransVersion(object :
                    CommonCallbacks.CompletionCallbackWith<RespCarNestGetImgBootTransVersion> {
                    override fun onSuccess(result: RespCarNestGetImgBootTransVersion?) {
                        ToastUtils.showShort("图传系统版本: ${result?.nestImgBootTransVersion}")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("获取图传系统版本失败")
                    }
                })
            },
            "传感器MCU APP版本" to {
                mNestController?.getSensorMcuAppVersion(object :
                    CommonCallbacks.CompletionCallbackWith<RespCarNestGetSensorMcuAppVersion> {
                    override fun onSuccess(result: RespCarNestGetSensorMcuAppVersion?) {
                        ToastUtils.showShort("传感器MCU APP版本: ${result?.sensorMcuAppVersion}")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("获取传感器MCU APP版本失败")
                    }
                })
            },
            "传感器MCU Boot版本" to {
                mNestController?.getSensorMcuBootVersion(object :
                    CommonCallbacks.CompletionCallbackWith<RespCarNestGetSensorMcuBootVersion> {
                    override fun onSuccess(result: RespCarNestGetSensorMcuBootVersion?) {
                        ToastUtils.showShort("传感器MCU Boot版本: ${result?.sensorMcuBootVersion}")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("获取传感器MCU Boot版本失败")
                    }
                })
            }
        ).forEach { (name, action) ->
            addButton("获取$name") { action.invoke() }
        }

        // 23. 获取机库版本（综合）
        addButton("获取机库版本") {
            mNestController?.getCarNestVersion(object :
                CommonCallbacks.CompletionCallbackWith<AckNestAllVersionInfo> {
                override fun onSuccess(result: AckNestAllVersionInfo?) {
                    ToastUtils.showLong("机库版本: ${result?.carNestVersion}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取机库版本失败")
                }
            })
        }



        addSectionTitle("起飞任务", mContentLayout)




        // 16. 一键起飞（三种模式，其他参数使用默认值）
        addButton("一键起飞") {
            mNestController?.nestOneKeyFly(
                (20 * 100).toShort(),  // height
                100,   // speed
                0,
                0.0,  // lng 示例
                0.0,   // lat 示例
                0, 0, 0,
                object : CommonCallbacks.CompletionCallbackWith<TakeOffResultBean> {
                    override fun onSuccess(result: TakeOffResultBean?) {
                        ToastUtils.showShort("起飞成功: $result")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("起飞失败")
                    }
                }
            )
        }
        addButton("一键降落") {
            mGDUFlightController?.exactBack(true,0,0,0) { var1 ->
                if (var1 == null) {
                    ToastUtils.showShort("降落成功")
                } else {
                    ToastUtils.showShort("降落失败: ${var1.msg}")
                }
            }
        }
        addButton("一键出库") {
            mGDUFlightController?.fcInfo1?.let { it ->
                //20260428 根据@志来要求，增加无人机在空中时执行一键出库（降落到指定位置）需求，区分于在舱内的一键出库
                val isGround = mGDUFlightController?.fcInfo1?.droneFlyState?.isGround() == true
                if (!isGround) {
                    //舱门是否开启  根据bug59365 增加仓门未关闭，禁止空中出库的语音提示
                    val isOpenDoor = mNestController?.notifyNestDoorStateChanged?.value == 2.toByte()
                    if (isOpenDoor) {
                        GduSoundManager.playSound(SoundConst.CAR_NEST_OUT_DOOR_NOT_CLOSE, 0, false)
                        return@addButton
                    }
                    mGDUFlightController?.carNestExactBack(true, (8 * 100).toShort(), 0, 0, 1, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                        override fun onSuccess(result: EmptyMsg?) {
                            ToastUtils.showShort("一键出库成功")
                        }

                        override fun onFailure(error: Error) {
                            ToastUtils.showShort("一键出库失败: ${error.msg}")
                        }
                    })
                } else {
                    var angle = (it.planeAngle / 100.0)
                    angle %= 360
                    val landPoint = calculateNewPosition(it.latitude, it.longitude, angle, 8.toDouble())
                    val landLat = landPoint[0]
                    val landLng = landPoint[1]
                    mNestController?.nestOneKeyFly((20 * 100).toShort(), 100.toShort(), 1.toByte(), landLng, landLat, 8 * 100, 0, 0,
                        callback = object : CommonCallbacks.CompletionCallbackWith<TakeOffResultBean> {
                            override fun onSuccess(result: TakeOffResultBean?) {
                                result?.let {
                                    if (it.code != 0) {
                                        ToastUtils.showShort("一键出库失败")
                                    } else {
                                        ToastUtils.showShort("一键出库成功")
                                    }
                                }
                            }

                            override fun onFailure(var1: Error?) {
                                ToastUtils.showShort("一键出库失败")

                            }
                        })
                }
            }
        }
        addButton("一键入库") {
            mNestController?.nestOneKeyFly((20 * 100).toShort(), 100.toShort(), 2.toByte(), 0.0, 0.0,
                callback = object :CommonCallbacks.CompletionCallbackWith<TakeOffResultBean>{
                    override fun onSuccess(result: TakeOffResultBean?) {
                        result?.let {
                            if (it.code != 0) {
                                ToastUtils.showShort("一键入库失败")
                            } else {
                                ToastUtils.showShort("一键入库成功")
                            }
                        }
                    }


                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("一键入库失败")
                    }
                })
        }



        // 28. 云台朝向设置
        addButton("云台-机头朝向目标点") {
            mNestController?.setGimbalTargetHeadType(
                0,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("机头朝向目标点成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("机头朝向目标点失败")
                    }
                })
        }
        addButton("云台-飞机跟随机库朝向") {
            mNestController?.setGimbalTargetHeadType(
                2,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("飞机跟随机库朝向成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("飞机跟随机库朝向失败")
                    }
                })
        }

        // 29. 获取云台朝向类型
        addButton("获取云台朝向类型") {
            mNestController?.getGimbalTargetHeadType(object :
                CommonCallbacks.CompletionCallbackWith<RespCarNestGetGimbalTargetType> {
                override fun onSuccess(result: RespCarNestGetGimbalTargetType?) {
                    ToastUtils.showShort("朝向类型: ${result?.gimbalTargetType}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取朝向类型失败")
                }
            })
        }

        // 30. 伴飞控制（仅开启/关闭，其他参数默认）
        addButton("开启伴飞-前方探路") {
            mNestController?.sensorMcuInfo?.value?.let {
                mNestController?.controlFlightFollow(
                    true,
                    it.fromBleNestLat,
                    it.fromBleNestLng,
                    0,
                    1,
                    100,
                    100,
                    1,
                    60,
                    1,
                    object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                        override fun onSuccess(result: EmptyMsg?) {
                            ToastUtils.showShort("开启伴飞成功")
                        }

                        override fun onFailure(var1: Error?) {
                            ToastUtils.showShort("开启伴飞失败")
                        }
                    })
            }
        }
        addButton("关闭伴飞") {
            mNestController?.controlFlightFollow(
                false,
                0.0, 0.0, 0, 0, 0, 0, 0, 0, 0,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("关闭伴飞成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("关闭伴飞失败")
                    }
                }
            )
        }

        addButton("指点飞行") {
            val list = arrayListOf<ParamPointBean>()
            mGDUFlightController?.fcInfo1?.let {
                list.add(ParamPointBean(0, it.longitude, it.latitude, 2, 60))
                list.add(ParamPointBean(0, it.longitude + 0.0009, it.latitude, 2, 80))
            }
            mGDUFlightController?.startMultiplePointFly(list, 15, 5, 5, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("指点飞行成功")
                }
                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("指点飞行失败")
                }
            })
        }

        addButton("环绕飞行/定点环绕-开始") {
            mNestController?.sensorMcuInfo?.value?.let {
                mGDUFlightController?.controlFlightSurround(
                    1.toByte(),
                    it.fromBleNestLat,
                    it.fromBleNestLng,
                    getSurroundSpeed(),
                    40.toShort(),
                    0.toShort(),
                    0.toByte(),
                    40.toShort(),
                    0,
                    object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                        override fun onSuccess(result: EmptyMsg?) {
                            ToastUtils.showShort("环绕飞行成功")
                        }

                        override fun onFailure(var1: Error?) {
                            ToastUtils.showShort("环绕飞行失败")
                        }
                    })
            }
        }
        addButton("环绕飞行/定点环绕-结束") {
            mNestController?.sensorMcuInfo?.value?.let {
                mGDUFlightController?.controlFlightSurround(
                    2.toByte(),
                    it.fromBleNestLat,
                    it.fromBleNestLng,
                    getSurroundSpeed(),
                    40.toShort(),
                    0.toShort(),
                    0.toByte(),
                    40.toShort(),
                    0,
                    object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                        override fun onSuccess(result: EmptyMsg?) {
                            ToastUtils.showShort("环绕飞行成功")
                        }

                        override fun onFailure(var1: Error?) {
                            ToastUtils.showShort("环绕飞行失败")
                        }
                    })
            }
        }

        // 31. 获取跟随信息
        addButton("获取跟随信息") {
            mNestController?.getFollowInfo(object :
                CommonCallbacks.CompletionCallbackWith<ParamCarNestFlightFollow> {
                override fun onSuccess(result: ParamCarNestFlightFollow?) {
                    ToastUtils.showShort("跟随信息: ${GsonUtils.toJson(result)}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取跟随信息失败")
                }
            })
        }

        // 34. 请求机库保存配置（默认高度距离）
        addButton("保存机库配置(10,5)") {
            mNestController?.requestSetConfigByNest(
                10,
                5,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("保存配置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("保存配置失败")
                    }
                })
        }

        // 35. 获取机库配置
        addButton("获取机库配置") {
            mNestController?.getConfigParamByNest(object :
                CommonCallbacks.CompletionCallbackWith<ParamConfigParam> {
                override fun onSuccess(result: ParamConfigParam?) {
                    ToastUtils.showShort("配置: ${GsonUtils.toJson(result)}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取配置失败")
                }
            })
        }

        // 36. 设置飞机低功耗模式（0关闭/1开启）
        addButton("是否进入飞机低功耗-关闭") {
            mNestController?.setLowPowerMode(0, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("关闭低功耗成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("关闭低功耗失败")
                }
            })
        }
        addButton("是否进入飞机低功耗-开启") {
            mNestController?.setLowPowerMode(1, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("开启低功耗成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("开启低功耗失败")
                }
            })
        }



        // 38. 设置机库WiFi密码（空密码示例）
        addButton("设置WiFi密码") {
            mNestController?.setNestWifiPwd(
                "12345678".toByteArray(),
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("设置WiFi密码成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("设置WiFi密码失败")
                    }
                })
        }

        // 39. 获取机库WiFi密码
        addButton("获取WiFi密码") {
            mNestController?.getNestWifiPwd(object : CommonCallbacks.CompletionCallbackWith<ParamNestWifi> {
                override fun onSuccess(result: ParamNestWifi?) {
                    ToastUtils.showShort("WiFi密码: ${String(result?.nestPwd ?: byteArrayOf())}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取WiFi密码失败")
                }
            })
        }

        // 40. 设置机库服务配置（空字符串）
        addButton("设置服务配置") {
            mNestController?.setNestSetupServiceConfig(
                "https://uver5.com",
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("设置服务配置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("设置服务配置失败")
                    }
                })
        }

        // 41. 获取机库服务配置
        addButton("获取服务配置") {
            mNestController?.getNestSetupServiceConfig(object :
                CommonCallbacks.CompletionCallbackWith<ParamSetupServiceConfig> {
                override fun onSuccess(result: ParamSetupServiceConfig?) {
                    ToastUtils.showShort("服务配置: ${GsonUtils.toJson(result)}")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("获取服务配置失败")
                }
            })
        }

//            // 42. 设置上云API配置（开启/关闭，其他默认）
//            addButton("开启上云API(默认地址)") {
//                mCarNestController?.setNestCloudApiConfig(true, "http://test.com", "admin", "123", object : MSdkCallback.ActionCallback<EmptyMsg> {
//                    override fun onSuccess(result: EmptyMsg?) {
//                        ToastUtils.showShort("开启上云API成功")
//                    }
//                    override fun onFailure(error: MError) {
//                        ToastUtils.showShort("开启上云API失败")
//                    }
//                })
//            }
//            addButton("关闭上云API") {
//                mCarNestController?.setNestCloudApiConfig(false, "", "", "", object : MSdkCallback.ActionCallback<EmptyMsg> {
//                    override fun onSuccess(result: EmptyMsg?) {
//                        ToastUtils.showShort("关闭上云API成功")
//                    }
//                    override fun onFailure(error: MError) {
//                        ToastUtils.showShort("关闭上云API失败")
//                    }
//                })
//            }
//
//            // 43. 获取上云API配置
//            addButton("获取上云API配置") {
//                mCarNestController?.getNestCloudApiConfig(object : MSdkCallback.ActionCallback<ParamCloudApiConfig> {
//                    override fun onSuccess(result: ParamCloudApiConfig?) {
//                        ToastUtils.showShort("上云配置: $result")
//                    }
//                    override fun onFailure(error: MError) {
//                        ToastUtils.showShort("获取上云配置失败")
//                    }
//                })
//            }

        // 44. 请求机库控制权
        addButton("请求控制权(默认)") {
            mNestController?.reqNestControlRights(
                "uuid",
                "192.168.1.1",
                0,
                0,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("请求控制权成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("请求控制权失败")
                    }
                })
        }

        // 45. 重试请求控制权
        addButton("重试控制权(默认)") {
            mNestController?.retryNestControlRights(
                "uuid",
                "192.168.1.1",
                0,
                0,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("重试控制权成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("重试控制权失败")
                    }
                })
        }

        // 46. 设置有线网络配置（默认空对象）
//            addButton("设置有线网络配置(空)") {
//                mCarNestController?.setWiredNetworkConfig(ParamWiredNetworkConfig(), object : MSdkCallback.ActionCallback<EmptyMsg> {
//                    override fun onSuccess(result: EmptyMsg?) {
//                        ToastUtils.showShort("设置有线网络成功")
//                    }
//                    override fun onFailure(error: MError) {
//                        ToastUtils.showShort("设置有线网络失败")
//                    }
//                })
//            }
//
//            // 47. 获取有线网络配置
//            addButton("获取有线网络配置") {
//                mCarNestController?.getWiredNetworkConfig(object : MSdkCallback.ActionCallback<ParamWiredNetworkConfig> {
//                    override fun onSuccess(result: ParamWiredNetworkConfig?) {
//                        ToastUtils.showShort("有线配置: $result")
//                    }
//                    override fun onFailure(error: MError) {
//                        ToastUtils.showShort("获取有线配置失败")
//                    }
//                })
//            }
    }

    /**
     * 添加 section 标题
     */
    private fun addSectionTitle(title: String?, parent: ViewGroup?) {
        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setPadding(0, 16, 0, 8)
            // 关键：设置 Flexbox 布局参数，使其占满整行
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.MATCH_PARENT,  // 宽度充满
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 以下属性确保该元素独占一行
                flexBasisPercent = 1f  // 占父容器宽度的 100%
                flexShrink = 0f        // 禁止收缩
                // 可设置对齐方式
                alignSelf = AlignItems.FLEX_START
            }
        }
        parent?.addView(titleView)
    }

    /**
     * 添加属性显示视图
     */
    private fun addPropertyView(name: String?, value: Any?) {
        val layout = LinearLayout(mContext)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(0, 4, 0, 4)

        val nameView = TextView(mContext)
        nameView.text = "$name: "
        nameView.setWidth(300)
        layout.addView(nameView)

        val valueView = TextView(mContext)
        valueView.text = value?.toString() ?: "null"
        layout.addView(valueView)

        mContentLayout!!.addView(layout)
    }

    /**
     * 添加可观察属性显示视图
     */
    private fun addObservablePropertyView(name: String?, initialValue: String?): TextView {
        val layout = LinearLayout(mContext)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(0, 4, 0, 4)

        val nameView = TextView(mContext)
        nameView.text = "$name: "
        nameView.setWidth(300)
        layout.addView(nameView)

        val valueView = TextView(mContext)
        valueView.text = initialValue
        layout.addView(valueView)

        mPropertyLayout?.addView(layout)
        return valueView
    }

    /**
     * 添加控制按钮
     */
    private fun addButton(text: String?, listener: View.OnClickListener?) {
        val button = Button(this).apply {
            this.text = text
            setOnClickListener(listener)
            setPadding(12, 4, 12, 4) // px
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
        }
        mContentLayout?.addView(button)
    }

    /**
     * 更新TextView内容（主线程）
     */
    private fun updateTextView(textView: TextView, content: String?) {
        runOnUiThread(Runnable { textView.text = content })
    }


    private fun initDebugControlButtons() {
        addSectionTitle("调试功能", mContentLayout)
        // 11. 机库内遥控器对频飞机 / 机库图传对频飞机
        addButton("机库遥控器对频") {
            mNestController?.rcFrequencyDrone(
                0x00,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("遥控器对频成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("遥控器对频失败")
                    }
                })
        }
        addButton("机库图传对频") {
            mNestController?.rcFrequencyDrone(
                0x01,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("图传对频成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("图传对频失败")
                    }
                })
        }
        addButton("机库低功耗模式") {
            mNestController?.nestModelControl(
                0x01,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("低功耗模式设置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("低功耗模式设置失败")
                    }
                })
        }
        // 2. 伺服控制：三个控制类型 × 三个消息 = 9 个按钮
        val servoTypes = mapOf(
            0x01.toByte() to "舱门",
            0x02.toByte() to "停机坪",
            0x03.toByte() to "飞机固定锁"
        )
        val servoMsgs = mapOf(
            0x00.toByte() to "关/收拢/解锁",
            0x01.toByte() to "开/展开/上锁"
        )
        for ((type, typeName) in servoTypes) {
            for ((msg, msgName) in servoMsgs) {
                val actionName = when (type) {
                    0x01.toByte() -> if (msg == 0x00.toByte()) "关" else "开"
                    0x02.toByte() -> if (msg == 0x00.toByte()) "收拢" else "展开"
                    0x03.toByte() -> if (msg == 0x00.toByte()) "解锁" else "上锁"
                    else -> ""
                }
                addButton("$typeName-$actionName") {
                    mNestController?.servoControl(
                        type,
                        msg,
                        object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                            override fun onSuccess(result: EmptyMsg?) {
                                ToastUtils.showShort("$typeName-$actionName 成功")
                            }

                            override fun onFailure(var1: Error?) {
                                ToastUtils.showShort("$typeName-$actionName 失败")
                            }
                        })
                }
            }
        }

        // 3. 外设控制：4种控制类型 × 2种消息 = 8 个按钮
        val periTypes = mapOf(
            0x01.toByte() to "飞机充电开关",
            0x02.toByte() to "车机电池充电开关",
            0x03.toByte() to "风扇开关",
            0x04.toByte() to "装饰灯"
        )
        val periMsgs = mapOf(
            0x00.toByte() to "关闭",
            0x01.toByte() to "开启"
        )
        for ((type, typeName) in periTypes) {
            for ((msg, msgName) in periMsgs) {
                addButton("外设-$typeName-$msgName") {
                    mNestController?.peripheralsControl(
                        type,
                        msg,
                        object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                            override fun onSuccess(result: EmptyMsg?) {
                                ToastUtils.showShort("外设-$typeName-$msgName 成功")
                            }

                            override fun onFailure(var1: Error?) {
                                ToastUtils.showShort("外设-$typeName-$msgName 失败")
                            }
                        })
                }
            }
        }


        // 4. 请求跳转：跳转到用户程序 / BootLoader
        addButton("跳转到用户程序") {
            mNestController?.reqJump(0x00, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("跳转用户程序成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("跳转用户程序失败")
                }
            })
        }
        addButton("跳转到BootLoader") {
            mNestController?.reqJump(0x01, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("跳转BootLoader成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("跳转BootLoader失败")
                }
            })
        }
        // 15. 机库模式控制
        addButton("机库重启") {
            mNestController?.nestModelControl(
                0x00,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("机库重启成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("机库重启失败")
                    }
                })
        }

        // 12. 机库灯语控制（17种）
        val lightModes = mapOf(
            0 to "红灯常亮", 1 to "红灯闪烁", 2 to "红灯呼吸",
            3 to "绿灯常亮", 4 to "绿灯闪烁", 5 to "绿灯呼吸",
            6 to "蓝灯常亮", 7 to "蓝灯闪烁", 8 to "蓝灯呼吸",
            9 to "红绿闪烁", 10 to "红绿呼吸", 11 to "红蓝闪烁",
            12 to "红蓝呼吸", 13 to "蓝绿闪烁", 14 to "蓝绿呼吸",
            15 to "红绿蓝闪烁", 16 to "红绿蓝呼吸"
        )
        for ((code, name) in lightModes) {
            addButton("灯语-$name") {
                mNestController?.nestLightControl(
                    code.toByte(),
                    object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                        override fun onSuccess(result: EmptyMsg?) {
                            ToastUtils.showShort("灯语控制成功")
                        }

                        override fun onFailure(var1: Error?) {
                            ToastUtils.showShort("灯语控制失败")
                        }
                    })
            }
        }
        // 13. 设置机库SN码（默认值）
        addButton("设置机库SN") {
            mNestController?.setNestSnCode("SN123", object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onFailure(error: Error) {
                    ToastUtils.showShort("SN设置失败")
                }

                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("SN设置成功")
                }
            })
        }
        // 6. 设置蓝牙名称（使用默认名称）
        addButton("设置蓝牙名称") {
            mNestController?.setBleName("BTGDU", object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("设置蓝牙名称成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("设置蓝牙名称失败")
                }
            })
        }

        // 7. 飞机任务准备：4个任务 × 2种类型 = 8 个按钮
        val taskInfos = mapOf(
            0x01.toByte() to "起飞任务准备",
            0x03.toByte() to "降落任务准备",
        )
        val taskTypes = mapOf(
            0.toByte() to "中止",
            1.toByte() to "执行"
        )
        for ((info, infoName) in taskInfos) {
            for ((type, typeName) in taskTypes) {
                addButton("任务-$infoName-$typeName") {
                    mNestController?.droneTaskPre(
                        info,
                        type,
                        object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                            override fun onSuccess(result: EmptyMsg?) {
                                ToastUtils.showShort("任务准备成功")
                            }

                            override fun onFailure(var1: Error?) {
                                ToastUtils.showShort("任务准备失败")
                            }
                        })
                }
            }
        }
        // 25. 上报遥控器经纬度（默认0）
        addButton("上报遥控器位置(0,0,0)") {
            mNestController?.uploadRCPosition(
                0.0,
                0.0,
                0.0,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("上报位置成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("上报位置失败")
                    }
                })
        }

        // 26. 演示模式开关
        addButton("开启演示模式") {
            mNestController?.changeHallMode(
                true,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("开启演示模式成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("开启演示模式失败")
                    }
                })
        }
        addButton("关闭演示模式") {
            mNestController?.changeHallMode(
                false,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("关闭演示模式成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("关闭演示模式失败")
                    }
                })
        }

        // 37. 视觉跟踪开关
        addButton("开启视觉跟踪") {
            mNestController?.setVisonTrack(true, object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                override fun onSuccess(result: EmptyMsg?) {
                    ToastUtils.showShort("开启视觉跟踪成功")
                }

                override fun onFailure(var1: Error?) {
                    ToastUtils.showShort("开启视觉跟踪失败")
                }
            })
        }
        addButton("关闭视觉跟踪") {
            mNestController?.setVisonTrack(
                false,
                object : CommonCallbacks.CompletionCallbackWith<EmptyMsg> {
                    override fun onSuccess(result: EmptyMsg?) {
                        ToastUtils.showShort("关闭视觉跟踪成功")
                    }

                    override fun onFailure(var1: Error?) {
                        ToastUtils.showShort("关闭视觉跟踪失败")
                    }
                })
        }
    }

    private fun calculateNewPosition(lat: Double, lon: Double, dirDeg: Double, distance: Double) : DoubleArray{
        val equatorRadius = 6371000.0 // WGS84椭球长半轴，单位：米

        // 角度转弧度（正北顺时针方向直接使用）
        val theta = Math.toRadians(dirDeg)
        val delta = distance / equatorRadius
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)

        // 新纬度计算
        val newLatRad = asin(sin(latRad) * cos(delta) + cos(latRad) * sin(delta) * cos(theta))
        val newLat = Math.toDegrees(newLatRad)

        // 新经度计算
        val y = sin(theta) * sin(delta) * cos(latRad)
        val x = cos(delta) - sin(latRad) * sin(newLatRad)
        val deltaLon = atan2(y, x)
        var newLon = Math.toDegrees(lonRad + deltaLon)
        // 规范化经度到[-180, 180]范围
        newLon = (newLon + 540) % 360 - 180;
        return doubleArrayOf(newLat, newLon)
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

    override fun onClick(v: View?) {
        // 按钮点击事件处理
    }
}
