package com.gdu.ux.core.base.notify

/**
 * @author wuqb
 * @date 2024/11/17
 * @description 观察者
 */
interface Observer {
    /** 通知数据更新 */
    fun notifyUpdate(t:Any?)
}
