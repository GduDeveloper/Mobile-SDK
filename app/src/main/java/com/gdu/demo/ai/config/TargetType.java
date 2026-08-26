package com.gdu.demo.ai.config;

/**
 *
 * @author zhangzhilai
 * @date 2018/2/5
 * 目标定位类型  0x01循环检  0x02单点定位
 */

public enum TargetType {
    TARGET_DETECT((byte) 0x01, "target_detect"),
    TARGET_LOCATE((byte)0x02, "target_locate");

    byte key;
    String value;
    TargetType(byte key, String value) {
        this.key = key;
        this.value = value;
    }

    public byte getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static TargetType get(int key){
        for(TargetType e : TargetType.values()){
            if (e.getKey() == key) {
                return e;
            }
        }
        return null;
    }
}
