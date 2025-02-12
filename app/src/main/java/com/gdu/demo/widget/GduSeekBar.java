package com.gdu.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gdu.demo.R;
import com.gdu.util.StringUtils;

/**
 * 自定义seekbar组件
 */
public class GduSeekBar extends RelativeLayout implements View.OnClickListener {

    private final Context mContext;
    private TextView mNameTextView;
    private TextView mMinSbValueTv;
    private TextView mMaxSbValueTv;
    private EditText mValueEditText;
    private SeekBar  mValueSeekBar;
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;

    private OnEditChangeListener listener;

    /** 是否是通过ProgressBar设置进度值 */
    private boolean isPBSet;

    private int mNameResId;
    /** 当前可设最大值 */
    private int mSeekBarMax;
    /** 当前可设最小值 */
    private int mSeekBarMin;
    private boolean isFirstLoadData = true;

    public GduSeekBar(Context context) {
        this(context, null);
    }

    public GduSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GduSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GduSeekBar);
        if (ta.hasValue(R.styleable.GduSeekBar_seekMax)) {
            mSeekBarMax = ta.getInt(R.styleable.GduSeekBar_seekMax, mSeekBarMax);
        }
        if (ta.hasValue(R.styleable.GduSeekBar_seekMin)) {
            mSeekBarMin = ta.getInt(R.styleable.GduSeekBar_seekMin, mSeekBarMin);
        }
        if (ta.hasValue(R.styleable.GduSeekBar_name)) {
            mNameResId = ta.getResourceId(R.styleable.GduSeekBar_name, 0);
        }
        initView();
        initData();
        initListener();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_seekbar, this);
        mNameTextView = findViewById(R.id.name_textview);
        mMinSbValueTv = findViewById(R.id.tv_sbMinValue);
        mMaxSbValueTv = findViewById(R.id.tv_sbMaxValue);
        mValueEditText = findViewById(R.id.value_edit);
        mValueSeekBar = findViewById(R.id.sb_value);
    }

    private void initData() {
        mNameTextView.setText(mContext.getResources().getString(mNameResId));
        mMinSbValueTv.setText(String.valueOf(mSeekBarMin));
        mMaxSbValueTv.setText(String.valueOf(mSeekBarMax));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mValueSeekBar.setMin(mSeekBarMin);
            mValueSeekBar.setMax(mSeekBarMax);
            mValueEditText.setText(String.valueOf(mValueSeekBar.getProgress()));
        } else {
            mValueSeekBar.setMax(mSeekBarMax - mSeekBarMin);
            mValueEditText.setText(String.valueOf(mValueSeekBar.getProgress() + mSeekBarMin));
        }
    }

    private void initListener() {
        mValueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isFirstLoadData) {
                    isFirstLoadData = false;
                    return;
                }
                if (mSeekBarMin > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    progress = progress + mSeekBarMin;
                }
                if (fromUser) {
                    mValueEditText.setText(String.valueOf(progress));
                }
                mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPBSet = true;
                mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });
        mValueEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String value = textView.getText().toString();

                    if (StringUtils.isEmptyString(value)) {
                        Toast.makeText(mContext, R.string.input_error, Toast.LENGTH_SHORT).show();
                        errorEdit();
                        return true;
                    }

                    int progressValue = Integer.parseInt(value);

                    if (mSeekBarMin > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        progressValue = progressValue - mSeekBarMin;
                    }

                    if (progressValue < mSeekBarMin || progressValue > mSeekBarMax) {
                        Toast.makeText(mContext, R.string.input_error, Toast.LENGTH_SHORT).show();
                        errorEdit();
                        return true;
                    }
                    mValueSeekBar.setProgress(progressValue);
                    if (listener != null) {
                        listener.onChange(progressValue);
                    }
                }
                return false;
            }
        });

        mValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String editValue = editable.toString();
                if (!StringUtils.isEmptyString(editValue)) {
                    int progressValue = Integer.parseInt(editValue);
                    if (progressValue > mSeekBarMax) {
                       editable.delete(editValue.length() - 1, editValue.length());
                    }
                }
            }
        });
    }

    private void errorEdit() {
        mValueEditText.setText(String.valueOf(mValueSeekBar.getProgress()));
        String inputStr = mValueEditText.getText().toString();
        mValueEditText.setSelection(inputStr.length());
    }


    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener){
        mOnSeekBarChangeListener = onSeekBarChangeListener;
    }


    /**
     * 设置名称
     * @param name
     */
    public void setName(String name){
        mNameTextView.setText(name);
    }

    /**
     * 设置进度条
     * @param progress
     */
    public void setProgress(int progress) {
        int curProgress = progress;
        if (mSeekBarMin > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            curProgress = progress - mSeekBarMin;
        }
        isPBSet = true;
        mValueSeekBar.setProgress(curProgress);
        mValueEditText.setText(String.valueOf(progress));
    }

    /**
     * 设置后面文字进度条值
     * @param progress
     */
    public void setTextProgress(int progress){
        isPBSet = true;
        mValueEditText.setText(String.valueOf(progress));
    }

    public void setMaxValue(int maxValue){
        mSeekBarMax = maxValue;
    }

    public int getSeekBarMax() {
        return mSeekBarMax;
    }

    public void setMinValue(int minValue){
        mSeekBarMin = minValue;
    }

    public int getSeekBarMin() {
        return mSeekBarMin;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mValueSeekBar != null) {
            mValueSeekBar.setEnabled(enabled);
        }
        if (mValueEditText != null) {
            mValueEditText.setEnabled(enabled);
        }
    }


    public void setOnEditChangeListener(OnEditChangeListener listener) {
        this.listener = listener;
    }

    public interface  OnEditChangeListener{
        void onChange(int progress);
    }
}
