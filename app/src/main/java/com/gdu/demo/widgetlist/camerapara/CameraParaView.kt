package com.gdu.demo.widgetlist.camerapara

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.gdu.config.GlobalVariable
import com.gdu.demo.R
import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.databinding.LayoutCameraParaBinding
import com.gdu.demo.utils.CameraUtil
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.sdk.camera.GDUCamera
import com.gdu.sdk.products.GDUAircraft
import com.gdu.util.GimbalUtil
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget
import com.gdu.ux.core.extension.getString
import java.math.BigDecimal
import java.math.RoundingMode

class CameraParaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<WidgetModel>(context, attrs, defStyleAttr)  {

    private lateinit var binding: LayoutCameraParaBinding
    private lateinit var popWindow: GimbalControlTopOptItemPopView

    private var mGDUCamera: GDUCamera? = null


    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = LayoutCameraParaBinding.bind(inflate(context, R.layout.layout_camera_para, this))
        mGDUCamera = (SdkDemoApplication.getProductInstance() as GDUAircraft).camera as? GDUCamera
        initListener()

    }

    private fun initListener() {
        binding.ivAutoSwitchBtn.setOnClickListener {
            val setAutoMode = if(binding.ivAutoSwitchBtn.isSelected) 0 else 1
            mGDUCamera?.setAutoOrManualMode(setAutoMode){ error ->
                if (error == null) {
                    binding.ivAutoSwitchBtn.isSelected = setAutoMode == 0
                    GlobalVariable.isAutoMode = setAutoMode == 1
                }
            }
        }

        binding.viewISOOver.setOnClickListener{
            showPopWindow(1)
        }
        binding.viewShutterOver.setOnClickListener {
            showPopWindow(2)
        }

        binding.viewEVOver.setOnClickListener {
            showPopWindow(3)
        }
        binding.ivAELockBtn.setOnClickListener{
            val open = binding.ivAELockBtn.isSelected
            mGDUCamera?.setAELockSwitch(open) { error ->

            }
        }
    }

    override fun initWidgetModel(): WidgetModel = CameraParaModel()



    override fun bindingData(data: Any) {
        when (data) {
            is CameraParaValue ->{


                if (GlobalVariable.sCameraLightType.toInt() == 2 || GlobalVariable.sCameraLightType.toInt() == 5 || GlobalVariable.sCameraLightType.toInt() == 6) {
                    binding.viewEVGroup.visibility = VISIBLE
                    binding.viewISOGroup.visibility =
                        if (CameraUtil.isSupportISOGimbal(GlobalVariable.gimbalType)) VISIBLE else GONE
                    binding.viewESGroup.visibility =
                        if (CameraUtil.isSupportESGimbal(GlobalVariable.gimbalType)) VISIBLE else GONE
                    binding.viewAutoSwitchGroup.visibility =
                        if (CameraUtil.isSupportAutoSwitchGimbal(GlobalVariable.gimbalType)) VISIBLE else GONE
                    binding.ivAELockBtn.setVisibility(if (CameraUtil.isSupportAELockGimbal(GlobalVariable.gimbalType)) VISIBLE else GONE)
                } else {
                    binding.viewEVGroup.visibility = GONE
                    binding.viewISOGroup.visibility = GONE
                    binding.viewESGroup.visibility = GONE
                    binding.viewAutoSwitchGroup.visibility = GONE
                    binding.ivAELockBtn.visibility = GONE
                }

                binding.ivAutoSwitchBtn.setSelected(!data.isAutoMode)

                binding.tvIsoSetContent.text = CameraUtil.getISODisplayByValue(context)

                //快门
                val esStr = CameraUtil.getESNameByValue(GlobalVariable.gimbalType, GlobalVariable.lightESValue, true);
                binding.tvShutterSetContent.text = esStr

                val ev = CameraUtil.getEVNameByValue(context, GlobalVariable.gimbalType, GlobalVariable.lightEvValue)
                binding.tvEvSetContent.text = ev

                binding.ivAELockBtn.isSelected = data.aeLockValue != 1

                updateISOAndShutterStatus(CameraUtil.checkISOAndShutterUnableSet(data.isAutoMode, data.aeLockValue))
                updateEVViewStatus(CameraUtil.checkEVUnableSet(data.isAutoMode, data.aeLockValue))

                val sdCardState = data.sdCardState
                if (sdCardState.isMultiSDCard) {
                    binding.viewIRStorageGroup.visibility = VISIBLE
                    if (sdCardState.vlSDState == 0) {
                        val formatNum = BigDecimal(sdCardState.reMainCardSum * 10.0 / 1024.0f).setScale(1, RoundingMode.HALF_UP).toFloat()
                        if (sdCardState.reMainCardSum < 0) {
                            binding.tvStorageContent.text = "0.0G"
                        } else {
                            binding.tvStorageContent.text = "$formatNum G"
                        }
                    } else if (sdCardState.vlSDState == 3) {
                        binding.tvStorageContent.text = getString(R.string.Label_NoCardInserted)
                    } else {
                        binding.tvStorageContent.text = "0.0G"
                    }

                    if (sdCardState.irSDStatus == 0) {
                        val formatNum = BigDecimal(sdCardState.reMainCard2Sum * 10.0 / 1024.0f).setScale(1, RoundingMode.HALF_UP).toFloat()
                        if (sdCardState.reMainCard2Sum < 0) {
                            binding.tvIRStorageContent.text = "0.0G"
                        } else {
                            binding.tvIRStorageContent.text = "$formatNum G"
                        }
                    }else if (sdCardState.irSDStatus == 3) {
                        binding.tvIRStorageContent.text = getString(R.string.Label_NoCardInserted)
                    } else {
                        binding.tvIRStorageContent.text = "0.0G"
                    }
                } else {
                    binding.viewIRStorageGroup.visibility = GONE
                    if (sdCardState.vlSDState == 0) {
                        val formatNum = BigDecimal(sdCardState.reMainCardSum * 10.0 / 1024.0f).setScale(1, RoundingMode.HALF_UP).toFloat()
                        if (sdCardState.reMainCardSum < 0) {
                            binding.tvStorageContent.text = "0.0G"
                        } else {
                            binding.tvStorageContent.text = "$formatNum G"
                        }
                    } else if (sdCardState.vlSDState == 3) {
                        binding.tvStorageContent.text = getString(R.string.Label_NoCardInserted)
                    } else {
                        binding.tvStorageContent.text = "0.0G"
                    }
                }
            }
        }
    }

    private fun updateISOAndShutterStatus(isSelect: Boolean) {
        binding.tvIsoSetLabel.setSelected(isSelect)
        binding.tvIsoSetContent.setSelected(isSelect)
        binding.tvShutterSetLabel.setSelected(isSelect)
        binding.tvShutterSetContent.setSelected(isSelect)
        binding.viewISOOver.setEnabled(!isSelect)
        binding.viewShutterOver.setEnabled(!isSelect)
    }
    private fun updateEVViewStatus(isSelected: Boolean) {
        binding.tvEvSetLabel.setSelected(isSelected)
        binding.tvEvSetContent.setSelected(isSelected)
        binding.viewEVOver.setEnabled(!isSelected)
    }


    /**
     *   type 1 :iso 2 shutter 3 ev
     */
    fun showPopWindow(type: Int) {
        popWindow = GimbalControlTopOptItemPopView(context, type)
        popWindow.setListener{ adapter, type, position ->
            val bean = adapter.getItem(position) as GimbalControlTopOptBean
            if (type == 1) {
                mGDUCamera?.setISO(bean.value) {

                }
            } else if (type == 2) {
                mGDUCamera?.setEs(bean.value) {

                }

            } else if (type == 3) {
                mGDUCamera?.setEV(bean.value) {

                }
            }
            popWindow.dismiss()
        }
        val selectIndex: Int
        when (type) {
            1 -> {
                val isoData = getISOData()
                selectIndex = CameraUtil.getISONameIndexByValue(GlobalVariable.gimbalType, GlobalVariable.lightISOValue.toString())
                popWindow.updateData(isoData, selectIndex)
                popWindow.showAsDropDown(binding.viewISOOver, 0, 0)
            }

            2 -> {
                val mShutterData = getShutterData()
                val esStr = CameraUtil.getESNameByValue(GlobalVariable.gimbalType, GlobalVariable.lightESValue, true)
                selectIndex = CameraUtil.getESNameIndexByValue(GlobalVariable.gimbalType, esStr)
                popWindow.updateData(mShutterData, selectIndex)
                popWindow.showAsDropDown(binding.viewShutterOver, 0, 0)
            }

            3 -> {
                val evData = getEvData()
                selectIndex = CameraUtil.getEVValueIndexByValue(GlobalVariable.gimbalType, GlobalVariable.lightEvValue)
                popWindow.updateData(evData, selectIndex)
                popWindow.showAsDropDown(binding.viewEVOver, 0, 0)
            }
            else -> {}
        }
    }

    private fun getISOData(): List<GimbalControlTopOptBean> {
        val isoData: MutableList<GimbalControlTopOptBean> = arrayListOf()
        var bean: GimbalControlTopOptBean
        val mISOValueArray = CameraUtil.getISOValuesByGimbalType(GlobalVariable.gimbalType)
        val mISONameArray = CameraUtil.getISONamesByGimbalType(GlobalVariable.gimbalType)
        if (mISOValueArray.size == mISONameArray.size) {
            for (i in mISONameArray.indices) {
                bean = GimbalControlTopOptBean()
                bean.name = mISONameArray[i]
                bean.value = mISOValueArray[i]
                isoData.add(bean)
            }
        }
        return isoData
    }

    private fun getShutterData():List<GimbalControlTopOptBean> {
        val mShutterData: MutableList<GimbalControlTopOptBean> = arrayListOf()
        var bean: GimbalControlTopOptBean
        val mShutterValueArray = CameraUtil.getESValuesByGimbalType(GlobalVariable.gimbalType)
        val mShutterNameArray = CameraUtil.getESNamesByGimbalType(GlobalVariable.gimbalType)
        if (mShutterValueArray.size == mShutterNameArray.size) {
            for (i in mShutterNameArray.indices) {
                bean = GimbalControlTopOptBean()
                bean.name = mShutterNameArray[i]
                bean.value = mShutterValueArray[i]
                mShutterData.add(bean)
            }
        }
        return mShutterData
    }

    private fun getEvData(): List<GimbalControlTopOptBean> {
        val evData: MutableList<GimbalControlTopOptBean> = arrayListOf()
        var bean: GimbalControlTopOptBean
        val mEVValueArray = CameraUtil.getEVValuesByGimbalType(GlobalVariable.gimbalType)
        val mEVNameArray = CameraUtil.getEVNamesByGimbalType(context, GlobalVariable.gimbalType)
        for (i in mEVNameArray.indices) {
            bean = GimbalControlTopOptBean()
            bean.name = mEVNameArray[i]
            bean.value = mEVValueArray[i]
            evData.add(bean)
        }
        return evData
    }


}