package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.databinding.FragmentSettingLidarBinding;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.SocketCallBack3;

/**
 * @Author: lixiqiang
 * @Date: 2022/9/17
 */
public class SettingLidarFragment extends Fragment {

    private FragmentSettingLidarBinding binding;
    public Handler handler;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingLidarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initData();
    }

    private void initData() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            return;
        }
//        GduApplication.getSingleApp().gduCommunication.getLidarVersion(new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK && bean != null && bean.frameContent != null) {
//                    String version = bean.frameContent[2] + "." + bean.frameContent[3] + "." + bean.frameContent[4];
//                    if (isAdded() && handler != null) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                binding.tvVersion.setText(version);
//                            }
//                        });
//                    }
//                }
//            }
//        });
//
//        GduApplication.getSingleApp().gduCommunication.getLidarSn(new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK && bean != null && bean.frameContent != null) {
//                    byte[] snArray = new byte[17];
//                    System.arraycopy(bean.frameContent, 2, snArray, 0, 17);
//                    String sn = new String(snArray);
//                    //雷达机芯SN码
//                    byte[] snRealAry = new byte[12];
//                    System.arraycopy(bean.frameContent, 21, snRealAry, 0, 12);
//                    String snReal = new String(snRealAry);
//
//                    if (isAdded() && handler != null) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                binding.tvSn.setText(sn);
//                                binding.tvRealSn.setText(snReal);
//                            }
//                        });
//                    }
//                }
//            }
//        });
    }

    private void initView() {
        handler = new Handler();
    }


    public static SettingLidarFragment newInstance() {
        Bundle args = new Bundle();
        SettingLidarFragment fragment = new SettingLidarFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
