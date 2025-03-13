package com.gdu.demo.flight.setting.adapter

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gdu.demo.R
import com.gdu.demo.flight.setting.bean.TargetDetectLabel
import com.gdu.demo.flight.setting.bean.TargetDetectModel
import com.gdu.util.ResourceUtil

class TargetDetectModelAdapter(val callback: ITargetLabelCheckCallback) : BaseQuickAdapter<TargetDetectModel, BaseViewHolder>(
    R.layout.target_detect_model_list_item) {

    private var labelAdapter: TargetDetectLabelAdapter? = null


    private fun getCheckedCount(list: MutableList<TargetDetectLabel>): Int {
        var count = 0
        list.forEach {
            if (it.isChecked) count++
        }
        return count
    }

    override fun convert(holder: BaseViewHolder, item: TargetDetectModel) {
            holder.setText(R.id.modelName, "${ResourceUtil.getStringById(R.string.target_detect_model)}${item.id}")
            var count = getCheckedCount(item.labels)
            holder.setText(R.id.modelUseState, "${ResourceUtil.getStringById(R.string.target_detect_using)}$count")
            if (count == 0) {
                holder.setVisible(R.id.modelUseState, false)
            } else {
                holder.setVisible(R.id.modelUseState, true)
            }
            labelAdapter = TargetDetectLabelAdapter(object : ITargetLabelCheckCallback {
                override fun onCheckChange(data: TargetDetectModel?) {
                    count = getCheckedCount(item.labels)
                    holder.setText(R.id.modelUseState, "${ResourceUtil.getStringById(R.string.target_detect_using)}$count")
                    if (count == 0) {
                        holder.setVisible(R.id.modelUseState, false)
                    } else {
                        holder.setVisible(R.id.modelUseState, true)
                    }
                    callback.onCheckChange(item)
                }
            })
            holder.getView<RecyclerView>(R.id.rvLabels)?.adapter = labelAdapter
            labelAdapter?.setNewInstance(item.labels)


        val modelMore = holder.getView<AppCompatImageView>(R.id.modelMore)
        val llModelMore = holder.getView<LinearLayout>(R.id.llModelMore)
        val rvLabels = holder.getView<RecyclerView>(R.id.rvLabels)
        llModelMore.setOnClickListener {
            if (rvLabels.visibility == View.VISIBLE) {
                rvLabels.visibility = View.GONE
                modelMore.setImageResource(R.drawable.delay_arrow)
            } else {
                rvLabels.visibility = View.VISIBLE
                modelMore.setImageResource(R.drawable.delay_arrow_down)
            }
        }
    }
}