package com.gdu.demo.widgetlist.takephoto.type

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.gdu.demo.R

/**
 * @author wuqb
 * @date 2026/8/28 15:13
 * @description 这里写描述
 */
class CaptureTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr){

    private var mCurrType = CaptureType.PICTURE // 标记当前显示的是正面还是反面
    private var mTypeListener: ((CaptureType) -> Unit)? = null
    private var mInit = false //用于判断首次不显示动画效果

    init {
        setImageResource(R.drawable.gimbal_ic_mode_picture)
    }

    private fun onClickHandle() {
        if (mCurrType == CaptureType.PICTURE) {
            mTypeListener?.invoke(CaptureType.VIDEO)
        } else {
            mTypeListener?.invoke(CaptureType.PICTURE)
        }
    }

    /**
     * 设置拍照类型
     * */
    fun onSwitchTakeType(shootType: CaptureType, anim:Boolean){
        if (mCurrType == shootType) return run { setImageSource() }
        mCurrType = shootType
        if (anim && mInit) {
            animate()
                .rotationY(90f)
                .setDuration(150)
                .setInterpolator(AccelerateInterpolator()) // 加速插值器
                .withLayer() // 启用硬件加速（优化性能）
                .withEndAction {
                    // 切换图标
                    setImageSource()
                    // 第二阶段动画：从 90 度旋转到 180 度（新图标出现）
                    rotationY = -90f // 先设置为 -90 度，避免回弹
                    animate()
                        .rotationY(0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }else{
            setImageSource()
        }
        mInit = true
    }

    /***
     * 设置图标
     * */
    private fun setImageSource(){
        when(mCurrType){
            CaptureType.VIDEO->{
                setImageResource(R.drawable.gimbal_ic_mode_video)
            }
            else->{
                setImageResource(R.drawable.gimbal_ic_mode_picture)
            }
        }
    }
}