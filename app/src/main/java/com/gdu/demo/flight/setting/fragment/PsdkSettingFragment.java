package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentPsdkSettingBinding;
import com.gdu.demo.flight.setting.bean.PSdkCustomViewBean;
import com.gdu.demo.flight.setting.bean.WidgetItemBean;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.demo.widget.SettingButtonLayout;
import com.gdu.demo.widget.SettingIntInputLayout;
import com.gdu.demo.widget.SettingListLayout;
import com.gdu.demo.widget.SettingScaleLayout;
import com.gdu.demo.widget.SettingSwitchLayout;
import com.gdu.demo.widget.SettingTextInputLayout;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.NumberUtils;
import com.gdu.util.StringUtils;
import com.rxjava.rxlife.RxLife;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/24
 */
public class PsdkSettingFragment extends Fragment {

    private FragmentPsdkSettingBinding binding;

    private Handler handler;

    private PSdkCustomViewBean viewBean;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPsdkSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler();
        initView();
        initData();
    }

    private void initView() {

        getCustomViewData();

        binding.ivShowData.setOnClickListener(listener);

        binding.llRoot.removeAllViews();

        if (viewBean != null && viewBean.getConfig_interface() != null && viewBean.getMain_interface().getFloating_window() != null
                && viewBean.getMain_interface().getFloating_window().getIs_enable()) {
            binding.layoutShowData.setVisibility(View.VISIBLE);
            binding.ivShowData.setSelected(GlobalVariable.isShowCurrentData);
        } else {
            binding.layoutShowData.setVisibility(View.GONE);
        }

        if (viewBean != null && viewBean.getConfig_interface() != null && viewBean.getConfig_interface().getText_input_box() != null
                && viewBean.getConfig_interface().getText_input_box().getIs_enable()) {
            addTextInputLayout(viewBean);
        }

        if (viewBean != null && viewBean.getConfig_interface() != null) {
            List<WidgetItemBean> list = viewBean.getConfig_interface().getWidget_list();
            for (int i = 0; i < list.size(); i++) {
                WidgetItemBean item = list.get(i);
                if (item != null) {
                    if ("button".equals(item.getWidget_type())) {
                        addButtonView(getContext(), binding.llRoot, item);
                    } else if ("switch".equals(item.getWidget_type())) {
                        addSwitch(getContext(), binding.llRoot, item);
                    } else if ("int_input_box".equals(item.getWidget_type())) {
                        addIntInputBox(getContext(), binding.llRoot, item);
                    } else if ("list".equals(item.getWidget_type())) {
                        addListView(getContext(), binding.llRoot, item);
                    } else if ("scale".equals(item.getWidget_type())) {
                        addScaleView(getContext(), binding.llRoot, item);
                    }
                }
            }
        }

        Observable.interval(0, 2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .to(RxLife.toMain(this))
                .subscribe(aLong -> {
                    updateStates();
                }, throwable ->{

                });

    }

    private void updateStates() {
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
             if (GlobalVariable.psdkSynStates == 5) {
                 binding.tvSynStatus.setText(R.string.str_has_syn);
            } else {
                 binding.tvSynStatus.setText(R.string.str_no_syn);
            }
        } else {
            binding.tvSynStatus.setText(R.string.DeviceNoConn);
        }

    }

    private void getCustomViewData() {
//        if (getActivity() instanceof ZorroRealControlActivity) {
//            ZorroRealControlActivity activity = (ZorroRealControlActivity) getActivity();
//            if (activity != null) {
//                viewBean = activity.getCustomViewBean();
//            }
//        }
    }


    private final View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_show_data:
                    boolean show = !binding.ivShowData.isSelected();
                    GlobalVariable.isShowCurrentData = show;
                    binding.ivShowData.setSelected(show);
//                    EventBus.getDefault().post(new ShowDataEvent());
                    break;
                default:
                    break;
            }

        }
    };

    private void addTextInputLayout(PSdkCustomViewBean bean) {
        SettingTextInputLayout layout = new SettingTextInputLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        layout.tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = layout.editText.getText().toString();
//                KeyboardUtils.hideKeyboard(layout.editText);
//                if (!TextUtils.isEmpty(input)) {
//                    GduApplication.getSingleApp().gduCommunication.psdkWidgetSetText(0xffff, (byte) 6, input, new SocketCallBack3() {
//                        @Override
//                        public void callBack(int code, GduFrame3 bean) {
//                            if (code == GduConfig.OK) {
//                            }
//                        }
//                    });
//                }
            }
        });
        if (bean.getConfig_interface().getText_input_box() != null
                && bean.getConfig_interface().getText_input_box().getWidget_name()!=null) {
            layout.setName(bean.getConfig_interface().getText_input_box().getWidget_name());
        }
        binding.llRoot.addView(layout);
    }

    private void initData() {
    }


    private void addButtonView(Context context, LinearLayout layout, WidgetItemBean itemBean) {

        SettingButtonLayout buttonLayout = new SettingButtonLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonLayout.setText(itemBean.getWidget_name());
        buttonLayout.setLayoutParams(params);


        buttonLayout.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeButtonStatus(itemBean.getWidget_index(), (byte) 1);
            }
        });
        layout.addView(buttonLayout);
    }

    private void addSwitch(Context context, LinearLayout layout, WidgetItemBean itemBean) {

        SettingSwitchLayout switchLayout = new SettingSwitchLayout(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        switchLayout.setLayoutParams(layoutParams);
        switchLayout.setName(itemBean.getWidget_name());
        switchLayout.iv_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int change = switchLayout.iv_switch.isSelected() ? 0 : 1;
//                GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 2, change, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (code == GduConfig.OK && isAdded()) {
//                                    switchLayout.iv_switch.setSelected(change == 1);
//                                }
//                            }
//                        });
//                    }
//                });
            }
        });
        layout.addView(switchLayout);

//        GduApplication.getSingleApp().gduCommunication.psdkWidgetGetState((short) itemBean.getWidget_index(), (byte) 2, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    int state = ByteUtilsLowBefore.byte2Int(bean.frameContent, 5);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (code == GduConfig.OK && isAdded()) {
//                                switchLayout.iv_switch.setSelected(state == 1);
//                            }
//                        }
//                    });
//                }
//            }
//        });

    }

    private void addScaleView(Context context, LinearLayout layout, WidgetItemBean itemBean) {
        SettingScaleLayout scaleLayout = new SettingScaleLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        scaleLayout.setLayoutParams(layoutParams);
        scaleLayout.setName(itemBean.getWidget_name());

        scaleLayout.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                scaleLayout.tv_progress.setText(seekBar.getProgress() + "");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int progress = seekBar.getProgress();
//                GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 3, progress, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (code == GduConfig.OK && isAdded()) {
//                                    scaleLayout.setProgress(progress);
//                                }
//                            }
//                        });
//                    }
//                });

            }
        });
        layout.addView(scaleLayout);

//        GduApplication.getSingleApp().gduCommunication.psdkWidgetGetState((short) itemBean.getWidget_index(), (byte) 3, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    int state = ByteUtilsLowBefore.byte2Int(bean.frameContent, 5);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            scaleLayout.setProgress(state);
//                        }
//                    });
//                }
//            }
//        });
    }

    private void addListView(Context context, LinearLayout layout, WidgetItemBean itemBean) {
        SettingListLayout listLayout = new SettingListLayout(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        listLayout.setLayoutParams(layoutParams);
        listLayout.setName(itemBean.getWidget_name());
        List<String> list = new ArrayList<>();
        for (int i = 0; i < itemBean.getList_item().size(); i++) {
            list.add(itemBean.getList_item().get(i).getItem_name());
        }
        listLayout.setArrayData(list);

        listLayout.optionView.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {

//                GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 4, position, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        if (code == GduConfig.OK && isAdded()) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    listLayout.optionView.setIndex(position);
//                                }
//                            });
//                        }
//                    }
//                });
            }
        });

        layout.addView(listLayout);

//        GduApplication.getSingleApp().gduCommunication.psdkWidgetGetState((short) itemBean.getWidget_index(), (byte) 4, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    int state = ByteUtilsLowBefore.byte2Int(bean.frameContent, 5);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (isAdded() && state < list.size()) {
//                                listLayout.optionView.setIndex(state);
//                            }
//                        }
//                    });
//                }
//            }
//        });
    }

    private void addIntInputBox(Context context, LinearLayout layout, WidgetItemBean itemBean) {
        SettingIntInputLayout intInputLayout = new SettingIntInputLayout(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        intInputLayout.setLayoutParams(layoutParams);
        intInputLayout.setName(itemBean.getWidget_name());
        layout.addView(intInputLayout);

        intInputLayout.editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String value = textView.getText().toString();
                    if (StringUtils.isEmptyString(value) || !NumberUtils.isNumeric(value)) {
                        Toast.makeText(requireContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    int valueInt = Integer.parseInt(value);
//                    GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 5, valueInt, new SocketCallBack3() {
//                        @Override
//                        public void callBack(int code, GduFrame3 bean) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (code == GduConfig.OK && isAdded()) {
//                                        Toaster.show(getString(R.string.string_set_success));
//                                    }
//                                }
//                            });
//                        }
//                    });
                }
                return false;
            }
        });


//        GduApplication.getSingleApp().gduCommunication.psdkWidgetGetState((short) itemBean.getWidget_index(), (byte) 5, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    int state = ByteUtilsLowBefore.byte2Int(bean.frameContent, 5);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (isAdded()) {
//                                intInputLayout.editText.setText(state + "");
//                            }
//                        }
//                    });
//                }
//            }
//        });
    }

    private void changeButtonStatus(int id, byte type) {
//        GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) id, (byte) 1, type, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (code == GduConfig.OK) {
//                        } else {
//                        }
//                    }
//                });
//            }
//        });

    }

    public static PsdkSettingFragment newInstance() {
        Bundle args = new Bundle();
        PsdkSettingFragment fragment = new PsdkSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
