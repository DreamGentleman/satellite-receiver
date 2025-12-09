package com.yxh.fangs.util;

import android.graphics.Point;

import com.bigemap.bmcore.entity.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheManager {

  public static final int RADIUS_EARTH_METERS = 6378137;
  public static final double MinLatitude = -85.05112877980659;
  public static final double MaxLatitude = 85.05112877980659;

  public static int mod(int number, final int modulus) {
    if (number > 0)
      return number % modulus;

    while (number < 0)
      number += modulus;

    return number;
  }

  public static Point getMapTileFromCoordinates(final boolean isMercator, final double aLat, final double aLon, final int zoom) {
    final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));
    int y;
    if (isMercator) {
      y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1 / Math.cos(aLat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoom));
    } else {
      y = (int) Math.floor((90 - aLat) / 180 * (1 << zoom) / 2);
    }
    return new Point(x, y);
  }

  public static double GroundResolution(final double latitude, final int levelOfDetail) {
    return GroundResolution(latitude, (double) levelOfDetail);
  }

  public static double GroundResolution(final double latitude, final double zoomLevel) {
    return GroundResolutionMapSize(wrap(latitude, -90, 90, 180), MapSize(zoomLevel));
  }

  public static double MapSize(final double pZoomLevel) {
    return getTileSize() * getFactor(pZoomLevel);
  }

  public static int getTileSize() {
    return 256;
  }

  public static double getFactor(final double pZoomLevel) {
    return Math.pow(2, pZoomLevel);
  }

  public static double GroundResolutionMapSize(double latitude, final double mapSize) {
    latitude = Clip(latitude, MinLatitude, MaxLatitude);
    return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * RADIUS_EARTH_METERS / mapSize;
  }

  public static double Clip(final double n, final double minValue, final double maxValue) {
    return Math.min(Math.max(n, minValue), maxValue);
  }

  private static double wrap(double n, final double minValue, final double maxValue, final double interval) {
    if (minValue > maxValue) {
      throw new IllegalArgumentException("minValue must be smaller than maxValue: "
            + minValue + ">" + maxValue);
    }
    if (interval > maxValue - minValue + 1) {
      throw new IllegalArgumentException(
            "interval must be equal or smaller than maxValue-minValue: " + "min: "
                  + minValue + " max:" + maxValue + " int:" + interval);
    }
    while (n < minValue) {
      n += interval;
    }
    while (n > maxValue) {
      n -= interval;
    }
    return n;
  }

  public static List<Long> getTilesCoverage(final ArrayList<GeoPoint> geoPoints, final boolean isMercator, final int zoomMin, final int zoomMax) {
    final List<Long> result = new ArrayList<>();
    for (int zoomLevel = zoomMin; zoomLevel <= zoomMax; zoomLevel++) {
      final Collection<Long> resultForZoom = getTilesCoverage(geoPoints, isMercator, zoomLevel);
      result.addAll(resultForZoom);
    }
    return result;
  }

  /**
   * 计算经纬度点集合覆盖的瓦片编号
   */
  public static Collection<Long> getTilesCoverage(final ArrayList<GeoPoint> geoPoints, final boolean isMercator, final int zoomLevel) {
    final Set<Long> result = new HashSet<>();
    GeoPoint prevPoint = null;
    Point tile, prevTile = null;
    final int mapTileUpperBound = 1 << zoomLevel;
    for (GeoPoint geoPoint : geoPoints) {

      final double d = GroundResolution(geoPoint.getLat(), zoomLevel);

      if (result.size() != 0) {
        if (prevPoint != null) {
          final double leadCoef = (geoPoint.getLat() - prevPoint.getLat()) / (geoPoint.getLon() - prevPoint.getLon());
          final double brng;
          if (geoPoint.getLon() > prevPoint.getLon()) {
            brng = Math.PI / 2 - Math.atan(leadCoef);
          } else {
            brng = 3 * Math.PI / 2 - Math.atan(leadCoef);
          }

          final GeoPoint wayPoint = new GeoPoint(prevPoint.getLat(), prevPoint.getLon());

          while ((((geoPoint.getLat() > prevPoint.getLat()) && (wayPoint.getLat() < geoPoint.getLat())) ||
                (geoPoint.getLat() < prevPoint.getLat()) && (wayPoint.getLat() > geoPoint.getLat())) &&
                (((geoPoint.getLon() > prevPoint.getLon()) && (wayPoint.getLon() < geoPoint.getLon())) ||
                      ((geoPoint.getLon() < prevPoint.getLon()) && (wayPoint.getLon() > geoPoint.getLon())))) {

            final double prevLatRad = wayPoint.getLat() * Math.PI / 180.0;
            final double prevLonRad = wayPoint.getLon() * Math.PI / 180.0;

            final double latRad = Math.asin(Math.sin(prevLatRad) * Math.cos(d / RADIUS_EARTH_METERS) + Math.cos(prevLatRad)
                  * Math.sin(d / RADIUS_EARTH_METERS) * Math.cos(brng));
            final double lonRad = prevLonRad + Math.atan2(Math.sin(brng) * Math.sin(d / RADIUS_EARTH_METERS)
                  * Math.cos(prevLatRad), Math.cos(d / RADIUS_EARTH_METERS) - Math.sin(prevLatRad) * Math.sin(latRad));

            wayPoint.setLat(((latRad * 180.0 / Math.PI)));
            wayPoint.setLon(((lonRad * 180.0 / Math.PI)));

            tile = getMapTileFromCoordinates(isMercator, wayPoint.getLat(), wayPoint.getLon(), zoomLevel);

            if (!tile.equals(prevTile)) {
              int ofsx = tile.x >= 0 ? 0 : -tile.x;
              int ofsy = tile.y >= 0 ? 0 : -tile.y;
              for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
                for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
                  final int tileY = mod(yAround, mapTileUpperBound);
                  final int tileX = mod(xAround, mapTileUpperBound);
                  result.add(MapTileIndex.getTileIndex(zoomLevel, tileX, tileY));
                }
              }
              prevTile = tile;
            }
          }
        }
      } else {
        tile = getMapTileFromCoordinates(isMercator, geoPoint.getLat(), geoPoint.getLon(), zoomLevel);
        prevTile = tile;

        int ofsx = tile.x >= 0 ? 0 : -tile.x;
        int ofsy = tile.y >= 0 ? 0 : -tile.y;
        for (int xAround = tile.x + ofsx; xAround <= tile.x + 1 + ofsx; xAround++) {
          for (int yAround = tile.y + ofsy; yAround <= tile.y + 1 + ofsy; yAround++) {
            final int tileY = mod(yAround, mapTileUpperBound);
            final int tileX = mod(xAround, mapTileUpperBound);
            result.add(MapTileIndex.getTileIndex(zoomLevel, tileX, tileY));
          }
        }
      }

      prevPoint = geoPoint;
    }
    return result;
  }
}