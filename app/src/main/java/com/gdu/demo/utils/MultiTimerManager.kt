package com.gdu.demo.utils


/**
 * @author wuqb
 * @date 2025/5/30
 * @description TODO
 */

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MultiTimerManager private constructor() {
    private val timers: MutableMap<String, TimerData> = ConcurrentHashMap()

    // 创建新定时器
    fun createTimer(timerId: String, interval: Long) {
        if (timers.containsKey(timerId)) {
            return  // 定时器已存在
        }

        val timerData = TimerData(timerId, interval)
        timers[timerId] = timerData
    }

    // 获取定时器Observable
    fun getTimerObservable(timerId: String): Observable<Long> {
        val timerData = timers[timerId]
        if (timerData != null) {
            return timerData.observable
        }
        return Observable.empty()
    }

    // 启动定时器
    fun startTimer(timerId: String) {
        val timerData = timers[timerId]
        timerData?.start()
    }

    // 停止定时器
    fun stopTimer(timerId: String) {
        val timerData = timers[timerId]
        timerData?.stop()
    }

    // 暂停定时器
    fun pauseTimer(timerId: String) {
        val timerData = timers[timerId]
        timerData?.pause()
    }

    // 恢复定时器
    fun resumeTimer(timerId: String) {
        val timerData = timers[timerId]
        timerData?.resume()
    }

    // 移除定时器
    fun removeTimer(timerId: String) {
        val timerData = timers.remove(timerId)
        timerData?.dispose()
    }

    // 启动所有定时器
    fun startAllTimers() {
        for (timer in timers.values) {
            timer.start()
        }
    }

    // 停止所有定时器
    fun stopAllTimers() {
        for (timer in timers.values) {
            timer.stop()
        }
    }

    val allTimers: Map<String, TimerData>
        // 获取所有定时器
        get() = ConcurrentHashMap(timers)

    // 定时器数据类
    class TimerData(val timerId: String, val interval: Long) {
        private val counterSubject = BehaviorSubject.createDefault(0L)
        private val disposables = CompositeDisposable()
        private var intervalDisposable: Disposable? = null
        var currentCount: Long = 0
            private set
        var isPaused: Boolean = false
            private set

        val observable: Observable<Long>
            get() = counterSubject.hide()

        fun start() {
            if (intervalDisposable != null && !intervalDisposable!!.isDisposed) {
                return  // 已经启动
            }

            intervalDisposable = Observable.interval(interval, TimeUnit.MILLISECONDS)
                .map { tick -> currentCount + 1 }
                .doOnNext { value -> currentCount = value }
                .subscribe(counterSubject::onNext)

            disposables.add(intervalDisposable!!)
            isPaused = false
        }

        fun stop() {
            if (intervalDisposable != null && !intervalDisposable!!.isDisposed) {
                intervalDisposable!!.dispose()
            }
            currentCount = 0
            counterSubject.onNext(0L)
            isPaused = false
        }

        fun pause() {
            if (intervalDisposable != null && !intervalDisposable!!.isDisposed) {
                intervalDisposable!!.dispose()
                isPaused = true
            }
        }

        fun resume() {
            if (isPaused) {
                start()
            }
        }

        fun dispose() {
            disposables.clear()
            counterSubject.onComplete()
        }

        val isRunning: Boolean
            get() = intervalDisposable != null && !intervalDisposable!!.isDisposed
    }

    companion object {

        val instance: MultiTimerManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { MultiTimerManager() }

        const val QUICK_TIMER = "quick_timer"  //200ms一次
        const val NORMAL_TIMER = "normal_timer"  //1s一次
    }
}