package com.gdu.demo.widget.zoomView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;

import com.gdu.demo.R;

import java.text.DecimalFormat;


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/8
 * 描    述:
 * ================================================
 */

public class SeekBar {
    private final String TAG = "SeekBar";
    //the indicator show mode
    public static final int INDICATOR_SHOW_WHEN_TOUCH = 0;
    public static final int INDICATOR_ALWAYS_HIDE = 1;
    public static final int INDICATOR_ALWAYS_SHOW_AFTER_TOUCH = 2;
    public static final int INDICATOR_ALWAYS_SHOW = 3;

    @IntDef({INDICATOR_SHOW_WHEN_TOUCH, INDICATOR_ALWAYS_HIDE, INDICATOR_ALWAYS_SHOW_AFTER_TOUCH, INDICATOR_ALWAYS_SHOW})
    public @interface IndicatorModeDef {
    }

    public static final int WRAP_CONTENT = -1;
    public static final int MATCH_PARENT = -2;

    private int mIndicatorShowMode;

    //进度提示背景的高度，宽度如果是0的话会自适应调整
    //Progress prompted the background height, width,
    private int mIndicatorHeight;
    private int mIndicatorWidth;
    //进度提示背景与按钮之间的距离
    //The progress indicates the distance between the background and the button
    private int mIndicatorMargin;
    private int mIndicatorDrawableId;
    private int mIndicatorArrowSize;
    private int mIndicatorTextSize;
    private int mIndicatorTextColor;
    private float mIndicatorRadius;
    private int mIndicatorBackgroundColor;
    private int indicatorPaddingLeft, indicatorPaddingRight, indicatorPaddingTop, indicatorPaddingBottom;
    private int mThumbDrawableId;
    protected int mThumbInactivatedDrawableId;
    private int mThumbWidth;
    private int mThumbHeight;

    //when you touch or move, the thumb will scale, default not scale
    float mThumbScaleRatio;

    //****************** the above is attr value  ******************//

    int mLeft, mRight, mTop, mBottom;
    float mCurrPercent;
    float material = 0;
    private boolean isShowIndicator;
    boolean isLeft;
    Bitmap mThumbBitmap;
    Bitmap mThumbInactivatedBitmap;
    Bitmap mIndicatorBitmap;
    ValueAnimator mAnimator;
    String mUserText2Draw;
    String mUserText2Thumb = "1";
    boolean isActivate = false;
    boolean isVisible = true;
    RangeSeekBar mRangeSeekBar;
    String indicatorTextStringFormat;
    Path mIndicatorArrowPath = new Path();
    Rect mIndicatorTextRect = new Rect();
    Rect mIndicatorRect = new Rect();
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    DecimalFormat mIndicatorTextDecimalFormat;
    int mScaleThumbWidth;
    int mScaleThumbHeight;

    public SeekBar(RangeSeekBar rangeSeekBar, AttributeSet attrs, boolean isLeft) {
        this.mRangeSeekBar = rangeSeekBar;
        this.isLeft = isLeft;
        initAttrs(attrs);
        initBitmap();
        initVariables();
    }

    protected void initAttrs(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        if (t == null) {
            return;
        }
        mIndicatorMargin = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_margin, 0);
        mIndicatorDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_indicator_drawable, 0);
        mIndicatorShowMode = t.getInt(R.styleable.RangeSeekBar_rsb_indicator_show_mode, INDICATOR_ALWAYS_HIDE);
        mIndicatorHeight = t.getLayoutDimension(R.styleable.RangeSeekBar_rsb_indicator_height, WRAP_CONTENT);
        mIndicatorWidth = t.getLayoutDimension(R.styleable.RangeSeekBar_rsb_indicator_width, WRAP_CONTENT);
        mIndicatorTextSize = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_text_size, Utils.dp2px(getContext(), 14));
        mIndicatorTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_indicator_text_color, Color.WHITE);
        mIndicatorBackgroundColor = t.getColor(R.styleable.RangeSeekBar_rsb_indicator_background_color, ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        indicatorPaddingLeft = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_left, 0);
        indicatorPaddingRight = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_right, 0);
        indicatorPaddingTop = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_top, 0);
        indicatorPaddingBottom = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_bottom, 0);
        mIndicatorArrowSize = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_arrow_size, 0);
        mThumbDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_thumb_drawable, R.drawable.rsb_default_thumb);
        mThumbInactivatedDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_thumb_inactivated_drawable, 0);
        mThumbWidth = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_thumb_width, Utils.dp2px(getContext(), 26));
        mThumbHeight = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_thumb_height, Utils.dp2px(getContext(), 26));
        mThumbScaleRatio = t.getFloat(R.styleable.RangeSeekBar_rsb_thumb_scale_ratio, 1f);
        mIndicatorRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_radius, 0f);
        t.recycle();
    }

    protected void initVariables() {
        mScaleThumbWidth = mThumbWidth;
        mScaleThumbHeight = mThumbHeight;
        if (mIndicatorHeight == WRAP_CONTENT) {
            mIndicatorHeight = Utils.measureText("8", mIndicatorTextSize).height() + indicatorPaddingTop + indicatorPaddingBottom;
        }
        if (mIndicatorArrowSize <= 0) {
            mIndicatorArrowSize = mThumbWidth / 4;
        }
    }

    public Context getContext() {
        return mRangeSeekBar.getContext();
    }

    public Resources getResources() {
        if (getContext() != null) {
            return getContext().getResources();
        }
        return null;
    }

    /**
     * 初始化进度提示的背景
     */
    private void initBitmap() {
        Log.i(TAG, "initBitmap()");
        setIndicatorDrawableId(mIndicatorDrawableId);
        setThumbDrawableId(mThumbDrawableId, mThumbWidth, mThumbHeight);
        setThumbInactivatedDrawableId(mThumbInactivatedDrawableId, mThumbWidth, mThumbHeight);
    }

    /**
     * 计算每个按钮的位置和尺寸
     * Calculates the position and size of each button
     *
     * @param x position x
     * @param y position y
     */
    protected void onSizeChanged(int x, int y) {
        Log.i(TAG, "onSizeChanged()");
        initVariables();
        initBitmap();
        mLeft = (int) (x - getThumbScaleWidth() / 2);
        mRight = (int) (x + getThumbScaleWidth() / 2);
        mTop = y - getThumbHeight() / 2;
        mBottom = y + getThumbHeight() / 2;
    }


    public void scaleThumb() {
        Log.i(TAG, "scaleThumb()");
        mScaleThumbWidth = (int) getThumbScaleWidth();
        mScaleThumbHeight = (int) getThumbScaleHeight();
        int y = mRangeSeekBar.getProgressBottom();
        mTop = y - mScaleThumbHeight / 2;
        mBottom = y + mScaleThumbHeight / 2;
        setThumbDrawableId(mThumbDrawableId, mScaleThumbWidth, mScaleThumbHeight);
    }

    public void resetThumb() {
        Log.i(TAG, "resetThumb()");
        mScaleThumbWidth = getThumbWidth();
        mScaleThumbHeight = getThumbHeight();
        int y = mRangeSeekBar.getProgressBottom();
        mTop = y - mScaleThumbHeight / 2;
        mBottom = y + mScaleThumbHeight / 2;
        setThumbDrawableId(mThumbDrawableId, mScaleThumbWidth, mScaleThumbHeight);
    }

    public float getRawHeight() {
        return getIndicatorHeight() + getIndicatorArrowSize() + getIndicatorMargin() + getThumbScaleHeight();
    }

    /**
     * 绘制按钮和提示背景和文字
     * Draw buttons and tips for background and text
     *
     * @param canvas Canvas
     */
    protected void draw(Canvas canvas) {
        Log.i(TAG, "draw()");
        if (!isVisible) {
            return;
        }
        final int offset = (int) (mRangeSeekBar.getProgressWidth() * mCurrPercent);
        Log.i(TAG, "draw() translateX offset = " + offset + "; mCurrPercent = " + mCurrPercent);
        canvas.save();
        canvas.translate(offset, 0);
        // translate canvas, then don't care left
        Log.i(TAG, "draw() translateX mLeft = " + mLeft);
        canvas.translate(mLeft, 0);
        if (isShowIndicator) {
            onDrawIndicator(canvas, mPaint, formatCurrentIndicatorText(mUserText2Draw));
        }
        onDrawThumb(canvas);
        canvas.restore();
    }


    /**
     * 绘制按钮
     * 如果没有图片资源，则绘制默认按钮
     * <p>
     * draw the thumb button
     * If there is no image resource, draw the default button
     *
     * @param canvas canvas
     */
    protected void onDrawThumb(Canvas canvas) {
        if (mThumbInactivatedBitmap != null && !isActivate) {
            canvas.drawBitmap(mThumbInactivatedBitmap, 0, mRangeSeekBar.getProgressTop() + (mRangeSeekBar.getProgressHeight() - mScaleThumbHeight) / 2f, null);
        } else if (mThumbBitmap != null) {
            canvas.drawBitmap(mThumbBitmap, 0, mRangeSeekBar.getProgressTop() + (mRangeSeekBar.getProgressHeight() - mScaleThumbHeight) / 2f, null);
        }
    }

    /**
     * 格式化提示文字
     * format the indicator text
     *
     * @param text2Draw
     * @return
     */
    protected String formatCurrentIndicatorText(String text2Draw) {
        Log.i(TAG, "formatCurrentIndicatorText() text2Draw = " + text2Draw);
        SeekBarState[] states = mRangeSeekBar.getRangeSeekBarState();
        if (TextUtils.isEmpty(text2Draw)) {
            if (isLeft) {
                if (mIndicatorTextDecimalFormat != null) {
                    text2Draw = mIndicatorTextDecimalFormat.format(states[0].value);
                } else {
                    text2Draw = states[0].indicatorText;
                }
            } else {
                if (mIndicatorTextDecimalFormat != null) {
                    text2Draw = mIndicatorTextDecimalFormat.format(states[1].value);
                } else {
                    text2Draw = states[1].indicatorText;
                }
            }
        }
        if (indicatorTextStringFormat != null) {
            text2Draw = String.format(indicatorTextStringFormat, text2Draw);
        }
        Log.i(TAG, "formatCurrentIndicatorText() result text2Draw = " + text2Draw);
        return text2Draw;
    }

    /**
     * This method will draw the indicator background dynamically according to the text.
     * you can use to set padding
     *
     * @param canvas    Canvas
     * @param text2Draw Indicator text
     */
    protected void onDrawIndicator(Canvas canvas, Paint paint, String text2Draw) {
        Log.i(TAG, "onDrawIndicator() text2Draw = " + text2Draw);
        if (text2Draw == null) {
            return;
        }
        paint.setTextSize(mIndicatorTextSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mIndicatorBackgroundColor);
        paint.getTextBounds(text2Draw, 0, text2Draw.length(), mIndicatorTextRect);
        int realIndicatorWidth = mIndicatorTextRect.width() + indicatorPaddingLeft + indicatorPaddingRight;
        if (mIndicatorWidth > realIndicatorWidth) {
            realIndicatorWidth = mIndicatorWidth;
        }

        int realIndicatorHeight = mIndicatorTextRect.height() + indicatorPaddingTop + indicatorPaddingBottom;
        if (mIndicatorHeight > realIndicatorHeight) {
            realIndicatorHeight = mIndicatorHeight;
        }

        mIndicatorRect.left = (int) (mScaleThumbWidth / 2f - realIndicatorWidth / 2f);
        mIndicatorRect.top = mBottom - realIndicatorHeight - mScaleThumbHeight - mIndicatorMargin;
        mIndicatorRect.right = mIndicatorRect.left + realIndicatorWidth;
        mIndicatorRect.bottom = mIndicatorRect.top + realIndicatorHeight;
        //draw default indicator arrow
        if (mIndicatorBitmap == null) {
            //arrow three point
            //  b   c
            //    a
            int ax = mScaleThumbWidth / 2;
            int ay = mIndicatorRect.bottom;
            int bx = ax - mIndicatorArrowSize;
            int by = ay - mIndicatorArrowSize;
            int cx = ax + mIndicatorArrowSize;
            mIndicatorArrowPath.reset();
            mIndicatorArrowPath.moveTo(ax, ay);
            mIndicatorArrowPath.lineTo(bx, by);
            mIndicatorArrowPath.lineTo(cx, by);
            mIndicatorArrowPath.close();
            canvas.drawPath(mIndicatorArrowPath, paint);
            mIndicatorRect.bottom -= mIndicatorArrowSize;
            mIndicatorRect.top -= mIndicatorArrowSize;
        }

        //indicator background edge processing
        int defaultPaddingOffset = Utils.dp2px(getContext(), 1);
        int leftOffset =
                mIndicatorRect.width() / 2 - (int) (mRangeSeekBar.getProgressWidth() * mCurrPercent) - mRangeSeekBar.getProgressLeft() + defaultPaddingOffset;
        int rightOffset =
                mIndicatorRect.width() / 2 - (int) (mRangeSeekBar.getProgressWidth() * (1 - mCurrPercent)) - mRangeSeekBar.getProgressPaddingRight() + defaultPaddingOffset;

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
        } else if (mIndicatorRadius > 0f) {
            canvas.drawRoundRect(new RectF(mIndicatorRect), mIndicatorRadius, mIndicatorRadius, paint);
        } else {
            canvas.drawRect(mIndicatorRect, paint);
        }

        //draw indicator content text
        int tx, ty;
        if (indicatorPaddingLeft > 0) {
            tx = mIndicatorRect.left + indicatorPaddingLeft;
        } else if (indicatorPaddingRight > 0) {
            tx = mIndicatorRect.right - indicatorPaddingRight - mIndicatorTextRect.width();
        } else {
            tx = mIndicatorRect.left + (realIndicatorWidth - mIndicatorTextRect.width()) / 2;
        }

        if (indicatorPaddingTop > 0) {
            ty = mIndicatorRect.top + mIndicatorTextRect.height() + indicatorPaddingTop;
        } else if (indicatorPaddingBottom > 0) {
            ty = mIndicatorRect.bottom - mIndicatorTextRect.height() - indicatorPaddingBottom;
        } else {
            ty = mIndicatorRect.bottom - (realIndicatorHeight - mIndicatorTextRect.height()) / 2 + 1;
        }

        //draw indicator text
        paint.setColor(mIndicatorTextColor);
        canvas.drawText(text2Draw, tx, ty, paint);
    }

    /**
     * 拖动检测
     *
     * @return is collide
     */
    protected boolean collide(float x, float y) {
        Log.i(TAG, "collide() x = " + x + "; y = " + y);
        int offset = (int) (mRangeSeekBar.getProgressWidth() * mCurrPercent);
        return x > mLeft + offset && x < mRight + offset && y > mTop && y < mBottom;
    }

    protected void slide(float percent) {
        Log.i(TAG, "slide() percent = " + percent);
        if (percent < 0) {
            percent = 0;
        } else if (percent > 1) {
            percent = 1;
        }
        mCurrPercent = percent;
    }

    protected void setShowIndicatorEnable(boolean isEnable) {
        Log.i(TAG, "setShowIndicatorEnable() isEnable = " + isEnable);
        switch (mIndicatorShowMode) {
            case INDICATOR_SHOW_WHEN_TOUCH:
                isShowIndicator = isEnable;
                break;

            case INDICATOR_ALWAYS_SHOW:
            case INDICATOR_ALWAYS_SHOW_AFTER_TOUCH:
                isShowIndicator = true;
                break;

            case INDICATOR_ALWAYS_HIDE:
                isShowIndicator = false;
                break;

            default:
                break;
        }
    }

    public void materialRestore() {
        Log.i(TAG, "materialRestore()");
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(material, 0);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                material = (float) animation.getAnimatedValue();
                if (mRangeSeekBar != null) {
                    mRangeSeekBar.invalidate();
                }
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                material = 0;
                if (mRangeSeekBar != null) {
                    mRangeSeekBar.invalidate();
                }
            }
        });
        mAnimator.start();
    }

    public void setIndicatorText(String text) {
        mUserText2Draw = text;
    }

    public void setThumbText(String text) {
        mUserText2Thumb = text;
    }

    public String getUserText2Thumb() {
        return mUserText2Thumb;
    }

    public void setIndicatorTextDecimalFormat(DecimalFormat formatPattern) {
        mIndicatorTextDecimalFormat = formatPattern;
    }

    public DecimalFormat getIndicatorTextDecimalFormat() {
        return mIndicatorTextDecimalFormat;
    }

    public void setIndicatorTextStringFormat(String formatPattern) {
        indicatorTextStringFormat = formatPattern;
    }

    public int getIndicatorDrawableId() {
        return mIndicatorDrawableId;
    }

    public void setIndicatorDrawableId(@DrawableRes int indicatorDrawableId) {
        if (indicatorDrawableId != 0) {
            this.mIndicatorDrawableId = indicatorDrawableId;
            mIndicatorBitmap = BitmapFactory.decodeResource(getResources(), indicatorDrawableId);
        }
    }


    public int getIndicatorArrowSize() {
        return mIndicatorArrowSize;
    }

    public void setIndicatorArrowSize(int indicatorArrowSize) {
        this.mIndicatorArrowSize = indicatorArrowSize;
    }

    public int getIndicatorPaddingLeft() {
        return indicatorPaddingLeft;
    }

    public void setIndicatorPaddingLeft(int indicatorPaddingLeft) {
        this.indicatorPaddingLeft = indicatorPaddingLeft;
    }

    public int getIndicatorPaddingRight() {
        return indicatorPaddingRight;
    }

    public void setIndicatorPaddingRight(int indicatorPaddingRight) {
        this.indicatorPaddingRight = indicatorPaddingRight;
    }

    public int getIndicatorPaddingTop() {
        return indicatorPaddingTop;
    }

    public void setIndicatorPaddingTop(int indicatorPaddingTop) {
        this.indicatorPaddingTop = indicatorPaddingTop;
    }

    public int getIndicatorPaddingBottom() {
        return indicatorPaddingBottom;
    }

    public void setIndicatorPaddingBottom(int indicatorPaddingBottom) {
        this.indicatorPaddingBottom = indicatorPaddingBottom;
    }

    public int getIndicatorMargin() {
        return mIndicatorMargin;
    }

    public void setIndicatorMargin(int indicatorMargin) {
        this.mIndicatorMargin = indicatorMargin;
    }

    public int getIndicatorShowMode() {
        return mIndicatorShowMode;
    }

    /**
     * the indicator show mode
     * {@link #INDICATOR_SHOW_WHEN_TOUCH}
     * {@link #INDICATOR_ALWAYS_SHOW}
     * {@link #INDICATOR_ALWAYS_SHOW_AFTER_TOUCH}
     * {@link #INDICATOR_ALWAYS_SHOW}
     *
     * @param indicatorShowMode
     */
    public void setIndicatorShowMode(@IndicatorModeDef int indicatorShowMode) {
        this.mIndicatorShowMode = indicatorShowMode;
    }

    public void showIndicator(boolean isShown) {
        isShowIndicator = isShown;
    }

    public boolean isShowIndicator() {
        return isShowIndicator;
    }

    /**
     * include indicator text Height、padding、margin
     *
     * @return The actual occupation height of indicator
     */
    public int getIndicatorRawHeight() {
        Log.i(TAG, "getIndicatorRawHeight()");
        int result = 0;
        if (mIndicatorHeight > 0) {
            if (mIndicatorBitmap != null) {
                result = mIndicatorHeight + mIndicatorMargin;
            } else {
                result = mIndicatorHeight + mIndicatorArrowSize + mIndicatorMargin;
            }
        } else {
            if (mIndicatorBitmap != null) {
                result = Utils.measureText("8", mIndicatorTextSize).height() + indicatorPaddingTop + indicatorPaddingBottom + mIndicatorMargin;
            } else {
                result = Utils.measureText("8", mIndicatorTextSize).height() + indicatorPaddingTop + indicatorPaddingBottom + mIndicatorMargin + mIndicatorArrowSize;
            }
        }
        Log.i(TAG, "getIndicatorRawHeight() result = " + result);
        return result;
    }

    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public void setIndicatorHeight(int indicatorHeight) {
        this.mIndicatorHeight = indicatorHeight;
    }

    public int getIndicatorWidth() {
        return mIndicatorWidth;
    }

    public void setIndicatorWidth(int indicatorWidth) {
        this.mIndicatorWidth = indicatorWidth;
    }

    public int getIndicatorTextSize() {
        return mIndicatorTextSize;
    }

    public void setIndicatorTextSize(int indicatorTextSize) {
        this.mIndicatorTextSize = indicatorTextSize;
    }

    public int getIndicatorTextColor() {
        return mIndicatorTextColor;
    }

    public void setIndicatorTextColor(@ColorInt int indicatorTextColor) {
        this.mIndicatorTextColor = indicatorTextColor;
    }

    public int getIndicatorBackgroundColor() {
        return mIndicatorBackgroundColor;
    }

    public void setIndicatorBackgroundColor(@ColorInt int indicatorBackgroundColor) {
        this.mIndicatorBackgroundColor = indicatorBackgroundColor;
    }

    public int getThumbInactivatedDrawableId() {
        return mThumbInactivatedDrawableId;
    }

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

    public int getThumbDrawableId() {
        return mThumbDrawableId;
    }

    public void setThumbDrawableId(@DrawableRes int thumbDrawableId, int width, int height) {
        Log.i(TAG, "setThumbDrawableId() width = " + width + "; height = " + height);
        if (thumbDrawableId != 0 && getResources() != null && width > 0 && height > 0) {
            this.mThumbDrawableId = thumbDrawableId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThumbBitmap = Utils.drawableToBitmap(width, height, getResources().getDrawable(thumbDrawableId, null));
            } else {
                mThumbBitmap = Utils.drawableToBitmap(width, height, getResources().getDrawable(thumbDrawableId));
            }
        }
    }

    public void setThumbDrawableId(@DrawableRes int thumbDrawableId) {
        Log.i(TAG, "setThumbDrawableId()");
        if (mThumbWidth <= 0 || mThumbHeight <= 0) {
            throw new IllegalArgumentException("please set thumbWidth and thumbHeight first!");
        }
        if (thumbDrawableId != 0 && getResources() != null) {
            this.mThumbDrawableId = thumbDrawableId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThumbBitmap = Utils.drawableToBitmap(mThumbWidth, mThumbHeight, getResources().getDrawable(thumbDrawableId, null));
            } else {
                mThumbBitmap = Utils.drawableToBitmap(mThumbWidth, mThumbHeight, getResources().getDrawable(thumbDrawableId));
            }
        }
    }

    public int getThumbWidth() {
        return mThumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        this.mThumbWidth = thumbWidth;
    }

    public float getThumbScaleHeight() {
        return mThumbHeight * mThumbScaleRatio;
    }

    public float getThumbScaleWidth() {
        return mThumbWidth * mThumbScaleRatio;
    }

    public int getThumbHeight() {
        return mThumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        this.mThumbHeight = thumbHeight;
    }

    public float getIndicatorRadius() {
        return mIndicatorRadius;
    }

    public void setIndicatorRadius(float indicatorRadius) {
        this.mIndicatorRadius = indicatorRadius;
    }

    protected boolean getActivate() {
        return isActivate;
    }

    protected void setActivate(boolean activate) {
        isActivate = activate;
    }

    public void setTypeface(Typeface typeFace) {
        mPaint.setTypeface(typeFace);
    }


    /**
     * when you touch or move, the thumb will scale, default not scale
     *
     * @return default 1.0f
     */
    public float getThumbScaleRatio() {
        return mThumbScaleRatio;
    }

    public boolean isVisible() {
        return isVisible;
    }

    /**
     * if visble is false, will clear the Canvas
     *
     * @param visible
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public float getProgress() {
        float range = mRangeSeekBar.getMaxProgress() - mRangeSeekBar.getMinProgress();
        return mRangeSeekBar.getMinProgress() + range * mCurrPercent;
    }
}
