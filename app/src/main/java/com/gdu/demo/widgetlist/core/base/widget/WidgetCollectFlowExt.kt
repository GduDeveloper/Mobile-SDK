package com.gdu.demo.widgetlist.core.base.widget

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * @author wuqb
 * @date 2026/8/28 18:27
 * @description 这里写描述
 */
fun <T> View.widgetCollectFlowData(flow: Flow<T>?, callback: (T) -> Unit) {
    findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
        lifecycleOwner.lifecycleScope.launch {
            flow?.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)?.collect {
                callback.invoke(it)
            }
        }
    }
}