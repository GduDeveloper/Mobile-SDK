package com.gdu.demo.widget.zoomView;


import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.DIRECTION_LEFT;
import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.DIRECTION_RIGHT;
import static com.gdu.demo.widget.zoomView.CustomVerticalRangeSeekBar.TEXT_DIRECTION_VERTICAL;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.gdu.demo.R;


public class CustomVerticalSeekBar extends SeekBar {
    private final String TAG = "CustomVerticalSeekBar";
    private int indicatorTextOrientation;
    private final IVerticalGetOrientation mVerticalGetOrientation;

    public CustomVerticalSeekBar(RangeSeekBar rangeSeekBar, AttributeSet attrs, IVerticalGetOrientation getOrientation, boolean isLeft) {
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
    public void setThumbInactivatedDrawableId(@DrawableRes int thumbInactivatedDrawableId, int width, int height) {
        Log.i(TAG, "setThumbInactivatedDrawableId() width = " + width + "; height = " + height);
        if (thumbInactivatedDrawableId != 0 && getResources() != null) {
            this.mThumbInactivatedDrawableId = thumbInactivatedDrawableId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThumbInactivatedBitmap = Utils.drawableToBitmap(width, height, getResources().getDrawable(thumbInactivatedDrawableId, null));
            } else {
                mThumbInactivatedBitmap = Utils.drawableToBitmap(width, height, getResources().getDrawable(thumbInactivatedDrawableId));
            }
        }
    }

    @Override
    protected void onDrawIndicator(Canvas canvas, Paint paint, String text2Draw) {
        Log.i(TAG, "onDrawIndicator() text2Draw = " + text2Draw);
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

    @Override
    protected void onDrawThumb(Canvas canvas) {
        Log.i(TAG, "onDrawThumb() mUserText2Thumb = " + mUserText2Thumb);
        if (mUserText2Thumb == null || "".equals(mUserText2Thumb)) {
            mUserText2Thumb = "1.0X";
        }
        final float topVar = mRangeSeekBar.getProgressTop() + (mRangeSeekBar.getProgressHeight() - mScaleThumbHeight) / 2f;
        final float thumbHeightVar = getThumbHeight();
        final float thumbWidthVar = getThumbWidth();
        Log.i(TAG, "onDrawThumb() topVar = " + topVar + "; thumbHeightVar = " + thumbHeightVar
                + "; thumbWidthVar = " + thumbWidthVar);
        mPaint.setStyle(Paint.Style.STROKE);
        final int strokeWidth = 2;
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_00B4FF));
        final RectF mStrokeRect = new RectF();
        mStrokeRect.left = 0;
        mStrokeRect.top = topVar;
        mStrokeRect.right = thumbWidthVar;
        mStrokeRect.bottom = thumbHeightVar;
        canvas.drawRoundRect(mStrokeRect, 5, 5, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_B200B4FF));
        final RectF mRect = new RectF();
        mRect.left = 0;
        mRect.top = topVar;
        mRect.right = thumbWidthVar;
        mRect.bottom = thumbHeightVar;
        canvas.drawRoundRect(mRect, 5, 5, mPaint);

        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mRangeSeekBar.getTickMarkTextSize());
        final Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float bottomLineY = mRect.centerY() - (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top;
        int degrees = 0;
        float rotateX = (mRect.left + mRect.width() / 2f);
        float rotateY = (mRect.bottom - mRect.height() / 2f);
        if (mVerticalGetOrientation.getOrientation() == DIRECTION_LEFT) {
            degrees = 90;
        } else if (mVerticalGetOrientation.getOrientation() == DIRECTION_RIGHT) {
            degrees = -90;
        }
        if (degrees != 0) {
            canvas.rotate(degrees, rotateX, rotateY);
        }
        mPaint.setFakeBoldText(true);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(mUserText2Thumb, rotateX, bottomLineY, mPaint);

        mPaint.setFakeBoldText(false);
        mPaint.setTypeface(Typeface.DEFAULT);
        if (degrees != 0) {
            canvas.rotate(-degrees, rotateX, rotateY);
        }
    }

    protected void drawVerticalIndicator(Canvas canvas, Paint paint, String text2Draw) {
        Log.i(TAG, "drawVerticalIndicator() text2Draw = " + text2Draw);
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
