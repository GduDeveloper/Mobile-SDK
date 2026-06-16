package com.gdu.demo.flight.msgbox

import androidx.fragment.app.FragmentActivity
import com.gdu.demo.utils.CommonDialog
import com.gdu.lib.util.core.XLogger
import com.gdu.msdk.device.component.hms.bean.ErrorCodeDialogBean
import com.gdu.sdk.base.Diagnostics
import kotlin.math.abs


/**
 * @author wuqb
 * @date 2025/1/7
 * @description 异常码弹窗提示
 */
class MsgBoxDialog(private val activity: FragmentActivity) {

    private var mErrorDialog: CommonDialog? = null
    private var mCurrentIndex = 0
    private var mPreWarnDialog:ArrayList<Diagnostics> = ArrayList()
    //两次展示时间在10s内不再显示弹窗,由于每次3秒判断一次，故此处用9s
    private val showDialogInterval = 8*1000
    private var mShowDialogTime: HashMap<Long, Long> = HashMap()

    fun showErrCodeDialog(mWarnDialog: ArrayList<Diagnostics>) {
        if (mWarnDialog.isEmpty() && mErrorDialog?.dialog?.isShowing == true)
            mErrorDialog?.dismissAllowingStateLoss()

        if (!equals(mWarnDialog, mPreWarnDialog)){
            mCurrentIndex = 0
            mPreWarnDialog.clear()
            mPreWarnDialog = mWarnDialog
        }else{
            if (mCurrentIndex >= mWarnDialog.size){
                mCurrentIndex = 0
            }
        }
        if (mErrorDialog == null || mErrorDialog?.dialog?.isShowing == false)
            if (mWarnDialog.size>mCurrentIndex)
                showDialog(mWarnDialog[mCurrentIndex])
    }

    private fun showDialog(bean: Diagnostics){
        if (mShowDialogTime.isNotEmpty() &&
            (abs(System.currentTimeMillis()-(mShowDialogTime[bean.code]?:0))<showDialogInterval)){
            showNextDialog()
            return
        }
//        if (mErrorDialog == null) {
//            var contentStr = activity.getString(bean.warnResId)
//            if (null!=bean.dialogBean) {
//                if (!TextUtils.isEmpty(bean.dialogBean?.contentStr)) {
//                    contentStr = bean.dialogBean?.contentStr ?: ""
//                } else if (bean.dialogBean?.contentStrId != 0) {
//                    contentStr = bean.dialogBean?.contentStrId?.let { ResourceUtils.getString(it) } ?: ""
//                }
//            }
//            if (TextUtils.isEmpty(contentStr)){
//                showNextDialog()
//                return
//            }
//            var positiveStr = activity.getString(R.string.i_know)
//            if (!TextUtils.isEmpty(bean.dialogBean?.positiveStr)){
//                positiveStr = bean.dialogBean?.positiveStr?:positiveStr
//            }else if (bean.dialogBean?.positiveStrId != 0){
//                positiveStr = bean.dialogBean?.positiveStrId?.let { ResourceUtils.getString(it) } ?:positiveStr
//            }
//            var negativeStr = ""
//            if (!TextUtils.isEmpty(bean.dialogBean?.negativeStr)){
//                negativeStr = bean.dialogBean?.negativeStr?:""
//            }else if (bean.dialogBean?.negativeStrId != 0){
//                negativeStr = bean.dialogBean?.negativeStrId?.let { ResourceUtils.getString(it) } ?:""
//            }
//
//            mErrorDialog = GduCommonDialog.Builder(activity)
//                .setPositiveListener { _, _ ->
//                    mShowDialogTime[bean.warnId] = System.currentTimeMillis()
//                    mErrorDialog = null
//                    if (null==bean.dialogBean?.functionId){
//                        showNextDialog()
//                        return@setPositiveListener
//                    }
//                    executeInstruction(bean.dialogBean?.functionId)
//                }
//                .setNegativeListener { dialog, which ->
//                    mShowDialogTime[bean.warnId] = System.currentTimeMillis()
//                    mErrorDialog = null
//                    showNextDialog()
//                }
//                .setContent(contentStr)
//                .setSure(positiveStr)
//                .setCancelVisible(!TextUtils.isEmpty(negativeStr))
//                .setCancel(negativeStr)
//                .build()
//        }
        if (mErrorDialog?.isAdded == false && (mErrorDialog?.dialog == null || mErrorDialog?.dialog?.isShowing == false)) {
            XLogger.APP.i("wuqb", "显示了异常弹窗："+bean.code)
            mErrorDialog?.show()
        }
    }

    /**
     * 展示下一个弹窗
     * */
    private fun showNextDialog(){
        mCurrentIndex += 1
        if (mPreWarnDialog.size>mCurrentIndex)
            showDialog(mPreWarnDialog[mCurrentIndex])
    }

    /**
     * 判断两个内容是否相同
     * */
    private fun equals(list1: ArrayList<Diagnostics>, list2: ArrayList<Diagnostics>):Boolean{
        if (list1.size != list2.size) { // 若两个Map的大小不同
            return false // 设置比较结果为false
        } else {
            for (i in 0 until list1.size) {
                if (list1[i].code != list2[i].code) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * 执行相应的操作指令
     * */
    private fun executeInstruction(functionId: Int?) {
        when(functionId){
            ErrorCodeDialogBean.FUNCTION_RETURN_IMMEDIATELY->{
//                GduApplication.getSingleApp().gduCommunication.lowBatteryBack(
//                    true
//                ) { code: Int, bean: GduFrame3? ->
//                    showNextDialog()
//                    LogUtils.i("showErrCode10104008Dialog lowBatteryBack() code = $code")
//                }
            }
        }
    }
    
}