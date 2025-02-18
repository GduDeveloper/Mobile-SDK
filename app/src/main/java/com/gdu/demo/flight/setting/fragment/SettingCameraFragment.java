package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingCameraBinding;
import com.gdu.demo.flight.setting.camera.CameraSetHelper;
import com.gdu.demo.flight.setting.camera.VLCameraSetHelper;
import com.gdu.event.GimbalEvent;
import com.lib.model.LiveType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingCameraFragment extends Fragment {

    private FragmentSettingCameraBinding mViewBinding;
    private VLCameraSetHelper mCameraSetHelper;

    private int currentSecondLevelType = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingCameraBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        initView();
        initData();
    }


    private void initView() {
        matchUI2Camera();
        mCameraSetHelper.setCloseListener(() -> {
            if (getParentFragment() != null) {
                ((SettingDialogFragment) getParentFragment()).dismiss();
            }
        });
    }

    private void initData() {
    }


    private void matchUI2Camera() {
        mCameraSetHelper = new VLCameraSetHelper(mViewBinding.layoutCamera.getRoot(), requireActivity());
    }

    public static SettingCameraFragment newInstance() {
        Bundle args = new Bundle();
        SettingCameraFragment fragment = new SettingCameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connGimbal(GimbalEvent event) {
        if (!isAdded()) {
            return;
        }
        if(mCameraSetHelper!=null){
            mCameraSetHelper.connGimbalListener(event);
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
        if (mCameraSetHelper != null) {
            mCameraSetHelper.onDestory();
            mCameraSetHelper = null;
        }
    }
}
