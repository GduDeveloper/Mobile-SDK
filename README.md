# GDU SDK for Android

## SDK简介
GDU无人机Android SDK目前开放了无人机控制，挂载云台（8K,
双光云台，1红外双光云台，四光云台）控制；实现实时图传显示，飞行信息反馈，航迹飞行等功能接口。便于开发者完成基于自身场景的更深层、更个性化飞行器开发需求。


## SDK版本更新记录

   [完整更新记录](https://github.com/GduDeveloper/Mobile-SDK/blob/master/document/UPDATE_VERSION.md)

## SDK集成
### 添加依赖文件
~~~xml
1.导入SDK开发包GduLibrary-*.*.*.jar到libs目录；
  导入libCRtp.so和librtmp.so到libs下的arm64-v8a和armeabi-v7a目录
2.添加jar包
 implementation fileTree(dir: 'libs', include: ['*.jar'])
3.添加so库
sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
~~~
## 提供开发接口
### 1.
    (1).GDUSDKManager: SDK提供注册，飞行器连接，关闭
    云台相机参数设置和获取等接口
    (2).GDUAircraft: 获取无人机系统各组件；
    (3).GDUFlightController:提供飞控相关接口。
    (4).GDUCamera:提供相机相关接口
    (5).GDUGimbal:提供云台相关接口
    (6).GDUBattery:提供电池相关接口
    (7).GDURemoteController:提供遥控器相关接口
    (8).GDURadar:提供雷达相关接口
    (9).GDUAirlink:提供图传链路相关接口
    (10).Mission:提供任务相关接口；包含如下功能：
        A.WaypointMission:航点任务
        B.FollowMeMission:GPS跟随
        C.HotpointMission:GPS环绕
    (11).GDUCodecManager:解码相关接口
    (12).GDUDiagnostics:异常提示相关接口
    
## 支持
   有问题可以联系dev@gdu-tech.com

