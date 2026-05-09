package com.gdu.demo.widget.psdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.gdu.demo.R;
import com.gdu.demo.widget.psdk.adapter.ItemAdapter;
import com.gdu.demo.widget.psdk.bean.WidgetItemBean;

import java.util.List;

/**
 * @Date: 2022/10/22
 */
public class CustomRecyclerView extends LinearLayout {

    private final Context context;

    private RecyclerView recyclerView;

    private ItemAdapter itemAdapter;

    private OnItemClickListener mOnItemClickListener;


    public CustomRecyclerView(Context context) {
        this(context, null);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_recycler, this);
        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        itemAdapter = new ItemAdapter(context);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child != null) {
                        int position = rv.getChildAdapterPosition(child);
                        itemAdapter.setSelectedPosition(position);
                        mOnItemClickListener.onItemClick(position);
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    public void setData(List<WidgetItemBean.ListItemBean> list) {
        if (itemAdapter != null) {
            itemAdapter.setData(list);
        }
    }

    public void addData(List<WidgetItemBean.ListItemBean> list) {
        if (itemAdapter != null) {
            itemAdapter.setData(list);
        }
    }

    public void setSelectedPosition(int position) {
        if (itemAdapter != null) {
            itemAdapter.setSelectedPosition(position);
        }
    }


    public void setItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}


