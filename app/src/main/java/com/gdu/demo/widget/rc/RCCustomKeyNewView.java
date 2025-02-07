package com.gdu.demo.widget.rc;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.RcCustomKeyViewBinding;
import com.gdu.demo.databinding.RcCustomKeyViewMenuPopBinding;
import com.gdu.sdk.customkey.RcCustomKeyBean;
import com.gdu.sdk.customkey.RcCustomKeyDaoHelper;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: lixiqiang
 * @Date: 2022/8/4
 */
public class RCCustomKeyNewView extends RelativeLayout {

    private final Context context;
    private RcCustomKeyViewBinding viewBinding;

    private RcCustomKeyMenAdapter menAdapter;

    private TextView currentTextView;

    // 点击的view
    private int clickViewType = -1;

    RcCustomKeyBean comb1;
    RcCustomKeyBean comb2;
    RcCustomKeyBean comb3;

    private PopupWindow mMenuPop;

    public RCCustomKeyNewView(Context context) {
        this(context, null);
    }

    public RCCustomKeyNewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RCCustomKeyNewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        setListener();
        initData();
    }


    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_custom_key_view, this);
        viewBinding = RcCustomKeyViewBinding.bind(view);
        initMenuPop();
    }

    // 自定义按键popwin
    private void initMenuPop(){
        RcCustomKeyViewMenuPopBinding menuPopBinding = RcCustomKeyViewMenuPopBinding.inflate(LayoutInflater.from(getContext()));
        menAdapter = new RcCustomKeyMenAdapter();
        menuPopBinding.rvMenu.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        menuPopBinding.rvMenu.setAdapter(menAdapter);
        setMenu(0);
        menAdapter.setOnItemClickListener((adapter, view1, position) -> {
            RcCustomKeyMenu menu = (RcCustomKeyMenu) adapter.getItem(position);
            if (menu != null) {
                setSelectedText(menu);
                if (clickViewType > 100) {
                    if (clickViewType == 101) {
                        comb1.setMenuType(menu.getMenuType());
                        saveCombKey(101);
                    } else if (clickViewType == 102) {
                        comb2.setMenuType(menu.getMenuType());
                        saveCombKey(102);
                    } else if (clickViewType == 103) {
                        comb3.setMenuType(menu.getMenuType());
                        saveCombKey(103);
                    }

                } else {
                    saveData(menu);
                }
            }
        });

        List<SettingMenuItem> list = new ArrayList<>();
        list.add(new SettingMenuItem(1,R.drawable.rc_custom_key_camera, ""));
        list.add(new SettingMenuItem(2,R.drawable.rc_custom_key_gimbal, ""));
        list.add(new SettingMenuItem(3,R.drawable.rc_custom_key_app, ""));
        list.add(new SettingMenuItem(4,R.drawable.rc_custom_key_flight_control, ""));

        RcMenuTitleAdapter titleAdapter = new RcMenuTitleAdapter();
        menuPopBinding.rvTitle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        menuPopBinding.rvTitle.setAdapter(titleAdapter);
        titleAdapter.setNewInstance(list);
        titleAdapter.setOnItemClickListener((adapter, view12, position) -> {
            titleAdapter.setSelectPosition(position);
            setMenu(position);
        });
        menuPopBinding.ivClose.setOnClickListener(v -> {
            if(mMenuPop != null) {
                mMenuPop.dismiss();
            }
        });
        mMenuPop = new PopupWindow(menuPopBinding.getRoot());
        mMenuPop.setWidth(getResources().getDimensionPixelSize(R.dimen.dp_230));
        mMenuPop.setHeight(GlobalVariable.screenHeight - getResources().getDimensionPixelSize(R.dimen.dp_140));
        mMenuPop.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mMenuPop.setFocusable(false);
        mMenuPop.setOutsideTouchable(true);
    }

    /**
     * 记录设置的按键对应的功能
     * clickViewType 按钮类型
     * selectedMenuType 自定义功能类型
     */
    private void saveData(RcCustomKeyMenu menu) {
        Logger.e("RcCustomKeyDaoHelper :clickView = " + clickViewType + ",selectedMenu  = " + menu.getMenuType());

        if (clickViewType == -1) {
            return;
        }
        RcCustomKeyDaoHelper.insertData(new RcCustomKeyBean((long) clickViewType, menu.getMenuType(), 0, 0));

        // c1 c2 取消之前的自定义按钮
//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess && (clickViewType == 1 || clickViewType == 2)) {
//            GduApplication.getSingleApp().gduCommunication.setRCC1AndC2((byte) 0, (byte) 0, null);
//        }
    }

    private void saveData(int viewType, RcCustomKeyMenu menu) {
        Logger.e("RcCustomKeyDaoHelper insertData viewType = " + viewType + " menuType = " + menu.getMenuType());
        RcCustomKeyDaoHelper.insertData(new RcCustomKeyBean((long) viewType, menu.getMenuType(), 0, 0));
        // c1 c2 取消之前的自定义按钮
//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess && (viewType == 1 || viewType == 2)) {
//            GduApplication.getSingleApp().gduCommunication.setRCC1AndC2((byte) 0, (byte) 0, null);
//        }
    }


    private void setSelectedText(RcCustomKeyMenu menu) {
        mMenuPop.dismiss();
        if (currentTextView != null) {
            currentTextView.setText(menu.getMenuName());
        }
    }

    private void setMenu(int position) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList();
        if (position == 0) {
            list = RcCustomKeyMenu.getCameraMenu(context);
        } else if (position == 1) {
            list = RcCustomKeyMenu.getGimbalMenu(context);
        } else if (position == 2) {
            list = RcCustomKeyMenu.getAppMenu(context);
        } else if (position == 3) {
            list = RcCustomKeyMenu.getFlightControlMenu(context);
        }
        menAdapter.setNewInstance(list);
    }


    private void setListener() {
        viewBinding.tvC1.setOnClickListener(listener);
        viewBinding.tvC2.setOnClickListener(listener);
        viewBinding.tvL1.setOnClickListener(listener);
        viewBinding.tvL2.setOnClickListener(listener);
        viewBinding.tvR1.setOnClickListener(listener);
        viewBinding.tvR2.setOnClickListener(listener);
        viewBinding.tvTop.setOnClickListener(listener);
        viewBinding.tvBot.setOnClickListener(listener);
        viewBinding.tvLeft.setOnClickListener(listener);
        viewBinding.tvRight.setOnClickListener(listener);
        viewBinding.tvCenter.setOnClickListener(listener);
        viewBinding.ivDelOne.setOnClickListener(listener);
        viewBinding.ivDelTwo.setOnClickListener(listener);

        viewBinding.ovComb11.setOnOptionClickListener((parentId, view, position) -> {
            if (comb1 != null && isSameCombKey(101, getKey1ValByPos(position), true)) {
                viewBinding.ovComb11.setIndex(getPos1ByKey(comb1.getCombKey1()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }

            viewBinding.ovComb11.setIndex(position);
            comb1.setCombKey1(getKey1ValByPos(position));
            saveCombKey(101);
        });

        viewBinding.ovComb12.setOnOptionClickListener((parentId, view, position) -> {
            if (comb1 != null && isSameCombKey(101, getKey2ValByPos(position), false)) {
                viewBinding.ovComb12.setIndex(getPos2ByKey(comb1.getCombKey2()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }

            viewBinding.ovComb12.setIndex(position);
            comb1.setCombKey2(getKey2ValByPos(position));
            saveCombKey(101);
        });
        viewBinding.ovComb21.setOnOptionClickListener((parentId, view, position) -> {
            if (comb2 != null && isSameCombKey(102, getKey1ValByPos(position), true)) {
                viewBinding.ovComb21.setIndex(getPos1ByKey(comb2.getCombKey1()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }

            viewBinding.ovComb21.setIndex(position);
            comb2.setCombKey1(getKey1ValByPos(position));
            saveCombKey(102);
        });

        viewBinding.ovComb22.setOnOptionClickListener((parentId, view, position) -> {
            if (comb2 != null && isSameCombKey(102, getKey2ValByPos(position), false)) {
                viewBinding.ovComb22.setIndex(getPos2ByKey(comb2.getCombKey2()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }

            viewBinding.ovComb22.setIndex(position);
            comb2.setCombKey2(getKey2ValByPos(position));
            saveCombKey(102);
        });

        viewBinding.ovComb31.setOnOptionClickListener((parentId, view, position) -> {
            if (comb3 != null && isSameCombKey(103, getKey1ValByPos(position), true)) {
                viewBinding.ovComb31.setIndex(getPos1ByKey(comb3.getCombKey1()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }
            viewBinding.ovComb31.setIndex(position);
            comb3.setCombKey1(getKey1ValByPos(position));
            saveCombKey(103);
        });

        viewBinding.ovComb32.setOnOptionClickListener((parentId, view, position) -> {
            if (comb3 != null && isSameCombKey(103, getKey2ValByPos(position), false)) {
                viewBinding.ovComb32.setIndex(getPos2ByKey(comb3.getCombKey2()));
                Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                return;
            }
            viewBinding.ovComb32.setIndex(position);
            comb3.setCombKey2(getKey2ValByPos(position));
            saveCombKey(103);
        });
        viewBinding.tvComb1.setOnClickListener(listener);
        viewBinding.tvComb2.setOnClickListener(listener);
        viewBinding.tvComb3.setOnClickListener(listener);
    }

    private void saveCombKey(int keyType) {

        if (keyType == 101) {
            if (comb1 != null  && comb1.getCombKey1() > 0 && comb1.getCombKey2() > 0 ) {

                if (comb1.getCombKey1() == comb1.getCombKey2()) {
                    Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                } else {
                    RcCustomKeyDaoHelper.insertData(comb1);
                    Toast.makeText(context, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                }
            }

        } else if (keyType == 102) {
            if (comb2 != null && comb2.getCombKey1() > 0 && comb2.getCombKey2() > 0 ) {

                if (comb2.getCombKey1() == comb2.getCombKey2()) {
                    Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                } else {
                    RcCustomKeyDaoHelper.insertData(comb2);
                    Toast.makeText(context, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (keyType == 103) {
            if (comb3 != null  && comb3.getCombKey1() > 0 && comb3.getCombKey2() > 0 ) {
                if (comb3.getCombKey1() == comb3.getCombKey2()) {
                    Toast.makeText(context, R.string.string_key_can_not_same, Toast.LENGTH_SHORT).show();
                } else {
                    RcCustomKeyDaoHelper.insertData(comb3);
                    Toast.makeText(context, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private final OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tv_c1:
                    showMenu(1, viewBinding.tvC1);
                    break;
                case R.id.tv_c2:
                    showMenu(2, viewBinding.tvC2);
                    break;
                case R.id.tv_l1:
                    showMenu(3, viewBinding.tvL1);
                    break;
                case R.id.tv_l2:
                    showMenu(4, viewBinding.tvL2);
                    break;
                case R.id.tv_r1:
                    showMenu(5, viewBinding.tvR1);
                    break;
                case R.id.tv_r2:
                    showMenu(6, viewBinding.tvR2);
                    break;
                case R.id.tv_top:
                    showMenu(7, viewBinding.tvTop);
                    break;
                case R.id.tv_bot:
                    showMenu(8, viewBinding.tvBot);
                    break;
                case R.id.tv_left:
                    showMenu(9, viewBinding.tvLeft);
                    break;
                case R.id.tv_right:
                    showMenu(10, viewBinding.tvRight);
                    break;
                case R.id.tv_center:
                    showMenu(11, viewBinding.tvCenter);
                    break;
                case R.id.tv_comb_1:
                    showMenu(101, viewBinding.tvComb1);
                    break;
                case R.id.tv_comb_2:
                    showMenu(102, viewBinding.tvComb2);
                    break;
                case R.id.tv_comb_3:
                    showMenu(103, viewBinding.tvComb3);
                    break;
                case R.id.iv_del_one:
                    delCustomKey(101);
                    break;
                case R.id.iv_del_two:
                    delCustomKey(102);
                    break;
                default:
                    break;
            }

        }
    };

    private void delCustomKey(int keyType) {
        RcCustomKeyBean comb = RcCustomKeyDaoHelper.query(keyType);
        if (comb != null) {
            RcCustomKeyDaoHelper.delData(comb);
            Toast.makeText(context, R.string.string_del_success, Toast.LENGTH_SHORT).show();
        }
        setCombKey(keyType);
    }

    private void showMenu(int type, TextView textView) {
        if (mMenuPop == null) {
            initMenuPop();
        }
        //mMenuPop.showAsDropDown(textView,0,0);
        int[] loc = new int[2];
        View anchor = viewBinding.getRoot();
        anchor.getLocationInWindow(loc);
        mMenuPop.showAtLocation(anchor,Gravity.START | Gravity.TOP, loc[0], loc[1]);
        clickViewType = type;
        currentTextView = textView;
    }

    private void initData() {
        ArrayList<RcCustomKeyMenu> list = RcCustomKeyMenu.getAllMenu(context);
        for (int i = 1; i < 12; i++) {
            setMenuText(list, i);
        }
        setCombKey(101);
        setCombKey(102);
        setCombKey(103);
    }

    private void setCombKey(int type) {
        RcCustomKeyBean comb = RcCustomKeyDaoHelper.query(type);
        if (comb != null) {
            if (type == 101) {
                comb1 = comb;
                viewBinding.ovComb11.setIndex(getPos1ByKey(comb.getCombKey1()));
                viewBinding.ovComb12.setIndex(getPos2ByKey(comb.getCombKey2()));
                viewBinding.tvComb1.setText(getMenuName(RcCustomKeyMenu.getAllMenu(context), comb));
            } else if (type == 102) {
                comb2 = comb;
                viewBinding.ovComb21.setIndex(getPos1ByKey(comb.getCombKey1()));
                viewBinding.ovComb22.setIndex(getPos2ByKey(comb.getCombKey2()));
                viewBinding.tvComb2.setText(getMenuName(RcCustomKeyMenu.getAllMenu(context), comb));
            } else if (type == 103){
                comb3 = comb;
                viewBinding.ovComb31.setIndex(getPos1ByKey(comb.getCombKey1()));
                viewBinding.ovComb32.setIndex(getPos2ByKey(comb.getCombKey2()));
                viewBinding.tvComb3.setText(getMenuName(RcCustomKeyMenu.getAllMenu(context), comb));
            }
        } else {
            if (type == 101) { //默认C1， C2
                comb1 = new RcCustomKeyBean((long) 101, 0, 1, 2);
                viewBinding.ovComb11.setIndex(0);
                viewBinding.ovComb12.setIndex(0);
                viewBinding.tvComb1.setText(context.getString(R.string.string_rc_key_no));
            } else if (type == 102) { //默认L1， R1
                comb2 = new RcCustomKeyBean((long) 102, 0, 3, 5);
                viewBinding.ovComb21.setIndex(1);
                viewBinding.ovComb22.setIndex(1);
                viewBinding.tvComb2.setText(context.getString(R.string.string_rc_key_no));
            } else if(type == 103){ //默认L2， R2
                comb3 = new RcCustomKeyBean((long) 103, 0, 4, 6);
                viewBinding.ovComb31.setIndex(2);
                viewBinding.ovComb32.setIndex(2);
                viewBinding.tvComb3.setText(context.getString(R.string.string_rc_key_no));
            }
        }
    }

    private void setMenuText(ArrayList<RcCustomKeyMenu> list, int viewType) {
        RcCustomKeyBean bean = RcCustomKeyDaoHelper.query(viewType);
        switch (viewType) {
            case 1:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(101, context.getString(R.string.string_rc_key_gimbal_center));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvC1.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvC1.setText(getMenuName(list, bean));
                }
                break;
            case 2:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(102, context.getString(R.string.string_rc_key_gimbal_down));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvC2.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvC2.setText(getMenuName(list, bean));
                }
                break;
            case 3:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(202, context.getString(R.string.string_rc_key_app_change_map));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvL1.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvL1.setText(getMenuName(list, bean));
                }
                break;
            case 4:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(5, context.getString(R.string.string_rc_key_camera_change_mode));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvL2.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvL2.setText(getMenuName(list, bean));
                }
                break;
            case 5:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(1, context.getString(R.string.string_rc_key_camera_enlarge));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvR1.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvR1.setText(getMenuName(list, bean));
                }
                break;
            case 6:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(2, context.getString(R.string.string_rc_key_camera_narrow));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvR2.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvR2.setText(getMenuName(list, bean));
                }
                break;
            case 7:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(206, context.getString(R.string.string_selected_last_target));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvTop.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvTop.setText(getMenuName(list, bean));
                }
                break;
            case 8:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(205, context.getString(R.string.string_selected_next_point));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvBot.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvBot.setText(getMenuName(list, bean));
                }
                break;
            case 9:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(203, context.getString(R.string.string_add_target_point));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvLeft.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvLeft.setText(getMenuName(list, bean));
                }
                break;
            case 10:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(204, context.getString(R.string.string_delete_selected_point));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvRight.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvRight.setText(getMenuName(list, bean));
                }
                break;
            case 11:
                if (bean == null) {
                    RcCustomKeyMenu rcCustomKeyMenu = new RcCustomKeyMenu(207, context.getString(R.string.string_look_for_target));
                    saveData(viewType, rcCustomKeyMenu);
                    viewBinding.tvCenter.setText(rcCustomKeyMenu.menuName);
                } else {
                    viewBinding.tvCenter.setText(getMenuName(list, bean));
                }
                break;
            default:
                break;
        }
    }

    private String getMenuName(ArrayList<RcCustomKeyMenu> list, RcCustomKeyBean bean) {
        String menuString = context.getString(R.string.string_rc_key_no);
        if (bean != null && bean.getMenuType() != 0) {
            for (RcCustomKeyMenu menu : list) {
                if (menu.menuType == bean.getMenuType()) {
                    menuString = menu.getMenuName();
                    break;
                }
            }
        }
        return menuString;
    }

    private boolean isSameCombKey(int keyType, int comBKey, boolean isLeftKey) {
        if (comb1 == null || comb2 == null || comb3 == null) {
            return false;
        }
        Log.d("DIYKey",keyType + "-" + comBKey + "-" + isLeftKey);
        int[] ints1 = new int[2];
        int[] ints2 = new int[2];
        int[] ints3 = new int[2];
        if (keyType == 101) {
            ints1[0] = isLeftKey ? comBKey : comb1.getCombKey1();
            ints1[1] = isLeftKey ? comb1.getCombKey2() : comBKey;
            ints2[0] = comb2.getCombKey1();
            ints2[1] = comb2.getCombKey2();
            ints3[0] = comb3.getCombKey1();
            ints3[1] = comb3.getCombKey2();
        } else if (keyType == 102) {
            ints1[0] = comb1.getCombKey1();
            ints1[1] = comb1.getCombKey2();
            ints2[0] = isLeftKey ? comBKey : comb2.getCombKey1();
            ints2[1] = isLeftKey ? comb2.getCombKey2() : comBKey;
            ints3[0] = comb3.getCombKey1();
            ints3[1] = comb3.getCombKey2();
        } else if (keyType == 103) {
            ints1[0] = comb1.getCombKey1();
            ints1[1] = comb1.getCombKey2();
            ints2[0] = comb2.getCombKey1();
            ints2[1] = comb2.getCombKey2();
            ints3[0] = isLeftKey ? comBKey : comb3.getCombKey1();
            ints3[1] = isLeftKey ? comb3.getCombKey2() : comBKey;
        }
        return Arrays.equals(ints1, ints2) || Arrays.equals(ints1, ints3) || Arrays.equals(ints2, ints3);
    }

    /**
     * C1:1, L1:3, L2:4
     */
    private int getKey1ValByPos(int pos){
        switch (pos){
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 4;
            default:
                return 0;
        }
    }

    /**
     * C1:0, L1:1, L2:2
     */
    private int getPos1ByKey(int key){
        switch (key){
            case 1:
                return 0;
            case 3:
                return 1;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * C2:2, R1:5, R2:6
     */
    private int getKey2ValByPos(int pos){
        switch (pos){
            case 0:
                return 2;
            case 1:
                return 5;
            case 2:
                return 6;
            default:
                return 0;
        }
    }

    /**
     * C2:0, R1:1, R2:2
     */
    private int getPos2ByKey(int key){
        switch (key){
            case 2:
                return 0;
            case 5:
                return 1;
            case 6:
                return 2;
            default:
                return 0;
        }
    }

}
