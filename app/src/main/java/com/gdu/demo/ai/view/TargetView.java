package com.gdu.demo.ai.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import com.gdu.demo.R;
import com.gdu.demo.ai.config.TargetType;
import com.gdu.lib.util.TimeClock;
import com.gdu.lib.util.core.XLogger;

/**
 * Created by zhangzhilai on 2018/8/17.
 */

public class TargetView extends View implements View.OnTouchListener {
    private static final String TAG = TargetView.class.getSimpleName();
    private final Context mContext;

    /**
     *  1  多目标检测
     */
    private int type;

    private OnTargetViewListener mOnTargetViewListener;

    private boolean isPoint;  //标识是点还是框，默认是点

    private boolean isTrack;  //标记是否是跟踪状态

    private boolean isSelect; //标记是否被选中

    private Bitmap mTargetBitmap;  //是点时，显示的图片
    private Paint mTargetPaint;    //目标是点

    /**
     * 框和覆盖层的画笔
     */
    private Paint mFramePaint;
    private Paint mCoverPaint;

    private Bitmap left_top_bitmap;
    private Bitmap left_bottom_bitmap;
    private Bitmap right_top_bitmap;
    private Bitmap right_bottom_bitmap;
    private int height;

    private Bitmap point_center_bitmap;

    private int weight;


    /**
     * 经纬度的画笔
     */
    private Paint mLatLngPaint;

    /**
     * 跟踪的画笔
     */
    private Paint mTrackTextPaint;
    private Paint mTrackBackStrockPaint;
    private Paint mTrackBackCoverPaint;

    /*****************
     * <p>框的位置</p>
     */
    private int left, top, right, bottom;

    private int pointX, pointY;

    private int mTargetColor;
    private int mStrokeTargetColor;

    private String mLatLng = "";
    private int mTrackId;
    private byte mTrackType;
    private short targetType;

    /***
     * <p>上一次的点击事件</p>
     */
    private long lastActionUpTime = 0;
    int downX;
    int downY;

    public TargetView(Context context) {
        this(context, null);
    }

    public TargetView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TargetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
//        initTest();
        init();
    }

    public void setOnTargetViewListener(OnTargetViewListener onTargetViewListener) {
        mOnTargetViewListener = onTargetViewListener;
    }

    public void setTrackId(int trackId) {
        mTrackId = trackId;
    }

    public int getTrackId() {
        return mTrackId;
    }

    public short getTargetType() {
        return targetType;
    }

    public void setTargetType(short targetType) {
        this.targetType = targetType;
    }

    private void init() {
        isPoint = false;
        mTargetColor = R.color.color_60e353;
        setOnTouchListener(this);
        mTargetPaint = new Paint();

        mLatLngPaint = new Paint();

        mTrackTextPaint = new Paint();
        mTrackBackStrockPaint = new Paint();
        mTrackBackStrockPaint.setColor(mContext.getResources().getColor((mTargetColor)));
        mTrackBackStrockPaint.setStyle(Paint.Style.STROKE);

        mTrackBackCoverPaint = new Paint();
        mTrackBackCoverPaint.setColor(mContext.getResources().getColor((mTargetColor)));
        mTrackBackCoverPaint.setStyle(Paint.Style.FILL);

        left_top_bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.track_icon_left_top_red);
        left_bottom_bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.track_icon_left_bottom_red);
        right_top_bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.track_icon_right_top_red);
        right_bottom_bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.track_icon_right_bottom_red);
        height = left_bottom_bitmap.getHeight();

        point_center_bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.target_point);
        weight = point_center_bitmap.getWidth();

        mFramePaint = new Paint();

        mFramePaint.setAntiAlias(true); // 打开抗矩齿
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(getResources().getDimensionPixelOffset(R.dimen.dp_1_5));

        mCoverPaint = new Paint();
        mCoverPaint.setAntiAlias(true); // 打开抗矩齿
        mCoverPaint.setStyle(Paint.Style.FILL);

        initPoint();
    }

    private void initPoint() {
        mTargetBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_person);
    }

    /**
     * 切换点和选择框
     *
     * @param isPoint
     */
    public void isPoint(boolean isPoint) {
        this.isPoint = isPoint;
        postInvalidate();
    }

    /**
     * 标记是否是跟踪状态
     */
    public void isTrack(boolean isTrack) {
        this.isTrack = isTrack;
    }

    /**
     * 设置经纬度
     *
     * @param latLng
     */
    public void setLatLng(String latLng) {
        mLatLng = latLng;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPoint) {
            canvas.drawBitmap(mTargetBitmap, left, top, mTargetPaint);
        } else {
            drawTrackFrame(canvas);
        }
    }

    /**
     * 标记目标是否被选中
     *
     * @param isSelect
     */
    public void setTargetSelected(boolean isSelect) {
        this.isSelect = isSelect;
        postInvalidate();
    }

    /**
     * 更新框的坐标
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void updateTrackPoint(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;

        XLogger.APP.i(TAG, "TrackPoint update left =" + left + ", top = " + top + ",right = " + right + ",bottom = " + bottom);

        postInvalidate();
    }

    public void updatePoint(int left, int top, int right, int bottom, int pointX , int pointY) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.pointX = pointX;
        this.pointY = pointY;
//        XLogger.APP.i("TrackPoint update left =" + left + ", top = " + top);
        postInvalidate();
    }

    /**
     * 画跟踪框
     * 1.上面是text（经纬度）
     * 2.下面跟踪框
     * 3.右上角，可点击button
     */
    private void drawTrackFrame(Canvas canvas) {
        if (isSelect || mTrackType == TargetType.TARGET_LOCATE.getKey()) {
            mTargetColor = R.color.color_ff0000;
            mStrokeTargetColor = R.color.transparent;
        } else {
            mTargetColor = R.color.color_60e353;
            mStrokeTargetColor = R.color.transparent;
        }


        // 多目标跟踪识别
        if (type == 1) {
            canvas.drawBitmap(point_center_bitmap, pointX - weight / 2, pointY - weight / 2, mFramePaint);
            mFramePaint.setColor(mContext.getResources().getColor((mTargetColor)));
//            canvas.drawRect(left, top, right, bottom, mFramePaint);
        } else {
            if (isSelect || mTrackType == TargetType.TARGET_LOCATE.getKey()) {
                canvas.drawBitmap(left_top_bitmap, left, top, mFramePaint);
                canvas.drawBitmap(left_bottom_bitmap, left, bottom - height, mFramePaint);
                canvas.drawBitmap(right_top_bitmap, right - height, top, mFramePaint);
                canvas.drawBitmap(right_bottom_bitmap, right - height, bottom - height, mFramePaint);

            } else {
                mCoverPaint.setColor(mContext.getResources().getColor((mStrokeTargetColor)));
                mFramePaint.setColor(mContext.getResources().getColor((mTargetColor)));
                canvas.drawRect(left, top, right, bottom, mFramePaint);
                canvas.drawRect(left, top, right, bottom, mCoverPaint);
                XLogger.APP.i(TAG, "TrackPoint draw  left =" + left + ", top = " + top + ",right = " + right + ",bottom = " + bottom);
                mLatLngPaint.setTextSize(30);
                mLatLngPaint.setColor(mContext.getResources().getColor((R.color.color_ff0000)));
                canvas.drawText(mLatLng, left, top - 10, mLatLngPaint);
            }
            if (!isTrack) {
                drawTrackText(canvas);
            }
        }


    }

    /**
     * 画跟踪按钮
     *
     * @param canvas
     */
    private void drawTrackText(Canvas canvas) {
        float trackLeft = right + 14;
        float trackTop = top + 20;
        float trackRight = right + 14 + 100;
        float trackBottom = top + 20 + 60;
        canvas.drawRect(trackLeft, trackTop, trackRight, trackBottom, mTrackBackCoverPaint);
        canvas.drawRect(trackLeft, trackTop, trackRight, trackBottom, mTrackBackCoverPaint);
        mTrackTextPaint.setTextSize(35);
        canvas.drawText("track", trackLeft, trackTop + 40, mTrackTextPaint);
    }

    /**
     * 判断是否点击了跟踪按钮，在框的右上角
     * @param downX
     * @param downY
     * @return
     */
//    private boolean isClickTrack(int downX, int downY){
//        float trackLeft = right + 14;
//        float trackTop = top + 20;
//        float trackRight = right + 14 + 100;
//        float trackBottom = top + 20 + 60;
//        if (downX > trackLeft && downX < trackRight &&
//                downY > trackTop && downY < trackBottom) {
//            return true;
//        }
//        return false;
//    }


    /**
     * 判断是否点击了跟踪按钮
     *
     * @param downX
     * @param downY
     * @return
     */
    private boolean isClickTrack(int downX, int downY) {
        float trackLeft = left;
        float trackTop = top;
        float trackRight = right;
        float trackBottom = bottom;

        boolean isIn = (downX > trackLeft && downX < trackRight &&
                downY > trackTop && downY < trackBottom);
//        XLogger.APP.d("onTrackClick  id = " + mTrackId + ", isIn = " + isIn + " , downX= " + downX + " ,downY = " + downY + " ,( " + left + "," + right + ", " + top + "," + bottom + ")");

        return isIn;
    }

    private boolean isClickPoint(int downX, int downY) {
        float trackLeft = left;
        float trackTop = top;
        float trackRight = left + mTargetBitmap.getWidth();
        float trackBottom = top + mTargetBitmap.getHeight();

        return downX > trackLeft && downX < trackRight &&
                downY > trackTop && downY < trackBottom;
    }

    private void clear() {
        if (mTargetBitmap != null) {
            mTargetBitmap.recycle();
            mTargetBitmap = null;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                return onTrackClick(downX, downY);
            case MotionEvent.ACTION_UP:
                long time = TimeClock.getSysLaunchRealtime();
                if (time - lastActionUpTime < 200) {
                    lastActionUpTime = 0;
                    if (mOnTargetViewListener != null) {
                        mOnTargetViewListener.onDoubleClick(mTrackId, mTrackType);
                    }
                } else {
                    lastActionUpTime = time;
                }
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;

            default:
                break;
        }
        return false;
    }

    public boolean onTrackClick(int downX, int downY) {
        if (type == 1) {
            boolean isIn = (downX > pointX - weight / 2 - 50 && downX < pointX + weight / 2 + 50 &&
                    downY > pointY - weight / 2 - 50 && downY < pointY + weight / 2 + 50);

            XLogger.APP.d(TAG, "onTrackClick   downX = " + downX + ", downY = " + downY + ",pointX = "
                    + pointX + ",pointY = " + pointY + ",weight = " + weight + "isIn = " + isIn);
            if (isIn && mOnTargetViewListener != null) {

                XLogger.APP.d(TAG, "onTrackClick   sss mTrackId =  " + mTrackId);

                mOnTargetViewListener.onTrackClick(mTrackId, mTrackType);
                return true;
            }
            return false;
        } else {
            boolean isClickTrack = isClickTrack(downX, downY);
            boolean isClickPoint = isClickPoint(downX, downY);
            if (!isPoint && isClickTrack && mOnTargetViewListener != null) {
                mOnTargetViewListener.onTrackClick(mTrackId, mTrackType);
                return true;
            } else if (isPoint && isClickPoint && mOnTargetViewListener != null) {
                mOnTargetViewListener.onTrackPointClick(mTrackId);
                return true;
            }
            return false;
        }
    }

    public byte getmTrackType() {
        return mTrackType;
    }

    public void setmTrackType(byte mTrackType) {
        this.mTrackType = mTrackType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public interface OnTargetViewListener {
        /**
         * 开始跟踪点击
         */
        void onTrackClick(int trackId, byte trackType);

        /**
         * 识别点点击
         */
        void onTrackPointClick(int trackId);

        /**
         * 双击事件
         *
         * @param trackId
         */
        void onDoubleClick(int trackId, byte trackType);
    }

    public void reset() {
        isTrack = false;
        mTrackId = 0;
        type = 0;
        mTrackType = 0;
        targetType = 0;
        mOnTargetViewListener = null;
    }
}
