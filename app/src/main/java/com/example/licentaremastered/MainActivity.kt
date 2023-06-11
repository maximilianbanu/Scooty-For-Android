package com.example.licentaremastered

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaremastered.data_management.DataSyncService
import com.example.licentaremastered.data_management.OSMDataRetrievalService
import com.example.licentaremastered.location.GeoGraph
import com.example.licentaremastered.location.LocationService
import com.example.licentaremastered.scotty.ScootyConst
import com.example.licentaremastered.scotty.ScootyHelper
import com.example.licentaremastered.scotty.ScootyLogic
import com.example.licentaremastered.utils.LocationBuffer
import com.example.licentaremastered.utils.Node
import com.example.licentaremastered.utils.Route
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    var locationService = LocationService()
    var osmDataRetrievalService = OSMDataRetrievalService()
    val logicHandler = ScootyLogic(locationService, osmDataRetrievalService)
    val locationBuffer = LocationBuffer(5)
    var isJourneyStarted = false
    var isJourneyOver = false
    var locationDebugTracker = 0

    private lateinit var destination_text_box: EditText
    private lateinit var destination_button: Button
    private lateinit var destination_input: String
    private lateinit var progressBar: ProgressBar
    private lateinit var location_text_view: TextView

    private lateinit var initialIntersection: Node
    private lateinit var previousIntersection: Node
    private lateinit var currentIntersection: Node
    private lateinit var destination: Node
    private lateinit var currRoute: Route
    private lateinit var currGraph: GeoGraph

    fun launchDestinationMode() {
        GlobalScope.launch {
            while (!isJourneyOver) {
//                TESTING_ONLY
                locationService.setUserLocation(ScootyConst.locationDebugList[locationDebugTracker])
                locationBuffer.add(locationService.getUserCurrentLocation())
                locationDebugTracker++
                scottyDestinationMode()
            }
        }
    }

    fun scottyDestinationMode() {
        val currUserLocation = locationService.getUserCurrentLocation()
        locationBuffer.add(currUserLocation)

        if (!isJourneyStarted) {
            initializeJourney(currUserLocation)
        }
        else {
            currGraph.checkIntersection(currUserLocation)?.let {
                currentIntersection = it
            }
            checkJourneyState()
        }
    }


    fun initializeJourney(currUserLocation: Pair<Double, Double>) {
        if (!::initialIntersection.isInitialized) {
            currGraph = logicHandler.generateDestinationGraph()
        }
        currGraph.checkIntersection(currUserLocation)?.let {
            currentIntersection = it
        }

        if (!::currentIntersection.isInitialized) {
            return
        }
        else {
            previousIntersection = currentIntersection
            val(graph, route) = logicHandler.buildNewRoute(currGraph, currentIntersection, currGraph.getDestinationNode())
            currGraph = graph
            currRoute = route

            val nextDirection = logicHandler.getNextDirection(currGraph, currRoute, currentIntersection, locationBuffer)
            DataSyncService.sendMessageToWatch(this, nextDirection)
            currRoute.moveNext()
            isJourneyStarted = true
        }
    }

    fun checkJourneyState() {
        if (currentIntersection == previousIntersection) {
            return
        }
        else {
            if (currentIntersection.id == currRoute.last()) {
                isJourneyOver = true
                return
            }
            if (!ScootyHelper.onTrack(currentIntersection, currRoute)) {
                val(graph, route) = logicHandler.buildNewRoute(currGraph, currentIntersection, currGraph.getDestinationNode())
                currGraph = graph
                currRoute = route
            }
            val nextDirection = logicHandler.getNextDirection(currGraph, currRoute, currentIntersection, locationBuffer)
            currRoute.moveNext()
        }
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        ScootyHelper.activateScottyPrerequisites(locationService, this)

//        DISABLED FOR TESTIN
//        locationService.activateUserLocationRefresher(this)
//            FOR TESTING ONLY
        locationService.setUserLocation(44.180402250495106 to 28.6258806417701)


        destination_button = findViewById(R.id.destination_button)
        destination_text_box = findViewById(R.id.destination_text)
        location_text_view = findViewById(R.id.location_info)
        progressBar = findViewById(R.id.progressBar)


        destination_button.setOnClickListener {

            Log.d("Main Activity", "Button pressed")
            Log.d("Main Activity", "Curr Location ${locationService.getUserCurrentLocation()}")


//            DISABLED FOR TESTING
//            locationService.activateLocationRefresh(this, locationBuffer)
            destination_input = destination_text_box.text.toString()
            locationService.setDestinationLocation(this, destination_input)

            launchDestinationMode()

//            progressBar.visibility = View.VISIBLE
//            destination_button.visibility = View.GONE
//
//            progressBar.visibility = View.GONE
//
//            dataSyncService.sendMessageToWatch(this, "DEEZ NUTS")
        }

    }
}