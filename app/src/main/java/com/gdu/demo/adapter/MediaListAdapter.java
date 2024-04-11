package com.gdu.demo.adapter;



import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.media.MediaFileBean;

public class MediaListAdapter extends BaseQuickAdapter<MediaFileBean, BaseViewHolder> {



    public MediaListAdapter() {
        super(R.layout.item_media_file);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, MediaFileBean mediaFile) {
        if (mediaFile != null) {
            holder.setText(R.id.tv_name, "name = " + mediaFile.getName() + "");
            holder.setText(R.id.tv_path, "path =" + mediaFile.getRaw().getPath());
            holder.setText(R.id.tv_time, "size =" + mediaFile.getRaw().getFilesize() + "");
            holder.setText(R.id.tv_length, "length = " + mediaFile.getDuration());
            holder.setImageBitmap(R.id.iv_cove, mediaFile.getThum_bitmap());
        }
    }
}
