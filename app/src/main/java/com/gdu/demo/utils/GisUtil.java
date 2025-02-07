package com.gdu.demo.utils;

public class GisUtil {
    public static double calculateDistance(double p1Lon,double p1Lat,double p2Lon,double p2Lat){
        double lat1 = Math.PI / 180 * p1Lat;
        double lon1 = Math.PI / 180 * p1Lon;
        double lat2 = Math.PI / 180 * p2Lat;
        double lon2 = Math.PI / 180 * p2Lon;
        float R = 6371F;
        return Math.acos(Math.sin(lat1) * Math.sin(lat2) + (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))) * R * 1000;
    }
}
