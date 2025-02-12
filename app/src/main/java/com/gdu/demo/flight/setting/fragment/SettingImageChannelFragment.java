package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingImageChannelBinding;
import com.gdu.demo.flight.setting.viewmodel.SettingSDRViewModel;
import com.gdu.drone.AirlinkType;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.util.CollectionUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.SPUtils;
import com.gdu.util.logger.MyLogUtils;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingImageChannelFragment extends Fragment {

    private FragmentActivity mActivity;
    private FragmentSettingImageChannelBinding mViewBinding;
    private SettingSDRViewModel sdrViewModel;
    private Handler mHandler;
    private boolean isAttached = false;

    private boolean isShowPushType;
    private CombinedData mCombinedData;
    /**
     * 上一次的图传类型
     */
    private byte mLastAirlinkType = AirlinkType.AUTO.getKey();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingImageChannelBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
        if (null != mActivity) {
            sdrViewModel = new ViewModelProvider(mActivity).get(SettingSDRViewModel.class);
        }
        initView();
        initData();
    }



    private void initView() {
        mHandler = new Handler(Looper.getMainLooper());
        sdrViewModel.getToastLiveData().observe(mActivity, data -> {
            if (data != 0) {
                Toast.makeText(mActivity, data, Toast.LENGTH_SHORT).show();
            }
        });
        showCurrentAirlinkType();
        mViewBinding.tvChartLabel.setVisibility(View.INVISIBLE);
        mViewBinding.ivChartIcon.setVisibility(View.INVISIBLE);
        mViewBinding.combinedChart.setVisibility(View.INVISIBLE);

        String[] channelNames;
        if (DroneUtil.isS200Serials()) {
            channelNames = getResources().getStringArray(R.array.array_channel_s200);
        } else {
            channelNames = getResources().getStringArray(R.array.array_channel);
        }
        mViewBinding.ovSwitchImgChannel.setData(channelNames);

        if (GlobalVariable.isRCSEE) {
            mViewBinding.groupHdmi.setVisibility(View.GONE);
        } else {
            mViewBinding.groupHdmi.setVisibility(View.VISIBLE);
        }

        if (GlobalVariable.isUseBackupsAirlink) {
            mViewBinding.layoutStream.setVisibility(View.VISIBLE);
            sdrViewModel.getVisibleLightStreamLiveData().observe(mActivity, data->{
                mHandler.post(() -> mViewBinding.ovSelectedStream.setIndex(data));
            });
            sdrViewModel.getVisibleLightStream();
        } else {
            mViewBinding.layoutStream.setVisibility(View.GONE);
        }

        initChartView();

        setListener();
    }

    private void setListener() {
        mViewBinding.ovSwitchImgChannel.setOnOptionClickListener((parentId, view, position) -> {
            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }
            if (GlobalVariable.droneFlyState != 1) {
                Toast.makeText(getContext(), R.string.string_in_flight_not_change_channel, Toast.LENGTH_SHORT).show();
            } else if(NetworkingHelper.isNetworkingMode()){
                Toast.makeText(getContext(), R.string.string_not_change_in_group, Toast.LENGTH_SHORT).show();
            } else {
                if (getPositionFromChannel(GlobalVariable.singalChannel) != position) {
                    sdrViewModel.setImageTransmissionInfo(position);
                }
            }
        });

        mViewBinding.selectHdmiCast.setIndex(SPUtils.getInt(getContext(), SPUtils.BACK_HDMI_CAST_POSITION));
        mViewBinding.selectWifiCast.setIndex(SPUtils.getInt(getContext(), SPUtils.BACK_WIFI_CAST_POSITION));

        mViewBinding.selectHdmiCast.setOnOptionClickListener((parentId, view, position) -> {
            switchHdmiCastType(position);
        });

        mViewBinding.selectWifiCast.setOnOptionClickListener((parentId, view, position) -> {
            switchWifiCastType(position);
        });
        sdrViewModel.getP301DLiveData().observe(mActivity, data->{
            mViewBinding.ivHdmiSwitch.setSelected(data);
        });
        mViewBinding.ivHdmiSwitch.setOnClickListener(view -> changeHdmiSwitch());

        sdrViewModel.getLtePushStreamTypeLiveData().observe(mActivity, data->{
            showPushTypeView();
        });
        mViewBinding.ovSwitchPushType.setOnOptionClickListener((parentId, view, position) -> {
            if (position == 0) {
                sdrViewModel.setLTEPushStreamType((byte)1);
            } else if (position == 1) {
                sdrViewModel.setLTEPushStreamType((byte)2);
            }
        });
        mViewBinding.ovSelectedStream.setOnOptionClickListener((parentId, view, position) -> changeOutStream(position));
    }

    private void switchWifiCastType(int position) {
        mHandler.post(() -> {
            SPUtils.put(getContext(), SPUtils.BACK_WIFI_CAST_POSITION,position);
            changeSelectIndex(WIFI_CAST,position);
        });
    }

    private void changeSelectIndex(int type, int position) {
        if (type == HDMI_CAST) {
            mViewBinding.selectHdmiCast.setIndex(position);
        }else if (type == WIFI_CAST){
            mViewBinding.selectWifiCast.setIndex(position);
        }
    }

    private int NO_CAST= 0;
    private int HDMI_CAST = 1;
    private int WIFI_CAST = 2;

    private void switchHdmiCastType(int position) {
        mHandler.post(() -> {
            SPUtils.put(getContext(), SPUtils.BACK_HDMI_CAST_POSITION, position);
            changeSelectIndex(HDMI_CAST,position);
        });
    }

    /**
     *  设置码流 变码流时 需要先关变码流再设置
     *
     */
    private void changeOutStream(int position) {
        if (GlobalVariable.sVariableBitstream == 1) {
            sdrViewModel.getSteamSwitchLiveData().observe(mActivity, data->{
                setStreamValue(position);
            });
            sdrViewModel.setChangeSteamSwitch((byte) 0);
        } else {
            setStreamValue(position);
        }
    }

    public void setStreamValue(int position) {
        sdrViewModel.getOutputStreamLiveData().observe(mActivity, data->{
            if (data) {
                mViewBinding.ovSelectedStream.setIndex(position);
                if (GlobalVariable.isUseBackupsAirlink) {
                    mViewBinding.layoutStream.setVisibility(View.VISIBLE);
                } else {
                    mViewBinding.layoutStream.setVisibility(View.GONE);
                }
            }
        });
        sdrViewModel.setOutputStream((byte) position);
    }

    private void initData() {
        mViewBinding.tvTvBandWithContent.setText("20MHz");
        //获取图传频率带宽
        sdrViewModel.getFrequencyBandwidthLiveData().observe(mActivity, data->{
            mViewBinding.tvTvBandWithContent.setText(data);
        });
        sdrViewModel.getITFrequencyBandwidth();
        sdrViewModel.getImageChannelLiveData().observe(mActivity, data->{
            setImgChannel(data);
        });
        sdrViewModel.getImageTransmissionInfo((byte) 3);
        if (GlobalVariable.sFourthGStatus != null) {
            mLastAirlinkType = GlobalVariable.sFourthGStatus.airlink_type;
            showPushTypeView();
        } else {
            mViewBinding.ovSwitchPushType.setIndex(0);
        }
        sdrViewModel.setP301D((byte) 3, (byte) 0);

        Observable.interval(1, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(this))
                .subscribe(aLong -> {
                    showCurrentAirlinkType();
                    generateChartData(GlobalVariable.combinedChartPoints, GlobalVariable.currentPoint);
                });
    }

    private void changeHdmiSwitch() {
        byte setStates = (byte) (mViewBinding.ivHdmiSwitch.isSelected() ? 0 : 1);
        sdrViewModel.setP301D((byte) 2, setStates);
    }

    public void showCurrentAirlinkType(){
        if (GlobalVariable.isUseBackupsAirlink) {
            if(!isShowPushType) {
                if(GlobalVariable.sFourthGStatus != null && GlobalVariable.sFourthGStatus.pushStreamType != 0) {
                    showPushTypeView();
                    isShowPushType = true;
                }
            }
            if (mViewBinding.layoutStream.getVisibility() == View.GONE) {
                mViewBinding.layoutStream.setVisibility(View.VISIBLE);
                mViewBinding.groupPushType.setVisibility(View.VISIBLE);
            }
        } else {
            if (mViewBinding.layoutStream.getVisibility() == View.VISIBLE) {
                mViewBinding.layoutStream.setVisibility(View.GONE);
                mViewBinding.groupPushType.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置信道显示位置
     * @param channel
     */
    private void setImgChannel(int channel){
        MyLogUtils.d("test setImgChannel channel="+channel);
        if (mHandler != null && isAdded() && isAttached) {
            mHandler.post(() -> {
                if (isAdded() && isAttached) {
                    int position = getPositionFromChannel(channel);
                    mViewBinding.ovSwitchImgChannel.setIndex(position);
                }
            });
        }
    }

    /**
     * 根据信道获取当前显示位置
     * @param channel 当前工作频段 0：2.4G  1：5G，S200系列多了2自动3查询的设置功能
     * @return
     */
    private int getPositionFromChannel(int channel){
        int position = 0;
        if (DroneUtil.isS200Serials()) {
            channel = sdrViewModel.getS200SetChannel();
            switch (channel) {
                case 0:
                    position = 1;
                    break;
                case 1:
                    position = 2;
                    break;
                case 2:
                    position = 0;
                    break;
            }
        } else {
            position = channel;
        }
        return position;
    }

    private void showPushTypeView() {
        if (mViewBinding != null && GlobalVariable.sFourthGStatus != null) {
            if (GlobalVariable.sFourthGStatus.pushStreamType == 1) {
                mViewBinding.ovSwitchPushType.setIndex(0);
            } else if (GlobalVariable.sFourthGStatus.pushStreamType == 2) {
                mViewBinding.ovSwitchPushType.setIndex(1);
            } else {
                MyLogUtils.w("showPushTypeView = " + GlobalVariable.sFourthGStatus.pushStreamType);
            }
        }
    }

    private void initChartView() {
        mCombinedData = new CombinedData();

        // 隐藏颜色含义控件
        mViewBinding.combinedChart.getLegend().setEnabled(false);
        mViewBinding.combinedChart.getDescription().setEnabled(false);
        mViewBinding.combinedChart.setNoDataText(getString(R.string.no_data_was_obtained));
        mViewBinding.combinedChart.setNoDataTextColor(getResources().getColor(R.color.color_EF4E22));
        mViewBinding.combinedChart.setDrawGridBackground(false);
        mViewBinding.combinedChart.setDrawBarShadow(false);
        mViewBinding.combinedChart.setHighlightPerTapEnabled(false);
        mViewBinding.combinedChart.setHighlightFullBarEnabled(false);
        mViewBinding.combinedChart.setDragEnabled(false);

        final YAxis rightYAxis = mViewBinding.combinedChart.getAxisRight();
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawAxisLine(true);
        rightYAxis.setEnabled(true);
        //网格虚线
        rightYAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        rightYAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        rightYAxis.setAxisMinimum(-110f);
        rightYAxis.setAxisMaximum(-50f);
        rightYAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        rightYAxis.setTextColor(getResources().getColor(R.color.black));

        //y轴配置
        final YAxis leftYAxis = mViewBinding.combinedChart.getAxisLeft();
        //网格虚线
        leftYAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        leftYAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        leftYAxis.setAxisMinimum(-110f);
        leftYAxis.setAxisMaximum(-50f);
        leftYAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        leftYAxis.setTextColor(getResources().getColor(R.color.black));
//        leftYAxis.setLabelCount(5);

        //x轴配置
        final XAxis xAxis = mViewBinding.combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(1f);
        xAxis.setGranularity(1f);
        //网格虚线
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //网格颜色
        xAxis.setGridColor(getResources().getColor(R.color.color_EF4E22));
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_EF4E22));
        xAxis.setTextColor(getResources().getColor(R.color.black));
    }

    /**
     * 添加柱状图数据
     */
    private void generateChartData(List<Short> shortList, byte selectNum) {
        if (!sdrViewModel.isImgChannelSwitching()) {
            if (!DroneUtil.isS200Serials()) {
                setImgChannel(GlobalVariable.singalChannel);
                mViewBinding.tvCurrentChannel.setVisibility(View.GONE);
            }else {
                mViewBinding.tvCurrentChannel.setVisibility(View.VISIBLE);
                mViewBinding.tvCurrentChannel.setText(getString(R.string.current_channel_is,
                        getString(GlobalVariable.singalChannel == 1?
                                R.string.Label_channel_5_8:
                                R.string.Label_channel_2_4)));
            }
        }
        if (!isAdded() || !getUserVisibleHint()) {
            return;
        }
        if (CollectionUtils.isEmptyList(shortList)) {
            return;
        }
        mViewBinding.tvChartLabel.setVisibility(View.VISIBLE);
        mViewBinding.ivChartIcon.setVisibility(View.VISIBLE);
        mViewBinding.combinedChart.setVisibility(View.VISIBLE);
        final BarData mBarData = new BarData();
        List<BarEntry> barEntryList = new ArrayList<>();

        final LineData mLineData = new LineData();
        final ArrayList<Entry> mLineEntryData = new ArrayList<>();

        for (int index = 0; index < shortList.size(); index++) {
            mLineEntryData.add(new Entry(index + 1, shortList.get(index)));
            if (selectNum == index) {
                barEntryList.add(new BarEntry(selectNum + 1, -110));
            }
        }

        final LineDataSet mLineDataSet = new LineDataSet(mLineEntryData, getString(R.string.channel_interference_reduction));
        mLineDataSet.setColor(getResources().getColor(R.color.color_EF4E22));
        mLineDataSet.setLineWidth(1f);
        mLineDataSet.setCircleColor(getResources().getColor(R.color.color_EF4E22));
        mLineDataSet.setCircleRadius(3f);
        mLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        mLineDataSet.setDrawValues(false);
        mLineDataSet.setValueTextSize(10f);
        mLineDataSet.setValueTextColor(Color.WHITE);

        mLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        mLineData.addDataSet(mLineDataSet);
        mCombinedData.setData(mLineData);
        mViewBinding.combinedChart.setData(mCombinedData);


        final BarDataSet amountBar = new BarDataSet(barEntryList, getString(R.string.channel_interference_reduction));
        amountBar.setAxisDependency(YAxis.AxisDependency.LEFT);
        // 柱状颜色
        amountBar.setColor(Color.rgb(0,128,0));
        amountBar.setValueTextSize(10);
        amountBar.setValueTextColor(getResources().getColor(R.color.white));
        mBarData.addDataSet(amountBar);
        //设置柱状图显示的大小
        mBarData.setBarWidth(0.2f);
        //设置是否显示数据点的数值
        mBarData.setDrawValues(false);
        mCombinedData.setData(mBarData);
        //以下是为了解决 柱x状图 左右两边只显示了一半的问题 根据实际情况而定
        mViewBinding.combinedChart.getXAxis().setAxisMinimum(0.75f);
        mViewBinding.combinedChart.getXAxis().setAxisMaximum((float) (mLineEntryData.size() + 0.25));
        mViewBinding.combinedChart.invalidate();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public static SettingImageChannelFragment newInstance() {
        Bundle args = new Bundle();
        SettingImageChannelFragment fragment = new SettingImageChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
