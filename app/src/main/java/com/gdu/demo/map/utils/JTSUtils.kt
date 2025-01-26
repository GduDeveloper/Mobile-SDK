package com.gdu.demo.map.utils

import com.gdu.demo.map.SpatialReference
import com.gdu.demo.map.geometry.Point
import com.gdu.util.logs.AppLog
import org.locationtech.jts.algorithm.distance.DistanceToPoint
import org.locationtech.jts.algorithm.distance.PointPairDistance
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.distance.DistanceOp
import org.locationtech.jts.operation.polygonize.Polygonizer
import org.locationtech.jts.operation.valid.IsValidOp

object JTSUtils {

    /**
     * 获取线的中点
     */
    fun polylineCenterPoint(polylinePoints: MutableList<Point>): Point? {
        val geometryFactory = GeometryFactory()
        val polyline = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints))
        val centerPoint = polyline.centroid
        val array = CoordinateUtils.transMercator2LatLng(centerPoint.x, centerPoint.y)
        if (array.size != 2) return null
        return Point(array[0], array[1], SpatialReference.WGS84)
    }

    fun polygonCenterPoint(polygonPoints: MutableList<Point>): Point? {
        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return null
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        val centerPoint = polygon.centroid
        val array = CoordinateUtils.transMercator2LatLng(centerPoint.x, centerPoint.y)
        if (array.size != 2) return null
        return Point(array[0], array[1], SpatialReference.WGS84)
    }

    fun polygonInteriorPoint(points: MutableList<Point>): Point? {
        val polygonPoints = mutableListOf<Point>()
        polygonPoints.addAll(points)

        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return null
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        val centerPoint = polygon.interiorPoint
        val array = CoordinateUtils.transMercator2LatLng(centerPoint.x, centerPoint.y)
        if (array.size != 2) return null
        return Point(array[0], array[1], SpatialReference.WGS84)
    }

    fun getPointLeft(point: Point): Point {
        val pointCoordinate = CoordinateUtils.transLatLng2MercatorCoordinate(point.latitude, point.longitude)
        val latLng = CoordinateUtils.transMercator2LatLng(pointCoordinate.x - 1000, pointCoordinate.y)
        return Point(latLng[0], latLng[1], SpatialReference.WGS84)
    }

    fun getPointRight(point: Point): Point {
        val pointCoordinate = CoordinateUtils.transLatLng2MercatorCoordinate(point.latitude, point.longitude)
        val latLng = CoordinateUtils.transMercator2LatLng(pointCoordinate.x + 1000, pointCoordinate.y)
        return Point(latLng[0], latLng[1], SpatialReference.WGS84)
    }

    fun calPolygonArea(polygonPoints: MutableList<Point>): Double {
        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return 0.0
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        polygonPoints.removeLast()
        return polygon.area
    }

    /**
     * 判断线是否在多边形内
     */
    fun isPolygonContainPolyline(polylinePoints: MutableList<Point>, points: MutableList<Point>): Boolean {
        val polygonPoints = mutableListOf<Point>()
        polygonPoints.addAll(points)
        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return false
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        val polyline = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints))
        return polygon.contains(polyline)
    }

    /**
     * 判断点是否在多边形内
     */
    fun isPolygonContainPoints(points: MutableList<Point>, polygonPoints: MutableList<Point>): Boolean {
        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return false
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        points.forEach {
            val point = geometryFactory.createPoint(CoordinateUtils.transLatLng2MercatorCoordinate(it.latitude, it.longitude))
            // 点在多边形内
            if (polygon.contains(point)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断线和线是否相交
     */
    fun isLineIntersect(polylinePoints1: MutableList<Point>, polylinePoints2: MutableList<Point>): Boolean {
        val geometryFactory = GeometryFactory()
        val polyline1 = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints1))
        val polyline2 = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints2))
        return polyline1.intersects(polyline2)
    }

    /**
     * 判断线和多边形是否相交
     */
    fun isLinePolygonIntersect(polylinePoints: MutableList<Point>, points: MutableList<Point>): Boolean {
        val polygonPoints = mutableListOf<Point>()
        polygonPoints.addAll(points)
        val geometryFactory = GeometryFactory()
        val polyline = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints))
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return false
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        return polyline.intersects(polygon)
    }

    /**
     * 计算点到线的距离
     */
    fun calDistancePoint2Polyline(point: Point, polylinePoints: MutableList<Point>): Double {
        val geometryFactory = GeometryFactory()
        val coordinate = CoordinateUtils.transLatLng2MercatorCoordinate(point.latitude, point.longitude)
        val polyline = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints))
        val pointPairDistance = PointPairDistance()
        DistanceToPoint.computeDistance(polyline, coordinate, pointPairDistance)
        // Glog.e("JTSUtils", "calDistancePoint2Polyline ${pointPairDistance.distance}")
        return pointPairDistance.distance
    }

    /**
     * 分割多边形
     * 一条线分割多个多边形
     */
    fun splitPolygons(polylinePoints: MutableList<Point>, polygonsPoints: MutableList<MutableList<Point>>, closing: Boolean): MutableList<MutableList<Point>>? {
        AppLog.e("JTSUtils", "splitPolygons input polygon size = ${polygonsPoints.size}")
        if (polylinePoints.isEmpty()) {
            return null
        }
        if (polygonsPoints.isEmpty()) {
            return null
        }
        polygonsPoints.forEach {
            if (it.isEmpty() || it.size < 3) {
                // 多边形不合法
                return null
            }
            if (it[0].latitude != it.last().latitude || it[0].longitude != it.last().longitude) {
                // 多边形闭合
                it.add(it[0])
            }
        }
        val geometryFactory = GeometryFactory()
        val polyline = geometryFactory.createLineString(CoordinateUtils.latLng2Coordinate(polylinePoints))
        var geometry = polyline as Geometry
        polygonsPoints.forEach {
            val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(it))
            geometry = geometry.union(polygon.boundary)
        }
        val lineList = arrayListOf<Geometry>()
        for (n in 0 until geometry.numGeometries) {
            lineList.add(geometry.getGeometryN(n))
        }
        // 多边形化
        val polygonizer = Polygonizer()
        polygonizer.add(lineList)
        val listOfPolygon = arrayListOf<MutableList<Point>>()
        polygonizer.polygons.forEach {
            val listOfPoints = CoordinateUtils.coordinate2latLng((it as Polygon).coordinates)
            if (!closing) { // 不要求多边形闭合，去掉最后一点
                listOfPoints.removeLast()
            }
            listOfPolygon.add(listOfPoints)
        }
        return listOfPolygon
    }

    /**
     * 判断多边形是否合法
     */
    fun isPolygonValid(points: MutableList<Point>): Boolean {
        val polygonPoints = mutableListOf<Point>()
        points.forEach { polygonPoints.add(it) }

        val geometryFactory = GeometryFactory()
        if (polygonPoints.isEmpty() || polygonPoints.size < 3) {
            // 多边形不合法
            return false
        }
        if (polygonPoints[0].latitude != polygonPoints.last().latitude || polygonPoints[0].longitude != polygonPoints.last().longitude) {
            // 多边形闭合
            polygonPoints.add(polygonPoints[0])
        }
        val polygon = geometryFactory.createPolygon(CoordinateUtils.latLng2Coordinate(polygonPoints))
        return IsValidOp.isValid(polygon)
    }

    /**
     * 计算点到点的距离
     */
    fun calPointsDistance(point1: Point, point2: Point): Double {
        val geometryFactory = GeometryFactory()
        val coordinate1 = CoordinateUtils.transLatLng2MercatorCoordinate(point1.latitude, point1.longitude)
        val coordinate2 = CoordinateUtils.transLatLng2MercatorCoordinate(point2.latitude, point2.longitude)
        return DistanceOp.distance(geometryFactory.createPoint(coordinate1), geometryFactory.createPoint(coordinate2))
    }
}