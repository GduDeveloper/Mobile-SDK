package com.gdu.demo.map.geometry

import com.gdu.demo.map.SpatialReference
import org.locationtech.jts.geom.Coordinate

open class Point @JvmOverloads constructor(var latitude: Double,
                                           var longitude: Double,
                                           var spatialReference: Int = SpatialReference.WGS84,
                                           val createTime: Long = System.currentTimeMillis()) : Geometry() {
    override fun clear() {
        //点不需要清除
    }

    override fun getGeometryType(): String {
        return TYPENAME_POINT
    }

    override fun contains(geometry: Geometry): Boolean {
        return false
    }

    override fun intersects(geometry: Geometry): Boolean {
        return false
    }

    override fun withInDistance(geometry: Geometry, dis: Double): Boolean {
        return when (geometry.getGeometryType()) {
            TYPENAME_POINT -> {
                val orgDesPoint = geometry as Point
                val srcPoint = Coordinate(this.longitude, this.latitude)
                val desPoint = Coordinate(orgDesPoint.longitude, orgDesPoint.latitude)
                val calculateDis = srcPoint.distance(desPoint) * 100000
                return calculateDis <= dis
            }
            else -> {
                return false
            }
        }
    }

    override fun toString(): String {
        return "lat/lng: ($latitude,$longitude)"
    }


    override fun equals(other: Any?): Boolean {
        if (other !is Point) {
            return super.equals(other)
        }
        val latIsEqu = this.latitude == other.latitude
        val lonIsEqu = this.longitude == other.longitude

        return latIsEqu && lonIsEqu
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + spatialReference
        return result
    }
}