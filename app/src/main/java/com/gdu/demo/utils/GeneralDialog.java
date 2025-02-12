package com.gdu.demo.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdu.demo.R;
import com.gdu.util.logs.AppLog;


public abstract class GeneralDialog extends Dialog implements View.OnClickListener {

    private TextView mGeneral_Title;//标题
    private TextView mGeneral_center_content;// content （单行/多行）
    private View mViewLine1;
    private ViewGroup mllBtnLayout;
    private TextView mNegative;//取消
    private TextView mPositive;//确定
    private View mView_SplitLine;
    private LinearLayout mGeneral_layout;
    private boolean isAutoDismiss = true;
    private int mCountNum = -1;
    private CountDownTime mTime;
    private String mPositiveText;

    public GeneralDialog(Context context, int theme) {
        super(context, theme);
        isAutoDismiss = true;
        initView();
    }

    public GeneralDialog(Context context, int theme, boolean isAutoDismiss) {
        super(context, theme);
        this.isAutoDismiss = isAutoDismiss;
        initView();
    }

    private void initView() {
        setContentView(R.layout.general_title_dialog);
        mGeneral_layout = findViewById(R.id.general_dialog);
        mGeneral_Title = findViewById(R.id.general_title);
        mView_SplitLine = findViewById(R.id.view_split_line);
        mGeneral_center_content = findViewById(R.id.general_center_content);
        mViewLine1 = findViewById(R.id.viewLine1);
        mllBtnLayout = findViewById(R.id.llBtnLayout);
        mNegative = findViewById(R.id.general_negative);
        mPositive = findViewById(R.id.general_positive);
        mNegative.setOnClickListener(this);
        mPositive.setOnClickListener(this);

        // 设置内容超出滚动
        mGeneral_center_content.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void setPositiveButtonText(int id) {
        mPositive.setText(id);
    }
    public void setPositiveButtonText(String text) {
        mPositive.setText(text);
    }


    public void setNegativeEnable(boolean isEnable) {
        mNegative.setEnabled(isEnable);
    }

    public void setPositiveButtonText(String text, int countNum) {
        mCountNum = countNum;
        mPositiveText = text;
        mPositive.setText(text+"("+countNum+")");
    }

    public void setNegativeButtonText(int id) {
        mNegative.setText(id);
    }

    public void setNegativeButtonText(String text) {
        mNegative.setText(text);
    }

    public void setTitleText(String text) {
        mGeneral_Title.setText(text);
    }

    public void setNoTitle() {mGeneral_Title.setVisibility(View.GONE);}

    public void setOnlycontent() {
        mGeneral_Title.setVisibility(View.GONE);
    }

    public void setTitleText(int id) {
        mGeneral_Title.setText(id);
    }

    public void setContentText(String text) {
        mGeneral_center_content.setText(text);
    }

    public void setContentText(int id) {
        mGeneral_center_content.setText(id);
    }

    class CountDownTime extends CountDownTimer {

        //构造函数  第一个参数代表总的计时时长  第二个参数代表计时间隔  单位都是毫秒
        public CountDownTime(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) { //每计时一次回调一次该方法
            mPositive.setText( mPositiveText+"("+l/1000+")");
        }

        @Override
        public void onFinish() { //计时结束回调该方法
            mPositive.setText( mPositiveText+"(0)");
            if(GeneralDialog.this != null && isShowing()) {
                positiveOnClick();
                dismiss();
            }
        }
    }

    @Override
    public void show() {
        super.show();
        if(mCountNum > 0){
            mTime = new CountDownTime(mCountNum * 1000L, 1000);
            mTime.start();
        }
    }

    public void setGeneralDialogWidth(int size) {
        ViewGroup.LayoutParams layoutParams = mGeneral_layout.getLayoutParams();
        layoutParams.width = size;
        mGeneral_layout.setLayoutParams(layoutParams);
    }

    public abstract void positiveOnClick();

    public abstract void negativeOnClick();

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.general_negative) {
            negativeOnClick();
            if (isAutoDismiss) {
                dismiss();
            }
        } else if (view.getId() == R.id.general_positive) {
            positiveOnClick();
            if (isAutoDismiss) {
                dismiss();
            }
        } else {
            dismiss();
        }
    }


    /**
     * <p>设置Content的 位置 【居左/居中】</p>
     *
     * @param gravity
     */
    public void setContentGravity(int gravity) {

        mGeneral_center_content.setGravity(gravity);
    }

    public void setOnlyButton() {
        mView_SplitLine.setVisibility(View.GONE);
        mNegative.setVisibility(View.GONE);
    }

    public void setNoButton() {
        mViewLine1.setVisibility(View.INVISIBLE);
        mllBtnLayout.setVisibility(View.GONE);
    }



    /**
     * new GeneralDialog(requireContext(), R.style.NormalDialog)  NormalDialog等样式中也加入了<item name="android:backgroundDimEnabled">true</item>蒙版设置
     * 目前概率性出现<a href="https://blog.csdn.net/qq_29418961/article/details/104583702">蒙版阴影</a>不消失问题(进入图库查看图片或视频在返回预览画面),而我自行测试未发现
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Window window = getWindow();
        if (hasFocus && window != null) {
            View decorView = window.getDecorView();
            if (decorView.getHeight() == 0 || decorView.getWidth() == 0) {
                decorView.requestLayout();
                AppLog.e("backgroundDimEnabled", "布局异常，重新布局");
            }
        }
    }
}
