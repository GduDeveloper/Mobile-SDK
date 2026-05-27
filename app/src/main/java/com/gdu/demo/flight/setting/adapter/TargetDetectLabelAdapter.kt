package com.gdu.demo.flight.setting.adapter

import androidx.appcompat.widget.AppCompatCheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gdu.demo.R
import com.gdu.demo.flight.setting.bean.TargetDetectLabel
import com.gdu.lib.util.core.XLogger

class TargetDetectLabelAdapter(val callback: ITargetLabelCheckCallback) : BaseQuickAdapter<TargetDetectLabel, BaseViewHolder>(
        R.layout.target_detect_label_list_item){

    override fun convert(holder: BaseViewHolder, item: TargetDetectLabel) {
        XLogger.APP.i("TargetDetectLabelAdapter", "id = ${item.id}, isChecked = ${item.isChecked}")
        holder.setText(R.id.labelName, item.name)
        val checkView = holder.getView<AppCompatCheckBox>(R.id.labelCheck)
        checkView.isChecked = item.isChecked
        checkView.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            callback.onCheckChange(null)
        }
    }
}