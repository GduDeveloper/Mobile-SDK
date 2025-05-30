package com.gdu.demo.widgetlist.core.base.widget

import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


/**
 * @author wuqb
 * @date 2024/11/9
 * @description 通用Widget Model, 将被所有的WidgetModel继承
 */
abstract class WidgetModel: CoroutineScope by MainScope() {

    protected var disposable: Disposable? = null
    //数据变更通知
    private val _dataState = MutableStateFlow<Any?>(null)
    val dataState: StateFlow<Any?> = _dataState.asStateFlow()

    @Synchronized
    open fun start() {
        initializeConnection()
        onStart()
    }

    private fun initializeConnection(){

    }

    /**
     * 通知数据刷新
     * @param data 数据内容
     * */
    protected fun notify(data:Any){
        _dataState.value = data
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
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
            disposable = null
        }
        onDestroy()
    }

    abstract fun onStart()

    open fun onDestroy(){}
}