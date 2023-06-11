package com.example.licentaremastered.scotty

import android.content.Context
import com.example.licentaremastered.location.LocationService
import com.example.licentaremastered.utils.Node
import com.example.licentaremastered.utils.Route
import com.example.licentaremastered.utils.Way

object ScootyHelper {

    fun activateScottyPrerequisites(locationService: LocationService, context: Context) {
        locationService.activateLocationClient(context)
        locationService.requestLocationPermission(context)
    }

    fun generateBoundingBox(boxCenter: Pair<Double, Double>, boxSize: Pair<Double, Double>): Array<Double> {

        var boundingBoxArray = arrayOf<Double>()

        boundingBoxArray += boxCenter.second - boxSize.second - ScootyConst.POINT_PROXIMITY
        boundingBoxArray += boxCenter.first - boxSize.first - ScootyConst.POINT_PROXIMITY
        boundingBoxArray += boxCenter.second + boxSize.second + ScootyConst.POINT_PROXIMITY
        boundingBoxArray += boxCenter.first + boxSize.first + ScootyConst.POINT_PROXIMITY

        return boundingBoxArray
    }

    fun findIntersections(wayList: MutableList<Way>): MutableList<Long> {
        val nodeCounts = hashMapOf<Long, Int>()
        val nodeIds = mutableListOf<Long>()

        for (way in wayList) {
            for (nodeId in way.nodes!!) {
                val count = nodeCounts.getOrDefault(nodeId, 0)
                nodeCounts[nodeId] = count + 1
                if (count == 1) {
                    nodeIds.add(nodeId)
                }
            }
        }
        return nodeIds
    }

    fun filterWays(wayList: MutableList<Way>, nodeIds: MutableList<Long>): List<Way> {
        val nodeIdsSet = nodeIds.toSet()
        return wayList.mapNotNull { way ->
            val filteredNodes = way.nodes?.filter { nodeId ->
                nodeId in nodeIdsSet
            }
            if (filteredNodes?.isNotEmpty() == true) {
                way.copy(nodes = filteredNodes)
            } else {
                null
            }
        }
    }

    fun onTrack(currIntersection: Node, currRoute: Route) : Boolean {
        return currIntersection.id == currRoute.getCurrentNodeId()
    }

}