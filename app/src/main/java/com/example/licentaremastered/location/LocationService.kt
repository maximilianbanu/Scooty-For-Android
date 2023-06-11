package com.example.licentaremastered.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.licentaremastered.scotty.ScootyConst
import com.example.licentaremastered.utils.LocationBuffer
import com.example.licentaremastered.utils.Node
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class LocationService {

    private var currUserLocation: Pair<Double, Double> = 0.00 to 0.00
    private var currDestinationLocation: Pair<Double, Double> = 0.00 to 0.00

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private var locationRefresh = false

    fun requestLocationPermission(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(context as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ScootyConst.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    fun activateLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getUserLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ScootyConst.LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        if (!this::fusedLocationClient.isInitialized) {
            activateLocationClient(context)
        }
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    setUserLocation(location.latitude to location.longitude)
                }

            }
    }

    fun setDestinationLocation(context: Context, destinationName: String) {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocationName(destinationName, 1)
        val address = addresses?.get(0)
        if (address != null) {
            Log.d("MainActivity", address.getAddressLine(0))
            currDestinationLocation = Pair(address.latitude, address.longitude)
            Log.d("MainActivity", currDestinationLocation.toString())
        }
    }

    fun getDestinationLocation(): Pair<Double, Double> {
        return currDestinationLocation
    }

    fun activateUserLocationRefresher(context: Context, locationBuffer: LocationBuffer) {
        GlobalScope.launch {
            while (locationRefresh) {
                getUserLocation(context)
                locationBuffer.add(getUserCurrentLocation())
                Log.d("Main Activity", "New Location ${getUserCurrentLocation()}")
                delay(1000)
            }
        }
    }

    fun activateLocationRefresh(context: Context, locationBuffer: LocationBuffer) {
        if (!locationRefresh) {
            locationRefresh = true
            activateUserLocationRefresher(context, locationBuffer)
        }
    }

    fun deactivateLocationRefresh() {
        locationRefresh = false
    }

    fun setUserLocation(location: Pair<Double, Double>) {
        currUserLocation = location
    }

    fun getUserCurrentLocation(): Pair<Double, Double> {
        return this.currUserLocation
    }
}