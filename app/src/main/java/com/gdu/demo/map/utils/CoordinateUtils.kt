package com.gdu.demo.map.utils

import com.gdu.demo.map.SpatialReference
import com.gdu.demo.map.geometry.Point
import org.locationtech.jts.geom.Coordinate
import java.lang.Math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/03/21
 * desc  : 坐标相关工具类
</pre> *
 */
object CoordinateUtils {
    private const val X_PI = 3.14159265358979324 * 3000.0 / 180.0
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    /**
     * BD09 坐标转 GCJ02 坐标
     *
     * @param lng BD09 坐标纬度
     * @param lat BD09 坐标经度
     * @return GCJ02 坐标：[经度，纬度]
     */
    fun bd09ToGcj02(lng: Double, lat: Double): DoubleArray {
        val x = lng - 0.0065
        val y = lat - 0.006
        val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI)
        val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI)
        val gg_lng = z * Math.cos(theta)
        val gg_lat = z * Math.sin(theta)
        return doubleArrayOf(gg_lng, gg_lat)
    }

    /**
     * GCJ02 坐标转 BD09 坐标
     *
     * @param lng GCJ02 坐标经度
     * @param lat GCJ02 坐标纬度
     * @return BD09 坐标：[经度，纬度]
     */
    fun gcj02ToBd09(lng: Double, lat: Double): DoubleArray {
        val z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI)
        val theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI)
        val bd_lng = z * Math.cos(theta) + 0.0065
        val bd_lat = z * Math.sin(theta) + 0.006
        return doubleArrayOf(bd_lng, bd_lat)
    }

    /**
     * GCJ02 坐标转 WGS84 坐标
     *
     * @param lng GCJ02 坐标经度
     * @param lat GCJ02 坐标纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    fun gcj02ToWGS84(lng: Double, lat: Double): DoubleArray {
        if (outOfChina(lng, lat)) {
            return doubleArrayOf(lng, lat)
        }
        var dlat = transformLat(lng - 105.0, lat - 35.0)
        var dlng = transformLng(lng - 105.0, lat - 35.0)
        val radlat: Double = lat / 180.0 * PI
        var magic = Math.sin(radlat)
        magic = 1 - EE * magic * magic
        val sqrtmagic = Math.sqrt(magic)
        dlat = dlat * 180.0 / (A * (1 - EE) / (magic * sqrtmagic) * PI)
        dlng = dlng * 180.0 / (A / sqrtmagic * Math.cos(radlat) * PI)
        val mglat = lat + dlat
        val mglng = lng + dlng
        return doubleArrayOf(lng * 2 - mglng, lat * 2 - mglat)
    }

    /**
     * WGS84 坐标转 GCJ02 坐标
     *
     * @param lng WGS84 坐标经度
     * @param lat WGS84 坐标纬度
     * @return GCJ02 坐标：[经度，纬度]
     */
    fun wgs84ToGcj02(lng: Double, lat: Double): DoubleArray {
        if (outOfChina(lng, lat)) {
            return doubleArrayOf(lng, lat)
        }
        var dlat = transformLat(lng - 105.0, lat - 35.0)
        var dlng = transformLng(lng - 105.0, lat - 35.0)
        val radlat: Double = lat / 180.0 * PI
        var magic = Math.sin(radlat)
        magic = 1 - EE * magic * magic
        val sqrtmagic = Math.sqrt(magic)
        dlat = dlat * 180.0 / (A * (1 - EE) / (magic * sqrtmagic) * PI)
        dlng = dlng * 180.0 / (A / sqrtmagic * Math.cos(radlat) * PI)
        val mglat = lat + dlat
        val mglng = lng + dlng
        return doubleArrayOf(mglng, mglat)
    }

    /**
     * BD09 坐标转 WGS84 坐标
     *
     * @param lng BD09 坐标经度
     * @param lat BD09 坐标纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    fun bd09ToWGS84(lng: Double, lat: Double): DoubleArray {
        val gcj = bd09ToGcj02(lng, lat)
        return gcj02ToWGS84(gcj[0], gcj[1])
    }

    /**
     * WGS84 坐标转 BD09 坐标
     *
     * @param lng WGS84 坐标经度
     * @param lat WGS84 坐标纬度
     * @return BD09 坐标：[经度，纬度]
     */
    fun wgs84ToBd09(lng: Double, lat: Double): DoubleArray {
        val gcj = wgs84ToGcj02(lng, lat)
        return gcj02ToBd09(gcj[0], gcj[1])
    }

    /**
     * Mercator 坐标转 WGS84 坐标
     *
     * @param lng Mercator 坐标经度
     * @param lat Mercator 坐标纬度
     * @return WGS84 坐标：[经度，纬度]
     */
    fun mercatorToWGS84(lng: Double, lat: Double): DoubleArray {
        val x = lng / 20037508.34 * 180.0
        var y = lat / 20037508.34 * 180.0
        y = 180 / PI * (2 * Math.atan(Math.exp(y * PI / 180.0)) - PI / 2)
        return doubleArrayOf(x, y)
    }

    /**
     * WGS84 坐标转 Mercator 坐标
     *
     * @param lng WGS84 坐标经度
     * @param lat WGS84 坐标纬度
     * @return Mercator 坐标：[经度，纬度]
     */
    fun wgs84ToMercator(lng: Double, lat: Double): DoubleArray {
        val x = lng * 20037508.34 / 180.0
        var y: Double = Math.log(Math.tan((90.0 + lat) * PI / 360.0)) / (PI / 180.0)
        y = y * 20037508.34 / 180.0
        return doubleArrayOf(x, y)
    }

    private fun transformLat(lng: Double, lat: Double): Double {
        var ret =
            -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(
                Math.abs(lng)
            )
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(lng: Double, lat: Double): Double {
        var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(
            Math.abs(lng)
        )
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    private fun outOfChina(lng: Double, lat: Double): Boolean {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
    }

    /**
     * 经纬度列表转平面坐标数组
     */
    fun latLng2Coordinate(points: MutableList<Point>): Array<Coordinate> {
        val coordinates: Array<Coordinate> = Array(points.size) { Coordinate() }
        for ((index, point) in points.withIndex()) {
            val mercator = transLatLng2Mercator(point.latitude, point.longitude)
            val coordinate = coordinates[index]
            coordinate.x = mercator[0]
            coordinate.y = mercator[1]
        }
        return coordinates
    }

    /**
     * 平面坐标数组转经纬度列表
     */
    fun coordinate2latLng(coordinateArrays: Array<Coordinate>): MutableList<Point> {
        val listOfPoints = arrayListOf<Point>()
        coordinateArrays.forEach {
            val latLngArray = transMercator2LatLng(it.x, it.y)
            listOfPoints.add(Point(latLngArray[0], latLngArray[1], SpatialReference.WGS84))
        }
        return listOfPoints
    }

    /**
     * 经纬度转墨卡托
     */
    private fun transLatLng2Mercator(lat: Double, lon: Double): DoubleArray {
        val x = lon * 20037508.342789 / 180
        var y = ln(tan((90 + lat) * kotlin.math.PI / 360)) / (kotlin.math.PI / 180)
        y = y * 20037508.342789 / 180
        return doubleArrayOf(x, y)
    }

    /**
     * 经纬度转墨卡托Coordinate
     */
    fun transLatLng2MercatorCoordinate(lat: Double, lon: Double): Coordinate {
        val x = lon * 20037508.342789 / 180
        var y = ln(tan((90 + lat) * kotlin.math.PI / 360)) / (kotlin.math.PI / 180)
        y = y * 20037508.342789 / 180
        return Coordinate(x, y)
    }

    /**
     * 墨卡托转经纬度
     */
    fun transMercator2LatLng(x: Double, y: Double): DoubleArray {
        val lon: Double = x / 20037508.34 * 180
        var lat: Double = y / 20037508.34 * 180
        lat = 180 / kotlin.math.PI * (2 * atan(exp(lat * kotlin.math.PI / 180)) - kotlin.math.PI / 2);
        return doubleArrayOf(lat, lon)
    }
}