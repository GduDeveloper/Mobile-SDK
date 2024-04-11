package com.gdu.demo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SuperSurfaceView  extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;

    public SuperSurfaceView(Context context) {
        super(context);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new MyThread()).start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class MyThread implements Runnable {
        @Override
        public void run() {
            Canvas canvas = surfaceHolder.lockCanvas(null);//获取画布
            Paint mPaint = new Paint();
            mPaint.setColor(Color.BLUE);
            canvas.drawRect(new RectF(100, 100, 1000, 550), mPaint);
            surfaceHolder.unlockCanvasAndPost(canvas);//解锁画布，提交画好的图像

        }
    }
}
