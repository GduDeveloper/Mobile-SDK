package com.gdu.ux.core

import org.reactivestreams.Subscription


/**
 * @author wuqb
 * @date 2024/11/15
 * @description TODO
 */
interface DataObserverInterface {
    fun onSubscribe(s: Subscription?)

    fun onNext(value: Any?)

    fun onError(e: Throwable?)

    fun onComplete()
}