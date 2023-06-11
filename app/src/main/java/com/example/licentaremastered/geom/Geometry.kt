package com.example.licentaremastered.geom

import kotlin.math.sqrt

object Geometry {
    fun getCartesianDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val deltaX = p1.first - p2.first
        val deltaY = p1.second - p2.second

        val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
        return distance
    }


}