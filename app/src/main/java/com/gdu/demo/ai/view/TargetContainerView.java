package com.gdu.demo.ai.view;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.core.util.Pools;

import com.gdu.demo.R;
import com.gdu.demo.ai.config.TargetType;
import com.gdu.lib.util.CollectionUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.key.value.ai.TargetMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangzhilai on 2018/8/17.
 * 存放目标的容器
 */

public class TargetContainerView extends RelativeLayout{
    private static final String TAG = TargetContainerView.class.getSimpleName();
    private final Context mContext;
    private SelectTargetView mZoneSelectTargetView;
    private SmartButtonView smartButtonView;
    private Map<Integer, TargetView> mDetectTargetViewMap;  //循环检测目标View
    private Map<Integer, TargetMode> mDetectTargetModeMap;  //循环检测目标

    private Map<Integer, TargetView> mLocateTargetViewMap;  //单点定位目标View
    private Map<Integer, TargetMode> mLocateTargetModeMap;  //单点定位目标

    private TargetView mLastSelectTargetView;
    private OnTargetContainerViewListener mOnTargetViewListener;


    private final Pools.SynchronizedPool<TargetView> cacheTargetViewPools = new Pools.SynchronizedPool<>(50);
    private final TargetView.OnTargetViewListener targetViewListener = new TargetView.OnTargetViewListener() {
        @Override
        public void onTrackClick(int trackId, byte trackType) {
            if (trackType == TargetType.TARGET_DETECT.getKey()) {
                if (mOnTargetViewListener != null){
                    mOnTargetViewListener.onTargetClick(mDetectTargetModeMap.get(trackId));
                    XLogger.APP.d(TAG,"onTrackClick  id = " + trackId);
                }
            }
        }

        @Override
        public void onTrackPointClick(int trackId) {
//                Log.d("test", "test onTrackPointClick trackId: " + trackId);
        }

        @Override
        public void onDoubleClick(int trackId, byte trackType) {
            if (mOnTargetViewListener != null){
                if (trackType == TargetType.TARGET_DETECT.getKey()) {
                    mOnTargetViewListener.onTargetDoubleClick(mDetectTargetModeMap.get(trackId));
                } else {
                    mOnTargetViewListener.onTargetDoubleClick(mLocateTargetModeMap.get(trackId));
                }
                XLogger.APP.d(TAG, "test onTrackPointClick trackId: " + trackId);
            }
        }
    };


    public TargetContainerView(Context context) {
        this(context, null);
    }

    public TargetContainerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TargetContainerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initData();
    }

    public void setOnTargetViewListener(OnTargetContainerViewListener onTargetViewListener){
        mOnTargetViewListener = onTargetViewListener;
    }

    private void initData() {
        mDetectTargetViewMap = new ConcurrentHashMap<>();
        mDetectTargetModeMap = new ConcurrentHashMap<>();

        mLocateTargetViewMap = new ConcurrentHashMap<>();
        mLocateTargetModeMap = new ConcurrentHashMap<>();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.bizai_view_target_container_layout, this);
        mZoneSelectTargetView = findViewById(R.id.zone_detect_view);
        mZoneSelectTargetView.setOnSelectCallBack(new SelectTargetView.OnSelectCallBack() {
            @Override
            public void onSelect(Point one, Point two) {
                if (mOnTargetViewListener != null) {
                    mOnTargetViewListener.onLocateRegionSelect(one, two);
                }
            }

            @Override
            public void onClick(Point point) {

            }

            @Override
            public void onDoubleClick(Point point) {

            }

            @Override
            public void regionTempIsShow(boolean isShow, boolean isClose) {

            }


        });
        smartButtonView = findViewById(R.id.smart_button_view);
    }

    /**
     * 目标框选view的显示和隐藏
     * @param isShow
     */
    public void showSelectTargetView(boolean isShow){
        if (isShow) {
            mZoneSelectTargetView.setVisibility(VISIBLE);
            mZoneSelectTargetView.setViewType(SelectTargetView.DRAW_TARGET_FRAME);
        } else {
            mZoneSelectTargetView.setVisibility(GONE);
        }
    }

    public SelectTargetView getSelectTargetView() {
        return mZoneSelectTargetView;
    }

    public SmartButtonView getSmartButtonView(){
        return smartButtonView;
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

    /**
     * 清除手动画出的view
     */
    public void clearSelectTargetView(){
//        if (mZoneSelectTargetView != null) {
//            mZoneSelectTargetView.clearDraw();
//        }
        if (mLocateTargetModeMap != null && mLocateTargetModeMap.size() > 0) {
            for (Integer id : mLocateTargetModeMap.keySet()) {
                XLogger.APP.d(TAG,"clearSelectTargetView  id = " + id);
                removeTargetView(mLocateTargetModeMap.get(id));
            }
        }
    }

    /**
     * 清除循环检测的view
     */
    public void clearDetectTargetView(){
        if (mDetectTargetModeMap != null && mDetectTargetModeMap.size() > 0) {
            for (Integer id : mDetectTargetModeMap.keySet()) {
                removeTargetView(mDetectTargetModeMap.get(id));
            }
        }
    }

    /**
     * 添加目标到界面
     * @param targetMode
     */
    public synchronized void addTarget(TargetMode targetMode){
        int id = targetMode.getId();
        if (targetMode.getLocateType() == TargetType.TARGET_DETECT.getKey()) {

            if (mDetectTargetModeMap == null) {
                return;
            }
            if (mDetectTargetViewMap.containsKey(id)) {
//                XLogger.APP.i("ContainsKey addTarget  id = " + id);
                mDetectTargetModeMap.put(id, targetMode);
                updateTargetView(targetMode);
            } else {
                addTargetView(targetMode);
            }
        } else {
            if (mLocateTargetViewMap == null) {
                return;
            }
            if (mLocateTargetViewMap.containsKey(id)) {
                mLocateTargetModeMap.put(id, targetMode);
                updateTargetView(targetMode);
            } else {
                addTargetView(targetMode);
            }
        }
    }

    public void addTargetViewList(List<TargetMode> targetModes) {
        if (CollectionUtils.isEmptyList(targetModes)) {
            return;
        }
        final List<TargetMode> data = new ArrayList<>();
        CollectionUtils.listAddAllAvoidNPE(data, targetModes);
        if (CollectionUtils.isEmptyList(data)) {
            return;
        }
        if (mDetectTargetViewMap != null && mDetectTargetViewMap.values().size() > 0) {
            for (TargetView targetView : mDetectTargetViewMap.values()) {
                if (!hasExist(targetView.getTrackId(), data)) {
                    removeTargetView(targetView);
                }
            }
        }

        if (mLocateTargetViewMap != null && mLocateTargetViewMap.values().size() > 0) {
            for (TargetView targetView : mLocateTargetViewMap.values()) {
                if (!hasExist(targetView.getTrackId(), data)) {
                    removeTargetView(targetView);
                }
            }
        }

        if (CollectionUtils.isEmptyList(data)) {
            return;
        }
        for (TargetMode targetMode : data) {
            if (targetMode != null) {
                addTarget(targetMode);
            }
        }
    }

    public boolean hasExist(int id, List<TargetMode> targetModes) {
        if (CollectionUtils.isEmptyList(targetModes)) {
            return false;
        }
        for (int i = 0; i < targetModes.size(); i++) {
            TargetMode itemMode = targetModes.get(i);
            if (itemMode == null) {
                continue;
            }
            int key = itemMode.getId();
            if (id == key) {
                return true;
            }
        }
        return false;
    }

    public void addTargetView(TargetMode targetMode){
        TargetView targetView = obtainTargetView();
        targetView.isTrack(true);
        targetView.setTrackId(targetMode.getId());
        targetView.setTargetType(targetMode.getTargetType());
        targetView.setmTrackType(targetMode.getLocateType());
        targetView.setType(targetMode.getType());
        if (targetMode.getLocateType() == TargetType.TARGET_DETECT.getKey()) {
            mDetectTargetModeMap.put(targetMode.getId(), targetMode);
            mDetectTargetViewMap.put(targetView.getTrackId(), targetView);
        } else {
            mLocateTargetModeMap.put(targetMode.getId(), targetMode);
            mLocateTargetViewMap.put(targetMode.getId(), targetView);
        }
        addView(targetView);
        updateTargetView(targetMode);
        targetView.setOnTargetViewListener(targetViewListener);
    }

    private void targetClick(int trackId){
        if (mOnTargetViewListener != null){
            TargetMode targetMode = mDetectTargetModeMap.get(trackId);
            mOnTargetViewListener.onTargetClick(targetMode);
        }
    }

    /**
     * 更新目标坐标
     */
    public void updateTargetView(TargetMode targetMode){
        TargetView targetView;
        if (targetMode.getLocateType() == TargetType.TARGET_DETECT.getKey()) {
            targetView = mDetectTargetViewMap.get(targetMode.getId());
        } else {
            targetView = mLocateTargetViewMap.get(targetMode.getId());
        }
        if (targetView == null) {
            return;
        }
        String targetType = "其他";
        int type = targetMode.getTargetType();
        switch (type) {
            case 1:
                targetType = "车";
                break;
            case 2:
                targetType = "人";
                break;
            case 3:
                targetType = "船";
                break;
            case 4:
                targetType = "红外人";
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
            case 15:
                break;
            case 16:
                targetType = "杆塔";
                break;
            case 17:
                targetType = "玻璃绝缘子";
                break;
            case 18:
                targetType = "复合绝缘子";
                break;
            case 19:
                targetType = "陶瓷绝缘子";
                break;
            case 20:
                targetType = "连接金具";
                break;
            case 21:
                targetType = "挂点金具";
                break;
            case 22:
                targetType = "驱鸟器";
                break;
            case 23:
                targetType = "相序牌";
                break;
            case 24:
                targetType = "防震锤";
                break;
            case 25:
                targetType = "均压环";
                break;
            case 26:
                break;
            case 27:
                break;
            case 28:
                break;
            case 29:
                break;
            case 30:
                break;
            case 31:
                break;
            case 32:
                break;
            case 33:
                break;
            case 34:
                break;
            case 35:
                break;
            case 36:
                break;
            case 37:
                break;
            case 38:
                break;
            case 39:
                break;
            case 40:
                break;
            case 41:
                break;
            case 42:
                break;
            case 43:
                break;
            case 44:
                break;
            case 45:
                break;
            case 46:
                break;
            case 47:
                break;
            case 48:
                break;
            case 49:
                break;
            case 50:
                break;
            case 51:
                targetType = "杆塔异物";
                break;
            case 52:
                targetType = "玻璃绝缘子自爆";
                break;
            case 53:
                targetType = "绝缘子伞裙破损";
                break;
            case 54:
                targetType = "绝缘子污秽";
                break;
            case 55:
                targetType = "绝缘子电弧灼伤";
                break;
            case 56:
                targetType = "均压环脱落";
                break;
            case 57:
                targetType = "均压环损坏";
                break;
            case 58:
                targetType = "均压环反装";
                break;
            case 59:
                targetType = "均压环缺失";
                break;
            case 60:
                targetType = "均压环倾斜";
                break;
            case 61:
                targetType = "大金具锈蚀";
                break;
            case 62:
                targetType = "防振锤脱落";
                break;
            case 63:
                targetType = "悬垂线夹歪斜";
                break;
            case 64:
                targetType = "小金具锈蚀";
                break;
            case 65:
                targetType = "螺栓缺螺母";
                break;
            case 66:
                targetType = "螺栓缺销子";
                break;
            case 67:
                targetType = "销钉安装不规范";
                break;
            case 68:
                targetType = "悬垂线夹缺垫片";
                break;
            case 69:
                targetType = "螺栓螺母欠扣";
                break;
            case 70:
                targetType = "导地线损伤";
                break;
            case 71:
                targetType = "导地线散股";
                break;
            case 72:
                targetType = "导地线断股";
                break;
            case 73:
                targetType = "导地线锈蚀";
                break;
            case 74:
                targetType = "立杆淹没";
                break;
            case 75:
                targetType = "余土堆积";
                break;
            case 76:
                targetType = "杂物堆积";
                break;
            case 77:
                targetType = "标志牌破损";
                break;
            case 78:
                targetType = "标志牌褪色";
                break;
            case 79:
                targetType = "驱鸟器损坏";
                break;
            case 80:
                targetType = "防鸟刺损坏";
                break;
            case 81:
                targetType = "塔吊";
                break;
            case 82:
                targetType = "推土机";
                break;
            case 83:
                targetType = "挖掘";
                break;
            case 84:
                targetType = "接地线外露";
                break;
            default:
                break;
        }

//        if (type == 0) {
//            if (targetMode.getProject() == 1) {
//                targetType = "杆塔";
//            } else if(targetMode.getProject() == 2){
//                targetType = "人";
//            }
//        } else if (type == 1) {
//            if (targetMode.getProject() == 1) {
//                targetType = "玻璃绝缘子";
//            } else if(targetMode.getProject() == 2){
//                targetType = "火点";
//            }
//        } else if(type == 2){
//            targetType = "复合绝缘子";
//        } else if(type == 3){
//            targetType = "陶瓷绝缘子";
//        } else if(type == 4){
//            targetType = "连接金具";
//        } else if(type == 5){
//            targetType = "挂点金具";
//        } else if(type == 6){
//            targetType = "驱鸟器";
//        } else if(type == 7){
//            targetType = "相序牌";
//        } else if(type == 8){
//            targetType = "防震锤";
//        } else if(type == 9){
//            targetType = "均压环";
//        } else if(type == 10){
//            targetType = "绝缘子自爆";
//        } else if(type == 11){
//            targetType = "相序牌褪色";
//        } else if(type == 12){
//            targetType = "防震锤破损";
//        } else if(type == 13){
//            targetType = "鸟巢";
//        } else if(type == 14){
//            targetType = "蜂巢";
//        }
//        String latLng = targetType + " " + targetMode.getTargetConfidence();
//        targetView.setLatLng(latLng);
        short left = targetMode.getLeftX();
        short top = targetMode.getLeftY();
        short height = targetMode.getHeight();
        short width = targetMode.getWidth();
        short pointX = targetMode.getTargetCenterPointX();
        short pointY = targetMode.getTargetCenterPointY();
        short right = (short) (left + width);
        short bottom = (short) (top + height);
        if (targetMode.getType() == 1) {
            targetView.updatePoint(left, top, right, bottom, pointX, pointY);
        } else {
            targetView.updateTrackPoint(left, top, right, bottom);
        }
    }


    public void removeTargetView(TargetMode targetMode){
        if (targetMode == null) {
            return;
        }
        int trackId = targetMode.getId();
        removeTargetById(trackId, targetMode.getLocateType());
    }

    public void removeTargetView(TargetView targetView){
        if (targetView == null) {
            return;
        }
        int trackId = targetView.getTrackId();
        byte targetType = targetView.getmTrackType();
        removeTargetById(trackId, targetType);
    }

    private void removeTargetById(int trackId, byte trackType){
        XLogger.APP.d(TAG,"clearSelectTargetView  removeTargetById = " + trackId);
        if (trackType == TargetType.TARGET_DETECT.getKey()) {
            if (mDetectTargetViewMap.containsKey(trackId)) {
                XLogger.APP.d(TAG,"clearSelectTargetView  removeTargetById  containsKey = " + trackId);
                TargetView targetView = mDetectTargetViewMap.get(trackId);
                mDetectTargetViewMap.remove(trackId);
                mDetectTargetModeMap.remove(trackId);
                if (targetView != null) {
                    removeView(targetView);
                    recycleTargetView(targetView);
                }
            }
        } else {
            if (mLocateTargetViewMap.containsKey(trackId)) {
                TargetView targetView = mLocateTargetViewMap.get(trackId);
                if (targetView != null) {
                    removeView(targetView);
                    recycleTargetView(targetView);
                }
                mLocateTargetViewMap.remove(trackId);
                mLocateTargetModeMap.remove(trackId);
            }
        }
    }

    /**
     * 选中目标
     * @param targetMode
     */
    public void selectTargetView(TargetMode targetMode, boolean isSelect){
        if (targetMode != null) {
            TargetView targetView;
            if (targetMode.getLocateType() == TargetType.TARGET_DETECT.getKey()) {
                targetView = mDetectTargetViewMap.get(targetMode.getId());
            } else {
                targetView = mLocateTargetViewMap.get(targetMode.getId());
            }
            targetView.setTargetSelected(isSelect);
            if (mLastSelectTargetView != null) {
                mLastSelectTargetView.setTargetSelected(false);
            }
            mLastSelectTargetView = targetView;
        }
    }

    /**
     * 获取左上角x坐标
     * @param targetMode
     * @return
     */
    private int getLeftX(TargetMode targetMode){
        return targetMode.getLeftX();
    }

    /**
     * 获取左上角y坐标
     * @param targetMode
     * @return
     */
    private int getLeftY(TargetMode targetMode){
        return targetMode.getLeftY();
    }

    /**
     * 获取右下角x坐标
     * @return
     */
    private int getRightX(TargetMode targetMode){
        return targetMode.getLeftX() + targetMode.getWidth();
    }

    /**
     * 获取右下角y坐标
     * @return
     */
    private int getRightY(TargetMode targetMode){
        return targetMode.getLeftY() + targetMode.getHeight();
    }

    /**
     * 移除所有的目标
     */
    public void removeAllTargetView(){

        if (mDetectTargetViewMap != null) {
            mDetectTargetViewMap.values();
            for (TargetView targetView : mDetectTargetViewMap.values()) {
                removeTargetView(targetView);
            }
        }
        if (mLocateTargetViewMap != null) {
            for (TargetView targetView : mLocateTargetViewMap.values()) {
                removeTargetView(targetView);
            }
        }
//        mZoneSelectTargetView.clearDraw();
    }

    /**
     * 移除某一类型目标
     */
    public void removeTargetViewByType(short targetType){

        if (mDetectTargetViewMap != null) {
            for (TargetView targetView : mDetectTargetViewMap.values()) {
                if(targetView.getTargetType() == targetType) {
                    removeTargetView(targetView);
                }
            }
        }

        if (mLocateTargetViewMap != null) {
            for (TargetView targetView : mLocateTargetViewMap.values()) {
                removeTargetView(targetView);
            }
        }
//        mZoneSelectTargetView.clearDraw();
    }

    float mStartY;
    float mStartX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = ev.getY();
                mStartX = ev.getX();
                judgeClickType();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    int clickCount = 0;
    int timeout = 700;
    boolean isMove;

    /**
     * 判断点击类型
     */
    private void judgeClickType() {
        clickCount++;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isMove) {
                    if (clickCount == 1) {
                        XLogger.APP.d(TAG, "test one click");
                    } else if (clickCount == 2) {
                        if (mOnTargetViewListener != null){
                            mOnTargetViewListener.onDoubleClick(mStartX, mStartY);
//                        targetLocate(mStartX, mStartY);
                            XLogger.APP.d(TAG, "test one doubleClick");
                        }
                    }
                }
                handler.removeCallbacks(this); //清空handler延时，并防内存泄漏
                clickCount = 0;//计数清零
            }
        }, timeout);//延时timeout后执行run方法中的代码
    }

    public Handler handler = new Handler() {

    };

    public interface OnTargetContainerViewListener{
        void onTargetClick(TargetMode targetMode);
        void onTargetDoubleClick(TargetMode targetMode);
        void onDoubleClick(float leftX, float leftY);
        void onLocateRegionSelect(Point one, Point two);
        void onLocateAim();
    }

    public void onDestroy(){
        removeAllTargetView();
        mDetectTargetViewMap = null;
        mDetectTargetModeMap = null;
        mLocateTargetViewMap = null;
        mLocateTargetModeMap = null;
    }

    private TargetView obtainTargetView() {
        TargetView tgv = cacheTargetViewPools.acquire();
        if (tgv == null) {
            tgv = new TargetView(mContext);
        } else {
            tgv.reset();
        }
        return tgv;
    }

    private void recycleTargetView(TargetView targetView) {
        if (targetView == null) {
            return;
        }
        cacheTargetViewPools.release(targetView);
    }
}
