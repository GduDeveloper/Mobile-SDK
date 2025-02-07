package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.demo.databinding.FragmentSettingPsdkCameraBinding;


public class SettingPSdkCameraFragment extends Fragment {

    private FragmentSettingPsdkCameraBinding mViewBinding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingPsdkCameraBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initData();
    }

    private void initData() {
    }

    private void initView() {

    }


    public static SettingPSdkCameraFragment newInstance() {
        Bundle args = new Bundle();
        SettingPSdkCameraFragment fragment = new SettingPSdkCameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
