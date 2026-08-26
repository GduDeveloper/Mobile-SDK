package com.gdu.demo.ai.util

import android.graphics.PointF
import android.util.Log
import com.gdu.lib.util.GsonUtils
import com.gdu.lib.util.core.XLogger
import com.gdu.lib.util.log.LogHz
import com.gdu.videoprocess.QOSInfoCollector
import com.gdu.videoprocess.videoprocessInterface
import kotlin.math.abs

internal object EglAiTransferUtil {
    private const val ASPECT_RATIO_TOLERANCE = 0.01f

    /**
     * 反向映射
     */
    fun convertAIVideoPointToScreenPointNormalization(
        videoPointX: Int, videoPointY: Int, videoWidth: Int, videoHeight: Int, streamId: String?
    ): PointF? {
        if (streamId.isNullOrEmpty()) {
            log("streamId == null")
            return null
        }
        val config: QOSInfoCollector.VideoConfig =
            QOSInfoCollector.getInstance().getVideoParams(streamId)
        if (config.width <= 0 || config.height <= 0 || config.surwidth <= 0 || config.sufheight <= 0
            || videoPointX <= 0 || videoPointY <= 0 || videoWidth <= 0 || videoHeight <= 0
        ) {
            log("config : " + GsonUtils.toJson(config))
            return null
        }
        try {
            var previewVideoX: Float = 0f
            var previewVideoY: Float = 0f
            previewVideoX = videoPointX.toFloat() * config.width / videoWidth
            previewVideoY = videoPointY.toFloat() * config.height / videoHeight
            log("0----previewVideoX = $previewVideoX, previewVideoY = $previewVideoY, config: " + GsonUtils.toJson(config))
            if (config.scaleMode == videoprocessInterface.Mode.FIT_CENTER) {
                val videoAspect = config.width / config.height.toFloat()
                val viewAspect = config.surwidth / config.sufheight.toFloat()
                val sameAspect = abs(viewAspect - viewAspect) <= ASPECT_RATIO_TOLERANCE
                if (sameAspect) {//宽高比相同
                    val scaleH: Float = config.sufheight * 1f / config.height
                    val scaleW: Float = config.surwidth * 1f / config.width
                    val screenX = (previewVideoX * scaleW).coerceIn(0f, config.surwidth.toFloat())
                    val screenY = (previewVideoY * scaleH).coerceIn(0f, config.sufheight.toFloat())
                    log("1----videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + screenX + ", screenY = " + screenY + ", config.sufheight = " + config.sufheight + ", config.surwidth = " + config.surwidth)
                    return PointF(screenX, screenY)
                } else if (videoAspect > viewAspect) {//宽铺满，上下缩放
                    val newVideoHeight: Float =
                        config.height * config.surwidth / config.width.toFloat()
                    val deltaX = 0f
                    val deltaY: Float = (config.sufheight - newVideoHeight) / 2
                    val scaleH: Float = newVideoHeight * 1f / config.height
                    val scaleW: Float = config.surwidth * 1f / config.width
                    val screenX =
                        (deltaX + previewVideoX * scaleW).coerceIn(0f, config.surwidth.toFloat())
                    val screenY =
                        (deltaY + previewVideoY * scaleH).coerceIn(0f, config.sufheight.toFloat())
                    log("2----videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + screenX + ", screenY = " + screenY + ", config.sufheight = " + config.sufheight + ", config.surwidth = " + config.surwidth)
                    return PointF(screenX, screenY)
                } else {//高铺满，宽缩放
                    val newVideoWidth: Float =
                        config.width * config.sufheight / config.height.toFloat()
                    val deltaX: Float = (config.surwidth - newVideoWidth) / 2
                    val deltaY = 0f
                    val scaleH: Float = config.sufheight * 1f / config.height
                    val scaleW: Float = newVideoWidth * 1f / config.width
                    val screenX =
                        (deltaX + previewVideoX * scaleW).coerceIn(0f, config.surwidth.toFloat())
                    val screenY =
                        (deltaY + previewVideoY * scaleH).coerceIn(0f, config.sufheight.toFloat())
                    log("3----videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + screenX + ", screenY = " + screenY + ", config.sufheight = " + config.sufheight + ", config.surwidth = " + config.surwidth)
                    return PointF(screenX, screenY)
                }
            } else if (config.scaleMode == videoprocessInterface.Mode.CENTER_CROP) {
                val videoAspect = config.width / config.height.toFloat()
                val viewAspect = config.surwidth / config.sufheight.toFloat()
                val sameAspect = abs(videoAspect - viewAspect) <= ASPECT_RATIO_TOLERANCE
                if (sameAspect) {//宽高比相同
                    val scaleH: Float = config.sufheight * 1f / config.height
                    val scaleW: Float = config.surwidth * 1f / config.width
                    val screenX = (previewVideoX * scaleW).coerceIn(0f, config.surwidth.toFloat())
                    val screenY = (previewVideoY * scaleH).coerceIn(0f, config.sufheight.toFloat())
                    log("4----videoSizeMode = " + config.sizeMode + ", videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + screenX + ", screenY = " + screenY + ", config.sufheight = " + config.sufheight + ", config.surwidth = " + config.surwidth + ", config.width = " + config.width + ", config.height = " + config.height)
                    return PointF(screenX, screenY)
                } else if (videoAspect > viewAspect) {//高铺满，宽裁剪
                    val newVideoWidth: Float =
                        config.width * config.sufheight / config.height.toFloat()
                    val newVideoX: Float = previewVideoX * newVideoWidth / config.width
                    val newVideoY: Float =
                        previewVideoY.toFloat() * config.sufheight / config.height
                    val deltaX: Float = (newVideoWidth - config.surwidth) / 2
                    val deltaY = 0f
                    val newVideoXRel: Float = newVideoX - deltaX
                    if (newVideoXRel < 0 || newVideoXRel > config.surwidth) {
                        log("null-1--newVideoXRel = $newVideoXRel , config.surwidth = ${config.surwidth} ")
                        return null
                    }
                    val realVideoX = newVideoXRel.coerceIn(0f, config.surwidth.toFloat())
                    val realVideoY = (newVideoY - deltaY).coerceIn(0f, config.sufheight.toFloat())
                    log("5----videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + realVideoX + ", screenY = " + realVideoY + ", config.sufheight = " + config.sufheight + ", config.surwidth = " + config.surwidth)
                    return PointF(realVideoX, realVideoY)
                } else {//宽铺满，高裁剪
                    val newVideoHeight: Float =
                        config.height * config.surwidth / config.width.toFloat()
                    val newVideoX: Float = previewVideoX.toFloat() * config.surwidth / config.width
                    val newVideoY: Float = previewVideoY * newVideoHeight / config.height
                    val deltaX = 0f
                    val deltaY: Float = (newVideoHeight - config.sufheight) / 2
                    val newVideoYRel: Float = newVideoY - deltaY
                    if (newVideoYRel < 0 || newVideoYRel > config.sufheight) {
                        log("null-2--newVideoYRel = $newVideoYRel , config.sufheight = ${config.sufheight} ")
                        return null
                    }
                    val realVideoX = newVideoX.coerceIn(0f, config.surwidth.toFloat())
                    val realVideoY =
                        newVideoYRel //(newVideoY - newVideoYRel).coerceIn(0f,config.sufheight.toFloat())
                    log("6----videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + realVideoX + ", screenY = " + realVideoY + ", config: " + GsonUtils.toJson(config))
                    return PointF(realVideoX, realVideoY)
                }
            } else {//默认全部按照fitxy处理，视频处暂时只有fitxy和centerInside两种
                val scaleH: Float = config.sufheight * 1f / config.height
                val scaleW: Float = config.surwidth * 1f / config.width
                val screenX = (previewVideoX * scaleW).coerceIn(0f, config.surwidth.toFloat())
                val screenY = (previewVideoY * scaleH).coerceIn(0f, config.sufheight.toFloat())
                log("7---- videoPointX = " + videoPointX + ", videoPointY = " + videoPointY + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight + ", screenX = " + screenX + ", screenY = " + screenY + ", config: " + GsonUtils.toJson(config))
                return PointF(screenX, screenY)
            }
//            log("8----config = " + (if (config != null) com.gdu.map.mapkit.utils.GsonUtils.toJson(config) else "null"))
        } catch (e: Exception) {
            log("9----Exception = " + (if (e != null) GsonUtils.toJson(e) else "null"))
            return null
        }
    }

    /**
     * 高性能均匀抽稀（高频实时数据复用容器）
     * @param source 原始数据
     * @param output 复用的输出列表，内部会先clear，减少GC
     * @param maxCount 最大点数
     */
    fun <T> thinDataReuse(source: List<T>, output: MutableList<T>, maxCount: Int = 50) {
        output.clear()
        val total = source.size
        // 数据不足直接全部加入
        if (total <= maxCount) {
            output.addAll(source)
            return
        }

        val step = total.toFloat() / maxCount
        repeat(maxCount) { i ->
            val index = (i * step).toInt()
            output.add(source[index])
        }
    }

    private fun log(str: String) {
        LogHz.hz("convertVideo", 2000) {
            XLogger.APP.d("convertVideo", str)
            Log.d("convertVideo", str)
        }
    }
}