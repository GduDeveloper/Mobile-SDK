package com.gdu.demo.setting

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gdu.demo.R
import com.gdu.demo.utils.DeviceUtils


class SettingLeftAdapter(context: Context) : BaseQuickAdapter<SettingMenuItem, BaseViewHolder>(R.layout.item_setting_left) {

    private var selectedPosition = 0

    fun setSelectPosition(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseViewHolder, item: SettingMenuItem) {
        val rootView = holder.getView<ViewGroup>(R.id.root_view)
        val params = rootView.layoutParams
        params.height = DeviceUtils.getScreenHeight(context) / itemCount
        Log.d(" SettingLeftAdapter","height = "+(DeviceUtils.getScreenHeight(context))+" , size = "+itemCount)
        Log.d(" SettingLeftAdapter","height = "+(DeviceUtils.getScreenHeight(context) / itemCount))
        rootView.layoutParams = params

        holder.setImageResource(R.id.iv_setting_icon, item.iconRes)
        holder.getView<View>(R.id.iv_setting_icon).isSelected = getItemPosition(item) == selectedPosition

        if (getItemPosition(item) == selectedPosition) {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_ff4e00))
        } else {
            rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_ffffff))
        }
    }

}