package com.gdu.demo.flight.setting.bean;

import java.util.List;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/20
 */
public class PSdkCustomViewBean {


    private VersionDTO version;
    private MainInterfaceDTO main_interface;
    private ConfigInterfaceDTO config_interface;

    public VersionDTO getVersion() {
        return version;
    }

    public void setVersion(VersionDTO version) {
        this.version = version;
    }

    public MainInterfaceDTO getMain_interface() {
        return main_interface;
    }

    public void setMain_interface(MainInterfaceDTO main_interface) {
        this.main_interface = main_interface;
    }

    public ConfigInterfaceDTO getConfig_interface() {
        return config_interface;
    }

    public void setConfig_interface(ConfigInterfaceDTO config_interface) {
        this.config_interface = config_interface;
    }

    public static class VersionDTO {
        private Integer major;
        private Integer minor;

        public Integer getMajor() {
            return major;
        }

        public void setMajor(Integer major) {
            this.major = major;
        }

        public Integer getMinor() {
            return minor;
        }

        public void setMinor(Integer minor) {
            this.minor = minor;
        }
    }

    public static class MainInterfaceDTO {
        private FloatingWindowDTO floating_window;
        private List<WidgetItemBean> widget_list;

        private Speaker speaker;

        public FloatingWindowDTO getFloating_window() {
            return floating_window;
        }

        public void setFloating_window(FloatingWindowDTO floating_window) {
            this.floating_window = floating_window;
        }

        public List<WidgetItemBean> getWidget_list() {
            return widget_list;
        }

        public void setWidget_list(List<WidgetItemBean> widget_list) {
            this.widget_list = widget_list;
        }

        public static class FloatingWindowDTO {
            private Boolean is_enable;

            public Boolean getIs_enable() {
                return is_enable;
            }

            public void setIs_enable(Boolean is_enable) {
                this.is_enable = is_enable;
            }
        }

        public Speaker getSpeaker() {
            return speaker;
        }

        public void setSpeaker(Speaker speaker) {
            this.speaker = speaker;
        }
    }

    public static class ConfigInterfaceDTO {
        private TextInputBoxDTO text_input_box;
        private List<WidgetItemBean> widget_list;

        public TextInputBoxDTO getText_input_box() {
            return text_input_box;
        }

        public void setText_input_box(TextInputBoxDTO text_input_box) {
            this.text_input_box = text_input_box;
        }

        public List<WidgetItemBean> getWidget_list() {
            return widget_list;
        }

        public void setWidget_list(List<WidgetItemBean> widget_list) {
            this.widget_list = widget_list;
        }

        public static class TextInputBoxDTO {
            private String widget_name;
            private String placeholder_text;
            private Boolean is_enable;

            public String getWidget_name() {
                return widget_name;
            }

            public void setWidget_name(String widget_name) {
                this.widget_name = widget_name;
            }

            public String getPlaceholder_text() {
                return placeholder_text;
            }

            public void setPlaceholder_text(String placeholder_text) {
                this.placeholder_text = placeholder_text;
            }

            public Boolean getIs_enable() {
                return is_enable;
            }

            public void setIs_enable(Boolean is_enable) {
                this.is_enable = is_enable;
            }
        }
    }

    public static class Speaker {

        private boolean is_enable_tts;
        private boolean is_enable_voice;

        public boolean isIs_enable_tts() {
            return is_enable_tts;
        }

        public void setIs_enable_tts(boolean is_enable_tts) {
            this.is_enable_tts = is_enable_tts;
        }

        public boolean isIs_enable_voice() {
            return is_enable_voice;
        }

        public void setIs_enable_voice(boolean is_enable_voice) {
            this.is_enable_voice = is_enable_voice;
        }
    }
}
