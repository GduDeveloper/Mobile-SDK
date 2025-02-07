package com.gdu.demo.widgetlist.radar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.gdu.demo.R;
import com.gdu.radar.ObstaclePoint;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FPVRadarView extends View {

    private final boolean enable = true;
    private final DecimalFormat df = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    //是否显示避障点坐标（调试用）
    private boolean showPointPosition = false;
    /**
     * 角度变化敏感度，当航向角度变化小于该值，则不更新
     */
    public final float ANGLE_CHANGED_SENSITIVITY = 0.1f;

    private int width = 300;
    private int height = 400;
    private float centerX = 0;
    private float centerY = 0;
    private float radius;
    //最大圆环半径的1/8
    private float R_1_8;
    private Paint circlePaint;
    private Paint warningPaint;
    private Paint textPaint;
    private Paint pathPaint;
    private final float pathPaintWidth = 5f;
    private Paint topTextPaint;
    private int colorCircleInnerCenter = Color.parseColor("#7Feeeeee");
    private int colorCircleInnerEnd = Color.parseColor("#7F111111");
    private int colorCircleOuter = Color.TRANSPARENT;
    private int colorCircleBorder = Color.parseColor("#7fffffff");
    private int colorTextBorder = Color.parseColor("#7F7F7F");
    private float widthCircleBorder = 2;
    //航向角度
    private float angleOffset = 0f;
    private float angleTextSize = 40;
    private float topTextSize = 40;
    private float stateTextSize = 40;
    private float returnTextSize = 40;
    final private String[] angles = new String[]{"N", "30", "60", "E", "120", "150", "S", "210", "240", "W", "300", "330"};
    private RadialGradient radialGradient;
    //避障死角范围
    final private RectF rectF = new RectF();
    //避障死角起始角偏移
    private float obstacleOffsetAngle = 45;
    //避障死角角度
    private float obstacleAngle = 15;
    //中心箭头的半径
    private float centerArrowRadius = 0f;
    //中心箭头的path
    final private Path arrowPath = new Path();
    //中心箭头顶角大小
    final private float arrowTopAngle = 40f;
    private int colorArrow = Color.parseColor("#00B5FF");
    //水平线
    final private Path horizonLine = new Path();
    //水平线倾斜角度 取值范围-180~180
    private float dipAngle = 0;
    private float topTextHeight;
    private float topTextDescent;
    //返航点背景path
    final private Path returnPointBg = new Path();
    private int colorReturnPointBg = Color.parseColor("#5a5c50");
    private int colorReturnPointBorder = Color.parseColor("#7fffffff");
    private int colorReturnPointNum = Color.parseColor("#70f549");
    private int colorReturnPointDistance = Color.parseColor("#09D93E");
    final private int radiusReturnPointIcon = 15;
    private float returnPointIconY;
    private float returnPointIconX;
    //到返航点的距离
    private String distance = "0m";
    private float returnPointNumX;

    //障碍点信息
    final private List<ObstaclePoint> obstaclePointList = new ArrayList<>();
    private Paint obstaclePaint;
    //障碍点实际距离与控件尺寸的比例
    private float distanceRate = 1f;
    private float maxDistance = 0f;
    //障碍点圆点半径
    private float radiusObstaclePoint = 10f;
    //云台角度
    final private Path gimbalDirection = new Path();
    final private float gimbalTopAngle = 30f;
    private float gimbalAngle = 0f;
    private final int colorGimbalArrow = Color.parseColor("#FFFFFF");
    //画避障关闭后的四个OFF
    private Paint stateTextPaint;
    private int stateTextColor = Color.parseColor("#ED4F2E");

    /**
     * 是否是圆弧排列的角度文字 true:圆弧排列 false:垂直排列
     */
    public boolean circleTextMode = true;

    /**
     * 是否显示避障关闭状态
     */
    private boolean isShowObstacleOFF = true;

    /**
     * 绘制圆弧排列的文字的弧线区域
     */
    private final RectF circleTextRectF = new RectF();
    /**
     * 绘制圆弧排列的文字path 由circleTextRectF计算得出
     */
    private final Path circleTextPath = new Path();

    public FPVRadarView(Context context) {
        this(context, null);
    }

    public FPVRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FPVRadarView);
            colorCircleInnerCenter = typedArray.getColor(R.styleable.FPVRadarView_color_circle_inner_center, colorCircleInnerCenter);
            colorCircleInnerEnd = typedArray.getColor(R.styleable.FPVRadarView_color_circle_inner_end, colorCircleInnerEnd);
            colorCircleOuter = typedArray.getColor(R.styleable.FPVRadarView_color_circle_outer, colorCircleOuter);
            colorCircleBorder = typedArray.getColor(R.styleable.FPVRadarView_color_circle_border, colorCircleBorder);
            widthCircleBorder = typedArray.getDimension(R.styleable.FPVRadarView_width_circle_border, widthCircleBorder);
            angleTextSize = typedArray.getDimension(R.styleable.FPVRadarView_angle_text_size, angleTextSize);
            topTextSize = typedArray.getDimension(R.styleable.FPVRadarView_top_text_size, topTextSize);
            obstacleOffsetAngle = typedArray.getFloat(R.styleable.FPVRadarView_obstacle_start_angle, obstacleOffsetAngle);
            obstacleAngle = typedArray.getFloat(R.styleable.FPVRadarView_obstacle_sweep_angle, obstacleAngle);
            colorArrow = typedArray.getColor(R.styleable.FPVRadarView_color_center_arrow, colorArrow);
            colorReturnPointBg = typedArray.getColor(R.styleable.FPVRadarView_color_return_point_bg, colorReturnPointBg);
            colorReturnPointBorder = typedArray.getColor(R.styleable.FPVRadarView_color_return_point_border, colorReturnPointBorder);
            colorReturnPointNum = typedArray.getColor(R.styleable.FPVRadarView_color_return_point_num, colorReturnPointNum);
            radiusObstaclePoint = typedArray.getDimension(R.styleable.FPVRadarView_radius_obstacle_point, radiusObstaclePoint);
            stateTextSize = typedArray.getDimension(R.styleable.FPVRadarView_state_text_size,stateTextSize);
            stateTextColor = typedArray.getColor(R.styleable.FPVRadarView_state_text_color, stateTextColor);
            returnTextSize = typedArray.getDimension(R.styleable.FPVRadarView_frv_return_text_size,returnTextSize);
            typedArray.recycle();
        }
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(angleTextSize);
        textPaint.setFakeBoldText(true);

        warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        warningPaint.setColor(Color.RED);
        warningPaint.setStyle(Paint.Style.FILL);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStrokeWidth(pathPaintWidth);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        topTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topTextPaint.setColor(Color.WHITE);
        topTextPaint.setTextAlign(Paint.Align.CENTER);
        topTextPaint.setTextSize(topTextSize);
        topTextPaint.setFakeBoldText(true);
        Paint.FontMetrics fontMetrics = topTextPaint.getFontMetrics();
        topTextDescent = fontMetrics.descent;
        topTextHeight = Math.abs(fontMetrics.ascent - topTextDescent);

        obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        obstaclePaint.setStyle(Paint.Style.FILL);

        stateTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stateTextPaint.setTextAlign(Paint.Align.CENTER);
        stateTextPaint.setColor(stateTextColor);
        stateTextPaint.setTextSize(stateTextSize);
        stateTextPaint.setFakeBoldText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getDefaultSize(width, widthMeasureSpec);
        height = getDefaultSize(height, heightMeasureSpec);

        centerX = width / 2f;
        centerY = (height + topTextHeight) / 2f;
        radius = Math.min(width, height - topTextHeight) / 2f * 0.9f;
        R_1_8 = radius / 8f;
        if (maxDistance != 0) {
            setObstacleMax(maxDistance);
        }
        centerArrowRadius = R_1_8;
        initIconPath();
        initReturnPointPath();
        initGimbalPath();
        //内圆渐变色
        radialGradient = new RadialGradient(centerX, centerY, R_1_8 * 6,
                colorCircleInnerCenter, colorCircleInnerEnd, Shader.TileMode.CLAMP);

        int rInner = (int) (R_1_8 * 6 - widthCircleBorder / 2);
        rectF.set(centerX - rInner, centerY - rInner, centerX + rInner, centerY + rInner);
        circleTextRectF.set(centerX - R_1_8 * 6, centerY - R_1_8 * 6, centerX + R_1_8 * 6, centerY + R_1_8 * 6);

        setMeasuredDimension(width, height);
    }

    /**
     * 返航点背景
     */
    private void initReturnPointPath() {
        returnPointBg.reset();
        float r = radius * 1.05f;
        float startAngle = 30;
        float sweepAngle = 20;
        Point point30 = calculatePointByAngle(startAngle, r);
        returnPointBg.moveTo(point30.x, point30.y);
        RectF ovl = new RectF(centerX - r, centerY - r, centerX + r, centerY + r);
        returnPointBg.addArc(ovl, startAngle, sweepAngle);
        Point point50 = calculatePointByAngle(startAngle + sweepAngle, r);
        returnPointBg.lineTo(point50.x, centerY + radius);
        returnPointBg.lineTo(point30.x + radiusReturnPointIcon * 2.5f, centerY + radius);
        returnPointBg.lineTo(point30.x + radiusReturnPointIcon * 2.5f, point30.y);
        returnPointBg.close();

        returnPointIconX = point30.x + radiusReturnPointIcon;
        returnPointIconY = point30.y + radiusReturnPointIcon * 1.5f;
        returnPointNumX = point50.x + (point30.x + radiusReturnPointIcon * 2.5f - point50.x) / 2f;
    }

    /**
     * 中心箭头
     */
    private void initIconPath() {
        arrowPath.reset();
        arrowPath.moveTo(centerX, centerY - centerArrowRadius);
        double radian = (90 - arrowTopAngle) * Math.PI / 180d;
        float bx = (float) (centerArrowRadius * Math.cos(radian));
        float by = (float) (centerArrowRadius * Math.sin(radian));
        arrowPath.lineTo(centerX + bx, centerY + by);
        arrowPath.lineTo(centerX, centerY + by / 2);
        arrowPath.lineTo(centerX - bx, centerY + by);
        arrowPath.close();

    }

    /**
     * 云台角度箭头
     */
    private void initGimbalPath() {
        gimbalDirection.reset();
        double radian = gimbalTopAngle / 2 * Math.PI / 180d;
        double tanA = Math.tan(radian);
        float x = (float) (tanA * (R_1_8 * 2 - pathPaintWidth * 2));
        gimbalDirection.moveTo(centerX, centerY - R_1_8 * 8 + pathPaintWidth);
        gimbalDirection.lineTo(centerX + x, centerY - R_1_8 * 6 - pathPaintWidth);
        gimbalDirection.lineTo(centerX, centerY - R_1_8 * 6.5f - pathPaintWidth);
        gimbalDirection.lineTo(centerX - x, centerY - R_1_8 * 6 - pathPaintWidth);
        gimbalDirection.close();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //顶部角度文字
        drawTopAngleText(canvas);

        //内部圆
        drawInnerCircle(canvas);

        //外部圆
        drawOuterCircle(canvas);

        //圆环线
        drawCircleBorder(canvas);

//        //绘制避障区死角
//        drawObstacleArea(canvas);

        //绘制进入避障区，避障区红色常量
        drawObstacleRedArea(canvas);

        //绘制云台方向
        drawGimbalDirection(canvas);

        textPaint.setTextSize(angleTextSize);
        //绘制角度文字
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        float halfHeight = Math.abs(ascent - descent) / 2f;
        drawAngleText(canvas, descent, halfHeight);

        //右下角返航点信息
        drawRightBottomReturnPointDistance(canvas, descent, halfHeight);

        //绘制水平线
        drawHorizontalLine(canvas);

        //绘制障碍点
        drawObstaclePoint(canvas);

        //绘制中心图标
        drawCenterArrowIcon(canvas);

        //绘制避障关闭后的4个OFF
        drawOffObstacleText(canvas);
    }

    private void drawGimbalDirection(Canvas canvas) {
        pathPaint.setColor(colorGimbalArrow);
        pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.save();
        canvas.rotate(gimbalAngle, centerX, centerY);
        canvas.drawPath(gimbalDirection, pathPaint);
        canvas.restore();
    }

    private void drawObstaclePoint(Canvas canvas) {
        for (ObstaclePoint obstaclePoint : obstaclePointList) {
            float distance = obstaclePoint.getDistance();
            float obstaclePointX = obstaclePoint.getX();
            float obstaclePointY = obstaclePoint.getY();
            PointF result;
            PointF pointF = transRealPosition2ViewPosition(obstaclePointX, obstaclePointY);
            int color;
            if (distance < 5) {
                color = 0;
                result = transPosition(pointF, R_1_8, R_1_8 * 2, scale2ViewBase(distance));
            } else if (distance >= 5 && distance < 10) {
                color = 1;
                result = transPosition(pointF, R_1_8 * 2, R_1_8 * 4, scale2ViewBase(distance));
            } else if (distance >= 10 && distance <= 40) {
                color = 2;
                result = transPosition(pointF, R_1_8 * 4, R_1_8 * 6, scale2ViewBase(distance));
            } else {
                continue;
            }
            obstaclePaint.setColor(getLevelColor(color));
            canvas.drawCircle(centerX + result.x, centerY - result.y, radiusObstaclePoint, obstaclePaint);
            if (showPointPosition) {
                textPaint.setTextSize(12);
                canvas.drawText("(" + obstaclePointX + "," + obstaclePointY + "," + df .format(obstaclePoint.getDistance()) + ")", centerX + result.x, centerY - result.y - 10, textPaint);
            }
        }
    }

    /**
     * 将实际障碍点坐标缩放为View上的坐标
     *
     * @param obstaclePointX 实际障碍点x
     * @param obstaclePointY 实际障碍点y
     */
    private PointF transRealPosition2ViewPosition(float obstaclePointX, float obstaclePointY) {
        float x = scale2ViewBase(obstaclePointX);
        float y = scale2ViewBase(obstaclePointY);
        return new PointF(x, y);
    }

    /**
     * 坐标点和距离映射，把实际坐标和距离映射到控件的尺度上
     * @param src
     * @return
     */
    private float scale2ViewBase(float src) {
        return 3 / 20f * src * R_1_8;
    }

    /**
     * @param src 原始坐标
     * @param r   内圈标系的半径
     * @param R   外圈坐标系半径
     * @param rn  原始坐标半径
     * @return
     */
    public PointF transPosition(PointF src, float r, float R, float rn) {
        PointF result = new PointF();
        float Rn;
        if (R == r) {
            Rn = rn / R;
        } else {
            Rn = (R - r) / R * rn + r;
        }
        result.x = Rn / rn * src.x;
        result.y = Rn / rn * src.y;
        return result;
    }

    private void drawHorizontalLine(Canvas canvas) {
        //右边线
        pathPaint.setColor(Color.WHITE);
        pathPaint.setStrokeWidth(pathPaintWidth);
        Point rightStart = calculatePointByAngle(dipAngle, R_1_8 * 9);
        Point rightEnd = calculatePointByAngle(dipAngle, centerX - R_1_8);
        horizonLine.moveTo(rightStart.x, rightStart.y);
        horizonLine.lineTo(rightEnd.x, rightEnd.y);
        canvas.drawPath(horizonLine, pathPaint);
        //左边线
        Point leftStart = calculatePointByAngle(dipAngle + 180, R_1_8 * 9);
        Point leftEnd = calculatePointByAngle(dipAngle + 180, centerX - R_1_8);
        horizonLine.moveTo(leftStart.x, leftStart.y);
        horizonLine.lineTo(leftEnd.x, leftEnd.y);
        canvas.drawPath(horizonLine, pathPaint);
        //重置路径
        horizonLine.reset();
    }

    private void drawCenterArrowIcon(Canvas canvas) {
        pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pathPaint.setColor(colorArrow);
        canvas.drawPath(arrowPath, pathPaint);
    }

    private void drawRightBottomReturnPointDistance(Canvas canvas, float descent, float halfHeight) {
        //背景
//        pathPaint.setColor(Color.RED);
//        canvas.drawPath(returnPointBg, pathPaint);
        //边框
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(2);
        pathPaint.setColor(colorReturnPointBorder);
        canvas.drawPath(returnPointBg, pathPaint);
        //返航点icon外圆
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4);
        circlePaint.setColor(colorTextBorder);
        //圆描边
        canvas.drawCircle(returnPointIconX, returnPointIconY, radiusReturnPointIcon, circlePaint);
        circlePaint.setStrokeWidth(2);
        circlePaint.setColor(colorReturnPointBorder);
        canvas.drawCircle(returnPointIconX, returnPointIconY, radiusReturnPointIcon, circlePaint);
        float dy = returnPointIconY + halfHeight;
        float by = dy - descent;
        textPaint.setStrokeWidth(2);
        textPaint.setColor(colorTextBorder);
        textPaint.setStyle(Paint.Style.STROKE);
        //文字H描边
        canvas.drawText("H", returnPointIconX, by, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(colorReturnPointBorder);
        //文字H
        canvas.drawText("H", returnPointIconX, by, textPaint);

        //返航点距离
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(2);
        textPaint.setColor(colorTextBorder);
        textPaint.setTextSize(returnTextSize);
        //绘制返航距离描边
        canvas.drawText(distance, returnPointNumX, centerY + radius - descent, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(colorReturnPointDistance);
        //绘制返航距离
        canvas.drawText(distance, returnPointNumX, centerY + radius - descent, textPaint);
    }

    private void drawAngleText(Canvas canvas, float descent, float halfHeight) {
        float vOffset = (R_1_8 * 2 - halfHeight) / 3;
        for (int i = 0; i < angles.length; i++) {
            //文字相对圆心的角度
            float currentAngel = i * 30 + (360 - angleOffset) - 90;
            //角度换算为弧度
            double radian = currentAngel * Math.PI / 180d;
            //文字相对圆心的位置
            float textXOffset = (float) (Math.cos(radian) * R_1_8 * 7);
            float textYOffset = (float) (Math.sin(radian) * R_1_8 * 7);
            //文字中心点坐标
            float xText = centerX + textXOffset;
            float yText = centerY + textYOffset;
            if (circleTextMode){
                if (i == 0) {
                    //正北N需要画背景
                    circlePaint.setStyle(Paint.Style.FILL);
                    circlePaint.setColor(Color.WHITE);
                    canvas.drawCircle(xText, yText, R_1_8 * 0.8f, circlePaint);
                    textPaint.setColor(colorArrow);
                } else {
                    textPaint.setColor(Color.WHITE);
                }
                circleTextPath.reset();
                circleTextPath.addArc(circleTextRectF, currentAngel-15, 30);
                canvas.drawTextOnPath(angles[i], circleTextPath, 0, -vOffset, textPaint);
            } else {
                if (i == 0) {
                    //正北N需要画背景
                    circlePaint.setStyle(Paint.Style.FILL);
                    circlePaint.setColor(Color.WHITE);
                    canvas.drawCircle(xText, yText, R_1_8 * 0.8f, circlePaint);
                    textPaint.setColor(colorArrow);
                } else {
                    textPaint.setColor(Color.WHITE);
                }
                float descentY = yText + halfHeight;
                float baseLineY = descentY - descent ;
                canvas.drawText(angles[i], xText, baseLineY, textPaint);
            }
        }
    }

    private void drawObstacleArea(Canvas canvas) {
        circlePaint.setColor(colorCircleInnerEnd);
        circlePaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 4; i++) {
            float startAngle = i * 90 + obstacleOffsetAngle - obstacleAngle / 2;
            canvas.drawArc(rectF, startAngle, obstacleAngle, true, circlePaint);
        }
    }

    private int currentObsState = -1;
    /**
     * obstacleState:
     *  0: 无盲区
     *  1: 前左盲区
     *  2: 前右盲区
     *  3: 后左盲区
     *  4: 后右盲区
     * @param canvas
     */
    private void drawObstacleRedArea(Canvas canvas){
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(colorCircleInnerEnd);
        canvas.drawArc(rectF, 180 + obstacleOffsetAngle - obstacleAngle / 2, obstacleAngle, true, currentObsState == 1?warningPaint:circlePaint);
        canvas.drawArc(rectF, 270 + obstacleOffsetAngle - obstacleAngle / 2, obstacleAngle, true, currentObsState == 2?warningPaint:circlePaint);
        canvas.drawArc(rectF, 90 + obstacleOffsetAngle - obstacleAngle / 2, obstacleAngle, true, currentObsState == 3?warningPaint:circlePaint);
        canvas.drawArc(rectF, obstacleOffsetAngle - obstacleAngle / 2, obstacleAngle, true, currentObsState == 4?warningPaint:circlePaint);
    }

    private void drawCircleBorder(Canvas canvas) {
        circlePaint.setStrokeWidth(widthCircleBorder);
        circlePaint.setColor(colorCircleBorder);
        for (int i = 0; i < 4; i++) {
            canvas.drawCircle(centerX, centerY, R_1_8 * 2 * (i + 1), circlePaint);
        }
    }

    private void drawOuterCircle(Canvas canvas) {
        circlePaint.setColor(colorCircleOuter);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(R_1_8 * 2);
        canvas.drawCircle(centerX, centerY, R_1_8 * 7, circlePaint);
    }

    private void drawInnerCircle(Canvas canvas) {
        circlePaint.setColor(colorCircleInnerCenter);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setShader(radialGradient);
        canvas.drawCircle(centerX, centerY, R_1_8 * 6, circlePaint);
        circlePaint.setShader(null);
    }

    private void drawTopAngleText(Canvas canvas) {
        float topTextDy = centerY - R_1_8 * 8;
        float topTextBy = topTextDy - topTextDescent;
        canvas.drawText((int) angleOffset + "°", centerX, topTextBy, topTextPaint);
    }

    private int getLevelColor(int level) {
        int color = Color.GREEN;
        switch (level) {
            case 0:
                color = Color.RED;
                break;
            case 1:
                color = Color.YELLOW;
                break;
            case 2:
                color = Color.BLUE;
                break;
        }
        return color;
    }

    public void setShowPositionInfo(boolean showPointPosition){
        this.showPointPosition = showPointPosition;
    }

    /**
     * 设置航向角
     *
     * @param angle 航向角
     */
    public void setHeadingAngle(float angle) {
        if (!enable) {
            return;
        }
        if (angle < 0 || angle > 360) {
            return;
        }
        //灵敏度处理
        if (Math.abs(angle - angleOffset) < ANGLE_CHANGED_SENSITIVITY) {
            return;
        }
        this.angleOffset = angle;
        invalidate();
    }

    /**
     * 设置水平倾角
     *
     * @param angle 角度值
     */
    public void setHorizontalDipAngle(float angle) {
        if (!enable) {
            return;
        }
        if (angle < -180 || angle > 180) {
            return;
        }
        if (angle == dipAngle) {
            return;
        }
        this.dipAngle = angle;
        invalidate();
    }

    /**
     * 设置返航点的距离
     *
     * @param distance
     */
    public void setReturnDistance(String distance) {
        if (!enable) {
            return;
        }
        if (TextUtils.equals(distance,this.distance)) {
            return;
        }
        this.distance = distance;
        invalidate();
    }

    /**
     * 设置障碍点的最大探测范围
     *
     * @param maxDistance 障碍点最大探测范围
     */
    public void setObstacleMax(float maxDistance) {
        float viewMaxDistance = R_1_8 * 6;
        this.maxDistance = maxDistance;
        if (viewMaxDistance != 0) {
            distanceRate = viewMaxDistance / maxDistance;
        }
    }

    public void setObsState(int obsState){
        if (currentObsState == obsState){
            return;
        }
        currentObsState = obsState;
        invalidate();
    }

    /**
     * 设置障碍点并刷新
     *
     * @param list 障碍点坐标集合
     */
    public void setObstacle(List<ObstaclePoint> list) {
        if (!enable) {
            return;
        }
        obstaclePointList.clear();
        obstaclePointList.addAll(list);
        invalidate();
    }

    /**
     * 设置障碍点并刷新
     * @param list 障碍点坐标集合
     * @param clear 多久后障碍点消失
     */
    public void setObstacle(List<ObstaclePoint> list,long clear){
        setObstacle(list);
        if (radarHandler.hasMessages(MSG_CLEAR_POINTS)){
            radarHandler.removeMessages(MSG_CLEAR_POINTS);
            radarHandler.sendEmptyMessageDelayed(MSG_CLEAR_POINTS,clear);
        }
    }

    /**
     * 清除绘制的障碍点
     */
    public void clearObstacle() {
        obstaclePointList.clear();
        invalidate();
    }

    public void setGimbalAngle(float angle) {
        if (!enable) {
            return;
        }
        if (angle == gimbalAngle) {
            return;
        }
        this.gimbalAngle = angle;
        invalidate();
    }

    /**
     * 通过角度和半径计算坐标
     *
     * @param angle  相对x轴的角度
     * @param radius 半径
     * @return 结果坐标点
     */
    private Point calculatePointByAngle(float angle, float radius) {
        //角度换算为弧度
        double radian = angle * Math.PI / 180d;
        double x = Math.cos(radian) * radius;
        double y = Math.sin(radian) * radius;
        return new Point((int) (centerX + x), (int) (centerY + y));
    }

    /**
     * 根据视觉避障开关是否话四个OFF字样
     */
    private void drawOffObstacleText(Canvas canvas){
        if (!isShowObstacleOFF){
            return;
        }
        final Paint.FontMetrics mFontMetrics = stateTextPaint.getFontMetrics();
        float R_1_2 = R_1_8 * 4;
        String text = "OFF";
        //左方
        canvas.drawText(text, centerX - R_1_2, getBaseLineByTextCenterY(centerY,mFontMetrics) , stateTextPaint);
        //上方
        canvas.drawText(text,centerX,getBaseLineByTextCenterY(centerY - R_1_2,mFontMetrics),stateTextPaint);
        //右方
        canvas.drawText(text, centerX + R_1_2, getBaseLineByTextCenterY(centerY,mFontMetrics) , stateTextPaint);
        //下方
        canvas.drawText(text, centerX, getBaseLineByTextCenterY(centerY + R_1_2,mFontMetrics), stateTextPaint);
    }

    private float getBaseLineByTextCenterY(float textCenterY,Paint.FontMetrics mFontMetrics){
        float textHeight = mFontMetrics.bottom - mFontMetrics.top;
        return textCenterY + textHeight/2 - mFontMetrics.bottom;
    }



    private float getTextWidth(String text,Paint paint) {
        if(!TextUtils.isEmpty(text) && paint != null){
            return paint.measureText(text);
        }
        return 0;
    }

    /**
     * 避障死角角度
     * @param angle
     */
    public void setObstacleDisableAngle(float angle){
        if (angle < 0 || angle > 90){
            return;
        }
        obstacleAngle = angle;
        postInvalidate();
    }

    public void setCircleTextMode(boolean circleTextModeEnable){
        if (!enable){
            return;
        }
        if (circleTextMode == circleTextModeEnable){
            return;
        }
        circleTextMode = circleTextModeEnable;
        invalidate();

    }

    public boolean isCircleTextMode(){
        return circleTextMode;
    }

    public void switchCircleTextMode(){
        setCircleTextMode(!circleTextMode);
    }

    public void setShowObstacleOFF(boolean isShow){
        if (isShowObstacleOFF == isShow){
            return;
        }
        isShowObstacleOFF = isShow;
        invalidate();
    }

    private final int MSG_CLEAR_POINTS = 101;
    private final Handler radarHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CLEAR_POINTS:
                    clearObstacle();
                    break;
            }
        }
    };

}
