package com.gdu.demo.widgetlist.flyState

import android.content.Context
import android.util.AttributeSet
import com.gdu.config.ConnStateEnum
import com.gdu.config.GlobalVariable
import com.gdu.demo.R
import com.gdu.demo.databinding.FlyStateLayoutBinding
import com.gdu.demo.utils.SettingDao
import com.gdu.demo.utils.UnitChnageUtils
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget
import java.math.BigDecimal
import java.math.RoundingMode

class FlyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<FlyStateModel>(context, attrs, defStyleAttr)  {


    private lateinit var binding: FlyStateLayoutBinding

    private var mUnitUtils: UnitChnageUtils? = null

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = FlyStateLayoutBinding.bind(inflate(context, R.layout.fly_state_layout, this))
        mUnitUtils = UnitChnageUtils()

    }

    override fun initWidgetModel(): FlyStateModel = FlyStateModel()

    override fun bindingData(data: Any) {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            binding.tvDis.text = context.getText(R.string.Label_N_A)
            binding.tvHeight.text = context.getText(R.string.Label_N_A)
            binding.tvHSpeed.text = context.getText(R.string.Label_N_A)
            binding.tvVSpeed.text = context.getText(R.string.Label_N_A)
            binding.tvHeadAngle.text = context.getText(R.string.Label_N_A)
            binding.tvEllipsoidHeight.text = context.getText(R.string.Label_N_A)
            binding.tvASL.text = context.getText(R.string.Label_N_A)
            return
        }

        when (data) {
            is FlyStateValue -> {
                val showDis = BigDecimal((data.dis / 1.0f).toDouble()).setScale(1, RoundingMode.HALF_UP).toFloat()
                binding.tvDis.text = UnitChnageUtils.getUnitString(showDis)

                val showHeight = BigDecimal((data.height / 100.0f).toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.tvHeight.text =UnitChnageUtils.getUnitString(showHeight)

                val showHS = BigDecimal((data.hs/100.0f).toDouble()).setScale(1, RoundingMode.HALF_UP).toFloat()
                binding.tvHSpeed.text = "$showHS m/s"

                val showVS = BigDecimal((data.vs/100.0f).toDouble()).setScale(1, RoundingMode.HALF_UP).toFloat()
                binding.tvVSpeed.text = "$showVS m/s"

                val showAngle =BigDecimal((data.headAngel/1.0f).toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.tvHeadAngle.text = "$showAngle °"

                val showEllipsoid = BigDecimal((data.ellipsoid_height/100.0f).toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.tvEllipsoidHeight.text = "$showEllipsoid m"

                val showAsl = BigDecimal((data.asl_height/100.0f).toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                binding.tvASL.text ="$showAsl m"

            }
        }
    }
}