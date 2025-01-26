package com.gdu.demo.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.media.MediaFile;

public class MediaListAdapter extends BaseQuickAdapter<MediaFile, BaseViewHolder> {


    public MediaListAdapter() {
        super(R.layout.item_media_file);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, MediaFile mediaFile) {
        if (mediaFile != null) {
            holder.setText(R.id.tv_name, "name = " + mediaFile.getName() + "");
            holder.setText(R.id.tv_path, "path =" + mediaFile.getPath());
            holder.setText(R.id.tv_time, "size =" + mediaFile.getFileSize() + "");
            holder.setText(R.id.tv_length, "length = " + mediaFile.getDuration());
            holder.setImageBitmap(R.id.iv_cove, mediaFile.getThumbnail());
        }
    }
}
