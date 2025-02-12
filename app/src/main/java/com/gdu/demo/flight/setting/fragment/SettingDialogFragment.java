package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.SettingDialogFragmentBinding;
import com.gdu.demo.flight.setting.adapter.SettingLeftAdapter;
import com.gdu.demo.utils.ToolManager;
import com.gdu.demo.widget.rc.SettingMenuItem;
import com.gdu.drone.GimbalType;
import com.gdu.sdk.airlink.AirlinkUtils;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.LanguageUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.StatusBarUtils;
import com.gdu.util.ViewUtils;

import java.util.ArrayList;

/**
 * 飞行设置弹窗
 * @Author: lixiqiang
 * @Date: 2022/5/7
 */
public class SettingDialogFragment extends DialogFragment {

    private SettingDialogFragmentBinding binding;
    private FragmentManager fragmentManager;

    private final ArrayList<SettingMenuItem> menuList = new ArrayList<>();

    private Fragment[] fragments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingDialogFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLocalConfig();
        hideNavigationBar();
        initView();

        initFragment();
        initData(savedInstanceState);
    }

    private void initLocalConfig() {
        LanguageUtil.initLanguageConfig(requireActivity());
    }

    private void initFragment() {
        menuList.clear();
        menuList.add(new SettingMenuItem(0, R.drawable.selector_set_fly, requireActivity().getString(R.string.title_fly)));
        menuList.add(new SettingMenuItem(1, R.drawable.selector_set_z4b_battery, requireActivity().getString(R.string.title_battery)));
        menuList.add(new SettingMenuItem(2, R.drawable.selector_set_img_channel, requireActivity().getString(R.string.title_imgChannel)));
        menuList.add(new SettingMenuItem(3, R.drawable.selector_set_rtk, requireActivity().getString(R.string.title_rtk)));
        menuList.add(new SettingMenuItem(4, R.drawable.selector_set_control, requireActivity().getString(R.string.title_control)));
        menuList.add(new SettingMenuItem(5, R.drawable.selector_set_vision, requireActivity().getString(R.string.title_vision)));
        menuList.add(new SettingMenuItem(6, R.drawable.selector_set_gimbal, requireActivity().getString(R.string.title_gimbal)));
        menuList.add(new SettingMenuItem(7, R.drawable.selector_set_common, requireActivity().getString(R.string.title_common)));
        fragments = new Fragment[menuList.size()];
    }

    private void hideNavigationBar() {
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                getDialog().setOnShowListener(dialogInterface -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    ToolManager.hideNavigationBar(window);
                });
            }
        }
    }

    private void initView() {
        binding.ivCancel.setOnClickListener(listener);
    }

    private void initData(Bundle savedInstanceState) {
        fragmentManager = getChildFragmentManager();
        SettingLeftAdapter adapter = new SettingLeftAdapter();
        binding.rvLeft.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvLeft.setAdapter(adapter);
        adapter.setNewInstance(menuList);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            ((SettingLeftAdapter) adapter1).setSelectPosition(position);
            SettingMenuItem menuItem = (SettingMenuItem) adapter1.getItem(position);
            if (menuItem != null) {
                showSelectedIndex(menuItem.getMenuType());
            }
            hideInputMethod(view.getWindowToken());
        });
        if (savedInstanceState == null) {
            showSelectedIndex(0);
        }

        AirlinkUtils.getUnique();
    }


    private void showSelectedIndex(int currentMenuType) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (currentMenuType) {
            case 0:
                if (fragments[0] == null) {
                    SettingFlyFragment flyFragment = SettingFlyFragment.newInstance();
                    fragments[0] = flyFragment;
                    fragmentTransaction.add(R.id.frame_layout, flyFragment);
                }
                break;
            case 1:
                if (fragments[1] == null) {
                    SettingBatteryFragment batteryFragment = SettingBatteryFragment.newInstance();
                    fragments[1] = batteryFragment;
                    fragmentTransaction.add(R.id.frame_layout, batteryFragment);
                }
                break;
            case 2:
                if (fragments[2] == null) {
                    SettingImageChannelFragment imageChannelFragment = SettingImageChannelFragment.newInstance();
                    fragments[2] = imageChannelFragment;
                    fragmentTransaction.add(R.id.frame_layout, imageChannelFragment);
                }
                break;
            case 3:
                if (fragments[3] == null) {
                    SettingRtkFragment rtkFragment = SettingRtkFragment.newInstance();
                    fragments[3]= (rtkFragment);
                    fragmentTransaction.add(R.id.frame_layout, rtkFragment);
                }
                break;
            case 4:
                if (fragments[4] == null) {
                    SettingRControlFragment rControlFragment = SettingRControlFragment.newInstance();
                    fragments[4] = rControlFragment;
                    fragmentTransaction.add(R.id.frame_layout, rControlFragment);
                }
                break;
            case 5:
                if (fragments[5] == null) {
                    SettingVisionFragment visionFragment = SettingVisionFragment.newInstance();
                    fragments[5] = visionFragment;
                    fragmentTransaction.add(R.id.frame_layout, visionFragment);
                }
                break;
            case 6:
                if (fragments[6] == null) {
                    SettingCameraFragment cameraFragment = SettingCameraFragment.newInstance();
                    fragments[6] = (cameraFragment);
                    fragmentTransaction.add(R.id.frame_layout, cameraFragment);
                }
                break;
            case 7:
                if (fragments[7] == null) {
                    SettingCommonFragment commonFragment = SettingCommonFragment.newInstance();
                    fragments[7] = (commonFragment);
                    fragmentTransaction.add(R.id.frame_layout, commonFragment);
                }
                break;
            default:
                break;
        }

        for (int i = 0; i < fragments.length; i++) {
            Fragment fragment = fragments[i];
            if (fragment != null) {
                if (i == currentMenuType) {
                    fragmentTransaction.show(fragment);
                } else {
                    fragmentTransaction.hide(fragment);
                }
            }
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void hideInputMethod(IBinder windowToken) {
        FragmentActivity activity = getActivity();
        if(activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public View.OnClickListener listener = view -> {
        if (view.getId() == R.id.iv_cancel) {
            dismiss();
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        initWindow();
    }

    private void initWindow() {
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setGravity(Gravity.RIGHT);

        int weight = (int) (ViewUtils.getWindowWidth(getContext()) * 0.6);
        window.setLayout(weight, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setWindowAnimations(R.style.SettingDialogAnim);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        // 背景变暗度  1背景全黑
        layoutParams.dimAmount = 0f;
        //自身透明度  1 完全不透明
        layoutParams.alpha = 1f;
        window.setAttributes(layoutParams);
        ToolManager.hideNavigationBar(window);
    }




    public static void show(FragmentManager manager) {
        Bundle args = new Bundle();
        SettingDialogFragment fragment = new SettingDialogFragment();
        fragment.setArguments(args);
        fragment.show(manager, "SettingDialogFragment");
    }
}
