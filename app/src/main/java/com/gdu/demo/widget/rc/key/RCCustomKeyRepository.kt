package com.gdu.demo.widget.rc.key

import com.gdu.demo.R
import com.gdu.lib.base.GduEnvConfig
import com.gdu.lib.util.MMKVUtils
import com.gdu.lib.util.ThreadHelper
import com.gdu.lib.util.core.GsonUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.interfaces.IGduDroneDevice
import com.google.gson.reflect.TypeToken

/**
 *
 * @author: guoyang
 * @date: 2025/5/31
 */
object RCCustomKeyRepository {

    private const val MMKV_KEY_RC_SINGLE_KEY_ACTION = "MMKV_KEY_RC_SINGLE_KEY_ACTION"
    private const val MMKV_KEY_DIY_VIEW_RC_KEY = "MMKV_KEY_DIY_VIEW_RC_KEY"

    // 是否已初始化
    @Volatile
    private var isInitialized = false

    // 不同类型的action；actionId - actionName
    val cameraActionList by lazy { getCameraActions() }
    val gimbalActionList by lazy { getGimbalActions() }
    val appActionList by lazy { getAppActions() }
    val fcActionList by lazy { getFCActions() }

    // 所有的action和name的map
    private val allActionNameMap = mutableMapOf<Int, String>()

    // actionID和遥控器按键ID的对应关系
    private var rcKeyActionIdMap = HashMap<Int, Int>()

    // diy组合按键的viewId以及遥控器按键Id的对应关系
    private var diyViewIdAndRCKeyIdMap = HashMap<Int, Int>()

    /**
     * 前置初始化
     */
    fun init() {
        if (isInitialized) {
            return
        }
        isInitialized = true
        loadSingleKeyActionMap()
        loadDiyViewAndRCKeyMap()
    }

    /**
     * 查询actionId对应的描述
     */
    fun findActionNameById(actionId: Int): String {
        if (allActionNameMap.isEmpty()) {
            cameraActionList.forEach { allActionNameMap[it.actionId] = it.actionName }
            gimbalActionList.forEach { allActionNameMap[it.actionId] = it.actionName }
            appActionList.forEach { allActionNameMap[it.actionId] = it.actionName }
            fcActionList.forEach { allActionNameMap[it.actionId] = it.actionName }
        }
        return allActionNameMap[actionId]?: GduEnvConfig.application.getString(R.string.string_rc_key_no)
    }

    /**
     * 查询动作ID
     */
    fun findActionIdByRCKeyId(rcKeyId: Int): Int {
        return rcKeyActionIdMap[rcKeyId]?: ICustomAction.KEY_UNDEFINED
    }

    /**
     * 查询组件按钮View绑定的遥控器按键
     */
    fun findDiyRCKeyByDiyView(diyViewId: Int): Int {
        return diyViewIdAndRCKeyIdMap[diyViewId]?: 0
    }

    /**
     * 改变按键的动作行为
     */
    fun changeRCKeyAction(rcKeyId: Int, actionId: Int) {
        rcKeyActionIdMap[rcKeyId] = actionId
        saveActionRCKey()
    }

    /**
     * 改变遥控器按键的组合
     */
    fun changeDiyViewAndRCKey(diyViewId: Int, rcKeyId: Int) {
        diyViewIdAndRCKeyIdMap[diyViewId] = rcKeyId
        saveDiyViewAndRCKey()
    }

    private fun loadSingleKeyActionMap() {
        val singleKeyActionStr = MMKVUtils.getKV().getString(MMKV_KEY_RC_SINGLE_KEY_ACTION, "")
        if (singleKeyActionStr.isEmpty()) {
            initDefaultActionRCKey()
            saveActionRCKey()
        } else {
            loadActionRCKeyMap(singleKeyActionStr)
        }
    }

    private fun loadDiyViewAndRCKeyMap() {
        val diyViewRcKeyString = MMKVUtils.getKV().getString(MMKV_KEY_DIY_VIEW_RC_KEY, "")
        if (diyViewRcKeyString.isEmpty()) {
            initDefaultDiyViewRCKeyMap()
            saveDiyViewAndRCKey()
        } else {
            loadDiyViewAndRCKey(diyViewRcKeyString)
        }
    }

    private fun saveActionRCKey() {
        ThreadHelper.runOnAsync {
            val json = GsonUtils.toJson(rcKeyActionIdMap)
            MMKVUtils.getKV().putString(MMKV_KEY_RC_SINGLE_KEY_ACTION, json)
            XLogger.APP.i("RCKeyAction", "saveActionRCKey json:$json")
        }
    }

    private fun saveDiyViewAndRCKey() {
        ThreadHelper.runOnAsync {
            val json = GsonUtils.toJson(diyViewIdAndRCKeyIdMap)
            MMKVUtils.getKV().putString(MMKV_KEY_DIY_VIEW_RC_KEY, json)
            XLogger.APP.i("RCKeyAction", "saveDiyViewAndRCKey json:$json")
        }
    }

    private fun loadActionRCKeyMap(json: String) {
        ThreadHelper.runOnAsync {
            rcKeyActionIdMap = GsonUtils.fromJson(json, object : TypeToken<HashMap<Int, Int>>() {}.type)
        }
    }

    private fun loadDiyViewAndRCKey(json: String) {
        ThreadHelper.runOnAsync {
            diyViewIdAndRCKeyIdMap = GsonUtils.fromJson(json, object : TypeToken<HashMap<Int, Int>>() {}.type)
        }
    }

    private fun getCameraActions(): MutableList<RCKeyActionItem> {
        val planType = IGduDroneDevice.get.planType.value
        val list = mutableListOf<RCKeyActionItem>()
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_ZOOM_IN, GduEnvConfig.application.getString(R.string.string_rc_key_camera_enlarge)))
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_ZOOM_OUT, GduEnvConfig.application.getString(R.string.string_rc_key_camera_narrow)))
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_INCREASE_EV, GduEnvConfig.application.getString(R.string.string_rc_key_camera_add_ev)))
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_DECREASE_EV, GduEnvConfig.application.getString(R.string.string_rc_key_camera_sub_ev)))
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_SWITCH_MODE, GduEnvConfig.application.getString(R.string.string_rc_key_camera_change_mode)))
        if (!planType.isS200Type() && !planType.isP300()) {
            list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_OPEN_FFC, GduEnvConfig.application.getString(R.string.string_ffc)))
        }
        list.add(RCKeyActionItem(ICustomAction.KEY_CAMERA_SWITCH_TEMP_ALARM, GduEnvConfig.application.getString(R.string.string_high_temp_warn)))
        list.add(RCKeyActionItem(ICustomAction.KEY_UNDEFINED, GduEnvConfig.application.getString(R.string.string_rc_key_no)))
        return list
    }


    private fun getGimbalActions(): MutableList<RCKeyActionItem> {
        val planType = IGduDroneDevice.get.planType.value
        val list = mutableListOf<RCKeyActionItem>()
        list.add(RCKeyActionItem(ICustomAction.KEY_GIMBAL_RECENTER, GduEnvConfig.application.getString(R.string.string_rc_key_gimbal_center)))
        list.add(RCKeyActionItem(ICustomAction.KEY_GIMBAL_DOWN, GduEnvConfig.application.getString(R.string.string_rc_key_gimbal_down)))
        if (!planType.isS200Type() && !planType.isP300()) {
            list.add(RCKeyActionItem(ICustomAction.KEY_GIMBAL_CENTER_DOWN, GduEnvConfig.application.getString(R.string.string_rc_key_gimbal_center_down)))
            list.add(RCKeyActionItem(ICustomAction.KEY_GIMBAL_PITCH_ANGLE_RECENTER, GduEnvConfig.application.getString(R.string.string_rc_key_gimbal_pitch_center)))
            list.add(RCKeyActionItem(ICustomAction.KEY_GIMBAL_YAW_ANGLE_RECENTER, GduEnvConfig.application.getString(R.string.string_rc_key_gimbal_couser_center)))
        }
        list.add(RCKeyActionItem(ICustomAction.KEY_UNDEFINED, GduEnvConfig.application.getString(R.string.string_rc_key_no)))
        return list
    }


    private fun getAppActions(): MutableList<RCKeyActionItem> {
        val list = mutableListOf<RCKeyActionItem>()
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_CHANGE_MAP, GduEnvConfig.application.getString(R.string.string_rc_key_app_change_map)))
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_ADD_TARGET_POINT, GduEnvConfig.application.getString(R.string.string_add_target_point)))
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_DELETE_SELECTED_POINT, GduEnvConfig.application.getString(R.string.string_delete_selected_point)))
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_SELECTED_NEXT_POINT, GduEnvConfig.application.getString(R.string.string_selected_next_point)))
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_SELECTED_PREVIOUS_POINT, GduEnvConfig.application.getString(R.string.string_selected_last_target)))
        list.add(RCKeyActionItem(ICustomAction.KEY_APP_LOOK_FOR_TARGET, GduEnvConfig.application.getString(R.string.string_look_for_target)))
        list.add(RCKeyActionItem(ICustomAction.KEY_UNDEFINED, GduEnvConfig.application.getString(R.string.string_rc_key_no)))
        return list
    }


    private fun getFCActions(): MutableList<RCKeyActionItem> {
        val list = mutableListOf<RCKeyActionItem>()
        list.add(RCKeyActionItem(ICustomAction.KEY_FC_SWITCH_NIGHT_LIGHT, GduEnvConfig.application.getString(R.string.string_rc_key_flight_control_night_light)))
        list.add(RCKeyActionItem(ICustomAction.KEY_FC_UPDATE_HOME_AIR, GduEnvConfig.application.getString(R.string.string_rc_key_flight_control_update_home_air)))
        list.add(RCKeyActionItem(ICustomAction.KEY_FC_UPDATE_HOME_RC, GduEnvConfig.application.getString(R.string.string_rc_key_flight_control_update_home_rc)))
        list.add(RCKeyActionItem(ICustomAction.KEY_UNDEFINED, GduEnvConfig.application.getString(R.string.string_rc_key_no)))
        return list
    }


    private fun initDefaultActionRCKey() {
        rcKeyActionIdMap.clear()
        rcKeyActionIdMap[IActionViewKey.VIEW_C1] = ICustomAction.KEY_GIMBAL_RECENTER
        rcKeyActionIdMap[IActionViewKey.VIEW_C2] = ICustomAction.KEY_GIMBAL_DOWN
        rcKeyActionIdMap[IActionViewKey.VIEW_L1] = ICustomAction.KEY_APP_CHANGE_MAP
        rcKeyActionIdMap[IActionViewKey.VIEW_L2] = ICustomAction.KEY_CAMERA_SWITCH_MODE
        rcKeyActionIdMap[IActionViewKey.VIEW_R1] = ICustomAction.KEY_CAMERA_ZOOM_IN
        rcKeyActionIdMap[IActionViewKey.VIEW_R2] = ICustomAction.KEY_CAMERA_ZOOM_OUT
        rcKeyActionIdMap[IActionViewKey.VIEW_FIVE_UP] = ICustomAction.KEY_APP_SELECTED_PREVIOUS_POINT
        rcKeyActionIdMap[IActionViewKey.VIEW_FIVE_BOTTOM] = ICustomAction.KEY_APP_SELECTED_NEXT_POINT
        rcKeyActionIdMap[IActionViewKey.VIEW_FIVE_LEFT] = ICustomAction.KEY_APP_ADD_TARGET_POINT
        rcKeyActionIdMap[IActionViewKey.VIEW_FIVE_RIGHT] = ICustomAction.KEY_APP_DELETE_SELECTED_POINT
        rcKeyActionIdMap[IActionViewKey.VIEW_FIVE_CENTER] = ICustomAction.KEY_APP_LOOK_FOR_TARGET
        rcKeyActionIdMap[IActionViewKey.VIEW_DIY1] = ICustomAction.KEY_UNDEFINED
        rcKeyActionIdMap[IActionViewKey.VIEW_DIY2] = ICustomAction.KEY_UNDEFINED
        rcKeyActionIdMap[IActionViewKey.VIEW_DIY3] = ICustomAction.KEY_UNDEFINED
    }

    private fun initDefaultDiyViewRCKeyMap() {
        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY1_LEFT] = IDiyRCKey.RC_KEY_LEFT_C1
        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY2_LEFT] = IDiyRCKey.RC_KEY_LEFT_L1
        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY3_LEFT] = IDiyRCKey.RC_KEY_LEFT_L2

        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY1_RIGHT] = IDiyRCKey.RC_KEY_RIGHT_C2
        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY2_RIGHT] = IDiyRCKey.RC_KEY_RIGHT_R1
        diyViewIdAndRCKeyIdMap[IDiyViewKey.VIEW_DIY3_RIGHT] = IDiyRCKey.RC_KEY_RIGHT_R2
    }
}