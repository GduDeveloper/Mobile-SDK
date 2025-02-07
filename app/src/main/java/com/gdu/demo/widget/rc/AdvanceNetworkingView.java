package com.gdu.demo.widget.rc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.widget.GduSpinner;
import com.gdu.remotecontroller.IMChildPointInfo;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.util.CollectionUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.RonLog;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * 高级组网模式
 */
public class AdvanceNetworkingView extends RelativeLayout implements View.OnClickListener{

    private final Context mContext;
    private GduSpinner mGduSettingOptionView;
    private RelativeLayout mNetworkingModeChooseLayout;
    private ImageView mCancelImageView;
    private LinearLayout mVL1Layout;     //连接飞机垂直线
    private LinearLayout mVL2Layout;     //连接飞机垂直线
    private LinearLayout mVL3Layout;     //连接飞机垂直线
    private LinearLayout mVL4Layout;     //连接飞机垂直线
    private LinearLayout mVL21Layout;    //连接遥控器垂直线
    private LinearLayout mVL22Layout;    //连接遥控器垂直线
    private LinearLayout mVL23Layout;    //连接遥控器垂直线
    private LinearLayout mVL24Layout;    //连接遥控器垂直线
    private LinearLayout mVL25Layout;    //1控N时连接遥控器垂直线

    private GridView mDroneListView;
    private NetworkingDroneAdapter mNetworkingDroneAdapter;
    private GridView mRCListView;
    private NetworkingRCAdapter mNetworkingRCAdapter;
    private OnANListener mOnANListener;

    private View mHL1Layout;    //连接飞机横线
    private View mHL2Layout;    //连接飞机横线
    private View mHL3Layout;    //连接飞机横线
    private View mHL21Layout;   //连接遥控器横线
    private View mHL22Layout;   //连接遥控器横线
    private View mHL23Layout;   //连接遥控器横线

    private List<IMChildPointInfo> mPointInfoList; //当前所有节点信息
    private List<IMChildPointInfo> mRCInfoList;
    private List<IMChildPointInfo> mDroneInfoList;
    private List<IMChildPointInfo> mNetInfoList;

    private List<IMChildPointInfo> mCurrentRCInfoList;
    private List<IMChildPointInfo> mCurrentDroneInfoList;
    private List<IMChildPointInfo> mCurrentNetInfoList;

    private long mLastChooseTime; //上一次切换模式的时间
    private int mLastOnlineNum;


    public AdvanceNetworkingView(Context context) {
        this(context, null);
    }

    public AdvanceNetworkingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceNetworkingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initList();
        initData();
        initListener();
        initUpdate();
    }

    /**
     * 更新当前的组网模式
     */
    private void initUpdate() {
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .to(RxLife.toMain(this, true))
                .subscribe(aLong -> {
                    if (getVisibility() != View.VISIBLE) {
                        return;
                    }
                    long currentTime = System.currentTimeMillis();
                    if (mLastChooseTime != 0) {
                        if (currentTime - mLastChooseTime > 3 * 1000) {  // 3秒后再更新数据，防止刚切换完成后，最新数据还没更新过来
                            updateData(false);
                        }
                    } else {
                        updateData(false);
                    }
                }, throwable -> {
                    MyLogUtils.e("更新组网信息出错", throwable);
                }, () -> {});
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_advanced_networking, this);
        mGduSettingOptionView  = findViewById(R.id.ov_networking_mode);
        mNetworkingModeChooseLayout  = findViewById(R.id.networking_mode_choose_layout);
        mCancelImageView  = findViewById(R.id.hide_imageview);

        mVL1Layout = findViewById(R.id.vertical_line1_layout);
        mVL2Layout = findViewById(R.id.vertical_line2_layout);
        mVL3Layout = findViewById(R.id.vertical_line3_layout);
        mVL4Layout = findViewById(R.id.vertical_line4_layout);
        mVL21Layout = findViewById(R.id.vertical_line21_layout);
        mVL22Layout = findViewById(R.id.vertical_line22_layout);
        mVL23Layout = findViewById(R.id.vertical_line23_layout);
        mVL24Layout = findViewById(R.id.vertical_line24_layout);
        mVL25Layout = findViewById(R.id.rc_vertical_layout);

        mHL1Layout = findViewById(R.id.horizon_line1_layout);
        mHL2Layout = findViewById(R.id.horizon_line2_layout);
        mHL3Layout = findViewById(R.id.horizon_line3_layout);
        mHL21Layout = findViewById(R.id.horizon_line21_layout);
        mHL22Layout = findViewById(R.id.horizon_line22_layout);
        mHL23Layout = findViewById(R.id.horizon_line23_layout);

        mDroneListView = findViewById(R.id.drone_list_view);
        mRCListView = findViewById(R.id.rc_list_view);
    }

    public void setOnANListener(OnANListener onANListener){
        mOnANListener = onANListener;
    }

    private void initListener() {
        mGduSettingOptionView.setOnOptionClickListener((parentId, view, position) -> {
            // 未连接飞机不能点击
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                Toast.makeText(mContext, R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!NetworkingHelper.isRCHasControlPower()) {
                Toast.makeText(mContext, R.string.string_rc_has_no_control, Toast.LENGTH_SHORT).show();
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastChooseTime < 5 * 1000) {
                Toast.makeText(mContext, R.string.frequent_operation, Toast.LENGTH_SHORT).show();
                return;
            }

            if (GlobalVariable.isUseBackupsAirlink) {
                Toast.makeText(mContext, R.string.string_not_change, Toast.LENGTH_SHORT).show();
                return;
            }

             if(!GlobalVariable.planeHadLock){
                 Toast.makeText(mContext, R.string.DroneUnLocked, Toast.LENGTH_SHORT).show();
                 return;
             }

            mLastChooseTime = currentTime;
            int mode = NetworkingHelper.getModeByPosition(position);
//            LoadingDialogUtils.createLoadDialog(mContext, mContext.getString(R.string.mode_changing));
//            GduApplication.getSingleApp().gduCommunication.setNetworking((byte)0x05, (byte) mode,
//                    (code, bean) -> MyLogUtils.i("setNetworking callBack() code = " + code));
        });

        mDroneListView.setOnItemClickListener((parent, view, position, id) -> {
            IMChildPointInfo info = mDroneInfoList.get(position);
            RonLog.LogD("test updateStatus Match mac " + info.mac);
            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None || info.mac == null) {
                Toast.makeText(mContext, R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                return;
            }
            if (info.connectStatus == 0) {
                showMatchDialog(info);
                RonLog.LogD("test updateStatus Match " + info.id);
//                GduApplication.getSingleApp().gduCommunication.setNetworking((byte)0x03, (byte) info.id, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        RonLog.LogD("test setNetworking 0x03 " + code);
//                    }
//                });
            }
        });


        mRCListView.setOnItemClickListener((parent, view, position, id) -> {
            IMChildPointInfo info = mRCInfoList.get(position);
            if (info.mac == null) {
                Toast.makeText(mContext, R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                return;
            }
            if (info.connectStatus == 0) {
                showMatchDialog(info);
//                GduApplication.getSingleApp().gduCommunication.setNetworking((byte)0x03, (byte) info.id, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        RonLog.LogD("test setNetworking 0x03 " + code);
//                    }
//                });
            }
        });

        mRCListView.setOnItemLongClickListener((parent, view, position, id) -> {
            IMChildPointInfo info = mRCInfoList.get(position);
            if (info.id != 0) {
                showDeletePointDialog(info);
            }
            return true;
        });

        mDroneListView.setOnItemLongClickListener((parent, view, position, id) -> {
            IMChildPointInfo info = mDroneInfoList.get(position);
            if (info.id != 0) {
                showDeletePointDialog(info);
            }
            return true;
        });

        mCancelImageView.setOnClickListener(v -> setVisibility(GONE));
    }

    private void initData() {
        mNetworkingDroneAdapter = new NetworkingDroneAdapter(mContext);
        mDroneListView.setAdapter(mNetworkingDroneAdapter);
        mNetworkingRCAdapter = new NetworkingRCAdapter(mContext);
        mRCListView.setAdapter(mNetworkingRCAdapter);
        updateData(true);
    }


    /**
     * 更新当前组网设备状态
     */
    public void updateData(boolean isChangeUI){
        List<List<IMChildPointInfo>> lists = NetworkingHelper.parseNetworkingData();
        mCurrentDroneInfoList = lists.get(0);
        mCurrentRCInfoList = lists.get(1);
        CollectionUtils.listAddAllAvoidNPE(mCurrentRCInfoList, lists.get(2));
//        mCurrentNetInfoList = lists.get(2);
        int position = NetworkingHelper.getPositionByMode();
        int onlineN = getOnlineNum(lists);
        boolean isOnlineChange = false;
        if (onlineN != mLastOnlineNum) {
            isChangeUI = true;
            mLastOnlineNum = onlineN;
            isOnlineChange = true;
        }
//        if (isChangeUI) {
            changeNetworkingUI(position, isOnlineChange);
//        }
        updateStatus();
    }

    /**
     * 获取在线设备数量
     * @param lists
     * @return
     */
    private int getOnlineNum(List<List<IMChildPointInfo>> lists){
        int online = 0;
        for (List<IMChildPointInfo> list : lists) {
            for (IMChildPointInfo info : list) {
                if (info.connectStatus == 1) {
                    online++;
                }
            }
        }
        return online;
    }

    /**
     * 初始化飞机，遥控器，机巢列表
     */
    private void initList(){
        mRCInfoList = new ArrayList<>();
        mDroneInfoList = new ArrayList<>();
        mNetInfoList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            IMChildPointInfo droneInfo = new IMChildPointInfo();
            droneInfo.id = i;
//            droneInfo.pairId = i;
            droneInfo.connectStatus = 0;
            mDroneInfoList.add(droneInfo);
            IMChildPointInfo rcInfo = new IMChildPointInfo();
            rcInfo.connectStatus = 0;
            mRCInfoList.add(rcInfo);
            IMChildPointInfo netInfo = new IMChildPointInfo();
            netInfo.connectStatus = 0;
            mNetInfoList.add(netInfo);
        }
    }

    /**
     * 设置组网模式选择view是否可见
     */
    public void setNetworkingModeChooseLayoutVisible(boolean isVisible){
        if(isVisible){
            mNetworkingModeChooseLayout.setVisibility(VISIBLE);
        } else {
            mNetworkingModeChooseLayout.setVisibility(GONE);
        }
    }

    public void setCancelImageViewVisible(boolean isVisible){
        if (isVisible) {
            mCancelImageView.setVisibility(VISIBLE);
        } else {
            mCancelImageView.setVisibility(GONE);
        }
    }

    /**
     * 更新各个节点状态
     */
    private void updateStatus() {
        int droneNum = mCurrentDroneInfoList.size();
        if (droneNum > 0 && droneNum < 4) {
            sortDroneList();
            for (int i = 0; i < droneNum; i++) {
                IMChildPointInfo tmpInfo = mCurrentDroneInfoList.get(i);
                IMChildPointInfo info = mDroneInfoList.get(i);
                info.id = tmpInfo.id;
                info.connectStatus = tmpInfo.connectStatus;
                info.type = tmpInfo.type;
                info.mac = tmpInfo.mac;
            }
        }
        int rcNum = mCurrentRCInfoList.size();
//        MyLogUtils.i("updateStatus() rcNum = " + rcNum);
        if (rcNum > 0 && rcNum < 4) {
            for (int i = 0; i < rcNum; i++) {
                IMChildPointInfo tmpRCInfo = mCurrentRCInfoList.get(i);
                IMChildPointInfo info = mRCInfoList.get(i);
                info.mac = tmpRCInfo.mac;
                info.type = tmpRCInfo.type;
                info.connectStatus = tmpRCInfo.connectStatus;
                info.id = tmpRCInfo.id;
                if (i == 0) {
//                    if (rcInfo.connectStatus == 1) {
//                        mRC1Layout.setBackgroundResource(R.drawable.shape_networking_online);
//                    } else {
//                        mRC1Layout.setBackgroundResource(R.drawable.shape_networking_offline);
//                    }
                }
            }
        }
//        mNetworkingDroneAdapter.setData(mDroneInfoList);
    }

    /**
     * 将飞机从ap到sta,从已连接到未连接排序
     */
    private void sortDroneList(){
        Collections.sort(mCurrentDroneInfoList, (t1, t2) -> {
            if (t1.id == 0) {
                return -1;
            } else {
                if (t1.connectStatus > t2.connectStatus) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    /**
     * 显示配对提示
     * @param info
     */
    private void showMatchDialog(IMChildPointInfo info){
        Toast.makeText(mContext, R.string.match, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示删除节点弹窗
     * @param info
     */
    private void showDeletePointDialog(IMChildPointInfo info){
//        GeneralDialog deletePointDialog = new GeneralDialog(mContext, R.style.NormalDialog) {
//            @Override
//            public void positiveOnClick() {
//                GduApplication.getSingleApp().gduCommunication.setNetworking((byte)0x04, (byte) info.id,
//                        (code, bean) -> MyLogUtils.i("setNetworking callBack() type = 0x04; code " + code));
//                this.dismiss();
//            }
//
//            @Override
//            public void negativeOnClick() {
//                this.dismiss();
//            }
//        };
//        deletePointDialog.setPositiveButtonText(mContext.getString(R.string.Label_Sure));
//        deletePointDialog.setNegativeButtonText(R.string.Label_cancel);
//        deletePointDialog.setNoTitle();
//        deletePointDialog.setContentText(getContext().getString(R.string.string_confirm_delete_current_node));
//        deletePointDialog.show();
    }

    private int mLastPosition = -1;

    /**
     * 根据组网模式调整View显示
     * @param position
     */
    private void changeNetworkingUI(int position, boolean isOnlineChange) {
        boolean isChangeUI = false;
        if (mLastPosition != position) {
            mLastPosition = position;
//            LoadingDialogUtils.cancelLoadingDialog();
            isChangeUI = true;
        } else {
//            LoadingDialogUtils.cancelLoadingDialog();
        }

        if (isOnlineChange) {
            isChangeUI = true;
        }
        if (!isChangeUI) {
            return;
        }

        setAllViewGone();
        List<IMChildPointInfo> droneList = new ArrayList<>();
        List<IMChildPointInfo> rcList = new ArrayList<>();
        int droneNum = 0;
        int rcNum = 0;
        mGduSettingOptionView.setIndex(position);
        if (mOnANListener != null) {
            mOnANListener.onModeChange(NetworkingHelper.getNameByMode(mContext, position + 1));
        }
        switch (position) {
            case 0: //1控1
                mRCListView.setNumColumns(1);
                mDroneListView.setNumColumns(1);
                droneNum = 1;
                rcNum = 1;
                mVL1Layout.setVisibility(VISIBLE);
                break;

            case 1: //2控1
                droneNum = 1;
                rcNum = 2;
                mVL25Layout.setVisibility(GONE);
                mRCListView.setNumColumns(2);
                mDroneListView.setNumColumns(1);
                mVL1Layout.setVisibility(VISIBLE);
                mVL21Layout.setVisibility(VISIBLE);
                mVL22Layout.setVisibility(VISIBLE);
                setWeight(mHL21Layout, 2);
                setWeight(mHL22Layout,4);
                setWeight(mHL23Layout,2);
                break;

            case 2:  //1控2
                mRCListView.setNumColumns(1);
                mDroneListView.setNumColumns(2);
                droneNum = 2;
                rcNum = 1;
                mVL25Layout.setVisibility(VISIBLE);
                mVL1Layout.setVisibility(VISIBLE);
                mVL2Layout.setVisibility(VISIBLE);
                setWeight(mHL1Layout, 2);
                setWeight(mHL2Layout,4);
                setWeight(mHL3Layout,2);
                break;

//            case 2: //3控1
//                droneNum = 1;
//                rcNum = 3;
//                mRCListView.setNumColumns(3);
//                mDroneListView.setNumColumns(1);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL21Layout.setVisibility(VISIBLE);
//                mVL22Layout.setVisibility(VISIBLE);
//                mVL23Layout.setVisibility(VISIBLE);
//                mVL25Layout.setVisibility(GONE);
//                setWeight(mHL21Layout, 1);
//                setWeight(mHL22Layout,4);
//                setWeight(mHL23Layout,1);
//                break;
//
//            case 3: //4控1
//                droneNum = 1;
//                rcNum = 4;
//                mRCListView.setNumColumns(4);
//                mDroneListView.setNumColumns(1);
//                mVL21Layout.setVisibility(VISIBLE);
//                mVL22Layout.setVisibility(VISIBLE);
//                mVL23Layout.setVisibility(VISIBLE);
//                mVL24Layout.setVisibility(VISIBLE);
//                mVL25Layout.setVisibility(GONE);
//                setWeight(mHL21Layout, 1);
//                setWeight(mHL22Layout,6);
//                setWeight(mHL23Layout,1);
//                break;
//
//            case 4:  //1控2
//                mRCListView.setNumColumns(1);
//                mDroneListView.setNumColumns(2);
//                droneNum = 2;
//                rcNum = 1;
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL25Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 2);
//                setWeight(mHL2Layout,4);
//                setWeight(mHL3Layout,2);
//                break;
//
//            case 5: //2控2
//                droneNum = 2;
//                rcNum = 2;
//                mRCListView.setNumColumns(2);
//                mDroneListView.setNumColumns(2);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL21Layout.setVisibility(VISIBLE);
//                mVL22Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 2);
//                setWeight(mHL2Layout,4);
//                setWeight(mHL3Layout,2);
//                setWeight(mHL21Layout, 2);
//                setWeight(mHL22Layout,4);
//                setWeight(mHL23Layout,2);
//                break;
//
//            case 6: //3控2
//                droneNum = 2;
//                rcNum = 3;
//                mRCListView.setNumColumns(3);
//                mDroneListView.setNumColumns(2);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL21Layout.setVisibility(VISIBLE);
//                mVL22Layout.setVisibility(VISIBLE);
//                mVL23Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 2);
//                setWeight(mHL2Layout,4);
//                setWeight(mHL3Layout,2);
//                setWeight(mHL21Layout, 1);
//                setWeight(mHL22Layout,4);
//                setWeight(mHL23Layout,1);
//                break;
//
//            case 7: //1控3
//                droneNum = 3;
//                rcNum = 1;
//                mRCListView.setNumColumns(1);
//                mDroneListView.setNumColumns(3);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL3Layout.setVisibility(VISIBLE);
//                mVL25Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 1);
//                setWeight(mHL2Layout,4);
//                setWeight(mHL3Layout,1);
//                break;
//
//            case 8: //2控3
//                droneNum = 3;
//                rcNum = 2;
//                mRCListView.setNumColumns(2);
//                mDroneListView.setNumColumns(3);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL3Layout.setVisibility(VISIBLE);
//                mVL21Layout.setVisibility(VISIBLE);
//                mVL22Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 1);
//                setWeight(mHL2Layout,4);
//                setWeight(mHL3Layout,1);
//                setWeight(mHL21Layout, 2);
//                setWeight(mHL22Layout,4);
//                setWeight(mHL23Layout,2);
//                break;
//
//            case 9: //1控4
//                droneNum = 4;
//                rcNum = 1;
//                mRCListView.setNumColumns(1);
//                mDroneListView.setNumColumns(4);
//                mVL1Layout.setVisibility(VISIBLE);
//                mVL2Layout.setVisibility(VISIBLE);
//                mVL3Layout.setVisibility(VISIBLE);
//                mVL4Layout.setVisibility(VISIBLE);
//                mVL25Layout.setVisibility(VISIBLE);
//                setWeight(mHL1Layout, 1);
//                setWeight(mHL2Layout,6);
//                setWeight(mHL3Layout,1);
//                break;

            default:
                break;
        }
        for (int i = 0; i < rcNum; i++) {
            rcList.add(mRCInfoList.get(i));
        }

        for (int i = 0; i < droneNum; i++) {
            droneList.add(mDroneInfoList.get(i));
        }
        mNetworkingDroneAdapter.setData(droneList);
        mNetworkingRCAdapter.setData(rcList);
    }

    /**
     * 更新UI时，先隐藏所有的view
     */
    private void setAllViewGone(){
        mVL1Layout.setVisibility(GONE);
        mVL2Layout.setVisibility(GONE);
        mVL3Layout.setVisibility(GONE);
        mVL4Layout.setVisibility(GONE);
        mHL1Layout.setVisibility(GONE);
        mHL2Layout.setVisibility(GONE);
        mHL3Layout.setVisibility(GONE);
        mVL21Layout.setVisibility(GONE);
        mVL22Layout.setVisibility(GONE);
        mVL23Layout.setVisibility(GONE);
        mVL24Layout.setVisibility(GONE);
        mHL21Layout.setVisibility(GONE);
        mHL22Layout.setVisibility(GONE);
        mHL23Layout.setVisibility(GONE);
    }

    /**
     * 设置view的weight
     * @param layout
     * @param weight
     */
    private void setWeight(View layout, int weight){
        layout.setVisibility(VISIBLE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.weight = weight;
        layout.setLayoutParams(params);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
        }
    }

    public interface OnANListener{
        void onModeChange(String name);
    }
}
