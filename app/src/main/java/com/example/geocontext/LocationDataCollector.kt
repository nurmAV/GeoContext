package com.example.geocontext

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*

/** Responsible for recording the location and making it available for
 * fragments.
 */

class LocationDataCollector: ViewModel() {

    private val TAG = "LocationDataCollector"
    val location: MutableLiveData<Location> by lazy {
        MutableLiveData<Location>()
    }

    lateinit var locationClient: FusedLocationProviderClient


    var locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if(p0 != null) {

                for(loc in p0.locations) {
                    location.setValue(loc)
                    Log.i(TAG, "Location updated: ${location.value}")
                }

            }
        }

    }

    fun getCurrentLocation(): MutableLiveData<Location> {
        return location
    }

    fun init(context: Context)  {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
         locationClient.lastLocation.addOnSuccessListener { loc: Location -> location.value = loc }

        locationClient.requestLocationUpdates(
            LocationRequest.create().setFastestInterval(10_000L)
            .setInterval(10_000L).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
            locationCallback,
            Looper.getMainLooper()
        )


    }


}

