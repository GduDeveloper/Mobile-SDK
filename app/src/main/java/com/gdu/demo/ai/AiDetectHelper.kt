package com.gdu.demo.ai

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.gdu.demo.SdkDemoApplication
import com.gdu.demo.ai.view.AIDetectView
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.key.value.AIModelState
import com.gdu.msdk.key.value.ai.TargetMode
import com.gdu.sdk.vision.listener.OnTargetDetectListener
import com.gdu.sdk.vision.listener.OnTargetDetectModelListener

/**
 * @Author: fuchi
 * @Date : 2026/8/27 - 13:19
 * @Desc : AI 识别帮助类
 */
class AiDetectHelper(private val context: Context, private val aiDetectView: AIDetectView) {
    private val tag = this.javaClass.simpleName

    init {
        SdkDemoApplication.getAircraftInstance().vision.setOnTargetDetectListener(object : OnTargetDetectListener{
            override fun onTargetDetecting(targetList: MutableList<TargetMode>?) {
                //算法已开启，才接收并刷新sei数据
                if (SdkDemoApplication.getAircraftInstance().vision.isAiDetectOpen()) {
                    setAiDetectData(targetList)
                }
            }

            override fun onTargetDetectFailed(errorCode: Int) {
            }

            override fun onTargetDetectStart() {
            }

            override fun onTargetDetectFinished() {
            }
        })
        //监听当前上报的算法列表，手动选择开启算法时，需在此列表中选择开启（或调用getTargetDetectModels()方法获取）
        SdkDemoApplication.getAircraftInstance().vision.setOnTargetDetectModelsListener(object : OnTargetDetectModelListener{
            override fun onTargetDetectModel(models: MutableList<AIModelState>) {

            }
        })
    }

    /**
     * 设置Ai识别数据
     */
    private fun setAiDetectData(targetList: MutableList<TargetMode>?){
        aiDetectView?.let {
            it.post {
                if (targetList.isNullOrEmpty()) {
                    it.clearView()
                } else {
                    it.setTargetList(targetList)
                }
            }
        }
    }

    /**
     * 开启Ai识别
     */
    fun startTargetDetect(){
        SdkDemoApplication.getAircraftInstance().vision.startTargetDetect { err ->
            log("AI识别是否开启成功：" + (err == null))
            showToast(if(err == null) "开启成功" else "开启失败")
        }
    }

    /**
     * 关闭Ai识别
     */
    fun stopTargetDetect(){
        SdkDemoApplication.getAircraftInstance().vision.stopTargetDetect { err ->
            setAiDetectData(mutableListOf())
            log("AI识别是否关闭：" + (err == null))
            showToast(if(err == null) "关闭成功" else "关闭失败")
        }
    }

    /**
     * 设置开启指定算法
     * 注意:
     *      1，此处传的算法列表，需要为飞机上报的原列表
     *      2，仅修改里列表中的是否开启字段即可，其他原样传入
     *      3，修改字段：state   开：0x01， 关：0x00
     */
    fun setTargetType(models: MutableList<AIModelState>){
        SdkDemoApplication.getAircraftInstance().vision.setTargetType(models) { var1 ->
            log("AI算法设置是否成功：" + (var1 == null))
        }
    }

    fun showToast(str: String){
        aiDetectView?.let {
            it.post{
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun log(logStr: String){
        XLogger.APP.d(tag, logStr)
        Log.d(tag, logStr)
    }
}