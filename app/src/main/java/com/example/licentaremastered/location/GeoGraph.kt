package com.example.licentaremastered.location

import com.example.licentaremastered.geom.Geometry
import com.example.licentaremastered.utils.Node
import com.example.licentaremastered.utils.Route
import com.example.licentaremastered.utils.Way

class GeoGraph(ways: List<Way>, nodes: List<Node>) {
    private val adjacencyList: MutableMap<Long, MutableList<Long>> = mutableMapOf()
    private val nodeInfoMap: Map<Long, Node> = nodes.associateBy { it.id }
    private val wayInfoMap: Map<Long, Way> = ways.associateBy { it.id }

    private lateinit var destinationNode: Node

    init {
        buildGraph(ways)
    }

    private fun buildGraph(ways: List<Way>) {
        for (way in ways) {
            val nodeIds = way.nodes
            if (nodeIds != null && nodeIds.size >= 2) {
                for (i in 0 until nodeIds.size - 1) {
                    val source = nodeIds[i]
                    val target = nodeIds[i + 1]
                    addEdge(source, target)
                    if (way.tags["oneway"] == "no" || way.tags["oneway"] == null) addEdge(target, source)
                }
            }
        }
    }

    private fun addEdge(source: Long, target: Long) {
        adjacencyList.getOrPut(source) { mutableListOf() }.add(target)
        adjacencyList.getOrPut(target) { mutableListOf() }
    }

    private fun getNeighbors(nodeId: Long): List<Long> {
        return adjacencyList[nodeId] ?: emptyList()
    }

    fun isNodeInGraph(nodeId: Long): Boolean {
        return nodeInfoMap[nodeId] != null
    }

    fun getDestinationNode(): Node {
        return this.destinationNode
    }

    fun setDestinationNode(destinationNode: Node) {
        this.destinationNode = destinationNode
    }

    fun getNode(nodeId: Long): Node {
        return nodeInfoMap[nodeId]!!
    }

    fun findShortestPath(source: Long, target: Long): Route {

        val visited = mutableSetOf<Long>()
        val queue = ArrayDeque<Pair<Long, List<Long>>>()
        queue.add(source to listOf(source))

        while (queue.isNotEmpty()) {
            val (current, path) = queue.removeFirst()
            if (current == target) {
                return Route(path)
            }

            visited.add(current)
            val neighbors = this.getNeighbors(current)

            for (neighbor in neighbors) {
                if (neighbor !in visited) {
                    queue.add(neighbor to (path + neighbor))
                }
            }
        }

        return Route(emptyList())
    }

    fun closestNodeToLocation(location: Pair<Double, Double>): Node {
        var closestNodeId : Long = -1
        var minDistance = 100.00

        for ((nodeId, node) in nodeInfoMap) {
            val distance = Geometry.getCartesianDistance(location, node.lat to node.lon)
            if (distance < minDistance) {
                minDistance = distance
                closestNodeId = nodeId
            }
        }
        return nodeInfoMap[closestNodeId]!!

    }

    fun checkIntersection(location: Pair<Double, Double>) :Node? {
        val closestNode = closestNodeToLocation(location)

        return if (GeoUtils.isInNodeProximity(location, closestNode)) {
            closestNode
        } else
            null
    }

    fun printGraph() {
        for ((node, neighbors) in adjacencyList) {
            val neighborsString = neighbors.joinToString(", ")
            println("Node $node: $neighborsString")
        }
    }
}