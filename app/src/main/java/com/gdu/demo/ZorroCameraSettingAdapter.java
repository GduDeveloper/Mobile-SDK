package com.gdu.demo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

import com.gdu.util.logs.RonLog;

/**
 * Created by Administrator on 2016/12/15.
 */
public class ZorroCameraSettingAdapter extends BaseAdapter /*implements AdapterView.OnItemSelectedListener*/{

    private  String[] strArray;
    private Context context;
    private Gallery gallery;
    private boolean isPhoto;
    private int[] ivArray;
    //private int selectPos;

    public ZorroCameraSettingAdapter(Context context, String[]strArray, Gallery gallery, boolean isPhoto, int[] ivArray) {
        this.strArray = strArray;
        this.context = context;
        this.gallery = gallery;
        this.isPhoto=isPhoto;
        this.ivArray=ivArray;
       // gallery.setOnItemSelectedListener(this);
        for (String str:this.strArray)
        {
            RonLog.LogE("1111111:"+ str);
        }
    }

    @Override
    public int getCount() {
        if (isPhoto){
            return ivArray.length+1;
        }else{
            return strArray.length;
        }
    }

    @Override
    public Object getItem(int position) {
        if (isPhoto){
            return ivArray[position];
        }else{
            return strArray[position];
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
                convertView = View.inflate(context, R.layout.camera_setting_gallery_item1,null);
                holder.text1=(TextView)convertView.findViewById(R.id.tv_item1);
                convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
            holder.text1.setText(strArray[position]);

        return convertView;
    }

  /*  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //selectPos = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }*/

    class ViewHolder {
        private TextView text;
        private TextView text1;
    }
}
