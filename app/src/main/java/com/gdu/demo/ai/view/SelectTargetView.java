package com.gdu.demo.ai.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.gdu.demo.R;
import com.gdu.lib.util.TimeClock;
import com.gdu.lib.util.core.XLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ron on 2017/2/21.
 */
public class SelectTargetView extends View implements View.OnTouchListener {
    private static final String TAG = SelectTargetView.class.getSimpleName();
    /**
     * 画识别框
     */
    public static final int DRAW_TARGET_FRAME = 2;
    /**
     * 识别框移动 不能画
     */
    public static final int TARGET_FRAME_MOVE = 3;
    /**
     * 画目标丢失后预测位置
     */
    public static final int DRAW_TARGET_LOSE = 4;

    /**
     * 2  DRAW_TARGET_FRAME 画识别框
     * 3  TARGET_FRAME_MOVE识别框移动 不能画
     */
    private int viewType;

    public static final int CENTER_MOVE = 1;
    public static final int RIGHT_BOTTOM_MOVE = 2;

    private int moveType = 0;

    private final int minWidth = 160;
    private final int minHeight = 160;
    private final int marginLimit = 0;
    private final int closeSize = 90;

    private Paint paint;
    private Paint coverPaint;

    private int downX;
    private int downY;
    private long mDownTime, mLastClickTime;

    private int perX;
    private int perY;

    private int lastMoveAtX;
    private int lastMoveAtY;

    private int currentMoveAtX;
    private int currentMoveAtY;


    private Context context;

    /***
     * <p>上一次的点击事件</p>
     */
    private long lastActionUpTime = 0;

    /*****************
     * <p>框的位置</p>
     */
    public int left, top, right, bottom;


    private List<OnSelectCallBack> mSelectCallBackList;
    private Bitmap left_top_bitmap;
    private Bitmap left_bottom_bitmap;
    private Bitmap right_top_bitmap;
    private Bitmap right_bottom_bitmap;
    private Bitmap center_bitmap;
//    private Bitmap closeBitmap;
    private Bitmap dragBitmap;
    private Bitmap targetLoseBitmap;
    private int height;
    private int centerImgWidth;
    private Point mLastPoint;

    private int loseLeft, loseTop, loseRight, loseBottom; //目标丢失后，云台下发的猜测轨迹框

    /**
     * 识别框或测温框是否已显示
     */
    private boolean isShow = false;

    /**
     *  是否全局测温
     */
    boolean isFullScreenMeasure = false;

    /**
     * 开启全局测温后记录上一次测温的位置 用于还原
     */
    private int lastLeft, lastTop, lastRight, lastBottom;

    private final Handler mHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            XLogger.APP.i("handleMessage() msgWhat = " + msg.what + "; this = " + System.identityHashCode(this));
            final Point mPoint = (Point) msg.obj;
            // 双击事件处理
            if (msg.what == 2) {
                if (mPoint == null) {
                    return;
                }
                if (!isShow) {
                    lastActionUpTime = 0;
                    callbackDoubleClick(mPoint);
                }
                initValue();

                // 单击事件处理
            }
//            else {
//                if (mPoint == null) {
//                    return;
//                }
//                if (checkCanIRTemp()&&!isShow) {
//                    lastActionUpTime = 0;
//                    callbackOneClick(mPoint);
//                }
//                initValue();
//            }
        }
    };

    public void setOnSelectCallBack(OnSelectCallBack onSelectCallBack) {
        mSelectCallBackList.add(onSelectCallBack);
    }

    public SelectTargetView(Context context) {
        super(context);
        init(context);
    }

    public SelectTargetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectTargetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mSelectCallBackList = new ArrayList<>();
        paint = new Paint();
        paint.setColor(context.getResources().getColor((R.color.color_60e353)));
        paint.setAntiAlias(true); // 打开抗矩齿
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getResources().getDimensionPixelOffset(R.dimen.dp_2));
        this.setOnTouchListener(this);
        this.context = context;
        coverPaint = new Paint();
        coverPaint.setColor(context.getResources().getColor((R.color.transparent)));
        coverPaint.setAntiAlias(true); // 打开抗矩齿
        coverPaint.setStyle(Paint.Style.FILL);

        left_top_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.track_icon_left_top);
        left_bottom_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.track_icon_left_bottom);
        right_top_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.track_icon_right_top);
        right_bottom_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.track_icon_right_bottom);
        center_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.track_icon_center);
        height = left_bottom_bitmap.getHeight();
        centerImgWidth = center_bitmap.getWidth();
//        closeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.temperature_close);
        dragBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.temperature_drag);
        targetLoseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.target_point);
    }

    private void initValue() {
        downX = -1;
        downY = -1;
        mDownTime = -1;
        mLastClickTime = -1;
        if (!isShow) {
            mLastPoint = null;
        }
    }

    public void setViewType(int viewType) {
        XLogger.APP.i(TAG,"setViewType() viewType = " + viewType);
        this.viewType = viewType;
    }

    public int getViewType(){
        return this.viewType;
    }

    /**
     * <P>3米以上显示绿色 框选，正常</P>
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void updateUi(int left, int top, int right, int bottom) {
        XLogger.APP.i(TAG,"updateUi() left = " + left + "; top = " + top + "; right = " + right + "; bottom = " + bottom
                + "; viewType = TARGET_FRAME_MOVE");
        paint.setColor(context.getResources().getColor((R.color.color_60e353)));
        coverPaint.setColor(context.getResources().getColor((R.color.transparent)));
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        viewType = TARGET_FRAME_MOVE;
        this.postInvalidate();
    }

    /**
     * <P>3米以下显示红色 框选，警告</P>
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void updateUiWithRed(int left, int top, int right, int bottom) {
        XLogger.APP.i(TAG,"updateUiWithRed() left = " + left + "; top = " + top + "; right = " + right
                + "; bottom = " + bottom + "; viewType = TARGET_FRAME_MOVE");
        paint.setColor(context.getResources().getColor((R.color.color_ff0000)));
        coverPaint.setColor(context.getResources().getColor((R.color.transparent)));
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        viewType = TARGET_FRAME_MOVE;
        this.postInvalidate();
    }

    /**
     * 目标丢失后，画预测位置
     */
    public void updateTargetLoseUi(int left, int top, int right, int bottom) {
        XLogger.APP.i(TAG,"updateTargetLoseUi() left = " + left + "; top = " + top + "; right = " + right
                + "; bottom = " + bottom + "; viewType = DRAW_TARGET_LOSE");
        paint.setColor(context.getResources().getColor((R.color.color_60e353)));
        this.viewType = DRAW_TARGET_LOSE;
        this.loseLeft = left;
        this.loseTop = top;
        this.loseRight = right;
        this.loseBottom = bottom;
        this.postInvalidate();
    }

    /***************
     * 停止跟踪
     */
    public void stopTrack() {
        XLogger.APP.i(TAG,"selectTarget stopTrack ");
        clearDraw();
    }

    /************
     * 清理界面
     */
    public void clearDraw() {
        left = 0;
        top = 0;
        right = 0;
        bottom = 0;
        isShow = false;
        XLogger.APP.i(TAG,"clearDraw() left = " + left + "; top = " + top + "; right = " + right + "; buttom = " + bottom);


        downX = 0;
        downY = 0;
        currentMoveAtX = 0;
        currentMoveAtY = 0;
        lastMoveAtX = 0;
        lastMoveAtY = 0;

        loseLeft = 0;
        loseTop = 0;
        loseRight = 0;
        loseBottom = 0;

        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        XLogger.APP.i("onDraw() viewType = " + viewType + "; this = " + System.identityHashCode(this));
        if (viewType == TARGET_FRAME_MOVE) {
            if (left != 0 && right != 0 && top != 0 && bottom != 0) {
                canvas.drawBitmap(left_top_bitmap, left, top, paint);
                canvas.drawBitmap(left_bottom_bitmap, left, bottom - height, paint);
                canvas.drawBitmap(right_top_bitmap, right - height, top, paint);
                canvas.drawBitmap(right_bottom_bitmap, right - height, bottom - height, paint);
                canvas.drawBitmap(center_bitmap, left + (right - left - centerImgWidth) * 0.5f,
                        top + (bottom - top - centerImgWidth) * 0.5f, paint);
            }
//            canvas.drawRect(left, top, right, bottom, coverPaint);
//            if (mTargetType != 0 && mTargetBitmap != null) {
//                canvas.drawBitmap(mTargetBitmap, left - 120, top, mTargetPaint);
//            }
        } else if (viewType == DRAW_TARGET_FRAME) {
            XLogger.APP.i(TAG,"onDraw() left = " + left + "; top = " + top + "; viewType = " + viewType);
            // 测温模式且测温开关关闭，不显示框
//            if (!GlobalVariable.irTempOpen) {
//                left = 0;
//                right = 0;
//                top = 0;
//                bottom = 0;
//                return;
//            }
            if (right <= 0 || bottom <= 0 || Math.abs(right - left) <= 0 || Math.abs(bottom - top) <= 0) {
                return;
            }
//            if (isShow && viewType == DRAW_TEMP_FRAME) {
////                XLogger.APP.i("onDraw() DRAW_TEMP_FRAME and isShow");
////                if (Math.abs(right - left) >= 120 && Math.abs(bottom - top) >= 120) {
////                    canvas.drawBitmap(closeBitmap, right - closeBitmap.getWidth(), top, paint);
////                }
//                canvas.drawBitmap(dragBitmap, right - dragBitmap.getWidth(),
//                        bottom - dragBitmap.getHeight(), paint);
//            }
            canvas.drawRect(left, top, right, bottom, paint);
            canvas.drawRect(left, top, right, bottom, coverPaint);
        } else if (viewType == DRAW_TARGET_LOSE) {
//            XLogger.APP.i("onDraw() DRAW_TARGET_LOSE");
            if (loseLeft == 0 || loseRight == 0 || loseTop == 0 || loseBottom == 0) {
                return;
            }
            canvas.drawBitmap(targetLoseBitmap, loseLeft + (loseRight - loseLeft - targetLoseBitmap.getWidth()) * 0.5f,
                    loseTop + (loseBottom - loseTop - targetLoseBitmap.getWidth()) * 0.5f, paint);
//                String text = context.getResources().getString(R.string.string_target_searching);
//                Rect textRect = new Rect();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        XLogger.APP.i("onTouch() viewType = " + viewType + "; isShow = " + isShow + "; this = " + System.identityHashCode(this));
        if (viewType == TARGET_FRAME_MOVE || viewType == 0) {
            return false;
        }

        if (viewType == DRAW_TARGET_FRAME && isShow) {
//            XLogger.APP.i("onTouch() DRAW_TARGET_FRAME and isShow ; this = " + System.identityHashCode(this));
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                perX = (int) event.getX();
                perY = (int) event.getY();
                mHandle.removeMessages(0);
                if (viewType == DRAW_TARGET_FRAME) {
                    if (isShow) {
                        moveType = getMoveType(event);
                        callBackShow(false, false);
                    } else {
                        left = downX;
                        top = downY;
                    }
                }
                mDownTime = TimeClock.getSysLaunchRealtime();
                break;

            case MotionEvent.ACTION_MOVE:
                currentMoveAtX = (int) event.getX();
                currentMoveAtY = (int) event.getY();

                int moveX = currentMoveAtX - perX;
                int moveY = currentMoveAtY - perY;
                perX = currentMoveAtX;
                perY = currentMoveAtY;

                lastMoveAtX = currentMoveAtX;
                lastMoveAtY = currentMoveAtY;

                if (viewType == DRAW_TARGET_FRAME) {
                    if (isShow) {
                        dragView(moveX, moveY);
                    } else {

                        if (viewType == DRAW_TARGET_FRAME) {
                            right = currentMoveAtX;
                            bottom = currentMoveAtY;
                            this.postInvalidate();

                        } else if (Math.abs(currentMoveAtX - downX) > 20 && Math.abs(currentMoveAtY - downY) > 20) {

                            right = currentMoveAtX;
                            bottom = currentMoveAtY;
                            int maxWidth = getRight() - getLeft();
                            int maxHeight = getBottom() - getTop();
                            if (right > maxWidth) {
                                right = maxWidth;
                            }
                            if (bottom > maxHeight) {
                                bottom = maxHeight;
                            }

                            if (right <= 0) {
                                right = 1;
                                lastMoveAtX = 1;
                            }
                            if (bottom <= 0) {
                                bottom = 1;
                            }


                            this.postInvalidate();
                        }
                    }
                }

                XLogger.APP.i(TAG,"onTouch() selectTarget  left = " + left + " , top = " + top);
                break;

            case MotionEvent.ACTION_UP:
                if (viewType == DRAW_TARGET_FRAME) {
                    if (right != 0 && bottom != 0) {
                        if (left > right) {
                            int temp = left;
                            left = right;
                            right = temp;
                        }
                        if (top > bottom) {
                            int temp = top;
                            top = bottom;
                            bottom = temp;
                        }
                    }

                    clickJudge(event);

                    if (right > 0 && Math.abs(right - left) > 0) {
                        isShow = true;
                    }
                    postInvalidate();
                } else {
                    clickJudge(event);
                }
                break;

            default:
                break;
        }
        return false;
    }

    private int getMoveType(MotionEvent event) {
        if (downX >= left && downX <= right && downY >= top && downY <= bottom) {
            int touchX = right - (int) event.getX();
            int touchY = bottom - (int) event.getY();
            if (touchX < closeSize && touchY < closeSize) {
                return RIGHT_BOTTOM_MOVE;
            } else {
                return CENTER_MOVE;
            }
        }
        return 0;
    }

    private void dragView(int moveX, int moveY) {
//        XLogger.APP.i("dragView() moveX = " + moveX + "; moveY = " + moveY);
        // 整体移动
        if (moveType == CENTER_MOVE) {
            int weight = right - left;
            int height = bottom - top;
            left += moveX;
            right += moveX;
            top += moveY;
            bottom += moveY;

            if (left < marginLimit) {
                left = marginLimit;
                right = left + weight;
            }

            if (top < marginLimit) {
                top = marginLimit;
                bottom = top + height;
            }
            int maxWidth = getRight() - getLeft();
            int maxHeight = getBottom() - getTop();

            if (right > maxWidth) {
                right = maxWidth;
                left = right - weight;
            }
            if (bottom > maxHeight) {
                bottom = maxHeight;
                top = bottom - height;
            }
            postInvalidate();
            // 右下角 放大范围
        } else if (moveType == RIGHT_BOTTOM_MOVE) {

            right += moveX;
            if (right - left < minWidth) {
                right = left + minWidth;
            }
            bottom += moveY;
            if (bottom - top < minHeight) {
                bottom = top + minHeight;
            }

            int maxWidth = getRight() - getLeft();
            int maxHeight = getBottom() - getTop();
            if (right > maxWidth) {
                right = maxWidth;
            }
            if (bottom > maxHeight) {
                bottom = maxHeight;
            }
            if (left != 0 || top != 0 || right != maxWidth || bottom != maxHeight) {
                isFullScreenMeasure = false;
            }
            postInvalidate();
        }
    }


    /**
     * 单击，双击，长按，拖动判断
     * @param event
     */
    private void clickJudge(MotionEvent event) {
        final long upTime = TimeClock.getSysLaunchRealtime();
        final float currentClickX = event.getX();
        final float currentClickY = event.getY();
        final long mUpTime = TimeClock.getSysLaunchRealtime();

        boolean isClick = Math.abs(currentClickX - downX) < 20 && Math.abs(currentClickY - downY) < 20 && Math.abs(mUpTime - mDownTime) < 200;
//        boolean isClose = right - currentClickX < closeSize && currentClickY - top < closeSize
//                && currentClickX < right && currentClickY > top;

        XLogger.APP.i(TAG,"clickJudge()  isClick = " + isClick + "; viewType = " + viewType);

        if (isClick) {
            // 目前目标识别不响应点击和双击事件
            if (viewType == DRAW_TARGET_FRAME) {
                clearDraw();
                return;
            }
            final boolean isDoubleClick = mLastPoint != null && Math.abs(currentClickX - mLastPoint.x) < 50 && Math.abs(currentClickY - mLastPoint.y) < 50
                    && mUpTime - mLastClickTime < 300;
            // 若两次点击事件时间差小于300ms则表示发生双击事件
            if (isDoubleClick) {
                XLogger.APP.i(TAG,"clickJudge() 双击事件");
                mHandle.removeMessages(0);
                final Point mPoint = new Point((int) currentClickX, (int) currentClickY);
                final Message msg = new Message();
                msg.what = 2;
                msg.obj = mPoint;
                mHandle.sendMessage(msg);
            } else {
                mLastClickTime = mUpTime;
                mLastPoint = new Point((int) currentClickX, (int) currentClickY);
                final Message msg = new Message();
                msg.what = 0;
                msg.obj = mLastPoint;
                mHandle.sendMessageDelayed(msg, 500);
            }
        } else {
            lastActionUpTime = upTime;
            if (lastMoveAtX > 0) {
                XLogger.APP.i(TAG,"clickJudge() 滑动事件");
                Point point1 = new Point(left, top);
                Point point2 = new Point(right, bottom);

                XLogger.APP.i(TAG,"clickJudge() selectTarget  left = " + left + ", top = " + top + ", right = " + right +
                        ", bottom = " + bottom);
//                if (viewType == DRAW_TEMP_FRAME) {
//                    if (right != 0 && bottom != 0 && !isFullScreenMeasure) {
//                        callBack(point1, point2);
//                    }
//                } else {
                    callBack(point1, point2);
//                }
            }
        }
        callBackShow(true, false);
    }

    /**
     * 回调区域选择
     * @param point1
     * @param point2
     */
    private void callBack(Point point1, Point point2) {
        XLogger.APP.i(TAG,"callBack() viewType = " + viewType);
        if (mSelectCallBackList == null) {
            return;
        }
        for (OnSelectCallBack selectCallBack : mSelectCallBackList) {
            selectCallBack.onSelect(point1, point2);
        }
    }

    /**
     * 回调单击
     * @param point1
     */
    private void callbackOneClick(Point point1) {
        XLogger.APP.i(TAG,"callbackOneClick() viewType = " + viewType);
        if (mSelectCallBackList == null) {
            return;
        }
        for (OnSelectCallBack selectCallBack : mSelectCallBackList) {
            selectCallBack.onClick(point1);
        }
    }

    /**
     * 回调双击
     * @param point
     */
    private void callbackDoubleClick(Point point) {
        XLogger.APP.i(TAG,"callbackDoubleClick() viewType = " + viewType);
        if (mSelectCallBackList == null) {
            return;
        }
        for (OnSelectCallBack selectCallBack : mSelectCallBackList) {
            selectCallBack.onDoubleClick(point);
        }
    }


    private void callBackShow(boolean show, boolean isClose) {
        XLogger.APP.i(TAG,"callBackShow() show = " + show + "; isClose = " + isClose);
        if (mSelectCallBackList == null) {
            return;
        }

        if (!isShow) {
            return;
        }
        if (downX < left || downX > right || downY < top || downY > bottom) {
            return;
        }
        notifySelectCallback(show, isClose);
    }

    private void notifySelectCallback(boolean show, boolean isClose) {
        if (mSelectCallBackList != null && mSelectCallBackList.size() > 0) {
            for (OnSelectCallBack selectCallBack : mSelectCallBackList) {
                selectCallBack.regionTempIsShow(show, isClose);
            }
        }
    }


    public interface OnSelectCallBack {
        void onSelect(Point one, Point two);
        void onClick(Point point);
        void onDoubleClick(Point point);

        /**
         * 区域测温内容是否需要显示
         */
        void regionTempIsShow(boolean isDown, boolean isClose);

    }


    public boolean getFullScreenMeasureTemp() {
        return isFullScreenMeasure;
    }


    public void onDestroy() {
        if (mSelectCallBackList == null) {
            return;
        }
        for (OnSelectCallBack selectCallBack : mSelectCallBackList) {
            selectCallBack = null;
        }
        mSelectCallBackList.clear();
        mSelectCallBackList = null;
    }
}
