package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingThirdGimbalBinding;
import com.gdu.util.SPUtils;


/**
 * @Author: lixiqiang
 * @Date: 2022/8/13
 */
public class SettingThirdGimbalFragment extends Fragment {

    private FragmentSettingThirdGimbalBinding binding;

    private Handler handler;

    private long lastTime;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingThirdGimbalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        boolean show = SPUtils.getTrueBoolean(requireContext(), SPUtils.FIVE_CAMERA_SHOW_VIEW);
        binding.ivShowView.setSelected(show);
        binding.ivShowView.setOnClickListener(listener);

        binding.ivSwitchOnOff.setSelected(GlobalVariable.fiveCameraOpen);
        binding.ivSwitchOnOff.setOnClickListener(listener);
    }



    private void initData() {
        handler = new Handler();
    }

    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_show_view:
                    changeShowView();
                    break;
                case R.id.iv_switch_on_off:
                    switchOnOff();
                    break;
                default:
                    break;
            }
        }
    };

    private void switchOnOff() {

        if (System.currentTimeMillis() - lastTime < 5 * 1000) {
            return;
        }
        lastTime = System.currentTimeMillis();
        boolean isOpen = !GlobalVariable.fiveCameraOpen;
//        GduApplication.getSingleApp().gduCommunication.fiveCameraSwitch(isOpen, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    GlobalVariable.fiveCameraOpen = isOpen;
//                    if (isAdded() && handler != null) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                binding.ivSwitchOnOff.setSelected(isOpen);
//                            }
//                        });
//                    }
//                }
//            }
//        });
    }


    private void changeShowView() {
        boolean isShow = !binding.ivShowView.isSelected();
        binding.ivShowView.setSelected(isShow);
        SPUtils.put(requireContext(), SPUtils.FIVE_CAMERA_SHOW_VIEW, isShow);
    }


    public static SettingThirdGimbalFragment newInstance() {
        Bundle args = new Bundle();
        SettingThirdGimbalFragment fragment = new SettingThirdGimbalFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
