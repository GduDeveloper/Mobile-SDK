package com.gdu.demo.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.gdu.demo.GduGallery;
import com.gdu.demo.R;
import com.gdu.demo.ZorroCameraSettingAdapter;
import com.gdu.util.logs.RonLog;

/**
 * Created by Woo on 2018-6-19.
 */
public class SelectListDialog {
    private Dialog dialog;

    private ValueType type;

    private int selectPostion;

    public void createDialog(final String[] data, Activity activity,
                             final ValueType type, final TestData.OnSelectValueListener onValueLis) {
        if (dialog != null && dialog.isShowing()) {
            RonLog.LogE("dialog正在显示");
            return;
        }
        this.type = type;
        dialog = new Dialog(activity, R.style.NormalDialog);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_list_dialog, null);
        GduGallery gduGallery = (GduGallery) view.findViewById(R.id.gallery_exposure);
        gduGallery.setCallbackDuringFling(false);
        if (data != null) {
            ZorroCameraSettingAdapter zorroCameraSettingAdapter =
                    new ZorroCameraSettingAdapter(activity, data, gduGallery, false, null);
            gduGallery.setAdapter(zorroCameraSettingAdapter);
            gduGallery.setOnGduGalleryListener(new GduGallery.OnGduGalleryListener() {
                @Override
                public void onItemSelected(int position) {
                    selectPostion = position;
                }
            });
        }

        Button button = (Button) view.findViewById(R.id.btn_sure);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onValueLis != null) {
                    onValueLis.onSelectValue(selectPostion, type);
                }
                dialog.cancel();
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }
}
