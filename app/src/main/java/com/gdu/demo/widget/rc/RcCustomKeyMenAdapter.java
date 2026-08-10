package com.gdu.demo.widget.rc;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.gdu.demo.R;
import com.gdu.demo.widget.rc.key.RCKeyActionItem;
import com.gdu.lib.util.LanguageUtil;

/**
 * @Author: lixiqiang
 * @Date: 2022/8/5
 */
public class RcCustomKeyMenAdapter extends BaseQuickAdapter<RCKeyActionItem, BaseViewHolder> {

    public RcCustomKeyMenAdapter() {
        super(R.layout.item_rc_custom_key_menu);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, RCKeyActionItem menu) {
        if (menu == null) {
            return;
        }
        holder.setText(R.id.tv_menu, menu.getActionName());
        if (LanguageUtil.getLocal(getContext()).getLanguage().equals("ru")){
            ((AppCompatTextView)holder.getView(R.id.tv_menu)).setTextSize(12);
        }
    }
}
