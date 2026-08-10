package com.gdu.demo.widget.rc.key

import com.gdu.lib.util.core.XLogger

/**
 *
 * @author: guoyang
 * @date: 2025/7/28
 */
interface IKeyAction {

    fun doAction(actionId: Int)

    fun log(msg: String?) {
        XLogger.APP.i("RCKeyAction", "$msg")
    }
}