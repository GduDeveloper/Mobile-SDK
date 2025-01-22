package com.gdu.demo.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdu.demo.databinding.FragmentSettingFlyBinding

class SettingFlyFragment : Fragment() {


    private lateinit var binding: FragmentSettingFlyBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingFlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object{
        fun newInstance(): SettingFlyFragment {
            val args = Bundle()
            val fragment = SettingFlyFragment()
            fragment.arguments = args
            return fragment
        }
    }

}









