package com.gdu.demo.map


import com.gdu.demo.map.geometry.Point
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 坐标系引用抽象类
 * @Author StormChen
 * @Date 2022/3/14-20:14
 * @Package com.stormchen.map
 * @Description
 */
object SpatialReference {

    /**
     * 高德坐标系（也是GCJ_02）
     */
    const val GAODE = 0

    /**
     * 火星坐标系
     */
    const val GCJ_02 = 0

    /**
     * wgs84坐标
     */
    const val WGS84 = 4326

    /**
     * 克拉索夫斯基椭球参数长半轴a
     */
    private const val a = 6378245.0

    /**
     * 克拉索夫斯基椭球参数第一偏心率平方
     */
    private const val ee = 0.00669342162296594323


    /**
     * 高德坐标转GPS坐标误差较小的方式
     *
     * @param latitude
     * @param longitude
     * @return
     */
    private fun toGPSPoint(point: Point): Point {
        val latitude = point.latitude
        val longitude = point.longitude
        var dev = calDev(latitude, longitude)
        var retLat = latitude - dev.latitude
        var retLon = longitude - dev.longitude
        for (i in 0..0) {
            dev = calDev(retLat, retLon)
            retLat = latitude - dev.latitude
            retLon = longitude - dev.longitude
        }
        // 2021-11-20 手动修正火星转大地坐标的误差(误差值时实际测试中得出的)
        Point(retLat - 0.000007359409029, retLon + 0.00000185845225, WGS84)
        point.latitude = retLat - 0.000007359409029
        point.longitude = retLon + 0.00000185845225
        point.spatialReference = WGS84
        return point
        // 修改电力认证分支版本时计算出的新的误差值先存放此处备用
//        return new LatLonPoint(retLat - 0.000006882934, retLon + 0.000001503741);
    }

    /**
     * 计算偏差
     *
     * @param wgLat
     * @param wgLon
     * @return
     */
    private fun calDev(wgLat: Double, wgLon: Double): Point {
        if (isOutOfChina(wgLat, wgLon)) {
            return Point(0.0, 0.0, WGS84)
        }
        var dLat: Double =
            calLat(wgLon - 105.0, wgLat - 35.0)
        var dLon: Double =
            calLon(wgLon - 105.0, wgLat - 35.0)
        val radLat: Double = wgLat / 180.0 * Math.PI
        var magic = sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat =
            dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * Math.PI)
        dLon =
            dLon * 180.0 / (a / sqrtMagic * cos(radLat) * Math.PI)
        return Point(dLat, dLon, WGS84)
    }

    /**
     * 判断坐标是否在国外
     */
    private fun isOutOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347) {
            return true
        }
        return lat < 0.8293 || lat > 55.8271
    }

    /**
     * 计算纬度
     */
    private fun calLat(x: Double, y: Double): Double {
        var resultLat = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
        +0.2 * sqrt(abs(x))
        resultLat += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        resultLat += (20.0 * sin(y * Math.PI) + 40.0 * sin(y / 3.0 * Math.PI)) * 2.0 / 3.0
        resultLat += (160.0 * sin(y / 12.0 * Math.PI) + 320 * sin(y * Math.PI / 30.0)) * 2.0 / 3.0
        return resultLat
    }

    /**
     * 计算经度
     */
    private fun calLon(x: Double, y: Double): Double {
        var resultLon =
            300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        resultLon += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(2.0 * x * Math.PI)) * 2.0 / 3.0
        resultLon += (20.0 * sin(x * Math.PI) + 40.0 * sin(x / 3.0 * Math.PI)) * 2.0 / 3.0
        resultLon += (150.0 * sin(x / 12.0 * Math.PI) + 300.0 * sin(x / 30.0 * Math.PI)) * 2.0 / 3.0
        return resultLon
    }

}