package com.gdu.ux.core.base.notify


/**
 * @author wuqb
 * @date 2024/11/17
 * @description TODO
 */
class Observable{

    companion object{
        val instance: Observable by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { Observable() }
    }

    private var observers: ArrayList<Observer> = ArrayList()

    /**  增加观察者方法 */
    fun add(observer: Observer){
        if (!observers.contains(observer))
            observers.add(observer)
    }

    /** 删除观察者方法 */
    fun remove(observer: Observer){
        if (observers.contains(observer))
            observers.remove(observer)
    }

    /**  删除所有观察者  */
    fun removeAll(){
        observers.clear()
    }

    /** 通知观察者数据更新 */
    fun notifyObservers(t: Any? = null) {
        for (observer in observers) {
            observer.notifyUpdate(t)
        }
    }
}