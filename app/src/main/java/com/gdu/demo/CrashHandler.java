package com.gdu.demo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import com.gdu.config.GduConfig;
import com.gdu.util.logs.RonLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	
	public static final String TAG = CrashHandler.class.getSimpleName();
	
	//系统默认的UncaughtException处理类 
	private UncaughtExceptionHandler mDefaultHandler;
	//CrashHandler实例
	private static final CrashHandler INSTANCE = new CrashHandler();
	//程序的Context对象
	private Context mContext;
	//用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	//用于格式化日期,作为日志文件名的一部分
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	public static final String LOG_DIR = GduConfig.BaseDirectory +"/crash/";
	

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() { }

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		//获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		//设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null)
		{
			//如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			// 结束所有Activity
				// 退出程序
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(1);

		}
		
		//退出程序
//		android.os.Process.killProcess(android.os.Process.myPid());
//		System.exit(1);
	}
	
	/**
	 * 记录log功能是否可用，Nox一键配置扫描ip地址时，253个，扫描失败有异常，保存异常日志时，由于太多会出问题
	 */
	private boolean enable = true;

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		
		if(!enable) return true;
		
		if (ex == null) {
			return false;
		}
		//使用Toast来显示异常信息
		/*new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, R.string.toast_app_dead, Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();*/
		
		//收集设备参数信息
		collectDeviceInfo(mContext);
		//保存日志文件 
		saveCrashInfo2File(ex);
		return true;
	}
	
	/**
	 * 收集设备参数信息
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return	返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {
		
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		RonLog.LogE(getClass().getSimpleName(), sb.toString());
		try {
			String time = dateFormat.format(new Date());
			String fileName = "log-" + time + "err.log";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(LOG_DIR);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(LOG_DIR +"/"+ fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
		}
		return null;
	}
}

