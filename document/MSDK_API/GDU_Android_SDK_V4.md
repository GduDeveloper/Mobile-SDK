# GDU_Android_SDK_Wrapper 说明文档

GDU Android SDK (GduWrapper 封装层) 说明文档

|修改时间|修改内容|修改人|
|---|---|---|
|2026-08-31|1. 根据 GduWrapper 封装层架构全面更新类名、包名及方法签名规范（去除旧版 GDU 前缀）<br>2. 核心类名更新：`GDUSDKManager` -> `SDKManager`，`GDUAircraft` -> `Aircraft`，`GDUFlightController` -> `FlightController`，`GDUBattery` -> `Battery`，`GDURemoteController` -> `RemoteController`，`GDUGimbal` -> `Gimbal`，`GDUCamera` -> `Camera`，`GDUAirLink` -> `AirLink`，`GDUMediaManager` -> `MediaManager`，`GDUCodecManager` -> `CodecManager`，`GDUDiagnostics` -> `Diagnostics`<br>3. 新增 `GduWrapper` 专有组件说明：`Vision` (AI 目标跟踪与检测)、`Radar` (雷达感知与避障)、`PSDKManager` & `Megaphone` (载荷管理与喊话器)、`Pipeline` (MOP 载荷数据传输管道)、`LTE` (4G/5G通信链路)、`TargetLocationUtil` (目标定位计算)、`NestController` (机库控制) 等<br>4. 保留并完善航线任务、健康管理 HMS、多机组网及机场开发集成指南|GDU|
|2026-08-20|1. 2.4.2 RemoteController 新增方法说明：getDRTK、connectDRTK、setRCStateCallback<br>2. 2.4.2.1 DRTK：地面端差分定位管理对象<br>3. 2.4.2.2 DRTKSelfCalibrateCallback：DRTK自校准回调接口<br>4. 2.4.2.3 DRTKBaseStationCoord：DRTK基站坐标对象|GDU|
|2026-08-10|1. 2.5.2.1 增加设置已执行的航点数，配合断点续飞使用<br>2. 2.5.2.2 增加设置航点是否为安全点，配合安全点返航使用<br>3. 2.5.2.6 增加航线完成后动作枚举，增加沿航迹返航和安全点返航类型<br>4. 2.6.3 增加飞行器log下载|GDU|
|2026-08-06|2.4.1 增加设置和获取飞机返航预留电量|GDU|
|2026-08-04|1. 2.4.3 增加图传软硬件版本号获取<br>2. 2.4.5.3 增加相册媒体文件获取与下载接口|GDU|
|2026-07-27|1. 2.4.1 修复机场返航问题<br>2. 2.7 增加机场集成使用说明|GDU|

---

# 1. 添加 GDU_SDK_ANDROID 开发包

### 1.1.1、添加依赖文件

将无人机 SDK 的 aar 包及所需依赖库复制到工程模块（如 App 模块或示例 Demo）的 `libs` 目录下。

### 1.1.2、在主工程 build.gradle 配置 dependencies

（1）添加 SDK 依赖，dependencies 配置方式如下：

```groovy
dependencies {
    api fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
    // 基础组件依赖
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

（2）SO 动态链接库配置到 build.gradle 的 android 闭包中，方式如下：

```groovy
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
}
```

### 1.1.3、配置权限

在 `AndroidManifest.xml` 中配置应用运行所需权限:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

# 2. 接口参考文档

# **2.1、MANAGER CLASSES**

## 2.1.1 SDKManager

`SDKManager` 是将 Mobile SDK 与普宙飞行器一起使用的核心入口类（单例模式）。用于注册 SDK、建立/断开与飞行器的通信连接、设置工作场景以及获取飞行器实例。

*包路径：`com.gdu.sdk.manager.SDKManager`*

|**限定符和类型**|**方法和说明**|
|---|---|
|SDKManager|**getInstance**() 获取 SDKManager 单例实例|
|BaseProduct?|**getProduct**() 获取当前飞行器实体对象（Aircraft）|
|void|**registerApp**(Context context, SDKManagerCallback callback) 注册初始化 SDK|
|void|**startConnectionToProduct**() 开始建立 SDK 和飞行器的通信连接，需在 registerApp 成功后调用|
|void|**setConnectScene**(ConnectScene scene) 设置连接场景（`ConnectScene.RC`: 普通遥控器模式, `ConnectScene.HANGAR`: 机巢模式, `ConnectScene.CUSTOM_RC`: 自定义遥控器模式）|
|String|**getSDKVersion**() 获取 SDK 版本号（当前返回 "V4.0"）|
|IDeviceManager|**getDeviceManager**() 获取底层设备管理器对象|
|void|**destroy**() 销毁并释放 SDK 资源|

### 2.1.1.1 SDKManagerCallback

SDK 全局状态与组件连接监听回调接口。

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**onRegister**(Error error) SDK 注册结果回调，成功返回 Error.REGISTRATION_SUCCESS|
|void|**onProductConnect**(BaseProduct? product) 飞行器连接成功回调|
|void|**onProductDisconnect**() 飞行器断开连接回调|
|void|**onProductChanged**(BaseProduct? product) 飞行器更换回调|
|void|**onComponentChange**(BaseComponent? oldComponent, BaseComponent? newComponent) 飞行器子组件（飞控、云台、相机、电池、图传等）连接状态变更回调|
|void|**onInitProcess**(SDKInitEvent event, int totalProcess) SDK 初始化进度事件回调|

---

# **2.2、BASE CLASSES**

### 2.2.1 BaseProduct

`BaseProduct` 基础产品抽象类，`Aircraft`（飞行器实体）继承该类，提供飞行器连接判定、SN 获取、机型查询以及健康管理状态监听等功能。

*包路径：`com.gdu.sdk.base.BaseProduct`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**getProductSN**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取飞行器序列号 SN|
|Model|**getModel**() 获取飞行器具体型号（如 S200, S220, S220_PRO, S280, S400, P200, P220, P220_PRO 等）|
|void|**setDiagnosticsInformationCallback**(Diagnostics.DiagnosticsInformationCallback callback) 设置健康管理/故障诊断监听回调|
|boolean|**isConnected**() 获取飞行器当前是否连接正常|

#### **2.2.1.1 VideoFeeder**

`VideoFeeder` 管理移动端实时视频流通道，获取主视频流（相机画面）或副视频流（如 FPV / 辅助视角流）。

*包路径：`com.gdu.sdk.camera.VideoFeeder`*

|**限定符和类型**|**方法和说明**|
|---|---|
|VideoFeeder|**getInstance**() 获取 VideoFeeder 单例实例|
|VideoFeed|**getPrimaryVideoFeed**() 获取主视频流通道（默认相机画面）|
|VideoFeed|**getSecondaryVideoFeed**() 获取副视频流通道|

##### **2.2.1.1.1 VideoFeed**

视频流数据接收与分发接口。

|**限定符和类型**|**方法和说明**|
|---|---|
|boolean|**addVideoDataListener**(VideoDataListener dataListener) 添加原始视频流数据监听|
|boolean|**removeVideoDataListener**(VideoDataListener dataListener) 移除视频流数据监听|
|Set&lt;VideoDataListener&gt;|**getListeners**() 获取所有已注册的视频流监听器集合|
|void|**destroy**() 销毁并清除所有视频流监听器|

##### **2.2.1.1.2 VideoDataListener**

视频数据监听接口。

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**onReceive**(byte[] datas, int len) 接收到原始视频帧数据（H264/H265 编码）回调|

### 2.2.2 BaseComponent

`BaseComponent` 基础组件抽象基类，飞控、电池、遥控器、云台、相机、图传等组件均继承该类。

*包路径：`com.gdu.sdk.base.BaseComponent`*

|**限定符和类型**|**方法和说明**|
|---|---|
|int|**getDeviceId**() 获取当前模块所属的设备 ID|
|void|**getFirmwareVersion**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; version) 获取组件固件版本号|
|void|**setComponentListener**(ComponentListener componentListener) 设置组件连接状态变化监听|
|boolean|**isConnected**() 判断组件当前是否已连接|

---

# **2.3、PRODUCT CLASSES**

### 2.3.1 Aircraft

`Aircraft` 飞行器实体类，继承自 `BaseProduct`。通过 `SDKManager.getInstance().getProduct()` 获取。包含飞行器全部硬件及功能子组件。

*包路径：`com.gdu.sdk.products.Aircraft`*

|**限定符和类型**|**方法和说明**|
|---|---|
|FlightController|**flightController** 获取飞控组件|
|Battery|**battery** 获取电池组件|
|RemoteController|**remoteController** 获取遥控器组件|
|BaseComponent?|**gimbal** 获取云台组件（支持多光/不同型号云台多态解析）|
|BaseComponent?|**camera** 获取相机组件（支持 8K, 30X, PDL1K, PFL, S200, S220Pro, PSDK 相机等多态解析）|
|AirLink|**airLink** 获取图传无线链路组件|
|Vision|**vision** 获取 AI 视觉感知与目标跟踪组件|
|Radar|**radar** 获取毫米波/超声雷达避障感知组件|
|Megaphone|**megaphone** 获取 PSDK 喊话器控制组件|
|LTE|**lte** 获取 4G/5G 备份图传通信组件|
|NoFlyZone|**noFlyZone** 获取禁飞区管理组件|
|CustomMsg|**customMsg** 获取 PSDK 自定义数据透传组件|
|Hms|**hms** 获取飞行器健康管理系统组件|
|CodecManager|**codecManager** 获取视频硬件编解码管理组件|
|VideoFeeder|**videoFeeder** 获取视频码流分发器|
|NestController|**nestController** 获取机库控制组件|
|void|**getProductSN**(CompletionCallbackWith&lt;String&gt; callback) 获取飞机序列号|
|Model|**getModel**() 获取飞机型号枚举|
|boolean|**isConnected**() 判断飞机是否连接|

---

# **2.4、COMPONENT CLASSES**

### 2.4.1 FlightController

`FlightController` 飞控组件，提供向飞行器发送各种飞行控制、返航降落、状态监听、限高限距、虚拟摇杆等核心指令。

*包路径：`com.gdu.sdk.flightcontroller.FlightController`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**startTakeoff**(CommonCallbacks.CompletionCallback callback) 一键起飞（默认起飞至 1.5 米悬停）|
|void|**startTakeoff**(int height, CommonCallbacks.CompletionCallback callback) 一键起飞到设定高度悬停（单位：cm）|
|void|**cancelTakeoff**(CommonCallbacks.CompletionCallback callback) 停止/取消起飞任务|
|void|**startLanding**(CommonCallbacks.CompletionCallback callback) 发送降落指令，从当前高度开始降落|
|void|**cancelLanding**(CommonCallbacks.CompletionCallback callback) 取消当前降落任务|
|void|**startGoHome**(CommonCallbacks.CompletionCallback callback) 发送一键返航指令|
|void|**cancelGoHome**(CommonCallbacks.CompletionCallback callback) 取消当前返航任务|
|void|**setHomeLocation**(LocationCoordinate2D homeLocation, CommonCallbacks.CompletionCallback callback) 设置飞机返航点坐标（Home 点）|
|void|**startPrecisionGoHome**(CommonCallbacks.CompletionCallback callback) 开启精准返航（配合视觉降落标靶）|
|void|**cancelPrecisionGoHome**(CommonCallbacks.CompletionCallback callback) 取消精准返航|
|void|**setPrecisionGoHomeStateCallback**(PrecisionGoHomeState.Callback callback) 设置精准返航状态监听|
|void|**exactBack**(boolean isOpen, CommonCallbacks.CompletionCallback callback) 移动精准返航控制（车载/移动平台降落）|
|void|**carNestExactBack**(boolean isOpen, short frontDis, short right, short topDis, int returnType, CommonCallbacks.CompletionCallbackWith&lt;EmptyMsg&gt; callback) 车载机库精准返航控制|
|void|**setMaxFlightHeight**(int maxHeight, CommonCallbacks.CompletionCallback callback) 设置最大飞行高度（范围 5-500，单位 米）|
|void|**getMaxFlightHeight**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取设置的最大飞行高度（单位 米）|
|void|**setFlightLimitHeight**(boolean isOpen, int limitHeight, CommonCallbacks.CompletionCallback callback) 设置限高开关及限高数值|
|void|**getDroneLimitHeight**(CommonCallbacks.CompletionCallbackWith&lt;LimitHeightInfo&gt; callback) 获取飞机限高详细信息（包含开关和高度值）|
|void|**setMaxFlightRadius**(int maxRadius, CommonCallbacks.CompletionCallback callback) 设置最远飞行半径（单位 米）|
|void|**getMaxFlightRadius**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取最远飞行半径（单位 米）|
|void|**setMaxFlightRadiusLimitationEnabled**(boolean enabled, CommonCallbacks.CompletionCallback callback) 设置限距功能开关|
|void|**getMaxFlightRadiusLimitationEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取限距功能开关状态|
|void|**setGoHomeHeightInMeters**(short backHeight, CommonCallbacks.CompletionCallback callback) 设置最低返航高度（高度范围 5m ~ 500m）|
|void|**getGoHomeHeightInMeters**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取当前设置的最低返航高度|
|void|**setConnectionFailSafeBehavior**(ConnectionFailSafeBehavior behavior, CommonCallbacks.CompletionCallback callback) 设置失控失联行为（HOVER: 悬停, GO_HOME: 返航, LANDING: 降落）|
|void|**getConnectionFailSafeBehavior**(CommonCallbacks.CompletionCallbackWith&lt;ConnectionFailSafeBehavior&gt; callback) 获取失控失联行为配置|
|void|**setLowBatteryWarningThreshold**(int percent, CommonCallbacks.CompletionCallback callback) 设置一级低电量告警阈值（百分比 15-50）|
|void|**getLowBatteryWarningThreshold**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取一级低电量告警阈值|
|void|**setSeriousLowBatteryWarningThreshold**(int percent, CommonCallbacks.CompletionCallback callback) 设置二级严重低电量告警阈值（百分比 10-45）|
|void|**getSeriousLowBatteryWarningThreshold**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取二级严重低电量告警阈值|
|void|**setLowBatteryWarningThreshold**(byte oneLevel, byte twoLevel, CommonCallbacks.CompletionCallback callback) 同时设置一级和二级低电量报警阈值|
|void|**getLowBatteryWarningThreshold**(CommonCallbacks.CompletionCallbackWith&lt;LowBatteryWarnInfo&gt; callback) 获取低电量报警详细配置|
|void|**startTapFly**(LocationCoordinate3D point, float horizonSpeed, float verticalSpeed, CommonCallbacks.CompletionCallback callback) 开始指点飞行|
|void|**stopTapFly**(CommonCallbacks.CompletionCallback callback) 停止指点飞行|
|void|**setTapFlyStateCallback**(TapFlyState.Callback callback) 设置指点飞行状态监听|
|void|**setStateCallback**(FlightControllerState.Callback callback) 设置飞控全量飞行状态监听（姿态、速度、高度、卫星数等）|
|FlightState?|**getCurrentFlightState**() 获取当前飞行器状态（起飞、降落、悬停、飞行、返航等）|
|void|**confirmSmartReturnToHomeRequest**(boolean isConfirm, CommonCallbacks.CompletionCallback callback) 确认或拒绝智能返航请求|
|RTK|**getRTK**() 获取 RTK 差分定位管理对象|
|Simulator|**getSimulator**() 获取飞行模拟器对象|
|Compass|**getCompass**() 获取磁力计/指南针校准对象|
|FlightAssistant|**getFlightAssistant**() 获取智能飞行辅助与避障对象|
|VirtualStick|**getVirtualStick**() 获取虚拟摇杆控制对象|
|void|**changYawAngular**(RotationMode mode, int angle, CommonCallbacks.CompletionCallback callback) 设置机头航向角（支持绝对角度与相对角度）|
|void|**setHorizontalSpeed**(short xSpeed, short ySpeed) 设置飞机水平飞行速度（单位 cm/s，X 前后，Y 左右）|
|void|**stopHorizontalSpeed**() 停止水平速度控制|
|void|**setVerticalSpeed**(short speed) 设置飞机垂直升降飞行速度（单位 cm/s，向上为正）|
|void|**stopVerticalSpeed**() 停止垂直速度控制|
|void|**setBackHomeAction**(byte index, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 设置返航行为（0: 返航降落, 1: 返航悬停不降落）|
|void|**getBackHomeAction**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取返航行为配置|
|void|**setBackHomeSpeed**(int backSpeed, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 设置返航巡航速度（单位 m/s）|
|void|**getDroneBackInfo**(CommonCallbacks.CompletionCallbackWith&lt;DroneBackInfo&gt; callback) 获取返航高度及速度信息|
|void|**setFlyModeState**(boolean isOpen, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 设置允许切换飞行模式开关|
|void|**getFlyModeState**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取飞行模式开关|
|void|**setTripodMode**(boolean isOpen, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 设置是否开启三脚架微动模式|
|void|**getTripodMode**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取三脚架模式状态|
|void|**checkIMUCalibration**(byte status, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 控制 IMU 校准（0 停止，1 开始）|
|void|**addIMUCalibrationCallback**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 设置 IMU 校准进度与状态监听|
|void|**controlFlightFollow**(boolean enable, double lat, double lng, float height, byte heightType, HeadingTypeEnum headingType, CommonCallbacks.CompletionCallbackWith&lt;EmptyMsg&gt; callback) 控制 GPS 跟随飞行|
|void|**updateGPSFollowPoint**(double lat, double lng, CommonCallbacks.CompletionCallbackWith&lt;EmptyMsg&gt; callback) 动态更新 GPS 跟随目标点坐标|
|void|**setGpsFollowStateCallback**(GPSFollowState.Callback callback) 设置 GPS 跟随状态监听|
|void|**controlFlightSurround**(byte type, double lat, double lng, byte surroundSpeed, short surroundDistance, short num, byte numType, short height, int surroundType, CommonCallbacks.CompletionCallbackWith&lt;EmptyMsg&gt; callback) 控制 GPS 环绕飞行|
|void|**setGpsSurroundStateCallback**(GPSSurroundState.Callback callback) 设置 GPS 环绕状态监听|

#### **2.4.1.1 FlightControllerState**

表示飞行控制器的全量实时运行状态信息。

*包路径：`com.gdu.sdk.flightcontroller.FlightControllerState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|Attitude|**getAttitude**() 获取飞机姿态（pitch 俯仰, roll 横滚, yaw 偏航角度）|
|GoHomeExecutionState|**getGoHomeExecutionState**() 获取返航执行状态（NOT_EXECUTING, GO_UP_TO_HEIGHT, TURN_DIRECTION_TO_HOME_POINT, AUTO_FLY_TO_HOME_POINT, GO_DOWN_TO_GROUND 等）|
|LocationCoordinate2D|**getHomeLocation**() 获取当前返航点（Home 点）经纬度|
|LocationCoordinate3D|**getAircraftLocation**() 获取飞机当前三维坐标（经纬度与相对高度）|
|FlightMode|**getFlightMode**() 获取操控模式（ATTI 姿态, GPS_NORMAL 标准, GPS_SPORT 运动, VISUAL 视觉, TRIPOD 三脚架）|
|FlightState|**getFlightState**() 获取当前飞行状态（FLING 飞行中, GROUND 地面, TAKEOFF 起飞中, LANDING 降落中, HOVERING 悬停中, BACKING 返航中）|
|boolean|**isHasReachedMaxFlightHeight**() 是否达到最大限定高度|
|boolean|**isHasReachedMaxFlightRadius**() 是否达到最大限定距离|
|boolean|**isHomeLocationSet**() 返航点是否已成功设置|
|boolean|**isGoingHome**() 飞机是否处于返航中|
|boolean|**isFlying**() 飞机是否在空中飞行|
|boolean|**isMotorsOn**() 电机是否已解锁启动|
|int|**getGoHomeHeight**() 获取设定的返航高度（单位：cm）|
|int|**getSatelliteCount**() 获取飞机搜到的 GNSS/GPS 卫星颗数|
|int|**getOnCarSatelliteCount**() 获取车载端搜到的卫星颗数|
|float|**getVelocityX**() 获取 X 轴飞行速度（单位：cm/s）|
|float|**getVelocityY**() 获取 Y 轴飞行速度（单位：cm/s）|
|float|**getVelocityZ**() 获取垂直升降速度（单位：cm/s）|
|int|**getFlightTimeInSeconds**() 获取当次飞行持续时间（单位：秒）|
|long|**getAllFlyTime**() 获取历史累计总飞行时长（单位：秒）|
|long|**getTotalFlightMovements**() 获取历史累计总飞行架次|
|boolean|**isSimulatorActive**() 模拟飞行功能是否开启|
|float|**getDistance**() 获取飞机距离起飞点的直线距离（单位：cm）|
|float|**getTakeoffLocationAltitude**() 获取起飞点海拔椭球高度（单位：米）|
|GoHomeAssessment|**getGoHomeAssessment**() 获取智能返航评估数据（降落所需时间、返航所需电量、倒计时等）|
|GPSSignalLevel|**getGpsSignalLevel**() 获取 GPS 信号质量等级（LEVEL_0 ~ LEVEL_10）|
|float|**getUltrasonicHeightInMeters**() 获取下视/超声波测距高度（单位：米）|
|int|**getDifferenceHeight**() 获取飞机相对车机的差分相对高度|
|boolean|**isDifferenceAvailable**() 车机差分相对定位是否有效|
|int|**getCarSpeed**() 获取车机行驶速度|
|int|**getAircraftMode**() 获取飞机模式（0: 普通模式, 1: 机巢模式）|

#### **2.4.1.2 RTK**

RTK 实时高精度差分定位管理。

*包路径：`com.gdu.sdk.flightcontroller.rtk.RTK`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setReferenceStationSource**(ReferenceStationSource currentReferenceSource, CommonCallbacks.CompletionCallback callback) 设置差分基准站源类型（BASE_STATION: 移动基站, CUSTOM_NETWORK_SERVICE: 自定义网络 NTRIP, ONBOARD_RTK: 机载）|
|void|**setStateCallback**(RTKState.Callback callback) 设置 RTK 解算状态监听|
|void|**connectRtk**(ReferenceStationSource currentReferenceSource, NetworkServiceSettings settings, GduRtkManager.OnRtkConnectListener listener) 连接 RTK 差分服务|
|void|**disconnectRtk**() 断开 RTK 差分服务|

#### **2.4.1.3 RTKState**

RTK 状态与卫星观测信息。

*包路径：`com.gdu.rtk.RTKState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|PositioningSolution|**getPositioningSolution**() 获取定位解状态（NONE, SINGLE_POINT 单点, FLOAT 浮点解, FIXED_POINT 固定解）|
|HeadingSolution|**getHeadingSolution**() 获取定向解状态|
|ReceiverInfo|**getMsReceiver1GPSInfo**() / **getMsReceiver1BeiDouInfo**() 主天线 GPS/北斗卫星信息|
|ReceiverInfo|**getMsReceiver2GPSInfo**() / **getMsReceiver2BeiDouInfo**() 从天线 GPS/北斗卫星信息|
|ReceiverInfo|**getBsReceiverGPSInfo**() / **getBsReceiverBeiDouInfo**() 基站 GPS/北斗卫星信息|
|LocationCoordinate2D|**getMsFusionLocation**() 获取 RTK 融合高精度经纬度|
|float|**getMsFusionAltitude**() 获取 RTK 融合椭球高（单位：cm）|
|LocationCoordinate2D|**getBsLocation**() / **getBsAltitude**() 获取基站经纬度与椭球高|
|float|**getAircraftAltitude**() 获取飞行器当前海拔高度（单位：cm）|

#### **2.4.1.4 FlightAssistant**

智能飞行辅助与避障控制类。

*包路径：`com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setVisionSensingEnabled**(boolean enabled, CommonCallbacks.CompletionCallback callback) 设置视觉感知开关|
|void|**getVisionSensingEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取视觉感知开关|
|void|**setObstacleAvoidanceStrategyEnabled**(boolean enabled, CommonCallbacks.CompletionCallback callback) 设置自主避障策略开关|
|void|**getObstacleAvoidanceStrategyEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取自主避障策略开关|
|void|**setUpwardVisionObstacleAvoidanceEnabled**(Boolean enabled, CommonCallbacks.CompletionCallback callback) 设置上视避障开关|
|void|**getUpwardVisionObstacleAvoidanceEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取上视避障开关|
|void|**setHorizontalVisionObstacleAvoidanceEnabled**(boolean enabled, CommonCallbacks.CompletionCallback callback) 设置水平全向避障开关|
|void|**getHorizontalVisionObstacleAvoidanceEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取水平全向避障开关|
|void|**setVisualObstaclesAvoidanceDistance**(float distance, FlightAssistantObstacleSensingDirection direction, CommonCallbacks.CompletionCallback callback) 设置指定方向的避障告警刹车距离（单位：米）|
|void|**getVisualObstaclesAvoidanceDistance**(FlightAssistantObstacleSensingDirection direction, CommonCallbacks.CompletionCallbackWith&lt;Float&gt; callback) 获取指定方向的避障距离|
|void|**setDownwardFillLightMode**(FillLightMode mode, CommonCallbacks.CompletionCallback callback) 设置下视补光灯模式（ON 开启, OFF 关闭, AUTO 自动）|
|void|**getDownwardFillLightMode**(CommonCallbacks.CompletionCallbackWith&lt;FillLightMode&gt; callback) 获取下视补光灯状态|
|void|**setRTHObstacleAvoidanceEnabled**(boolean isOpen, CommonCallbacks.CompletionCallback callback) 设置返航过程自主避障开关|
|void|**getRTHObstacleAvoidanceEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取返航避障开关|
|void|**setLandingProtectionEnabled**(boolean isOpen, CommonCallbacks.CompletionCallback callback) 设置下视降落保护与地形探测开关|
|void|**getLandingProtectionEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取降落保护开关|

#### **2.4.1.5 Compass**

磁力计与指南针校准组件。

*包路径：`com.gdu.sdk.flightcontroller.Compass`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**startCalibration**(CommonCallbacks.CompletionCallback callback) 启动指南针校磁模式|
|void|**stopCalibration**(CommonCallbacks.CompletionCallback callback) 退出/停止指南针校磁|
|void|**setCompassCalibrationStateCallback**(CompassCalibrationState.Callback callback) 设置指南针校准进度与状态回调|

#### **2.4.1.6 VirtualStick**

虚拟摇杆实时飞行控制组件。

*包路径：`com.gdu.sdk.flightcontroller.virtualstick.VirtualStick`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setVirtualStickModeEnabled**(boolean enable, CommonCallbacks.CompletionCallback callback) 开启或关闭虚拟摇杆模式|
|void|**getVirtualStickModeEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取虚拟摇杆模式开关|
|boolean|**isVirtualStickControlModeAvailable**() 判断当前状态下虚拟摇杆是否允许介入控制|
|void|**setVerticalControlMode**(VerticalControlMode mode) 设置垂直通道控制模式（VELOCITY 速度控制 / POSITION 高度位置控制）|
|void|**setRollPitchControlMode**(RollPitchControlMode mode) 设置横滚俯仰控制模式（VELOCITY 速度控制 / ANGLE 角度控制）|
|void|**setYawControlMode**(YawControlMode mode) 设置偏航角控制模式（ANGULAR_VELOCITY 角速度 / ANGLE 绝对角度）|
|void|**setRollPitchCoordinateSystem**(FlightCoordinateSystem system) 设置坐标系（BODY 机体坐标系 / GROUND 地面坐标系）|
|void|**sendVirtualStickFlightControlData**(FlightControlData data, CommonCallbacks.CompletionCallback callback) 发送虚拟摇杆控制量（pitch, roll, yaw, verticalThrottle）|

---

### 2.4.2 RemoteController

`RemoteController` 遥控器组件，提供摇杆映射模式、对频、中位/最大行程校准、多机组网多控、以及地面差分站 DRTK 管理等功能。

*包路径：`com.gdu.sdk.remotecontroller.RemoteController`*

|**限定符和类型**|**方法和说明**|
|---|---|
|String|**getRCSN**() 获取遥控器序列号 SN|
|void|**setAircraftMappingStyle**(AircraftMappingStyle style, CommonCallbacks.CompletionCallback callback) 设置摇杆控制手势（STYLE_1: 日本手, STYLE_2: 美国手, STYLE_3: 中国手）|
|void|**getAircraftMappingStyle**(CommonCallbacks.CompletionCallbackWith&lt;AircraftMappingStyle&gt; callback) 获取当前摇杆手势映射模式|
|void|**startPairing**(CommonCallbacks.CompletionCallback callback) 触发遥控器与飞机无线对频|
|void|**setChargeRemainingCallback**(RCBatteryState.Callback callback) 设置遥控器电池电量监听|
|void|**setRCStateCallback**(RCState.Callback callback) 设置遥控器状态监听（移动站使能、经纬度及朝向角）|
|void|**startRCMedianCalibration**(CommonCallbacks.CompletionCallback callback) 开始遥控器中位校准|
|void|**startRCMaximumCalibration**(CommonCallbacks.CompletionCallback callback) 开始遥控器最大行程校准|
|void|**stopRCCalibration**(CommonCallbacks.CompletionCallback callback) 停止遥控器摇杆校准|
|void|**setCalibrationStateCallback**(CalibrationState.Callback callback) 设置遥控器校准状态监听|
|void|**setRCControlRodValue**(short rollV, short pitchV, short acceleratorV, short headingV, CommonCallbacks.CompletionCallback callback) 设置遥控器虚拟摇杆杆量数值|
|void|**setContinueSendRCControlMidValueEnable**(boolean isEnable, CommonCallbacks.CompletionCallback callback) 设置是否持续发送摇杆中值（用于无物理摇杆的控制终端/机巢场景）|
|void|**getContinueSendRCControlMidValueEnable**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取是否正在持续发送摇杆中值|
|void|**setMultiControlMode**(MultiControlMode controlMode, CommonCallbacks.CompletionCallback callback) 设置多控模式（一控一、一控多、多控一等）|
|void|**getMultiControlMode**(CommonCallbacks.CompletionCallbackWith&lt;MultiControlMode&gt; callback) 获取当前多控模式|
|void|**getDroneList**(CommonCallbacks.CompletionCallbackWith&lt;List&lt;MultiControlInfo&gt;&gt; callback) 获取组网中的飞机列表|
|void|**getRCList**(CommonCallbacks.CompletionCallbackWith&lt;List&lt;MultiControlInfo&gt;&gt; callback) 获取组网中的遥控器列表|
|void|**setDroneControlEnable**(int droneId, CommonCallbacks.CompletionCallback callback) 切换/获取指定飞机的飞行控制权|
|void|**getDroneControlEnable**(int droneId, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 查询当前是否拥有指定飞机的控制权|
|void|**setDroneLivingEnable**(int droneId, CommonCallbacks.CompletionCallback callback) 设置多控指定飞机的视频流拉流使能|
|void|**getDroneLivingEnable**(int droneId, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取指定飞机的视频流拉流使能状态|
|DRTK|**getDRTK**() 获取地面端差分定位管理对象（DRTK）|
|void|**connectDRTK**(CommonCallbacks.CompletionCallback callback) 请求地面 NPS 连接（图传盒子与差分站场景）|

#### **2.4.2.1 DRTK**

地面端差分定位管理对象。

*包路径：`com.gdu.sdk.remotecontroller.DRTK`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setDRTKStateCallback**(DRTKStateInfo.Callback callback) 设置 DRTK 基站定位状态监听（UTC时间、经纬度、卫星数、海拔、解算状态等）|
|void|**setNestDRTKSelfCalibrateCallback**(DRTKSelfCalibrateCallback callback) 设置机库 DRTK 自标定校准状态监听|
|void|**startNestDRTKSelfCalibra**(String ipAddress, String port, String userID, String password, String mountedPoint, CommonCallbacks.CompletionCallback callback) 配置 NTRIP 差分服务器参数并启动机库 DRTK 自校准|
|void|**stopNestDRTKSelfCalibra**(CommonCallbacks.CompletionCallback callback) 停止机库 DRTK 自校准|
|void|**setDRTKBaseStationCoord**(DRTKBaseStationCoord coord, CommonCallbacks.CompletionCallback callback) 设置 DRTK 基站坐标（纬度、经度、海拔高度）|
|void|**getDRTKBaseStationCoord**(CommonCallbacks.CompletionCallbackWith&lt;DRTKBaseStationCoord&gt; callback) 获取 DRTK 基站设定坐标|
|void|**setDRTKMode**(DRTKMode mode, CommonCallbacks.CompletionCallback callback) 设置 DRTK 运行模式（BASE_STATION 基准站, MOBILE_STATION 移动站）|
|void|**getDRTKMode**(CommonCallbacks.CompletionCallbackWith&lt;DRTKMode&gt; callback) 获取当前 DRTK 运行模式|

---

### 2.4.3 Battery

`Battery` 电池组件，管理连接的智能飞行电池实时状态与健康数据。

*包路径：`com.gdu.sdk.battery.Battery`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setStateCallback**(BatteryState.Callback callback) 设置电池实时状态监听（电量百分比、电压、电流、温度、电芯电压列表等）|
|void|**getSerialNumber**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取电池唯一序列号 SN|
|void|**getFirmwareVersion**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取电池固件版本号|
|void|**destroy**() 销毁并释放监听|

#### **2.4.3.1 BatteryState**

电池实时状态信息。

*包路径：`com.gdu.battery.BatteryState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|int|**getChargeRemainingInPercent**() 获取当前电池剩余电量百分比（0-100）|
|int|**getFullChargeCapacity**() 获取电池出厂总容量（单位：mAh）|
|int|**getChargeRemaining**() 获取电池当前剩余容量（单位：mAh）|
|int|**getVoltage**() 获取电池总电压（单位：mV）|
|int|**getCurrent**() 获取电池当前放电/充电电流（单位：mA）|
|float|**getTemperature**() 获取电池温度（单位：℃）|
|int|**getNumberOfDischarges**() 获取电池历史充放电循环次数|
|ConnectionState|**getConnectionState**() 获取电池连接通信状态（NORMAL 正常, EXCEPTION 异常, INVALID 无效）|
|List&lt;Integer&gt;|**getCellVoltages**() 获取各串单体电芯实时电压列表（单位：mV）|

---

### 2.4.4 AirLink

`AirLink` 图传无线链路组件，管理飞机与地面端之间 SDR/无线信道通信、码流带宽及信号质量。

*包路径：`com.gdu.sdk.airlink.AirLink`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setUplinkSignalQualityCallback**(SignalQualityCallback callback) 设置上行与下行图传信号质量百分比监听|
|void|**getVisibleLightStream**(CommonCallbacks.CompletionCallbackWith&lt;Int&gt; callback) 获取可见光输出码流设置参数|
|void|**setImageTransmissionInfo**(int channel, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 设置图传频段信道号|
|void|**getITFrequencyBandwidth**(CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 获取图传无线频率带宽配置|
|void|**setITFrequencyBandwidth**(byte value, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 设置图传无线频率带宽|
|void|**setLTEPushStreamType**(byte type, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 切换推流协议方式（1: RTMP, 2: WebRTC）|
|void|**setChangeSteamSwitch**(byte switchType, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 设置动态码流自适应变换开关|
|void|**setOutputStream**(byte stream, CommonCallbacks.CompletionCallbackWith&lt;Byte&gt; callback) 设置可见光视频输出码流值（0: 0.5M, 1: 1M, 2: 1.5M, 3: 2M, 4: 4M, 5: 8M）|
|void|**set4GServiceIp**(byte[] value, CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 配置 4G 云台推流服务器 IP/域名|
|void|**getFirmwareVersion**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取图传固件版本号|

---

### 2.4.5 Gimbal

`Gimbal` 云台组件，提供云台三轴姿态角度控制、回中、自校准以及红外/多光模式设置。

*包路径：`com.gdu.sdk.gimbal.Gimbal`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setStateCallback**(GimbalState.Callback callback) 设置云台姿态与校准状态监听|
|void|**startCalibration**(CommonCallbacks.CompletionCallback callback) 启动云台自动校准|
|void|**reset**(CommonCallbacks.CompletionCallback callback) 云台一键快速回中|
|void|**rotate**(Rotation rotation, CommonCallbacks.CompletionCallback callback) 控制云台俯仰(Pitch)、横滚(Roll)、偏航(Yaw)角度旋转（支持绝对角度与相对角度模式）|
|GimbalType|**getGimbalType**() 获取当前挂载的云台硬件类型|
|SettingsDefinitions.DisplayMode|**getSuppprtDisplayMode**() 获取云台支持的光学显示模式集合（红外、可见光、广角、变焦、分屏）|
|void|**getFirmwareVersion**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取云台固件版本号|
|void|**getGimbalSN**(CommonCallbacks.CompletionCallbackWith&lt;String&gt; callback) 获取云台序列号 SN|

#### **2.4.5.1 GimbalState**

云台实时运行状态。

*包路径：`com.gdu.gimbal.GimbalState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|Attitude|**getAttitudeInDegrees**() 获取云台姿态角度（Pitch 俯仰, Roll 横滚, Yaw 航向）|
|boolean|**isCalibrating**() 云台是否正在校准中|
|boolean|**isCalibrationSuccessful**() 云台最近一次校准是否成功|
|int|**getCalibrationState**() 获取云台校准详细状态码|

---

### 2.4.6 Camera

`Camera` 相机组件，控制拍照、录像、光学变焦、红外测温、激光测距、曝光参数调节以及相册媒体管理。

*包路径：`com.gdu.sdk.camera.Camera`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**startShootPhoto**(CommonCallbacks.CompletionCallback callback) 触发单张/连拍照|
|void|**stopShootPhoto**(CommonCallbacks.CompletionCallback callback) 停止拍照|
|void|**startRecordVideo**(CommonCallbacks.CompletionCallback callback) 开始录像|
|void|**stopRecordVideo**(CommonCallbacks.CompletionCallback callback) 停止录像|
|void|**setMode**(CameraMode cameraMode, CommonCallbacks.CompletionCallback completionCallback) 切换相机工作模式（SHOOT_PHOTO 拍照模式, RECORD_VIDEO 录像模式）|
|void|**getMode**(CommonCallbacks.CompletionCallbackWith&lt;CameraMode&gt; callback) 获取当前相机工作模式|
|void|**setStorageStateCallBack**(StorageState.Callback callback) 设置 SD 卡存储容量与挂载状态监听|
|void|**setSystemStateCallback**(SystemState.Callback callback) 设置相机系统运行状态监听（录像进行时长、拍照存储中等）|
|void|**formatSDCard**(FormatSDCardType type, CommonCallbacks.CompletionCallback callback) 格式化相机存储 SD 卡|
|void|**setHDLiveViewEnabled**(boolean enable, CommonCallbacks.CompletionCallback callback) 设置图传预览流高清画质开关（1080P / 720P）|
|void|**getHDLiveViewEnabled**(CommonCallbacks.CompletionCallbackWith&lt;Boolean&gt; callback) 获取图传高清预览流开关状态|
|boolean|**isOpticalZoomSupported**() 查询当前相机硬件是否支持连续光学变焦|
|void|**getOpticalZoomSpec**(CommonCallbacks.CompletionCallbackWith&lt;SettingsDefinitions.OpticalZoomSpec&gt; callback) 获取光学变焦倍率与焦距范围规格|
|void|**getOpticalZoomFocalLength**(CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 获取当前光学变焦焦距数值|
|void|**setDisplayMode**(SettingsDefinitions.DisplayMode displayMode, CommonCallbacks.CompletionCallback callback) 设置多光相机画面显示模式（THERMAL_ONLY 红外, VISUAL_ONLY 可见光, WAL 广角, ZL 变焦, PIP 分屏）|
|void|**getDisplayMode**(CommonCallbacks.CompletionCallbackWith&lt;SettingsDefinitions.DisplayMode&gt; callback) 获取当前多光相机画面显示模式|
|void|**setLaserRangingResultCallback**(LaserRangingResult.Callback callback) 设置激光测距实时结果回调（目标距离、目标经纬高）|
|void|**openLaserRanging**(CommonCallbacks.CompletionCallback callback) 开启激光测距|
|void|**closeLaserRanging**(CommonCallbacks.CompletionCallback callback) 关闭激光测距|
|void|**setZoom**(float ratioValue, CommonCallbacks.CompletionCallback callback) 设置数码变焦倍率|
|float|**getCurrentZoom**() 获取当前数码变焦倍率|
|void|**setVideoCodingFormat**(int format, CommonCallbacks.CompletionCallbackWith&lt;Integer&gt; callback) 设置视频编码格式（0:H264, 1:H265）|
|void|**setExposureCompensation**(SettingsDefinitions.ExposureCompensation ev, CommonCallbacks.CompletionCallback callback) 设置曝光补偿 EV 值|
|void|**getExposureCompensation**(CommonCallbacks.CompletionCallbackWith&lt;SettingsDefinitions.ExposureCompensation&gt; callback) 获取当前曝光补偿 EV 值|
|void|**setISO**(SettingsDefinitions.ISO iso, CommonCallbacks.CompletionCallback callback) 设置感光度 ISO|
|void|**getISO**(CommonCallbacks.CompletionCallbackWith&lt;SettingsDefinitions.ISO&gt; callback) 获取当前感光度 ISO|
|void|**setShutterSpeed**(SettingsDefinitions.ShutterSpeed shutterSpeed, CommonCallbacks.CompletionCallback callback) 设置快门速度|
|void|**getShutterSpeed**(CommonCallbacks.CompletionCallbackWith&lt;SettingsDefinitions.ShutterSpeed&gt; callback) 获取当前快门速度|
|MediaManager|**getMediaManager**() 获取相机媒体相册管理器|

#### **2.4.6.1 StorageState**

相机 SD 卡存储状态。

*包路径：`com.gdu.camera.StorageState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|int|**getTotalSpace**() 获取 SD 卡总存储容量（单位：MB）|
|int|**getRemainingSpace**() 获取 SD 卡当前剩余可用容量（单位：MB）|
|boolean|**isInserted**() SD 卡是否已正常插入检测到|
|boolean|**isFull**() SD 卡存储空间是否已满|
|boolean|**isFormatting**() SD 卡是否正在格式化中|
|boolean|**isFormatted**() SD 卡格式化操作是否成功完成|

#### **2.4.6.2 SystemState**

相机实时工作状态。

*包路径：`com.gdu.sdk.camera.SystemState`*

|**限定符和类型**|**方法和说明**|
|---|---|
|CameraMode|**getMode**() 获取当前相机模式（SHOOT_PHOTO / RECORD_VIDEO）|
|boolean|**isRecording**() 相机当前是否处于录像中|
|boolean|**isPhotoStored**() 照片文件是否已完全写入存储介质|
|int|**getCurrentVideoRecordingTimeInSeconds**() 当前录像持续时间（单位：秒）|

#### **2.4.6.3 MediaManager**

相机媒体库管理，提供相册列表获取、缩略图、预览图与原始大图/视频文件的分包高速下载。

*包路径：`com.gdu.sdk.camera.MediaManager`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**enable**(CommonCallbacks.CompletionCallback callback) 切换相机进入回放/媒体下载模式|
|void|**disable**(CommonCallbacks.CompletionCallback callback) 退出媒体下载模式恢复拍摄状态|
|void|**refreshMediaList**(CommonCallbacks.CompletionCallback callback) 刷新机载媒体文件列表索引|
|void|**getMediaFileList**(FileDownCallback.OnMediaListCallBack listener) 异步获取相机 SD 卡中的媒体文件列表（包含 MediaFile: 文件名、大小、时间戳、时长、文件类型等）|
|void|**getThumbnail**(String path, String saveDir, FileDownCallback.OnMediaFileCallBack callBack) 下载指定媒体文件的缩略图至本地目录|
|void|**getPreview**(String path, String saveDir, FileDownCallback.OnMediaFileCallBack callBack) 下载指定媒体文件的低分辨率预览图|
|void|**getRawImage**(String path, String saveDir, FileDownCallback.OnMediaFileCallBack callBack) 下载相机原始高清大图/视频文件（支持分块进度与断点保存）|

---

# **2.5、MISSION CLASSES**

### 2.5.1 MissionControl

`MissionControl` 航线与自主航迹任务总控制器。

*包路径：`com.gdu.sdk.mission.MissionControl`*

|**限定符和类型**|**方法和说明**|
|---|---|
|MissionControl|**getInstance**() 获取 MissionControl 单例实例|
|WaypointMissionOperator|**getWaypointMissionOperator**() 获取航点任务执行操作类|

### 2.5.2 WaypointMissionOperator

`WaypointMissionOperator` 航点任务执行器，负责航线任务的加载、校验、上传、开始、暂停、恢复、停止及全生命周期状态监听。

*包路径：`com.gdu.sdk.mission.waypoint.WaypointMissionOperator`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**addListener**(WaypointMissionOperatorListener operatorListener) 添加航点任务执行与上传进度监听器|
|void|**removeListener**(WaypointMissionOperatorListener operatorListener) 移除航点任务监听器|
|void|**loadMission**(WaypointMission mission) 将内存中的航点任务对象加载并转换为 KMZ 航线结构|
|void|**uploadMission**(CommonCallbacks.CompletionCallback completionCallback) 将已加载的航线文件高速上传至飞行器|
|void|**startMission**(CommonCallbacks.CompletionCallback completionCallback) 开始执行已就绪的航点任务|
|void|**startMissionWithTaskID**(long taskId, CommonCallbacks.CompletionCallbackWith&lt;String&gt; completionCallback) 携带指定业务 TaskID 开始航线任务（返回标准任务标识名称）|
|void|**pauseMission**(CommonCallbacks.CompletionCallback completionCallback) 暂停当前正在执行的航线任务（飞机空中悬停）|
|void|**resumeMission**(CommonCallbacks.CompletionCallback completionCallback) 恢复执行已暂停的航线任务|
|void|**stopMission**(CommonCallbacks.CompletionCallback completionCallback) 终止并退出当前航线任务|
|WaypointMissionState|**getCurrentState**() 获取当前航点任务状态（READY_TO_UPLOAD, UPLOADING, READY_TO_EXECUTE, EXECUTING, EXECUTION_PAUSED 等）|

#### **2.5.2.1 WaypointMission**

航线任务全局定义。

*包路径：`com.gdu.common.mission.waypoint.WaypointMission`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setMissionID**(int missionID) 设置航线任务数字 ID|
|void|**setWaypointCount**(int count) 设置航线包含的总航点数|
|void|**setMaxFlightSpeed**(float maxFlightSpeed) 设置航线允许的最大飞行速度（m/s）|
|void|**setAutoFlightSpeed**(float autoFlightSpeed) 设置航线巡航自动飞行速度（m/s）|
|void|**setFinishedAction**(WaypointMissionFinishedAction action) 设置航线完成后的动作（NO_ACTION 悬停, GO_HOME 直线返航, AUTO_LAND 自动降落, RETURN_ALONG_ROUTE 沿航迹返航, RETURN_TO_SAFE_POINT 安全点返航）|
|void|**setHeadingMode**(WaypointMissionHeadingMode mode) 设置机头航向模式（AUTO 自动指向下一航点, USING_INITIAL_DIRECTION 保持初始方向, USING_WAYPOINT_HEADING 使用各航点指定朝向）|
|void|**setWaypointList**(List&lt;Waypoint&gt; list) 设置航点集合列表|
|void|**setAltitudeMode**(int altitudeMode) 设置高程基准类型（0: 相对起飞点高度, 1: WGS84 椭球高, 2: ASL 海拔高度）|
|void|**setMissionExecutedWaypointCount**(int count) 设置断点续飞起始航点索引|

#### **2.5.2.2 Waypoint**

单个航路点配置。

*包路径：`com.gdu.common.mission.waypoint.Waypoint`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setCoordinate**(LocationCoordinate2D coordinate) 设置航点经纬度坐标|
|void|**setAltitude**(double altitude) 设置航点目标高度（单位：米）|
|void|**setHeading**(float heading) 设置到达该航点时的机头朝向角度（-180° ~ 180°）|
|void|**setAutoFlightSpeed**(float autoFlightSpeed) 设置该航段自定义飞行速度|
|void|**setGimbalPitch**(float gimbalPitch) 设置该航点关联的云台俯仰角度|
|void|**setWaypointActions**(List&lt;WaypointAction&gt; waypointActions) 设置到达该航点后触发的动作序列（拍照、录像、定点悬停等）|
|void|**setIsSafeReturnPoint**(int type) 标记该航点是否为安全返航中继点（0: 非安全点, 1: 安全点）|

---

# **2.6、MISC CLASSES**

### 2.6.1 CodecManager

`CodecManager` 视频硬件编解码管理组件，支持底层 Surface 硬解码渲染、RGBA/YUV 原始帧数据截取以及本地 MP4 录像录制。

*包路径：`com.gdu.sdk.codec.CodecManager`*

|**限定符和类型**|**方法和说明**|
|---|---|
|构造方法|**CodecManager**(Context context, SurfaceTexture surfaceTexture, int width, int height) 创建硬件解码渲染器|
|void|**sendDataToDecoder**(byte[] data, int len) 向硬件解码器输入 H264/H265 码流帧数据|
|void|**startPreview**(int streamIndex, Surface surface) 在指定 Surface 上开启预览|
|void|**stopPreview**(int streamIndex) 停止指定索引流的预览|
|void|**onSurfaceLayoutChanged**(int streamIndex, int width, int height) 通知布局尺寸变更|
|void|**registerSeiCallback**(MediaInterface.MediaSeiCallback callback) 注册 SEI 数据帧回调监听器|
|void|**onDestroy**() 销毁并释放解码资源|

### 2.6.2 Diagnostics & HMS

`Diagnostics` 与 `Hms` 飞行器健康管理系统，监控全机各传感器及硬件模块故障报警。

*包路径：`com.gdu.sdk.base.Diagnostics` & `com.gdu.sdk.hms.Hms`*

|**限定符和类型**|**方法和说明**|
|---|---|
|DiagnosticsType|**getType**() 获取故障所属模块类型（BATTERY, CAMERA, FLIGHT_CONTROLLER, GIMBAL, REMOTE_CONTROLLER, RTK, RADAR 等）|
|int|**getCode**() / **getSubCode**() 获取故障主错误码与子错误码|
|String|**getReason**() 获取故障原因详细文字描述|
|String|**getSolution**() 获取故障处理建议与解决指引|

### 2.6.3 LogManager

`LogManager` 飞行器与遥控器日志管理组件，用于拉取机载黑匣子日志索引并高速分片下载至移动端。

*包路径：`com.gdu.sdk.dronelog.LogManager`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**getAircraftLogList**(OnGetAircraftLogListener listener) 获取飞行器机载存储的日志文件列表|
|void|**downloadAircraftLog**(String saveLocalRootPath, MutableList&lt;BaseLogBean&gt; data, OnDownloadLogFileListener listener) 启动指定机载日志文件下载（包含 onStart, onProgress, onSuccess, onFailure 进度与结果回调）|

### 2.6.4 Vision

`Vision` AI 视觉识别与智能目标跟踪组件。

*包路径：`com.gdu.sdk.vision.Vision`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setOnTargetTrackListener**(OnTargetTrackListener listener) 注册 AI 目标检测与连续轨迹跟踪监听回调|
|void|**startSmartTrack**(CommonCallbacks.CompletionCallback callback) 开启智能跟踪模式|
|void|**stopSmartTrack**(CommonCallbacks.CompletionCallback callback) 停止智能跟踪模式|
|void|**detectTarget**(byte selectedType, short targetId, int pointX, int pointY, int pointX1, int pointY1, byte targetType, CommonCallbacks.CompletionCallback callback) 开始指定目标的检测与跟踪|
|void|**cancelSmartTrack**(CommonCallbacks.CompletionCallback callback) 取消当前智能跟踪|

### 2.6.5 Radar

`Radar` 毫米波与超声雷达避障感知组件。

*包路径：`com.gdu.sdk.radar.Radar`*

|**限定符和类型**|**方法和说明**|
|---|---|
|void|**setRadarPerceptionInformationCallback**(CompletionCallbackWith&lt;PerceptionInformation&gt; callback) 注册雷达点云与全向障碍物距离分布感知回调（包含各角度障碍物距离与上下视测距）|

### 2.6.6 PSDKManager & Megaphone

`PSDKManager` 与 `Megaphone` 载荷扩展接口与智能喊话器控制组件。

*包路径：`com.gdu.sdk.psdk.PSDKManager` & `com.gdu.sdk.psdk.Megaphone`*

|**限定符和类型**|**方法和说明**|
|---|---|
|PSDKManager|**getInstance**() 获取 PSDK 管理器单例|
|void|**getCustomView**() 请求并解析机载 PSDK 设备的自定义 UI 控件布局 JSON 与图标资源|
|void|**setPSDKWidgetState**(int iconId, byte type, int value, CompletionCallback callback) 控制 PSDK 载荷自定义控件状态|
|void|**getPSDKWidgetState**(int iconId, byte type, CompletionCallbackWith&lt;Integer&gt; callback) 查询 PSDK 载荷自定义控件状态|
|void|**playText**(String text) 播放 TTS 文字语音|
|void|**playVoice**(String path) 播放指定索引的音频文件|
|void|**setStateListener**(OnMegaPhoneStateListener listener) 设置喊话器状态监听|

### 2.6.7 Pipeline

`Pipeline` MOP (Mobile Onboard Payload) 载荷双向高速数据传输管道组件。

*包路径：`com.gdu.sdk.mop.Pipeline`*

|**限定符和类型**|**方法和说明**|
|---|---|
|int|**writeData**(byte[] data, int offset, int length, CompletionCallback callback) 向机载载荷设备透传写入自定义字节流数据|
|int|**readData**(byte[] buff, int offset, int length) 从载荷传输管道读取返回数据|
|void|**setOnPipelineDataListener**(PipelineDataListener listener) 设置载荷管道数据接收监听（onDataReceived, onError, onDisconnected）|

### 2.6.8 TargetLocationUtil

`TargetLocationUtil` 目标地理定位解算工具类。

*包路径：`com.gdu.library.TargetLocationUtil`*

|**限定符和类型**|**方法和说明**|
|---|---|
|TargetLocationResult|**calTargetLocation**(LocationInParam param) 根据输入的飞行器与云台图像参数计算目标的空间定位坐标|

---

# **2.7 机场/机库开发与使用说明**

#### 2.7.1 初始化配置

1. 在初始化注册 SDK 时，设置场景为机巢模式：
   ```java
   SDKManager.getInstance().setConnectScene(ConnectScene.HANGAR);
   SDKManager.getInstance().registerApp(context, callback);
   ```

2. 开启无遥控器持续中值发送：
   由于机库场景下通常无物理遥控器硬件摇杆连接，需调用：
   ```java
   aircraft.getRemoteController().setContinueSendRCControlMidValueEnable(true, callback);
   ```
   可有效避免飞行器起飞后误触发摇杆丢失降落或航线中断的问题。

#### 2.7.2 飞机机巢模式与备降点参数配置

通过 `FlightController.setAircraftOperationScenarioMode(AircraftOperationScenarioInfo mode, callback)` 配置：

1. `aircraftMode`: 必须设置为 `AircraftOperationScenarioMode.AIRPORT_MODE`（1: 机巢模式）；
2. `alternateLandingPointValid`: 备降点有效标志必须设为 `true`；
3. `alternateLandingPointLongitude` 与 `alternateLandingPointLatitude`: 必须传入有效的备降点经纬度坐标；
4. `dockType`: 设置实际机库型号（1=K01, 2=K02, 3=K03, 5=K05, 21=K02P, 31=K03P, 51=K05P）。

#### 2.7.3 机场场景返航与降落流程

1. 机场返航调用 `FlightController.exactBack(...)` 或 `FlightController.carNestExactBack(...)`；
2. 降落阶段分为高空进近、二维码靶标视觉识别定位与精准对准降落；
3. 当发生大风或标靶遮挡异常时，系统最多支持 3 次自动复飞重试；若 3 次对准仍失败，将自动直飞预设备降点安全降落。
