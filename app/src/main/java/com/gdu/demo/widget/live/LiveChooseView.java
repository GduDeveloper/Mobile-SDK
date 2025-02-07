package com.gdu.demo.widget.live;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.util.SPUtils;
import com.gdu.util.ViewUtils;
import com.lib.model.LiveType;

import java.util.List;

/**
 * @author zhangzhilai
 * @date 2018/3/19
 */

public class LiveChooseView extends RelativeLayout implements View.OnClickListener {

    private final String GMT_08 = "GMT+08:00"; //中国时区

    private OnLiveChooseViewListener mOnLiveChooseViewListener;

    private EditText editText;

    private Button btn_StartLive;

    private Button btn_ScanQRCode;

    private Button btn_GetPushUrl;

    private ListView listView;

    private final Context context;

    private final LiveHistoryDao liveHistoryDao;

    private Button btn_stick;
    private TextView tv_platform_list;
    private View live_platform_overlying;
    private CameraParamListView live_platform_listview;
    private int selectPosition;

    private LinearLayout   mCustomLiveLayout;
    private RelativeLayout mSystemLiveLayout;
    private Button  mSystemLiveButton;

    public LiveChooseView(Context context) {
        this(context, null);
    }

    public LiveChooseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveChooseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        liveHistoryDao = new LiveHistoryDao(context);
        initView();
        initListener();
    }

    private void initListener() {
        btn_StartLive.setOnClickListener(this);
        btn_ScanQRCode.setOnClickListener(this);
        btn_GetPushUrl.setOnClickListener(this);
        btn_stick.setOnClickListener(this);
        tv_platform_list.setOnClickListener(this);
        live_platform_overlying.setOnClickListener(this);
        mSystemLiveButton.setOnClickListener(this);
        String[] platformArray = context.getResources().getStringArray(R.array.array_platform_type);

        LayoutParams layoutParams = new LayoutParams(ViewUtils.dip2px(context, 130), ViewUtils.dip2px(context, 32) * platformArray.length);
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        layoutParams.topMargin = ViewUtils.dip2px(context, 64);
        live_platform_listview.setLayoutParams(layoutParams);

        live_platform_listview.setData(platformArray);
        int liveType = SPUtils.getInt(context, SPUtils.LIVE_TYPE);
        if (liveType == LiveType.CUSTOM_LIVE.getKey()) {
            live_platform_listview.selectItem(1);
            tv_platform_list.setText(context.getString(R.string.Label_Live_Tx_Custom));
            mSystemLiveLayout.setVisibility(GONE);
            mCustomLiveLayout.setVisibility(VISIBLE);

        } else {
            live_platform_listview.selectItem(0);
            tv_platform_list.setText(context.getString(R.string.Label_live_normal));
            mSystemLiveLayout.setVisibility(VISIBLE);
            mCustomLiveLayout.setVisibility(GONE);
        }

        live_platform_listview.setOnCameraParamSelectListener(new CameraParamListView.OnCameraParamSelectListener() {
            @Override
            public void onCameraParamSelect(int position, String value) {
                tv_platform_list.setText(value);
                selectPosition = position;
                if (position == 0) {
                    mSystemLiveLayout.setVisibility(VISIBLE);
                    mCustomLiveLayout.setVisibility(GONE);
//                    SPUtils.put(context, SPUtils.LIVE_TYPE, LiveType.SYSTEM_LIVE.getKey());
                } else {
                    mSystemLiveLayout.setVisibility(GONE);
                    mCustomLiveLayout.setVisibility(VISIBLE);
                    SPUtils.put(context, SPUtils.LIVE_TYPE, LiveType.CUSTOM_LIVE.getKey());
                }
            }
        });
    }

    public void setOnLiveChooseViewListener(OnLiveChooseViewListener onLiveChooseViewListener) {
        mOnLiveChooseViewListener = onLiveChooseViewListener;
    }

    private void initView() {
        View contentView = LayoutInflater.from(context).inflate(R.layout.view_live_choose, this);
        editText = contentView.findViewById(R.id.edit_url);
        btn_StartLive = contentView.findViewById(R.id.btn_startLive);
        btn_ScanQRCode = contentView.findViewById(R.id.btn_scan_qr_code);
        btn_stick = contentView.findViewById(R.id.btn_stick);
        listView = contentView.findViewById(R.id.btn_history);
        tv_platform_list = contentView.findViewById(R.id.tv_platform_list);
        live_platform_overlying = contentView.findViewById(R.id.live_platform_overlying);
        live_platform_listview = contentView.findViewById(R.id.live_platform_listview);

        btn_GetPushUrl = contentView.findViewById(R.id.btn_get_push_url);
        btn_GetPushUrl.setVisibility(GONE);

        mCustomLiveLayout = findViewById(R.id.ll_input);
        mSystemLiveLayout = findViewById(R.id.system_live_layout);
        mSystemLiveButton = findViewById(R.id.btn_start_system_live);
        if (GlobalVariable.isRCSEE) {
            btn_ScanQRCode.setVisibility(GONE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (VISIBLE == visibility) {
            updateHistory();
            if (mClipboardManager == null) {
                mClipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            }
            btn_stick.setClickable(mClipboardManager.hasPrimaryClip()
                    && mClipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN));
        }
    }

    /**
     * 设置推流地址
     *
     * @param url
     */
    public void setRtmpUrl(String url) {
        editText.setText(url);
    }


    private ClipboardManager mClipboardManager;

    private final Handler handler = new Handler();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.sina_live_layout:
                mOnLiveChooseViewListener.onLiveClick(LiveType.WEIBO_LIVE);
                break;
            case R.id.facebook_live_layout:
                mOnLiveChooseViewListener.onLiveClick(LiveType.FACEBOOK_LIVE);
                break;
            case R.id.custom_live_layout:
                mOnLiveChooseViewListener.onLiveClick(LiveType.CUSTOM_LIVE);
                break;
            case R.id.douyu_live_layout:
                mOnLiveChooseViewListener.onLiveClick(LiveType.DOUYU_LIVE);
                break;*/
            case R.id.btn_stick:
                if (mClipboardManager == null) {
                    mClipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                }
                if (mClipboardManager.hasPrimaryClip()
                        && mClipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);
                    CharSequence text = item.getText();
                    if (text == null) {
                        return;
                    }
                    editText.setText(text);
                }
                break;
            case R.id.btn_startLive:
                String url = editText.getText().toString().trim();
                if (!url.startsWith("rtmp://")) {

//                    dialogUtils.createDialogWithSingleBtn(context.getString(R.string.Label_WrongRtmp),
//                            context.getString(R.string.Label_WrongRtmp_content),
//                            context.getString(R.string.Label_Sure));

                } else {
                    if (tv_platform_list.getText().toString().equals(context.getString(R.string.tx_clound_live))) {   //腾讯云直播
                        mOnLiveChooseViewListener.onLiveClick(LiveType.TENCENT_LIVE, url);
                    } else {
                        mOnLiveChooseViewListener.onLiveClick(LiveType.CUSTOM_LIVE, url);
                    }
                    beginLive();
                }
                break;
            case R.id.btn_start_system_live:
                if (mOnLiveChooseViewListener != null) {
//                    mOnLiveChooseViewListener.onLiveClick(LiveType.SYSTEM_LIVE, "");
                }
                break;
            case R.id.btn_get_push_url:
                //new GetUrlAsync().execute();
                break;
            case R.id.btn_scan_qr_code:
                break;
            case R.id.tv_platform_list:
                live_platform_listview.setVisibility(VISIBLE);
                int liveType = SPUtils.getInt(context, SPUtils.LIVE_TYPE);
                if (liveType == LiveType.CUSTOM_LIVE.getKey()) {
                    selectPosition = 1;
                } else {
                    selectPosition = 0;
                }
                live_platform_listview.selectItem(selectPosition);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        live_platform_overlying.setVisibility(VISIBLE);
                    }
                }, 200);
                break;


            case R.id.live_platform_overlying:
                if (live_platform_overlying.getVisibility() == GONE) {
                    live_platform_overlying.setVisibility(VISIBLE);
                    live_platform_listview.setVisibility(VISIBLE);
                } else {
                    live_platform_overlying.setVisibility(GONE);
                    live_platform_listview.setVisibility(GONE);
                }
                break;

            default:
                break;

        }
    }


    List<LiveHistory> histories;
    private LiveHistoryAdapter liveHistoryAdapter;

    /****************
     * 更新历史
     */
    public void updateHistory() {
        histories = liveHistoryDao.queueHistry();
        if (liveHistoryAdapter == null) {
            liveHistoryAdapter = new LiveHistoryAdapter(context);
            listView.setAdapter(liveHistoryAdapter);
            listView.setOnItemClickListener(onItemClickListener);

        }
        liveHistoryAdapter.updateUI(histories);
    }

    /*************************
     *  点击事件---ron
     */
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (histories == null || histories.size() == 0) {
                return;
            }
            LiveHistory liveHistory = histories.get(position);
            editText.setText(liveHistory.History);
        }
    };

    /***************
     * 开始直播后回调
     */
    private void beginLive() {
        String url = editText.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (histories != null) {
            for (LiveHistory history : histories) {
                if (history.History.equals(url)) {
                    return;
                }
            }
        }


        if (histories != null && histories.size() > 3) {
            liveHistoryDao.deleteHistory(histories.get(3));
        }
        liveHistoryDao.insertHistory(url);
    }

    public interface OnLiveChooseViewListener {
        void onLiveClick(LiveType liveType, String rtmpUrl);

        void onOpenQRCode();

    }
}
