package com.gdu.demo.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gdu.demo.R;

/**
 * @Author: fuchi
 * @date: 2023/8/29 20:16
 * @description:
 */
public class LoadingDialogUtils {

    public static Dialog loadingDialog;


    public static void createLoadDialog(Context context){
        createLoadDialog(context, "");
    }

    public static void createLoadDialog(Context context, String tv) {
        createLoadDialog(context, tv, true);
    }

    public static void createLoadDialog(Context context, boolean isCancelable) {
        createLoadDialog(context, "", isCancelable);
    }

    public static void createLogLoadDialog(Context context, String tv, boolean isCancelableAndFinish){
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }
        if(!checkActivityIsOk(context)) {
            return;
        }
        loadingDialog = new Dialog(context, R.style.CommonDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.loading, null);
        ImageView gifLoading = view.findViewById(R.id.iv_gif_loading);
        Glide.with(gifLoading).asGif().load(R.drawable.loading_anim).fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.DATA).into(gifLoading);
        TextView tv_load_dialog = view.findViewById(R.id.tv_load_dialog);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(isCancelableAndFinish);
        //此处用于处理当dialog弹出时，会拦截activity的onBackPress事件
        loadingDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 在这里处理Back键被按下的事件
                ((FragmentActivity)context).finish();
//                dialog.dismiss();
                return false;
            }
            return true;
        });
        tv_load_dialog.setText(tv);
        if(TextUtils.isEmpty(tv)) {
            tv_load_dialog.setVisibility(View.GONE);
        }
        loadingDialog.setContentView(view);
        loadingDialog.setOnCancelListener(dialog -> loadingDialog = null);
        ToolManager.focusNotAle(loadingDialog.getWindow());
        loadingDialog.show();
        ToolManager.hideNavigationBar(loadingDialog.getWindow());
        ToolManager.clearFocusNotAle(loadingDialog.getWindow());
    }

    public static Dialog createLoadDialog(Context context, String tv, boolean isCancelable) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return null;
        }
        if(!checkActivityIsOk(context)) {
            return null;
        }
        Dialog dialog = new Dialog(context, R.style.CommonDialog);
        loadingDialog = dialog;
        View view = LayoutInflater.from(context).inflate(R.layout.loading, null);
        ImageView gifLoading = view.findViewById(R.id.iv_gif_loading);
        Glide.with(gifLoading).asGif().load(R.drawable.loading_anim).fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.DATA).into(gifLoading);
        TextView tv_load_dialog = view.findViewById(R.id.tv_load_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(isCancelable);
        tv_load_dialog.setText(tv);
        if(TextUtils.isEmpty(tv)) {
            tv_load_dialog.setVisibility(View.GONE);
        }
        dialog.setContentView(view);
        dialog.setOnCancelListener(it -> loadingDialog = null);
        ToolManager.focusNotAle(dialog.getWindow());
        dialog.show();
        ToolManager.hideNavigationBar(dialog.getWindow());
        ToolManager.clearFocusNotAle(dialog.getWindow());
        return dialog;
    }


    public static void cancelLoadingDialog(){
        try {
            if(loadingDialog != null){
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * <p>判断动画是否在执行中</p>
     */
    public static boolean isLoadShowing() {
        if (loadingDialog == null) {
            return false;
        } else {
            return loadingDialog.isShowing();
        }
    }

    private static boolean checkActivityIsOk(Context activity)
    {
        if (activity instanceof Activity) {
            return !(((Activity) activity).isDestroyed() || ((Activity) activity).isFinishing());
        }
        return true;
    }


}
