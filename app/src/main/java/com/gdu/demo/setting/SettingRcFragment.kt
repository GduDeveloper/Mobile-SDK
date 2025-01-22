package com.gdu.demo.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdu.demo.databinding.FragmentSettingFlyBinding
import com.gdu.demo.databinding.FragmentSettingRcBinding

class SettingRcFragment : Fragment() {


    private lateinit var binding: FragmentSettingRcBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingRcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object{
        fun newInstance(): SettingRcFragment {
            val args = Bundle()
            val fragment = SettingRcFragment()
            fragment.arguments = args
            return fragment
        }
    }

}









