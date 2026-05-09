package com.gdu.demo.widget.psdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.R;


/**
 * @Date: 2022/10/22
 */
public class CustomProgressLayout extends LinearLayout {


    private final Context context;

    private VerticalSeekBar seekBar;
    private TextView tvValue;
    private TextView tvAdd;
    private TextView tvDev;

    private SeekBarChangeListener changeListener;

    private int startProgress;
    private boolean isTouchSeekBar;


    public CustomProgressLayout(Context context) {
        this(context, null);
    }

    public CustomProgressLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomProgressLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;

        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_progress_bar, this);
        tvValue = findViewById(R.id.tv_value);
        seekBar = findViewById(R.id.vertical_seekbar);
        tvAdd = findViewById(R.id.tv_add);
        tvDev = findViewById(R.id.tv_dev);
        seekBar.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {
                isTouchSeekBar = true;
                startProgress = progress;
            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {

                tvValue.setText(progress + "");
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {
                isTouchSeekBar = false;
                if (changeListener != null) {
                    changeListener.onChange(startProgress, progress);
                }
            }
        });

        tvAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                int progress = seekBar.getProgress();
                if (progress >= 100) {
                    return;
                }
                if (changeListener != null) {
                    changeListener.onChange(progress, ++progress);
                }

            }
        });

        tvDev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                int progress = seekBar.getProgress();
                if (progress <= 0) {
                    return;
                }
                if (changeListener != null) {
                    changeListener.onChange(progress, --progress);
                }

            }
        });
    }

    public void setProgress(int progress) {

        if (isTouchSeekBar) {
            return;
        }
        seekBar.setProgress(progress);
        tvValue.setText(progress + "");
    }

    public void setChangeListener(SeekBarChangeListener listener) {

        this.changeListener = listener;
    }


    public interface SeekBarChangeListener {
        void onChange(int startProgress, int progress);
    }
}

