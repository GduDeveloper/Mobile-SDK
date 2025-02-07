package com.gdu.demo.widget.rc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.drone.ControlHand;

/**
 * 自定义按键view
 */
public class RCCustomKeyView extends RelativeLayout implements View.OnClickListener{

    private final byte SUCCESSC1C2 = 0x04;
    private final int FAILEC1 = 0x05;
    private final int FAILEC2 = 0x06;
    private final byte GETC1C2 = 0x07;

    private final Context mContext;

    private GduSpinner mC1SettingView;
    private GduSpinner mC2SettingView;

    private byte preCl;
    private byte curC1 = 0;
    private byte preC2;
    private byte curC2 = 0;
    private String[] C1C2_Data;


    public RCCustomKeyView(Context context) {
        this(context, null);
    }

    public RCCustomKeyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RCCustomKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initListener();
        initData();
    }

    private void initData() {

    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_custom_key, this);
        mC1SettingView = findViewById(R.id.c1_setting_view);
        mC2SettingView = findViewById(R.id.c2_setting_view);
    }

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GETC1C2:
                    switch (msg.arg1) {
                        case 0:
                            curC1 = (byte) 0x00;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;
                        case 1:
                            curC1 = (byte) 0x01;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;

                        case 2:
                            curC1 = (byte) 0x02;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;

                        case 3:
                            curC1 = (byte) 0x03;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;

                        case 4:
                            curC1 = (byte) 0x04;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;

                        case 6:
                            curC1 = (byte) 0x06;
                            preCl = curC1;
                            mC1SettingView.setText(C1C2_Data[curC1]);
                            break;
                        default:
                            break;
                    }
                    switch (msg.arg2) {
                        case 0:
                            curC2 = (byte) 0x00;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;
                        case 1:
                            curC2 = (byte) 0x01;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;

                        case 2:
                            curC2 = (byte) 0x02;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;

                        case 3:
                            curC2 = (byte) 0x03;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;

                        case 4:
                            curC2 = (byte) 0x04;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;

                        case 6:
                            curC2 = (byte) 0x06;
                            preC2 = curC2;
                            mC2SettingView.setText(C1C2_Data[curC2]);
                            break;
                        default:
                            break;
                    }
                    break;

                case SUCCESSC1C2:
                    mC1SettingView.setText(C1C2_Data[curC1]);
                    mC2SettingView.setText(C1C2_Data[curC2]);
                    Toast.makeText(mContext, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case FAILEC1:
                    Toast.makeText(mContext, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                    if (C1C2_Data != null) {
                        mC1SettingView.setText(C1C2_Data[curC1]);
                    }
                    break;

                case FAILEC2:
                    Toast.makeText(mContext, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                    if (C1C2_Data != null) {
                        mC2SettingView.setText(C1C2_Data[curC2]);
                    }
                    break;
                default:
                    break;

            }
            return false;
        }
    });


    /**
     * <P>shang</P>
     * <li>20171110-修改-便于扩展和集中管理C1,C2</li>
     * <li>在String -中的 【PlanSet_C1C2_Parameter】 可直接增加和修改 </li>
     * <li>PS：鉴于GDUPro 的C1，C2 功能多达9种选项。并且增加代码的可复用率和可修改程度，故此修改</li>
     */
    public void initC1C2Event() {
        C1C2_Data = mContext.getResources().getStringArray(R.array.PlanSet_C1C2_Parameter);
        //通过Socket 回调获取 飞机C1，C2 预设值
//        GduApplication.getSingleApp().gduCommunication.getRCC1AndC2(new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    if (handler != null && bean.frameContent.length > 2) {
//                        Message message = new Message();
//                        message.what = GETC1C2;
//                        if (C1C2_Data.length <= bean.frameContent[2]) {
//                            message.arg1 = 0;
//                        } else {
//                            message.arg1 = bean.frameContent[2];
//                        }
//
//                        if (C1C2_Data.length <= bean.frameContent[3]) {
//                            message.arg2 = 0;
//                        } else {
//                            message.arg2 = bean.frameContent[3];
//                        }
//
//                        handler.sendMessage(message);
//                    }
//                }
//            }
//        });
    }



    /**
     * <P>shang</P>
     * <P>设置C1 的参数事件</P>
     */
    private void setC1Parameter(final int value) {
        if (connStateToast()) {
//            GduApplication.getSingleApp().gduCommunication.setRCC1AndC2((byte) value, curC2, new SocketCallBack3() {   //TODO  暂定
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//                    if (code == GduConfig.OK) {
//                        curC1 = (byte) value;
//                        handler.obtainMessage(SUCCESSC1C2).sendToTarget();
//                    } else {
//                        curC1 = preCl;
//                        handler.obtainMessage(FAILEC1, 1).sendToTarget();
//                    }
//                }
//            });
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
     * <P>shang</P>
     * <P>设置C2 的参数事件</P>
     */
    private void setC2Parameter(final int value) {
        if (connStateToast()) {
//            GduApplication.getSingleApp().gduCommunication.setRCC1AndC2(curC1, (byte) value, new SocketCallBack3() {  //TODO  暂定
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//                    if (code == GduConfig.OK) {
//                        curC2 = (byte) value;
//                        handler.obtainMessage(SUCCESSC1C2).sendToTarget();
//                    } else {
//                        curC2 = preC2;
//                        handler.obtainMessage(FAILEC2, 1).sendToTarget();
//                    }
//                }
//            });
        }
    }

    public void setOnControlHandModeListener(OnControlHandModeListener onControlHandModeListener){
//        mOnControlHandModeListener = onControlHandModeListener;
    }

    private void initListener() {
        mC1SettingView.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {
                setC1Parameter(position);
            }
        });
        mC2SettingView.setOnOptionClickListener(new GduSpinner.OnOptionClickListener() {
            @Override
            public void onOptionClick(int parentId, View view, int position) {
                setC2Parameter(position);
            }
        });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }


    public interface OnControlHandModeListener{
        void onSetControlHand(int value, ControlHand controlHand);
    }
}
