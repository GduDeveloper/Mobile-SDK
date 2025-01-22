package com.gdu.demo.utils

import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager

object DeviceUtils {



    fun getScreenWidth(ctx: Context):Int{
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outSize = Point()
        wm.defaultDisplay.getRealSize(outSize)
        return outSize.x //获得宽
    }

    fun getScreenHeight(ctx: Context):Int{
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outSize = Point()
        wm.defaultDisplay.getRealSize(outSize)
        return outSize.y //获得宽
    }
}