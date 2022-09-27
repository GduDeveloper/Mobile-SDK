# GDU SDK for Android

## SDK简介
GDU无人机Android SDK目前开放了无人机控制，挂载云台（8K,
双光云台，1红外双光云台，四光云台）控制；实现实时图传显示，飞行信息反馈，航迹飞行等功能接口。便于开发者完成基于自身场景的更深层、更个性化飞行器开发需求。

## SDK集成
### 添加依赖文件
~~~xml
1.导入SDK开发包GduLibrary-*.*.*.jar和xstream-1.4.11-java7.jar到libs目录； 
  导入libCRtp.so和libresrtmp.so到libs下的arm64-v8a目录
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
### 1.飞行参数配置
    (1).GduDroneApi: SDK接口初始化，提供飞行器连接，关闭；飞行器信息监听；飞行器校磁；
    云台相机参数设置和获取等接口
    (2).GduSettingManager: 遥控器控制手切换，限高限距设置；
    (3).Gimbal:云台设置，设置/获取分辨率等,云台角度控制。
### 2.飞行控制交互接口 
    (1).GduControlManager: 飞机基本控制,起飞，降落，返航
    (2).GduMapManager:提供电子围栏点上传，及围栏清除等功能
    (3).RoutePlanManager:航迹功能
### 3.飞机实时图像
    (1).GduPlayView:主要功能是实时显示飞机端图传画面，提供开始，暂停预览, 飞行器拍照，录像等等功能
    (2).CustomRTMPSender：云端推流  
    
## 支持
   有问题可以联系dev@gdu.com 
   
   
