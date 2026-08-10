package com.gdu.demo.widget.rc;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.gdu.demo.R;
import com.gdu.demo.databinding.RcCustomKeyViewBinding;
import com.gdu.demo.databinding.RcCustomKeyViewMenuPopBinding;
import com.gdu.demo.utils.ScreenUtils;
import com.gdu.demo.widget.rc.key.CustomKeyActionPopupWindow;
import com.gdu.demo.widget.rc.key.IActionViewKey;
import com.gdu.demo.widget.rc.key.IDiyViewKey;
import com.gdu.demo.widget.rc.key.RCCustomKeyRepository;
import com.gdu.demo.widget.rc.key.RCCustomKeyViewModel;
import com.gdu.demo.widget.rc.key.RCKeyActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: lixiqiang
 * @Date: 2022/8/4
 */
public class RCCustomKeyNewView extends RelativeLayout {

    private final Context context;
    private RcCustomKeyViewBinding viewBinding;

    private RcCustomKeyMenAdapter menAdapter;
    private final RCCustomKeyViewModel viewModel = new RCCustomKeyViewModel();

    private TextView currentTextView;
    private int clickViewType = -1;

    private PopupWindow mMenuPop;
    private CustomKeyActionPopupWindow actionPopupWindow;

    private static final int TAG_KEY_VIEW_KEY = R.id.tv_c_title;

    public RCCustomKeyNewView(Context context) {
        this(context, null);
    }

    public RCCustomKeyNewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RCCustomKeyNewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        setListener();
        initData();
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_custom_key_view, this);
        viewBinding = RcCustomKeyViewBinding.bind(view);
        initMenuPop();
    }

    private void initMenuPop() {
        RcCustomKeyViewMenuPopBinding menuPopBinding = RcCustomKeyViewMenuPopBinding.inflate(LayoutInflater.from(getContext()));
        menAdapter = new RcCustomKeyMenAdapter();
        menuPopBinding.rvMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        menuPopBinding.rvMenu.setAdapter(menAdapter);
        setMenu(0);
        menAdapter.setOnItemClickListener((adapter, view1, position) -> {
            RCKeyActionItem menu = (RCKeyActionItem) adapter.getItem(position);
            if (menu != null) {
                onActionSelected(menu);
            }
        });

        List<SettingMenuItem> list = new ArrayList<>();
        list.add(new SettingMenuItem(1, R.drawable.rc_custom_key_camera, ""));
        list.add(new SettingMenuItem(2, R.drawable.rc_custom_key_gimbal, ""));
        list.add(new SettingMenuItem(3, R.drawable.rc_custom_key_app, ""));
        list.add(new SettingMenuItem(4, R.drawable.rc_custom_key_flight_control, ""));

        RcMenuTitleAdapter titleAdapter = new RcMenuTitleAdapter();
        menuPopBinding.rvTitle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        menuPopBinding.rvTitle.setAdapter(titleAdapter);
        titleAdapter.setNewInstance(list);
        titleAdapter.setOnItemClickListener((adapter, view12, position) -> {
            titleAdapter.setSelectPosition(position);
            setMenu(position);
        });
        menuPopBinding.ivClose.setOnClickListener(v -> {
            if (mMenuPop != null) {
                mMenuPop.dismiss();
            }
        });
        mMenuPop = new PopupWindow(menuPopBinding.getRoot());
        mMenuPop.setWidth(getResources().getDimensionPixelSize(R.dimen.dp_230));
        mMenuPop.setHeight(ScreenUtils.getScreenHeight(getContext()) - getResources().getDimensionPixelSize(R.dimen.dp_140));
        mMenuPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mMenuPop.setFocusable(false);
        mMenuPop.setOutsideTouchable(true);
    }

    private void onActionSelected(RCKeyActionItem menu) {
        mMenuPop.dismiss();
        if (currentTextView == null || clickViewType == -1) {
            return;
        }
        currentTextView.setText(menu.getActionName());

        if (clickViewType > 100) {
            // 组合按键的逻辑
            // TODO: 组合按键动作保存（如有需要可扩展）
        } else {
            // 单按键保存动作
            viewModel.saveViewAndAction(clickViewType, menu.getActionId());
        }
    }

    private void setMenu(int position) {
        List<RCKeyActionItem> list = new ArrayList<>();
        if (position == 0) {
            list = RCCustomKeyRepository.INSTANCE.getCameraActionList();
        } else if (position == 1) {
            list = RCCustomKeyRepository.INSTANCE.getGimbalActionList();
        } else if (position == 2) {
            list = RCCustomKeyRepository.INSTANCE.getAppActionList();
        } else if (position == 3) {
            list = RCCustomKeyRepository.INSTANCE.getFcActionList();
        }
        menAdapter.setNewInstance(list);
    }

    private void bindViewAction(TextView view, int rcKeyId) {
        view.setTag(TAG_KEY_VIEW_KEY, rcKeyId);
        view.setText(viewModel.findActionNameByRCKey(rcKeyId));
    }

    private void bindDiyViewAndRCKey(com.gdu.demo.widget.GduSpinner view, int diyKeyViewId) {
        view.setTag(TAG_KEY_VIEW_KEY, diyKeyViewId);
        view.setIndex(viewModel.findDiyRCKeyByDiyView(diyKeyViewId));
    }

    private int getBindViewKey(View view) {
        Object tag = view.getTag(TAG_KEY_VIEW_KEY);
        return tag instanceof Integer ? (Integer) tag : 0;
    }

    private void setListener() {
        OnClickListener actionClickListener = v -> {
//            if (currentTextView != null) {
//                currentTextView.setBackgroundResource(R.drawable.spinner_bg_r2_arrow);
//            }
            currentTextView = (TextView) v;
//            currentTextView.setBackgroundResource(R.drawable.spinner_bg_r2_arrow_selected);
            showActionPopupWindow();
        };

        viewBinding.tvC1.setOnClickListener(actionClickListener);
        viewBinding.tvC2.setOnClickListener(actionClickListener);
        viewBinding.tvL1.setOnClickListener(actionClickListener);
        viewBinding.tvL2.setOnClickListener(actionClickListener);
        viewBinding.tvR1.setOnClickListener(actionClickListener);
        viewBinding.tvR2.setOnClickListener(actionClickListener);
        viewBinding.tvTop.setOnClickListener(actionClickListener);
        viewBinding.tvBot.setOnClickListener(actionClickListener);
        viewBinding.tvLeft.setOnClickListener(actionClickListener);
        viewBinding.tvRight.setOnClickListener(actionClickListener);
        viewBinding.tvCenter.setOnClickListener(actionClickListener);
        viewBinding.tvComb1.setOnClickListener(actionClickListener);
        viewBinding.tvComb2.setOnClickListener(actionClickListener);
        viewBinding.tvComb3.setOnClickListener(actionClickListener);

        bindViewAction(viewBinding.tvC1, IActionViewKey.VIEW_C1);
        bindViewAction(viewBinding.tvC2, IActionViewKey.VIEW_C2);
        bindViewAction(viewBinding.tvL1, IActionViewKey.VIEW_L1);
        bindViewAction(viewBinding.tvL2, IActionViewKey.VIEW_L2);
        bindViewAction(viewBinding.tvR1, IActionViewKey.VIEW_R1);
        bindViewAction(viewBinding.tvR2, IActionViewKey.VIEW_R2);
        bindViewAction(viewBinding.tvTop, IActionViewKey.VIEW_FIVE_UP);
        bindViewAction(viewBinding.tvBot, IActionViewKey.VIEW_FIVE_BOTTOM);
        bindViewAction(viewBinding.tvLeft, IActionViewKey.VIEW_FIVE_LEFT);
        bindViewAction(viewBinding.tvRight, IActionViewKey.VIEW_FIVE_RIGHT);
        bindViewAction(viewBinding.tvCenter, IActionViewKey.VIEW_FIVE_CENTER);
        bindViewAction(viewBinding.tvComb1, IActionViewKey.VIEW_DIY1);
        bindViewAction(viewBinding.tvComb2, IActionViewKey.VIEW_DIY2);
        bindViewAction(viewBinding.tvComb3, IActionViewKey.VIEW_DIY3);

        bindDiyViewAndRCKey(viewBinding.ovComb11, IDiyViewKey.VIEW_DIY1_LEFT);
        bindDiyViewAndRCKey(viewBinding.ovComb21, IDiyViewKey.VIEW_DIY2_LEFT);
        bindDiyViewAndRCKey(viewBinding.ovComb31, IDiyViewKey.VIEW_DIY3_LEFT);
        bindDiyViewAndRCKey(viewBinding.ovComb12, IDiyViewKey.VIEW_DIY1_RIGHT);
        bindDiyViewAndRCKey(viewBinding.ovComb22, IDiyViewKey.VIEW_DIY2_RIGHT);
        bindDiyViewAndRCKey(viewBinding.ovComb32, IDiyViewKey.VIEW_DIY3_RIGHT);

        setDiyOptionClickListener(viewBinding.ovComb11);
        setDiyOptionClickListener(viewBinding.ovComb21);
        setDiyOptionClickListener(viewBinding.ovComb31);
        setDiyOptionClickListener(viewBinding.ovComb12);
        setDiyOptionClickListener(viewBinding.ovComb22);
        setDiyOptionClickListener(viewBinding.ovComb32);

        viewBinding.ivDelOne.setOnClickListener(v -> {
            // TODO: 删除组合按键1
        });
        viewBinding.ivDelTwo.setOnClickListener(v -> {
            // TODO: 删除组合按键2
        });
    }

    private void setDiyOptionClickListener(com.gdu.demo.widget.GduSpinner view) {
        view.setOnOptionClickListener((parentId, v, position) -> {
            int diyViewId = getBindViewKey(view);
            if (viewModel.isSameDiyKey(diyViewId, position)) {
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
            } else {
                viewModel.saveDiyViewAndRCKey(diyViewId, position);
                view.setIndex(position);
                Toast.makeText(context, R.string.string_set_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showActionPopupWindow() {
        if (actionPopupWindow == null) {
            actionPopupWindow = new CustomKeyActionPopupWindow(context);
            actionPopupWindow.setOnDismissListener(() -> {
                if (currentTextView != null) {
                    currentTextView.setBackgroundResource(R.drawable.spinner_bg_r2_arrow);
                }
            });
            actionPopupWindow.setOnChooseListener(action -> {
                if (currentTextView != null) {
                    currentTextView.setText(action.getActionName());
                    int rcKeyId = getBindViewKey(currentTextView);
                    viewModel.saveViewAndAction(rcKeyId, action.getActionId());
                }
                return null;
            });
        }
        if (!actionPopupWindow.isShowing()) {
            actionPopupWindow.show(viewBinding.getRoot());
        }
    }

    private void initData() {
        // 单按键已通过bindViewAction初始化文本
        // 组合按键默认值
        setCombKey(101);
        setCombKey(102);
        setCombKey(103);
    }

    private void setCombKey(int type) {
        if (type == 101) {
            viewBinding.ovComb11.setIndex(getPos1ByKey(1));
            viewBinding.ovComb12.setIndex(getPos2ByKey(2));
            viewBinding.tvComb1.setText(context.getString(R.string.string_rc_key_no));
        } else if (type == 102) {
            viewBinding.ovComb21.setIndex(getPos1ByKey(3));
            viewBinding.ovComb22.setIndex(getPos2ByKey(5));
            viewBinding.tvComb2.setText(context.getString(R.string.string_rc_key_no));
        } else if (type == 103) {
            viewBinding.ovComb31.setIndex(getPos1ByKey(4));
            viewBinding.ovComb32.setIndex(getPos2ByKey(6));
            viewBinding.tvComb3.setText(context.getString(R.string.string_rc_key_no));
        }
    }

    private int getKey1ValByPos(int pos) {
        switch (pos) {
            case 0: return 1;
            case 1: return 3;
            case 2: return 4;
            default: return 0;
        }
    }

    private int getPos1ByKey(int key) {
        switch (key) {
            case 1: return 0;
            case 3: return 1;
            case 4: return 2;
            default: return 0;
        }
    }

    private int getKey2ValByPos(int pos) {
        switch (pos) {
            case 0: return 2;
            case 1: return 5;
            case 2: return 6;
            default: return 0;
        }
    }

    private int getPos2ByKey(int key) {
        switch (key) {
            case 2: return 0;
            case 5: return 1;
            case 6: return 2;
            default: return 0;
        }
    }
}
