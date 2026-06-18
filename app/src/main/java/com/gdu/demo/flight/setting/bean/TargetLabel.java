package com.gdu.demo.flight.setting.bean;


import com.gdu.demo.R;

public enum TargetLabel {
    LABEL_0000(0, R.string.target_label_0000),
    LABEL_0037(37, R.string.target_label_0037),
    LABEL_0122(122, R.string.target_label_0122);

    final int key;
    final int value;

    TargetLabel(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    public static TargetLabel get(int key) {
        for (TargetLabel e : TargetLabel.values()) {
            if (e.key == key) {
                return e;
            }
        }
        return null;
    }
}
