package com.gdu.demo.widget.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.gdu.demo.R;

import java.util.List;

/**
 * Created by Woo on 2019-1-8.
 * 自由直播的历史展示
 */

public class LiveHistoryAdapter extends BaseAdapter
{
    private List<LiveHistory> histories;

    private final Context context;

    public LiveHistoryAdapter(Context context)
    {
        this.context = context;
    }

    public void updateUI( List<LiveHistory> lists )
    {
        this.histories = lists;
        this.notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return histories == null||histories.size() == 0?1:histories.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
       View view =  LayoutInflater.from( context ).inflate(R.layout.adapter_live_history,null);
       TextView textView = view.findViewById(R.id.tv_TextView);
        if(histories == null || histories.size() == 0 )
        {
            textView.setText(R.string.Label_Nothing);
        }else
        {
            textView.setText(histories.get(position).History);
        }
        return view;
    }
}
