package com.gdu.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.gdu.demo.R;
import com.gdu.demo.utils.ScreenUtils;


/**
 * 视频画面九宫格view(自定义)
 */
public class CustomJiuGongGeView extends View {

    private Context mContext;
    private Paint mPaint;

    private float viewWidth, viewHeight;
    private float[] lineArr;

    public CustomJiuGongGeView(Context context) {
        this(context, null);
    }

    public CustomJiuGongGeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CustomJiuGongGeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        initPant();
    }

    private void initPant() {
        mPaint = new Paint();
        mPaint.setColor(mContext.getColor(R.color.color_CCFFFFFF));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measureSpec(widthMeasureSpec, 100);
        int measureHeight = measureSpec(heightMeasureSpec, 100);

        viewWidth = measureWidth;
        viewHeight = measureHeight;

        float subWith = viewWidth / 3;
        float subHeight = viewHeight / 3;

        lineArr = new float[]{
                0, 0,
                viewWidth, viewHeight,
                viewWidth, 0,
                0, viewHeight,
                0, subHeight * 2,
                viewWidth, subHeight * 2,
                viewWidth, subHeight,
                0, subHeight,
                subWith, 0,
                subWith, viewHeight,
                subWith * 2, viewHeight,
                subWith * 2, 0};

        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measureSpec(int measureSpec, float defValue) {
        int defSize = ScreenUtils.dp2px(mContext, defValue);
        int result = defSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            // 指定数值或者match_parent
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // 为warp_content时
            result = Math.min(result, specSize);
        } else {
            // view想多大就多大
            result = (int) defSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lineArr != null && mPaint != null) {
            canvas.drawLines(lineArr, mPaint);
        }
    }
}
