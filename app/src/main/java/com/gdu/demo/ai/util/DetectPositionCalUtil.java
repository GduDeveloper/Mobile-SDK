package com.gdu.demo.ai.util;

import android.graphics.PointF;
import com.gdu.lib.util.core.BarUtils;
import com.gdu.lib.util.core.ScreenUtils;
import com.gdu.videoprocess.QOSInfoCollector;
import java.util.ArrayList;
import java.util.List;

public class DetectPositionCalUtil {

    private DetectPositionCalUtil() {
    }

    /**
     * 将算法给的 1920 * 1080 的坐标转换到遥控器 1920 * 1200的屏幕上，
     * 不同视频渲染模式下，展示的效果不同
     * @param mode 视频原始分辨率  16:9（1920 * 1080）
     *                     4:3(220Pro拍照模式下可见光分辨率为 1440*1080)
     *                     5:4(1k红外 1280:1024)
     * @param x 算法返回的x坐标值（1920*1080分辨率下）
     * @return 转换后的X坐标
     */
    public static short transX(QOSInfoCollector.VideoSizeMode mode, float x) {
//        if (!ChannelManager.isCarNest()) {
//            if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
//                //需要区分P300和S200系列
//                boolean isP300 = IGduDroneDevice.get().getPlanType().getValue().isP300();
//                if (isP300) {
//                    //20251216根据产品最新定义，4:3比例下，不显示黑边，以宽为基准做等比拉升，故，需要按拉升计算坐标
//                    return (short) x;
//                } else {
//                    return (short)(0.75f * x + 240);
//                }
//                //在1440x1080区域里的坐标为（x,y） 映射到拉升后的1920x1080的坐标为,需区分中心点（960,540）左还是右
////                if (x < 960) {
////                    return (short) (4 * (x - 240) / 3);
////                } else if (x > 960) {
////                    return (short) (4 * (x - 1440) / 3 + 1920);
////                } else {
//
////                }
////                return (short)(4* (x - 240)/3);
//            } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
//                //视觉算法是等比加黑边后的值 视觉将图像等比缩放到 1350 * 1080 然后左右加 （1920 -1350）/2 的黑边后
//                // 进行算法识别再返回 1920 * 1080的坐标值
//                return (short) (1.111f * x - 107);
//            } else {
//                return (short) x;
//            }
//        } else {
            //车机渠道，均为16:9进行拉伸,x方向为按1920比例进行拉伸
            return (short) (((float) ScreenUtils.getScreenWidth() / 1920) * x);
//        }
    }

    /**
     * 反向转换，将手动画框转换成视觉需要的坐标
     *
     * @param mode 视频原始分辨率模式 同上
     * @param x
     * @return
     */
    public static short dTransX(QOSInfoCollector.VideoSizeMode mode, float x) {
        float result = x;
        if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
            result = (x - 240) * 1.25f;
        } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
            result = (x + 107) * 0.9f;
        }
        return (short) result;
    }

    public static short transY(QOSInfoCollector.VideoSizeMode mode, float y) {
        return (short) (y * 1.111f);
    }

    public static short transYNew(QOSInfoCollector.VideoSizeMode mode, float y) {
        //20251216根据产品最新定义，4:3比例下，不显示黑边，以宽为基准做等比拉升，故，需要按拉升计算坐标
        //在1440x1080区域里的坐标为（x,y） 映射到拉升后的1920x1080的坐标为（4/3(x - 240), 4y/3 - 180）
//        if (!ChannelManager.isCarNest()) {
//            if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
//                return (short) ((4 * y) / 3 - 180);
//            } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_16_9) {
//                //20260118 最新视图更换为1920 x 1200
////                return (short) (y);
//                return (short) (y * 1.111f);
//            } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
//                return (short) (y);
//            } else {
//                return (short) (y);
//            }
//        } else {
            //车机渠道Y方向按1080铺满，若DF屏，还需要减去底部状态栏
            int barHeight = BarUtils.getNavigationBarHeight();
            int screenHeight = ScreenUtils.getScreenHeight();
            return (short) (((float) (screenHeight) / 1080) * y);
//        }
    }

    public static short dTransY(QOSInfoCollector.VideoSizeMode mode, float y) {
//        return (short) (y * 0.9f);
//        if (!ChannelManager.isCarNest()) {
//            return (short) (y);
//        } else {
            //车机渠道Y方向按1080铺满，若DF屏，还需要减去底部状态栏
            int barHeight = BarUtils.getNavigationBarHeight();
            int screenHeight = ScreenUtils.getScreenHeight();
            return (short) ( (1080 * y) / (screenHeight - barHeight));
//        }
    }

    public static short transWidth(QOSInfoCollector.VideoSizeMode mode, float w) {
//        if (!ChannelManager.isCarNest()) {
//            //20251216根据产品最新定义，4:3比例下，不显示黑边，以宽为基准做等比拉升，故，需要按拉升计算坐标
//            if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
////                return (short)( w * 0.75f);
//                return (short)( w );
//            } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
//                return (short)( w * 1.111f);
//            } else {
//                return (short) w;
//            }
//        } else {
            //车机屏横向按1920铺满
            return (short) (((float) ScreenUtils.getScreenWidth() / 1920) * w);
//        }
    }

    public static short dTransWidth(QOSInfoCollector.VideoSizeMode mode, float w) {
        float result = w;
        if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
            result = w * 1.25f;
        } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
            result = w * 0.9f;
        }
        return (short) result;
    }

    public static short transHeight(QOSInfoCollector.VideoSizeMode mode, float h) {
//        return (short) (h * 1.111f);
//        if (!ChannelManager.isCarNest()) {
//            //20251216根据产品最新定义，4:3比例下，不显示黑边，以宽为基准做等比拉升
////            return (short) h;
//            if (mode == QOSInfoCollector.VideoSizeMode.MODE_4_3) {
//                return (short) (h * 1.25);
//            } else if (mode == QOSInfoCollector.VideoSizeMode.MODE_5_4) {
//                return (short) h;
//            } else {//MODE_16_9
//                return (short) h;
//            }
//        } else {
            //车机渠道Y方向按1080铺满，若DF屏，还需要减去底部状态栏
            int barHeight = BarUtils.getNavigationBarHeight();
            int screenHeight = ScreenUtils.getScreenHeight();
            return (short) (((float) (screenHeight - barHeight) / 1080) * h);
//        }
    }

    public static short dTransHeight(QOSInfoCollector.VideoSizeMode mode, float h) {
        return (short) ( h * 0.9f);
    }

    /**
     * 将左上角坐标和宽高转换为 左上角和右下角
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static List<Short> transXYWH2XYXY(String streamId, short x, short y, short w, short h) {
//        short transX = transX(mode, x);
////        short transY = transY(mode, y);
//        short transY = transYNew(mode, y);
//        short transWidth = transWidth(mode, w);
//        short transHeight = transHeight(mode, h);
//        List<Short> result = new ArrayList<>();
//        result.add(transX);
//        result.add(transY);
//        result.add((short) (transX + transWidth));
//        result.add((short) (transY + transHeight));
        PointF startPoint = EglAiTransferUtil.INSTANCE.convertAIVideoPointToScreenPointNormalization(x, y, 1920, 1080,
                streamId);
        PointF endPoint = EglAiTransferUtil.INSTANCE.convertAIVideoPointToScreenPointNormalization(x + w, y + h, 1920, 1080,
                streamId);
        if (startPoint == null || endPoint == null) {
            return null;
        }
        List<Short> result = new ArrayList<>();
        result.add((short) startPoint.x);
        result.add((short) startPoint.y);
        result.add((short) (endPoint.x));
        result.add((short) (endPoint.y));
        return result;
    }

}
