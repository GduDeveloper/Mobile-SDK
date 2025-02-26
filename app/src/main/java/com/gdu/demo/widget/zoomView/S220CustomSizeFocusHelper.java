package com.gdu.demo.widget.zoomView;

import android.util.Log;
import android.view.View;

import com.gdu.common.error.GDUError;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.gimbal.GDUGimbal;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class S220CustomSizeFocusHelper extends CustomSizeFocusHelper {

    private GDUCamera mGduCamera;
    private Disposable disposable;

    public S220CustomSizeFocusHelper(CustomVerticalRangeSeekBar rangeSeekBar) {
        super(rangeSeekBar);
    }

    private int sendAndReceiveCmdNum = 0;
    /** 电子变倍开始的值 */
    private final int ELECTRON_ZOOM_START_SIZE = 10;

    @Override
    protected void initView() {
        MyLogUtils.i("initView()");
        mRangeSeekBar.setRange(0, 1000);
        mRangeSeekBar.setSteps(24);
        mRangeSeekBar.getLeftSeekBar().setThumbText("1.0X");

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        updateZoom();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateZoom() {
        if (mGduCamera == null) {
            mGduCamera = (GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera();
        }
        if (mGduCamera == null) {
            return;
        }
        boolean isSupport = mGduCamera.isDigitalZoomSupported();
        if (isSupport) {
            mRangeSeekBar.setVisibility(View.VISIBLE);
        } else {
            mRangeSeekBar.setVisibility(View.GONE);
        }
        float currentZoom = mGduCamera.getCurrentZoom();
        updateCurFocusSize(currentZoom);
    }

    @Override
    protected void initListener() {
        MyLogUtils.i("initListener()");
        mRangeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                MyLogUtils.i("onRangeChanged() leftValue = " + leftValue + "; isFromUser = " + isFromUser);
                final float mFocusSize = getCurFocusValue(leftValue);
                String formatSizeStr = "";
                if (mFocusSize <= ELECTRON_ZOOM_START_SIZE) {
                    mRangeSeekBar.setStepsAutoBonding(false);
                    BigDecimal mBigDecimal = new BigDecimal(mFocusSize);
                    mBigDecimal = mBigDecimal.setScale(1, RoundingMode.HALF_UP);
                    formatSizeStr = mBigDecimal.toPlainString() + "X";
                } else if (mFocusSize > ELECTRON_ZOOM_START_SIZE) {
                    mRangeSeekBar.setStepsAutoBonding(false);
                    float size = ELECTRON_ZOOM_START_SIZE + (mFocusSize - ELECTRON_ZOOM_START_SIZE) * ELECTRON_ZOOM_START_SIZE;
                    BigDecimal mBigDecimal = new BigDecimal(size);
                    mBigDecimal = mBigDecimal.setScale(1, RoundingMode.HALF_UP);
                    formatSizeStr = mBigDecimal.toPlainString() + "X";
                }
                MyLogUtils.i("onRangeChanged() mFocusSize = " + mFocusSize + "; formatSizeStr = " + formatSizeStr);
                if (CommonUtils.isEmptyString(formatSizeStr)) {
                    return;
                }
                mRangeSeekBar.getLeftSeekBar().setThumbText(formatSizeStr);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                MyLogUtils.i("onStartTrackingTouch() isLeft = " + isLeft);
                isManualSetFocus = true;
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                MyLogUtils.i("onStopTrackingTouch() isLeft = " + isLeft);
                if (!isLeft) {
                    return;
                }
                sendFocusCmd(view);
            }
        });
    }

    @Override
    public void updateCurFocusSize(float size) {
        Log.d("updateCurFocusSize", "updateCurFocusSize() size = " + size);
        if (mRangeSeekBar == null || isManualSetFocus || size < 0) {
            return;
        }
        final float partProgress = getPartProgress();
        final float mFocusSize = getCurFocusValue(mRangeSeekBar.getLeftSeekBar().getProgress());
        if (size > ELECTRON_ZOOM_START_SIZE) {
            size = (size - ELECTRON_ZOOM_START_SIZE) / ELECTRON_ZOOM_START_SIZE + ELECTRON_ZOOM_START_SIZE;
        }
        MyLogUtils.i("updateCurFocusSize() partProgress = " + partProgress + "; mFocusSize = " + mFocusSize + "; after size = " + size);
        final BigDecimal bd1 = new BigDecimal(String.valueOf(size));
        final BigDecimal bd2 = new BigDecimal(String.valueOf(mFocusSize));
        if (bd1.compareTo(bd2) == 0) {
            return;
        }
        final float progressValue = (size - 1) * partProgress;
        MyLogUtils.i("updateCurFocusSize() progressValue = " + progressValue);
        mRangeSeekBar.setProgress(progressValue);
    }

    private void sendFocusCmd(RangeSeekBar view) {
        MyLogUtils.i("sendFocusCmd()");
        try {
            if (view == null) {
                return;
            }
            final float ratioValue = Float.parseFloat(view.getLeftSeekBar().getUserText2Thumb().replace("X", ""));
            MyLogUtils.i("sendFocusCmd() ratioValue = " + ratioValue);
            sendAndReceiveCmdNum += 1;
            zoomCustomSizeRatio((short) (ratioValue));
        } catch (Exception e) {
            MyLogUtils.e("解析变焦倍数出错", e);
        }
    }


    /**
     * 变倍
     * @param ratio
     */
    private void zoomCustomSizeRatio(final short ratio) {
        Log.d("zoomCustomSizeRatio","zoomCustomSizeRatio() ratio = " + ratio);
        if (mGduCamera != null) {
            mGduCamera.setZoom(ratio, error -> {

            });
        }
    }

    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

}
