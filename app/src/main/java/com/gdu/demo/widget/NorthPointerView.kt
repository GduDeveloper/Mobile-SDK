package com.gdu.demo.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.gdu.config.GduConfig
import com.gdu.config.GlobalVariable
import com.gdu.demo.R
import com.gdu.util.SPUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit

/**
 *@author dingwenxiang
 *@date 2024/12/10 19:23
 *@desc 伪3d指北针
 */
class NorthPointerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val img = ResourcesCompat.getDrawable(resources, R.drawable.north_pointer, null)
    private val imgInverse = ResourcesCompat.getDrawable(resources, R.drawable.north_pointer_inverse, null)
    private var curImg: Drawable? = null

    private var isShow = SPUtils.getBoolean(getContext(), GduConfig.NORTH_POINTER)


    /*
     * 更新View:
     * 云台的航向角是相对飞机的，俯仰角是相对大地的
     * android:rotationX="50" 俯仰角 GlobalVariable.pitchAngle  GlobalVariable.HolderPitch  下视-90度为基准则俯仰角为0时rotationX=90,下视-10则rotationX=80
     * android:rotationY="45" 横滚角 GlobalVariable.rollAngle   GlobalVariable.HolderRoll    设计不考虑横滚角
     * android:rotation ="30" 航向角 GlobalVariable.planeAngle + GlobalVariable.HolderYAW    航向角为30度则相对角度为-30
     */
    fun update() {
        val pitch = GlobalVariable.HolderPitch / 100f
        if (pitch > 0) {
            if (curImg != imgInverse) {
                curImg = imgInverse
                setImageDrawable(imgInverse)
            }
        } else {
            if (curImg != img) {
                curImg = img
                setImageDrawable(img)
            }
        }
        rotationX = pitch + 90
        rotation = -(GlobalVariable.planeAngle / 100f + GlobalVariable.HolderYAW / 100f)
    }

    /* 自动更新 */
    private var disposable: Disposable? = null
    var isAutoUpdate = false
        set(value) {
            field = value
            disposable?.dispose()
            if (value) {
                disposable = Observable.interval(100, 250, TimeUnit.MILLISECONDS)
                    //.filter { visibility == VISIBLE && windowVisibility == VISIBLE }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        // 磁力计异常不显示并toast提示
                        val isMagneticAbnormal = GlobalVariable.sRemoteCalibration == 0
                                && (GlobalVariable.magneticAbnormal.toInt() == 1
                                || GlobalVariable.magneticAbnormal.toInt() == 2
                                || GlobalVariable.sMagneticNotCalibration.toInt() == 1)
                        if (isMagneticAbnormal) {
                            if (visibility == VISIBLE) {
                                visibility = GONE
                                Toast.makeText(context, R.string.gyroscope_abnormal, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (visibility != VISIBLE && isAutoUpdate) {
                                visibility = VISIBLE
                            }
                        }
                        // 更新
                        if (visibility != VISIBLE || windowVisibility != VISIBLE) {
                            return@subscribe
                        }
                        update()
                    }
            }
        }

    init {
        //cameraDistance = 10000f
        visibility = if (isShow) VISIBLE else GONE
        isAutoUpdate = isShow
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
        disposable?.dispose()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(e: EventNorthPointer) {
        isShow = e.isShow
        visibility = if (isShow) VISIBLE else GONE
        isAutoUpdate = e.isShow
    }

    data class EventNorthPointer(val isShow: Boolean)
}