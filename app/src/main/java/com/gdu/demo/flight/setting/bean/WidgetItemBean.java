package com.gdu.demo.flight.setting.bean;

import java.util.List;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/22
 */
public class WidgetItemBean {


    private Integer widget_index;
    private String widget_type;
    private String widget_name;
    private List<ListItemBean> list_item;
    private IconFileSet icon_file_set;
    private CustomizeRcButtonsConfigDTO customize_rc_buttons_config;

    public int getWidget_index() {
        return widget_index;
    }

    public void setWidget_index(Integer widget_index) {
        this.widget_index = widget_index;
    }

    public String getWidget_type() {
        return widget_type;
    }

    public void setWidget_type(String widget_type) {
        this.widget_type = widget_type;
    }

    public String getWidget_name() {
        return widget_name;
    }

    public void setWidget_name(String widget_name) {
        this.widget_name = widget_name;
    }

    public List<ListItemBean> getList_item() {
        return list_item;
    }

    public void setList_item(List<ListItemBean> list_item) {
        this.list_item = list_item;
    }

    public IconFileSet getIcon_file_set() {
        return icon_file_set;
    }

    public void setIcon_file_set(IconFileSet icon_file_set) {
        this.icon_file_set = icon_file_set;
    }

    public CustomizeRcButtonsConfigDTO getCustomize_rc_buttons_config() {
        return customize_rc_buttons_config;
    }

    public void setCustomize_rc_buttons_config(CustomizeRcButtonsConfigDTO customize_rc_buttons_config) {
        this.customize_rc_buttons_config = customize_rc_buttons_config;
    }

    public static class CustomizeRcButtonsConfigDTO {
        private Boolean is_enable;
        private Integer mapping_config_display_order;

        public Boolean getIs_enable() {
            return is_enable;
        }

        public void setIs_enable(Boolean is_enable) {
            this.is_enable = is_enable;
        }

        public Integer getMapping_config_display_order() {
            return mapping_config_display_order;
        }

        public void setMapping_config_display_order(Integer mapping_config_display_order) {
            this.mapping_config_display_order = mapping_config_display_order;
        }
    }

    public static class ListItemBean {
        private String item_name;
        private IconFileSet icon_file_set;

        public String getItem_name() {
            return item_name;
        }

        public void setItem_name(String item_name) {
            this.item_name = item_name;
        }

        public IconFileSet getIcon_file_set() {
            return icon_file_set;
        }

        public void setIcon_file_set(IconFileSet icon_file_set) {
            this.icon_file_set = icon_file_set;
        }
    }
}
