package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gdu.api.GduSettingManager;
import com.gdu.drone.ControlHand;

/**
 * Created by zhangzhilai on 2018/5/31.
 * 3. 设置界面
 */

public class SettingActivity extends Activity implements View.OnClickListener {

    private TextView mControlHandTextView;
    private TextView mElevationTypeTextView;

    private TextView mLimitHeightTextView;
    private TextView mLimitDistanceTextView;
    private TextView mReturnHeightTextView;
    private TextView mLimitHeightAndWidthTextView;

    private GduSettingManager mGduSettingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initManager();
        initView();
    }

    private void initManager() {
        mGduSettingManager = new GduSettingManager();
    }



    /***************
     * 显示获取的一些状态信息
     */
    private TextView tv_ShowMsg;


    private void initView() {
        tv_ShowMsg = (TextView) findViewById(R.id.tv_ShowMSG);
        mControlHandTextView = (TextView) findViewById(R.id.control_hand_textview);
        mLimitHeightTextView   = (TextView)findViewById(R.id.limit_height_textview);
        mLimitDistanceTextView = (TextView)findViewById(R.id.limit_distance_textview);
        mReturnHeightTextView  = (TextView)findViewById(R.id.return_height_textview);
        mLimitHeightAndWidthTextView  = (TextView)findViewById(R.id.limit_height_width_textview);
        mElevationTypeTextView = (TextView) findViewById(R.id.elevation_type_textview);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void getFlyMode(View view) {
    }

    public void setFlyMode(View view) {
    }

    int type = 1;
    public void setControlHand(View view) {
        ControlHand controlHand;
        if (type % 3 == 0) {
            controlHand = ControlHand.HAND_JAPAN;
            type++;
        } else if(type % 3 == 1){
            controlHand = ControlHand.HAND_AMERICA;
            type++;
        } else {
            controlHand = ControlHand.HAND_CHINA;
            type++;
        }
        mGduSettingManager.setControlHand(controlHand, onSettingListener);
    }
    public void getControlHand(View view) {
        mGduSettingManager.getControlHand(new GduSettingManager.OnSettingListener<ControlHand>() {
            @Override
            public void onSetSucceed(ControlHand data) {
                showText(mControlHandTextView, data + "");
            }

            @Override
            public void onSetFailed() {
                showText(mControlHandTextView, "onSetFailed");
            }
        });
    }

    GduSettingManager.OnSettingListener onSettingListener = new GduSettingManager.OnSettingListener() {

        @Override
        public void onSetSucceed(Object data) {
            Log.d("test", "test onSetSucceed");
        }

        @Override
        public void onSetFailed() {
            Log.d("test", "test onSetFailed");
        }
    };

    private void showText(final TextView textView, final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(content);
            }
        });
    }


    public void setLimitHeight(View view) {
        mGduSettingManager.setLimitHeight(50, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mLimitHeightTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mLimitHeightTextView, "fail");
            }
        });
    }

    public void getLimitHeight(View view) {
        mGduSettingManager.getLimitHeight(new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mLimitHeightTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mLimitHeightTextView, "fail");
            }
        });
    }

    public void setLimitDistance(View view) {
        mGduSettingManager.setLimitDistance(100, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mLimitDistanceTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mLimitDistanceTextView, "fail");
            }
        });
    }

    public void getLimitDistance(View view) {
        mGduSettingManager.getLimitDistance(new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mLimitDistanceTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mLimitDistanceTextView, "fail");
            }
        });
    }

    public void setReturnHeight(View view) {
        mGduSettingManager.setReturnHeight(30, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mReturnHeightTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mReturnHeightTextView, "fail");
            }
        });
    }

    public void getReturnHeight(View view) {
       int returnHeight = mGduSettingManager.getReturnHeight();
        showText(mReturnHeightTextView, "" + returnHeight);
    }


    public void setLimitHeightAndWidth(View view) {
        mGduSettingManager.setLimitHeightAndDistance(120, 1000, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object data) {
                showText(mReturnHeightTextView, data.toString());
            }

            @Override
            public void onSetFailed() {
                showText(mReturnHeightTextView, "fail");
            }
        });
    }

    public void getLimitHeightAndWidth(View view) {
        mGduSettingManager.getLimitHeightAndDistance(new GduSettingManager.OnSettingListener<int[]>() {
            @Override
            public void onSetSucceed(int[] ints) {
                showText(mLimitHeightAndWidthTextView, ints[0] + "," + ints[1]);
            }

            @Override
            public void onSetFailed() {

            }
        });
    }

    public void setAll(View view){
        mGduSettingManager.setReturnHeight(110, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {

            }

            @Override
            public void onSetFailed() {

            }
        });
        mGduSettingManager.setLimitDistance(300, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {

            }

            @Override
            public void onSetFailed() {

            }
        });
        mGduSettingManager.setLimitHeight(300, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {

            }

            @Override
            public void onSetFailed() {

            }
        });

    }

    /**
     * 设置海拔高程
     *
     * @param view
     */
    public void setElevationType1(View view) {
        mGduSettingManager.setElevationType((byte) 2, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {
                showText(mElevationTypeTextView, "设置成功");
            }

            @Override
            public void onSetFailed() {
                showText(mElevationTypeTextView, "设置失败");
            }
        });
    }

    /**
     * 设置椭球高程
     *
     * @param view
     */
    public void setElevationType2(View view) {
        mGduSettingManager.setElevationType((byte) 1, new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {
                showText(mElevationTypeTextView, "设置成功");
            }

            @Override
            public void onSetFailed() {
                showText(mElevationTypeTextView, "设置失败");
            }
        });
    }

    public void getElevationType(View view) {
        mGduSettingManager.getElevationType(new GduSettingManager.OnSettingListener() {
            @Override
            public void onSetSucceed(Object o) {
                String type = "" + o;
                if ((byte) o == 1) {
                    type = "椭球高程";
                }
                if ((byte) o == 2) {
                    type = "海拔高程";
                }
                showText(mElevationTypeTextView, type);
            }

            @Override
            public void onSetFailed() {
                showText(mElevationTypeTextView, "获取失败");
            }
        });
    }

    @Override
    public void onClick(View v) {

    }
}
