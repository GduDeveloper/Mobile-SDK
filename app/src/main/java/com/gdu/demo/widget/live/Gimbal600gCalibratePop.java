package com.gdu.demo.widget.live;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.gdu.demo.R;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class Gimbal600gCalibratePop extends GduBasePop implements View.OnClickListener {

    private TextView mTvTop;
    private TextView mTvBottom;
    private TextView mTvLeft;
    private TextView mTvRight;
    private TextView mTvSave;
    private EditText mEtMegaphone;
    private EditText mEtAzimuth;
    private ImageView mIvClose;
    private TextView mTvSet;

    public Gimbal600gCalibratePop(Context context, int w, int h) {
        super(context, w, h);
        setAnimationStyle(R.style.gyro_calibrate_pop);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.pop_600g_calibrate;
    }

    @Override
    protected void initListener() {
        mIvClose.setOnClickListener(this);
        mTvTop.setOnClickListener(this);
        mTvBottom.setOnClickListener(this);
        mTvLeft.setOnClickListener(this);
        mTvRight.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mTvSet.setOnClickListener(this);
    }

    @Override
    public void initView() {
        mIvClose = findViewById(R.id.iv_close);
        mTvTop = findViewById(R.id.tv_top);
        mTvBottom = findViewById(R.id.tv_bottom);
        mTvLeft = findViewById(R.id.tv_left);
        mTvRight = findViewById(R.id.tv_right);
        mTvSave = findViewById(R.id.tv_save);
        mTvSet = findViewById(R.id.tv_set);
        mEtMegaphone = findViewById(R.id.et_gimbal_angle_megaphone);
        mEtAzimuth = findViewById(R.id.et_gimbal_angle_azimuth);
        mEtAzimuth.addTextChangedListener(new DecimalLimitWatcher(mEtAzimuth));
        mEtMegaphone.addTextChangedListener(new DecimalLimitWatcher(mEtMegaphone));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.tv_top:
                selectTopBottom(true);
                break;
            case R.id.tv_bottom:
                selectTopBottom(false);
                break;
            case R.id.tv_left:
                selectLeftRight(true);
                break;
            case R.id.tv_right:
                selectLeftRight(false);
                break;
            case R.id.tv_set:
                setCalibrateValue(false);
                break;
            case R.id.tv_save:
                setCalibrateValue(true);
                break;
        }
    }

    private void setCalibrateValue(boolean isSave) {
        boolean left = mTvLeft.isSelected();
        boolean right = mTvRight.isSelected();
        boolean top = mTvTop.isSelected();
        boolean bottom = mTvBottom.isSelected();

        short azimuthValue = 0;
        short megaphoneValue = 0;
        if (left || right) {
            String azimuthStr = mEtAzimuth.getText().toString().trim();
            if (TextUtils.isEmpty(azimuthStr)) {
                azimuthValue = 0;
            }else {
                azimuthValue = (short) (Float.parseFloat(azimuthStr) * 10);
                if (left) {
                    azimuthValue = (short) -azimuthValue;
                }
            }
        }
        if (top || bottom) {
            String megaphoneStr = mEtMegaphone.getText().toString().trim();
            if (TextUtils.isEmpty(megaphoneStr)) {
                megaphoneValue = 0;
            }else {
                megaphoneValue = (short) (Float.parseFloat(megaphoneStr) * 10);
                if (bottom) {
                    megaphoneValue = (short) -megaphoneValue;
                }
            }
        }

//        GduApplication.getSingleApp().gduCommunication.calibrate600gGimbalMountAngel(isSave,
//                megaphoneValue, azimuthValue, (code, bean) -> {
//                    Observable.just("")
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(result -> {
//                                Toaster.show(mContext.getString(code == 0 ?
//                                        R.string.Label_SettingSuccess : R.string.Label_SettingFail));
//                            });
//                });
    }

    public void selectTopBottom(boolean isTop) {
        mTvTop.setSelected(isTop);
        mTvBottom.setSelected(!isTop);
    }

    public void selectLeftRight(boolean isLeft) {
        mTvLeft.setSelected(isLeft);
        mTvRight.setSelected(!isLeft);
    }

    public class DecimalLimitWatcher implements TextWatcher {

        private final EditText mEditText;

        public DecimalLimitWatcher(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null) {
                String string = s.toString();
                if (string.contains(".")) {
                    String[] split = string.split("\\.");
                    if (split.length == 2) {
                        String endDecimal = split[1];
                        if (endDecimal.length() > 1) {
                            mEditText.setText(string.substring(0, split[0].length() + 2));
                            mEditText.setSelection(split[0].length() + 2);
                        }
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
