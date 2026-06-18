package com.gdu.demo.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;

import com.gdu.lib.base.GduEnvConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by Administrator on 2017/8/5.
 */

public class SystemUtils {
    public static final String SYS_EMUI = "sys_emui";
    public static final String SYS_MIUI = "sys_miui";
    public static final String SYS_FLYME = "sys_flyme";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    public static String getSystem(){
        String SYS = "";
        try {
            Properties prop= new Properties();
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            if(prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null){
                SYS = SYS_MIUI;//小米
            }else if(prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                    ||prop.getProperty(KEY_EMUI_VERSION, null) != null
                    ||prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null){
                SYS = SYS_EMUI;//华为
            }else if(getMeizuFlymeOSFlag().toLowerCase().contains("flyme")){
                SYS = SYS_FLYME;//魅族
            }
        } catch (IOException e){
            e.printStackTrace();
            return SYS;
        }
        return SYS;
    }

    /**
     * <p>获取版本信息</p>
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

    /**
     * 判断手机是否装载sd卡n
     *
     * @return
     */
    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static boolean isMIUI(){
        return getSystem().equals(SYS_MIUI);
    }

    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String)get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    /**
     * 4.4 以上可以直接判断准确
     *
     * 4.4 以下非MIUI直接返回true
     *
     * 4.4 以下MIUI 可 判断 上一次打开app 时 是否开启了悬浮窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;

        if(!isMIUI()){
            return true;
        }

        if (version >= 19) {
            return checkOp(context, 24);  //自己写就是24 为什么是24?看AppOpsManager //AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if(isMIUI()){
                return (context.getApplicationInfo().flags & 1 << 27) == 1 << 27;
            }else{
                return true;
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class managerClass = manager.getClass();
                Method method = managerClass.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int isAllowNum = (Integer) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());

                return AppOpsManager.MODE_ALLOWED == isAllowNum;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 判断当前语言是否是中文
     * @return
     */
    public static boolean isZh(Context context){
        final Locale locale = context.getResources().getConfiguration().locale;
        final String language = locale.getLanguage();
        return language.endsWith("zh");
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }


    /**
     * 获取当前进程名称
     * @param context context
     * @return processNameStr
     */
    public static String getProcessName(Context context) {
        final ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String processNameStr = "";
        final List<ActivityManager.RunningAppProcessInfo> runningApps =
                manager.getRunningAppProcesses();
        if (runningApps == null) {
            return processNameStr;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    processNameStr = proInfo.processName;
                }
            }
        }
        return processNameStr;
    }


    /**
     * 是否是主进程
     * @return true
     */
    public static boolean isMainProcess() {
        return getProcessName(GduEnvConfig.application).equals(GduEnvConfig.application.getPackageName());
    }


    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *<p>此权限调用需谨慎，经常bug</p>
     * @return  手机IMEI
     */
//    public static String getIMEI(Context ctx) {
//        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
//        if (tm != null) {
//            return tm.getDeviceId();
//        }
//        return null;
//    }
}
