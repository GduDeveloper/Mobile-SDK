package com.gdu.demo.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;

/**
 * RecyclerView Adapter的基类
 */

public abstract class BaseRVAdapter<T> extends BaseQuickAdapter<T, QuickViewHolder> {
    private final int mLayoutResId;

    public BaseRVAdapter(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(mLayoutResId, viewGroup);
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable T item) {
        onBindVH(quickViewHolder, item, position);
    }

    public abstract void onBindVH(final QuickViewHolder holder, T data, int position);

//    protected void setCheckedChange(CompoundButton cb, final int position) {
//        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (onViewCheckedChangeListener != null) {
//                onViewCheckedChangeListener.onViewCheckedChanged(buttonView, isChecked, position);
//            }
//        });
//    }
//
//    /**
//     * @param onViewClickListener
//     */
//    public OnViewCheckedChangeListener onViewCheckedChangeListener;
//
//    public void setOnViewCheckedChangeListener(OnViewCheckedChangeListener onViewCheckedChangeListener) {
//        this.onViewCheckedChangeListener = onViewCheckedChangeListener;
//    }
//
//    public interface OnViewCheckedChangeListener {
//        void onViewCheckedChanged(CompoundButton buttonView, boolean isChecked, int position);
//    }

}
