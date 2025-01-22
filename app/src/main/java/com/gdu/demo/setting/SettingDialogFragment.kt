package com.gdu.demo.setting

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdu.demo.R
import com.gdu.demo.databinding.SettingDialogFragmentBinding
import com.gdu.demo.utils.DeviceUtils

class SettingDialogFragment : DialogFragment() {

    private lateinit var binding: SettingDialogFragmentBinding
    private val menuList = mutableListOf<SettingMenuItem>()
    private var fragments = mutableListOf<Fragment?>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SettingDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()

    }

    private fun initView() {
        menuList.clear()
        menuList.add(SettingMenuItem(0, R.drawable.selector_set_fly, requireActivity().getString(R.string.title_fly)))
        menuList.add(SettingMenuItem(1, R.drawable.selector_set_z4b_battery, requireActivity().getString(R.string.title_battery)))
        menuList.add(SettingMenuItem(2, R.drawable.selector_set_img_channel, requireActivity().getString(R.string.title_imgChannel)))
        menuList.add(SettingMenuItem(3, R.drawable.selector_set_rtk, requireActivity().getString(R.string.title_rtk)))
        menuList.add(SettingMenuItem(4, R.drawable.selector_set_control, requireActivity().getString(R.string.title_control)))
        menuList.add(SettingMenuItem(5, R.drawable.selector_set_vision, requireActivity().getString(R.string.title_vision)))
        menuList.add(SettingMenuItem(6, R.drawable.selector_set_gimbal, requireActivity().getString(R.string.title_gimbal)))
        menuList.add(SettingMenuItem(7, R.drawable.selector_set_common, requireActivity().getString(R.string.title_common)))

        fragments = MutableList(menuList.size){ null}

        val leftAdapter = SettingLeftAdapter(requireContext())
        binding.rvLeft.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvLeft.adapter = leftAdapter
        leftAdapter.setNewInstance(menuList)

        leftAdapter.setOnItemClickListener { adapter1, view, position ->
            (adapter1 as SettingLeftAdapter).setSelectPosition(position)
            val menuItem: SettingMenuItem = adapter1.getItem(position)
            showSelectedIndex(menuItem.menuType)
        }

        showSelectedIndex(0)
    }

    private fun showSelectedIndex(currentMenuType: Int) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        when (currentMenuType) {
            0 -> {
                if (fragments[0] == null) {
                    val flyFragment: SettingFlyFragment = SettingFlyFragment.newInstance()
                    fragments[0] = flyFragment
                    fragmentTransaction.add(R.id.frame_layout, flyFragment)
                }
            }

            1 -> {
                if (fragments[1] == null) {
                    val flyFragment: SettingRcFragment = SettingRcFragment.newInstance()
                    fragments[1] = flyFragment
                    fragmentTransaction.add(R.id.frame_layout, flyFragment)
                }
            }
        }
        for (i in fragments.indices) {
            val fragment = fragments[i]
            fragment?.let {
                if (i == currentMenuType) {
                    fragmentTransaction.show(it)
                } else {
                    fragmentTransaction.hide(it)
                }
            }
        }
        fragmentTransaction.commitAllowingStateLoss()
    }


    private fun initData() {
    }


    override fun onStart() {
        super.onStart()
        initWindow()

    }

    private fun initWindow() {
        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.decorView.setPadding(0, 0, 0, 0)
            it.setGravity(Gravity.RIGHT)

            it.setLayout((DeviceUtils.getScreenWidth(requireContext()) * 0.6).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
            it.setWindowAnimations(R.style.SettingDialogAnim)
            val layoutParams = it.attributes
            // 背景变暗度  1背景全黑
            layoutParams.dimAmount = 0f
            //自身透明度  1 完全不透明
            layoutParams.alpha = 1f
            it.attributes = layoutParams
        }
    }


    companion object {

        fun show(manager: FragmentManager) {
            val args = Bundle()
            val dialogFragment = SettingDialogFragment()
            dialogFragment.arguments = args
            dialogFragment.show(manager, "SettingDialogFragment")
        }
    }

}