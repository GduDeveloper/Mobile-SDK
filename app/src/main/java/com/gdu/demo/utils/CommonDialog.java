package com.gdu.demo.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gdu.demo.R;
import com.gdu.util.logs.AppLog;

/**
 * @author wuqb
 * @date 2025/1/23
 * @description 通用弹窗
 */
public class CommonDialog extends AppCompatDialogFragment {

    /** 弹窗相关参数 */
    private DialogParam param;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CommonDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (param.layoutResId<=0){
            view = inflater.inflate(R.layout.dialog_common, container, false);
            TextView tvTitle = view.findViewById(R.id.dialog_title);
            TextView btnCancel = view.findViewById(R.id.dialog_cancel);
            TextView btnSure = view.findViewById(R.id.dialog_sure);
            //标题
            if(TextUtils.isEmpty(param.title)) {
                tvTitle.setVisibility(View.GONE);
            } else {
                tvTitle.setText(param.title);
            }
            //内容
            if (!TextUtils.isEmpty(param.content)) {
                TextView tv_content = view.findViewById(R.id.dialog_content);
                tv_content.setText(param.content);
            }
            //自定义内容区
            if (param.customContentView != null) {
                FrameLayout mCustomLayout = view.findViewById(R.id.dialog_custom_content);
                mCustomLayout.addView(param.customContentView);
                mCustomLayout.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(param.cancel)) {
                btnCancel.setText(param.cancel);
            }
            btnCancel.setVisibility(param.cancelVisible?View.VISIBLE:View.GONE);

            if (!TextUtils.isEmpty(param.sure)) {
                btnSure.setText(param.sure);
            }

            btnCancel.setOnClickListener(v -> {
                if (null!=getDialog() && getDialog().getWindow() != null && getDialog().isShowing()) {
                    dismiss();
                }
                if (null != param.negativeListener){
                    param.negativeListener.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                }
            });
            btnSure.setOnClickListener(v -> {
                if (null!=getDialog() && getDialog().getWindow() != null && getDialog().isShowing()) {
                    dismiss();
                }
                if (null != param.positiveListener){
                    param.positiveListener.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                }
            });
        }else {
            view = inflater.inflate(param.layoutResId, container, false);
            if (null != param.onBindViewListener){
                param.onBindViewListener.onBind(getDialog(), view);
            }
        }
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        createDialog();
    }

    /**
     * 创建弹窗及属性
     * */
    private void createDialog(){
        if (getContext() == null || getDialog() == null || getDialog().getWindow() == null){
            return;
        }
        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(param.isOutsideCancel);
        dialog.setCancelable(param.cancelable);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置宽高
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (param.mWidth > 0) {
            layoutParams.width = param.mWidth;
        } else {
            layoutParams.width = (int) getResources().getDimension(R.dimen.dp_260);
        }
        if (param.mHeight > 0) {
            layoutParams.height = param.mHeight;
        } else {
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        //透明度
        layoutParams.dimAmount = param.mDimAmount;
        window.setAttributes(layoutParams);
        dialog.setOnDismissListener(dialogInterface -> {
            if (null!=param.onDismissListener){
                param.onDismissListener.onDismiss(dialogInterface);
            }
        });
    }

    public CommonDialog show() {
        try {
            FragmentTransaction ft = param.fragmentManager.beginTransaction();
            ft.add(this, "CommomDialog");
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
        }
        return this;
    }

    public static class Builder{

        private final DialogParam P;

        public Builder(FragmentManager fragmentManager){
            P = new DialogParam();
            P.fragmentManager = fragmentManager;
        }

        public Builder setLayoutResId(int layoutResId){
            P.layoutResId = layoutResId;
            return this;
        }

        public Builder setTitle(String title){
            P.title = title;
            return this;
        }

        public Builder setContent(String content){
            P.content = content;
            return this;
        }

        public Builder setCancel(String cancel){
            P.cancel = cancel;
            return this;
        }

        public Builder setCancelVisible(boolean cancelVisible){
            P.cancelVisible = cancelVisible;
            return this;
        }

        public Builder setSure(String sure){
            P.sure = sure;
            return this;
        }

        /**
         * 设置弹窗在弹窗区域外是否可以取消
         *
         * @param isOutsideCancel  默认true
         */
        public Builder setCancelableOutside(boolean isOutsideCancel){
            P.isOutsideCancel = isOutsideCancel;
            return this;
        }

        /**
         * 设置弹窗是否可以取消
         *
         * @param cancelable  默认true
         */
        public Builder setCancelable(boolean cancelable){
            P.cancelable = cancelable;
            return this;
        }

        /**
         * 设置弹窗自定义Content布局
         *
         * @param view
         */
        public Builder setCustomContentView(View view){
            P.customContentView = view;
            return this;
        }

        /**
         * 设置弹窗宽度(单位:像素)
         *
         * @param widthPx
         */
        public Builder setWidth(int widthPx) {
            P.mWidth = widthPx;
            return this;
        }

        /**
         * 设置弹窗高度(px)
         *
         * @param heightPx
         */
        public Builder setHeight(int heightPx) {
            P.mHeight = heightPx;
            return this;
        }

        /**
         * 设置弹窗背景透明度(0-1f)
         *
         * @param dim 默认0.6f
         */
        public Builder setDimAmount(float dim) {
            P.mDimAmount = dim;
            return this;
        }

        /**
         * 取消按钮监听
         * */
        public Builder setNegativeListener(DialogInterface.OnClickListener negativeListener) {
            P.negativeListener = negativeListener;
            return this;
        }
        /**
         * 确认按钮监听
         * */
        public Builder setPositiveListener(DialogInterface.OnClickListener positiveListener) {
            P.positiveListener = positiveListener;
            return this;
        }

        /**
         * 确认按钮监听
         * */
        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            P.onDismissListener = onDismissListener;
            return this;
        }

        /**
         * 自定义Layout布局内容监听
         * */
        public Builder setLayoutBindViewListener(OnBindViewListener onBindViewListener) {
            P.onBindViewListener = onBindViewListener;
            return this;
        }

        public CommonDialog build(){
            CommonDialog commonDialog = new CommonDialog();
            commonDialog.param = P;
            return commonDialog;
        }
    }

    public static class DialogParam{
        /** 弹窗所需管理器 */
        private FragmentManager fragmentManager;
        /** 弹窗宽度 */
        private int mWidth;
        /** 弹窗高度 */
        private int mHeight;
        /** 弹窗背景透明度 */
        private float mDimAmount = 0.6f;
        /** 弹窗是否可以取消 */
        private boolean cancelable = true;
        /** 自定义布局文件资源 */
        private int layoutResId;
        /* 使用默认布局 */
        /** 默认布局title */
        private String title;
        /** 默认布局content */
        private String content;
        /** 默认布局取消按钮文案 */
        private String cancel;
        /** 设置取消按钮是否可见-用于切换单按钮布局 */
        private boolean cancelVisible = true;
        /** 默认布局确认按钮文案 */
        private String sure;
        /** 是否可以点击外部区域取消弹窗 */
        private boolean isOutsideCancel = true;
        /** 自定义内容布局 */
        private View customContentView;
        /** 取消按钮点击事件回调 */
        private DialogInterface.OnClickListener negativeListener;
        /** 确认按钮点击事件回调 */
        private DialogInterface.OnClickListener positiveListener;
        /** dismiss回调 */
        private DialogInterface.OnDismissListener onDismissListener;
        /** 自定义Layout布局回调 */
        private OnBindViewListener onBindViewListener;
    }

    public interface OnBindViewListener{
        void onBind(DialogInterface dialogInterface, View itemView);
    }
}
