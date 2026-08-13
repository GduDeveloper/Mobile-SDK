package com.gdu.demo.widget.rc.key

import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.interfaces.IGduRCDevice
import com.gdu.msdk.key.callback.MSdkCallback
import com.gdu.msdk.key.value.rc.CycleRCRocker

/**
 * 遥控器实体按键响应动作管理类
 *
 * @author: guoyang
 * @date: 2025/6/3
 */
object RCCustomKeyActionManager {

    /**
     * 是否已初始化
     */
    @Volatile
    private var isInitialized = false

    /**
     * 是否触发了组合按键事件
     */
    private var isMultiAction = false

    private fun doAction(actionId: Int) {
        log("doAction = $actionId")
        CameraAction.doAction(actionId)
        GimbalAction.doAction(actionId)
        AppAction.doAction(actionId)
        FCAction.doAction(actionId)
    }

    private fun log(msg: String?) {
        XLogger.APP.i("RCKeyAction", "$msg")
    }

    fun init() {
        if (isInitialized) {
            return
        }
        isInitialized = true
        log("RCCustomKeyActionManager init")
        IGduRCDevice.get.cycleRCRockerInfo.listen(object : MSdkCallback.KeyListener<CycleRCRocker>{
            override fun onValueChange(oldValue: CycleRCRocker?, newValue: CycleRCRocker?) {
                val keyBean = newValue?.key ?: return

                // TODO GUOYANG 北斗短报文sos逻辑

                if (keyBean.isSame(oldValue?.key)) {
                    // 按键状态没有变化
                    return
                }

                val keyUpEvents = keyBean.getKeyUpEvents(oldValue?.key)
                if (keyUpEvents.isEmpty()) {
                    // 没有抬起事件
                    isMultiAction = false
                    return
                }

                if (keyUpEvents.size == 1) {
                    //只有单个按键抬起
                    if (isMultiAction) {
                        isMultiAction = false
                        return
                    }
                    val key = keyUpEvents[0]

                    if (key == IActionViewKey.VIEW_C1) {
//                        if (RouteBox.getCurrentPluginType() == ApiPluginType.PLUGIN_TASK_RECORDING
//                            && RouteBox.onC1KeyClick()) {
//                            //如果是航迹任务录制，并且消费C1按键事件直接返回
//                            return
//                        }
                    }

                    log("single Key = $key")

                    doAction(
                        RCCustomKeyRepository.findActionIdByRCKeyId(key)
                    )

                } else {
                    //多个按键抬起
                    // 是否有定义的组合按钮按下
                    val key1 = keyUpEvents[0]
                    val key2 = keyUpEvents[1]

                    // TODO GUOYANG 北斗短报文sos逻辑 不响应L1 R1

                    // 匹配自定义组件1是否响应
                    checkDiyKeyPress(key1, key2, IDiyViewKey.VIEW_DIY1_LEFT, IDiyViewKey.VIEW_DIY1_RIGHT) {
                        log("diy Key = VIEW_DIY1")
                        doAction(
                            RCCustomKeyRepository.findActionIdByRCKeyId(IActionViewKey.VIEW_DIY1)
                        )
                    }

                    // 匹配自定义组件2是否响应
                    checkDiyKeyPress(key1, key2, IDiyViewKey.VIEW_DIY2_LEFT, IDiyViewKey.VIEW_DIY2_RIGHT) {
                        log("diy Key = VIEW_DIY2")
                        doAction(
                            RCCustomKeyRepository.findActionIdByRCKeyId(IActionViewKey.VIEW_DIY2)
                        )
                    }

                    // 匹配自定义组件3是否响应
                    checkDiyKeyPress(key1, key2, IDiyViewKey.VIEW_DIY3_LEFT, IDiyViewKey.VIEW_DIY3_RIGHT) {
                        log("diy Key = VIEW_DIY3")
                        doAction(
                            RCCustomKeyRepository.findActionIdByRCKeyId(IActionViewKey.VIEW_DIY3)
                        )
                    }
                }
            }
        })
    }

    private fun checkDiyKeyPress(key1: Int, key2: Int, leftViewId: Int, rightViewId: Int, call: ()-> Unit) {
        val leftKey = transformLeftDiyRCKeyToActionKey(
            RCCustomKeyRepository.findDiyRCKeyByDiyView(leftViewId)
        )
        val rightKey = transformRightDiyRCKeyToActionKey(
            RCCustomKeyRepository.findDiyRCKeyByDiyView(rightViewId)
        )

        if ((leftKey == key1 && rightKey == key2)
                || (leftKey == key2 && rightKey == key1)) {
            isMultiAction = true
            call.invoke()
        }
    }


}