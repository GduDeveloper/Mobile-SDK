package com.gdu.ux.core.base.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.ux.core.base.notify.Observable
import com.gdu.ux.core.base.notify.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch


/**
 * @author wuqb
 * @date 2024/11/11
 * @description 通用Widget, 所有Widget父类
 */
abstract class ConstraintLayoutWidget<T: WidgetModel> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), WidgetDataBinding, CoroutineScope by MainScope(),
    Observer {

    var widgetModel:T? = null
    private val mCacheData = HashMap<WidgetDataBinding, Any>()

    init {
        initView(context, attrs, defStyleAttr)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode){
            loadData()
        }
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            destroy()
        }
        super.onDetachedFromWindow()
    }

    /**
     * 接收被观察者数据
     * */
    override fun notifyUpdate(t: Any?) {
        for ((key, value) in mCacheData) {
            key.bindingData(value)
            mCacheData.remove(key)
        }
    }

    protected abstract fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)

    protected abstract fun initWidgetModel():T

    /**是否实时刷新，刷新机制分为实时刷新和同屏刷新两种*/
    open fun isRealTimeRefresh():Boolean{
        return true
    }

    /**
     * 是否布局不可见时绑定并更新布局数据, 默认在布局不可见时不获取数据也不绑定数据
     * */
    open fun isBingInInVisible():Boolean{
        return false
    }

    private fun loadData(){
        if (!isBingInInVisible() && visibility != VISIBLE)
            return
        widgetModel = initWidgetModel()
        Observable.instance.add(this)
        initData()
    }

    open fun initData(){
        launch {
            widgetModel?.dataChangeChannel?.consumeEach { data ->
                if (!isBingInInVisible() && visibility != VISIBLE)
                    return@launch
                if (!isRealTimeRefresh()){
                    mCacheData[this@ConstraintLayoutWidget] = data
                }else {
                    if (mCacheData.containsKey(this@ConstraintLayoutWidget)){
                        mCacheData.remove(this@ConstraintLayoutWidget)
                    }
                    bindingData(data)
                }
            }
        }
        widgetModel?.start()
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (widgetModel == null && visibility == VISIBLE){
            loadData()
        }else{
            destroy()
        }
    }

    open fun destroy(){
        Observable.instance.remove(this)
        widgetModel?.destroy()
        widgetModel = null
    }
}