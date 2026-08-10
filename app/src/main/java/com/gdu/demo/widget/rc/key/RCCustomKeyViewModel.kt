package com.gdu.demo.widget.rc.key

import androidx.lifecycle.ViewModel
import com.gdu.msdk.device.component.interfaces.IFlightController
import com.gdu.msdk.device.interfaces.IGduDroneDevice

/**
 *
 * @author: guoyang
 * @date: 2025/5/26
 */
class RCCustomKeyViewModel: ViewModel(){


    fun findActionNameByRCKey(rcKeyId: Int): String {
        return RCCustomKeyRepository.findActionNameById(
            RCCustomKeyRepository.findActionIdByRCKeyId(rcKeyId)
        )
    }

    fun findDiyRCKeyByDiyView(diyViewId: Int): Int {
        return RCCustomKeyRepository.findDiyRCKeyByDiyView(diyViewId)
    }

    /**
     * 绑定viewKey以及ActionKey
     * @param rcKeyId IActionViewKey
     * @param actionId ICustomAction
     */
    fun saveViewAndAction(rcKeyId: Int, actionId: Int) {
        RCCustomKeyRepository.changeRCKeyAction(rcKeyId, actionId)

        if (rcKeyId == IActionViewKey.VIEW_C1 || rcKeyId == IActionViewKey.VIEW_C2) {
            if (IGduDroneDevice.get.isConnected) {
                // c1 c2 取消之前的自定义按钮
                IFlightController.get.clearRCC1C2Key()
            }
        }
    }

    /**
     * 检查3个组合键是否相等
     */
    fun isSameDiyKey(bindViewKey: Int, pos: Int): Boolean {
        val ints1 = IntArray(2)
        ints1[0] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY1_LEFT)
        ints1[1] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY1_RIGHT)
        val ints2 = IntArray(2)
        ints2[0] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY2_LEFT)
        ints2[1] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY2_RIGHT)
        val ints3 = IntArray(2)
        ints3[0] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY3_LEFT)
        ints3[1] = findDiyRCKeyByDiyView(IDiyViewKey.VIEW_DIY3_RIGHT)

        when(bindViewKey) {
            IDiyViewKey.VIEW_DIY1_LEFT -> { ints1[0] = pos }
            IDiyViewKey.VIEW_DIY1_RIGHT -> { ints1[1] = pos }
            IDiyViewKey.VIEW_DIY2_LEFT -> { ints2[0] = pos }
            IDiyViewKey.VIEW_DIY2_RIGHT -> { ints2[1] = pos }
            IDiyViewKey.VIEW_DIY3_LEFT -> { ints3[0] = pos }
            IDiyViewKey.VIEW_DIY3_RIGHT -> { ints3[1] = pos }
        }
        return ints1.contentEquals(ints2) || ints1.contentEquals(ints3) || ints2.contentEquals(ints3)
    }

    fun saveDiyViewAndRCKey(diyViewId: Int, rcKeyId: Int) {
        RCCustomKeyRepository.changeDiyViewAndRCKey(diyViewId, rcKeyId)
    }

}