package com.gdu.demo.utils;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @author wuqb
 * @date 2025/1/24
 * @description 设备屏幕相关工具类
 */
public class ScreenUtils {

    /**
     * 获取屏幕宽度
     * @param context 上下文
     * @return 屏幕宽度
     * */
    public static int getScreenWidth(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(outSize);
        return outSize.x;
    }

    /**
     * 获取屏幕高度
     * @param context 上下文
     * @return 屏幕高度
     * */
    public static int getScreenHeight(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(outSize);
        return outSize.y;
    }
}
