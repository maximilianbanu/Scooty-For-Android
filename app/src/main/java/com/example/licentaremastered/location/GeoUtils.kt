package com.example.licentaremastered.location

import com.example.licentaremastered.scotty.ScootyConst
import com.example.licentaremastered.utils.Node
import kotlin.math.abs

object GeoUtils {
    fun getMiddlePoint(location1: Pair<Double, Double>, location2: Pair<Double, Double>): Pair<Double, Double> {
        val middleLat = (location1.first + location2.first) / 2
        val middleLon = (location1.second + location2.second) / 2
        return middleLat to middleLon
    }

    fun isInNodeProximity(currLoc: Pair<Double, Double>, node: Node): Boolean {
        val intersectionProximity = ScootyConst.INTERSECT_PROXIMITY
        val (currLocX, currLocY) = currLoc
        val (nodeX, nodeY) = Pair(node.lat, node.lon)

        return (abs(nodeX - currLocX) <= intersectionProximity && abs(nodeY - currLocY) <= intersectionProximity)
    }
}