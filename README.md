# GDU SDK for Android

## SDK简介
GDU无人机Android SDK目前开放了无人机控制，挂载云台（4K,
10倍，30倍，红外）控制；实现实时图传显示，飞行信息反馈，航迹飞行等功能。便于开发者完成基于自身场景的更深层、更个性化飞行器开发需求。

## SDK集成
### 添加依赖文件
~~~xml
1.添加jar包
 implementation fileTree(dir: 'libs', include: ['*.jar'])
2.添加so库
sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
~~~
## SDK Demo运行
下载GduSDKDemo，