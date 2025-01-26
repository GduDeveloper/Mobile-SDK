package com.gdu.demo.flight.pre.adapter;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.demo.flight.pre.bean.BaseFlightStatusBean;
import com.gdu.demo.widget.FlightStatusItemView;


/**
 * @author wuqb
 * @date 2025/1/21
 * @description 飞行检查页飞行状态检查适配器
 */
public class PreFlightStatusAdapter extends BaseQuickAdapter<BaseFlightStatusBean, BaseViewHolder> {

    public PreFlightStatusAdapter() {
        super(R.layout.adapter_pre_flight_status);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, BaseFlightStatusBean statusBean) {
        FlightStatusItemView status = holder.getView(R.id.pre_flight_status_content);
        if (null!=statusBean) {
            status.setTitle(statusBean.getTitle());
            if (statusBean.getContentStrId() != 0){
                status.setContent(statusBean.getContentStrId());
            }else {
                status.setContent(statusBean.getContent());
            }

            status.setContentEnable(statusBean.isContentEnable());
            status.setContentSelect(statusBean.isContentSelect());
            if (statusBean.getContentTextColor() != 0)
                status.setContentTextColor(ResourcesCompat.getColor(status.getResources(),
                        statusBean.getContentTextColor(), null));
        }
    }
}
