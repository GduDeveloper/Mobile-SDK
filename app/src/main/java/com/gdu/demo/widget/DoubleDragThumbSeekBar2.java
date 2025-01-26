package com.gdu.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.gdu.demo.R;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.util.ViewUtils;

import java.math.BigDecimal;

/**
 * 刷滑块进度拖动条
 */
public class DoubleDragThumbSeekBar2 extends View {
//    private static final String TAG = "DoubleDragThumbSeekBar";

    /** 点击在前滑块上 */
    private static final int CLICK_ON_LOW = 1;
    /** 点击在后滑块上 */
    private static final int CLICK_ON_HIGH = 2;
    /** 点击在前滑块区域(含滑动后进度条区域) */
    private static final int CLICK_IN_LOW_AREA = 3;
    /** 点击在后滑块区域(含滑动后进度条区域) */
    private static final int CLICK_IN_HIGH_AREA = 4;
    /** 点击在外部区域(非滑动过后区域) */
    private static final int CLICK_OUT_AREA = 5;
    /** 点击在无效区域 */
    private static final int CLICK_INVAILD = 0;

    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
            android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };
    /** 滑动条控件底层颜色 */
    private final int mBaseBgColor;
    /** 滑动条控件第一颜色 */
    private final int mOneBgColor;
    /** 滑动条控件第二颜色 */
    private final int mTwoBgColor;

    /** 前滑块 */
    private final Drawable mThumbLow;
    /** 后滑块 */
    private final Drawable mThumbHigh;
    /** 控件宽度=滑动条宽度+滑动块宽度 */
    private int mScrollBarWidth;
    /** 控件高度=滑动块高度+底部刻度条高度+滑块与刻度条直接的间距+上下Padding距离 */
    private int mViewHeight;
    /** 滑动条高度 */
    private final int mScrollBarHeight = 6;
    /** 滑动块宽度 */
    private final int mThumbWidth;
    /** 滑动块高度 */
    private final int mThumbHeight;
    /** 前滑块中心坐标 */
    private double mOffsetLow = 0;
    /** 后滑块中心坐标 */
    private double mOffsetHigh = 0;
    /** 总刻度是固定距离 两边各去掉半个滑块距离 */
    private int mDistance = 0;
    /** 滑动块底部刻度条高度 */
    private int mThumbMarginBot;
    /** 当前点击状态 */
    private int mFlag = CLICK_INVAILD;
    private OnSeekBarChangeListener mBarChangeListener;

    /** 默认前滑块位置百分比(默认值10) */
    private double curLowProgress = 0;
    /** 默认后滑块位置百分比(默认值20) */
    private double curHeightProgress = 0;
    /** 是否是手动设置值 */
    private boolean isEdit = false;

    /** 两个滑块间最小百分比间距(当前为5) */
    private double minMarginPercent = 5;
    /** 换算后间距View的宽度 */
    private double minMarginViewWidth = 0;

    /** 前滑块最小设置值(百分比) */
    private double lowPbMinSetValue = 10;
    /** 前滑块最大设置值(百分比) */
    private double lowPbMaxSetValue = 45;
    /** 后滑块最小设置值(百分比) */
    private double heightPbMinSetValue = 10;
    /** 后滑块最大设置值(百分比) */
    private double heightPbMaxSetValue = 50;

    /** 前滑块最小值(像素) */
    private double lowPbMinValue = 0;
    /** 前滑块最大值(像素) */
    private double lowPbMaxValue = 0;
    /** 后滑块最小值(像素) */
    private double heightPbMinValue = 0;
    /** 后滑块最大值(像素) */
    private double heightPbMaxValue = 0;
    /** 每个步长对应的实际距离 */
    private double stepDistance = 1;

//    private Paint mTextPaint;
    private Paint mScalePaint;
    private Paint mBgPaint;

    /** 是否使用单位 */
    private boolean isObstacleSet = false;
    /** 最大进度值 */
    private int maxPb = 100;
    /** 底层背景进度矩形 */
    private final RectF mBaseBgRf;
    /** 第一层背景进度矩形 */
    private final RectF mHasScrollBarSecondBg;
    /** 第二层背景进度矩形 */
    private final RectF mOnePbBgRf;

    private String lowValueStr = "";
    private String heightValueStr = "";

    /** 是否显示刻度 */
    private boolean isShowScale;
    /** 显示刻度高度(单位dp) */
    private int mScaleHeight = 3;
    /** 刻度颜色值 */
    private int mScaleColor = R.color.color_8A8A8A;

    public DoubleDragThumbSeekBar2(Context context) {
        this(context, null);
    }

    public DoubleDragThumbSeekBar2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleDragThumbSeekBar2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleDragThumbSeekBar);
        mThumbLow = ContextCompat.getDrawable(getContext(), mTypedArray.getResourceId(R.styleable.DoubleDragThumbSeekBar_ddt_oneThumb,
                R.drawable.seek_bar_botton));

        mThumbHigh = ContextCompat.getDrawable(getContext(), mTypedArray.getResourceId(R.styleable.DoubleDragThumbSeekBar_ddt_twoThumb,
                R.drawable.seek_bar_botton));

        mBaseBgColor = mTypedArray.getColor(R.styleable.DoubleDragThumbSeekBar_ddt_basePbColor,
                ContextCompat.getColor(getContext(), R.color.color_CFCFCF));

        mOneBgColor = mTypedArray.getColor(R.styleable.DoubleDragThumbSeekBar_ddt_onePbColor,
                ContextCompat.getColor(getContext(), R.color.color_FCB341));

        mTwoBgColor = mTypedArray.getColor(R.styleable.DoubleDragThumbSeekBar_ddt_twoPbColor,
                ContextCompat.getColor(getContext(), R.color.color_ED4F2E));

        mTypedArray.recycle();

        mThumbLow.setState(STATE_NORMAL);
        mThumbHigh.setState(STATE_NORMAL);

        mThumbWidth = mThumbLow.getIntrinsicWidth();
        mThumbHeight = mThumbLow.getIntrinsicHeight();

        mBaseBgRf = new RectF();
        mHasScrollBarSecondBg = new RectF();
        mOnePbBgRf = new RectF();

        initPant();
    }

    private void initPant() {
//        mTextPaint = new Paint();
//        mTextPaint.setTextAlign(Paint.Align.CENTER);
//        mTextPaint.setColor(Color.RED);
//        mTextPaint.setTextSize(getContext().getResources().getDimension(R.dimen.dp_11));
//        mTextPaint.setFakeBoldText(true);

        mScalePaint = new Paint();
        mScalePaint.setColor(ContextCompat.getColor(getContext(), mScaleColor));
        mScalePaint.setAntiAlias(true);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setTextAlign(Paint.Align.CENTER);
        mBgPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_999999));
    }

    //默认执行，计算view的宽高,在onDraw()之前
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = measureWidth(widthMeasureSpec);
        mScrollBarWidth = width;
        final int halfThumbWidth = mThumbWidth / 2;
//        mOffsetLow = halfThumbWidth;
//        mOffsetHigh = width - halfThumbWidth;
        mDistance = width - mThumbWidth;
        stepDistance = formatDouble(mDistance / (double)maxPb);

//        minMarginViewWidth = formatDouble(minMarginPercent / stepDistance);
        minMarginViewWidth = formatDouble(minMarginPercent * stepDistance);

        lowPbMinValue = formatDouble((lowPbMinSetValue * stepDistance) + halfThumbWidth);
        lowPbMaxValue = formatDouble((lowPbMaxSetValue * stepDistance) + halfThumbWidth);
        heightPbMinValue = formatDouble((heightPbMinSetValue * stepDistance) + halfThumbWidth);
        heightPbMaxValue = formatDouble((heightPbMaxSetValue * stepDistance) + halfThumbWidth);

//        final Paint.FontMetrics mFontMetrics = mTextPaint.getFontMetrics();
//        mThumbMarginBot = (int) (mFontMetrics.bottom - mFontMetrics.top);
        if (isShowScale) {
            mThumbMarginBot = ViewUtils.dip2px(getContext(), mScaleHeight);
        } else {
            mThumbMarginBot = 0;
        }

        mOffsetLow = formatDouble((curLowProgress * stepDistance) + halfThumbWidth);
        mOffsetHigh = formatDouble((curHeightProgress * stepDistance) + halfThumbWidth);
//        MyLogUtils.i("onMeasure() mScrollBarWidth = " + mScrollBarWidth
//                + "; halfThumbWidth = " + halfThumbWidth
//                + "; maxPb = " + maxPb
//                + "; mDistance = " + mDistance
//                + "; minMarginViewWidth = " + minMarginViewWidth
//                + "; lowPbMinValue = " + lowPbMinValue
//                + "; lowPbMaxValue = " + lowPbMaxValue
//                + "; heightPbMinValue = " + heightPbMinValue
//                + "; heightPbMaxValue = " + heightPbMaxValue
//                + "; stepDistance = " + stepDistance
//                + "; mThumbMarginTop = " + mThumbMarginTop
//                + "; curLowProgress = " + curLowProgress
//                + "; curHeightProgress = " + curHeightProgress
//                + "; mOffsetLow = " + mOffsetLow
//                + "; mOffsetHigh = " + mOffsetHigh);
        int spaceHeight = ViewUtils.dip2px(getContext(), 1);
        if (isShowScale) {
            mViewHeight = mThumbHeight + mThumbMarginBot + spaceHeight + getPaddingTop() + getPaddingBottom();
        } else {
            mViewHeight = mThumbHeight + spaceHeight + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, mViewHeight);
    }

    private int measureWidth(int measureSpec) {
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            //wrap_content
            case MeasureSpec.AT_MOST:
                break;

            //fill_parent或者精确值
            case MeasureSpec.EXACTLY:
                break;

            default:
                break;
        }

        return specSize;
    }

    private void initOrUpdateBgRect(Canvas canvas) {
        final int halfThumbWidth = mThumbWidth / 2;
        final int halfHeight = mViewHeight / 2;
        // 进度条背景顶部起始位置
        final int topLocation = halfHeight - mScrollBarHeight / 2;
        // 进度条背景底部结束位置
        final int bottomLocation = topLocation + mScrollBarHeight;

        if (mBaseBgRf != null) {
            mBaseBgRf.left = halfThumbWidth;
            mBaseBgRf.top = topLocation;
            mBaseBgRf.right = mScrollBarWidth - halfThumbWidth;
            mBaseBgRf.bottom = bottomLocation;
        }

        if (mHasScrollBarSecondBg != null) {
            mHasScrollBarSecondBg.left = halfThumbWidth;
            mHasScrollBarSecondBg.top = topLocation;
            mHasScrollBarSecondBg.right = (int) mOffsetHigh;
            mHasScrollBarSecondBg.bottom = bottomLocation;
        }

        if (mOnePbBgRf != null) {
            mOnePbBgRf.left = halfThumbWidth;
            mOnePbBgRf.top = topLocation;
            mOnePbBgRf.right = (int) mOffsetLow;
            mOnePbBgRf.bottom = bottomLocation;
        }
        mBgPaint.setColor(mBaseBgColor);
        // 灰色，不会动
        if (mBaseBgRf != null) {
            canvas.drawRoundRect(mBaseBgRf, ViewUtils.dip2px(getContext(),5),
                    ViewUtils.dip2px(getContext(), 5), mBgPaint);
        }

        mBgPaint.setColor(mOneBgColor);
        if (mHasScrollBarSecondBg != null) {
            canvas.drawRoundRect(mHasScrollBarSecondBg, ViewUtils.dip2px(getContext(),5),
                    ViewUtils.dip2px(getContext(),5), mBgPaint);
        }

        mBgPaint.setColor(mTwoBgColor);
        if (mOnePbBgRf != null) {
            canvas.drawRoundRect(mOnePbBgRf, ViewUtils.dip2px(getContext(),5),
                    ViewUtils.dip2px(getContext(),5), mBgPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initOrUpdateBgRect(canvas);
        int oneDpToPixValue = ViewUtils.dip2px(getContext(), 1);
        final int halfThumbWidth = mThumbWidth / 2;
        final int halfThumbHeight = mThumbHeight / 2;
        final int halfViewHeight = mViewHeight / 2;
        // 除去滑块高度后的其他高度
        int halfRenameViewHeight = (mViewHeight - mThumbHeight) / 2;
        final int thumbBot = halfViewHeight + halfThumbHeight - halfRenameViewHeight;
        // 第一滑块
        mThumbLow.setBounds((int) (mOffsetLow - halfThumbWidth), 0,
                (int) (mOffsetLow + halfThumbWidth), thumbBot);
        mThumbLow.draw(canvas);

        // 第二滑块
        int highLeft = (int)(mOffsetHigh - halfThumbWidth);
        int highRight = (int)(mOffsetHigh + halfThumbWidth);
        if (highRight > mScrollBarWidth) {
            highLeft = mScrollBarWidth - mThumbWidth;
            highRight = mScrollBarWidth;
        }
        mThumbHigh.setBounds(highLeft, 0, highRight, thumbBot);
        mThumbHigh.draw(canvas);
//        curLowProgress = formatDouble((mOffsetLow - halfThumbWidth) * maxPb / mDistance);
//        curHeightProgress = formatDouble((mOffsetHigh - halfThumbWidth) * maxPb / mDistance);
        curLowProgress = formatDouble((mOffsetLow - halfThumbWidth)  / stepDistance);
        curHeightProgress = formatDouble((mOffsetHigh - halfThumbWidth) / stepDistance);
//        MyLogUtils.i("onDraw() curLowProgress = " + curLowProgress
//                + "; curHeightProgress = " + curHeightProgress
//                + "; mOffsetLow = " + mOffsetLow
//                + "; mOffsetHigh = " + mOffsetHigh
//                + "; stepDistance = " + stepDistance);
        // 判断数值边界超出显示
        if (curHeightProgress > maxPb) {
            curHeightProgress = maxPb;
        }
        if (isObstacleSet) {
            lowValueStr = UnitChnageUtils.getDecimalFormatUnit((float) formatDouble(curLowProgress / 100));
            heightValueStr = UnitChnageUtils.getDecimalFormatUnit((float) formatDouble(curHeightProgress / 100));
        } else {
            lowValueStr = (int) curLowProgress + "";
            heightValueStr = (int) curHeightProgress + "";
        }
        if (isShowScale) {
            int scaleTop = thumbBot + oneDpToPixValue;
            int scaleBot = scaleTop + mThumbMarginBot;
//            MyLogUtils.i("onDraw() halfRenameViewHeight = " + halfRenameViewHeight + "; scaleTop = " + scaleTop + "; " +
//                    "scaleBot = " + scaleBot);
            for (int i = 1; i < maxPb / 10; i++) {
                int scale = (int) (stepDistance * (i * 10)) + halfThumbWidth;
                Rect mRect = new Rect(scale - oneDpToPixValue / 2, scaleTop, scale + oneDpToPixValue / 2, scaleBot);
                canvas.drawRect(mRect, mScalePaint);
            }
        }

        if (mBarChangeListener != null && !isEdit) {
            mBarChangeListener.onValueUpdate(lowValueStr, heightValueStr);
            mBarChangeListener.onProgressChanged(this, curLowProgress, curHeightProgress);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) {
            return false;
        }
        //按下
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (mBarChangeListener != null) {
                    mBarChangeListener.onProgressBefore();
                    isEdit = false;
                }
                mFlag = getAreaFlag(e);
                switch (mFlag) {
                    case CLICK_ON_LOW:
                        mThumbLow.setState(STATE_PRESSED);
                        break;

                    case CLICK_ON_HIGH:
                        mThumbHigh.setState(STATE_PRESSED);
                        break;

                    case CLICK_IN_LOW_AREA:
                        mThumbLow.setState(STATE_PRESSED);
                        mOffsetLow = formatDouble(e.getX());
                        lowValueOutOfBoundsJudge();
                        break;

                    case CLICK_IN_HIGH_AREA:
                        mThumbHigh.setState(STATE_PRESSED);
                        mOffsetHigh = formatDouble(e.getX());
                        heightValueOutOfBoundsJudge();
                        break;

                    default:
                        break;
                }
                //设置进度条
                invalidate();
                //移动move
                break;

            case MotionEvent.ACTION_MOVE:
                if (mFlag == CLICK_ON_LOW) {
                    mOffsetLow = formatDouble(e.getX());
                    lowValueOutOfBoundsJudge();
                } else if (mFlag == CLICK_ON_HIGH) {
                    mOffsetHigh = formatDouble(e.getX());
                    heightValueOutOfBoundsJudge();
                } else if (mFlag == CLICK_IN_LOW_AREA) {
                    mOffsetLow = formatDouble(e.getX());
                    lowValueOutOfBoundsJudge();
                } else if (mFlag == CLICK_IN_HIGH_AREA) {
                    mOffsetHigh = formatDouble(e.getX());
                    heightValueOutOfBoundsJudge();
                }
                //设置进度条
                invalidate();
                //抬起
                break;

            case MotionEvent.ACTION_UP:
//            Log.d("ACTION_UP", "------------------");
                mThumbLow.setState(STATE_NORMAL);
                mThumbHigh.setState(STATE_NORMAL);

                if (mBarChangeListener != null) {
                    mBarChangeListener.onProgressAfter();
                    mBarChangeListener.onValueUpdate(lowValueStr, heightValueStr);
                }
                getParent().requestDisallowInterceptTouchEvent(false);
                //这两个for循环 是用来自动对齐刻度的，注释后，就可以自由滑动到任意位置
//            for (int i = 0; i < money.length; i++) {
//                 if(Math.abs(mOffsetLow-i* ((mScollBarWidth-mThumbWidth)/ (money.length-1)))<=
//                 (mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//                     mprogressLow=i;
//                     mOffsetLow =i* ((mScollBarWidth-mThumbWidth)/(money.length-1));
//                     invalidate();
//                     break;
//                }
//            }
//
//            for (int i = 0; i < money.length; i++) {
//                  if(Math.abs(mOffsetHigh-i* ((mScollBarWidth-mThumbWidth)/(money.length-1) ))<(mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//                      mprogressHigh=i;
//                       mOffsetHigh =i* ((mScollBarWidth-mThumbWidth)/(money.length-1));
//                       invalidate();
//                       break;
//                }
//            }
                break;

            default:
                break;
        }
        return true;
    }

    private void heightValueOutOfBoundsJudge() {
//        MyLogUtils.i("heightValueOutOfBoundsJudge() minMarginViewWidth = " + minMarginViewWidth
//                + "; heightPbMinValue = " + heightPbMinValue
//                + "; heightPbMaxValue = " + heightPbMaxValue);
        final int halfThumbWidth = mThumbWidth >> 1;
        if (mOffsetHigh - (mOffsetLow + minMarginViewWidth) <= 0) {
            mOffsetHigh = mOffsetLow + minMarginViewWidth;
        }
        if (mOffsetHigh < heightPbMinValue) {
            mOffsetHigh = heightPbMinValue;
        } else if (mOffsetHigh > heightPbMaxValue) {
            mOffsetHigh = heightPbMaxValue;
        } else if (mOffsetHigh < halfThumbWidth) {
            mOffsetHigh = halfThumbWidth;
            mOffsetLow = halfThumbWidth;
        } else if (mOffsetHigh > mScrollBarWidth - halfThumbWidth) {
            mOffsetHigh = halfThumbWidth + mDistance;
        }
//        MyLogUtils.i("lowValueOutOfBoundsJudge() mOffsetHigh = " + mOffsetHigh);
    }

    private void lowValueOutOfBoundsJudge() {
//        MyLogUtils.i("lowValueOutOfBoundsJudge() minMarginViewWidth = " + minMarginViewWidth
//                + "; mOffsetHigh = " + mOffsetHigh
//                + "; mOffsetLow = " + mOffsetLow
//                + "; lowPbMinValue = " + lowPbMinValue
//                + "; lowPbMaxValue = " + lowPbMaxValue);
        final int halfThumbWidth = mThumbWidth / 2;
        if ((mOffsetHigh - minMarginViewWidth) - mOffsetLow <= 0) {
            mOffsetLow = mOffsetHigh - minMarginViewWidth;
        }
        if (mOffsetLow < lowPbMinValue) {
            mOffsetLow = lowPbMinValue;
        } else if (mOffsetLow > lowPbMaxValue) {
            mOffsetLow = lowPbMaxValue;
        } else if (mOffsetLow < 0 || mOffsetLow <= halfThumbWidth) {
            mOffsetLow = halfThumbWidth;
        } else if (mOffsetLow >= mScrollBarWidth - halfThumbWidth) {
            mOffsetLow = halfThumbWidth + mDistance;
            mOffsetHigh = mOffsetLow;
        }
//        MyLogUtils.i("lowValueOutOfBoundsJudge() mOffsetLow = " + mOffsetLow);
    }

    public int getAreaFlag(MotionEvent e) {
        final int halfThumbWidth = mThumbWidth / 2;
        int top = mThumbMarginBot;
        int bottom = mThumbHeight + mThumbMarginBot;
        final boolean isClickLow = e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetLow - halfThumbWidth)
                && e.getX() <= mOffsetLow + halfThumbWidth;
        if (isClickLow) {
            return CLICK_ON_LOW;
        }

        final boolean isClickHeight = e.getY() >= top && e.getY() <= bottom && e.getX() >= (mOffsetHigh - halfThumbWidth)
                && e.getX() <= (mOffsetHigh + halfThumbWidth);
        if (isClickHeight) {
            return CLICK_ON_HIGH;
        }

        final boolean isClickLowArea = e.getY() >= top && e.getY() <= bottom && ((e.getX() >= 0
                && e.getX() < (mOffsetLow - halfThumbWidth)) || (e.getX() > (mOffsetLow + halfThumbWidth)
                && e.getX() <= (mOffsetHigh + mOffsetLow) / 2));
        if (isClickLowArea) {
            return CLICK_IN_LOW_AREA;
        }

        final boolean isClickHeightArea = e.getY() >= top && e.getY() <= bottom && ((e.getX() >
                (mOffsetHigh + mOffsetLow) / 2 && e.getX() < (mOffsetHigh - halfThumbWidth))
                || (e.getX() > (mOffsetHigh + halfThumbWidth) && e.getX() <= mScrollBarWidth));
        if (isClickHeightArea) {
            return CLICK_IN_HIGH_AREA;
        }

        final boolean isClickOutArea =
                !(e.getX() >= 0 && e.getX() <= mScrollBarWidth && e.getY() >= top && e.getY() <= bottom);
        if (isClickOutArea) {
            return CLICK_OUT_AREA;
        } else {
            return CLICK_INVAILD;
        }
    }

    /**
     * 格式化触控坐标
     * @param value 触控坐标点
     */
    private double formatTouchCoordinate(double value) {
        final int halfThumbWidth = mThumbWidth / 2;
        int tmpStep = (int) ((value - halfThumbWidth) / stepDistance);
        return formatDouble(tmpStep * stepDistance + mThumbWidth / 2);
    }

    /**
     * 设置前滑块的值
     * @param progressLow
     */
    public void setProgressLow(double progressLow) {
        if (mThumbLow.getState() == STATE_PRESSED || mThumbHigh.getState() == STATE_PRESSED) {
            //如果当前正处于触控滑动，则屏蔽手动设置值
            return;
        }
        if (progressLow < lowPbMinSetValue) {
            this.curLowProgress = lowPbMinSetValue;
        } else if(progressLow > lowPbMaxSetValue) {
            this.curLowProgress = lowPbMaxSetValue;
        } else {
            this.curLowProgress = progressLow;
        }
        mOffsetLow = formatDouble(curLowProgress * stepDistance + (mThumbWidth >> 1)) ;
        isEdit = true;
        invalidate();
    }

    /**
     * 设置后滑块的值
     * @param progressHigh
     */
    public void setProgressHigh(double progressHigh) {
        if (mThumbLow.getState() == STATE_PRESSED || mThumbHigh.getState() == STATE_PRESSED) {
            //如果当前正处于触控滑动，则屏蔽手动设置值
            return;
        }
        if (progressHigh < heightPbMinSetValue) {
            this.curHeightProgress = heightPbMinSetValue;
        } else if(progressHigh > heightPbMaxSetValue) {
            this.curHeightProgress = heightPbMaxSetValue;
        } else {
            this.curHeightProgress = progressHigh;
        }
        mOffsetHigh = formatDouble(curHeightProgress * stepDistance + (mThumbWidth >> 1)) ;
        isEdit = true;
        invalidate();
    }

    public void setLowAndHeightProgress(double progressLow, double progressHigh) {
//        MyLogUtils.i("setLowAndHeightProgress() progressLow = " + progressLow
//                + "; progressHigh = " + progressHigh);
        if (mThumbLow.getState() == STATE_PRESSED || mThumbHigh.getState() == STATE_PRESSED) {
            //如果当前正处于触控滑动，则屏蔽手动设置值
            return;
        }
        if (progressLow < lowPbMinSetValue) {
            this.curLowProgress = lowPbMinSetValue;
        } else if(progressLow > lowPbMaxSetValue) {
            this.curLowProgress = lowPbMaxSetValue;
        } else {
            this.curLowProgress = progressLow;
        }
        mOffsetLow = formatDouble(curLowProgress * stepDistance + (mThumbWidth >> 1)) ;
//        MyLogUtils.i("setLowAndHeightProgress() mOffsetLow = " + mOffsetLow);
        if (progressHigh < heightPbMinSetValue) {
            this.curHeightProgress = heightPbMinSetValue;
        } else if(progressHigh > heightPbMaxSetValue) {
            this.curHeightProgress = heightPbMaxSetValue;
        } else {
            this.curHeightProgress = progressHigh;
        }
        mOffsetHigh = formatDouble(curHeightProgress * stepDistance + (mThumbWidth >> 1)) ;
        isEdit = true;
        invalidate();
        if (mBarChangeListener == null) {
            return;
        }
        if (isObstacleSet) {
            lowValueStr = UnitChnageUtils.getDecimalFormatUnit((float) formatDouble(curLowProgress / 100));
            heightValueStr = UnitChnageUtils.getDecimalFormatUnit((float) formatDouble(curHeightProgress / 100));
        } else {
            lowValueStr = (int) curLowProgress + "";
            heightValueStr = (int) curHeightProgress + "";
        }
        mBarChangeListener.onValueUpdate(lowValueStr, heightValueStr);
    }

    /**
     * 设置前滑块的最大、最小值
     * @param lowMin
     * @param lowMax
     */
    public void setLowMinMaxValue(double lowMin, double lowMax) {
        this.lowPbMinSetValue = lowMin;
        this.lowPbMaxSetValue = lowMax;
        isEdit = true;
    }

    /**
     * 设置后滑块的最大、最小值
     * @param heightMin
     * @param heightMax
     */
    public void setHeightMinMaxValue(double heightMin, double heightMax) {
        this.heightPbMinSetValue = heightMin;
        this.heightPbMaxSetValue = heightMax;
        isEdit = true;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mBarChangeListener = mListener;
    }

    /**
     * 回调函数，在滑动时实时调用，改变输入框的值
     */
    public interface OnSeekBarChangeListener {
        /**
         * 滑动前
         */
        void onProgressBefore();

        /**
         * 滑动时(不要在滑动时进行UI操作否则会导致严重卡顿)
         * @param seekBar
         * @param progressLow
         * @param progressHigh
         */
        void onProgressChanged(DoubleDragThumbSeekBar2 seekBar, double progressLow,
                               double progressHigh);

        /**
         * 滑块上值更新
         * @param lowStr
         * @param heightStr
         */
        void onValueUpdate(String lowStr, String heightStr);

        /**
         * 滑动后
         */
        void onProgressAfter();
    }

//    private int formatInt(double value) {
//        BigDecimal bd = new BigDecimal(value);
//        BigDecimal bd1 = bd.setScale(0, BigDecimal.ROUND_HALF_UP);
//        return bd1.intValue();
//    }

    private double formatDouble(double pDouble) {
        BigDecimal bd = new BigDecimal(pDouble);
        BigDecimal bd1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        pDouble = bd1.doubleValue();
        return pDouble;
    }

    public void setMinMarginPercent(double minMarginPercent) {
        this.minMarginPercent = minMarginPercent;
    }

    public void setMaxPb(int maxPb) {
        this.maxPb = maxPb;
    }

    public void setObstacleSet(boolean obstacleSet) {
        isObstacleSet = obstacleSet;
    }

    public void setShowScale(boolean mShowScale) {
        isShowScale = mShowScale;
    }

    public void setScaleHeight(int mScaleHeight) {
        this.mScaleHeight = mScaleHeight;
    }

    public void setScaleColor(int mScaleColor) {
        this.mScaleColor = mScaleColor;
    }

    public double getCurLowProgress() {
        //和最大值比较，避免超出
        int curValue = Double.valueOf(curLowProgress).intValue();
        int setValue = Double.valueOf(lowPbMaxSetValue).intValue();
        if(curValue > setValue){
            curLowProgress = lowPbMaxSetValue;
        }
        return curLowProgress;
    }

    public double getCurHeightProgress() {
        return curHeightProgress;
    }

    public String getLowValueStr() {
        return lowValueStr;
    }

    public String getHeightValueStr() {
        return heightValueStr;
    }
}
