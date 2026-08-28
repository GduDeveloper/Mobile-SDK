package com.gdu.demo.ai.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.gdu.demo.R;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.key.value.ai.TargetMode;

import java.util.List;

/**
 * Created by zhangzhilai on 2018/8/17.
 * 存放目标的容器
 */

public class SmartButtonView extends RelativeLayout{
    private static final String TAG = SmartButtonView.class.getSimpleName();
    private final Context mContext;
    private RelativeLayout rl_menu;
//    private TextView quitOutBottom;
    private TextView stopButton;
    private TextView tv_follow;
    private TextView tv_stop_follow;
    private String lastTip = "";

    public SmartButtonView(Context context) {
        this(context, null);
    }

    public SmartButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.bizai_view_smart_button, this);
        rl_menu = findViewById(R.id.rl_menu);
        stopButton = findViewById(R.id.but_stop);
//        quitOutBottom = findViewById(R.id.but_quitOut);
        tv_follow = findViewById(R.id.tv_follow);
        tv_stop_follow = findViewById(R.id.tv_stop_follow);

    }

//    public TextView getQuitOutBottom() {
//        return quitOutBottom;
//    }

    public TextView getStopButton() {
        return stopButton;
    }

    public TextView getTv_follow() {
        return tv_follow;
    }

    public TextView getTv_stop_follow() {
        return tv_stop_follow;
    }



    //    /**
//     * 是否可点击
//     * @param isTouchAble
//     */
//    public void setTargetListViewTouchAble(boolean isTouchAble){
//        if (isTouchAble) {
//            mZoneSelectTargetView.setViewType(SelectTargetView.DRAW_TARGET_FRAME);
//        } else {
//            mZoneSelectTargetView.setViewType(SelectTargetView.TARGET_FRAME_MOVE);
//        }
//    }

    public boolean hasExist(int id, List<TargetMode> targetModes) {
        if (CollectionUtils.isEmptyList(targetModes)) {
            return false;
        }
        for (int i = 0; i < targetModes.size(); i++) {
            int key = targetModes.get(i).getId();
            if (id == key) {
                return true;
            }
        }
        return false;
    }

    public void onDestroy(){
    }

}
