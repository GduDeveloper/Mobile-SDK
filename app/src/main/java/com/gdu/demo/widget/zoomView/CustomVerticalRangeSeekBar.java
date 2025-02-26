package com.gdu.demo.widget.zoomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.IntDef;

import com.gdu.demo.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomVerticalRangeSeekBar extends RangeSeekBar implements IVerticalGetOrientation{
    private final String TAG = "MyVerticalRangeSeekBar";

    /**
     * @hide
     */
    @IntDef({TEXT_DIRECTION_VERTICAL, TEXT_DIRECTION_HORIZONTAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextDirectionDef {
    }

    public final static int TEXT_DIRECTION_VERTICAL = 1;
    public final static int TEXT_DIRECTION_HORIZONTAL = 2;

    //direction of VerticalRangeSeekBar

    /**
     * @hide
     */
    @IntDef({DIRECTION_LEFT, DIRECTION_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionDef {
    }

    public final static int DIRECTION_LEFT = 1;
    public final static int DIRECTION_RIGHT = 2;

    /** 垂直拖动条绘制方向 */
    private int mOrientation = DIRECTION_LEFT;
    /** 刻度绘制方向 */
    private int mTickMarkDirection = TEXT_DIRECTION_VERTICAL;
    /** 最大刻度宽度 */
    private int mMaxTickMarkWidth;

    public CustomVerticalRangeSeekBar(Context context) {
        this(context, null);
    }

    public CustomVerticalRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initAttrs(AttributeSet attrs) {
        super.initAttrs(attrs);
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar);
            mOrientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_orientation, DIRECTION_LEFT);
            mTickMarkDirection = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_tick_mark_orientation, TEXT_DIRECTION_VERTICAL);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initSeekBar(AttributeSet attrs) {
        mLeftSB = new CustomVerticalSeekBar(this, attrs, this,true);
        mRightSB = new CustomVerticalSeekBar(this, attrs, this,false);
        mRightSB.setVisible(getSeekBarMode() != SEEKBAR_MODE_SINGLE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */
//        Log.i(TAG, "onMeasure() CustomVerticalRangeSeekBar widthMode = " + widthMode);
        if (widthMode == MeasureSpec.EXACTLY) {
            widthSize = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        } else if (widthMode == MeasureSpec.AT_MOST && getParent() instanceof ViewGroup
                && widthSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            widthSize = MeasureSpec.makeMeasureSpec(((ViewGroup) getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST);
        } else {
            int heightNeeded;
            if (getGravity() == Gravity.CENTER) {
                heightNeeded = 2 * getProgressTop() + getProgressHeight();
            } else {
                heightNeeded = (int) getRawHeight();
            }
            widthSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY);
        }
//        Log.i(TAG, "onMeasure() CustomVerticalRangeSeekBar widthSize = " + widthSize);
        super.onMeasure(widthSize, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOrientation == DIRECTION_LEFT) {
//            Log.i(TAG, "onDraw() getHeight = " + getHeight());
            canvas.rotate(-90);
//            Log.i(TAG, "onDraw() getHeight = -" + getHeight());
            canvas.translate(-getHeight(), 0);
        } else {
            canvas.rotate(90);
            canvas.translate(0, -getWidth());
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onDrawTickMark(Canvas canvas, Paint paint) {
        Log.i(TAG, "onDrawTickMark()");
        if (getTickMarkTextArray() != null) {
            int arrayLength = getTickMarkTextArray().length;
            int trickPartWidth = getProgressWidth() / (arrayLength - 1);
            for (int i = 0; i < arrayLength; i++) {
                final String text2Draw = getTickMarkTextArray()[i].toString();
                Log.i(TAG, "onDrawTickMark() text2Draw = " + text2Draw);
                if (TextUtils.isEmpty(text2Draw)) {
                    continue;
                }
                paint.getTextBounds(text2Draw, 0, text2Draw.length(), mTickMarkTextRect);
                paint.setColor(getTickMarkTextColor());
                //平分显示
                float x;
                if (getTickMarkMode() == TRICK_MARK_MODE_OTHER) {
                    if (getTickMarkGravity() == TICK_MARK_GRAVITY_RIGHT) {
                        x = getProgressLeft() + i * trickPartWidth - mTickMarkTextRect.width();
                    } else if (getTickMarkGravity() == TICK_MARK_GRAVITY_CENTER) {
                        x = getProgressLeft() + i * trickPartWidth - mTickMarkTextRect.width() / 2f;
                    } else {
                        x = getProgressLeft() + i * trickPartWidth;
                    }
                } else {
                    float num = Utils.parseFloat(text2Draw);
                    SeekBarState[] states = getRangeSeekBarState();
                    if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(num, states[1].value) != 1 && (getSeekBarMode() == SEEKBAR_MODE_RANGE)) {
                        paint.setColor(getTickMarkInRangeTextColor());
                    }
                    //按实际比例显示
                    x = getProgressLeft() + getProgressWidth() * (num - getMinProgress()) / (getMaxProgress() - getMinProgress())
                            - mTickMarkTextRect.width() / 2f;
                }
                float y;
                if (getTickMarkLayoutGravity() == Gravity.TOP) {
                    y = getProgressTop() - getTickMarkTextMargin();
                } else {
                    y = getProgressBottom() + getTickMarkTextMargin() + mTickMarkTextRect.height();
                }
                int degrees = 0;
                float rotateX = (x + mTickMarkTextRect.width() / 2f);
                float rotateY = (y - mTickMarkTextRect.height() / 2f);
                if (mTickMarkDirection == TEXT_DIRECTION_VERTICAL) {
                    if (mOrientation == DIRECTION_LEFT) {
                        degrees = 90;
                    } else if (mOrientation == DIRECTION_RIGHT) {
                        degrees = -90;
                    }
                }
                if (degrees != 0) {
                    canvas.rotate(degrees, rotateX, rotateY);
                }
                paint.setFakeBoldText(true);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(text2Draw, x, y, paint);
                if (degrees != 0) {
                    canvas.rotate(-degrees, rotateX, rotateY);
                }
            }
        }

    }


    @Override
    protected int getTickMarkRawHeight() {
        Log.i(TAG, "getTickMarkRawHeight()");
        int height = 0;
        if (mMaxTickMarkWidth > 0) {
            height  = getTickMarkTextMargin() + mMaxTickMarkWidth;
        } else {
            if (getTickMarkTextArray() != null && getTickMarkTextArray().length > 0) {
                int arrayLength = getTickMarkTextArray().length;
                mMaxTickMarkWidth = Utils.measureText(String.valueOf(getTickMarkTextArray()[0]), getTickMarkTextSize()).width();
                for (int i = 1; i < arrayLength; i++) {
                    int width = Utils.measureText(String.valueOf(getTickMarkTextArray()[i]), getTickMarkTextSize()).width();
                    if (mMaxTickMarkWidth < width) {
                        mMaxTickMarkWidth = width;
                    }
                }
                height = getTickMarkTextMargin() + mMaxTickMarkWidth;
            }
        }
        Log.i(TAG, "getTickMarkRawHeight() height = " + height);
        return height;
    }

    @Override
    public void setTickMarkTextSize(int tickMarkTextSize) {
        super.setTickMarkTextSize(tickMarkTextSize);
        mMaxTickMarkWidth = 0;
    }

    @Override
    public void setTickMarkTextArray(CharSequence[] tickMarkTextArray) {
        super.setTickMarkTextArray(tickMarkTextArray);
        mMaxTickMarkWidth = 0;
    }

    @Override
    protected float getEventX(MotionEvent event) {
        if (mOrientation == DIRECTION_LEFT) {
            return getHeight() - event.getY();
        } else {
            return event.getY();
        }
    }

    @Override
    protected float getEventY(MotionEvent event) {
        if (mOrientation == DIRECTION_LEFT) {
            return event.getX();
        } else {
            return -event.getX() + getWidth();
        }
    }

    /**
     * if is single mode, please use it to get the SeekBar
     *
     * @return left seek bar
     */
    @Override
    public CustomVerticalSeekBar getLeftSeekBar() {
        return (CustomVerticalSeekBar) mLeftSB;
    }

    @Override
    public CustomVerticalSeekBar getRightSeekBar() {
        return (CustomVerticalSeekBar) mRightSB;
    }

    @Override
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * set VerticalRangeSeekBar Orientation
     * {@link #DIRECTION_LEFT}
     * {@link #DIRECTION_RIGHT}
     * @param orientation
     */
    public void setOrientation(@DirectionDef int orientation) {
        this.mOrientation = orientation;
    }

    public int getTickMarkDirection() {
        return mTickMarkDirection;
    }

    /**
     * set tick mark text direction
     * {@link #TEXT_DIRECTION_VERTICAL}
     * {@link #TEXT_DIRECTION_HORIZONTAL}
     * @param tickMarkDirection
     */
    public void setTickMarkDirection(@TextDirectionDef int tickMarkDirection) {
        this.mTickMarkDirection = tickMarkDirection;
    }
}
