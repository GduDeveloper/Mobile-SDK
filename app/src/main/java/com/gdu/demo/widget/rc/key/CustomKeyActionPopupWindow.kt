package com.gdu.demo.widget.rc.key

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdu.demo.R
import com.gdu.demo.databinding.RcCustomKeyViewMenuPopBinding
import com.gdu.demo.widget.rc.RcCustomKeyMenAdapter
import com.gdu.demo.widget.rc.RcMenuTitleAdapter
import com.gdu.demo.widget.rc.SettingMenuItem
import com.gdu.lib.util.core.ScreenUtils
import com.gdu.lib.util.core.XLogger

/**
 *
 * @author: guoyang
 * @date: 2025/5/29
 */
class CustomKeyActionPopupWindow(val context: Context) : PopupWindow(context) {

    private var onChooseClick: ((RCKeyActionItem) -> Unit)? = null

    private val menuAdapter = RcCustomKeyMenAdapter()

    private val binding: RcCustomKeyViewMenuPopBinding by lazy {
        RcCustomKeyViewMenuPopBinding.inflate(LayoutInflater.from(context))
    }

    init {
        this.contentView = binding.root

        initMenu()
        initTitle()

        binding.ivClose.setOnClickListener {
            if (this.isShowing) {
                this.dismiss()
            }
        }

        // init pop属性
        this.isFocusable = false
        this.isOutsideTouchable = true
        this.width = context.resources.getDimensionPixelSize(R.dimen.dp_230)
        this.height = ScreenUtils.getScreenHeight() - context.resources.getDimensionPixelSize(R.dimen.dp_140)
        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    fun setOnChooseListener(call: (RCKeyActionItem) -> Unit) {
        onChooseClick = call
    }

    private fun initMenu() {
        setMenu(0)
        binding.rvMenu.apply {
            setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
            setAdapter(menuAdapter)
        }
        menuAdapter.setOnItemClickListener { adapter, _, position ->
            XLogger.APP.i("RCCustom", "start setOnItemClickListener ")
            adapter.getItem(position)?.let {
                onChooseClick?.invoke(it as RCKeyActionItem)
                this.dismiss()
            }
            XLogger.APP.i("RCCustom", "end setOnItemClickListener ")
        }
    }

    private fun initTitle() {
        val list = ArrayList<SettingMenuItem>()
        list.add(SettingMenuItem(0, R.drawable.rc_custom_key_camera, ""))
        list.add(SettingMenuItem(1,R.drawable.rc_custom_key_gimbal, ""))
        list.add(SettingMenuItem(2,R.drawable.rc_custom_key_app, ""))
        list.add(SettingMenuItem(3,R.drawable.rc_custom_key_flight_control, ""))

        val titleAdapter = RcMenuTitleAdapter()
        binding.rvTitle.apply {
            setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))
            setAdapter(titleAdapter)
        }
        titleAdapter.setList(list)
        titleAdapter.setOnItemClickListener { _, _, position ->
            titleAdapter.setSelectPosition(position)
            setMenu(position)
        }
    }

    private fun setMenu(pos: Int) {
        val list = when (pos) {
            0 -> { RCCustomKeyRepository.cameraActionList }
            1 -> { RCCustomKeyRepository.gimbalActionList }
            2 -> { RCCustomKeyRepository.appActionList }
            else -> { RCCustomKeyRepository.fcActionList } /** 3 */
        }
        menuAdapter.setList(list)
    }

    fun show(parentView: View) {
        val loc = IntArray(2)
        parentView.getLocationInWindow(loc)

        showAtLocation(
            parentView,
            Gravity.START or Gravity.TOP,
            loc[0],
            loc[1]
        )
    }

}