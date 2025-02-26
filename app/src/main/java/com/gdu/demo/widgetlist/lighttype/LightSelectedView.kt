package com.gdu.demo.widgetlist.lighttype

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.gdu.camera.SettingsDefinitions
import com.gdu.common.error.GDUError
import com.gdu.demo.R
import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.databinding.LayoutLightSelectedBinding
import com.gdu.demo.widgetlist.core.base.widget.WidgetModel
import com.gdu.demo.widgetlist.flyState.FlyStateModel
import com.gdu.sdk.camera.GDUCamera
import com.gdu.sdk.gimbal.GDUGimbal
import com.gdu.sdk.products.GDUAircraft
import com.gdu.sdk.util.CommonCallbacks.CompletionCallback
import com.gdu.ux.core.base.widget.ConstraintLayoutWidget

class LightSelectedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayoutWidget<LightTypeModel>(context, attrs, defStyleAttr) {

    private lateinit var binding: LayoutLightSelectedBinding

    private var mGDUGimbal: GDUGimbal? = null

    private var currentType = -1


    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        
        binding = LayoutLightSelectedBinding.bind(inflate(context, R.layout.layout_light_selected, this))
        
        binding.tvVisibleLight.setOnClickListener {
            changeLight(SettingsDefinitions.DisplayMode.VISUAL_ONLY)

        }
        binding.tvWide.setOnClickListener{
            changeLight(SettingsDefinitions.DisplayMode.WAL)
        }
        binding.tvZoom.setOnClickListener{
            changeLight(SettingsDefinitions.DisplayMode.ZL)
        }
        binding.tvIr.setOnClickListener{
            changeLight(SettingsDefinitions.DisplayMode.THERMAL_ONLY)
        }
        
        binding.tvSplit.setOnClickListener{
            changeLight(SettingsDefinitions.DisplayMode.PIP)
        }

        mGDUGimbal = (SdkDemoApplication.getProductInstance() as GDUAircraft).gimbal as? GDUGimbal
        mGDUGimbal?.let {
            val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
            for (mode in list) {

                when (mode) {
                    SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                        binding.tvIr.visibility = VISIBLE
                    }
                    SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                        binding.tvVisibleLight.visibility = VISIBLE
                    }
                    SettingsDefinitions.DisplayMode.WAL -> {
                        binding.tvWide.visibility = VISIBLE
                    }
                    SettingsDefinitions.DisplayMode.ZL -> {
                        binding.tvZoom.visibility = VISIBLE
                    }
                    SettingsDefinitions.DisplayMode.PIP -> {
                        binding.tvSplit.visibility = VISIBLE }

                    else ->{ }

                }
            }
        }




    }

    override fun initWidgetModel(): LightTypeModel = LightTypeModel()


    override fun bindingData(data: Any) {

        if (data is LightTypeValue) {
            if (data.lightType == currentType) {
                return
            }
            currentType = data.lightType
            mGDUGimbal = (SdkDemoApplication.getProductInstance() as GDUAircraft).gimbal as? GDUGimbal
            when (data.lightType) {
                0x00 -> {
                    mGDUGimbal?.let {
                        val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
                        for (mode in list) {
                            when (mode) {
                                SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                                    binding.tvIr.visibility = GONE
                                }
                                SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                                    binding.tvVisibleLight.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.WAL -> {
                                    binding.tvWide.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.ZL -> {
                                    binding.tvZoom.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.PIP -> {
                                    binding.tvSplit.visibility = VISIBLE }

                                else ->{ }

                            }
                        }
                    }
                }

                0x02 ->{
                    mGDUGimbal?.let {
                        val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
                        for (mode in list) {
                            when (mode) {
                                SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                                    binding.tvIr.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                                    binding.tvVisibleLight.visibility = GONE
                                }
                                SettingsDefinitions.DisplayMode.WAL -> {
                                    binding.tvWide.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.ZL -> {
                                    binding.tvZoom.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.PIP -> {
                                    binding.tvSplit.visibility = VISIBLE }

                                else ->{ }

                            }
                        }
                    }
                }

                0x05 ->{
                    mGDUGimbal?.let {
                        val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
                        for (mode in list) {
                            when (mode) {
                                SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                                    binding.tvIr.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                                    binding.tvVisibleLight.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.WAL -> {
                                    binding.tvWide.visibility = GONE
                                }
                                SettingsDefinitions.DisplayMode.ZL -> {
                                    binding.tvZoom.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.PIP -> {
                                    binding.tvSplit.visibility = VISIBLE }

                                else ->{ }

                            }
                        }
                    }
                }
                0x06 ->{
                    mGDUGimbal?.let {
                        val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
                        for (mode in list) {
                            when (mode) {
                                SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                                    binding.tvIr.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                                    binding.tvVisibleLight.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.WAL -> {
                                    binding.tvWide.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.ZL -> {
                                    binding.tvZoom.visibility = GONE
                                }
                                SettingsDefinitions.DisplayMode.PIP -> {
                                    binding.tvSplit.visibility = VISIBLE }

                                else ->{ }

                            }
                        }
                    }
                }
                0x07 ->{
                    mGDUGimbal?.let {
                        val list: List<SettingsDefinitions.DisplayMode> = it.supportDisplayMode
                        for (mode in list) {
                            when (mode) {
                                SettingsDefinitions.DisplayMode.THERMAL_ONLY -> {
                                    binding.tvIr.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.VISUAL_ONLY -> {
                                    binding.tvVisibleLight.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.WAL -> {
                                    binding.tvWide.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.ZL -> {
                                    binding.tvZoom.visibility = VISIBLE
                                }
                                SettingsDefinitions.DisplayMode.PIP -> {
                                    binding.tvSplit.visibility = GONE }

                                else ->{ }

                            }
                        }
                    }
                }

            }


        }
    }



    private fun changeLight(type: SettingsDefinitions.DisplayMode) {
        val mGDUCamera = (SdkDemoApplication.getProductInstance() as GDUAircraft).camera as? GDUCamera
        mGDUCamera?.setDisplayMode(type) { error ->
            if (error == null) {
                Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "设置失败", Toast.LENGTH_SHORT).show()
            }
        }


    }



}