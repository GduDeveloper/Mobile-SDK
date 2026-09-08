package com.gdu.demo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.gdu.config.GlobalVariable;
import com.gdu.util.RectUtil;

import java.util.List;

/**
 * 红外测温叠加层：在视频画面上实时绘制测温点和测温区域框，并支持触摸选取测温点/区域。
 * <p>
 * 坐标转换统一使用 {@link RectUtil}：
 * - 显示：协议坐标 → 屏幕坐标，用 {@link RectUtil#videoPoint2ScreenArg(int, int, int, int, int)}
 * - 选取：屏幕坐标 → 协议坐标，用 {@link RectUtil#screenPoint2VideoArg(int, int, int, int)}
 */
public class IRTempOverlayView extends View {

    /**
     * 测温点画笔
     */
    private final Paint mSpotPaint;

    /**
     * 测温点温度文字画笔
     */
    private final Paint mSpotTextPaint;

    /**
     * 测温框画笔
     */
    private final Paint mAreaPaint;

    /**
     * 测温框温度文字画笔
     */
    private final Paint mAreaTextPaint;

    /**
     * 最高温点画笔
     */
    private final Paint mHighTempPaint;

    /**
     * 最低温点画笔
     */
    private final Paint mLowTempPaint;

    /**
     * 拖动选取时的预览框画笔
     */
    private final Paint mSelectPaint;

    /**
     * 触摸拖动阈值（像素），小于该值视为点击选取测温点，否则为拖动选取区域
     */
    private static final float TOUCH_SLOP = 20f;

    /**
     * 光标测温点（协议坐标，-1 表示未设置）
     */
    private int mSpotX = -1;

    private int mSpotY = -1;

    private float mSpotTemp = 0f;

    /**
     * 测温区域（协议坐标）：中心点 + 宽高
     */
    private int mAreaCenterX = -1;

    private int mAreaCenterY = -1;

    private int mAreaWidth = 0;

    private int mAreaHeight = 0;

    private float mAreaAvgTemp = 0f;

    /**
     * 最高温点（协议坐标）
     */
    private int mHighestTempX = -1;

    private int mHighestTempY = -1;

    private float mHighestTemp = 0f;

    /**
     * 最低温点（协议坐标）
     */
    private int mLowestTempX = -1;

    private int mLowestTempY = -1;

    private float mLowestTemp = 0f;

    /**
     * 触摸按下时的屏幕坐标
     */
    private float mDownX;

    private float mDownY;

    /**
     * 拖动选取预览框（屏幕坐标）
     */
    private RectF mSelectRect;

    /**
     * 触摸选取监听
     */
    private OnTempSelectListener mOnTempSelectListener;

    public interface OnTempSelectListener {

        /**
         * 点击选取测温点
         *
         * @param protoX 协议坐标X
         * @param protoY 协议坐标Y
         */
        void onPointSelected(int protoX, int protoY);

        /**
         * 拖动选取测温区域
         *
         * @param centerX 协议坐标中心X
         * @param centerY 协议坐标中心Y
         * @param width   协议坐标宽度
         * @param height  协议坐标高度
         */
        void onAreaSelected(int centerX, int centerY, int width, int height);
    }

    public IRTempOverlayView(Context context) {
        this(context, null);
    }

    public IRTempOverlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IRTempOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSpotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSpotPaint.setColor(0xFFFF4E00);
        mSpotPaint.setStyle(Paint.Style.STROKE);
        mSpotPaint.setStrokeWidth(3);

        mSpotTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSpotTextPaint.setColor(0xFFFFFFFF);
        mSpotTextPaint.setTextSize(28);
        mSpotTextPaint.setFakeBoldText(true);

        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaPaint.setColor(0xFF00E5FF);
        mAreaPaint.setStyle(Paint.Style.STROKE);
        mAreaPaint.setStrokeWidth(3);

        mAreaTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaTextPaint.setColor(0xFF00E5FF);
        mAreaTextPaint.setTextSize(26);
        mAreaTextPaint.setFakeBoldText(true);

        mHighTempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighTempPaint.setColor(0xFFFF0000);
        mHighTempPaint.setStyle(Paint.Style.STROKE);
        mHighTempPaint.setStrokeWidth(3);

        mLowTempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLowTempPaint.setColor(0xFF0080FF);
        mLowTempPaint.setStyle(Paint.Style.STROKE);
        mLowTempPaint.setStrokeWidth(3);

        mSelectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectPaint.setColor(0xFFFFEB3B);
        mSelectPaint.setStyle(Paint.Style.STROKE);
        mSelectPaint.setStrokeWidth(2);
    }

    /**
     * 设置触摸选取监听
     */
    public void setOnTempSelectListener(OnTempSelectListener listener) {
        mOnTempSelectListener = listener;
    }

    /**
     * 设置光标测温点（协议坐标）
     */
    public void setSpotPoint(int x, int y, float temp) {
        mSpotX = x;
        mSpotY = y;
        mSpotTemp = temp;
    }

    /**
     * 设置测温区域（协议坐标，中心点 + 宽高）
     */
    public void setThermalArea(int centerX, int centerY, int width, int height, float avgTemp) {
        mAreaCenterX = centerX;
        mAreaCenterY = centerY;
        mAreaWidth = width;
        mAreaHeight = height;
        mAreaAvgTemp = avgTemp;
    }

    /**
     * 设置最高温点（协议坐标）
     */
    public void setHighestTempPoint(int x, int y, float temp) {
        mHighestTempX = x;
        mHighestTempY = y;
        mHighestTemp = temp;
    }

    /**
     * 设置最低温点（协议坐标）
     */
    public void setLowestTempPoint(int x, int y, float temp) {
        mLowestTempX = x;
        mLowestTempY = y;
        mLowestTemp = temp;
    }

    /**
     * 清除全部叠加内容
     */
    public void clearOverlay() {
        mSpotX = -1;
        mSpotY = -1;
        mSpotTemp = 0f;
        mAreaCenterX = -1;
        mAreaCenterY = -1;
        mAreaWidth = 0;
        mAreaHeight = 0;
        mAreaAvgTemp = 0f;
        mHighestTempX = -1;
        mHighestTempY = -1;
        mHighestTemp = 0f;
        mLowestTempX = -1;
        mLowestTempY = -1;
        mLowestTemp = 0f;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                mSelectRect = null;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDrag(event.getX(), event.getY())) {
                    if (mSelectRect == null) {
                        mSelectRect = new RectF(mDownX, mDownY, event.getX(), event.getY());
                    } else {
                        mSelectRect.right = event.getX();
                        mSelectRect.bottom = event.getY();
                    }
                    postInvalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                handleTouchUp(event.getX(), event.getY());
                mSelectRect = null;
                postInvalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean isDrag(float x, float y) {
        return Math.abs(x - mDownX) > TOUCH_SLOP || Math.abs(y - mDownY) > TOUCH_SLOP;
    }

    private void handleTouchUp(float upX, float upY) {
        if (mOnTempSelectListener == null) {
            return;
        }
        if (isDrag(upX, upY)) {
            // 拖动选取测温区域：屏幕坐标 → 协议坐标
            List<Short> proto = RectUtil.screenPoint2VideoArg(
                    (int) mDownX, (int) upX, (int) mDownY, (int) upY);
            if (proto == null || proto.size() < 4) {
                return;
            }
            int centerX = proto.get(0) + proto.get(2) / 2;
            int centerY = proto.get(1) + proto.get(3) / 2;
            mOnTempSelectListener.onAreaSelected(centerX, centerY, proto.get(2), proto.get(3));
        } else {
            // 点击选取测温点：屏幕坐标 → 协议坐标
            List<Short> proto = RectUtil.screenPoint2VideoArg(
                    (int) upX, (int) upX, (int) upY, (int) upY);
            if (proto == null || proto.size() < 2) {
                return;
            }
            mOnTempSelectListener.onPointSelected(proto.get(0), proto.get(1));
        }
    }

    /**
     * 协议坐标（点）转屏幕坐标
     */
    private float[] toScreenPoint(int x, int y) {
        List<Short> screenPoint = RectUtil.videoPoint2ScreenArg(x, y, 0, 0, GlobalVariable.FPVType);
        if (screenPoint == null || screenPoint.size() < 2) {
            return new float[]{x, y};
        }
        return new float[]{screenPoint.get(0), screenPoint.get(1)};
    }

    /**
     * 协议坐标（矩形）转屏幕坐标，返回 [left, top, right, bottom]
     */
    private float[] toScreenRect(int leftX, int leftY, int width, int height) {
        List<Short> screenRect = RectUtil.videoPoint2ScreenArg(leftX, leftY, width, height, GlobalVariable.FPVType);
        if (screenRect == null || screenRect.size() < 4) {
            return new float[]{leftX, leftY, leftX + width, leftY + height};
        }
        return new float[]{screenRect.get(0), screenRect.get(1), screenRect.get(2), screenRect.get(3)};
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 测温区域框（协议坐标转屏幕坐标）
        if (mAreaCenterX >= 0 && mAreaCenterY >= 0 && mAreaWidth > 0 && mAreaHeight > 0) {
            float[] rect = toScreenRect(mAreaCenterX - mAreaWidth / 2, mAreaCenterY - mAreaHeight / 2,
                    mAreaWidth, mAreaHeight);
            canvas.drawRect(rect[0], rect[1], rect[2], rect[3], mAreaPaint);
            canvas.drawText("区域: " + mAreaAvgTemp + "℃",
                    rect[0], Math.max(rect[1] - 8, 30), mAreaTextPaint);
        }

        // 最高温点
        if (mHighestTempX >= 0 && mHighestTempY >= 0) {
            float[] p = toScreenPoint(mHighestTempX, mHighestTempY);
            drawCross(canvas, p[0], p[1], 22, mHighTempPaint);
            canvas.drawText(mHighestTemp + "℃", p[0] + 12, p[1] - 12, mAreaTextPaint);
        }

        // 最低温点
        if (mLowestTempX >= 0 && mLowestTempY >= 0) {
            float[] p = toScreenPoint(mLowestTempX, mLowestTempY);
            drawCross(canvas, p[0], p[1], 18, mLowTempPaint);
            canvas.drawText(mLowestTemp + "℃", p[0] + 12, p[1] - 12, mSpotTextPaint);
        }

        // 光标测温点
        if (mSpotX >= 0 && mSpotY >= 0) {
            float[] p = toScreenPoint(mSpotX, mSpotY);
            canvas.drawCircle(p[0], p[1], 16, mSpotPaint);
            canvas.drawCircle(p[0], p[1], 5, mSpotPaint);
            canvas.drawText("光标: " + mSpotTemp + "℃", p[0] + 18, p[1] - 14, mSpotTextPaint);
        }

        // 拖动选取预览框（屏幕坐标）
        if (mSelectRect != null) {
            canvas.drawRect(mSelectRect, mSelectPaint);
        }
    }

    private void drawCross(Canvas canvas, float x, float y, float radius, Paint paint) {
        canvas.drawLine(x - radius, y, x + radius, y, paint);
        canvas.drawLine(x, y - radius, x, y + radius, paint);
    }
}
