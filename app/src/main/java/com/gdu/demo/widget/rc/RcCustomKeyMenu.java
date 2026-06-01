package com.gdu.demo.widget.rc;

import android.content.Context;

import com.gdu.demo.R;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;

import java.util.ArrayList;

/**
 * @Author: lixiqiang
 * @Date: 2022/8/10
 */
public class RcCustomKeyMenu {

    int menuType;
    String menuName;

    public RcCustomKeyMenu(int menuType, String menuName) {
        this.menuType = menuType;
        this.menuName = menuName;
    }

    public int getMenuType() {
        return menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public static ArrayList<RcCustomKeyMenu> getCameraMenu(Context context) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList<>();
        list.add(new RcCustomKeyMenu(1, context.getString(R.string.string_rc_key_camera_enlarge)));
        list.add(new RcCustomKeyMenu(2, context.getString(R.string.string_rc_key_camera_narrow)));
        list.add(new RcCustomKeyMenu(3, context.getString(R.string.string_rc_key_camera_add_ev)));
        list.add(new RcCustomKeyMenu(4, context.getString(R.string.string_rc_key_camera_sub_ev)));
        list.add(new RcCustomKeyMenu(5, context.getString(R.string.string_rc_key_camera_change_mode)));
        if (!IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            list.add(new RcCustomKeyMenu(7, context.getString(R.string.string_ffc)));
        }
        list.add(new RcCustomKeyMenu(8, context.getString(R.string.string_high_temp_warn)));
        list.add(new RcCustomKeyMenu(0, context.getString(R.string.string_rc_key_no)));
        return list;
    }

    public static ArrayList<RcCustomKeyMenu> getGimbalMenu(Context context) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList<>();
        list.add(new RcCustomKeyMenu(101, context.getString(R.string.string_rc_key_gimbal_center)));
        list.add(new RcCustomKeyMenu(102, context.getString(R.string.string_rc_key_gimbal_down)));
        if (!IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            list.add(new RcCustomKeyMenu(103, context.getString(R.string.string_rc_key_gimbal_center_down)));
            list.add(new RcCustomKeyMenu(104, context.getString(R.string.string_rc_key_gimbal_pitch_center)));
            list.add(new RcCustomKeyMenu(105, context.getString(R.string.string_rc_key_gimbal_couser_center)));
        }
        list.add(new RcCustomKeyMenu(0, context.getString(R.string.string_rc_key_no)));
        return list;
    }

    public static ArrayList<RcCustomKeyMenu> getAppMenu(Context context) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList<>();
        list.add(new RcCustomKeyMenu(202, context.getString(R.string.string_rc_key_app_change_map)));
        list.add(new RcCustomKeyMenu(203, context.getString(R.string.string_add_target_point)));
        list.add(new RcCustomKeyMenu(204, context.getString(R.string.string_delete_selected_point)));
        list.add(new RcCustomKeyMenu(205, context.getString(R.string.string_selected_next_point)));
        list.add(new RcCustomKeyMenu(206, context.getString(R.string.string_selected_last_target)));
        list.add(new RcCustomKeyMenu(207, context.getString(R.string.string_look_for_target)));
        list.add(new RcCustomKeyMenu(0, context.getString(R.string.string_rc_key_no)));
        return list;
    }

    public static ArrayList<RcCustomKeyMenu> getFlightControlMenu(Context context) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList<>();
        list.add(new RcCustomKeyMenu(301, context.getString(R.string.string_rc_key_flight_control_night_light)));
        list.add(new RcCustomKeyMenu(302, context.getString(R.string.string_rc_key_flight_control_update_home_air)));
        list.add(new RcCustomKeyMenu(303, context.getString(R.string.string_rc_key_flight_control_update_home_rc)));
        list.add(new RcCustomKeyMenu(0, context.getString(R.string.string_rc_key_no)));
        return list;
    }

    public static ArrayList<RcCustomKeyMenu> getAllMenu(Context context) {
        ArrayList<RcCustomKeyMenu> list = new ArrayList<>();
        list.addAll(getCameraMenu(context));
        list.addAll(getGimbalMenu(context));
        list.addAll(getAppMenu(context));
        list.addAll(getFlightControlMenu(context));
        return list;
    }
}
