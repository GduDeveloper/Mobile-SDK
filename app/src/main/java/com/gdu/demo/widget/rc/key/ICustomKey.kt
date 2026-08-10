package com.gdu.demo.widget.rc.key

/**
 * 所有的动作
 */
interface ICustomAction {
    companion object {
        // 未定义
        const val KEY_UNDEFINED: Int = 0

        // ----------- 相机动作KEY --------------
        // 变焦放大
        const val KEY_CAMERA_ZOOM_IN: Int = 1
        // 变焦缩小
        const val KEY_CAMERA_ZOOM_OUT: Int = 2
        // 增加EV值
        const val KEY_CAMERA_INCREASE_EV: Int = 3
        // 减小EV值
        const val KEY_CAMERA_DECREASE_EV: Int = 4
        // 切换画面显示模式
        const val KEY_CAMERA_SWITCH_MODE: Int = 5
        // open ffc
        const val KEY_CAMERA_OPEN_FFC: Int = 7
        // 开启/关闭高温报警
        const val KEY_CAMERA_SWITCH_TEMP_ALARM: Int = 8

        // ----------- 云台动作KEY --------------
        // 云台回中
        const val KEY_GIMBAL_RECENTER: Int = 101
        // 云台朝下
        const val KEY_GIMBAL_DOWN: Int = 102
        // 云台回中朝下
        const val KEY_GIMBAL_CENTER_DOWN: Int = 103
        // 云台俯仰回中
        const val KEY_GIMBAL_PITCH_ANGLE_RECENTER: Int = 104
        // 云台偏航回中
        const val KEY_GIMBAL_YAW_ANGLE_RECENTER: Int = 105

        // ----------- App动作KEY --------------
        // 地图/图传画面切换
        const val KEY_APP_CHANGE_MAP: Int = 202
        // 新增目标点
        const val KEY_APP_ADD_TARGET_POINT: Int = 203
        // 删除选中目标点
        const val KEY_APP_DELETE_SELECTED_POINT: Int = 204
        // 选中下一个目标点
        const val KEY_APP_SELECTED_NEXT_POINT: Int = 205
        // 选中上一个目标点
        const val KEY_APP_SELECTED_PREVIOUS_POINT: Int = 206
        // 看向目标
        const val KEY_APP_LOOK_FOR_TARGET: Int = 207

        // ----------- 飞控动作KEY --------------
        // 夜航灯开启/关闭
        const val KEY_FC_SWITCH_NIGHT_LIGHT: Int = 301
        // 更新HOME点（当前飞行器位置）
        const val KEY_FC_UPDATE_HOME_AIR: Int = 302
        // 更新HOME点（当前遥控器位置）
        const val KEY_FC_UPDATE_HOME_RC: Int = 303

    }
}

/**
 * 显示动作Action的View
 */
interface IActionViewKey {
    companion object{
        // 单键
        const val VIEW_C1 = 1001
        const val VIEW_C2 = 1002
        const val VIEW_L1 = 1003
        const val VIEW_L2 = 1004
        const val VIEW_R1 = 1005
        const val VIEW_R2 = 1006
        const val VIEW_FIVE_UP = 1007
        const val VIEW_FIVE_BOTTOM = 1008
        const val VIEW_FIVE_LEFT = 1009
        const val VIEW_FIVE_RIGHT = 1010
        const val VIEW_FIVE_CENTER = 1011
        // 组合按键（只包含c1\c2\l1\l2\r1\r2）
        const val VIEW_DIY1 = 1101
        const val VIEW_DIY2 = 1102
        const val VIEW_DIY3 = 1103
    }
}

/**
 * 显示RC按键的View
 */
interface IDiyViewKey {
    companion object{
        const val VIEW_DIY1_LEFT = 2001
        const val VIEW_DIY1_RIGHT = 2002

        const val VIEW_DIY2_LEFT = 2011
        const val VIEW_DIY2_RIGHT = 2012

        const val VIEW_DIY3_LEFT = 2021
        const val VIEW_DIY3_RIGHT = 2022
    }
}

/**
 * 显示RC按键的View
 */
interface IDiyRCKey {
    companion object{
        const val RC_KEY_LEFT_C1 = 0
        const val RC_KEY_LEFT_L1 = 1
        const val RC_KEY_LEFT_L2 = 2

        const val RC_KEY_RIGHT_C2 = 0
        const val RC_KEY_RIGHT_R1 = 1
        const val RC_KEY_RIGHT_R2 = 2
    }
}


fun transformLeftDiyRCKeyToActionKey(diyRCKey: Int): Int {
    return when(diyRCKey) {
        IDiyRCKey.RC_KEY_LEFT_L1 -> {
            IActionViewKey.VIEW_L1
        }
        IDiyRCKey.RC_KEY_LEFT_L2 -> {
            IActionViewKey.VIEW_L2
        }
        else -> {
            IActionViewKey.VIEW_C1
        }
    }
}

fun transformRightDiyRCKeyToActionKey(diyRCKey: Int): Int {
    return when(diyRCKey) {
        IDiyRCKey.RC_KEY_RIGHT_R1 -> {
            IActionViewKey.VIEW_R1
        }
        IDiyRCKey.RC_KEY_RIGHT_R2 -> {
            IActionViewKey.VIEW_R2
        }
        else -> {
            IActionViewKey.VIEW_C2
        }
    }
}

