package com.gdu.demo.widget.zoomView;


import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.DIRECTION_LEFT;
import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.DIRECTION_RIGHT;
import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.TEXT_DIRECTION_VERTICAL;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.gdu.demo.R;


/**
 * 作    者：JayGoo
 * 创建日期：2019-06-05
 * 描    述:
 */
public class VerticalSeekBar extends SeekBar {
    private final String TAG = "VerticalSeekBar";
    private int indicatorTextOrientation;
    IVerticalGetOrientation mVerticalGetOrientation;

    public VerticalSeekBar(RangeSeekBar rangeSeekBar, AttributeSet attrs, IVerticalGetOrientation getOrientation, boolean isLeft) {
        super(rangeSeekBar, attrs, isLeft);
        mVerticalGetOrientation = getOrientation;
    }

    @Override
    protected void initAttrs(AttributeSet attrs) {
        super.initAttrs(attrs);
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar);
            indicatorTextOrientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_indicator_text_orientation, TEXT_DIRECTION_VERTICAL);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDrawIndicator(Canvas canvas, Paint paint, String text2Draw) {
        if (text2Draw == null) {
            return;
        }
        //draw indicator
        if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
            drawVerticalIndicator(canvas, paint, text2Draw);
        } else {
            super.onDrawIndicator(canvas, paint, text2Draw);
        }
    }

    protected void drawVerticalIndicator(Canvas canvas, Paint paint, String text2Draw) {
        //measure indicator text
        paint.setTextSize(getIndicatorTextSize());
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getIndicatorBackgroundColor());
        paint.getTextBounds(text2Draw, 0, text2Draw.length(), mIndicatorTextRect);

        int realIndicatorWidth = mIndicatorTextRect.height() + getIndicatorPaddingLeft() + getIndicatorPaddingRight();
        if (getIndicatorWidth() > realIndicatorWidth) {
            realIndicatorWidth = getIndicatorWidth();
        }

        int realIndicatorHeight = mIndicatorTextRect.width() + getIndicatorPaddingTop() + getIndicatorPaddingBottom();
        if (getIndicatorHeight() > realIndicatorHeight) {
            realIndicatorHeight = getIndicatorHeight();
        }

        mIndicatorRect.left = mScaleThumbWidth / 2 - realIndicatorWidth / 2;
        mIndicatorRect.top = mBottom - realIndicatorHeight - mScaleThumbHeight - getIndicatorMargin();
        mIndicatorRect.right = mIndicatorRect.left + realIndicatorWidth;
        mIndicatorRect.bottom = mIndicatorRect.top + realIndicatorHeight;

        //draw default indicator arrow
        if (mIndicatorBitmap == null) {
            //arrow three point
            //  b   c
            //    a
            int ax = mScaleThumbWidth / 2;
            int ay = mIndicatorRect.bottom;
            int bx = ax - getIndicatorArrowSize();
            int by = ay - getIndicatorArrowSize();
            int cx = ax + getIndicatorArrowSize();
            mIndicatorArrowPath.reset();
            mIndicatorArrowPath.moveTo(ax, ay);
            mIndicatorArrowPath.lineTo(bx, by);
            mIndicatorArrowPath.lineTo(cx, by);
            mIndicatorArrowPath.close();
            canvas.drawPath(mIndicatorArrowPath, paint);
            mIndicatorRect.bottom -= getIndicatorArrowSize();
            mIndicatorRect.top -= getIndicatorArrowSize();
        }

        int defaultPaddingOffset = Utils.dp2px(getContext(), 1);
        int leftOffset = mIndicatorRect.width() / 2 - (int) (mRangeSeekBar.getProgressWidth() * mCurrPercent) - mRangeSeekBar.getProgressLeft() + defaultPaddingOffset;
        int rightOffset = mIndicatorRect.width() / 2 - (int) (mRangeSeekBar.getProgressWidth() * (1 - mCurrPercent)) - mRangeSeekBar.getProgressPaddingRight() + defaultPaddingOffset;
        if (leftOffset > 0) {
            mIndicatorRect.left += leftOffset;
            mIndicatorRect.right += leftOffset;
        } else if (rightOffset > 0) {
            mIndicatorRect.left -= rightOffset;
            mIndicatorRect.right -= rightOffset;
        }

        //draw indicator background
        if (mIndicatorBitmap != null) {
            Utils.drawBitmap(canvas, paint, mIndicatorBitmap, mIndicatorRect);
        } else if (getIndicatorRadius() > 0f) {
            canvas.drawRoundRect(new RectF(mIndicatorRect), getIndicatorRadius(), getIndicatorRadius(), paint);
        } else {
            canvas.drawRect(mIndicatorRect, paint);
        }

        //draw indicator content text
        int tx = mIndicatorRect.left + (mIndicatorRect.width() - mIndicatorTextRect.width()) / 2 + getIndicatorPaddingLeft() - getIndicatorPaddingRight();
        int ty = mIndicatorRect.bottom - (mIndicatorRect.height() - mIndicatorTextRect.height()) / 2 + getIndicatorPaddingTop() - getIndicatorPaddingBottom();

        //draw indicator text
        paint.setColor(getIndicatorTextColor());

        int degrees = 0;
        float rotateX = (tx + mIndicatorTextRect.width() / 2f);
        float rotateY = (ty - mIndicatorTextRect.height() / 2f);

        if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
            if (mVerticalGetOrientation.getOrientation() == DIRECTION_LEFT) {
                degrees = 90;
            } else if (mVerticalGetOrientation.getOrientation() == DIRECTION_RIGHT) {
                degrees = -90;
            }
        }
        if (degrees != 0) {
            canvas.rotate(degrees, rotateX, rotateY);
        }
        canvas.drawText(text2Draw, tx, ty, paint);
        if (degrees != 0) {
            canvas.rotate(-degrees, rotateX, rotateY);
        }
    }

    public int getIndicatorTextOrientation() {
        return indicatorTextOrientation;
    }

    public void setIndicatorTextOrientation(@VerticalRangeSeekBar.TextDirectionDef int orientation) {
        this.indicatorTextOrientation = orientation;
    }
}
