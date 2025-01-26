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
 * @date 2024/11/8
 * @description 飞机电量Widget
 */
class BatteryWidget@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<BatteryWidgetModel>(context, attrs, defStyleAttr) {

    private val mBatteryIcon = findViewById<ImageView>(R.id.ux_aircraft_battery_icon)
    private val mBatteryValue = findViewById<TextView>(R.id.ux_aircraft_battery_value)
    private val mBatteryVoltage = findViewById<TextView>(R.id.ux_aircraft_battery_voltage)

    private var batteryColorStates: MutableMap<BatteryStatus, ColorStateList> = mutableMapOf(
        BatteryStatus.NORMAL to ColorStateList.valueOf(Color.WHITE),
        BatteryStatus.OVERHEATING to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.WARNING_LEVEL_1 to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.WARNING_LEVEL_2 to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.ERROR to ColorStateList.valueOf(getColor(R.color.red))
    )
    private var voltageColorStates: MutableMap<BatteryStatus, ColorStateList> = mutableMapOf(
        BatteryStatus.NORMAL to ColorStateList.valueOf(Color.WHITE),
        BatteryStatus.OVERHEATING to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.WARNING_LEVEL_1 to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.WARNING_LEVEL_2 to ColorStateList.valueOf(getColor(R.color.color_FFC600)),
        BatteryStatus.ERROR to ColorStateList.valueOf(getColor(R.color.red))
    )

    private var voltageBackgroundStates: MutableMap<BatteryStatus, Drawable?> = mutableMapOf(
        BatteryStatus.NORMAL to getDrawable(R.drawable.shape_stroke_white_r2),
        BatteryStatus.OVERHEATING to getDrawable(R.drawable.shape_stroke_ffc600_r2),
        BatteryStatus.WARNING_LEVEL_1 to getDrawable(R.drawable.shape_stroke_ffc600_r2),
        BatteryStatus.WARNING_LEVEL_2 to getDrawable(R.drawable.shape_stroke_ffc600_r2),
        BatteryStatus.ERROR to getDrawable(R.drawable.shape_stroke_red_r2)
    )
    private var batteryIconStates: MutableMap<BatteryStatus, Drawable> = mutableMapOf(
        BatteryStatus.NORMAL to getDrawable(R.drawable.top_aircraft_electricity),
        BatteryStatus.OVERHEATING to getDrawable(R.drawable.top_aircraft_electricity_low_one),
        BatteryStatus.WARNING_LEVEL_1 to getDrawable(R.drawable.top_aircraft_electricity_low_one),
        BatteryStatus.WARNING_LEVEL_2 to getDrawable(R.drawable.top_aircraft_electricity_low_one),
        BatteryStatus.ERROR to getDrawable(R.drawable.top_aircraft_electricity_low)
    )

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.ux_widget_battery, this)
    }

    override fun initWidgetModel():BatteryWidgetModel = BatteryWidgetModel()

    override fun bindingData(data: Any) {
        when(data){
            is BatteryState.SingleBatteryState->{
                batteryIconStates[data.batteryStatus]?.let {
                    mBatteryIcon.imageDrawable = it
                }
                mBatteryValue.text = resources.getString(R.string.ux_battery_percent, data.percentageRemaining)
                mBatteryVoltage.text = resources.getString(R.string.ux_battery_voltage_unit, data.voltageLevel)
                setPercentageTextColorByState(mBatteryValue, data.batteryStatus)
                setVoltageTextColorByState(mBatteryVoltage, data.batteryStatus)
                mBatteryVoltage.background = voltageBackgroundStates[data.batteryStatus]
            }
        }
    }

    private fun setPercentageTextColorByState(textView: TextView, batteryStatus: BatteryStatus) {
        batteryColorStates[batteryStatus]?.let {
            textView.textColorStateList = it
        }
    }

    private fun setVoltageTextColorByState(textView: TextView, batteryStatus: BatteryStatus) {
        voltageColorStates[batteryStatus]?.let {
            textView.textColorStateList = it
        }
    }
}