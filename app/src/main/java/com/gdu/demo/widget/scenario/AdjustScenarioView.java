package com.gdu.demo.widget.scenario;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flyco.tablayout.SegmentTabLayout;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.util.ThreadHelper;


public class AdjustScenarioView extends RelativeLayout {

    private SegmentTabLayout tabAdjust;
    private TextView tv_sensitivity_title;

//    private final String[] mTitles;
    private View vAdjustExp;
    private View vAdjustSensitivity;
//    private EXPCoordinateSystemView expUpDown;
//    private EXPCoordinateSystemView expRightLeft;
//    private EXPCoordinateSystemView expForwardBack;
    private EditText etExpForwardBack;
    private EditText etExpRightLeft;
    private EditText etExpUpDown;
    public static final int SETTING_SENSITIVITY_SUCCEED = 3;
    private SeekBar sbStatePose;
    private SeekBar sbLift;
    private SeekBar sbCourse;
    private SeekBar sbBrake;
    private EditText etStatePoseValue;
    private EditText etLiftValue;
    private EditText etCourseValue;
    private EditText etBrakeValue;

    private Double upDownExp = 0.5;
    private Double rightLeftExp = 0.5;
    private Double forWardBackExp = 0.5;

    private TextView tv_reset_sensitivity;
    private TextView tv_reset_exp;
    private int statePose;
    private int lift;
    private int course;
    private int brake;

    private TextView tv_state_pose_range;
    private TextView tv_lift_range;
    private TextView tv_course_range;
    private TextView tv_brake_range;



    public AdjustScenarioView(Context context) {
        this(context, null);
    }

    public AdjustScenarioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdjustScenarioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        if (UavStaticVar.isOpenTextEnvironment) {
//            mTitles = new String[]{"EXP", getResources().getString(R.string.sensitivity)};
//        } else {
//            mTitles = new String[]{getResources().getString(R.string.sensitivity)};
//        }

        initView(context);
        initEXPValue();
        initSensitivityValue();
        initListener();
    }

    private void initEXPValue() {
//        if(GduApplication.getSingleApp().gduCommunication==null){
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.getEXP((code, bean) -> {
//            if (code == GduConfig.OK) {
//                byte[] frameContent = bean.frameContent;
//                ThreadHelper.runOnUiThread(() -> {
//                    parseExpValue(frameContent);
//                });
//            }
//        });
    }

    private void initSensitivityValue() {
//        if (GduApplication.getSingleApp().gduCommunication == null) {
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.getSensitivity((code, bean) -> {
//            if (code == GduConfig.OK) {
//                byte[] frameContent = bean.frameContent;
//                ThreadHelper.runOnUiThread(() -> {
//                    parseSensitivityValue(frameContent);
//                });
//            }
//        });
    }

    /**
     * 解析EXP的数据并赋值
     *
     * @param frameContent ack payload
     */
    private void parseExpValue(byte[] frameContent) {
        if (frameContent.length < 5) {
            return;
        }
        int index = 2;
        upDownExp = Double.valueOf((frameContent[index++] / 100f));
        rightLeftExp = Double.valueOf(frameContent[index++] / 100f);
        forWardBackExp = Double.valueOf(frameContent[index] / 100f);

//        expUpDown.setExp(upDownExp);
//        expRightLeft.setExp(rightLeftExp);
//        expForwardBack.setExp(forWardBackExp);

        etExpUpDown.setText(String.valueOf(upDownExp));
        etExpRightLeft.setText(String.valueOf(rightLeftExp));
        etExpForwardBack.setText(String.valueOf(forWardBackExp));
    }

    private void parseSensitivityValue(byte[] frameContent) {
        if (frameContent.length < 6) {
            return;
        }
        int index = 2;
        statePose = disposeByte(frameContent[index++]);
        lift = disposeByte(frameContent[index++]);
        course = disposeByte(frameContent[index++]);
        brake = disposeByte(frameContent[index]);

        sbStatePose.setProgress(statePose);
        sbLift.setProgress(lift);
        sbCourse.setProgress(course);
        sbBrake.setProgress(brake);
    }

    /**
     * 因为C存在无整形，如果为负数加256获取实际数据
     *
     * @param b byte
     * @return 转换后的byte
     */
    private int disposeByte(byte b) {
        if (b < 0) {
            return b + 256;
        }
        return b;
    }

    private void initView(Context context) {
//        View view = LayoutInflater.from(context).inflate(R.layout.view_adjust_scenario, this, true);
//        tabAdjust = view.findViewById(R.id.tab_adjust);
//        this.vAdjustExp = view.findViewById(R.id.v_adjust_exp);
//        this.vAdjustSensitivity = view.findViewById(R.id.v_adjust_sensitivity);
//        tv_sensitivity_title = view.findViewById(R.id.tv_sensitivity_title);
//        initEXPView(vAdjustExp);
//        initSensitivity(vAdjustSensitivity);

//        tabAdjust.setTabData(mTitles);
//        tabAdjust.setCurrentTab(0);
//
//        if (UavStaticVar.isOpenTextEnvironment) {
//            vAdjustExp.setVisibility(VISIBLE);
//            vAdjustSensitivity.setVisibility(GONE);
//            tabAdjust.setVisibility(VISIBLE);
//            tv_sensitivity_title.setVisibility(GONE);
//        } else {
//            vAdjustExp.setVisibility(GONE);
//            vAdjustSensitivity.setVisibility(VISIBLE);
//            tabAdjust.setVisibility(GONE);
//            tv_sensitivity_title.setVisibility(VISIBLE);
//        }
    }

    /**
     * 初始化EXP相关控件
     *
     * @param vAdjustExp EXPView
     */
    private void initEXPView(View vAdjustExp) {
//        expUpDown = vAdjustExp.findViewById(R.id.exp_up_down);
//        expRightLeft = vAdjustExp.findViewById(R.id.exp_right_left);
//        expForwardBack = vAdjustExp.findViewById(R.id.exp_forward_back);
//        etExpUpDown = vAdjustExp.findViewById(R.id.et_exp_up_down);
//        etExpRightLeft = vAdjustExp.findViewById(R.id.et_exp_right_left);
//        etExpForwardBack = vAdjustExp.findViewById(R.id.et_exp_forward_back);

//        tv_reset_exp = vAdjustExp.findViewById(R.id.tv_reset_exp);

//        tv_reset_exp.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                resetExp();
//            }
//        });
    }

    private void resetExp() {
//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
//            Toaster.show(getResources().getString(R.string.DeviceNoConn));
//            return;
//        }
//        upDownExp = 0.5;
//        etExpUpDown.setText(String.valueOf(upDownExp));
//        expUpDown.setExp(upDownExp);
//        rightLeftExp = 0.5;
//        etExpRightLeft.setText(String.valueOf(rightLeftExp));
//        expRightLeft.setExp(rightLeftExp);
//        forWardBackExp = 0.5;
//        etExpForwardBack.setText(String.valueOf(forWardBackExp));
//        expForwardBack.setExp(forWardBackExp);
//        sendExpToFlight();
    }

    @SuppressLint("SetTextI18n")
    private void initSensitivity(View vAdjustSensitivity) {
//        sbStatePose = vAdjustSensitivity.findViewById(R.id.sb_state_pose);
//        sbLift = vAdjustSensitivity.findViewById(R.id.sb_lift);
//        sbCourse = vAdjustSensitivity.findViewById(R.id.sb_course);
//        sbBrake = vAdjustSensitivity.findViewById(R.id.sb_brake);
//
//        etStatePoseValue = vAdjustSensitivity.findViewById(R.id.et_state_pose_value);
//        etLiftValue = vAdjustSensitivity.findViewById(R.id.et_lift_value);
//        etCourseValue = vAdjustSensitivity.findViewById(R.id.et_course_value);
//        etBrakeValue = vAdjustSensitivity.findViewById(R.id.et_brake_value);
//        tv_reset_sensitivity = vAdjustSensitivity.findViewById(R.id.tv_reset_sensitivity);
//        //初始化text值
//        etStatePoseValue.setText(sbStatePose.getProgress() + "");
//        etLiftValue.setText(sbLift.getProgress() + "");
//        etCourseValue.setText(sbCourse.getProgress() + "");
//        etBrakeValue.setText(sbBrake.getProgress() + "");
//
//        statePose = sbStatePose.getProgress();
//        lift = sbLift.getProgress();
//        course = sbCourse.getProgress();
//        brake = sbBrake.getProgress();
//
//        tv_state_pose_range = findViewById(R.id.tv_state_pose_range);
//        tv_lift_range = findViewById(R.id.tv_lift_range);
//        tv_course_range = findViewById(R.id.tv_course_range);
//        tv_brake_range = findViewById(R.id.tv_brake_range);
//
//
//        tv_reset_sensitivity.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                resetSensitivity();
//            }
//        });
//
//        if(CommonUtils.curPlanIsSmallFlight()){
//            tv_state_pose_range.setText("(80%-120%)");
//            sbStatePose.setMax(120);
//            tv_lift_range.setText("(80%-120%)");
//            sbLift.setMax(120);
//            tv_course_range.setText("(50%-200%)");
//            sbCourse.setMax(200);
//            tv_brake_range.setText("(80%-120%)");
//            sbBrake.setMax(120);
//
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            sbStatePose.setMin(30);
//            sbLift.setMin(30);
//            sbCourse.setMin(30);
//            sbBrake.setMin(60);
//            if(CommonUtils.curPlanIsSmallFlight()) {
//                sbStatePose.setMin(80);
//                sbLift.setMin(80);
//                sbCourse.setMin(50);
//                sbBrake.setMin(80);
//            }
//        }

    }

    private void resetSensitivity() {

//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
//            Toaster.show(getResources().getString(R.string.DeviceNoConn));
//            return;
//        }
//        sbStatePose.setProgress(100);
//        sbLift.setProgress(100);
//        sbCourse.setProgress(100);
//        sbBrake.setProgress(100);
    }

    private void initListener() {

//        tabAdjust.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelect(int position) {
//                if (UavStaticVar.isOpenTextEnvironment) {
//                    vAdjustExp.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
//                    vAdjustSensitivity.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
//                }
//            }
//
//            @Override
//            public void onTabReselect(int position) {
//
//            }
//        });
//
//        initEXPViewListener();
//        initSensitivityViewListener();

    }

    /**
     * 初始化EXP控件相关的监听
     */
    private void initEXPViewListener() {
        //---------------------------EditText完成输入监听---------------------------------------------
//        etExpUpDown.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etExpUpDown.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    return true;
//                }
//                checkAndSetEXP(etExpUpDown, expUpDown,1);
//                etExpUpDown.clearFocus();
//            }
//            return false;
//
//        });
//
//        etExpRightLeft.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etExpRightLeft.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    return true;
//                }
//                checkAndSetEXP(etExpRightLeft, expRightLeft,2);
//                etExpRightLeft.clearFocus();
//            }
//            return false;
//
//        });
//
//        etExpForwardBack.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etExpForwardBack.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    return true;
//                }
//                checkAndSetEXP(etExpForwardBack, expForwardBack,3);
//                etExpForwardBack.clearFocus();
//            }
//            return false;
//        });
//
//        //--------------------------------EXP控件监听拖动---------------------------------------------
//        expUpDown.setExpValueChangeListener((exp, isFromUser) -> {
//            if (isFromUser) {
//                upDownExp = exp;
//                etExpUpDown.setText(String.valueOf(exp));
//                sendExpToFlight();
//            }
//        });
//        expRightLeft.setExpValueChangeListener((exp, isFromUser) -> {
//            if (isFromUser) {
//                rightLeftExp = exp;
//                etExpRightLeft.setText(String.valueOf(exp));
//                sendExpToFlight();
//            }
//        });
//        expForwardBack.setExpValueChangeListener((exp, isFromUser) -> {
//            if (isFromUser) {
//                forWardBackExp = exp;
//                etExpForwardBack.setText(String.valueOf(exp));
//                sendExpToFlight();
//            }
//        });
        
        

    }

//    /**
//     * 检查EXP数据并设置
//     *
//     * @param editText 编辑框
//     * @param expView  EXPView
//     */
//    private void checkAndSetEXP(EditText editText, EXPCoordinateSystemView expView, int type) {
//
//        double value = Double.parseDouble(editText.getText().toString());
//        if (value < 0.2 || value > 0.8) {
//            Toaster.show(getContext().getString(R.string.string_value_in_right_range));
//            if (type == 1) {
//                editText.setText(String.valueOf(upDownExp));
//            } else if (type == 2) {
//                editText.setText(String.valueOf(rightLeftExp));
//            } else if (type == 3) {
//                editText.setText(String.valueOf(forWardBackExp));
//            }
//            return;
//        }
//        if (type == 1) {
//            upDownExp = value;
//        } else if (type == 2) {
//            rightLeftExp = value;
//        } else if (type == 3) {
//            forWardBackExp = value;
//        }
//        expView.setExp(value);
//        sendExpToFlight();
//    }
//
//
//    /**
//     * 初始化灵敏度界面的监听
//     */
//    @SuppressLint("SetTextI18n")
//    private void initSensitivityViewListener() {
//        sbStatePose.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                int min = 30;
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    min = 80;
//                }
//                checkAndSetSensitivityValue(seekBar, etStatePoseValue, min, progress);
//                //如果是通过EditText设置数值则在changed里面发送数值
//                if (!fromUser) {
//                    sendSensitivityToFlight();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                sendSensitivityToFlight();
//            }
//        });
//
//        sbLift.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                int min = 30;
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    min = 80;
//                }
//                checkAndSetSensitivityValue(seekBar, etLiftValue, min, progress);
//                //如果是通过EditText设置数值则在changed里面发送数值
//                if (!fromUser) {
//                    sendSensitivityToFlight();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                sendSensitivityToFlight();
//            }
//        });
//
//        sbCourse.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                int min = 30;
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    min = 50;
//                }
//                checkAndSetSensitivityValue(seekBar, etCourseValue, min, progress);
//                //如果是通过EditText设置数值则在changed里面发送数值
//                if (!fromUser) {
//                    sendSensitivityToFlight();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                sendSensitivityToFlight();
//            }
//        });
//
//        sbBrake.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                int min = 60;
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    min = 80;
//                }
//                checkAndSetSensitivityValue(seekBar, etBrakeValue, min, progress);
//                //如果是通过EditText设置数值则在changed里面发送数值
//                if (!fromUser) {
//                    sendSensitivityToFlight();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                sendSensitivityToFlight();
//            }
//        });
//        //--------------------------------------EditText完成监听-------------------------------------
//        etStatePoseValue.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etStatePoseValue.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    etStatePoseValue.setText(statePose + "");
//                    return true;
//                }
//                if (Integer.parseInt(inputStr) > sbStatePose.getMax()) {
//                    etStatePoseValue.setText(sbStatePose.getMax() + "");
//                }
//                sbStatePose.setProgress(Integer.parseInt(v.getText().toString()));
//
//            }
//            return false;
//        });
//
//        etLiftValue.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etLiftValue.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    etLiftValue.setText(lift + "");
//                    return true;
//                }
//                if (Integer.parseInt(inputStr) > sbLift.getMax()) {
//                    etLiftValue.setText(sbLift.getMax() + "");
//                }
//                sbLift.setProgress(Integer.parseInt(v.getText().toString()));
//            }
//            return false;
//        });
//
//        etCourseValue.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etCourseValue.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    etCourseValue.setText(course + "");
//                    return true;
//                }
//                if (Integer.parseInt(inputStr) > sbCourse.getMax()) {
//                    etCourseValue.setText(sbCourse.getMax() + "");
//
//                }
//                sbCourse.setProgress(Integer.parseInt(v.getText().toString()));
//            }
//            return false;
//        });
//
//        etBrakeValue.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                String inputStr = etBrakeValue.getText().toString();
//                if (CommonUtils.isEmptyString(inputStr)) {
//                    Toaster.show(getResources().getString(R.string.please_input_value));
//                    etBrakeValue.setText(brake + "");
//                    return true;
//                }
//                if (Integer.parseInt(inputStr) > sbBrake.getMax()) {
//                    etBrakeValue.setText(sbBrake.getMax() + "");
//                }
//                sbBrake.setProgress(Integer.parseInt(v.getText().toString()));
//            }
//            return false;
//        });
//
//        setFocusListener(etStatePoseValue);
//        setFocusListener(etLiftValue);
//        setFocusListener(etCourseValue);
//        setFocusListener(etBrakeValue);
//    }
//
//    private void setFocusListener(EditText editText) {
//        editText.setOnTouchListener(new OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (MotionEvent.ACTION_DOWN == event.getAction()) {
//                    editText.setCursorVisible(true);
//                }
//                return false;
//            }
//        });
//    }
//
//    /**
//     * 检查灵敏度并设置给text
//     *
//     * @param seekBar  seekBar
//     * @param textView textView
//     * @param minValue 最小值
//     * @param progress progress
//     */
//    @SuppressLint("SetTextI18n")
//    private void checkAndSetSensitivityValue(SeekBar seekBar, TextView textView, int minValue, int progress) {
//        clearEditTextFocus();
//        if (progress < minValue) {
////            Toast.makeText(mContext, "最小值为" + minValue + "%", Toast.LENGTH_SHORT).show();
//            seekBar.setProgress(minValue);
//            textView.setText(minValue + "");
//            return;
//        }
//
//        textView.setText(progress + "");
//
//    }
//
//    /**
//     * 发送EXP值到无人机
//     */
//    private void sendExpToFlight() {
//        byte upDownValue = (byte) (Double.parseDouble(etExpUpDown.getText().toString()) * 100);
//        byte rightLeftValue = (byte) (Double.parseDouble(etExpRightLeft.getText().toString()) * 100);
//        byte forwardBackValue = (byte) (Double.parseDouble(etExpForwardBack.getText().toString()) * 100);
//        if (GduApplication.getSingleApp().gduCommunication == null) {
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.setEXP(upDownValue, rightLeftValue, forwardBackValue, (code, bean) -> {
//        });
//    }
//
//    /**
//     * 发送灵敏度到飞机
//     */
//    private void sendSensitivityToFlight() {
//
//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
//            Toaster.show(getResources().getString(R.string.DeviceNoConn));
//            return;
//        }
//
//        byte statePoseValue = (byte) sbStatePose.getProgress();
//        byte liftValue = (byte) sbLift.getProgress();
//        byte courseValue = (byte) sbCourse.getProgress();
//        byte brakeValue = (byte) sbBrake.getProgress();
//
//        if (GduApplication.getSingleApp().gduCommunication == null) {
//            return;
//        }
//        GduApplication.getSingleApp().gduCommunication.setSensitivity(statePoseValue, liftValue, courseValue, brakeValue, (code, bean) -> {
//            if (code == GduConfig.OK) {
//                statePose = statePoseValue;
//                lift = liftValue;
//                course = courseValue;
//                brake = brakeValue;
//            }
//
//        });
//    }
//
//    /**
//     * 清除EditText光标
//     */
//    private void clearEditTextFocus() {
//        etStatePoseValue.setCursorVisible(false);
//        etLiftValue.setCursorVisible(false);
//        etCourseValue.setCursorVisible(false);
//        etBrakeValue.setCursorVisible(false);
//    }


}
