package com.gdu.demo.flight.setting.adapter

import androidx.appcompat.widget.AppCompatCheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gdu.demo.R
import com.gdu.demo.flight.setting.bean.TargetDetectLabel

class TargetDetectLabelAdapter(val callback: ITargetLabelCheckCallback) : BaseQuickAdapter<TargetDetectLabel, BaseViewHolder>(
    R.layout.target_detect_label_list_item) {

    override fun convert(holder: BaseViewHolder, data: TargetDetectLabel) {
        holder?.setText(R.id.labelName, data.name)
            val checkView = holder?.getView<AppCompatCheckBox>(R.id.labelCheck)
            checkView?.isChecked = data.isChecked
            checkView?.setOnCheckedChangeListener { _, isChecked ->
                data.isChecked = isChecked
                callback.onCheckChange(null) }

    }
}