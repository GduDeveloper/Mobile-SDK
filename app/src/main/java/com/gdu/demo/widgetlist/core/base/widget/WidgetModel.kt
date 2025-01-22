package com.gdu.demo.widgetlist.core.base.widget

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


/**
 * @author wuqb
 * @date 2024/11/9
 * @description 通用Widget Model, 将被所有的WidgetModel继承
 */
abstract class WidgetModel: CoroutineScope by MainScope() {
    //数据变更通知
    val dataChangeChannel = Channel<Any>(Channel.CONFLATED)


    fun intervalFlow(period: Long, initialDelay: Long = 0) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }.flowOn(Dispatchers.IO)

    @Synchronized
    open fun start() {
        initializeConnection()
        onStart()
    }

    private fun initializeConnection(){

    }

    /**
     * 通知数据刷新
     * @param channel 数据刷新通道
     * @param data 数据内容
     * */
    protected fun notify(channel: Channel<Any>, data:Any){
        launch {
            channel.send(data)
        }
    }

    /**
     * 判断设备是否未连接病给予提示
     *
     * @return true 未连接
     */
    fun isConnect(): Boolean {
        return GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess
    }

    @Synchronized
    fun destroy(){
        onDestroy()
    }

    abstract fun onStart()

    abstract fun onDestroy()
}