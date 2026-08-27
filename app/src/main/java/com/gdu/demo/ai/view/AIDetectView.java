package com.gdu.demo.ai.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gdu.demo.R;
import com.gdu.msdk.key.value.ai.TargetMode;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AI检测画框View
 */
public class AIDetectView extends View {

    private final List<TargetMode> targetList = new CopyOnWriteArrayList<>();
    private final RectF mRectF = new RectF();
    private Paint mPaint;
    private Paint mTextPaint;
    private float textPaintBottom = 0;
//    private boolean isVisible = false;

    public AIDetectView(Context context) {
        super(context);
        initView(context, null);
    }

    public AIDetectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.dp_1));
        mPaint.setColor(ContextCompat.getColor(context, R.color.color_60e353));
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(ContextCompat.getColor(context, R.color.color_60e353));
        mTextPaint.setTextSize(context.getResources().getDimension(R.dimen.sp_10));
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        textPaintBottom = fontMetrics.bottom;
//        isVisible = getVisibility() == VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!targetList.isEmpty()) {
            for (TargetMode targetMode : targetList) {
                short leftX = targetMode.getLeftX();
                short leftY = targetMode.getLeftY();
                short width = targetMode.getWidth();
                short height = targetMode.getHeight();
                mRectF.set(leftX, leftY, leftX + width, leftY + height);
                canvas.drawRect(mRectF, mPaint);
                float baseLine = leftY - textPaintBottom;
                String targetName = targetMode.getTargetName();
//                canvas.drawText(targetName + targetMode.getId(),leftX,baseLine,mTextPaint);
                //20260119 根据薛总演示后，视觉黄和产品李同步，去掉目标id显示
                canvas.drawText(targetName == null ? "" : targetName ,leftX,baseLine,mTextPaint);
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
//        isVisible = visibility == VISIBLE;
    }

    public void setTargetList(List<TargetMode> list) {
//        if (!isVisible){
//            return;
//        }
        targetList.clear();
        if (list != null && !list.isEmpty()) {
            targetList.addAll(list);
        }
        invalidate();
    }

    public void removeTargetViewByType(int type) {
//        if (!isVisible){
//            return;
//        }
        if (targetList.isEmpty()) {
            return;
        }
        targetList.removeIf(next -> next.getType() == type);
        invalidate();
    }

    public void clearView() {
//        if (!isVisible){
//            return;
//        }
        if (targetList.isEmpty()){
            return;
        }
        targetList.clear();
        invalidate();
    }

}
