package com.gdu.demo.map.geometry

import com.gdu.demo.map.SpatialReference
import org.locationtech.jts.geom.Coordinate


/**
 * 地图绘制元素类型抽象类
 */
abstract class Geometry {


    abstract fun clear()

    /**
     * 元素类型
     */
    abstract fun getGeometryType(): String

    abstract fun contains(geometry: Geometry): Boolean

    abstract fun intersects(geometry: Geometry): Boolean

    /**
     * 计算与目标点的距离是否在限定范围内
     * @param geometry 目标点
     * @param distance 限定距离(米)
     */
    abstract fun withInDistance(geometry: Geometry, distance: Double): Boolean

    companion object {
        const val TYPENAME_POINT = "Point"
        const val TYPENAME_POLYLINE = "Polyline"
        const val TYPENAME_POLYGON = "Polygon"
        const val TYPENAME_CIRCLE = "Circle"
    }


    protected fun points2Coordinates(points: MutableList<Point>): Array<Coordinate> {
        val coordinates: Array<Coordinate> = Array(points.size) { Coordinate() }
        for ((i, point) in points.withIndex()) {
            val coordinate = coordinates[i]
            coordinate.x = point.longitude
            coordinate.y = point.latitude
        }
        return coordinates
    }


    protected fun coordinates2Points(coordinates: Array<Coordinate>): MutableList<Point> {
        val points: MutableList<Point> = arrayListOf()
        for (coordinate in coordinates) {
            points.add(Point(coordinate.y, coordinate.x, SpatialReference.WGS84))
        }
        return points
    }
}