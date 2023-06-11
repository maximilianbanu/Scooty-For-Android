package com.example.licentaremastered.scotty

import android.util.Log
import com.example.licentaremastered.location.LocationService
import com.example.licentaremastered.data_management.OSMDataRetrievalService
import com.example.licentaremastered.location.Compass
import com.example.licentaremastered.location.GeoGraph
import com.example.licentaremastered.location.GeoUtils
import com.example.licentaremastered.utils.LocationBuffer
import com.example.licentaremastered.utils.Node
import com.example.licentaremastered.utils.Route
import kotlin.math.abs

class ScootyLogic (private val locationService: LocationService, private val osmService: OSMDataRetrievalService){

    fun generateUserAreaGraph(): GeoGraph {

        val currLocation: Pair<Double, Double> = locationService.getUserCurrentLocation()
        val boundingBoxArray: Array<Double> = ScootyHelper.generateBoundingBox(currLocation,  0.00 to 0.00)

        val currAreaGraph = osmService.getAreaInfo(boundingBoxArray)


        Log.d("Main Activity", currAreaGraph.findShortestPath(148423455, 2469714249).toString())

        currAreaGraph.printGraph()

        return currAreaGraph

    }

    fun generateDestinationGraph(): GeoGraph {
        val currLocation: Pair<Double, Double> = locationService.getUserCurrentLocation()
        val currDestinationLocation: Pair<Double, Double> = locationService.getDestinationLocation()

        val middleLocation = GeoUtils.getMiddlePoint(currLocation, currDestinationLocation)

        val latRange = abs(currDestinationLocation.first - currLocation.first) / 1.5 + 0.0002
        val lonRange = abs(currDestinationLocation.second - currLocation.second) / 1.5 + 0.0002

        val boundingBoxArray: Array<Double> =
            ScootyHelper.generateBoundingBox(middleLocation, latRange to lonRange)

        val destinationGraph = osmService.getAreaInfo(boundingBoxArray)

        destinationGraph.setDestinationNode(destinationGraph.closestNodeToLocation(currDestinationLocation))

        return destinationGraph

    }

    fun buildNewRoute(currDestinationGraph: GeoGraph, currIntersectionNode: Node, destinationNode: Node): Pair<GeoGraph, Route> {
        return if (currDestinationGraph.isNodeInGraph(currIntersectionNode.id)) {
            currDestinationGraph to currDestinationGraph.findShortestPath(currIntersectionNode.id, destinationNode.id)
        } else {
            val newDestinationGraph = generateDestinationGraph()
            newDestinationGraph to newDestinationGraph.findShortestPath(currIntersectionNode.id, destinationNode.id)
        }
    }

    fun getNextDirection(currDestinationGraph: GeoGraph, currRoute: Route, currIntersectionNode: Node, currLocationBuffer: LocationBuffer): String {
        val currIntersectionLocation = currIntersectionNode.location()
        val nextNodeLocation = currDestinationGraph.getNode(currRoute.getCurrentNodeId()).location()

        val currUserBearing = Compass.getAverageBearing(currLocationBuffer.getBuffer())!!
        val nextBearing = Compass.getAverageBearing(listOf(currIntersectionLocation, nextNodeLocation))!!

        return Compass.getDirection(currUserBearing, nextBearing)

    }

}