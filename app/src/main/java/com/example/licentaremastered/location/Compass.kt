package com.example.licentaremastered.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object Compass {
    fun getBearing(point1: Pair<Double, Double>, point2: Pair<Double, Double>): Double {
        val (lat1, lon1) = point1
        val (lat2, lon2) = point2

        val (rLat1, rLon1, rLat2, rLon2) = listOf(lat1, lon1, lat2, lon2).map {
            Math.toRadians(
                it
            )
        }

        val dLon = rLon2 - rLon1

        val y = sin(dLon) * cos(rLat2)
        val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLon)

        var bearing = atan2(y, x)
        bearing = Math.toDegrees(bearing)
        bearing = (bearing + 360) % 360

        return bearing
    }

    fun getAverageBearing(locations: List<Pair<Double, Double>>): Double? {
        if (locations.size < 2) {
            return null
        }

        var totalBearing = 0.0

        for (i in 0 until locations.size - 1) {
            val bearingI = getBearing(locations[i], locations[i + 1])
            totalBearing += bearingI
        }

        val avgBearing = totalBearing / (locations.size - 1)

        return avgBearing
    }

    fun getDirection(bearing1: Double, bearing2: Double): String {
        val directions = mapOf(
            Pair(0.0, 22.5) to "Forward",
            Pair(22.5, 67.5) to "Forward Right",
            Pair(67.5, 112.5) to "Right",
            Pair(112.5, 157.5) to "Down Right",
            Pair(157.5, 202.5) to "Down",
            Pair(202.5, 247.5) to "Down Left",
            Pair(247.5, 292.5) to "Left",
            Pair(292.5, 337.5) to "Forward Left",
            Pair(337.5, 360.0) to "Forward"
        )

        val bearingDiff = (bearing2 - bearing1 + 360.0) % 360.0
        var key: Pair<Double, Double>? = null
        for (k in directions.keys) {
            if (bearingDiff >= k.first && bearingDiff < k.second) {
                key = k
                break
            }
        }
        return directions[key] ?: "Invalid bearings"
    }
}