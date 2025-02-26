package com.gdu.demo.widget.zoomView;


import static com.gdu.demo.widget.zoomView.SeekBar.INDICATOR_ALWAYS_HIDE;
import static com.gdu.demo.widget.zoomView.SeekBar.INDICATOR_ALWAYS_SHOW;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import com.gdu.demo.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class RangeSeekBar extends View {
    private final String TAG = "RangeSeekBar";

    private final static int MIN_INTERCEPT_DISTANCE = 100;

    //normal seekBar mode
    public final static int SEEKBAR_MODE_SINGLE = 1;
    //RangeSeekBar
    public final static int SEEKBAR_MODE_RANGE = 2;

    /**
     * @hide
     */
    @IntDef({SEEKBAR_MODE_SINGLE, SEEKBAR_MODE_RANGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekBarModeDef {
    }

    //number according to the actual proportion of the number of arranged;
    public final static int TRICK_MARK_MODE_NUMBER = 0;
    //other equally arranged
    public final static int TRICK_MARK_MODE_OTHER = 1;

    /**
     * @hide
     */
    @IntDef({TRICK_MARK_MODE_NUMBER, TRICK_MARK_MODE_OTHER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkModeDef {
    }

    //tick mark text gravity
    public final static int TICK_MARK_GRAVITY_LEFT = 0;
    public final static int TICK_MARK_GRAVITY_CENTER = 1;
    public final static int TICK_MARK_GRAVITY_RIGHT = 2;

    /**
     * @hide
     */
    @IntDef({TICK_MARK_GRAVITY_LEFT, TICK_MARK_GRAVITY_CENTER, TICK_MARK_GRAVITY_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkGravityDef {
    }

    /**
     * @hide
     */
    @IntDef({Gravity.TOP, Gravity.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkLayoutGravityDef {
    }

    /**
     * @hide
     */
    @IntDef({Gravity.TOP, Gravity.CENTER, Gravity.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GravityDef {
    }

    public static class Gravity {
        public final static int TOP = 0;
        public final static int BOTTOM = 1;
        public final static int CENTER = 2;
    }

    /**
     * @hide
     */
    @IntDef({SEEKBAR_PROGRESS_FILL_MODE_FILL, SEEKBAR_PROGRESS_FILL_MODE_FILL_PROGRESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekBarProgressFillModeDef {
    }

    //progress填充整个控件宽度
    public final static int SEEKBAR_PROGRESS_FILL_MODE_FILL = 0;
    //progress填充整个range宽度
    public final static int SEEKBAR_PROGRESS_FILL_MODE_FILL_PROGRESS = 1;

    private int mProgressTop, mProgressBottom, mProgressLeft, mProgressRight;
    private int mSeekBarMode;
    /** 刻度模式：number根据数字实际比例排列；other 均分排列 */
    private int mTickMarkMode;
    /** 刻度与进度条间的间距 */
    //The spacing between the tick mark and the progress bar
    private int mTickMarkTextMargin;
    /** 刻度文字与提示文字的大小 */
    //tick mark text and prompt text size
    private int mTickMarkTextSize;
    private int mTickMarkGravity;
    private int mTickMarkLayoutGravity;
    private int mTickMarkTextColor;
    private int mTickMarkInRangeTextColor;
    /** 刻度上显示的文字 */
    //The texts displayed on the scale
    private CharSequence[] mTickMarkTextArray;
    /** 进度条圆角 */
    //radius of progress bar
    private float mProgressRadius;
    /** 进度中进度条的颜色 */
    //the color of seekBar in progress
    private int mProgressColor;
    /** 默认进度条颜色 */
    //the default color of the progress bar
    private int mProgressDefaultColor;

    //the drawable of seekBar in progress
    private int mProgressDrawableId;
    //the default Drawable of the progress bar
    private int mProgressDefaultDrawableId;

    //the progress height
    private int mProgressHeight;
    // the progress width
    private int mProgressWidth;
    //the range interval of RangeSeekBar
    private float mMinInterval;
    /** rsg_gravity */
    private int mGravity;
    /** enable RangeSeekBar two thumb Overlap */
    private boolean mEnableThumbOverlap;

    /** the color of step divs */
    private int mStepsColor;
    /** the width of each step */
    private float mStepsWidth;
    /** the height of each step */
    private float mStepsHeight;
    /** the radius of step divs */
    private float mStepsRadius;
    /** steps is 0 will disable StepSeekBar */
    private int mSteps;
    /** the thumb will automatic bonding close to its value */
    private boolean mStepsAutoBonding;
    private int mStepsDrawableId;
    /** True values set by the user */
    private float mMinProgress, mMaxProgress;

    /** 进度条圆角 */
    //radius of progress bar
    private int mProgressFillMode;
    //****************** the above is attr value  ******************//

    private boolean isEnable = true;
    float mTouchDownX, mTouchDownY;
    /** 剩余最小间隔的进度 */
    float mReservePercent;
    boolean isScaleThumb = false;
    Paint mPaint = new Paint();
    RectF mProgressDefaultDstRect = new RectF();
    RectF mProgressDstRect = new RectF();
    Rect mProgressSrcRect = new Rect();
    RectF mStepDivRect = new RectF();
    Rect mTickMarkTextRect = new Rect();
    SeekBar mLeftSB;
    SeekBar mRightSB;
    SeekBar mCurrTouchSB;
    Bitmap mProgressBitmap;
    Bitmap mProgressDefaultBitmap;
    List<Bitmap> mStepsBitmaps = new ArrayList<>();
    private int mProgressPaddingRight;
    private OnRangeChangedListener mCallback;

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initPaint();
        initSeekBar(attrs);
        initStepsBitmap();
    }

    private void initProgressBitmap() {
        Log.i(TAG, "initProgressBitmap()");
        if (mProgressBitmap == null) {
            mProgressBitmap = Utils.drawableToBitmap(getContext(), mProgressWidth, mProgressHeight, mProgressDrawableId);
        }
        if (mProgressDefaultBitmap == null) {
            mProgressDefaultBitmap = Utils.drawableToBitmap(getContext(), mProgressWidth, mProgressHeight, mProgressDefaultDrawableId);
        }
    }

    private boolean verifyStepsMode() {
        return mSteps >= 1 && !(mStepsHeight <= 0) && !(mStepsWidth <= 0);
    }

    private void initStepsBitmap() {
        Log.i(TAG, "initStepsBitmap()");
        if (!verifyStepsMode() || mStepsDrawableId == 0) {
            return;
        }
        if (mStepsBitmaps.isEmpty()) {
            Bitmap bitmap = Utils.drawableToBitmap(getContext(), (int) mStepsWidth, (int) mStepsHeight, mStepsDrawableId);
            for (int i = 0; i <= mSteps; i++) {
                mStepsBitmaps.add(bitmap);
            }
        }
    }

    protected void initSeekBar(AttributeSet attrs) {
        Log.i(TAG, "initSeekBar()");
        mLeftSB = new SeekBar(this, attrs, true);
        mRightSB = new SeekBar(this, attrs, false);
        mRightSB.setVisible(mSeekBarMode != SEEKBAR_MODE_SINGLE);
    }


    protected void initAttrs(AttributeSet attrs) {
        Log.i(TAG, "initAttrs()");
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
            mSeekBarMode = t.getInt(R.styleable.RangeSeekBar_rsb_mode, SEEKBAR_MODE_RANGE);
            mProgressFillMode = t.getInt(R.styleable.RangeSeekBar_rsb_progress_fill_mode, SEEKBAR_PROGRESS_FILL_MODE_FILL);
            mMinProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_min, 0);
            mMaxProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_max, 100);
            mMinInterval = t.getFloat(R.styleable.RangeSeekBar_rsb_min_interval, 0);
            mGravity = t.getInt(R.styleable.RangeSeekBar_rsb_gravity, Gravity.TOP);
            mProgressColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_color, 0xFF4BD962);
            mProgressRadius = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_progress_radius, -1);
            mProgressDefaultColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_default_color, 0xFFD7D7D7);
            mProgressDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable, 0);
            mProgressDefaultDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable_default, 0);
            mProgressHeight = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_progress_height, Utils.dp2px(getContext(), 2));
            mTickMarkMode = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_mode, TRICK_MARK_MODE_NUMBER);
            mTickMarkGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_gravity, TICK_MARK_GRAVITY_CENTER);
            mTickMarkLayoutGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_layout_gravity, Gravity.TOP);
            mTickMarkTextArray = t.getTextArray(R.styleable.RangeSeekBar_rsb_tick_mark_text_array);
            mTickMarkTextMargin = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_margin, Utils.dp2px(getContext(), 7));
            mTickMarkTextSize = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_size, Utils.dp2px(getContext(), 12));
            mTickMarkTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, mProgressDefaultColor);
            mTickMarkInRangeTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_in_range_text_color, mProgressColor);
            mSteps = t.getInt(R.styleable.RangeSeekBar_rsb_steps, 0);
            mStepsColor = t.getColor(R.styleable.RangeSeekBar_rsb_step_color, 0xFF9d9d9d);
            mStepsRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_step_radius, 0);
            mStepsWidth = t.getDimension(R.styleable.RangeSeekBar_rsb_step_width, 0);
            mStepsHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_step_height, 0);
            mStepsDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_step_drawable, 0);
            mStepsAutoBonding = t.getBoolean(R.styleable.RangeSeekBar_rsb_step_auto_bonding, true);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * measure progress bar position
     */
    protected void onMeasureProgress(int w, int h) {
        Log.i(TAG, "onMeasureProgress() w = " + w + "; h = " + h);
        final int viewHeight = h - getPaddingBottom() - getPaddingTop();
        Log.i(TAG, "onMeasureProgress() viewHeight = " + viewHeight + "; mGravity = " + mGravity);
        if (h <= 0) {
            return;
        }
        if (mGravity == Gravity.TOP) {
            //calculate the height of indicator and thumb exceeds the part of the progress
            float maxIndicatorHeight = 0;
            if (mLeftSB.getIndicatorShowMode() != INDICATOR_ALWAYS_HIDE
                    || mRightSB.getIndicatorShowMode() != INDICATOR_ALWAYS_HIDE) {
                maxIndicatorHeight = Math.max(mLeftSB.getIndicatorRawHeight(), mRightSB.getIndicatorRawHeight());
            }
            float thumbHeight = Math.max(mLeftSB.getThumbScaleHeight(), mRightSB.getThumbScaleHeight());
            thumbHeight -= mProgressHeight / 2f;

            //default height is indicator + thumb exceeds the part of the progress bar
            //if tickMark height is greater than (indicator + thumb exceeds the part of the progress)
            mProgressTop = (int) (maxIndicatorHeight + (thumbHeight - mProgressHeight) / 2f);
            if (mTickMarkTextArray != null && mTickMarkLayoutGravity == Gravity.TOP) {
                mProgressTop = (int) Math.max(getTickMarkRawHeight(), maxIndicatorHeight + (thumbHeight - mProgressHeight) / 2f);
            }
            mProgressBottom = mProgressTop + mProgressHeight;
        } else if (mGravity == Gravity.BOTTOM) {
            if (mTickMarkTextArray != null && mTickMarkLayoutGravity == Gravity.BOTTOM) {
                mProgressBottom = viewHeight - getTickMarkRawHeight();
            } else {
                mProgressBottom = (int) (viewHeight - Math.max(mLeftSB.getThumbScaleHeight(), mRightSB.getThumbScaleHeight()) / 2f
                        + mProgressHeight / 2f);
            }
            mProgressTop = mProgressBottom - mProgressHeight;
        } else {
            mProgressTop = (viewHeight - mProgressHeight) / 2;
            mProgressBottom = mProgressTop + mProgressHeight;
        }

        int maxThumbWidth = (int) Math.max(mLeftSB.getThumbScaleWidth(), mRightSB.getThumbScaleWidth());
        mProgressLeft = maxThumbWidth / 2 + getPaddingLeft();
        mProgressRight = w - maxThumbWidth / 2 - getPaddingRight();
        mProgressWidth = mProgressRight - mProgressLeft;
        mProgressDefaultDstRect.set(getProgressLeft(), getProgressTop(), getProgressRight(), getProgressBottom());
        mProgressPaddingRight = w - mProgressRight;
        //default value
        if (mProgressRadius <= 0) {
            mProgressRadius = (int) ((getProgressBottom() - getProgressTop()) * 0.45f);
        }
        Log.i(TAG, "initProgressBitmap()");
        initProgressBitmap();
    }

    //Android 7.0以后，优化了View的绘制，onMeasure和onSizeChanged调用顺序有所变化
    //Android7.0以下：onMeasure--->onSizeChanged--->onMeasure
    //Android7.0以上：onMeasure--->onSizeChanged
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.i(TAG, "onMeasure()");
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */
//        Log.i(TAG, "onMeasure() RangeSeekBar heightMode = " + heightMode);
        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else if (heightMode == MeasureSpec.AT_MOST && getParent() instanceof ViewGroup
                && heightSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            heightSize = MeasureSpec.makeMeasureSpec(((ViewGroup) getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST);
        } else {
            int heightNeeded;
            if (mGravity == Gravity.CENTER) {
                if (mTickMarkTextArray != null && mTickMarkLayoutGravity == Gravity.BOTTOM) {
                    heightNeeded = (int) (2 * (getRawHeight() - getTickMarkRawHeight()));
                } else {
                    heightNeeded = (int) (2 * (getRawHeight() - Math.max(mLeftSB.getThumbScaleHeight(), mRightSB.getThumbScaleHeight()) / 2));
                }
            } else {
                heightNeeded = (int) getRawHeight();
            }
            heightSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY);
        }
//        Log.i(TAG, "onMeasure() RangeSeekBar heightSize = " + heightSize);
        super.onMeasure(widthMeasureSpec, heightSize);
    }

    protected int getTickMarkRawHeight() {
        Log.i(TAG, "getTickMarkRawHeight()");
        int result = 0;
        if (mTickMarkTextArray != null && mTickMarkTextArray.length > 0) {
            result = mTickMarkTextMargin + Utils.measureText(String.valueOf(mTickMarkTextArray[0]), mTickMarkTextSize).height() + 3;
        }
        Log.i(TAG, "getTickMarkRawHeight() result = " + result);
        return result;
    }

    protected float getRawHeight() {
        Log.i(TAG, "getRawHeight()");
        float rawHeight;
        if (mSeekBarMode == SEEKBAR_MODE_SINGLE) {
            rawHeight = mLeftSB.getRawHeight();
            if (mTickMarkLayoutGravity == Gravity.BOTTOM && mTickMarkTextArray != null) {
                float h = Math.max((mLeftSB.getThumbScaleHeight() - mProgressHeight) / 2, getTickMarkRawHeight());
                rawHeight = rawHeight - mLeftSB.getThumbScaleHeight() / 2 + mProgressHeight / 2f + h;
            }
        } else {
            rawHeight = Math.max(mLeftSB.getRawHeight(), mRightSB.getRawHeight());
            if (mTickMarkLayoutGravity == Gravity.BOTTOM && mTickMarkTextArray != null) {
                float thumbHeight = Math.max(mLeftSB.getThumbScaleHeight(), mRightSB.getThumbScaleHeight());
                float h = Math.max((thumbHeight - mProgressHeight) / 2, getTickMarkRawHeight());
                rawHeight = rawHeight - thumbHeight / 2 + mProgressHeight / 2f + h;
            }
        }
        Log.i(TAG, "getRawHeight() rawHeight = " + rawHeight);
        return rawHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChanged() w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        onMeasureProgress(w, h);
        //set default value
        setRange(mMinProgress, mMaxProgress, mMinInterval);
        // initializes the positions of the two thumbs
        final int lineCenterY = (getProgressBottom() + getProgressTop()) / 2;
        Log.i(TAG, "onSizeChanged() lineCenterY = " + lineCenterY);
        mLeftSB.onSizeChanged(getProgressLeft(), lineCenterY);
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mRightSB.onSizeChanged(getProgressLeft(), lineCenterY);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        Log.i(TAG, "onDraw()");
        super.onDraw(canvas);
        onDrawTickMark(canvas, mPaint);
        onDrawProgressBar(canvas, mPaint);
        onDrawSteps(canvas, mPaint);
        onDrawSeekBar(canvas);
    }

    //绘制刻度，并且根据当前位置是否在刻度范围内设置不同的颜色显示
    // Draw the scales, and according to the current position is set within
    // the scale range of different color display
    protected void onDrawTickMark(Canvas canvas, Paint paint) {
        Log.i(TAG, "onDrawTickMark() tickArraySize");
        if (mTickMarkTextArray != null) {
            Log.i(TAG, "onDrawTickMark() tickArraySize = " + mTickMarkTextArray.length);
            // 单个刻度文字标签宽度
            final int trickPartWidth = mProgressWidth / (mTickMarkTextArray.length - 1);
            for (int i = 0; i < mTickMarkTextArray.length; i++) {
                final String text2Draw = mTickMarkTextArray[i].toString();
                if (TextUtils.isEmpty(text2Draw)) {
                    continue;
                }
                paint.getTextBounds(text2Draw, 0, text2Draw.length(), mTickMarkTextRect);
                paint.setColor(mTickMarkTextColor);
                //平分显示
                float x;
                if (mTickMarkMode == TRICK_MARK_MODE_OTHER) {
                    if (mTickMarkGravity == TICK_MARK_GRAVITY_RIGHT) {
                        x = getProgressLeft() + i * trickPartWidth - mTickMarkTextRect.width();
                    } else if (mTickMarkGravity == TICK_MARK_GRAVITY_CENTER) {
                        x = getProgressLeft() + i * trickPartWidth - mTickMarkTextRect.width() / 2f;
                    } else {
                        x = getProgressLeft() + i * trickPartWidth;
                    }
                } else {
                    float num = Utils.parseFloat(text2Draw);
                    SeekBarState[] states = getRangeSeekBarState();
                    if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(num, states[1].value) != 1 && (mSeekBarMode == SEEKBAR_MODE_RANGE)) {
                        paint.setColor(mTickMarkInRangeTextColor);
                    }
                    //按实际比例显示
                    x = getProgressLeft() + mProgressWidth * (num - mMinProgress) / (mMaxProgress - mMinProgress)
                            - mTickMarkTextRect.width() / 2f;
                }
                float y;
                if (mTickMarkLayoutGravity == Gravity.TOP) {
                    y = getProgressTop() - mTickMarkTextMargin;
                } else {
                    y = getProgressBottom() + mTickMarkTextMargin + mTickMarkTextRect.height();
                }
                canvas.drawText(text2Draw, x, y, paint);
            }
        }
    }

    //绘制进度条
    // draw the progress bar
    protected void onDrawProgressBar(Canvas canvas, Paint paint) {
        Log.i(TAG, "onDrawProgressBar()");

        //draw default progress
        if (Utils.verifyBitmap(mProgressDefaultBitmap)) {
            canvas.drawBitmap(mProgressDefaultBitmap, null, mProgressDefaultDstRect, paint);
        } else {
            paint.setColor(mProgressDefaultColor);
            canvas.drawRoundRect(mProgressDefaultDstRect, mProgressRadius, mProgressRadius, paint);
        }

        //draw progress
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mProgressDstRect.top = getProgressTop();
            mProgressDstRect.left = mLeftSB.mLeft + mLeftSB.getThumbScaleWidth() / 2f + mProgressWidth * mLeftSB.mCurrPercent;
            mProgressDstRect.right = mRightSB.mLeft + mRightSB.getThumbScaleWidth() / 2f + mProgressWidth * mRightSB.mCurrPercent;
            mProgressDstRect.bottom = getProgressBottom();
        } else {
            mProgressDstRect.top = getProgressTop();
            mProgressDstRect.left = mLeftSB.mLeft + mLeftSB.getThumbScaleWidth() / 2f;
            mProgressDstRect.right = mLeftSB.mLeft + mLeftSB.getThumbScaleWidth() / 2f + mProgressWidth * mLeftSB.mCurrPercent;
            mProgressDstRect.bottom = getProgressBottom();
        }

        if (Utils.verifyBitmap(mProgressBitmap)) {
            mProgressSrcRect.top = 0;
            mProgressSrcRect.bottom = mProgressBitmap.getHeight();
            int bitmapWidth = mProgressBitmap.getWidth();
            if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                if (mProgressFillMode == SEEKBAR_PROGRESS_FILL_MODE_FILL_PROGRESS) {
                    mProgressSrcRect.left = 0;
                    mProgressSrcRect.right = bitmapWidth;
                } else {
                    mProgressSrcRect.left = (int) (bitmapWidth * mLeftSB.mCurrPercent);
                    mProgressSrcRect.right = (int) (bitmapWidth * mRightSB.mCurrPercent);
                }
            } else {
                mProgressSrcRect.left = 0;
                mProgressSrcRect.right = (int) (bitmapWidth * mLeftSB.mCurrPercent);
            }
            canvas.drawBitmap(mProgressBitmap, mProgressSrcRect, mProgressDstRect, null);
        } else {
            paint.setColor(mProgressColor);
            canvas.drawRoundRect(mProgressDstRect, mProgressRadius, mProgressRadius, paint);
        }

    }

    //draw steps
    protected void onDrawSteps(Canvas canvas, Paint paint) {
        Log.i(TAG, "onDrawSteps()");
        if (!verifyStepsMode()) {
            return;
        }
        final int stepMarks = getProgressWidth() / (mSteps);
        float extHeight = (mStepsHeight - getProgressHeight()) / 2f;
        for (int k = 0; k <= mSteps; k++) {
            float x = getProgressLeft() + k * stepMarks - mStepsWidth / 2f;
            mStepDivRect.set(x, getProgressTop() - extHeight, x + mStepsWidth, getProgressBottom() + extHeight);
            if (mStepsBitmaps.isEmpty() || mStepsBitmaps.size() <= k) {
                paint.setColor(mStepsColor);
                canvas.drawRoundRect(mStepDivRect, mStepsRadius, mStepsRadius, paint);
            } else {
                canvas.drawBitmap(mStepsBitmaps.get(k), null, mStepDivRect, paint);
            }
        }
    }

    /**
     * 绘制SeekBar相关
     * @param canvas
     */
    protected void onDrawSeekBar(Canvas canvas) {
        Log.i(TAG, "onDrawSeekBar()");
        //draw left SeekBar
        if (mLeftSB.getIndicatorShowMode() == INDICATOR_ALWAYS_SHOW) {
            mLeftSB.setShowIndicatorEnable(true);
        }
        mLeftSB.draw(canvas);
        //draw right SeekBar
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            if (mRightSB.getIndicatorShowMode() == INDICATOR_ALWAYS_SHOW) {
                mRightSB.setShowIndicatorEnable(true);
            }
            mRightSB.draw(canvas);
        }
    }

    //初始化画笔
    private void initPaint() {
        Log.i(TAG, "initPaint()");
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mProgressDefaultColor);
        mPaint.setFakeBoldText(true);
        mPaint.setTextSize(mTickMarkTextSize);
    }


    private void changeThumbActivateState(boolean hasActivate) {
        Log.i(TAG, "changeThumbActivateState() hasActivate = " + hasActivate);
        if (hasActivate && mCurrTouchSB != null) {
            boolean state = mCurrTouchSB == mLeftSB;
            mLeftSB.setActivate(state);
            if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                mRightSB.setActivate(!state);
            }
        } else {
            mLeftSB.setActivate(false);
            if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                mRightSB.setActivate(false);
            }
        }
    }

    protected float getEventX(MotionEvent event) {
        return event.getX();
    }

    protected float getEventY(MotionEvent event) {
        return event.getY();
    }

    /**
     * scale the touch seekBar thumb
     */
    private void scaleCurrentSeekBarThumb() {
        Log.i(TAG, "scaleCurrentSeekBarThumb()");
        if (mCurrTouchSB != null && mCurrTouchSB.getThumbScaleRatio() > 1f && !isScaleThumb) {
            isScaleThumb = true;
            mCurrTouchSB.scaleThumb();
        }
    }

    /**
     * reset the touch seekBar thumb
     */
    private void resetCurrentSeekBarThumb() {
        Log.i(TAG, "resetCurrentSeekBarThumb()");
        if (mCurrTouchSB != null && mCurrTouchSB.getThumbScaleRatio() > 1f && isScaleThumb) {
            isScaleThumb = false;
            mCurrTouchSB.resetThumb();
        }
    }

    //calculate currTouchSB percent by MotionEvent
    protected float calculateCurrentSeekBarPercent(float touchDownX) {
        Log.i(TAG, "calculateCurrentSeekBarPercent() touchDownX = " + touchDownX);
        if (mCurrTouchSB == null) {
            return 0;
        }
        float percent = (touchDownX - getProgressLeft()) * 1f / (mProgressWidth);
        if (touchDownX < getProgressLeft()) {
            percent = 0;
        } else if (touchDownX > getProgressRight()) {
            percent = 1;
        }
        //RangeMode minimum interval
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            if (mCurrTouchSB == mLeftSB) {
                if (percent > mRightSB.mCurrPercent - mReservePercent) {
                    percent = mRightSB.mCurrPercent - mReservePercent;
                }
            } else if (mCurrTouchSB == mRightSB) {
                if (percent < mLeftSB.mCurrPercent + mReservePercent) {
                    percent = mLeftSB.mCurrPercent + mReservePercent;
                }
            }
        }
        Log.i(TAG, "calculateCurrentSeekBarPercent() percent = " + percent);
        return percent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent() isEnable = " + isEnable);
        if (!isEnable) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = getEventX(event);
                mTouchDownY = getEventY(event);
                if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                    if (mRightSB.mCurrPercent >= 1 && mLeftSB.collide(getEventX(event), getEventY(event))) {
                        mCurrTouchSB = mLeftSB;
                        scaleCurrentSeekBarThumb();
                    } else if (mRightSB.collide(getEventX(event), getEventY(event))) {
                        mCurrTouchSB = mRightSB;
                        scaleCurrentSeekBarThumb();
                    } else {
                        float performClick = (mTouchDownX - getProgressLeft()) * 1f / (mProgressWidth);
                        float distanceLeft = Math.abs(mLeftSB.mCurrPercent - performClick);
                        float distanceRight = Math.abs(mRightSB.mCurrPercent - performClick);
                        if (distanceLeft < distanceRight) {
                            mCurrTouchSB = mLeftSB;
                        } else {
                            mCurrTouchSB = mRightSB;
                        }
                        performClick = calculateCurrentSeekBarPercent(mTouchDownX);
                        mCurrTouchSB.slide(performClick);
                    }
                } else {
                    mCurrTouchSB = mLeftSB;
                    scaleCurrentSeekBarThumb();
                }

                //Intercept parent TouchEvent
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mCallback != null) {
                    mCallback.onStartTrackingTouch(this, mCurrTouchSB == mLeftSB);
                }
                changeThumbActivateState(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mCurrTouchSB != null) {
                    float x = getEventX(event);
                    if ((mSeekBarMode == SEEKBAR_MODE_RANGE) && mLeftSB.mCurrPercent == mRightSB.mCurrPercent) {
                        mCurrTouchSB.materialRestore();
                        if (mCallback != null) {
                            mCallback.onStopTrackingTouch(this, mCurrTouchSB == mLeftSB);
                        }
                        if (x - mTouchDownX > 0) {
                            //method to move right
                            if (mCurrTouchSB != mRightSB) {
                                mCurrTouchSB.setShowIndicatorEnable(false);
                                resetCurrentSeekBarThumb();
                                mCurrTouchSB = mRightSB;
                            }
                        } else {
                            //method to move left
                            if (mCurrTouchSB != mLeftSB) {
                                mCurrTouchSB.setShowIndicatorEnable(false);
                                resetCurrentSeekBarThumb();
                                mCurrTouchSB = mLeftSB;
                            }
                        }
                        if (mCallback != null) {
                            mCallback.onStartTrackingTouch(this, mCurrTouchSB == mLeftSB);
                        }
                    }
                    scaleCurrentSeekBarThumb();
                    mCurrTouchSB.material = mCurrTouchSB.material >= 1 ? 1 : mCurrTouchSB.material + 0.1f;
                    mTouchDownX = x;
                    mCurrTouchSB.slide(calculateCurrentSeekBarPercent(mTouchDownX));
                    mCurrTouchSB.setShowIndicatorEnable(true);
                }

                if (mCallback != null) {
                    SeekBarState[] states = getRangeSeekBarState();
                    mCallback.onRangeChanged(this, states[0].value, states[1].value, true);
                }
                invalidate();
                //Intercept parent TouchEvent
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                changeThumbActivateState(true);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                    mRightSB.setShowIndicatorEnable(false);
                }
                if (mCurrTouchSB != null) {
                    if (mCurrTouchSB == mLeftSB) {
                        resetCurrentSeekBarThumb();
                    } else if (mCurrTouchSB == mRightSB) {
                        resetCurrentSeekBarThumb();
                    }
                }
                if (mLeftSB != null) {
                    mLeftSB.setShowIndicatorEnable(false);
                }
                if (mCallback != null) {
                    SeekBarState[] states = getRangeSeekBarState();
                    mCallback.onRangeChanged(this, states[0].value, states[1].value, false);
                }
                //Intercept parent TouchEvent
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                changeThumbActivateState(false);
                break;
            case MotionEvent.ACTION_UP:
                if (mCurrTouchSB != null) {
                    if (verifyStepsMode() && mStepsAutoBonding) {
                        float percent = calculateCurrentSeekBarPercent(getEventX(event));
                        float stepPercent = 1.0f / mSteps;
                        int stepSelected =
                                new BigDecimal(percent / stepPercent).setScale(0, RoundingMode.HALF_UP).intValue();
                        mCurrTouchSB.slide(stepSelected * stepPercent);
                    }

                    if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
                        mRightSB.setShowIndicatorEnable(false);
                    }
                    mLeftSB.setShowIndicatorEnable(false);
                    mCurrTouchSB.materialRestore();
                    resetCurrentSeekBarThumb();
                    if (mCallback != null) {
                        SeekBarState[] states = getRangeSeekBarState();
                        mCallback.onRangeChanged(this, states[0].value, states[1].value, false);
                    }
                    //Intercept parent TouchEvent
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (mCallback != null) {
                        mCallback.onStopTrackingTouch(this, mCurrTouchSB == mLeftSB);
                    }
                    changeThumbActivateState(false);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.minValue = mMinProgress;
        ss.maxValue = mMaxProgress;
        ss.rangeInterval = mMinInterval;
        SeekBarState[] results = getRangeSeekBarState();
        ss.currSelectedMin = results[0].value;
        ss.currSelectedMax = results[1].value;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            float min = ss.minValue;
            float max = ss.maxValue;
            float rangeInterval = ss.rangeInterval;
            setRange(min, max, rangeInterval);
            float currSelectedMin = ss.currSelectedMin;
            float currSelectedMax = ss.currSelectedMax;
            setProgress(currSelectedMin, currSelectedMax);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //******************* Attributes getter and setter *******************//

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        mCallback = listener;
    }

    public void setProgress(float value) {
        Log.i(TAG, "setProgress() value = " + value);
        setProgress(value, mMaxProgress);
    }

    public void setProgress(float leftValue, float rightValue) {
        Log.i(TAG, "setProgress() leftValue = " + leftValue + "; rightValue = " + rightValue);
        leftValue = Math.min(leftValue, rightValue);
        rightValue = Math.max(leftValue, rightValue);
        if (rightValue - leftValue < mMinInterval) {
            if (leftValue - mMinProgress > mMaxProgress - rightValue) {
                leftValue = rightValue - mMinInterval;
            } else {
                rightValue = leftValue + mMinInterval;
            }
        }

        if (leftValue < mMinProgress) {
            return;
        }
        if (rightValue > mMaxProgress) {
            return;
        }

        float range = mMaxProgress - mMinProgress;
        mLeftSB.mCurrPercent = Math.abs(leftValue - mMinProgress) / range;
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mRightSB.mCurrPercent = Math.abs(rightValue - mMinProgress) / range;
        }

        if (mCallback != null) {
            mCallback.onRangeChanged(this, leftValue, rightValue, false);
        }
        invalidate();
    }


    /**
     * 设置范围
     *
     * @param min 最小值
     * @param max 最大值
     */
    public void setRange(float min, float max) {
        Log.i(TAG, "setRange() min = " + min + "; max = " + max);
        setRange(min, max, mMinInterval);
    }

    /**
     * 设置范围
     *
     * @param min         最小值
     * @param max         最大值
     * @param minInterval 最小间隔
     */
    public void setRange(float min, float max, float minInterval) {
        Log.i(TAG, "setRange() min = " + min + "; max = " + max + "; minInterval = " + minInterval);
        if (max <= min) {
            throw new IllegalArgumentException("setRange() max must be greater than min ! #max:" + max + " #min:" + min);
        }
        if (minInterval < 0) {
            throw new IllegalArgumentException("setRange() interval must be greater than zero ! #minInterval:" + minInterval);
        }
        if (minInterval >= max - min) {
            throw new IllegalArgumentException("setRange() interval must be less than (max - min) ! #minInterval:" + minInterval + " #max - min:" + (max - min));
        }

        mMaxProgress = max;
        mMinProgress = min;
        this.mMinInterval = minInterval;
        mReservePercent = minInterval / (max - min);

        //set default value
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            if (mLeftSB.mCurrPercent + mReservePercent <= 1 && mLeftSB.mCurrPercent + mReservePercent > mRightSB.mCurrPercent) {
                mRightSB.mCurrPercent = mLeftSB.mCurrPercent + mReservePercent;
            } else if (mRightSB.mCurrPercent - mReservePercent >= 0 && mRightSB.mCurrPercent - mReservePercent < mLeftSB.mCurrPercent) {
                mLeftSB.mCurrPercent = mRightSB.mCurrPercent - mReservePercent;
            }
        }
        invalidate();
    }

    public SeekBarState[] getRangeSeekBarState() {
        SeekBarState leftSeekBarState = new SeekBarState();
        leftSeekBarState.value = mLeftSB.getProgress();

        leftSeekBarState.indicatorText = String.valueOf(leftSeekBarState.value);
        if (Utils.compareFloat(leftSeekBarState.value, mMinProgress) == 0) {
            leftSeekBarState.isMin = true;
        } else if (Utils.compareFloat(leftSeekBarState.value, mMaxProgress) == 0) {
            leftSeekBarState.isMax = true;
        }

        SeekBarState rightSeekBarState = new SeekBarState();
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBarState.value = mRightSB.getProgress();
            rightSeekBarState.indicatorText = String.valueOf(rightSeekBarState.value);
            if (Utils.compareFloat(mRightSB.mCurrPercent, mMinProgress) == 0) {
                rightSeekBarState.isMin = true;
            } else if (Utils.compareFloat(mRightSB.mCurrPercent, mMaxProgress) == 0) {
                rightSeekBarState.isMax = true;
            }
        }

        return new SeekBarState[]{leftSeekBarState, rightSeekBarState};
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnable = enabled;
    }

    public void setIndicatorText(String progress) {
        mLeftSB.setIndicatorText(progress);
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mRightSB.setIndicatorText(progress);
        }
    }

    /**
     * format number indicator text
     *
     * @param formatPattern format rules
     */
    public void setIndicatorTextDecimalFormat(DecimalFormat formatPattern) {
        mLeftSB.setIndicatorTextDecimalFormat(formatPattern);
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mRightSB.setIndicatorTextDecimalFormat(formatPattern);
        }
    }

    /**
     * format string indicator text
     *
     * @param formatPattern format rules
     */
    public void setIndicatorTextStringFormat(String formatPattern) {
        mLeftSB.setIndicatorTextStringFormat(formatPattern);
        if (mSeekBarMode == SEEKBAR_MODE_RANGE) {
            mRightSB.setIndicatorTextStringFormat(formatPattern);
        }
    }

    /**
     * if is single mode, please use it to get the SeekBar
     *
     * @return left seek bar
     */
    public SeekBar getLeftSeekBar() {
        return mLeftSB;
    }

    public SeekBar getRightSeekBar() {
        return mRightSB;
    }


    public int getProgressTop() {
        return mProgressTop;
    }

    public int getProgressBottom() {
        return mProgressBottom;
    }

    public int getProgressLeft() {
        return mProgressLeft;
    }

    public int getProgressRight() {
        return mProgressRight;
    }

    public int getProgressPaddingRight() {
        return mProgressPaddingRight;
    }

    public int getProgressHeight() {
        return mProgressHeight;
    }

    public void setProgressHeight(int progressHeight) {
        this.mProgressHeight = progressHeight;
    }

    public float getMinProgress() {
        return mMinProgress;
    }

    public float getMaxProgress() {
        return mMaxProgress;
    }

    public void setProgressColor(@ColorInt int progressDefaultColor, @ColorInt int progressColor) {
        this.mProgressDefaultColor = progressDefaultColor;
        this.mProgressColor = progressColor;
    }

    public int getTickMarkTextColor() {
        return mTickMarkTextColor;
    }

    public void setTickMarkTextColor(@ColorInt int tickMarkTextColor) {
        this.mTickMarkTextColor = tickMarkTextColor;
    }

    public int getTickMarkInRangeTextColor() {
        return mTickMarkInRangeTextColor;
    }

    public void setTickMarkInRangeTextColor(@ColorInt int tickMarkInRangeTextColor) {
        this.mTickMarkInRangeTextColor = tickMarkInRangeTextColor;
    }

    public int getSeekBarMode() {
        return mSeekBarMode;
    }

    /**
     * {@link #SEEKBAR_MODE_SINGLE} is single SeekBar
     * {@link #SEEKBAR_MODE_RANGE} is range SeekBar
     * @param seekBarMode
     */
    public void setSeekBarMode(@SeekBarModeDef int seekBarMode) {
        this.mSeekBarMode = seekBarMode;
        mRightSB.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE);
    }

    public int getTickMarkMode() {
        return mTickMarkMode;
    }

    /**
     * {@link #TICK_MARK_GRAVITY_LEFT} is number tick mark, it will locate the position according to the value.
     * {@link #TICK_MARK_GRAVITY_RIGHT} is text tick mark, it will be equally positioned.
     * @param tickMarkMode
     */
    public void setTickMarkMode(@TickMarkModeDef int tickMarkMode) {
        this.mTickMarkMode = tickMarkMode;
    }

    public int getTickMarkTextMargin() {
        return mTickMarkTextMargin;
    }

    public void setTickMarkTextMargin(int tickMarkTextMargin) {
        this.mTickMarkTextMargin = tickMarkTextMargin;
    }

    public int getTickMarkTextSize() {
        return mTickMarkTextSize;
    }

    public void setTickMarkTextSize(int tickMarkTextSize) {
        this.mTickMarkTextSize = tickMarkTextSize;
    }

    public int getTickMarkGravity() {
        return mTickMarkGravity;
    }

    /**
     * the tick mark text gravity
     * {@link #TICK_MARK_GRAVITY_LEFT}
     * {@link #TICK_MARK_GRAVITY_RIGHT}
     * {@link #TICK_MARK_GRAVITY_CENTER}
     * @param tickMarkGravity
     */
    public void setTickMarkGravity(@TickMarkGravityDef int tickMarkGravity) {
        this.mTickMarkGravity = tickMarkGravity;
    }

    public CharSequence[] getTickMarkTextArray() {
        return mTickMarkTextArray;
    }

    public void setTickMarkTextArray(CharSequence[] tickMarkTextArray) {
        this.mTickMarkTextArray = tickMarkTextArray;
    }

    public float getMinInterval() {
        return mMinInterval;
    }

    public float getProgressRadius() {
        return mProgressRadius;
    }

    public void setProgressRadius(float progressRadius) {
        this.mProgressRadius = progressRadius;
    }

    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(@ColorInt int progressColor) {
        this.mProgressColor = progressColor;
    }

    public int getProgressDefaultColor() {
        return mProgressDefaultColor;
    }

    public void setProgressDefaultColor(@ColorInt int progressDefaultColor) {
        this.mProgressDefaultColor = progressDefaultColor;
    }

    public int getProgressDrawableId() {
        return mProgressDrawableId;
    }

    public void setProgressDrawableId(@DrawableRes int progressDrawableId) {
        this.mProgressDrawableId = progressDrawableId;
        mProgressBitmap = null;
        initProgressBitmap();
    }

    public int getProgressDefaultDrawableId() {
        return mProgressDefaultDrawableId;
    }

    public void setProgressDefaultDrawableId(@DrawableRes int progressDefaultDrawableId) {
        this.mProgressDefaultDrawableId = progressDefaultDrawableId;
        mProgressDefaultBitmap = null;
        initProgressBitmap();
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.mProgressWidth = progressWidth;
    }


    public void setTypeface(Typeface typeFace) {
        mPaint.setTypeface(typeFace);
    }

    public boolean isEnableThumbOverlap() {
        return mEnableThumbOverlap;
    }

    public void setEnableThumbOverlap(boolean enableThumbOverlap) {
        this.mEnableThumbOverlap = enableThumbOverlap;
    }

    public void setSteps(int steps) {
        this.mSteps = steps;
    }

    public int getSteps() {
        return mSteps;
    }

    public int getStepsColor() {
        return mStepsColor;
    }

    public void setStepsColor(@ColorInt int stepsColor) {
        this.mStepsColor = stepsColor;
    }

    public float getStepsWidth() {
        return mStepsWidth;
    }

    public void setStepsWidth(float stepsWidth) {
        this.mStepsWidth = stepsWidth;
    }

    public float getStepsHeight() {
        return mStepsHeight;
    }

    public void setStepsHeight(float stepsHeight) {
        this.mStepsHeight = stepsHeight;
    }

    public float getStepsRadius() {
        return mStepsRadius;
    }

    public void setStepsRadius(float stepsRadius) {
        this.mStepsRadius = stepsRadius;
    }

    public void setProgressTop(int progressTop) {
        this.mProgressTop = progressTop;
    }

    public void setProgressBottom(int progressBottom) {
        this.mProgressBottom = progressBottom;
    }

    public void setProgressLeft(int progressLeft) {
        this.mProgressLeft = progressLeft;
    }

    public void setProgressRight(int progressRight) {
        this.mProgressRight = progressRight;
    }

    public int getTickMarkLayoutGravity() {
        return mTickMarkLayoutGravity;
    }

    /**
     * the tick mark layout gravity
     * Gravity.TOP and Gravity.BOTTOM
     * @param tickMarkLayoutGravity
     */
    public void setTickMarkLayoutGravity(@TickMarkLayoutGravityDef int tickMarkLayoutGravity) {
        this.mTickMarkLayoutGravity = tickMarkLayoutGravity;
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * the RangeSeekBar gravity
     * Gravity.TOP and Gravity.BOTTOM
     * @param gravity
     */
    public void setGravity(@GravityDef int gravity) {
        this.mGravity = gravity;
    }

    public boolean isStepsAutoBonding() {
        return mStepsAutoBonding;
    }

    public void setStepsAutoBonding(boolean stepsAutoBonding) {
        this.mStepsAutoBonding = stepsAutoBonding;
    }

    public int getStepsDrawableId() {
        return mStepsDrawableId;
    }

    public void setStepsDrawableId(@DrawableRes int stepsDrawableId) {
        this.mStepsBitmaps.clear();
        this.mStepsDrawableId = stepsDrawableId;
        initStepsBitmap();
    }

    public List<Bitmap> getStepsBitmaps() {
        return mStepsBitmaps;
    }

    public void setStepsBitmaps(List<Bitmap> stepsBitmaps) {
        Log.i(TAG, "setStepsBitmaps() mapSize = " + stepsBitmaps.size());
        if (stepsBitmaps == null || stepsBitmaps.isEmpty() || stepsBitmaps.size() <= mSteps) {
            throw new IllegalArgumentException("stepsBitmaps must > steps !");
        }
        this.mStepsBitmaps.clear();
        this.mStepsBitmaps.addAll(stepsBitmaps);
    }

    public void setStepsDrawable(List<Integer> stepsDrawableIds) {
        Log.i(TAG, "setStepsDrawable() mapSize = " + stepsDrawableIds.size());
        if (stepsDrawableIds == null || stepsDrawableIds.isEmpty() || stepsDrawableIds.size() <= mSteps) {
            throw new IllegalArgumentException("stepsDrawableIds must > steps !");
        }
        if (!verifyStepsMode()) {
            throw new IllegalArgumentException("stepsWidth must > 0, stepsHeight must > 0,steps must > 0 First!!");
        }
        List<Bitmap> stepsBitmaps = new ArrayList<>();
        for (int i = 0; i < stepsDrawableIds.size(); i++) {
            stepsBitmaps.add(Utils.drawableToBitmap(getContext(), (int) mStepsWidth, (int) mStepsHeight, stepsDrawableIds.get(i)));
        }
        setStepsBitmaps(stepsBitmaps);
    }


}
