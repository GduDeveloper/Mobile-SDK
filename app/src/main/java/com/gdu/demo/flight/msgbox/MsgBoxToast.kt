package com.gdu.demo.flight.msgbox

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gdu.demo.R
import com.gdu.lib.util.GsonUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.sdk.base.Diagnostics


/**
 * @author wuqb
 * @date 2025/1/7
 * @description 异常码Toast显示
 */
class MsgBoxToast: DialogFragment(){

    private var mCurrentIndex = 0
    private var mPreWarnToast:ArrayList<Diagnostics> = ArrayList()
    private var containerRv:RecyclerView? = null

    private val mBaseAdapter: BaseQuickAdapter<Diagnostics, BaseViewHolder> = object : BaseQuickAdapter<Diagnostics, BaseViewHolder>(R.layout.msgbox_toast_item) {
        override fun convert(holder: BaseViewHolder, item: Diagnostics) {
            holder.setText(R.id.msgbox_toast_item, item.reason)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view:View = inflater.inflate(R.layout.msgbox_toast, container, false)
        containerRv = view.findViewById(R.id.msgbox_toast_rv)
        containerRv?.adapter = mBaseAdapter
        return view
    }


    /**
     * 由于是默认3s执行一次，故该处无需处理时间及计时器
     * */
    fun showToast(mWarnToast: ArrayList<Diagnostics>){
        if (equals(mWarnToast, mPreWarnToast)){
            mCurrentIndex++
            if (mCurrentIndex>=mPreWarnToast.size)
                mCurrentIndex = 0
        }else{
            mCurrentIndex = 0
            mPreWarnToast.clear()
            mPreWarnToast = mWarnToast
        }
        val data = ArrayList<Diagnostics>()
        if (mWarnToast.size > mCurrentIndex) {
            data.add(mWarnToast[mCurrentIndex])
        }
        if (mWarnToast.size > (mCurrentIndex+1)){
            mCurrentIndex += 1
            data.add(mWarnToast[mCurrentIndex])
        }
        mBaseAdapter.setList(data)
        XLogger.ERRCODE.i("显示Toast异常码消息："+ GsonUtils.toJson(data))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val layoutParams = it.attributes
            layoutParams.dimAmount = 0f
            layoutParams.gravity = Gravity.TOP
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog?.setCancelable(false)
    }

    /**
     * 判断两个内容是否相同
     * */
    private fun equals(list1: ArrayList<Diagnostics>, list2: ArrayList<Diagnostics>):Boolean{
        if (list1.size != list2.size) { // 若两个Map的大小不同
            return false // 设置比较结果为false
        } else {
            for (i in 0 until list1.size) {
                if (list1[i].code != list2[i].code) {
                    return false
                }
            }
        }
        return true
    }

    fun show(manager: FragmentManager) {
        if (isShowing(manager)){
            XLogger.APP.e("已经有显示的Toast异常码")
            return
        }
        if (manager.isDestroyed || manager.isStateSaved) {
            XLogger.APP.e("显示Toast异常码容器异常")
            return
        }
        try {
            manager.beginTransaction().apply {
                val fragments = manager.fragments
                for (fragment in fragments) {
                    if (fragment is MsgBoxToast) {
                        remove(fragment)
                    }
                }
            }.commitAllowingStateLoss()
            XLogger.APP.i("msgToast:show")
            super.show(manager, "ErrorToast")
        } catch (e: IllegalStateException) {
            // 记录日志，但不崩溃
            e.printStackTrace()
            XLogger.APP.e("崩溃了.......异常码toast弹窗崩溃了")
        }
    }

    private fun isShowing(fragmentManager: FragmentManager): Boolean {
        val fragment = fragmentManager.findFragmentByTag("ErrorToast")
        return fragment != null && fragment.isAdded || dialog?.isShowing == true
    }

    fun onDismiss(manager: FragmentManager?){
        manager?.let {
            if (isShowing(it))
                dismissAllowingStateLoss()
        }
    }
}