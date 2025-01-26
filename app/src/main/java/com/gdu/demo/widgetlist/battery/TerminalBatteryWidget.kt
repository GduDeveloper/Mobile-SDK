package com.gdu.demo.widgetlist.battery

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.gdu.demo.R
import com.gdu.demo.widgetlist.battery.bean.BatteryState
import com.gdu.demo.widgetlist.battery.bean.BatteryStatus
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget
import com.gdu.ux.core.extension.getColor
import com.gdu.ux.core.extension.getDrawable
import com.gdu.ux.core.extension.imageDrawable
import com.gdu.ux.core.extension.textColorStateList


/**
 * @author wuqb
 * @date 2024/11/13
 * @description 终端电池电量widget  如：遥控器，车机等
 */
class TerminalBatteryWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<TerminalBatteryWidgetModel>(context, attrs, defStyleAttr){

    private val mBatteryIcon = findViewById<ImageView>(R.id.ux_terminal_battery_icon)
    private val mBatteryValue = findViewById<TextView>(R.id.ux_terminal_battery_value)

    private var batteryColorStates: MutableMap<BatteryStatus, ColorStateList> = mutableMapOf(
        BatteryStatus.DEFAULT to ColorStateList.valueOf(Color.WHITE),
        BatteryStatus.NORMAL to ColorStateList.valueOf(Color.WHITE),
        BatteryStatus.WARNING_LEVEL_1 to ColorStateList.valueOf(getColor(R.color.red)),
        BatteryStatus.WARNING_LEVEL_2 to ColorStateList.valueOf(getColor(R.color.red))
    )
    private var batteryIconStates: MutableMap<BatteryStatus, Drawable> = mutableMapOf(
        BatteryStatus.DEFAULT to getDrawable(R.drawable.top_remot_rc),
        BatteryStatus.NORMAL to getDrawable(R.drawable.top_remot_rc),
        BatteryStatus.WARNING_LEVEL_1 to getDrawable(R.drawable.top_remot_rc_low),
        BatteryStatus.WARNING_LEVEL_2 to getDrawable(R.drawable.top_remot_rc_low)
    )

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.ux_widget_terminal_battery, this)
    }

    override fun initWidgetModel(): TerminalBatteryWidgetModel = TerminalBatteryWidgetModel()

    override fun bindingData(data: Any) {
        when (data) {
            is BatteryState.SingleBatteryState -> {
                batteryIconStates[data.batteryStatus]?.let {
                    mBatteryIcon.imageDrawable = it
                }
                batteryColorStates[data.batteryStatus]?.let {
                    mBatteryValue.textColorStateList = it
                }

                mBatteryValue.text = resources.getString(R.string.ux_battery_percent, data.percentageRemaining)

                when(data.batteryStatus){
                    BatteryStatus.DEFAULT->mBatteryValue.setText(R.string.Label_N_A)
                    BatteryStatus.WARNING_LEVEL_2->{
                        onIconFlicker()
                    }
                    BatteryStatus.WARNING_LEVEL_1->{
                        onIconFlicker()
                        // todo 语音播报
                    }
                    else -> {

                    }
                }
            }
        }
    }

    /**
     * 低电量时图标闪烁
     * */
    private fun onIconFlicker(){
        mBatteryIcon.visibility = if(mBatteryIcon.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
    }
}