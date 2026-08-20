package com.gdu.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.gdu.demo.databinding.ActivityDroneLogBinding
import com.gdu.lib.util.core.GsonUtils
import com.gdu.lib.util.core.ToastUtils
import com.gdu.sdk.dronelog.LogManager
import com.gdu.sdk.dronelog.bean.BaseLogBean
import com.gdu.sdk.dronelog.listener.OnDownloadLogFileListener
import com.gdu.sdk.dronelog.listener.OnGetAircraftLogListener
import com.gdu.ux.core.extension.textColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author: fuchi
 * @Date : 2026/8/20 - 9:09
 * @Desc : 获取飞行器日志
 */
class DroneLogActivity  : FragmentActivity(), View.OnClickListener {
    private val tag = this.javaClass.simpleName
    private var binding : ActivityDroneLogBinding ?= null
    private var logManager: LogManager ?= null
    private var selectLogData: MutableList<BaseLogBean> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDroneLogBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.getDroneLogData?.setOnClickListener(this)
        binding?.downloadDroneLogFile?.setOnClickListener(this)
        binding?.ivBack?.setOnClickListener(this)
        logManager = LogManager(this.lifecycleScope)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.get_drone_log_data ->{ //获取飞机日志在线数据
                logManager?.getAircraftLogList(object : OnGetAircraftLogListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDroneOnLineLogData(data: MutableList<BaseLogBean>, failReason: String) {
                        selectLogData.clear()
                        if (data.isNotEmpty()) {
                            selectLogData.add(data[0])
                        }
                        lifecycleScope.launch(Dispatchers.Main){
                            binding?.showDroneLogData?.text = GsonUtils.toJson(data[0]) + "\n" +(if (failReason.isEmpty()) "" else ("飞机日志获取失败：$failReason"))
                        }
                    }
                })
            }
            R.id.download_drone_log_file ->{ //下载飞行器日志
                if (selectLogData.isEmpty()) {
                    ToastUtils.showShort("当前无飞机日志可下载")
                    return
                }
                logManager?.downloadAircraftLog("", selectLogData, object : OnDownloadLogFileListener{
                    override fun onStart() {
                        Log.d(tag, "downloadAircraftLog - onStart()" )
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDroneLogDownloadProgress(desc: String, currentIndex: Int, totalIndex: Int, progress: Int, currentBytes: Long, totalBytes: Long) {
                        Log.d(tag, "onDroneLogDownloadProgress-- desc: $desc, currentIndex: $currentIndex, totalIndex : $totalIndex, progress : $progress, currentBytes : $currentBytes, totalBytes : $totalBytes")
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding?.downloadProgressTv?.text = "下载进度：$progress %"
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onDroneLogPkgProgress(desc: String, progress: Int, currentBytes: Long, totalBytes: Long) {
                        Log.d(tag, "onDroneLogPkgProgress-- desc: $desc, progress : $progress, currentBytes : $currentBytes, totalBytes : $totalBytes")
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding?.pkgProgressTv?.text = "打包进度：$progress %"
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFailure(error: Int, errMsg: String) {
                        Log.d(tag, "onFailure - error: $error, errMsg: $errMsg")
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding?.downloadResultTv?.text = "下载失败：$errMsg"
                            binding?.downloadResultTv?.textColor = resources.getColor(R.color.color_EB4242)
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(downloadResult: MutableList<BaseLogBean>) {
                        Log.d(tag, "onSuccess - downloadResult: " + GsonUtils.toJson(downloadResult))
                        lifecycleScope.launch(Dispatchers.Main) {
                            val downFilePath = if (downloadResult.isNotEmpty()) downloadResult[0].fileSavePath else ""
                            binding?.downloadResultTv?.text = "下载成功：$downFilePath"
                            binding?.downloadResultTv?.textColor = resources.getColor(R.color.color_00C586)
                        }
                    }
                })
            }
            R.id.iv_back ->{
                this.finish()
            }
        }
    }
}