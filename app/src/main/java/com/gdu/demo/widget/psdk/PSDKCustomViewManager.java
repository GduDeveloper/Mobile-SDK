package com.gdu.demo.widget.psdk;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gdu.common.error.GDUError;
import com.gdu.config.GlobalVariable;

import com.gdu.demo.R;
import com.gdu.demo.widget.psdk.bean.ImageIconBean;
import com.gdu.demo.widget.psdk.bean.PSdkCustomViewBean;
import com.gdu.demo.widget.psdk.bean.WidgetItemBean;
import com.gdu.demo.widget.psdk.widget.CustomFloatWindow;
import com.gdu.demo.widget.psdk.widget.CustomProgressLayout;
import com.gdu.demo.widget.psdk.widget.CustomRecyclerView;
import com.gdu.sdk.psdk.PSDKManager;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/20
 */
public class PSDKCustomViewManager {

    private static final String TAG = "PSDKCustomViewManager";

    private final Context mContext;

    private final Handler handler;

    private String floatWindowStr = "";

    private PSdkCustomViewBean mPSDKCustomViewBean;

    private RelativeLayout mRootLayout;

    private LinearLayout mRootView;

    private CustomFloatWindow floatWindowView;


    private Disposable disposable;

    private Disposable switchDisposable;

    private Map<String, Bitmap> iconsMap = new HashMap<>();
    private final HashMap<Integer, ImageIconBean> iconBeansMap = new HashMap<>();


    private long clickTime = 0;

    private int iconNum = 0;

    private List<View> showViewList = new ArrayList<>();


    public PSDKCustomViewManager(Context context) {
        mContext = context;
        handler = new Handler();
        PSDKManager.getInstance().addCallBack(new PSDKManager.CallBack() {
            @Override
            public void onCustomViewJsonUpdate(String viewJson) {
                mPSDKCustomViewBean = new Gson().fromJson(viewJson, PSdkCustomViewBean.class);
                AppLog.d(TAG, "viewJson : " + viewJson);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }

            @Override
            public void onIconNamesUpdate(List<String> list) {

            }

            @Override
            public void onIconsUpdate(Map<String, Bitmap> map) {
                AppLog.d(TAG, "onIconsUpdate: " + map);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iconsMap = map;
                        updateView();
                    }
                });
            }

            @Override
            public void onFlowWindowStateUpdate(String s) {
                floatWindowStr = s;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateFloatWindow();
                    }
                });
//                AppLog.d(TAG, "onFlowWindowStateUpdate: " + s);
            }

            @Override
            public void onWidgetStateUpdate(int viewId, int viewType, int value) {
                AppLog.d(TAG, "onWidgetStateUpdate viewId: " + viewId + ", viewType: " + viewType + ", value: " + value);
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateViews(viewId, viewType, value);
                        }
                    });
                }
            }
        });
        PSDKManager.getInstance().getCustomView();

//        GduApplication.getSingleApp().gduCommunication.addCycleACKCB(GduSocketConfig3.CYCLE_CUSTOM_VIEW_STATE_STATE_MSG_ID, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (bean != null && bean.frameContent != null && bean.frameContent.length >= 7) {
//                    int viewId = ByteUtilsLowBefore.byte2short(bean.frameContent, 0);
//                    int viewType = bean.frameContent[2];
//                    int value = ByteUtilsLowBefore.byte2Int(bean.frameContent, 3);
//
//                    if (handler != null) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateViews(viewId, viewType, value);
//                            }
//                        });
//                    }
//                }
//            }
//        });

    }

    public void addView(RelativeLayout viewGroup, LinearLayout rootView){
        mRootLayout = viewGroup;
        mRootView = rootView;
    }

    public void updateView() {
        PSdkCustomViewBean viewBean = mPSDKCustomViewBean;
        MyLogUtils.i("addView()");
        try {
            mRootView.removeAllViews();
            iconBeansMap.clear();
            showViewList.clear();
            iconNum = 0;

            if (viewBean != null && viewBean.getMain_interface() != null && viewBean.getMain_interface().getFloating_window() != null
                    && viewBean.getMain_interface().getFloating_window().getIs_enable()) {
                addFloatWindow(mContext.getApplicationContext(), mRootLayout);
            }

            if (viewBean != null && viewBean.getMain_interface() != null && viewBean.getMain_interface().getWidget_list() != null) {
                MyLogUtils.i("addView() list handle");
                List<WidgetItemBean> list = viewBean.getMain_interface().getWidget_list();
                for (int i = 0; i < list.size(); i++) {
                    WidgetItemBean item = list.get(i);
                    if ("button".equals(item.getWidget_type())) {
                        addButtonView(mContext.getApplicationContext(), mRootView, item);
                        iconNum++;
                    } else if ("switch".equals(item.getWidget_type())) {
                        addSwitch(mContext.getApplicationContext(), mRootView, item);
                        iconNum++;
                    } else if ("list".equals(item.getWidget_type())) {
                        addListView(mContext.getApplicationContext(), mRootLayout, mRootView, item);
                        iconNum++;
                    } else if ("scale".equals(item.getWidget_type())) {
                        addScaleView(mContext.getApplicationContext(), mRootLayout, mRootView, item);
                        iconNum++;
                    }
                }
                updateIconState();

            } else {
                mRootView.removeAllViews();
            }
        } catch (Exception e) {
            MyLogUtils.e("addView error", e);
        }

    }

    private void updateIconState() {

        disposableSwitch();
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe( Disposable d) {
                        switchDisposable  = d;
                    }

                    @Override
                    public void onNext( Long aLong) {
                        for (Map.Entry<Integer, ImageIconBean> entry : iconBeansMap.entrySet()) {
                            int key = entry.getKey();
                            ImageIconBean iconBean = entry.getValue();
                            // 处理每个键值对的逻辑
                            System.out.println(key + "：" + iconBean.getUnSelectedIconName());
                            PSDKManager.getInstance().getPSDKWidgetState((short) iconBean.getId(), (byte) iconBean.getType(), new CommonCallbacks.CompletionCallbackWith() {
                                @Override
                                public void onSuccess(Object o) {
                                    int state = (int) o;
                                    if (handler != null) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (iconBean.getType() == 2) {
                                                    updateIconBean(iconBean.getId(), state == 1);
                                                } else if (iconBean.getType() == 3) {
                                                    iconBean.getProgressLayout().setProgress(state);
                                                } else if (iconBean.getType() == 4) {
                                                    iconBean.getCustomRecyclerView().setSelectedPosition(state);
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(GDUError gduError) {

                                }
                            });
//                            GduApplication.getSingleApp().gduCommunication.psdkWidgetGetState((short) iconBean.getId(), (byte) iconBean.getType(), new SocketCallBack3() {
//                                @Override
//                                public void callBack(int code, GduFrame3 bean) {
//                                    if (code == GduConfig.OK) {
//                                        int state = ByteUtilsLowBefore.byte2Int(bean.frameContent, 5);
////                                        MyLogUtils.d("updateIconBean   id =  " + iconBean.getId() + ", type = " + iconBean.getType() + ", state = " + state);
//                                        if (handler != null) {
//                                            handler.post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    if (iconBean.getType() == 2) {
//                                                        updateIconBean(iconBean.getId(), state == 1);
//                                                    } else if (iconBean.getType() == 3) {
//                                                        iconBean.getProgressLayout().setProgress(state);
//                                                    } else if (iconBean.getType() == 4) {
//                                                        iconBean.getCustomRecyclerView().setSelectedPosition(state);
//                                                    }
//                                                }
//                                            });
//                                        }
//                                    }
//                                }
//                            });

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void addButtonView(Context context, LinearLayout rootView, WidgetItemBean itemBean) {
        AppLog.d(TAG, "addButtonView");
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_24),
                (int) context.getResources().getDimension(R.dimen.dp_24));

        params.leftMargin = (int) context.getResources().getDimension(R.dimen.dp_16);
        imageView.setLayoutParams(params);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - clickTime < 1000) {
                    return;
                }
                changeButtonStatus(itemBean.getWidget_index(), (byte) 1);
                changeButtonStatus(itemBean.getWidget_index(), (byte) 0);
            }
        });

        ImageIconBean iconBean = new ImageIconBean();
        iconBean.setId(itemBean.getWidget_index());
        iconBean.setImageView(imageView);
        iconBean.setType(1);

        if (itemBean.getIcon_file_set() != null) {
            iconBean.setSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_selected());
            iconBean.setUnSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_unselected());
        }
        iconBeansMap.put(itemBean.getWidget_index(), iconBean);

        imageView.setClickable(true);
        imageView.setEnabled(true);
        Bitmap selectedBitmap = iconsMap.get(iconBean.getSelectedIconName());
        Bitmap unSelectedBitmap = iconsMap.get(iconBean.getUnSelectedIconName());
        StateListDrawable selector = new StateListDrawable();
        Drawable selectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), selectedBitmap);
        Drawable unselectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), unSelectedBitmap);
        selector.addState(new int[]{android.R.attr.state_pressed}, selectedDrawable);
        selector.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        // 添加一个默认状态, 默认状态必须写在其他状态的最后面, 否则其他状态失效
        selector.addState(new int[]{}, unselectedDrawable);
        imageView.setImageDrawable(selector);

        rootView.addView(imageView);
    }

    private void changeButtonStatus(int iconId, byte type) {
        PSDKManager.getInstance().setPSDKWidgetState((short) iconId, type, 0, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError gduError) {

            }
        });
    }


    private void addSwitch(Context context, LinearLayout rootView, WidgetItemBean itemBean) {
        AppLog.d(TAG, "addSwitch");
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_24),
                (int) context.getResources().getDimension(R.dimen.dp_24));

        params.leftMargin = (int) context.getResources().getDimension(R.dimen.dp_16);
        imageView.setLayoutParams(params);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (System.currentTimeMillis() - clickTime < 1000) {
                    return ;
                }
                clickTime = System.currentTimeMillis();

                byte change = 1;
                ImageIconBean bean = getIconBean(itemBean.getWidget_index());
                if (bean != null) {
                    change = (byte) (bean.isSelected() ? 0 : 1);
                }
//                MyLogUtils.d("SwitchButton   change = " + change);
                byte finalChange = change;
                PSDKManager.getInstance().setPSDKWidgetState((short) itemBean.getWidget_index(), (byte) 2, finalChange, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError gduError) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (gduError == null) {
                                    updateIconBean(itemBean.getWidget_index(), finalChange == 1);
                                } else {
//                                    Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
                                }
                            }
                        });
                    }
                });
//                GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 2, finalChange, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (code == GduConfig.OK) {
//                                    updateIconBean(itemBean.getWidget_index(), finalChange == 1);
//                                } else {
//                                    Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
//                                }
//                            }
//                        });
//                    }
//                });
            }
        });

        ImageIconBean iconBean = new ImageIconBean();
        iconBean.setId(itemBean.getWidget_index());
        iconBean.setImageView(imageView);
        iconBean.setType(2);

        if (itemBean.getIcon_file_set() != null) {
            iconBean.setSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_selected());
            iconBean.setUnSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_unselected());
        }
        iconBeansMap.put(itemBean.getWidget_index(), iconBean);


        String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
        if (!TextUtils.isEmpty(name) && iconsMap.get(name) != null) {
            imageView.setImageBitmap(iconsMap.get(name));
        }

        imageView.setClickable(true);
        imageView.setEnabled(true);
//        MyLogUtils.d("addSwitch   " + iconBean.getSelectedIconName() + ", " + iconBean.getUnSelectedIconName());
        Bitmap selectedBitmap = iconsMap.get(iconBean.getSelectedIconName());
        Bitmap unSelectedBitmap = iconsMap.get(iconBean.getUnSelectedIconName());
        StateListDrawable selector = new StateListDrawable();
        Drawable selectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), selectedBitmap);
        Drawable unselectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), unSelectedBitmap);
        selector.addState(new int[]{android.R.attr.state_pressed}, selectedDrawable);
        selector.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        // 添加一个默认状态, 默认状态必须写在其他状态的最后面, 否则其他状态失效
        selector.addState(new int[]{}, unselectedDrawable);
        imageView.setImageDrawable(selector);

        rootView.addView(imageView);

    }


    private void addListView(Context context, ViewGroup viewGroup, LinearLayout rootView, WidgetItemBean itemBean) {
        AppLog.d(TAG, "addListView");
        CustomRecyclerView recyclerView = new CustomRecyclerView(context);
        showViewList.add(recyclerView);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_120),
                (int) context.getResources().getDimension(R.dimen.dp_140));
        // 上对齐
        layoutParams.addRule(RelativeLayout.ALIGN_TOP , R.id.layout_custom_root);
        // 左对齐
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.layout_custom_root);
        layoutParams.topMargin = (int) context.getResources().getDimension(R.dimen.dp_30);
        layoutParams.leftMargin = (int) (context.getResources().getDimension(R.dimen.dp_16)
                + context.getResources().getDimension(R.dimen.dp_40) * iconNum);
        recyclerView.setLayoutParams(layoutParams);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setData(itemBean.getList_item());
//        recyclerView.setItemClickListener((adapter, view, position) -> {
//            PSDKManager.getInstance().psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 4, position, (CommonCallbacks.CompletionCallback) gduError -> {
//                if (gduError == null) {
//                    recyclerView.setSelectedPosition(position);
//                } else {
////                        Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
//                }
//            });
//            GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 4, position, (code, bean) -> handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == GduConfig.OK) {
//                        recyclerView.setSelectedPosition(position);
//                    } else {
//                        Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
//                    }
//                }
//            }));
//        });

        viewGroup.addView(recyclerView);
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_24),
                (int) context.getResources().getDimension(R.dimen.dp_24));

        params.leftMargin = (int) context.getResources().getDimension(R.dimen.dp_16);
        imageView.setLayoutParams(params);
        imageView.setSelected(false);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerView.getVisibility() == View.GONE) {
                    setWidgetVisible(false);
                }
                recyclerView.setVisibility(recyclerView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        ImageIconBean iconBean = new ImageIconBean();
        iconBean.setId(itemBean.getWidget_index());
        iconBean.setImageView(imageView);
        iconBean.setType(4);
        iconBean.setCustomRecyclerView(recyclerView);
        if (itemBean.getIcon_file_set() != null) {
            iconBean.setSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_selected());
            iconBean.setUnSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_unselected());
        }
        iconBeansMap.put(itemBean.getWidget_index(), iconBean);

        String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
        if (!TextUtils.isEmpty(name)) {
            iconBean.getImageView().setImageBitmap(iconsMap.get(name));
        }

        imageView.setClickable(true);
        imageView.setEnabled(true);
//        MyLogUtils.d("addSwitch   " + iconBean.getSelectedIconName() + ", " + iconBean.getUnSelectedIconName());
        Bitmap selectedBitmap = iconsMap.get(iconBean.getSelectedIconName());
        Bitmap unSelectedBitmap = iconsMap.get(iconBean.getUnSelectedIconName());
        StateListDrawable selector = new StateListDrawable();
        Drawable selectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), selectedBitmap);
        Drawable unselectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), unSelectedBitmap);
        selector.addState(new int[]{android.R.attr.state_pressed}, selectedDrawable);
        selector.addState(new int[]{android.R.attr.state_selected}, unselectedDrawable);
        // 添加一个默认状态, 默认状态必须写在其他状态的最后面, 否则其他状态失效
        selector.addState(new int[]{}, unselectedDrawable);
        imageView.setImageDrawable(selector);

        rootView.addView(imageView);

    }

    private void addScaleView(Context context, RelativeLayout viewGroup, LinearLayout rootView, WidgetItemBean itemBean) {
        AppLog.d(TAG, "addScaleView");
        CustomProgressLayout progressLayout = new CustomProgressLayout(context);
        showViewList.add(progressLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_30),
                (int) context.getResources().getDimension(R.dimen.dp_220));

        //上对齐
        layoutParams.addRule(RelativeLayout.ALIGN_TOP , R.id.layout_custom_root);
        // 左对齐
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.layout_custom_root);
        layoutParams.topMargin = (int) context.getResources().getDimension(R.dimen.dp_20);
        layoutParams.leftMargin = (int) (context.getResources().getDimension(R.dimen.dp_16)
                + context.getResources().getDimension(R.dimen.dp_40) * iconNum);

        progressLayout.setLayoutParams(layoutParams);
        progressLayout.setVisibility(View.GONE);
        progressLayout.setChangeListener(new CustomProgressLayout.SeekBarChangeListener() {
            @Override
            public void onChange(int startProgress, int progress) {
                PSDKManager.getInstance().setPSDKWidgetState((short) itemBean.getWidget_index(), (byte) 3, progress, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError gduError) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (gduError == null) {
                                    progressLayout.setProgress(progress);
                                } else {
//                                    Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
                                }
                            }
                        });
                    }
                });
//                GduApplication.getSingleApp().gduCommunication.psdkWidgetChange((short) itemBean.getWidget_index(), (byte) 3, progress, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (code == GduConfig.OK) {
//                                    progressLayout.setProgress(progress);
//                                } else {
//                                    Toaster.show(GduActivityManager.getInstance().getTopActivity().getString(R.string.Label_SettingFail));
//                                }
//                            }
//                        });
//                    }
//                });
            }
        });

        viewGroup.addView(progressLayout);

        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.dp_24),
                (int) context.getResources().getDimension(R.dimen.dp_24));

        params.leftMargin = (int) context.getResources().getDimension(R.dimen.dp_16);
        imageView.setLayoutParams(params);
        imageView.setSelected(false);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progressLayout.getVisibility() == View.GONE) {
                    setWidgetVisible(false);
                }
                progressLayout.setVisibility(progressLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        ImageIconBean iconBean = new ImageIconBean();
        iconBean.setImageView(imageView);
        iconBean.setType(3);
        iconBean.setId(itemBean.getWidget_index());
        iconBean.setProgressLayout(progressLayout);

        if (itemBean.getIcon_file_set() != null) {
            iconBean.setSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_selected());
            iconBean.setUnSelectedIconName(itemBean.getIcon_file_set().getIcon_file_name_unselected());
        }
        iconBeansMap.put(itemBean.getWidget_index(), iconBean);

        String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
        if (!TextUtils.isEmpty(name)) {
            iconBean.getImageView().setImageBitmap(iconsMap.get(name));
        }
        imageView.setClickable(true);
        imageView.setEnabled(true);
//        MyLogUtils.d("addSwitch   " + iconBean.getSelectedIconName() + ", " + iconBean.getUnSelectedIconName());
        Bitmap selectedBitmap = iconsMap.get(iconBean.getSelectedIconName());
        Bitmap unSelectedBitmap = iconsMap.get(iconBean.getUnSelectedIconName());
        StateListDrawable selector = new StateListDrawable();
        Drawable selectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), selectedBitmap);
        Drawable unselectedDrawable = new BitmapDrawable(mContext.getApplicationContext().getResources(), unSelectedBitmap);
        selector.addState(new int[]{android.R.attr.state_pressed}, selectedDrawable);
        selector.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        // 添加一个默认状态, 默认状态必须写在其他状态的最后面, 否则其他状态失效
        selector.addState(new int[]{}, unselectedDrawable);
        imageView.setImageDrawable(selector);

        rootView.addView(imageView);
    }



    private void addFloatWindow(Context context, RelativeLayout viewGroup) {
//        MyLogUtils.i("addFloatWindow()");
        if (viewGroup != null && floatWindowView != null) {
            viewGroup.removeView(floatWindowView);
        }
        floatWindowView = new CustomFloatWindow(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) context.getResources().getDimension(R.dimen.dp_60);
        layoutParams.topMargin = (int) context.getResources().getDimension(R.dimen.dp_80);
        floatWindowView.setLayoutParams(layoutParams);
        floatWindowView.setVisibility(GlobalVariable.isShowCurrentData ? View.VISIBLE : View.GONE);
        viewGroup.addView(floatWindowView);

    }


    public void updateFloatWindow() {
        if (floatWindowView != null && floatWindowView.getVisibility() == View.VISIBLE) {
            floatWindowView.updateText(floatWindowStr);
        }

//        if (floatWindowView2 != null && floatWindowView2.getVisibility() == View.VISIBLE) {
//            floatWindowView2.updateText(floatWindowStr2);
//        }
    }




    public void changeFloatWindow(boolean show) {
        if (floatWindowView != null) {
            if(show){
                if(floatWindowView.getVisibility() != View.VISIBLE){
                    floatWindowView.setVisibility(View.VISIBLE);
                }
            }else{
                if(floatWindowView.getVisibility() != View.GONE){
                    floatWindowView.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setIconsMap(HashMap<String, Bitmap> hashMap) {
        this.iconsMap = hashMap;
    }


    public void updateIcons(HashMap<String, Bitmap> hashMap) {
        iconsMap = hashMap;
        for (Integer key : iconBeansMap.keySet()) {
            ImageIconBean iconBean = iconBeansMap.get(key);
            if (iconBean != null) {
                String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
                if (!TextUtils.isEmpty(name)) {
                    iconBean.getImageView().setImageBitmap(iconsMap.get(name));
                }
            }
        }
    }


    private void updateIconBean(int viewId, boolean isSelected) {
        if (iconBeansMap != null) {
            ImageIconBean iconBean = iconBeansMap.get(viewId);
            if (iconBean != null) {
                iconBean.setSelected(isSelected);
                String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
                if (!TextUtils.isEmpty(name) && iconsMap.get(name) != null) {
                    iconBean.getImageView().setImageBitmap(iconsMap.get(name));
                }
            }
        }
    }

    private void updateViews(int viewId, int viewType, int value) {
        if (iconBeansMap != null) {
            ImageIconBean iconBean = iconBeansMap.get(viewId);
            if (iconBean != null) {
                if (viewType == 1) {
                    iconBean.setSelected(value == 1);
                    String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
                    if (!TextUtils.isEmpty(name) && iconsMap.get(name) != null) {
                        iconBean.getImageView().setImageBitmap(iconsMap.get(name));
                    }
                } else if (viewId == 2) {
                    iconBean.setSelected(value == 1);
                    String name = iconBean.isSelected() ? iconBean.getSelectedIconName() : iconBean.getUnSelectedIconName();
                    if (!TextUtils.isEmpty(name) && iconsMap.get(name) != null) {
                        iconBean.getImageView().setImageBitmap(iconsMap.get(name));
                    }
                } else if (viewId == 3) {
                    if (iconBean.getProgressLayout() != null) {
                        iconBean.getProgressLayout().setProgress(value);
                    }
                } else if (viewId == 4) {
                    if (iconBean.getCustomRecyclerView() != null) {
                        iconBean.getCustomRecyclerView().setSelectedPosition(value);
                    }
                }
            }
        }
    }

    public void setWidgetVisible(boolean isShow) {
        if (showViewList != null && showViewList.size() > 0) {
            for (View view : showViewList) {
                view.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        }
    }


    private ImageIconBean getIconBean(int viewId) {
        if (iconBeansMap != null) {
            return iconBeansMap.get(viewId);
        }
        return null;
    }

    private void disposable() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

    }

    private void disposableSwitch() {
        if (switchDisposable != null && !switchDisposable.isDisposed()) {
            switchDisposable.dispose();
        }
    }

    public void destroy() {
        floatWindowStr = "";
        floatWindowView = null;
        iconsMap.clear();
        disposable();
        disposableSwitch();
//        GduApplication.getSingleApp().gduCommunication.removeCycleACKCB(GduSocketConfig3.CYCLE_ACK_GET_FLOAT_WINDOW_STATE);
    }


}
