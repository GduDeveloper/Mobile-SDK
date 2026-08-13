package com.gdu.demo.widget.rc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.demo.R;
import com.gdu.demo.databinding.RcCustomKeyViewBinding;
import com.gdu.demo.widget.rc.key.CustomKeyActionPopupWindow;
import com.gdu.demo.widget.rc.key.IActionViewKey;
import com.gdu.demo.widget.rc.key.IDiyViewKey;
import com.gdu.demo.widget.rc.key.RCCustomKeyViewModel;
import com.gdu.lib.util.core.XLogger;


/**
 * @Author: lixiqiang
 * @Date: 2022/8/4
 */
public class RCCustomKeyNewView extends RelativeLayout {

    private final Context context;
    private RcCustomKeyViewBinding viewBinding;

    private final RCCustomKeyViewModel viewModel = new RCCustomKeyViewModel();

    private TextView currentTextView;

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
                XLogger.APP.i("RCKeyAction", "start setOnChooseListener " + Thread.currentThread().getName());
                if (currentTextView != null) {
                    currentTextView.setText(action.getActionName());
                    currentTextView.requestLayout();
                    currentTextView.postInvalidate();
                    int rcKeyId = getBindViewKey(currentTextView);
                    viewModel.saveViewAndAction(rcKeyId, action.getActionId());
                }
                XLogger.INSTANCE.getAPP().i("RCKeyAction", "end setOnChooseListener ActionName = " + action.getActionName());
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
    }

}
