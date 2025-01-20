package com.gdu.demo.widgetlist.rtk

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.gdu.demo.R
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class RTKWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<RTKStateModel>(context, attrs, defStyleAttr) {


    private val tvRtk1Satellite = findViewById<TextView>(R.id.tv_rtk1_satellite)
    private val tvRtk1Status = findViewById<TextView>(R.id.tv_rtk1_status)


    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.ux_widget_rtk_state, this)
    }

    override fun initWidgetModel(): RTKStateModel = RTKStateModel()


    override fun bindingData(data: Any) {
        when(data){
            is RTKStateValue->{
                if (TextUtils.isEmpty(data.status) && data.satellite == (-1).toByte()){
                    tvRtk1Satellite.setText(R.string.Label_N_A)
                    tvRtk1Status.setText(R.string.Label_N_A)
                }else{
                    tvRtk1Satellite.text = data.satellite.toString();
                    tvRtk1Status.text = data.status
                }
            }
        }
    }
}