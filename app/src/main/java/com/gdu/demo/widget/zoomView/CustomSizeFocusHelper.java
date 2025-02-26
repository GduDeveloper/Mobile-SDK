package com.gdu.demo.widget.zoomView;


public abstract class CustomSizeFocusHelper {
    protected CustomVerticalRangeSeekBar mRangeSeekBar;
    /**
     * 标识当前是否在手动设置变焦
     */
    protected boolean isManualSetFocus = false;

    public CustomSizeFocusHelper(CustomVerticalRangeSeekBar rangeSeekBar) {
        mRangeSeekBar = rangeSeekBar;
        initView();
        initListener();
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected float getCurFocusValue(float leftValue) {
        final float partProgress = getPartProgress();
        return leftValue / partProgress + 1;
    }

    protected float getPartProgress() {
        final float maxProgress = mRangeSeekBar.getMaxProgress();
        final int mSteps = mRangeSeekBar.getSteps();
        return maxProgress / mSteps;
    }

    /**
     * 依据相机当前倍率，同步进度条显示
     * @param size 相机变焦倍率
     */
    public abstract void updateCurFocusSize(float size);
}
