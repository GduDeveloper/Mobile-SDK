package com.gdu.demo.widgetlist.signal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.gdu.demo.R;


/**
 * 下沉GduApp中SignalQuality自定义view，后续陆续替换
 */

public class SignalQuality extends View {
    private Paint paint;
    private float[] points;
    private int lines;
    private int lastLines;

    /**
     * 信号格增加描边
     */
    private final Paint mStrokePaint = new Paint();
    private final int mStrokeColor1 = Color.parseColor("#FF000000");
    private final int mStrokeColor2 = Color.parseColor("#FF3C3C3C");

    public SignalQuality(Context context) {
        super(context);
        init();
    }

    public SignalQuality(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalQuality(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        points = new float[24];

        // 初始化描边画笔
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(0.5f);
        mStrokePaint.setColor(mStrokeColor2);
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initLine();

        for (int i = 0; i <24; i++) {
            if (i % 4 == 0) {
                if (i/4<lines){
                    paint.setColor(getResources().getColor(R.color.white));
                    mStrokePaint.setColor(mStrokeColor1);
                }else {
                    paint.setColor(getResources().getColor(R.color.color_33_WHITE));
                    mStrokePaint.setColor(mStrokeColor2);
                }
                canvas.drawLine(points[i] , points[i + 1] , points[i + 2] , points[i + 3] , paint);
                // 描边框
                canvas.drawRect(points[i] - paint.getStrokeWidth() * 0.6f, points[i + 1], points[i + 2] + paint.getStrokeWidth() * 0.6f, points[i + 3], mStrokePaint);
            }
        }
    }

    private void initLine() {

        int width = getWidth();
        int height = getHeight();
        int oneWidth = width / 6;
        int oneHeight = height / 6;
        int paintWidth = oneWidth / 2;
        paint.setStrokeWidth(paintWidth);
        mStrokePaint.setStrokeWidth(Math.min(0.5f, paintWidth/8f));

        for (int i = 0; i < 24; i++) {
            if (i % 4 == 0) {
                points[i] = i / 4 * oneWidth + paintWidth;
                points[i + 1] = height - oneHeight * (i / 4 + 1);
                points[i + 2] = i / 4 * oneWidth + paintWidth;
                points[i + 3] = height;
            }
        }
    }

    /**
     * 设置信号格数
     * @param line
     */
    public void setLines(int line){
        lines = line;
        if (lastLines != lines){
            invalidate();
            lastLines=lines;
        }
    }

    public void setNetSignalQuality(int quality) {

        if (quality == 0) {
            lines = 0;
        }else if (quality >= -70) {
            lines = 6;
        } else if ( quality >= -90) {
            lines = 5;
        } else if (quality >= -95) {
            lines = 4;
        } else if (quality >= -100) {
            lines = 3;
        } else if (quality >= -105) {
            lines = 2;
        } else if (quality >= -115) {
            lines = 1;
        } else {
            lines = 0;
        }

        if (lastLines != lines) {
            invalidate();
            lastLines = lines;
        }
    }

    /**
     * 20240730遥控器信号格需要进行下调整，改为使用MCS的值进行判定，总共是6格分别对应如下，麻烦出个版本我们验下，不用缓存
     * 20240819确定使用MCS显示图传信号强度与判断图传信号弱（MCS<2）并提测，不分飞机类型, 4g/5g网络信号没有MCS不改动
     * 断连  0格  （传-1置零）
     * MCS0 1格
     * MCS1 2格
     * MCS2 4格
     * MCS3 5格
     * MCS4 6格
     * @param mcs 编码速率 0-4（无三格信号）
     */
    public void setMCSQuality(int mcs) {
        int line;
        if (mcs == 0) {
            line = 1;
        } else if (mcs == 1) {
            line = 2;
        } else if (mcs == 2) {
            line = 4;
        } else if (mcs == 3) {
            line = 5;
        } else if (mcs == 4) {
            line = 6;
        } else {
            line = 0;
        }
        setLines(line);
    }

}
