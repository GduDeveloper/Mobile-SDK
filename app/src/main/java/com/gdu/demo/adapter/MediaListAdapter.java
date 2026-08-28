package com.gdu.demo.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.lib.util.GlideUtils;
import com.gdu.media.MediaFile;

import java.io.File;

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
            if (!TextUtils.isEmpty(mediaFile.localThumbPath)) {
                GlideUtils.loadImage(getContext(), new File(mediaFile.localThumbPath), holder.getView(R.id.iv_cove));
            } else {
                holder.setImageResource(R.id.iv_cove, R.drawable.default_photo_video);
            }
        }
    }
}
