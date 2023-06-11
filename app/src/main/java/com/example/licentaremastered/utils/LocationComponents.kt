package com.example.licentaremastered.utils

import com.example.licentaremastered.geom.Geometry
import com.example.licentaremastered.scotty.ScootyConst
import java.util.ArrayDeque

data class Way(
    val id: Long = -1,
    val tags: Map<String, String> = mapOf(),
    val nodes: List<Long>? = listOf()
) {
    override fun toString(): String {
        return "WAY ID: " + id.toString() + System.lineSeparator() + "TAGS: " + tags.toString() + System.lineSeparator() + nodes.toString()  + System.lineSeparator()
    }
}

data class Node(
    val id: Long = -1,
    val lat: Double = 0.00,
    val lon: Double = 0.00
) {
    override fun toString(): String {
        return "NODE ID: " + id.toString() + System.lineSeparator() + "LOCATION: " + lat.toString() + lon.toString() + System.lineSeparator()
    }

    fun location(): Pair<Double, Double> {
        return lat to lon
    }
}

class Route(private val items: List<Long>) {
    private var currentIndex: Int = 1

    fun getCurrentNodeId(): Long {
        return items[currentIndex]
    }

    fun moveNext() {
        if (currentIndex < items.size - 1) {
            currentIndex++
        }
    }

    fun last(): Long {
        return items.last()
    }
}

class LocationBuffer(private val maxSize: Int) : ArrayDeque<Pair<Double, Double>>() {

    override fun add(element: Pair<Double, Double>): Boolean {
        if (size == 0) {
            return super.add(element)
        }
        if (size > 0  && Geometry.getCartesianDistance(last, element) > ScootyConst.BUFFERING_MIN_DISTANCE) {
            if (size >= maxSize) {
                removeFirst()
            }
            return super.add(element)
        }
        else return false
    }

    fun getBuffer(): List<Pair<Double, Double>> {
        return toList()
    }
}
