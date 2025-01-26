package com.gdu.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.gdu.demo.R;
import com.gdu.demo.utils.ScreenUtils;

/**
 * @author wuqb
 * @date 2025/1/28
 * 统一Spinner下拉选择控件
 * @since 2.1.16 统一样式大小sp11 颜色color_3E505C
 *        2.1.18 新增向上弹出和间距
 *        2.1.65 超出滚动改为换行显示 使用maxwidth、minHeight、layout_height="wrap_content"配合
 */

public class GduSpinner extends AppCompatTextView implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    private PopupWindow mSpinnerPopup;

    private final ListView mPopupListView;

    private String[] mOptionString;

    private OnOptionClickListener mOnOptionClickListener;

    private int mPopupWindowHeight;

    private OptionsAdapter mOptionsAdapter;

    public GduSpinner(Context context) {
        this(context,null);
    }

    public GduSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GduSpinner);
        if (ta.hasValue(R.styleable.GduSpinner_viewHeight)) {
            mPopupWindowHeight = ta.getDimensionPixelOffset(R.styleable.GduSpinner_viewHeight, mPopupWindowHeight);
        }
        if (ta.hasValue(R.styleable.GduSpinner_optionArray)) {
            int array = ta.getResourceId(R.styleable.GduSpinner_optionArray, 0);
            this.mOptionString = getResources().getStringArray(array);
        }
        if (mOptionString != null && mOptionString.length > 0) {
            setText(mOptionString[0]);
        }
        setSelected(true);
        mPopupListView = new ListView(context);
        mPopupListView.setVerticalScrollBarEnabled(false);
        init();
        ta.recycle();
    }

    private void init() {
        mOptionsAdapter = new OptionsAdapter();
        mPopupListView.setAdapter(mOptionsAdapter);
        mPopupListView.setDivider(null);
        setOnClickListener(this);
        mPopupListView.setOnItemClickListener(this);
        // 设置统一样式
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sp_11));
        setTextColor(getResources().getColor(R.color.color_3E505C, null));
        setBackgroundResource(R.drawable.spinner_bg_r2_arrow);
        int paddingStart = getResources().getDimensionPixelSize(R.dimen.dp_6);
        int paddingEnd = getResources().getDimensionPixelSize(R.dimen.dp_20);
        int paddingVertical = getResources().getDimensionPixelSize(R.dimen.dp_1);
        setPadding(paddingStart + 1, paddingVertical, paddingEnd, paddingVertical);
        setLineSpacing(0, 0.95f);
        if(getEllipsize() == TextUtils.TruncateAt.MARQUEE) {
            setHeight(getResources().getDimensionPixelSize(R.dimen.dp_20));
        } else {
            int minH = getResources().getDimensionPixelSize(R.dimen.dp_20);
            if(getMinHeight() < minH) {
                setMinHeight(minH);
            }
            setMaxLines(3);
            if(getEllipsize() == null) {
                setEllipsize(TextUtils.TruncateAt.END);
            }
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    if(layoutParams != null) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if(layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            setLayoutParams(layoutParams);
                        }
                    }
                }
            });

        }
        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }

    /**
     * java代码设置值
     * @param datas
     */
    public void setData(String[] datas){
        mOptionString = datas;
        setText(mOptionString[0]);
        mOptionsAdapter.notifyDataSetChanged();
    }


    public void setOnOptionClickListener(OnOptionClickListener onOptionClickListener) {
        mOnOptionClickListener = onOptionClickListener;
    }

    public void setIndex(final int index) {
        post(() -> {
            try {
                String value = "";
                if (mOptionString != null) {
                    if (mOptionString.length > index) {
                        value = mOptionString[index];
                    } else {
                        value = mOptionString[mOptionString.length - 1];
                    }
                }
                setText(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mOptionString != null && mOptionString.length > 0) {
            showOptionPop();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnOptionClickListener != null) {
            mOnOptionClickListener.onOptionClick(getId(), ((LinearLayout)view).getChildAt(0), position);
        }
        mSpinnerPopup.dismiss();
    }

    class OptionsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mOptionString != null) {
                return mOptionString.length;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mOptionString != null) {
                return mOptionString[position];
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.widget_spinner_item, parent, false);
            } else {
                view = convertView;
            }
            TextView option = view.findViewById(R.id.option_tv);
            int margin = 1;// dp0.5
            ViewGroup.LayoutParams layoutParams = option.getLayoutParams();
            if(getCount() > 1 && layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParam = (ViewGroup.MarginLayoutParams)layoutParams;
                if (position == 0) {
                    marginParam.bottomMargin = margin;
                } else if(position == getCount() -1) {
                    marginParam.topMargin = margin;
                } else {
                    marginParam.topMargin = marginParam.bottomMargin = margin;
                }
                option.setLayoutParams(marginParam);
            }
            option.setText(mOptionString[position]);
            option.setTextSize(TypedValue.COMPLEX_UNIT_PX, GduSpinner.this.getTextSize());
            option.setSelected(mOptionString[position].equals(getText().toString()));
            return view;
        }
    }

    private void showOptionPop() {
        int[] position = new int[2];
        getLocationOnScreen(position);
        int screenHeight = ScreenUtils.getScreenHeight(getContext());
        int HeightToBottom = screenHeight  - position[1] - getHeight();
        int marginDis = getResources().getDimensionPixelSize(R.dimen.dp_3);
        int contentH = getListViewHeightBasedOnChildren(mPopupListView, getWidth());
        int showH = Math.min(contentH, screenHeight - marginDis * 10);
        boolean isShowDown = HeightToBottom > showH + marginDis * 1.5;
        if (mSpinnerPopup == null) {
            mSpinnerPopup = new PopupWindow(mPopupListView, getWidth(), showH);
            mSpinnerPopup.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shape_bg_white_r2));
            mSpinnerPopup.setElevation(getResources().getDimension(R.dimen.dp_1));
            mSpinnerPopup.setFocusable(false);
            mSpinnerPopup.setOutsideTouchable(true);
        } else {
            // 没有初始化时需重新设置宽度与依附的view下拉框宽度相等
            mSpinnerPopup.setWidth(getWidth());
            mSpinnerPopup.setHeight(showH);
        }
        mSpinnerPopup.showAsDropDown(this,0, isShowDown? marginDis : -marginDis);
    }

    public void hideOptionPop() {
        if (mSpinnerPopup != null && mSpinnerPopup.isShowing()) {
            mSpinnerPopup.dismiss();
        }
    }

    public static int getListViewHeightBasedOnChildren(ListView listView, int width) {
        int totalHeight = 0;
        ListAdapter listAdapter = listView.getAdapter();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),0);
            totalHeight += listItem.getMeasuredHeight();
        }
        totalHeight += listView.getPaddingTop() + listView.getPaddingBottom();
        totalHeight += listView.getDividerHeight() * (listAdapter.getCount() - 1);
        return totalHeight;
    }

    public interface OnOptionClickListener {
        void onOptionClick(int parentId, View view, int position);
    }
}
