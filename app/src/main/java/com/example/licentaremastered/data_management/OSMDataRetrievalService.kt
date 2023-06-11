package com.example.licentaremastered.data_management

import android.util.Log
import com.example.licentaremastered.scotty.ScootyHelper
import com.example.licentaremastered.location.GeoGraph
import com.example.licentaremastered.utils.Node
import com.example.licentaremastered.utils.Way
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.concurrent.CountDownLatch

class OSMDataRetrievalService() {

    private val client = OkHttpClient()

    private lateinit var intersectionNodes: List<Node>
    private lateinit var areaGraph : GeoGraph

    fun getAreaInfo(boundingBoxArray: Array<Double>): GeoGraph {
        val left = boundingBoxArray[0]
        val bottom = boundingBoxArray[1]
        val right = boundingBoxArray[2]
        val top = boundingBoxArray[3]

        val bbox = "$bottom,$left,$top,$right"

        val latch = CountDownLatch(1)

        val url =
            "https://overpass-api.de/api/interpreter?data=[out:json];way($bbox)[highway][\"highway\"!~\"footway|service\"];(._;>;);out;"
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful) {
                    areaGraph = processResponse(response)
                    latch.countDown()
                }
            }
        })

        latch.await()

        return areaGraph
    }

    private fun processResponse(response: Response): GeoGraph {

        val gson = Gson()
        val jsonResponse = response.body?.string()

        // Process the Overpass API response here
        val nodes = mutableListOf<JsonObject>()
        val ways = mutableListOf<JsonObject>()

        val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject
        val jsonElements = jsonObject.get("elements").asJsonArray
        for (jsonElement in jsonElements) {
            val element = jsonElement.asJsonObject
            when (element.get("type").asString) {
                "node" -> nodes.add(element)
                "way" -> ways.add(element)
            }
        }

        val nodeList = nodes.map { gson.fromJson(it, Node::class.java) }.toMutableList()
        val wayList = ways.map { gson.fromJson(it, Way::class.java) }.toMutableList()

        val nodeIds = ScootyHelper.findIntersections(wayList)
        intersectionNodes = nodeList.filter { it.id in nodeIds }

        val filteredWays = ScootyHelper.filterWays(wayList, nodeIds)

        Log.d("Main Activity", filteredWays.toString())

        return GeoGraph(filteredWays, intersectionNodes)

    }
}
