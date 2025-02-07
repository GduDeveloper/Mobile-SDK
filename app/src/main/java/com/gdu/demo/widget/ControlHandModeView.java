package com.gdu.demo.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.drone.ControlHand;

/**
 * 控制手view
 */
public class ControlHandModeView extends RelativeLayout implements View.OnClickListener{

    private final Context mContext;
    private OnControlHandModeListener mOnControlHandModeListener;

    private Button mAmericaControlView;
    private Button mChinaControlView;
    private Button mJapanControlView;

    private ImageView mLeftHandImageView;
    private ImageView mRightHandImageView;

    private TextView mLeftHandUpTextView;
    private TextView mLeftHandDownTextView;
    private TextView mLeftHandLeftTextView;
    private TextView mLeftHandRightTextView;

    private TextView mRightHandUpTextView;
    private TextView mRightHandDownTextView;
    private TextView mRightHandLeftTextView;
    private TextView mRightHandRightTextView;

    private ControlHand mSelectedHand = GlobalVariable.controlHand;//控制手型

    public ControlHandModeView(Context context) {
        this(context, null);
    }

    public ControlHandModeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlHandModeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initListener();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_control_hand, this);
        mAmericaControlView =  findViewById(R.id.bt_america_control);
        mChinaControlView = findViewById(R.id.bt_china_control);
        mJapanControlView = findViewById(R.id.bt_japan_control);
        mLeftHandImageView = findViewById(R.id.iv_left_hand);
        mRightHandImageView = findViewById(R.id.iv_right_hand);

        mLeftHandUpTextView = findViewById(R.id.left_hand_up);
        mLeftHandDownTextView = findViewById(R.id.left_hand_down);
        mLeftHandLeftTextView = findViewById(R.id.left_hand_left);
        mLeftHandRightTextView = findViewById(R.id.left_hand_right);
        mRightHandUpTextView = findViewById(R.id.right_hand_up);
        mRightHandDownTextView = findViewById(R.id.right_hand_down);
        mRightHandLeftTextView = findViewById(R.id.right_hand_left);
        mRightHandRightTextView = findViewById(R.id.right_hand_right);
    }

    public void setOnControlHandModeListener(OnControlHandModeListener onControlHandModeListener){
        mOnControlHandModeListener = onControlHandModeListener;
    }

    private void initListener() {
        mAmericaControlView.setOnClickListener(this);
        mChinaControlView.setOnClickListener(this);
        mJapanControlView.setOnClickListener(this);
    }

    public void switchControlEnable() {
        showControlHand();
        mAmericaControlView.setClickable(true);
        mChinaControlView.setClickable(true);
        mJapanControlView.setClickable(true);
    }

    /**
     * 显示操作手
     */
    private void showControlHand() {
        if (mSelectedHand == ControlHand.HAND_AMERICA) {
            mAmericaControlView.setSelected(true);
            mChinaControlView.setSelected(false);
            mJapanControlView.setSelected(false);
        } else if (mSelectedHand == ControlHand.HAND_CHINA) {
            mAmericaControlView.setSelected(false);
            mChinaControlView.setSelected(true);
            mJapanControlView.setSelected(false);
        } else if (mSelectedHand == ControlHand.HAND_JAPAN) {
            mAmericaControlView.setSelected(false);
            mChinaControlView.setSelected(false);
            mJapanControlView.setSelected(true);
        } else {
            mAmericaControlView.setSelected(false);
            mChinaControlView.setSelected(false);
            mJapanControlView.setSelected(false);
        }
        setControlHandText();
    }

    private void setControlHandText() {
        switch (mSelectedHand) {
            case HAND_AMERICA:
                setHandTextAppr(mAmericaControlView, true);
                setHandTextAppr(mChinaControlView, false);
                setHandTextAppr(mJapanControlView, false);
                showHandImageView(R.drawable.icon_america_left_hand, R.drawable.icon_america_right_hand);
                showLeftHandHint(R.string.control_up, R.string.control_down, R.string.control_turn_left, R.string.control_turn_right);
                showRightHandHint(R.string.control_forward, R.string.control_back, R.string.control_left, R.string.control_right);

                break;
            case HAND_CHINA:
                setHandTextAppr(mAmericaControlView, false);
                setHandTextAppr(mChinaControlView, true);
                setHandTextAppr(mJapanControlView, false);
                showHandImageView(R.drawable.icon_china_left_hand, R.drawable.icon_china_right_hand);
                showLeftHandHint(R.string.control_forward, R.string.control_back, R.string.control_left, R.string.control_right);
                showRightHandHint(R.string.control_up, R.string.control_down, R.string.control_turn_left, R.string.control_turn_right);
                break;
            case HAND_JAPAN:
                setHandTextAppr(mAmericaControlView, false);
                setHandTextAppr(mChinaControlView, false);
                setHandTextAppr(mJapanControlView, true);
                showHandImageView(R.drawable.icon_japan_left_hand, R.drawable.icon_japan_right_hand);
                showLeftHandHint(R.string.control_forward, R.string.control_back, R.string.control_turn_left, R.string.control_turn_right);
                showRightHandHint(R.string.control_up, R.string.control_down, R.string.control_left, R.string.control_right);
                break;
        }
    }

    private void showLeftHandHint(int leftUp, int leftDown, int leftLeft, int leftRight){
        mLeftHandUpTextView.setText(leftUp);
        mLeftHandDownTextView.setText(leftDown);
        mLeftHandLeftTextView.setText(leftLeft);
        mLeftHandRightTextView.setText(leftRight);
    }

    private void showRightHandHint(int rightUp, int rightDown, int rightLeft, int rightRight){
        mRightHandUpTextView.setText(rightUp);
        mRightHandDownTextView.setText(rightDown);
        mRightHandLeftTextView.setText(rightLeft);
        mRightHandRightTextView.setText(rightRight);
    }

    private void showHandImageView(int leftHandImageRes, int rightHandImageRes){
        mLeftHandImageView.setImageResource(leftHandImageRes);
        mRightHandImageView.setImageResource(rightHandImageRes);
    }

    void setHandTextAppr(Button tv, boolean selected) {
        if (selected) {
            tv.setBackgroundResource(R.drawable.shape_bg_ff4e00);
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        } else {
            tv.setBackground(null);
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }

    /**
     * <P>shang</P>
     * <li>设置控制模式，控制手方法</li>
     * <li>不论是否成功，都要重新赋值 selectHand </li>
     * <li>ps:经过测试发现 当每次回调成功后，还会在执行  【showControlHand()】方法三次。只限在进入设置页面后立马点击切换</li>
     * <li>并经过跟踪，发现是在plansetactivity 的 initView 生命周期中 初始化，但是不知道为毛，能进来三次。</li>
     */
    public void setControlHandPic() {
        mSelectedHand = GlobalVariable.controlHand;
//        BBLog.LogE("setControlHandPic：", "isSuccess;" + isSuccess + "----" + "perControlHand:" + perControlHand + "----" + "selectHand.ordinal():" + selectHand);
        mAmericaControlView.setSelected(mSelectedHand == ControlHand.HAND_AMERICA);
        mChinaControlView.setSelected(mSelectedHand == ControlHand.HAND_CHINA);
        mJapanControlView.setSelected(mSelectedHand == ControlHand.HAND_JAPAN);
        setControlHandText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_america_control:  //选择美国手
                if (!connStateToast()) {
                    return;
                }
                if(GlobalVariable.controlHand==ControlHand.HAND_AMERICA){
                    return;
                }
                selectControlHand(01);
                break;

            case R.id.bt_china_control:  //选择中国手
                if (!connStateToast()) {
                    return;
                }
                if(GlobalVariable.controlHand==ControlHand.HAND_CHINA){
                    return;
                }
                selectControlHand(02);
                break;

            case R.id.bt_japan_control:  //选择日本手
                if (!connStateToast()) {
                    return;
                }
                if(GlobalVariable.controlHand==ControlHand.HAND_JAPAN){
                    return;
                }
                selectControlHand(03);
                break;
        }
    }

    /**
     * <P>shang</P>
     * <P>无人机连接状态提示</P>
     */
    private boolean connStateToast() {
        if (UavStaticVar.isOpenTextEnvironment) {
            return true;
        }
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(mContext, R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_MoreOne:
                Toast.makeText(mContext, R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_Sucess:
                return true;
        }
        return false;
    }

    /**
     * 设置操作手
     *
     * @param country
     */
    private void selectControlHand(final int country) {
        if (mContext instanceof FragmentActivity) {
            new CommonDialog.Builder(((FragmentActivity) mContext).getSupportFragmentManager())
                    .setTitle(mContext.getString(R.string.sure_switch_hand_title))
                    .setContent(mContext.getString(R.string.sure_switch_hand_content))
                    .setCancel(mContext.getString(R.string.Label_cancel))
                    .setSure(mContext.getString(R.string.Label_Sure))
                    .setPositiveListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mOnControlHandModeListener != null) {
                                switch (country) {
                                    case 01:
                                        mOnControlHandModeListener.onSetControlHand(01, ControlHand.HAND_AMERICA);
                                        break;
                                    case 02:
                                        mOnControlHandModeListener.onSetControlHand(02, ControlHand.HAND_CHINA);
                                        break;
                                    case 03:
                                        mOnControlHandModeListener.onSetControlHand(03, ControlHand.HAND_JAPAN);
                                        break;
                                }
                            }
                        }
                    })
                    .build().show();
        }
    }

    public interface OnControlHandModeListener{
        void onSetControlHand(int value, ControlHand controlHand);
    }
}
